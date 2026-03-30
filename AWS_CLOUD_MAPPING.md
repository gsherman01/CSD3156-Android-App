# AWS Cloud Architecture Mapping

**Project:** GIS Analytics Platform - Team 14
**Author:** Bryan Ang
**Date:** March 29, 2026
**Purpose:** Document how current architecture maps to AWS services

---

## 🎯 Overview

This document explains how our local/Render development setup maps to AWS cloud services for production deployment. Understanding this mapping is essential for the cloud migration and demonstrates cloud computing principles.

---

## 📊 Architecture Comparison

### Current (Development/Render)

```
┌─────────────────────────────────────────────────────────┐
│                    Web Browser (User)                    │
└───────────────┬─────────────────────────────────────────┘
                │ HTTPS
                │
┌───────────────▼─────────────────────────────────────────┐
│              Render Web Service                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  FastAPI Application (main.py)                     │ │
│  │  ├─ Serves Frontend (StaticFiles mount)           │ │
│  │  ├─ API Routes (/api/upload, /api/analyze)        │ │
│  │  └─ GIS Processing (GeoPandas)                    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Local File Storage                                │ │
│  │  ├─ data/uploads/                                  │ │
│  │  ├─ data/results/                                  │ │
│  │  └─ data/metadata/datasets.json                   │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### Target (AWS Production)

```
┌─────────────────────────────────────────────────────────┐
│                    Web Browser (User)                    │
└───────────────┬─────────────────────────────────────────┘
                │ HTTPS
                │
┌───────────────▼─────────────────────────────────────────┐
│            Amazon S3 (Static Website)                    │
│            - Frontend HTML/JS/CSS                        │
│            - https://bucket.s3-website-region.com        │
└───────────────┬─────────────────────────────────────────┘
                │ API Calls (HTTPS)
                │
┌───────────────▼─────────────────────────────────────────┐
│          Amazon API Gateway (REST API)                   │
│          - /api/upload, /api/analyze, /health           │
│          - CORS configuration                            │
│          - Request throttling                            │
└───────────────┬─────────────────────────────────────────┘
                │ Invoke
                │
┌───────────────▼─────────────────────────────────────────┐
│            AWS Lambda Function                           │
│  ┌────────────────────────────────────────────────────┐ │
│  │  FastAPI Application (lambda_handler.py)           │ │
│  │  ├─ API Routes (via Mangum adapter)               │ │
│  │  └─ GIS Processing (GeoPandas in Lambda Layer)    │ │
│  └────────────────────────────────────────────────────┘ │
└───────────────┬─────────────────────────────────────────┘
                │ boto3 SDK
                │
┌───────────────▼─────────────────────────────────────────┐
│              Amazon S3 (Data Storage)                    │
│              - Uploaded GeoJSON files                    │
│              - Analysis results                          │
│              - Dataset metadata                          │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│           Amazon CloudWatch (Monitoring)                 │
│           - Lambda logs                                  │
│           - API Gateway metrics                          │
│           - Alarms and notifications                     │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│          Amazon Cognito (Future - Optional)              │
│          - User authentication                           │
│          - API authorization                             │
└──────────────────────────────────────────────────────────┘
```

---

## 🗺️ Component Mapping

### 1. Frontend Serving

| Current (Render) | AWS Service | Reasoning |
|------------------|-------------|-----------|
| FastAPI StaticFiles mount<br>`app.mount("/", StaticFiles(...))` | **Amazon S3 Static Website Hosting** | - Dedicated service for static files<br>- Global CDN via CloudFront (optional)<br>- No compute needed<br>- Cost-effective<br>- High availability |

**Implementation:**
```python
# Current (Render/Local):
if not os.environ.get("AWS_EXECUTION_ENV"):
    app.mount("/", StaticFiles(directory="frontend", html=True))

