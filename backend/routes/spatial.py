"""Spatial query routes for GIS analysis endpoints."""

import logging

from fastapi import APIRouter, HTTPException, Query, Request

from ..models.schemas import AnalysisResponse, SpatialQueryResponse
from ..services.gis_service import gis_service
from ..services.storage_service import storage_service

logger = logging.getLogger("backend.spatial")
router = APIRouter(prefix="/api", tags=["spatial"])


def _request_id(request: Request) -> str:
    return getattr(request.state, "request_id", "unknown")


@router.get("/spatial-query", response_model=SpatialQueryResponse)
def spatial_query(
    request: Request,
    dataset_path: str = Query(..., description="Path or key returned from upload endpoint."),
    operation: str = Query(..., description="buffer | nearest"),
    radius: float | None = Query(default=None, description="Buffer radius in map units."),
) -> SpatialQueryResponse:
    normalized_operation = operation.strip().lower()
    request_id = _request_id(request)
    logger.info(
        "Spatial query processing started: operation=%s dataset=%s request_id=%s",
        normalized_operation,
        dataset_path,
        request_id,
    )
    try:
        result = gis_service.run_spatial_query(dataset_path, normalized_operation, radius)
        logger.info(
            "Spatial query complete: operation=%s dataset=%s request_id=%s",
            normalized_operation,
            dataset_path,
            request_id,
        )
        return SpatialQueryResponse(operation=normalized_operation, result=result)
    except RuntimeError as exc:
        logger.error(
            "Spatial query runtime error: operation=%s request_id=%s error=%s",
            normalized_operation,
            request_id,
            exc,
        )
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        logger.warning(
            "Spatial query validation error: operation=%s request_id=%s error=%s",
            normalized_operation,
            request_id,
            exc,
        )
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception(
            "Spatial query failed: operation=%s request_id=%s",
            normalized_operation,
            request_id,
        )
        raise HTTPException(
            status_code=500,
            detail="Spatial query failed due to an unexpected server error.",
        ) from exc


@router.get("/analyze", response_model=AnalysisResponse)
def analyze(
    request: Request,
    source: str = Query(..., description="Local path or s3:// URI for the primary GeoJSON"),
    operation: str = Query(..., description="buffer | intersection | nearest"),
    radius: float | None = Query(default=None, description="Buffer radius for buffer operation"),
    secondary_source: str | None = Query(
        default=None,
        description="Second local path or s3:// URI (required for intersection/nearest)",
    ),
) -> AnalysisResponse:
    normalized_operation = operation.strip().lower()
    request_id = _request_id(request)
    logger.info(
        "Analysis processing started: operation=%s source=%s secondary=%s request_id=%s",
        normalized_operation,
        source,
        secondary_source,
        request_id,
    )
    try:
        geojson = gis_service.analyze_geojson(
            source=source,
            operation=normalized_operation,
            radius=radius,
            secondary_source=secondary_source,
        )
        feature_count = len(geojson.get("features", []))
        result_storage_key = storage_service.save_result_geojson(geojson, normalized_operation)
        logger.info(
            "Analysis complete: operation=%s source=%s secondary=%s features=%s result=%s request_id=%s",
            normalized_operation,
            source,
            secondary_source,
            feature_count,
            result_storage_key,
            request_id,
        )
        return AnalysisResponse(
            success=True,
            operation=normalized_operation,
            source=source,
            feature_count=feature_count,
            geojson=geojson,
            result_storage_key=result_storage_key,
        )
    except RuntimeError as exc:
        logger.error(
            "Analysis runtime error: operation=%s request_id=%s error=%s",
            normalized_operation,
            request_id,
            exc,
        )
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        logger.warning(
            "Analysis validation error: operation=%s request_id=%s error=%s",
            normalized_operation,
            request_id,
            exc,
        )
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception(
            "Analysis failed: operation=%s request_id=%s",
            normalized_operation,
            request_id,
        )
        raise HTTPException(
            status_code=500,
            detail="Analysis failed due to an unexpected server error.",
        ) from exc
