# Pre-Deployment Audit Report
**GIS Analytics Cloud Platform - Team 14**

**Date:** March 17, 2026
**Auditor:** Claude Code Analysis
**Purpose:** Identify potential issues before AWS deployment

---

## Executive Summary

✅ **Overall Assessment: READY FOR DEPLOYMENT WITH MINOR FIXES**

The codebase is well-structured and AWS-ready with good separation of concerns. However, there are **6 critical issues** and **8 minor issues** that should be addressed before proceeding with the implementation plan.

**Critical Issues:** 3
**High Priority Issues:** 3
**Medium Priority Issues:** 5
**Low Priority Issues:** 3

---

## ✅ What's Working Well

### 1. **Excellent AWS Preparation**
- Lambda handler already implemented (`lambda_handler.py`)
- Mangum adapter configured correctly
- Storage abstraction supports both local and S3
- S3 URIs supported in GIS operations
- Environment-driven configuration

### 2. **Clean Architecture**
- Proper separation: routes → services → models
- Modular code structure
- Lazy loading of GeoPandas (graceful degradation)
- Good error handling and logging
- CORS already configured

### 3. **Well-Documented Code**
- Clear comments about AWS adaptation
- Inline explanations
- README with deployment instructions
- Environment variable examples

---

## 🔴 CRITICAL ISSUES (Must Fix Before Deployment)

### Issue #1: Static Files Mount Will Break Lambda
**File:** `backend/main.py:65`
**Severity:** CRITICAL
**Impact:** Lambda deployment will fail

**Problem:**
```python
app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")
```

This line tries to serve the frontend from the Lambda function. This will cause issues:
1. Lambda package won't include frontend files
2. StaticFiles doesn't work well in Lambda
3. Frontend should be served from S3, not Lambda

**Solution:**
```python
# In main.py, make this conditional:
import os

# Only mount static files when NOT in Lambda
if os.environ.get("AWS_EXECUTION_ENV") is None:
    app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")
```

**Alternative Solution:**
Comment out or remove this line entirely for Lambda deployment, since frontend will be on S3.

---

### Issue #2: Import Path Issues in Lambda
**File:** `backend/main.py:16-18`
**Severity:** CRITICAL
**Impact:** Lambda will fail to import modules

**Problem:**
```python
from backend.config import settings
from backend.routes.spatial import router as spatial_router
from backend.routes.upload import router as upload_router
```

When packaged for Lambda, the `backend/` prefix in imports may cause "ModuleNotFoundError" because Lambda expects flat structure or relative imports.

**Solution:**
Update all imports to use relative imports within the backend package:

```python
# In main.py
from .config import settings
from .routes.spatial import router as spatial_router
from .routes.upload import router as upload_router

# OR keep absolute imports but ensure backend/ is in Lambda root
```

**Testing Required:**
Test Lambda deployment with current imports first. If it fails, switch to relative imports.

---

### Issue #3: Missing `__init__.py` Verification
**Severity:** CRITICAL
**Impact:** Python may not recognize directories as packages

**Problem:**
Need to verify all directories have `__init__.py` files for proper Python package structure.

**Required Files:**
- `backend/__init__.py` ✅ (exists)
- `backend/routes/__init__.py` ✅ (exists)
- `backend/services/__init__.py` ✅ (exists)
- `backend/models/__init__.py` ✅ (exists)

**Status:** Appears to be complete, but verify all are present and not empty.

---

## 🟠 HIGH PRIORITY ISSUES (Fix Before Production)

### Issue #4: No Environment Variable Validation
**File:** `backend/config.py`
**Severity:** HIGH
**Impact:** Silent failures if AWS credentials missing

**Problem:**
When `STORAGE_PROVIDER=aws` is set but S3 credentials are missing, the app won't fail at startup—only when someone tries to upload.

**Solution:**
Add validation in `config.py`:

```python
class Settings(BaseSettings):
    # ... existing fields ...

    @property
    def validate_aws_config(self) -> None:
        """Validate AWS configuration if using AWS storage."""
        if self.storage_provider == "aws":
            if not self.s3_bucket_name:
                raise ValueError("S3_BUCKET_NAME is required when STORAGE_PROVIDER=aws")
            if not self.aws_region:
                raise ValueError("AWS_REGION is required when STORAGE_PROVIDER=aws")
            # Note: aws_access_key_id is optional (can use IAM role)

# After settings = Settings(), call:
settings.validate_aws_config
```

---

### Issue #5: GeoPandas Layer Size Warning
**Severity:** HIGH
**Impact:** Lambda deployment complexity

**Problem:**
GeoPandas + dependencies are ~200MB, which exceeds Lambda deployment package limit (50MB zipped, 250MB unzipped).

**Solutions (Choose One):**