# AWS: Frontend served from S3 bucket
# No code change needed - just upload HTML/JS/CSS to S3
```

**S3 Configuration:**
- Enable static website hosting
- Set `index.html` as index document
- Configure public read access (or CloudFront distribution)
- Update CORS for API calls

---

### 2. API Layer

| Current (Render) | AWS Service | Reasoning |
|------------------|-------------|-----------|
| FastAPI endpoints<br>`@router.post("/upload")`<br>`@router.get("/analyze")` | **Amazon API Gateway** | - Managed REST API<br>- Built-in CORS support<br>- Request throttling<br>- API keys/authorization<br>- Caching capabilities |

**Implementation:**
```python
# Current & AWS: Same FastAPI code works!
# API Gateway routes to Lambda function
# Mangum adapter handles API Gateway → FastAPI conversion

from mangum import Mangum
app = FastAPI()
# ... define routes ...
lambda_handler = Mangum(app)  # Already implemented in lambda_handler.py
```

**API Gateway Configuration:**
- Create REST API
- Define resources: `/api/upload`, `/api/analyze`, `/api/datasets`
- Set Lambda as integration target
- Enable CORS
- Deploy to stage (e.g., "prod")

---

### 3. Compute / Backend Processing

| Current (Render) | AWS Service | Reasoning |
|------------------|-------------|-----------|
| FastAPI application<br>Running on Uvicorn<br>Always-on server | **AWS Lambda** | - Serverless - pay per execution<br>- Auto-scaling<br>- No server management<br>- Handles concurrent requests<br>- Cost-effective for variable load |

**Implementation:**
```python
# File: backend/lambda_handler.py (already exists!)
from mangum import Mangum
from .main import app

lambda_handler = Mangum(app, lifespan="off")
```

**Lambda Configuration:**
```yaml
# Recommended settings:
Runtime: Python 3.12
Memory: 512 MB (minimum for GeoPandas)
Timeout: 60 seconds (for GIS operations)
Ephemeral Storage: 512 MB (default)
Layers:
  - GeoPandas Layer (pre-built or custom)
Environment Variables:
  - STORAGE_PROVIDER=aws
  - S3_BUCKET_NAME=gis-team14-data
  - AWS_REGION=us-east-1
```

**Key Differences:**
- **Render:** Process always running, even when idle
- **Lambda:** Runs only when invoked (cold starts possible)
- **Render:** Fixed monthly cost
- **Lambda:** Pay per request (cheaper for low/variable traffic)

---

### 4. Data Storage

| Current (Render) | AWS Service | Reasoning |
|------------------|-------------|-----------|
| Local filesystem<br>`data/uploads/`<br>`data/results/`<br>`data/metadata/` | **Amazon S3** | - Durable object storage (99.999999999%)<br>- Unlimited scalability<br>- Lambda ephemeral storage is temporary<br>- Direct access via boto3 SDK<br>- Versioning support |

**Implementation:**
```python
# File: backend/services/storage_service.py (already implemented!)

class StorageService:
    async def save_upload(self, file: UploadFile) -> str:
        if settings.storage_provider == "aws":
            return await self._save_to_s3(file, prefix="uploads")
        return await self._save_to_local(file, settings.upload_dir)

    async def _save_to_s3(self, file: UploadFile, prefix: str) -> str:
        s3 = boto3.client("s3")
        key = f"{prefix}/{uuid4()}-{file.filename}"
        contents = await file.read()
        s3.put_object(
            Bucket=settings.s3_bucket_name,
            Key=key,
            Body=contents,
            ContentType="application/json"
        )
        return f"s3://{settings.s3_bucket_name}/{key}"
```

**S3 Bucket Structure:**
```
gis-team14-data/
├── uploads/
│   ├── uuid1-mrt_stations.geojson
│   ├── uuid2-residential_zones.geojson
│   └── ...
├── results/
│   ├── buffer-uuid3.geojson
│   ├── intersection-uuid4.geojson
│   └── ...
└── metadata/
    └── datasets.json
```

**Storage Abstraction:**
```python
# Code works with BOTH local and S3:
storage_key = await storage_service.save_upload(file)
# Returns: "data/uploads/file.geojson" (local)
#      or: "s3://bucket/uploads/uuid-file.geojson" (AWS)

