from fastapi import APIRouter, File, HTTPException, UploadFile

from backend.models.schemas import UploadResponse
from backend.services.gis_service import gis_service
from backend.services.storage_service import storage_service

router = APIRouter(prefix="/api", tags=["upload"])


@router.post("/upload", response_model=UploadResponse)
async def upload_geojson(file: UploadFile = File(...)) -> UploadResponse:
    if not file.filename.lower().endswith((".geojson", ".json")):
        raise HTTPException(status_code=400, detail="Only GeoJSON files are supported.")

    storage_key = await storage_service.save_upload(file)
    summary = gis_service.summarize_geojson(storage_key)

    return UploadResponse(
        filename=file.filename,
        feature_count=summary["feature_count"],
        storage_key=storage_key,
    )
