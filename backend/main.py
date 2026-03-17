"""FastAPI entrypoint.

AWS adaptation notes:
- For Lambda, wrap this ASGI app with Mangum and deploy via API Gateway.
- Keep routes/services decoupled so handlers can be reused in serverless functions.
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from backend.config import settings
from backend.routes.spatial import router as spatial_router
from backend.routes.upload import router as upload_router

# App object consumed by Uvicorn locally and by Mangum in Lambda mode.
app = FastAPI(title=settings.app_name)

# CORS is open for development. Restrict origins before production release.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register API modules.
app.include_router(upload_router)
app.include_router(spatial_router)

# Local static hosting for frontend. In AWS, this can be served by S3/CloudFront.
app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")


@app.get("/health")
def health() -> dict:
    """Simple liveness endpoint for platform health checks."""
    return {"status": "ok", "app": settings.app_name}
