"""Storage abstraction for local mode and future AWS S3 mode.

AWS adaptation notes:
- Local provider writes files to disk for Render/local testing.
- AWS provider should upload to S3 using boto3 (stubbed here for skeleton use).
"""

from __future__ import annotations

from pathlib import Path

from fastapi import UploadFile

from backend.config import settings


class StorageService:
    async def save_upload(self, file: UploadFile) -> str:
        if settings.storage_provider == "aws":
            return await self._save_to_s3(file)
        return await self._save_to_local(file)

    async def _save_to_local(self, file: UploadFile) -> str:
        upload_dir = Path(settings.upload_dir)
        upload_dir.mkdir(parents=True, exist_ok=True)
        destination = upload_dir / file.filename

        contents = await file.read()
        destination.write_bytes(contents)
        await file.seek(0)

        return str(destination)

    async def _save_to_s3(self, file: UploadFile) -> str:
        # TODO(AWS): Implement boto3 upload_fileobj for Lambda/API Gateway deployment.
        # Suggested key prefix keeps uploads partitioned by dataset type/date.
        return f"s3://{settings.s3_bucket_name}/{file.filename}"


storage_service = StorageService()
