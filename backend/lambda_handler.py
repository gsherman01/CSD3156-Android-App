"""AWS Lambda entrypoint.

This file lets API Gateway invoke the FastAPI app through Mangum.
    Used when deploying FastAPI behind API Gateway + Lambda.
"""

from mangum import Mangum

from backend.main import app

# Exported handler name expected by AWS Lambda runtime configuration.
handler = Mangum(app)