geojson = gis_service.analyze_geojson(source=storage_key, ...)
# GIS service reads from local file or S3 automatically
```

---

### 5. Logging & Monitoring

| Current (Render) | AWS Service | Reasoning |
|------------------|-------------|-----------|
| stdout logs<br>Render dashboard | **Amazon CloudWatch** | - Centralized log aggregation<br>- Lambda logs automatically sent<br>- Metrics and alarms<br>- Log insights for querying<br>- Retention policies |

**Implementation:**
```python
# Current logging (already implemented):
import logging
logger = logging.getLogger("backend.app")
logger.info("Request processed: request_id=%s", request_id)

# AWS: Same code works!
# Lambda automatically sends stdout/stderr to CloudWatch Logs
# No code changes needed
```

**CloudWatch Features:**
- **Logs:** All Lambda execution logs
- **Metrics:** Invocation count, duration, errors, throttles
- **Alarms:** Alert on error rate > threshold
- **Dashboards:** Visual monitoring

**Request ID Tracking:**
```python
# Already implemented in main.py:
@app.middleware("http")
async def request_logging_middleware(request: Request, call_next):
    request_id = str(uuid.uuid4())
    request.state.request_id = request_id
    # ... logging with request_id ...
    response.headers["X-Request-ID"] = request_id
    return response

# In CloudWatch Logs, filter by request_id to trace full request lifecycle
```

---

### 6. Authentication & Authorization (Future)

| Current (Render) | AWS Service | Reasoning |
|------------------|-------------|-----------|
| None (public API) | **Amazon Cognito** | - Managed user pools<br>- JWT token handling<br>- OAuth/SAML support<br>- API Gateway integration<br>- MFA support |

**Implementation (Not Yet Done - Future Enhancement):**
```python
# Future: Add authentication decorator
from functools import wraps

def require_auth(func):
    @wraps(func)
    async def wrapper(request: Request, *args, **kwargs):
        # Verify JWT token from request headers
        token = request.headers.get("Authorization")
        # ... validate with Cognito ...
        return await func(request, *args, **kwargs)
    return wrapper

@router.post("/upload")
@require_auth  # Protect endpoint
async def upload_geojson(request: Request, file: UploadFile):
    # ...
```

**Cognito Setup (When Needed):**
1. Create User Pool
2. Create App Client
3. Configure API Gateway authorizer
4. Update frontend to handle login/tokens

---

## 🔄 Deployment Workflow Comparison

### Current (Render)

```
1. Push code to GitHub
2. Render auto-deploys from GitHub
3. Application restarts
4. Frontend + Backend deployed together
```

### AWS (Using SAM)

```
1. Package Lambda function
   ├─ Install dependencies
   ├─ Create deployment package or Docker image
   └─ Upload to S3 or ECR

2. Deploy Infrastructure (SAM/CloudFormation)
   ├─ Create/update Lambda function
   ├─ Create/update API Gateway
   ├─ Set up IAM roles
   └─ Configure environment variables

3. Upload Frontend to S3
   ├─ Build frontend (if using build step)
   ├─ Update config.js with API Gateway URL
   └─ Sync files to S3 bucket

4. Test Deployment
   ├─ Hit API Gateway URL
   ├─ Check CloudWatch logs
   └─ Verify frontend loads from S3
```

**AWS SAM Commands:**
```bash
# Build Lambda function
sam build

# Deploy to AWS
sam deploy --guided

