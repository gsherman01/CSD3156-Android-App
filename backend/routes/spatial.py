from fastapi import APIRouter, HTTPException, Query

from backend.models.schemas import SpatialQueryResponse
from backend.services.gis_service import gis_service

router = APIRouter(prefix="/api", tags=["spatial"])


@router.get("/spatial-query", response_model=SpatialQueryResponse)
def spatial_query(
    dataset_path: str = Query(..., description="Path or key returned from upload endpoint."),
    operation: str = Query(..., description="buffer | nearest"),
    radius: float | None = Query(default=None, description="Buffer radius in map units."),
) -> SpatialQueryResponse:
    try:
        result = gis_service.run_spatial_query(dataset_path, operation, radius)
        return SpatialQueryResponse(operation=operation, result=result)
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Spatial query failed: {exc}") from exc
