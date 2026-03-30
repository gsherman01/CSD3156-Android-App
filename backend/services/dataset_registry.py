"""Lightweight dataset metadata registry for local/Render demo workflows.

This keeps uploaded dataset metadata in one JSON file so developers can inspect
what has been uploaded without introducing a full database yet.
"""

from __future__ import annotations

import json
import logging
from datetime import datetime, timezone
from pathlib import Path
from uuid import uuid4

from ..config import settings

logger = logging.getLogger("backend.registry")


class DatasetRegistry:
    """Stores basic dataset metadata for uploads."""

    def __init__(self) -> None:
        self.registry_path = Path(settings.metadata_registry_path)
        self._in_memory_rows: list[dict] = []

        # Lambda filesystem is read-only except /tmp. In AWS mode we avoid
        # startup writes under data/* and keep lightweight metadata in memory.
        if settings.storage_provider == "aws":
            logger.info("Dataset registry running in in-memory mode for AWS storage provider")
            return

        self.registry_path.parent.mkdir(parents=True, exist_ok=True)
        if not self.registry_path.exists():
            self.registry_path.write_text("[]", encoding="utf-8")
        self._initialize_demo_datasets()

    def _read_all(self) -> list[dict]:
        if settings.storage_provider == "aws":
            return list(self._in_memory_rows)
        return json.loads(self.registry_path.read_text(encoding="utf-8"))

    def _write_all(self, rows: list[dict]) -> None:
        if settings.storage_provider == "aws":
            self._in_memory_rows = list(rows)
            return
        self.registry_path.write_text(json.dumps(rows, indent=2), encoding="utf-8")

    def create_dataset_record(self, filename: str, storage_key: str, summary: dict) -> str:
        dataset_id = str(uuid4())
        rows = self._read_all()
        rows.append(
            {
                "dataset_id": dataset_id,
                "filename": filename,
                "storage_key": storage_key,
                "feature_count": summary.get("feature_count"),
                "crs": summary.get("crs"),
                "bounds": summary.get("bounds"),
                "uploaded_at": datetime.now(timezone.utc).isoformat(),
            }
        )
        self._write_all(rows)
        logger.info("Dataset metadata recorded: dataset_id=%s storage_key=%s", dataset_id, storage_key)
        return dataset_id

    def _initialize_demo_datasets(self) -> None:
        """Initialize demo datasets if registry is empty."""
        rows = self._read_all()
        if rows:
            return  # Already has datasets, don't re-initialize

        demo_files = [
            ("data/demo/singapore_demo_points.geojson", "Singapore MRT Stations (Demo)"),
            ("data/demo/singapore_demo_zones.geojson", "Singapore Residential Zones (Demo)"),
            ("data/demo/singapore_demo_sites.geojson", "Singapore Sites (Demo)"),
        ]

        for demo_path, demo_name in demo_files:
            if Path(demo_path).exists():
                try:
                    # Import here to avoid circular dependency
                    from .gis_service import gis_service

                    summary = gis_service.summarize_geojson(demo_path)
                    dataset_id = str(uuid4())
                    rows.append(
                        {
                            "dataset_id": dataset_id,
                            "filename": demo_name,
                            "storage_key": demo_path,
                            "feature_count": summary.get("feature_count", 0),
                            "crs": summary.get("crs"),
                            "bounds": summary.get("bounds"),
                            "uploaded_at": datetime.now(timezone.utc).isoformat(),
                            "is_demo": True,
                        }
                    )
                    logger.info("Demo dataset registered: %s", demo_name)
                except Exception as exc:  # noqa: BLE001
                    logger.warning("Failed to register demo dataset %s: %s", demo_name, exc)

        if rows:
            self._write_all(rows)


registry = DatasetRegistry()
