"""Spatial query routes for GIS analysis endpoints."""

import logging

from fastapi import APIRouter, HTTPException, Query

from backend.models.schemas import AnalysisResponse, SpatialQueryResponse
from backend.services.gis_service import gis_service

logger = logging.getLogger("backend.spatial")
router = APIRouter(prefix="/api", tags=["spatial"])


@router.get("/spatial-query", response_model=SpatialQueryResponse)
def spatial_query(
    dataset_path: str = Query(..., description="Path or key returned from upload endpoint."),
    operation: str = Query(..., description="buffer | nearest"),
    radius: float | None = Query(default=None, description="Buffer radius in map units."),
) -> SpatialQueryResponse:
    try:
        result = gis_service.run_spatial_query(dataset_path, operation, radius)
        logger.info("Spatial query complete: operation=%s dataset=%s", operation, dataset_path)
        return SpatialQueryResponse(operation=operation, result=result)
    except RuntimeError as exc:
        logger.error("Spatial query runtime error: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        logger.warning("Spatial query validation error: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Spatial query failed")
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
    try:
        geojson = gis_service.analyze_geojson(
            source=source,
            operation=operation,
            radius=radius,
            secondary_source=secondary_source,
        )
        feature_count = len(geojson.get("features", []))
        logger.info(
            "Analysis complete: operation=%s source=%s secondary=%s features=%s",
            operation,
            source,
            secondary_source,
            feature_count,
        )
        return AnalysisResponse(
            success=True,
            operation=operation,
            source=source,
            feature_count=feature_count,
            geojson=geojson,
        )
    except RuntimeError as exc:
        logger.error("Analysis runtime error: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        logger.warning("Analysis validation error: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Analysis failed")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {exc}") from exc
