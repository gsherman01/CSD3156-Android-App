"""AWS Lambda entrypoint.

This file lets API Gateway invoke the FastAPI app through Mangum.
"""

from mangum import Mangum

from backend.config import settings
from backend.main import app

# Exported handler name expected by AWS Lambda runtime configuration.
handler = Mangum(app, api_gateway_base_path=settings.api_gateway_base_path or "/")