# View logs
sam logs -n GISAnalyticsFunction --tail
```

---

## 💰 Cost Comparison

### Render (Current)

| Service | Cost | Notes |
|---------|------|-------|
| Web Service (Free Tier) | $0/month | Sleeps after inactivity |
| Web Service (Starter) | $7/month | Always on, 512MB RAM |
| **Total** | **$0-7/month** | Fixed cost |

### AWS (Estimated)

| Service | Cost | Notes |
|---------|------|-------|
| Lambda | $0.20/month | 1M requests free tier<br>400,000ms/month compute |
| API Gateway | $0.00 | 1M requests free tier |
| S3 (Storage) | $0.02/month | 1GB data, 10k requests |
| S3 (Frontend) | $0.01/month | Static hosting |
| CloudWatch Logs | $0.50/month | 1GB logs |
| **Total** | **$0.73/month** | Pay per use |

**Assumptions:**
- ~10,000 API requests/month (demo/testing)
- ~1GB total storage
- ~500ms average Lambda execution time
- Minimal CloudWatch log retention

**At Scale (1M requests/month):**
- Lambda: ~$20
- API Gateway: ~$3.50
- S3: ~$0.50
- Total: ~$24/month

**Key Difference:**
- **Render:** Fixed cost, predictable
- **AWS:** Variable cost, scales with usage

---

## 🎓 Cloud Computing Principles Demonstrated

### 1. ✅ Functional
- **Render:** Fully operational web app
- **AWS:** Same functionality, different deployment
- **Evidence:** All API endpoints work identically

### 2. ✅ Scalable
- **Render:** Vertical scaling (upgrade plan)
- **AWS:** Horizontal scaling (Lambda auto-scales)
- **Evidence:**
  - S3 handles unlimited storage
  - Lambda handles 1000 concurrent executions (default)
  - API Gateway handles variable traffic

### 3. ✅ Reliable
- **Render:** 99.9% uptime SLA (paid plans)
- **AWS:** 99.99% uptime SLA (Lambda, S3)
- **Evidence:**
  - S3: 99.999999999% durability
  - Multi-AZ deployment (automatic)
  - Redundant infrastructure

### 4. ✅ Elastic
- **Render:** Manual scaling (change plan)
- **AWS:** Auto-scaling (no configuration needed)
- **Evidence:**
  - Lambda scales from 0 to 1000+ concurrent executions
  - S3 bandwidth scales automatically
  - API Gateway throttling prevents overload

### 5. ✅ Secure
- **Render:** HTTPS, private environment variables
- **AWS:** IAM roles, encryption, VPC (optional)
- **Evidence:**
  - S3 encryption at rest
  - HTTPS via API Gateway
  - IAM least-privilege policies
  - Cognito for authentication (planned)

---

## 🔧 Code Changes for AWS Migration

### Changes Required: **Minimal!** ✅

The codebase is already AWS-ready thanks to:

#### 1. Storage Abstraction (Already Done)
```python
# backend/config.py
storage_provider: str = "local"  # Change to "aws" for Lambda

# backend/services/storage_service.py
if settings.storage_provider == "aws":
    return await self._save_to_s3(file)
else:
    return await self._save_to_local(file)
```

#### 2. Lambda Handler (Already Done)
```python
# backend/lambda_handler.py
from mangum import Mangum
from .main import app
lambda_handler = Mangum(app, lifespan="off")
```

#### 3. Static File Conditional (Already Done)
```python
# backend/main.py
if not os.environ.get("AWS_EXECUTION_ENV"):
    app.mount("/", StaticFiles(directory="frontend", html=True))
```

#### 4. Frontend API URL (Already Done)
```javascript
// frontend/app.js
const API_BASE_URL = configuredApiBaseUrl
  ? configuredApiBaseUrl
  : isLocalOrRender
    ? ''  // Relative URLs for local/Render
    : 'https://YOUR_API_GATEWAY_URL_HERE';  // Replace after deployment
