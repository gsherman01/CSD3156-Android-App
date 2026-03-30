"""Storage abstraction for local mode and optional AWS S3 mode."""

from __future__ import annotations

import json
import logging
from datetime import datetime, timezone
from pathlib import Path
from uuid import uuid4

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from fastapi import UploadFile

from ..config import settings

logger = logging.getLogger("backend.storage")


class StorageService:
    """Handles where uploaded files and analysis results are saved."""

    def get_mode(self) -> str:
        """Return the current storage mode using user-facing names."""
        return "s3" if settings.storage_provider == "aws" else "local"

    def is_available(self) -> bool:
        """Check whether the configured storage backend is reachable or writable."""
        if settings.storage_provider == "aws":
            return self._is_s3_available()
        return self._is_local_storage_available()

    async def save_upload(self, file: UploadFile) -> str:
        if settings.storage_provider == "aws":
            return await self._save_to_s3(file, prefix="uploads")
        return await self._save_to_local(file, settings.upload_dir)

    def delete_file(self, storage_key: str) -> None:
        if not storage_key:
            return

        try:
            if storage_key.startswith("s3://"):
                bucket, key = storage_key.removeprefix("s3://").split("/", 1)
                self._build_s3_client().delete_object(Bucket=bucket, Key=key)
                logger.info("Deleted file from S3: %s", storage_key)
                return

            path = Path(storage_key)
            if path.exists():
                path.unlink()
                logger.info("Deleted local file: %s", storage_key)
        except (BotoCoreError, ClientError, OSError, ValueError) as exc:
            logger.warning("Failed to delete stored file %s: %s", storage_key, exc)

    def save_result_geojson(self, geojson: dict, operation: str) -> str:
        if settings.storage_provider == "aws":
            return self._save_result_to_s3(geojson, operation)
        return self._save_result_to_local(geojson, operation)

    async def _save_to_local(self, file: UploadFile, base_dir: str) -> str:
        target_dir = Path(base_dir)
        target_dir.mkdir(parents=True, exist_ok=True)
        safe_name = Path(file.filename or "upload.geojson").name
        destination = target_dir / safe_name

        contents = await file.read()
        destination.write_bytes(contents)
        await file.seek(0)

        logger.info("Saved file locally: %s", destination)
        return str(destination)

    def _save_result_to_local(self, geojson: dict, operation: str) -> str:
        target_dir = Path(settings.results_dir)
        target_dir.mkdir(parents=True, exist_ok=True)
        stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        destination = target_dir / f"{operation}-{stamp}.geojson"
        destination.write_text(json.dumps(geojson), encoding="utf-8")
        logger.info("Saved result locally: %s", destination)
        return str(destination)

    async def _save_to_s3(self, file: UploadFile, prefix: str) -> str:
        if not settings.s3_bucket_name:
            raise RuntimeError("S3_BUCKET_NAME is required when STORAGE_PROVIDER=aws")

        stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        safe_name = Path(file.filename or "upload.geojson").name
        key = f"{prefix}/{stamp}-{safe_name}"

        client_kwargs = {"region_name": settings.aws_region}
        if settings.aws_access_key_id and settings.aws_secret_access_key:
            client_kwargs["aws_access_key_id"] = settings.aws_access_key_id
            client_kwargs["aws_secret_access_key"] = settings.aws_secret_access_key
            if settings.aws_session_token:
                client_kwargs["aws_session_token"] = settings.aws_session_token

        contents = await file.read()

        s3 = boto3.client("s3", **client_kwargs)
        s3.put_object(
            Bucket=settings.s3_bucket_name,
            Key=key,
            Body=contents,
            ContentType=file.content_type or "application/geo+json",
        )

        # Best effort rewind for any downstream handlers.
        try:
            await file.seek(0)
        except ValueError:
            logger.warning("Upload file stream already closed after S3 upload: %s", file.filename)

        uri = f"s3://{settings.s3_bucket_name}/{key}"
        logger.info("Uploaded file to S3: %s", uri)
        return uri

    def _save_result_to_s3(self, geojson: dict, operation: str) -> str:
        if not settings.s3_bucket_name:
            raise RuntimeError("S3_BUCKET_NAME is required when STORAGE_PROVIDER=aws")

        stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        key = f"results/{operation}-{stamp}.geojson"

        s3 = self._build_s3_client()
        s3.put_object(
            Bucket=settings.s3_bucket_name,
            Key=key,
            Body=json.dumps(geojson).encode("utf-8"),
            ContentType="application/geo+json",
        )
        uri = f"s3://{settings.s3_bucket_name}/{key}"
        logger.info("Uploaded result to S3: %s", uri)
        return uri

    def _build_s3_client(self):
        client_kwargs = {"region_name": settings.aws_region}
        if settings.aws_access_key_id and settings.aws_secret_access_key:
            client_kwargs["aws_access_key_id"] = settings.aws_access_key_id
            client_kwargs["aws_secret_access_key"] = settings.aws_secret_access_key
            if settings.aws_session_token:
                client_kwargs["aws_session_token"] = settings.aws_session_token
        return boto3.client("s3", **client_kwargs)

    def _is_local_storage_available(self) -> bool:
        paths_to_check = (
            Path(settings.upload_dir),
            Path(settings.results_dir),
            Path(settings.metadata_registry_path).parent,
        )

        try:
            for path in paths_to_check:
                path.mkdir(parents=True, exist_ok=True)
                probe = path / f".healthcheck-{uuid4().hex}.tmp"
                probe.write_text("ok", encoding="utf-8")
                probe.unlink()
            return True
        except OSError as exc:
            logger.warning("Local storage unavailable: %s", exc)
            return False

    def _is_s3_available(self) -> bool:
        if not settings.s3_bucket_name:
            logger.warning("S3 storage unavailable: missing bucket configuration")
            return False

        try:
            self._build_s3_client().head_bucket(Bucket=settings.s3_bucket_name)
            return True
        except (BotoCoreError, ClientError) as exc:
            logger.warning("S3 storage unavailable: %s", exc)
            return False


storage_service = StorageService()
