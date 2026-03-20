"""GIS processing services.

AWS adaptation notes:
- Logic is isolated from FastAPI routing for easy Lambda handler reuse.
- For heavy workloads, move these functions behind async jobs (Lambda + SQS / Step Functions).
"""

from __future__ import annotations

import logging
from pathlib import Path
from tempfile import NamedTemporaryFile

from ..config import settings

logger = logging.getLogger("backend.gis")


def _load_geopandas():
    """Import GeoPandas lazily so app startup still works without GIS wheels."""
    import geopandas as gpd  # type: ignore

    return gpd


class GISService:
    """All GIS operations used by API routes."""

    def _read_geojson_source(self, source: str):
        """Read a GeoJSON source from local path or S3 URI."""
        gpd = _load_geopandas()

        if source.startswith("s3://"):
            import boto3

            bucket, key = source.removeprefix("s3://").split("/", 1)
            client_kwargs = {"region_name": settings.aws_region}
            if settings.aws_access_key_id and settings.aws_secret_access_key:
                client_kwargs["aws_access_key_id"] = settings.aws_access_key_id
                client_kwargs["aws_secret_access_key"] = settings.aws_secret_access_key
                if settings.aws_session_token:
                    client_kwargs["aws_session_token"] = settings.aws_session_token

            s3 = boto3.client("s3", **client_kwargs)
            with NamedTemporaryFile(suffix=".geojson", delete=True) as tmp:
                s3.download_file(bucket, key, tmp.name)
                logger.info("Downloaded source from S3: s3://%s/%s", bucket, key)
                return gpd.read_file(tmp.name)

        logger.info("Reading local GeoJSON source: %s", source)
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


Path(settings.upload_dir).mkdir(parents=True, exist_ok=True)

gis_service = GISService()
