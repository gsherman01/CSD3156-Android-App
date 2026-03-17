"""Storage abstraction for local mode and optional AWS S3 mode."""

from __future__ import annotations

import logging
from datetime import datetime, timezone
from pathlib import Path

import boto3
from fastapi import UploadFile

from backend.config import settings

logger = logging.getLogger("backend.storage")


class StorageService:
    """Handles where uploaded files are saved (disk vs S3)."""

    async def save_upload(self, file: UploadFile) -> str:
        if settings.storage_provider == "aws":
            return await self._save_to_s3(file)
        return await self._save_to_local(file)

    async def _save_to_local(self, file: UploadFile) -> str:
        """Render/local mode using env-defined upload path (no hardcoded absolute path)."""
        upload_dir = Path(settings.upload_dir)
        upload_dir.mkdir(parents=True, exist_ok=True)
        safe_name = Path(file.filename or "upload.geojson").name
        destination = upload_dir / safe_name

        contents = await file.read()
        destination.write_bytes(contents)
        await file.seek(0)

        logger.info("Saved file locally: %s", destination)
        return str(destination)

    async def _save_to_s3(self, file: UploadFile) -> str:
        """AWS mode: upload file to S3 and return s3:// URI."""
        if not settings.s3_bucket_name:
            raise RuntimeError("S3_BUCKET_NAME is required when STORAGE_PROVIDER=aws")

        stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        safe_name = Path(file.filename or "upload.geojson").name
        key = f"uploads/{stamp}-{safe_name}"

        # Credentials are pulled from env or IAM role automatically.
        client_kwargs = {"region_name": settings.aws_region}
        if settings.aws_access_key_id and settings.aws_secret_access_key:
            client_kwargs["aws_access_key_id"] = settings.aws_access_key_id
            client_kwargs["aws_secret_access_key"] = settings.aws_secret_access_key
            if settings.aws_session_token:
                client_kwargs["aws_session_token"] = settings.aws_session_token

        s3 = boto3.client("s3", **client_kwargs)
        s3.upload_fileobj(file.file, settings.s3_bucket_name, key)
        await file.seek(0)

        uri = f"s3://{settings.s3_bucket_name}/{key}"
        logger.info("Uploaded file to S3: %s", uri)
        return uri


storage_service = StorageService()
