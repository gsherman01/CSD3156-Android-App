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

app = FastAPI(title=settings.app_name)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # tighten in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(upload_router)
app.include_router(spatial_router)

# Serve frontend for local testing. In cloud deployments, this may move to S3/CloudFront.
app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "app": settings.app_name}
