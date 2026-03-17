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


class SpatialQueryRequest(BaseModel):
    """Example request shape for spatial operations."""

    operation: str = Field(..., description="buffer | nearest")
    radius: Optional[float] = Field(default=None, description="Required for buffer")


class SpatialQueryResponse(BaseModel):
    """Response returned by spatial query endpoint."""

    operation: str
    result: Dict[str, Any]
