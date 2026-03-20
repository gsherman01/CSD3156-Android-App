"""FastAPI entrypoint with cloud-friendly logging and error handling."""

import logging
import os
import time
import uuid

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles

from .config import settings
from .routes.spatial import router as spatial_router
from .routes.upload import router as upload_router

# Configure logging once for stdout (works well for Render/cloud logs).
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger("backend.app")

app = FastAPI(title=settings.app_name)

# Keep this open in development; narrow to specific origins for production/S3 hosting.
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
    request_id = str(uuid.uuid4())
    request.state.request_id = request_id
    start = time.perf_counter()
    response = await call_next(request)
    duration_ms = (time.perf_counter() - start) * 1000
    response.headers["X-Request-ID"] = request_id
    logger.info(
        "%s %s -> %s (%.2f ms) request_id=%s",
        request.method,
        request.url.path,
        response.status_code,
        duration_ms,
        request_id,
    )
    return response


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    """Catch-all handler so unexpected errors are logged and structured."""
    logger.exception(
        "Unhandled error on %s %s request_id=%s",
        request.method,
        request.url.path,
        getattr(request.state, "request_id", "unknown"),
    )
    return JSONResponse(status_code=500, content={"detail": "Internal server error"})


app.include_router(upload_router)
app.include_router(spatial_router)


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "app": settings.app_name, "env": settings.app_env}


# Static files are useful locally/Render, but should not be served by Lambda.
# Keep this mount after explicit API/health routes so it does not swallow paths like /health.
if not os.environ.get("AWS_EXECUTION_ENV"):
    try:
        app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")
    except RuntimeError:
        logger.warning("Frontend directory not found, skipping static file mount")
