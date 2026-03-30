"""Upload route: accepts GeoJSON files and validates them via GeoPandas."""

import json
import logging
from json import JSONDecodeError

from fastapi import APIRouter, File, Request, UploadFile, status
from fastapi.responses import JSONResponse

from ..models.schemas import UploadResponse
from ..services.dataset_registry import registry
from ..services.gis_service import gis_service
from ..services.storage_service import storage_service

logger = logging.getLogger("backend.upload")
router = APIRouter(prefix="/api", tags=["upload"])
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5MB limit for Lambda/API Gateway friendliness.


def _request_id(request: Request) -> str:
    return getattr(request.state, "request_id", "unknown")


def upload_error_response(
    status_code: int,
    *,
    message: str,
    filename: str,
    storage_key: str | None = None,
) -> JSONResponse:
    """Return upload errors with both proper HTTP status and frontend-friendly JSON."""
    payload = UploadResponse(
        success=False,
        message=message,
        filename=filename,
        storage_key=storage_key,
    )
    return JSONResponse(status_code=status_code, content=payload.model_dump())


@router.get("/datasets", response_model=None)
def list_datasets(request: Request):
    """List all uploaded datasets with metadata."""
    request_id = _request_id(request)
    logger.info("Dataset list processing started request_id=%s", request_id)
    try:
        datasets = registry._read_all()
        logger.info("Dataset list complete: count=%d request_id=%s", len(datasets), request_id)
        return {
            "success": True,
            "count": len(datasets),
            "datasets": datasets,
        }
    except Exception as exc:  # noqa: BLE001
        logger.exception("Dataset list failed request_id=%s", request_id)
        return JSONResponse(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            content={
                "success": False,
                "count": 0,
                "datasets": [],
                "error": str(exc),
            },
        )


@router.post(
    "/upload",
    response_model=UploadResponse,
    responses={
        400: {"model": UploadResponse},
        413: {"model": UploadResponse},
        500: {"model": UploadResponse},
        503: {"model": UploadResponse},
    },
)
async def upload_geojson(
    request: Request,
    file: UploadFile = File(...),
) -> UploadResponse | JSONResponse:
    request_id = _request_id(request)
    logger.info("Upload processing started: filename=%s request_id=%s", file.filename, request_id)
    if not file.filename or not file.filename.lower().endswith((".geojson", ".json")):
        logger.warning(
            "Rejected upload with unsupported extension: %s request_id=%s",
            file.filename,
            request_id,
        )
        return upload_error_response(
            status.HTTP_400_BAD_REQUEST,
            message="Only GeoJSON files are supported.",
            filename=file.filename or "unknown",
        )

    storage_key: str | None = None

    try:
        file_contents = await file.read()
        if not file_contents.strip():
            logger.warning("Rejected empty upload: %s request_id=%s", file.filename, request_id)
            return upload_error_response(
                status.HTTP_400_BAD_REQUEST,
                message="Uploaded file is empty.",
                filename=file.filename or "unknown",
            )

        try:
            json.loads(file_contents)
        except JSONDecodeError:
            logger.warning("Rejected invalid JSON upload: %s request_id=%s", file.filename, request_id)
            return upload_error_response(
                status.HTTP_400_BAD_REQUEST,
                message="Uploaded file is not valid JSON/GeoJSON.",
                filename=file.filename or "unknown",
            )

        if len(file_contents) > MAX_FILE_SIZE:
            logger.warning(
                "File too large: %s (%d bytes) request_id=%s",
                file.filename,
                len(file_contents),
                request_id,
            )
            return upload_error_response(
                status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                message=f"File too large. Maximum size is {MAX_FILE_SIZE / (1024 * 1024):.0f}MB.",
                filename=file.filename,
            )
        await file.seek(0)
    except Exception as exc:  # noqa: BLE001
        logger.error("Error reading file %s request_id=%s: %s", file.filename, request_id, exc)
        return upload_error_response(
            status.HTTP_500_INTERNAL_SERVER_ERROR,
            message="Error reading uploaded file.",
            filename=file.filename or "unknown",
        )

    try:
        storage_key = await storage_service.save_upload(file)
        summary = gis_service.summarize_geojson(storage_key)
        dataset_id = registry.create_dataset_record(file.filename, storage_key, summary)
        logger.info(
            "Upload complete: filename=%s storage_key=%s feature_count=%s dataset_id=%s request_id=%s",
            file.filename,
            storage_key,
            summary["feature_count"],
            dataset_id,
            request_id,
        )
        return UploadResponse(
            success=True,
            message="Upload and validation succeeded.",
            filename=file.filename,
            feature_count=summary["feature_count"],
            storage_key=storage_key,
            dataset_id=dataset_id,
        )
    except ValueError as exc:
        logger.warning("Upload rejected for file=%s request_id=%s: %s", file.filename, request_id, exc)
        if storage_key:
            storage_service.delete_file(storage_key)
        return upload_error_response(
            status.HTTP_400_BAD_REQUEST,
            message=str(exc),
            filename=file.filename,
        )
    except RuntimeError as exc:
        logger.error("Upload failed due to runtime dependency issue request_id=%s: %s", request_id, exc)
        if storage_key:
            storage_service.delete_file(storage_key)
        return upload_error_response(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            message=str(exc),
            filename=file.filename,
        )
    except Exception as exc:  # noqa: BLE001
        logger.exception("Upload failed unexpectedly for file=%s request_id=%s", file.filename, request_id)
        if storage_key:
            storage_service.delete_file(storage_key)
        return upload_error_response(
            status.HTTP_500_INTERNAL_SERVER_ERROR,
            message="Upload failed unexpectedly while validating the GeoJSON file.",
            filename=file.filename,
        )
