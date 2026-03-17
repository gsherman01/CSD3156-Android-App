"""GIS processing services.

AWS adaptation notes:
- Logic is isolated from FastAPI routing for easy Lambda handler reuse.
- For heavy workloads, move these functions behind async jobs (Lambda + SQS / Step Functions).
"""

from __future__ import annotations

from pathlib import Path


def _load_geopandas():
    """Import GeoPandas lazily so app startup still works without GIS wheels.

    This is useful on Windows CMD + Python 3.14 where Shapely/GEOS wheels
    are not consistently available yet.
    """
    try:
        import geopandas as gpd  # type: ignore
    except Exception as exc:  # noqa: BLE001
        raise RuntimeError(
            "GeoPandas/Shapely dependencies are unavailable. "
            "Use Python 3.11/3.12 in WSL or install GEOS dev libraries, "
            "then reinstall requirements."
        ) from exc

    return gpd


class GISService:
    def summarize_geojson(self, file_path: str) -> dict:
        gpd = _load_geopandas()
        gdf = gpd.read_file(file_path)
        return {
            "feature_count": int(len(gdf)),
            "columns": [c for c in gdf.columns if c != "geometry"],
            "crs": str(gdf.crs),
            "bounds": gdf.total_bounds.tolist() if len(gdf) else None,
        }

    def run_spatial_query(self, file_path: str, operation: str, radius: float | None) -> dict:
        gpd = _load_geopandas()
        gdf = gpd.read_file(file_path)

        if operation == "buffer":
            if radius is None:
                raise ValueError("radius is required for buffer operation")
            buffered = gdf.copy()
            buffered["geometry"] = buffered.geometry.buffer(radius)
            return {
                "feature_count": int(len(buffered)),
                "preview_geojson": buffered.head(10).to_json(),
            }

        if operation == "nearest":
            centroids = gdf.geometry.centroid
            if len(centroids) < 2:
                return {"message": "Need at least 2 features for nearest distance."}

            first = centroids.iloc[0]
            distances = centroids.distance(first)
            nearest_idx = distances[distances > 0].idxmin()
            return {
                "from_index": int(0),
                "nearest_index": int(nearest_idx),
                "distance": float(distances.loc[nearest_idx]),
            }

        raise ValueError(f"Unsupported operation: {operation}")


# Ensure default upload path exists in local mode.
Path("data/uploads").mkdir(parents=True, exist_ok=True)

gis_service = GISService()
