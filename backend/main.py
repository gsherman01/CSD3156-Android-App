"""FastAPI entrypoint with cloud-friendly logging and error handling."""

import logging
import os
import time
import uuid

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles

from .config import settings
from .routes.spatial import router as spatial_router
from .routes.upload import router as upload_router
from .services.storage_service import storage_service

# Configure logging once for stdout (works well for Render/cloud logs).
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger("backend.app")

app = FastAPI(title=settings.app_name)

# CORS configuration: Use specific origins in production for security
# In production, set CORS_ORIGINS env var to your S3 frontend URL
cors_origins = os.environ.get("CORS_ORIGINS", "").split(",") if os.environ.get("CORS_ORIGINS") else ["*"]
# Filter out empty strings
cors_origins = [origin.strip() for origin in cors_origins if origin.strip()]
if not cors_origins:
    cors_origins = ["*"]

logger.info("CORS enabled for origins: %s", cors_origins)

app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.middleware("http")
async def request_logging_middleware(request: Request, call_next):
    """Log request method/path/status/latency for basic monitoring."""
    request_id = str(uuid.uuid4())
    request.state.request_id = request_id
    logger.info(
        "Request received: method=%s path=%s query=%s request_id=%s",
        request.method,
        request.url.path,
        request.url.query or "-",
        request_id,
    )
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


@app.exception_handler(RequestValidationError)
async def request_validation_exception_handler(request: Request, exc: RequestValidationError):
    """Return simpler messages for missing or invalid request parameters."""
    messages: list[str] = []

    for error in exc.errors():
        location = error.get("loc", [])
        field_name = location[-1] if location else "request"
        location_type = location[0] if location else "request"

        if error.get("type") == "missing":
            if location_type == "query":
                messages.append(f"Missing required query parameter: {field_name}.")
            elif location_type == "body":
                messages.append(f"Missing required request body field: {field_name}.")
            else:
                messages.append(f"Missing required field: {field_name}.")
            continue

        messages.append(f"Invalid value for '{field_name}': {error.get('msg', 'Validation failed')}.")

    detail = " ".join(messages) if messages else "Request validation failed."
    logger.warning(
        "Request validation error on %s %s request_id=%s detail=%s",
        request.method,
        request.url.path,
        getattr(request.state, "request_id", "unknown"),
        detail,
    )
    return JSONResponse(status_code=422, content={"detail": detail, "errors": messages})


app.include_router(upload_router)
app.include_router(spatial_router)


@app.get("/health")
def health() -> dict:
    storage_available = storage_service.is_available()
    return {
        "status": "ok",
        "storage": "available" if storage_available else "unavailable",
        "mode": storage_service.get_mode(),
    }


# Static files are useful locally/Render, but should not be served by Lambda.
# Keep this mount after explicit API/health routes so it does not swallow paths like /health.
if not os.environ.get("AWS_EXECUTION_ENV"):
    try:
        app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")
    except RuntimeError:
        logger.warning("Frontend directory not found, skipping static file mount")
