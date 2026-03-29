"""Upload route: accepts GeoJSON files and validates them via GeoPandas."""

import logging

from fastapi import APIRouter, File, UploadFile

from ..models.schemas import UploadResponse
from ..services.dataset_registry import registry
from ..services.gis_service import gis_service
from ..services.storage_service import storage_service

logger = logging.getLogger("backend.upload")
router = APIRouter(prefix="/api", tags=["upload"])
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5MB limit for Lambda/API Gateway friendliness.


@router.get("/datasets")
def list_datasets() -> dict:
    """List all uploaded datasets with metadata."""
    try:
        datasets = registry._read_all()
        logger.info("Retrieved %d datasets from registry", len(datasets))
        return {
            "success": True,
            "count": len(datasets),
            "datasets": datasets,
        }
    except Exception as exc:  # noqa: BLE001
        logger.exception("Failed to retrieve datasets")
        return {
            "success": False,
            "count": 0,
            "datasets": [],
            "error": str(exc),
        }


@router.post("/upload", response_model=UploadResponse)
async def upload_geojson(file: UploadFile = File(...)) -> UploadResponse:
    if not file.filename or not file.filename.lower().endswith((".geojson", ".json")):
        logger.warning("Rejected upload with unsupported extension: %s", file.filename)
        return UploadResponse(
            success=False,
            message="Only GeoJSON files are supported.",
            filename=file.filename or "unknown",
        )

    try:
        file_contents = await file.read()
        if len(file_contents) > MAX_FILE_SIZE:
            logger.warning("File too large: %s (%d bytes)", file.filename, len(file_contents))
            return UploadResponse(
                success=False,
                message=f"File too large. Maximum size is {MAX_FILE_SIZE / (1024 * 1024):.0f}MB.",
                filename=file.filename,
            )
        await file.seek(0)
    except Exception as exc:  # noqa: BLE001
        logger.error("Error reading file %s: %s", file.filename, exc)
        return UploadResponse(
            success=False,
            message="Error reading uploaded file.",
            filename=file.filename or "unknown",
        )

    try:
        logger.info("Upload started: %s", file.filename)
        storage_key = await storage_service.save_upload(file)
        summary = gis_service.summarize_geojson(storage_key)
        dataset_id = registry.create_dataset_record(file.filename, storage_key, summary)
        logger.info(
            "Upload processed: filename=%s storage_key=%s feature_count=%s dataset_id=%s",
            file.filename,
            storage_key,
            summary["feature_count"],
            dataset_id,
        )
        return UploadResponse(
            success=True,
            message="Upload and validation succeeded.",
            filename=file.filename,
            feature_count=summary["feature_count"],
            storage_key=storage_key,
            dataset_id=dataset_id,
        )
    except RuntimeError as exc:
        logger.error("Upload failed due to runtime dependency issue: %s", exc)
        return UploadResponse(
            success=False,
            message=str(exc),
            filename=file.filename,
            storage_key=None,
        )
    except Exception as exc:  # noqa: BLE001
        logger.exception("Upload failed unexpectedly for file=%s", file.filename)
        return UploadResponse(
            success=False,
            message=f"Upload failed: {exc}",
            filename=file.filename,
            storage_key=None,
        )