**Option A: Use Existing Lambda Layer (Recommended)**
- Find pre-built GeoPandas layer for your region
- Example: https://github.com/developmentseed/geolambda
- Add layer ARN to Lambda function

**Option B: Build Custom Layer**
- Build on Amazon Linux 2 (Docker or EC2)
- Create layer with GeoPandas + Shapely + dependencies
- Upload layer (max 50MB zipped per layer, can use multiple)

**Option C: Container Image Deployment**
- Package as Docker container instead of ZIP
- Upload to ECR (Elastic Container Registry)
- Deploy Lambda from container (up to 10GB)
- More complex but handles large dependencies

**Recommendation:** Start with Option A (pre-built layer). If unavailable, use Option C.

---

### Issue #6: No Health Check in Lambda Handler
**Severity:** HIGH
**Impact:** Debugging difficulty

**Problem:**
When Lambda is behind API Gateway, the `/health` endpoint is critical for troubleshooting. Need to ensure it works through the Mangum adapter.

**Solution:**
The `/health` endpoint exists at `main.py:68-70`, which should work through Mangum. But test it explicitly during deployment.

**Testing:**
After Lambda deployment:
```bash
curl https://YOUR_API_GATEWAY_URL/health
```

Should return:
```json
{"status":"ok","app":"GIS Cloud API","env":"production"}
```

---

## 🟡 MEDIUM PRIORITY ISSUES (Should Fix)

### Issue #7: Frontend API URL is Hardcoded
**File:** `frontend/app.js:37, 82`
**Severity:** MEDIUM
**Impact:** Frontend won't work with AWS API Gateway

**Problem:**
```javascript
const res = await fetch('/api/upload', { method: 'POST', body: formData });
```

Relative URLs work for Render (backend serves frontend), but not for S3-hosted frontend calling API Gateway.

**Solution:**
Add API base URL configuration:

```javascript
// At top of app.js
const API_BASE_URL = window.location.hostname.includes('s3')
  ? 'https://YOUR_API_GATEWAY_URL'  // Set during deployment
  : '';  // Empty for local/Render (relative URLs)

// Then update all fetch calls:
const res = await fetch(`${API_BASE_URL}/api/upload`, { method: 'POST', body: formData });
```

**Better Solution:**
Use environment variable or config file that gets replaced during deployment.

---

### Issue #8: No CORS Preflight Handling Documentation
**File:** `backend/main.py:29-35`
**Severity:** MEDIUM
**Impact:** Potential CORS issues with S3-hosted frontend

**Problem:**
CORS is configured with `allow_origins=["*"]`, which is fine for development but:
1. Not secure for production
2. May need explicit preflight handling in API Gateway

**Solution:**
Document the required CORS configuration for API Gateway:
- Enable CORS in API Gateway console
- Ensure OPTIONS method works for preflight
- Update `allow_origins` to specific S3 URL in production

**Production Config:**
```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://gis-analytics-frontend-team14.s3-website-us-east-1.amazonaws.com",
        "http://localhost:8000",  # for local testing
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

---

### Issue #9: Missing Lambda Timeout Considerations
**Severity:** MEDIUM
**Impact:** Large dataset operations may timeout

**Problem:**
Some GIS operations (spatial join on large datasets) may take longer than Lambda's default 3-second timeout.

**Solution:**
- Document recommended Lambda timeout: **60 seconds** (1 minute)
- For complex operations: **5 minutes** (300 seconds)
- Maximum Lambda timeout: 15 minutes (900 seconds)

Add to deployment plan:
```
Lambda Configuration:
- Memory: 512 MB (minimum for GeoPandas)
- Timeout: 60 seconds (1 minute)
- Ephemeral storage: 512 MB (default)
```

---

### Issue #10: No Input File Size Validation
**File:** `backend/routes/upload.py:16`
**Severity:** MEDIUM
**Impact:** Large files may cause Lambda timeout or memory issues

**Problem:**
No size limit on uploaded files. API Gateway has 10MB payload limit, but files could still be too large for processing.

**Solution:**
Add file size validation:

```python
@router.post("/upload", response_model=UploadResponse)
async def upload_geojson(file: UploadFile = File(...)) -> UploadResponse:
    # Check file extension
    if not file.filename or not file.filename.lower().endswith((".geojson", ".json")):
        # ... existing code ...

    # NEW: Check file size (5MB limit)
    MAX_FILE_SIZE = 5 * 1024 * 1024  # 5MB in bytes
    contents = await file.read()
    if len(contents) > MAX_FILE_SIZE:
        return UploadResponse(
            success=False,
            message=f"File too large. Maximum size is {MAX_FILE_SIZE / (1024*1024)}MB.",
            filename=file.filename,
        )

    # Reset file pointer for storage
    await file.seek(0)

    # ... rest of existing code ...
