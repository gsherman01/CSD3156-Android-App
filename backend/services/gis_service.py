"""GIS processing services.

AWS adaptation notes:
- Logic is isolated from FastAPI routing for easy Lambda handler reuse.
- For heavy workloads, move these functions behind async jobs (Lambda + SQS / Step Functions).
"""

from __future__ import annotations

from pathlib import Path
from tempfile import NamedTemporaryFile


def _load_geopandas():
    """Import GeoPandas lazily so app startup still works without GIS wheels."""
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
    """All GIS operations used by API routes."""

    def _read_geojson_source(self, source: str):
        """Read a GeoJSON source from local path or S3 URI.

        S3 hook:
        - In AWS mode this method can be run inside Lambda.
        - It currently downloads the object with boto3 to a temp file, then reads with GeoPandas.
        """
        gpd = _load_geopandas()

        if source.startswith("s3://"):
            # Placeholder hook for AWS migration: this flow is Lambda-friendly.
            # Ensure IAM role has s3:GetObject permission on the bucket/key.
            try:
                import boto3
            except Exception as exc:  # noqa: BLE001
                raise RuntimeError("boto3 is required to read s3:// sources.") from exc

            bucket, key = source.removeprefix("s3://").split("/", 1)
            s3 = boto3.client("s3")

            with NamedTemporaryFile(suffix=".geojson", delete=True) as tmp:
                s3.download_file(bucket, key, tmp.name)
                return gpd.read_file(tmp.name)

        return gpd.read_file(source)

    def summarize_geojson(self, file_path: str) -> dict:
        gdf = self._read_geojson_source(file_path)
        return {
            "feature_count": int(len(gdf)),
            "columns": [c for c in gdf.columns if c != "geometry"],
            "crs": str(gdf.crs),
            "bounds": gdf.total_bounds.tolist() if len(gdf) else None,
        }

    def run_spatial_query(self, file_path: str, operation: str, radius: float | None) -> dict:
        gdf = self._read_geojson_source(file_path)

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

    def analyze_geojson(
        self,
        source: str,
        operation: str,
        radius: float | None = None,
        secondary_source: str | None = None,
    ) -> dict:
        """Run GIS analysis and return GeoJSON suitable for frontend mapping.

        Supported operations:
        - buffer: uses `radius` and returns buffered geometries.
        - intersection: requires `secondary_source` and overlays both datasets.
        - nearest: creates line geometries from each feature to nearest in secondary dataset.

        AWS hook:
        - This method is pure service logic and can be called from Lambda handlers.
        """
        gpd = _load_geopandas()
        base_gdf = self._read_geojson_source(source)

        if operation == "buffer":
            if radius is None:
                raise ValueError("radius is required for buffer")
            result = base_gdf.copy()
            result["geometry"] = result.geometry.buffer(radius)
            return result.__geo_interface__

        if operation == "intersection":
            if not secondary_source:
                raise ValueError("secondary_source is required for intersection")
            other_gdf = self._read_geojson_source(secondary_source)
            if base_gdf.crs and other_gdf.crs and base_gdf.crs != other_gdf.crs:
                other_gdf = other_gdf.to_crs(base_gdf.crs)
            result = gpd.overlay(base_gdf, other_gdf, how="intersection")
            return result.__geo_interface__

        if operation == "nearest":
            if not secondary_source:
                raise ValueError("secondary_source is required for nearest")
            other_gdf = self._read_geojson_source(secondary_source)
            if base_gdf.crs and other_gdf.crs and base_gdf.crs != other_gdf.crs:
                other_gdf = other_gdf.to_crs(base_gdf.crs)

            # Build a line from each source feature centroid to nearest target centroid.
            source_centroids = base_gdf.geometry.centroid
            target_centroids = other_gdf.geometry.centroid

            from shapely.geometry import LineString

            lines = []
            for src_pt in source_centroids:
                nearest_idx = target_centroids.distance(src_pt).idxmin()
                nearest_pt = target_centroids.loc[nearest_idx]
                lines.append({"geometry": LineString([src_pt, nearest_pt])})

            line_gdf = gpd.GeoDataFrame(lines, geometry="geometry", crs=base_gdf.crs)
            return line_gdf.__geo_interface__

        raise ValueError(f"Unsupported analysis operation: {operation}")


# Ensure default upload path exists in local mode.
Path("data/uploads").mkdir(parents=True, exist_ok=True)

gis_service = GISService()
