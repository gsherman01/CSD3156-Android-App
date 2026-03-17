"""Upload route: accepts GeoJSON files and validates them via GeoPandas."""

import logging

from fastapi import APIRouter, File, UploadFile

from backend.models.schemas import UploadResponse
from backend.services.gis_service import gis_service
from backend.services.storage_service import storage_service

logger = logging.getLogger("backend.upload")
router = APIRouter(prefix="/api", tags=["upload"])


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
        logger.info("Upload started: %s", file.filename)
        storage_key = await storage_service.save_upload(file)
        summary = gis_service.summarize_geojson(storage_key)
        logger.info(
            "Upload processed: filename=%s storage_key=%s feature_count=%s",
            file.filename,
            storage_key,
            summary["feature_count"],
        )
        return UploadResponse(
            success=True,
            message="Upload and validation succeeded.",
            filename=file.filename,
            feature_count=summary["feature_count"],
            storage_key=storage_key,
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