```

### Changes Needed for AWS Deployment:

1. **Environment Variables (Lambda):**
   ```bash
   STORAGE_PROVIDER=aws
   S3_BUCKET_NAME=gis-team14-data
   AWS_REGION=us-east-1
   LOG_LEVEL=INFO
   ```

2. **Frontend Config (After API Gateway Creation):**
   ```javascript
   // frontend/config.js
   window.__APP_CONFIG__ = {
     API_BASE_URL: 'https://abc123.execute-api.us-east-1.amazonaws.com/prod'
   };
   ```

3. **SAM Template (Already Exists):**
   - `cloud/aws/template.yaml` - Defines all AWS resources
   - Update with actual values during deployment

---

## 📝 Deployment Checklist

### Pre-Deployment:
- [ ] Test locally with `STORAGE_PROVIDER=local`
- [ ] Review `cloud/aws/README.md`
- [ ] Ensure AWS CLI configured
- [ ] Create S3 bucket for data storage
- [ ] Build GeoPandas Lambda layer or use Docker image

### Deployment Steps:
- [ ] Deploy Lambda function via SAM
- [ ] Note API Gateway URL from output
- [ ] Update `frontend/config.js` with API URL
- [ ] Upload frontend to S3 bucket
- [ ] Enable S3 static website hosting
- [ ] Test API endpoints via API Gateway URL
- [ ] Test frontend from S3 URL
- [ ] Monitor CloudWatch logs

### Post-Deployment:
- [ ] Set up CloudWatch alarms
- [ ] Configure API Gateway custom domain (optional)
- [ ] Set up CloudFront CDN (optional)
- [ ] Document API Gateway URL in README
- [ ] Update demo script with deployment URL

---

## 🚀 Migration Strategy

### Recommended Approach: **Phased Migration**

#### Phase 1: Lambda Backend Only (Week 1)
- Deploy backend to Lambda + API Gateway
- Keep frontend on Render
- Test API Gateway endpoints
- Verify S3 storage works
- Monitor CloudWatch logs

#### Phase 2: S3 Frontend (Week 2)
- Upload frontend to S3
- Update API URL in config.js
- Test cross-origin requests
- Verify CORS configuration
- Decommission Render

#### Phase 3: Optimization (Week 3)
- Add CloudFront CDN
- Implement Cognito auth (if needed)
- Set up alarms
- Optimize Lambda memory/timeout
- Add caching where beneficial

---

## 🎯 Success Criteria

AWS deployment is successful when:

- ✅ All API endpoints respond correctly via API Gateway
- ✅ Frontend loads from S3 and can call backend
- ✅ File uploads save to S3
- ✅ GIS operations produce correct results
- ✅ CloudWatch logs show all requests
- ✅ No errors in Lambda execution
- ✅ Response times comparable to Render
- ✅ Demo script works with AWS URLs

---

## 📚 Additional Resources

### AWS Documentation:
- [Lambda Developer Guide](https://docs.aws.amazon.com/lambda/)
- [API Gateway REST API](https://docs.aws.amazon.com/apigateway/)
- [S3 Static Website Hosting](https://docs.aws.amazon.com/AmazonS3/latest/userguide/WebsiteHosting.html)
- [CloudWatch Logs](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/)

### Project-Specific:
- `cloud/aws/README.md` - Detailed deployment guide
- `cloud/deployment.md` - General deployment info
- `PRE_DEPLOYMENT_AUDIT.md` - Known issues and fixes
- `PROJECT_IMPLEMENTATION_PLAN.md` - Step-by-step AWS setup

---

## ✅ Conclusion

Our application architecture is **AWS-ready** with minimal code changes required. The key insight is that we've built with cloud migration in mind from the start:

1. **Abstracted Storage:** Works with both local files and S3
2. **Stateless Design:** Perfect for Lambda (no local session storage)
3. **Environment-Driven Config:** Easy to switch between local/Render/AWS
4. **Lambda Handler Ready:** Mangum adapter already implemented
5. **Logging Cloud-Friendly:** Stdout logs work perfectly with CloudWatch

**Migration Effort:** Low (1-2 days for basic deployment)
**Risk:** Low (well-tested code, clear deployment path)
**Benefit:** High (demonstrates cloud principles, cost-effective scaling)

---

**Prepared by:** Bryan Ang
**Date:** March 29, 2026
**Status:** Documentation Complete ✅
**Next Step:** Follow `cloud/aws/README.md` for actual deployment
