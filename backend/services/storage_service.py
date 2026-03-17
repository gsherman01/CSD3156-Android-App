"""Storage abstraction for local mode and future AWS S3 mode.

Switching strategy:
- Keep `STORAGE_PROVIDER=local` for Render/local testing.
- Set `STORAGE_PROVIDER=aws` and fill S3 env vars to migrate.
    AWS adaptation notes:
    - Local provider writes files to disk for Render/local testing.
    - AWS provider should upload to S3 using boto3 (stubbed here for skeleton use). 
"""

from __future__ import annotations

from pathlib import Path

from fastapi import UploadFile

from backend.config import settings


class StorageService:
    """Handles where uploaded files are saved (disk vs S3)."""

    async def save_upload(self, file: UploadFile) -> str:
        """Save an uploaded file and return a storage key/path."""
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
        """AWS mode placeholder.

        Replace with boto3 upload when S3 is enabled.
        """
        # Example implementation when moving to AWS:
        #
        # import boto3
        # s3 = boto3.client("s3", region_name=settings.aws_region)
        # key = f"uploads/{file.filename}"
        # s3.upload_fileobj(file.file, settings.s3_bucket_name, key)
        # await file.seek(0)
        # return f"s3://{settings.s3_bucket_name}/{key}"
        #
        return f"s3://{settings.s3_bucket_name}/{file.filename}"


storage_service = StorageService()