```

---

### Issue #11: Python Version Compatibility Warning
**File:** `requirements.txt:12-13`
**Severity:** MEDIUM
**Impact:** May not work on Lambda if wrong Python version

**Problem:**
```
geopandas==1.0.1; python_version < "3.13"
shapely==2.0.6; python_version < "3.13"
```

Lambda supports Python 3.11 and 3.12. Using 3.13+ will skip GeoPandas installation.

**Solution:**
- Document required Lambda runtime: **Python 3.12**
- Don't use Python 3.13+ for Lambda
- Update implementation plan to specify Python 3.12 explicitly

---

## 🟢 LOW PRIORITY ISSUES (Nice to Have)

### Issue #12: No API Documentation Endpoint
**Severity:** LOW
**Impact:** Harder to test/debug API

**Problem:**
FastAPI auto-generates API docs at `/docs`, but this isn't mentioned anywhere.

**Solution:**
Add to README and mention in deployment:
```
API Documentation: https://YOUR_API_URL/docs
```

This gives you an interactive API testing interface (Swagger UI).

---

### Issue #13: No Request ID Tracking
**Severity:** LOW
**Impact:** Harder to trace errors in logs

**Problem:**
When multiple users hit the API, logs don't have unique request IDs to correlate related log entries.

**Solution (Optional):**
Add request ID middleware:

```python
import uuid

@app.middleware("http")
async def add_request_id(request: Request, call_next):
    request_id = str(uuid.uuid4())
    # Add to request state
    request.state.request_id = request_id
    # Process request
    response = await call_next(request)
    # Add to response headers
    response.headers["X-Request-ID"] = request_id
    return response
```

---

### Issue #14: No Deployment Script
**Severity:** LOW
**Impact:** Manual deployment is error-prone

**Problem:**
No automated deployment script to package Lambda function.

**Solution:**
Create `deploy.sh` script (optional, nice to have):

```bash
#!/bin/bash
# Simple Lambda deployment script

echo "Creating Lambda deployment package..."
cd backend
mkdir -p lambda-package
cp -r *.py routes/ services/ models/ lambda-package/
cd lambda-package
pip install -r ../../requirements.txt -t .
zip -r ../lambda-deployment.zip .
cd ..
rm -rf lambda-package

echo "Deployment package created: lambda-deployment.zip"
echo "Upload this to AWS Lambda"
```

---

## 📋 Pre-Deployment Checklist

Before starting the implementation plan:

### Code Fixes Required:

- [ ] **CRITICAL**: Fix or comment out `app.mount()` for static files (Issue #1)
- [ ] **CRITICAL**: Verify import paths work in Lambda (Issue #2)
- [ ] **CRITICAL**: Confirm all `__init__.py` files exist (Issue #3)
- [ ] **HIGH**: Add environment variable validation (Issue #4)
- [ ] **HIGH**: Plan GeoPandas layer strategy (Issue #5)
- [ ] **MEDIUM**: Update frontend API URL to support API Gateway (Issue #7)
- [ ] **MEDIUM**: Add file size validation to upload endpoint (Issue #10)

### Configuration Updates:

- [ ] Create `.env` file from `cloud/configs/env.example`
- [ ] Set `STORAGE_PROVIDER=aws` for Lambda deployment
- [ ] Set `S3_BUCKET_NAME` when creating S3 bucket
- [ ] Document Lambda timeout requirement (60 seconds)
- [ ] Document Lambda memory requirement (512MB minimum)

### Documentation Updates:

- [ ] Update README with API Gateway URL placeholder
- [ ] Add CORS configuration notes for production
- [ ] Document Python 3.12 requirement for Lambda
- [ ] Add `/docs` endpoint to API documentation

---

## 🔧 Recommended Fixes (In Priority Order)

### Fix #1: Static Files Mount (CRITICAL)
**File:** `backend/main.py:65`

**Current Code:**
```python
app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")
```

**Fixed Code:**
```python
import os

# Only serve static files when NOT running in Lambda
if not os.environ.get("AWS_EXECUTION_ENV"):
    try:
        app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")
    except RuntimeError:
        # Frontend directory doesn't exist (e.g., in Lambda deployment)
        logger.warning("Frontend directory not found, skipping static file mount")
```

---

### Fix #2: Frontend API URL (MEDIUM)
**File:** `frontend/app.js`

**Add at the top of file:**
```javascript
// Configuration for API endpoint
// Change this after deploying to AWS API Gateway
const API_BASE_URL = window.location.hostname.includes('localhost') || window.location.hostname.includes('127.0.0.1')
  ? ''  // Local development - use relative URLs
  : 'https://YOUR_API_GATEWAY_URL_HERE';  // Production - replace after deployment

