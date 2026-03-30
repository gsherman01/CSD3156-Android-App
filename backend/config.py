"""Centralized application configuration.

Designed for:
- local execution (safe defaults)
- Render deployment (env-driven)
- future AWS migration
"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Environment-driven settings used across API and services."""

    # App/runtime
    app_name: str = "GIS Cloud API"
    app_env: str = "development"
    log_level: str = "INFO"
    host: str = "0.0.0.0"
    port: int = 8000
    api_gateway_base_path: str = ""

    # Storage mode: local | aws
    storage_provider: str = "local"
    upload_dir: str = "data/uploads"
    results_dir: str = "data/results"
    metadata_registry_path: str = "data/metadata/datasets.json"

    # AWS settings (safe defaults for local)
    aws_region: str = "ap-southeast-1"
    s3_bucket_name: str = ""
    aws_access_key_id: str = ""
    aws_secret_access_key: str = ""
    aws_session_token: str = ""

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    @property
    def validate_aws_config(self) -> None:
        """Validate required AWS settings when S3 storage is enabled."""
        if self.storage_provider == "aws":
            if not self.s3_bucket_name:
                raise ValueError("S3_BUCKET_NAME is required when STORAGE_PROVIDER=aws")
            if not self.aws_region:
                raise ValueError("AWS_REGION is required when STORAGE_PROVIDER=aws")


settings = Settings()
settings.validate_aws_config
