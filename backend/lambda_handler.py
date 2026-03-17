"""AWS Lambda entrypoint.

Used when deploying FastAPI behind API Gateway + Lambda.
"""

from mangum import Mangum

from backend.main import app

handler = Mangum(app)
