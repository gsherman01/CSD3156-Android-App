# Cloud-Powered GIS Analytics Platform  
## Master Prompt / Project Context


# GIS Cloud Application Project — UPDATED and Shorten Master Context

## Project Goal
Develop a cloud-based web application for GIS analytics and strategic decision-making. The system should demonstrate core cloud properties: functional, scalable, reliable, elastic, and secure. The application will be developed and tested on Render for stability and ease, but all code and architecture will be designed for **future migration to AWS**.

---

## Development Environment
- Codebase managed in **GitHub** repository
- Development primarily on **VSCode**
- Backend: Python + FastAPI (or Flask)
- Frontend: HTML/JS with Leaflet (GIS visualization)
- GIS Data Processing: GeoPandas, Pandas
- All deployment logic is initially done on **Render free tier** for rapid iteration

---

## Cloud Architecture Strategy
### Render (Development & Testing)
- Runs backend web service (API + compute)
- Serves frontend static site
- Logs for monitoring
- HTTPS for basic security
- Handles functional requirements and basic reliability

### AWS (Target Deployment / Migration)
- API layer: Amazon API Gateway
- Compute: AWS Lambda / EC2
- Storage: Amazon S3 (scalable, durable storage)
- Authentication & Authorization: Amazon Cognito
- Monitoring: Amazon CloudWatch
- Code is designed to call AWS services via SDKs (e.g., boto3 for Python)
- Render environment mirrors AWS folder structure and API calls for seamless migration

---

## Code Guidelines for AWS Compatibility
- External services (S3, Cognito) accessed via SDK calls with environment variables
- Modular backend code: APIs separated from service implementations
- Data processing pipelines structured in standalone Python modules
- Deployment-ready scripts (requirements.txt, start commands) match AWS Lambda / EC2 conventions
- Any temporary Render-specific settings clearly isolated for later replacement

---

## Team Roles
- Leader: Architecture, Render setup, main code development
- Team members: Minor code contributions (<10%), testing, documentation, minor frontend edits

---

## Cloud Principles Demonstrated
| Property     | How Implemented in Project                      |
|-------------|-----------------------------------------------|
| Functional  | Fully working backend + frontend on Render     |
| Scalable    | AWS-ready code calls S3 for scalable storage   |
| Reliable    | Render hosting + durable storage on AWS       |
| Elastic     | API design prepared for scaling on API Gateway|
| Secure      | HTTPS + placeholder for Cognito authentication|
---

## 1. Project Overview

You are assisting in the development of a **Cloud-Powered GIS Analytics Platform**, a web-based system that allows users to upload spatial data and perform geospatial analysis through cloud-hosted services.

The system must:
- Provide GIS analysis tools (e.g., spatial join, buffer, proximity)
- Be accessible via a web interface
- Be deployed on cloud infrastructure
- Demonstrate cloud computing principles:
  - Functional
  - Scalable
  - Reliable
  - Elastic
  - Secure

This project prioritizes:
- **Simplicity in architecture**
- **Low configuration overhead**
- **Clear separation of concerns**
- **AI-assisted code generation**

---

## 2. System Architecture Overview

High-level architecture:

User → Web Interface → API → Cloud Compute → Data Storage

Components:
- Frontend: Map-based UI
- Backend: API for GIS processing
- Cloud: Hosting, compute, storage, authentication

---

## 3. Development Constraints

IMPORTANT:

- The system must be implemented in a **single repository**
- Avoid complex DevOps tools (no Kubernetes unless explicitly required)
- Prefer **serverless or minimal configuration solutions**
- All code must be:
  - modular
  - readable
  - AI-editable

---

## 4. Separation of Responsibilities

This project is divided into TWO STRICTLY SEPARATED DOMAINS:

---

# A. CLOUD DEPLOYMENT & CONFIGURATION

## Purpose

Define and manage cloud infrastructure required to run the application.

## Scope

