"""Spatial query routes for GIS analysis endpoints."""

from fastapi import APIRouter, HTTPException, Query

from backend.models.schemas import AnalysisResponse, SpatialQueryResponse
from backend.services.gis_service import gis_service

router = APIRouter(prefix="/api", tags=["spatial"])


@router.get("/spatial-query", response_model=SpatialQueryResponse)
def spatial_query(
    dataset_path: str = Query(..., description="Path or key returned from upload endpoint."),
    operation: str = Query(..., description="buffer | nearest"),
    radius: float | None = Query(default=None, description="Buffer radius in map units."),
) -> SpatialQueryResponse:
    """Run a lightweight spatial operation over a previously uploaded dataset."""
    try:
        result = gis_service.run_spatial_query(dataset_path, operation, radius)
        return SpatialQueryResponse(operation=operation, result=result)
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Spatial query failed: {exc}") from exc


@router.get("/analyze", response_model=AnalysisResponse)
def analyze(
    source: str = Query(..., description="Local path or s3:// URI for the primary GeoJSON"),
    operation: str = Query(..., description="buffer | intersection | nearest"),
    radius: float | None = Query(default=None, description="Buffer radius for buffer operation"),
    secondary_source: str | None = Query(
        default=None,
        description="Second local path or s3:// URI (required for intersection/nearest)",
    ),
) -> AnalysisResponse:
    """Perform GIS analysis and return GeoJSON for frontend map rendering.

    AWS Lambda hook:
    - This route only orchestrates HTTP concerns.
    - `gis_service.analyze_geojson` can be reused in a Lambda handler directly.
    """
    try:
        geojson = gis_service.analyze_geojson(
            source=source,
            operation=operation,
            radius=radius,
            secondary_source=secondary_source,
        )
        return AnalysisResponse(
            success=True,
            operation=operation,
            source=source,
            feature_count=len(geojson.get("features", [])),
            geojson=geojson,
        )
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Analysis failed: {exc}") from exc