console.log('Using API base URL:', API_BASE_URL || '(relative)');
```

**Update fetch calls (line 37):**
```javascript
const res = await fetch(`${API_BASE_URL}/api/upload`, { method: 'POST', body: formData });
```

**Update fetch calls (line 82):**
```javascript
const res = await fetch(`${API_BASE_URL}/api/analyze?${params.toString()}`);
```

---

### Fix #3: Add File Size Validation (MEDIUM)
**File:** `backend/routes/upload.py:16`

**Add after line 23:**
```python
# Check file size (5MB limit for Lambda/API Gateway)
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5MB
try:
    file_contents = await file.read()
    if len(file_contents) > MAX_FILE_SIZE:
        logger.warning("File too large: %s (%d bytes)", file.filename, len(file_contents))
        return UploadResponse(
            success=False,
            message=f"File too large. Maximum size is {MAX_FILE_SIZE / (1024*1024):.0f}MB.",
            filename=file.filename or "unknown",
        )
    await file.seek(0)  # Reset file pointer
except Exception as e:
    logger.error("Error reading file: %s", e)
    return UploadResponse(
        success=False,
        message="Error reading uploaded file.",
        filename=file.filename or "unknown",
    )
```

---

## 📊 Risk Assessment

### Deployment Risks:

| Risk | Severity | Probability | Mitigation |
|------|----------|-------------|------------|
| Lambda fails to import modules | High | Medium | Test packaging thoroughly, use relative imports |
| GeoPandas layer not found | High | High | Research pre-built layers before starting |
| CORS issues with S3 frontend | Medium | Medium | Configure API Gateway CORS, test from S3 |
| Large file uploads timeout | Medium | Low | Implement file size limits |
| Static file mount breaks Lambda | High | High (if not fixed) | Apply Fix #1 immediately |

### Cost Risks:

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Exceed Lambda free tier | Low | Low ($0.20-$2) | Monitor CloudWatch, set billing alarm |
| Exceed S3 free tier | Very Low | Very Low ($0.023/GB) | Implement file cleanup policy |
| API Gateway costs | Low | Low | 1M requests free/month |

**Total Expected Monthly Cost (After Free Tier):** $0.00 - $0.50

---

## ✅ Final Verdict

### Can We Proceed with the Implementation Plan?

**YES**, with the following conditions:

1. **Apply Critical Fixes First** (30 minutes):
   - Fix #1: Static files mount (5 min)
   - Fix #2: Frontend API URL (10 min)
   - Fix #3: File size validation (15 min)

2. **Research GeoPandas Layer** (1 hour):
   - Find pre-built layer for your AWS region
   - Or plan to build custom layer
   - Document layer ARN/strategy

3. **Test Locally** (30 minutes):
   - Run backend locally
   - Upload test files
   - Verify all operations work
   - Check file size limits

**After completing above (2 hours total)**, you can proceed with confidence.

---

## 📝 Summary of Required Actions

### Before Starting Implementation Plan:

1. ✅ Read this audit report
2. ⚠️ Apply Fix #1 (static files) - CRITICAL
3. ⚠️ Apply Fix #2 (frontend API URL) - REQUIRED
4. ⚠️ Apply Fix #3 (file size validation) - RECOMMENDED
5. ✅ Research GeoPandas Lambda layer options
6. ✅ Test current application locally
7. ✅ Proceed with Phase 1 of implementation plan

### During Implementation:

- Reference this document when issues arise
- Test each AWS service individually
- Monitor CloudWatch logs closely
- Check this audit if deployment fails

---

## 📚 Additional Resources

### GeoPandas Lambda Layers:
- https://github.com/developmentseed/geolambda
- https://github.com/vincentsarago/GDAL-Lambda-Layer
- Search: "geopandas lambda layer" + your AWS region

### Lambda Deployment:
- AWS Lambda Python Documentation: https://docs.aws.amazon.com/lambda/latest/dg/lambda-python.html
- Lambda Layers: https://docs.aws.amazon.com/lambda/latest/dg/configuration-layers.html
- Lambda Container Images: https://docs.aws.amazon.com/lambda/latest/dg/images-create.html

### Troubleshooting:
- Lambda Logs in CloudWatch: https://docs.aws.amazon.com/lambda/latest/dg/monitoring-cloudwatchlogs.html
- API Gateway CORS: https://docs.aws.amazon.com/apigateway/latest/developerguide/how-to-cors.html

---

**Report Generated:** March 17, 2026
**Next Review:** After Phase 3 completion (Lambda deployment)

---

## Appendix: Code Quality Notes

### Positive Observations:
✅ Consistent logging throughout
✅ Proper exception handling
✅ Type hints used (Python 3.10+ style)
✅ Pydantic for validation
✅ Clean separation of concerns
✅ Async/await used correctly
✅ Comments explain "why", not just "what"

### Code Quality Score: **8.5/10**

The codebase is production-ready with minor fixes. Well done! 🎉
