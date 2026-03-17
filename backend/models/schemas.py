from typing import Any, Dict, Optional

from pydantic import BaseModel, Field


class UploadResponse(BaseModel):
    filename: str
    feature_count: int
    storage_key: str


class SpatialQueryRequest(BaseModel):
    operation: str = Field(..., description="buffer | nearest")
    radius: Optional[float] = Field(default=None, description="Required for buffer")


class SpatialQueryResponse(BaseModel):
    operation: str
    result: Dict[str, Any]
