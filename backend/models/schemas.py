"""Pydantic schemas used by API routes.

These keep request/response contracts explicit and easy to validate.
"""

from typing import Any, Dict, Optional

from pydantic import BaseModel, Field


class UploadResponse(BaseModel):
    """Response for GeoJSON upload endpoint."""

    success: bool
    message: str
    filename: str
    feature_count: Optional[int] = None
    storage_key: Optional[str] = None
    dataset_id: Optional[str] = None


class SpatialQueryRequest(BaseModel):
    """Example request shape for basic spatial operations."""

    operation: str = Field(..., description="buffer | nearest")
    radius: Optional[float] = Field(default=None, description="Required for buffer")


class SpatialQueryResponse(BaseModel):
    """Response returned by legacy spatial query endpoint."""

    operation: str
    result: Dict[str, Any]


class AnalysisResponse(BaseModel):
    """GeoJSON-first response used by the richer analysis endpoint."""

    success: bool
    operation: str
    source: str
    feature_count: int
    geojson: Dict[str, Any]
    result_storage_key: Optional[str] = None
    message: str = "Analysis complete"
