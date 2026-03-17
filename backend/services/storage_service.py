"""Storage abstraction for local mode and optional AWS S3 mode.

Switching strategy:
- Keep `STORAGE_PROVIDER=local` for Render/local testing.
- Set `STORAGE_PROVIDER=aws` and configure AWS env vars for S3 storage.

This abstraction keeps API endpoints unchanged while storage backend changes.
"""

from __future__ import annotations

from datetime import datetime, timezone
from pathlib import Path

import boto3
from fastapi import UploadFile

from backend.config import settings


class StorageService:
    """Handles where uploaded files are saved (disk vs S3)."""

    async def save_upload(self, file: UploadFile) -> str:
        """Save an uploaded file and return a storage key/path.

        Endpoint code does not need to know if this is local disk or S3.
        """
        if settings.storage_provider == "aws":
            return await self._save_to_s3(file)
        return await self._save_to_local(file)

    async def _save_to_local(self, file: UploadFile) -> str:
        """Render/local mode: persist file under `UPLOAD_DIR`."""
        upload_dir = Path(settings.upload_dir)
        upload_dir.mkdir(parents=True, exist_ok=True)
        destination = upload_dir / file.filename

        contents = await file.read()
        destination.write_bytes(contents)
        await file.seek(0)

        return str(destination)

    async def _save_to_s3(self, file: UploadFile) -> str:
        """AWS mode: upload file to S3 and return s3:// URI.

        To switch to AWS storage:
        1. STORAGE_PROVIDER=aws
        2. Set AWS_REGION and S3_BUCKET_NAME
        3. Ensure credentials/IAM permissions include PutObject/GetObject
        """
        if not settings.s3_bucket_name:
            raise RuntimeError("S3_BUCKET_NAME is required when STORAGE_PROVIDER=aws")

        # Prefix uploads by UTC timestamp to avoid filename collisions.
        stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        safe_name = Path(file.filename or "upload.geojson").name
        key = f"uploads/{stamp}-{safe_name}"

        s3 = boto3.client("s3", region_name=settings.aws_region)
        s3.upload_fileobj(file.file, settings.s3_bucket_name, key)
        await file.seek(0)

        return f"s3://{settings.s3_bucket_name}/{key}"


storage_service = StorageService()
