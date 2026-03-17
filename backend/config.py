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

    # Storage mode: local | aws
    storage_provider: str = "local"
    upload_dir: str = "data/uploads"

    # AWS settings (safe defaults for local)
    aws_region: str = "ap-southeast-1"
    s3_bucket_name: str = ""
    aws_access_key_id: str = ""
    aws_secret_access_key: str = ""
    aws_session_token: str = ""

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")


settings = Settings()
