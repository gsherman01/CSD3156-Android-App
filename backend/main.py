"""FastAPI entrypoint with cloud-friendly logging and error handling.
yeah quite abit of logging 

AWS adaptation notes: is this done?
- For Lambda, wrap this ASGI app with Mangum and deploy via API Gateway.
- Keep routes/services decoupled so handlers can be reused in serverless functions.
"""
import logging
import time

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles

from backend.config import settings
from backend.routes.spatial import router as spatial_router
from backend.routes.upload import router as upload_router

# Configure logging once for stdout (works well for Render/cloud logs).
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger("backend.app")

app = FastAPI(title=settings.app_name)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.middleware("http")
async def request_logging_middleware(request: Request, call_next):
    """Log request method/path/status/latency for basic monitoring."""
    start = time.perf_counter()
    response = await call_next(request)
    duration_ms = (time.perf_counter() - start) * 1000
    logger.info(
        "%s %s -> %s (%.2f ms)",
        request.method,
        request.url.path,
        response.status_code,
        duration_ms,
    )
    return response


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    """Catch-all handler so unexpected errors are logged and structured."""
    logger.exception("Unhandled error on %s %s", request.method, request.url.path)
    return JSONResponse(status_code=500, content={"detail": "Internal server error"})


app.include_router(upload_router)
app.include_router(spatial_router)

# Keep static hosting local-friendly; on AWS this can move to S3/CloudFront.
app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "app": settings.app_name, "env": settings.app_env}
