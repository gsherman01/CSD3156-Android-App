"""Upload route: accepts GeoJSON files and validates them via GeoPandas."""

from fastapi import APIRouter, File, UploadFile

from backend.models.schemas import UploadResponse
from backend.services.gis_service import gis_service
from backend.services.storage_service import storage_service

router = APIRouter(prefix="/api", tags=["upload"])


@router.post("/upload", response_model=UploadResponse)
async def upload_geojson(file: UploadFile = File(...)) -> UploadResponse:
    """Handle GeoJSON upload.

    Flow:
    1. Basic extension check.
    2. Save to local path or S3 placeholder (based on config).
    3. Parse with GeoPandas to validate and summarize.
    """
    if not file.filename or not file.filename.lower().endswith((".geojson", ".json")):
        return UploadResponse(
            success=False,
            message="Only GeoJSON files are supported.",
            filename=file.filename or "unknown",
        )

    try:
        storage_key = await storage_service.save_upload(file)
        summary = gis_service.summarize_geojson(storage_key)
        return UploadResponse(
            success=True,
            message="Upload and validation succeeded.",
            filename=file.filename,
            feature_count=summary["feature_count"],
            storage_key=storage_key,
        )
    except RuntimeError as exc:
        # Triggered when GIS libs are unavailable on the host environment.
        return UploadResponse(
            success=False,
            message=str(exc),
            filename=file.filename,
            storage_key=None,
        )
    except Exception as exc:  # noqa: BLE001
        return UploadResponse(
            success=False,
            message=f"Upload failed: {exc}",
            filename=file.filename,
            storage_key=None,
        )
