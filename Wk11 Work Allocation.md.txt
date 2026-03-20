# 🧩 GIS Analytics Platform — Task Allocation (Agent-Ready)

## 📌 Project Context

This project is a cloud-ready GIS Analytics Platform built using:

* FastAPI backend
* Leaflet-based frontend
* Local → Render → AWS deployment pipeline

### Current Capabilities

* Upload GeoJSON datasets
* Perform spatial analysis:

  * Buffer (proximity)
  * Intersection (overlap)
  * Nearest (distance)
* Store datasets locally (S3-ready abstraction)
* Basic frontend + API integration

---

## 🎯 Phase Goal

Improve:

* Usability (UI clarity)
* Dataset handling (selection + reuse)
* Stability (no crashes)
* Demo quality (clear storytelling)

⚠️ Constraints:

* No major backend refactoring
* Keep architecture stable
* All tasks scoped for ~1 working session

---

# 👤 Kang Ting — Frontend UX & Interaction

## 🎯 Objective

Make the application intuitive and self-explanatory without external instructions.

---

## ✅ Tasks

### 1. Instruction Panel

Add a visible UI component (top or sidebar):

**Content:**

* Step 1: Upload dataset
* Step 2: Select operation
* Step 3: View results on map

---

### 2. Improve Button Labels

Replace technical terms:

* Buffer → **Find Nearby Area**
* Nearest → **Find Closest Location**
* Intersection → **Find Overlapping Areas**

---

### 3. Add Tooltips

Add hover descriptions for each operation:

* Buffer → "Creates a zone around features (e.g. 200m radius)"
* Nearest → "Finds closest feature between datasets"
* Intersection → "Finds overlapping areas between datasets"

---

### 4. Loading Indicator

When API is triggered:

* Show "Processing..." message
* Disable buttons temporarily

---

## 📦 Deliverables

* Updated UI with guidance
* Improved user clarity
* No backend changes required

---

# 👤 Sam Tsang — Dataset Management & Data Integration

## 🎯 Objective

Enable dataset reuse, selection, and improved interaction with stored data.

---

## ✅ Tasks

### 1. Dataset Manager UI

Create a panel displaying available datasets.

**Features:**

* Fetch dataset list from backend registry
* Display dataset names
* Allow:

  * Select as PRIMARY dataset
  * Select as SECONDARY dataset

---

### 2. Dataset Metadata Display

When a dataset is selected, show:

* Dataset name
* Feature count
* Optional: CRS or bounds

---

### 3. Preloaded Demo Datasets

Ensure system includes:

* MRT Stations dataset
* Residential Buildings dataset

Requirements:

* Available without upload
* Selectable from Dataset Manager

---

### 4. OPTIONAL — External Data Integration

Integrate one dataset from:
👉 data.gov.sg

**Steps:**

1. Fetch dataset via API
2. Extract:

   * name
   * latitude
   * longitude
3. Convert to GeoJSON
4. Register in system as selectable dataset

⚠️ Keep dataset small and simple

---

## 📦 Deliverables

* Dataset Manager UI
* Working dataset selection system
* 2–3 usable demo datasets
* (Optional) 1 external dataset

---

# 👤 Bryan Ang — Stability & Cloud Readiness

## 🎯 Objective

Ensure system reliability and demonstrate cloud-ready practices.

---

## ✅ Tasks

### 1. Enhanced Health Endpoint

Improve `/health` endpoint to include:

* App status
* Storage availability
* Current mode (local / S3)

**Expected output:**

```json
{
  "status": "ok",
  "storage": "available",
  "mode": "local"
}
```

---

### 2. Error Handling

Ensure backend handles:

* Invalid GeoJSON uploads
* Missing parameters (e.g. buffer radius)
* Empty datasets
* Unsupported operations

**Requirement:**

* Return clear error messages
* No crashes

---

### 3. Logging Improvements

Ensure logs include:

* Request received
* Operation type
* Processing start
* Processing success/failure

---

### 4. Basic Load Testing

Simulate multiple requests:

* Send repeated API calls
* Confirm system remains responsive

---

### 5. OPTIONAL — Cloud Mapping Documentation

Document how system maps to AWS:

* FastAPI → Lambda
* API routes → API Gateway
* Storage → S3
* Logs → CloudWatch

(No implementation required)

---

## 📦 Deliverables

* Stable backend behavior
* Documented test cases
* Verified system reliability

---

# 👤 Steven — Demo & UI Refinement

## 🎯 Objective

Make the system presentation-ready and visually clear.

---

## ✅ Tasks

### 1. Demo Script

Create structured demo flow:

1. Select MRT dataset
2. Run buffer analysis
3. Select residential dataset
4. Run intersection
5. Run nearest analysis

---

### 2. Visual Highlighting

Ensure map clearly differentiates:

* Buffer → semi-transparent area
* Intersection → distinct color
* Nearest → visible connection lines

---

### 3. Result Summary Panel

Add UI component displaying:

* Number of features processed
* Key insights

**Example:**

* "2 residential areas within MRT coverage"
* "1 area underserved"

---

### 4. UI Polish

Improve:

* Layout spacing
* Alignment
* Color consistency

---

## 📦 Deliverables

* Clear demo flow
* Visual clarity on map
* Insight summary visible

---

# 👤 Project Leader (You)

## Responsibilities

* Maintain backend stability (no major changes)
* Ensure frontend ↔ backend integration
* Guide demo narrative:

👉 MRT Accessibility Analysis

---

# 🎯 Final Expected Outcome

System should:

* Be intuitive without explanation
* Support dataset reuse and selection
* Be stable under demo conditions
* Clearly communicate GIS insights
* Be ready for Render deployment and AWS explanation

---

# 🔥 Key Principle

Do NOT:

* Add new complex features
* Refactor architecture
* Overcomplicate UI

DO:

* Improve clarity
* Improve stability
* Improve presentation

---
