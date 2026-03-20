"""Storage abstraction for local mode and optional AWS S3 mode."""

from __future__ import annotations

import json
import logging
from datetime import datetime, timezone
from pathlib import Path

import boto3
from fastapi import UploadFile

from ..config import settings

logger = logging.getLogger("backend.storage")


class StorageService:
    """Handles where uploaded files and analysis results are saved."""

    async def save_upload(self, file: UploadFile) -> str:
        if settings.storage_provider == "aws":
            return await self._save_to_s3(file, prefix="uploads")
        return await self._save_to_local(file, settings.upload_dir)

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

        s3 = boto3.client("s3", **client_kwargs)
        s3.upload_fileobj(file.file, settings.s3_bucket_name, key)
        await file.seek(0)

        uri = f"s3://{settings.s3_bucket_name}/{key}"
        logger.info("Uploaded file to S3: %s", uri)
        return uri

    def _save_result_to_s3(self, geojson: dict, operation: str) -> str:
        if not settings.s3_bucket_name:
            raise RuntimeError("S3_BUCKET_NAME is required when STORAGE_PROVIDER=aws")

        stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        key = f"results/{operation}-{stamp}.geojson"

        client_kwargs = {"region_name": settings.aws_region}
        if settings.aws_access_key_id and settings.aws_secret_access_key:
            client_kwargs["aws_access_key_id"] = settings.aws_access_key_id
            client_kwargs["aws_secret_access_key"] = settings.aws_secret_access_key
            if settings.aws_session_token:
                client_kwargs["aws_session_token"] = settings.aws_session_token

        s3 = boto3.client("s3", **client_kwargs)
        s3.put_object(
            Bucket=settings.s3_bucket_name,
            Key=key,
            Body=json.dumps(geojson).encode("utf-8"),
            ContentType="application/geo+json",
        )
        uri = f"s3://{settings.s3_bucket_name}/{key}"
        logger.info("Uploaded result to S3: %s", uri)
        return uri


storage_service = StorageService()
