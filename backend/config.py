"""Application configuration.

AWS adaptation notes:
- Replace local defaults with secrets manager / parameter store in production.
- Keep provider switch (local|aws) so Render development can run without AWS resources.
"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "GIS Cloud API"
    storage_provider: str = "local"  # local | aws
    upload_dir: str = "data/uploads"

    # AWS placeholders for migration-ready wiring
    aws_region: str = "ap-southeast-1"
    s3_bucket_name: str = ""

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