Includes:
- Deployment setup
- Infrastructure configuration
- Service integration

## Cloud Platform

Use:
- AWS (preferred)

## Required Services

- API Layer:
  - Amazon API Gateway

- Compute:
  - AWS Lambda (preferred)
  OR
  - EC2 (only if necessary)

- Storage:
  - Amazon S3 (for GeoJSON datasets)

- Authentication:
  - Amazon Cognito

- Monitoring:
  - Amazon CloudWatch

---

## Responsibilities

AI should:

1. Generate deployment architecture
2. Provide infrastructure setup steps
3. Configure API endpoints
4. Connect services together
5. Ensure:
   - scalability (auto scaling / serverless)
   - reliability (managed services)
   - security (authentication, IAM roles)

---

## Output Format (Cloud Tasks)

When generating cloud-related outputs, always include:

- Architecture explanation
- Step-by-step setup guide
- Configuration files (if any)
- Minimal working deployment

---

## Constraints

- Avoid over-engineering
- Prefer serverless solutions
- Minimize manual configuration
- Ensure free-tier compatibility where possible

---

# B. APPLICATION IMPLEMENTATION (SERVICES & LOGIC)

## Purpose

Implement the actual GIS functionality of the system.

## Scope

Includes:
- API logic
- GIS processing
- Data handling
- Frontend interaction

---

## Backend Requirements

Language:
- Python

Framework:
- FastAPI (preferred)

GIS Libraries:
- GeoPandas
- Shapely
- Pandas

---

## Required Features

Implement the following endpoints:

### 1. Spatial Join
- Input: two GeoJSON datasets
- Output: merged dataset based on spatial relationship

### 2. Buffer Analysis
- Input: dataset + radius
- Output: buffered geometries

### 3. Nearest Feature
- Input: two datasets
- Output: nearest distance relationships

### 4. Data Upload
- Accept GeoJSON files
- Store or process temporarily

---

## Frontend Requirements (Minimal)

- Simple web page
- Map visualization using Leaflet
- Ability to:
  - upload files
  - trigger API calls
  - display results

---

## Responsibilities

AI should:

1. Generate clean API structure
2. Implement GIS functions
3. Ensure modular code
4. Keep logic independent from cloud layer

---

## Output Format (App Tasks)

When generating application code:

- Provide file structure
- Provide complete functions
- Include example inputs/outputs
- Ensure code is runnable locally

---

## Constraints

- Keep code simple and modular
- Avoid tight coupling with cloud services
- Ensure easy debugging
- Keep everything in a single repo structure

---

## 5. Repository Structure

Expected structure:
project-root/

backend/
main.py
routes/
services/
models/

frontend/
index.html
app.js

cloud/
deployment.md
configs/

data/


---

## 6. Prompting Guidelines for AI

When using AI:

### For Cloud Tasks:
- Clearly specify: "CLOUD TASK"
- Ask for:
  - architecture
  - deployment steps
  - service configuration

### For App Tasks:
- Clearly specify: "APP TASK"
- Ask for:
  - API endpoints
  - GIS logic
  - frontend interaction

---

## 7. Example Prompts

### Example 1 (Cloud)

"CLOUD TASK:  
Design a serverless deployment for this GIS API using AWS Lambda, API Gateway, and S3. Provide step-by-step setup instructions."

---

### Example 2 (App)

"APP TASK:  
Implement a FastAPI endpoint for buffer analysis using GeoPandas. Input: GeoJSON + radius. Output: buffered GeoJSON."

---

## 8. Design Philosophy

- Keep it simple
- Prioritize clarity over complexity
- Use cloud where it adds value (not everywhere)
- Ensure all parts are AI-friendly and modular

---

## 9. Expected Outcome

A working system that:

- Allows users to perform GIS analysis via web
- Demonstrates cloud computing principles
- Uses modern development workflow with AI assistance
- Can be presented as a real-world GIS cloud application

---