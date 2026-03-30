# 🎬 GIS Analytics Platform - Demo Script

**Project:** Cloud-Powered GIS Analytics Platform
**Team:** Team 14
**Demo Scenario:** MRT Accessibility Analysis in Singapore

---

## 🎯 Demo Objective

Demonstrate how the GIS Analytics Platform enables users to analyze spatial relationships between MRT stations and residential zones in Singapore, showcasing:
- Cloud-based GIS processing
- Interactive web mapping
- Spatial analysis capabilities
- User-friendly interface

---

## 📋 Pre-Demo Checklist

- [ ] Application deployed and accessible (Render or AWS)
- [ ] Demo datasets loaded:
  - Singapore MRT Stations (Demo)
  - Singapore Residential Zones (Demo)
- [ ] Browser open with application URL
- [ ] Internet connection stable
- [ ] Screen recording/sharing ready (if remote)

---

## 🎭 Demo Flow (5-7 minutes)

### **Scene 1: Introduction (30 seconds)**

**Script:**
> "Hello! Today I'm demonstrating our Cloud-Powered GIS Analytics Platform. This web application allows users to perform spatial analysis on geographic data entirely in the cloud. We'll analyze MRT accessibility in Singapore to identify underserved areas."

**Action:**
- Show the landing page
- Point out the clean interface

---

### **Scene 2: Understanding the Interface (1 minute)**

**Script:**
> "The interface is designed to be intuitive. At the top, we have clear instructions on how to use the platform. Below that, you can see our dataset manager showing available datasets, including our demo datasets."

**Action:**
1. Point to the **Instruction Panel** (blue banner)
   - "Step 1: Upload or select datasets"
   - "Step 2: Choose an operation"
   - "Step 3: View results"

2. Point to the **Dataset Manager Panel** (orange banner)
   - Show the demo datasets with their feature counts
   - Highlight the "DEMO" badges

3. Scroll to show the **Operation Buttons**
   - Point out the user-friendly labels:
     - "Find Nearby Area"
     - "Find Overlapping Areas"
     - "Find Closest Location"
   - Hover over one to show the **tooltip**

---

### **Scene 3: Selecting Datasets (1 minute)**

**Script:**
> "Let's start our analysis. First, I'll select the MRT Stations dataset as our primary dataset. Notice how the dataset card is highlighted in green when selected, and the stations appear on the map in green."

**Action:**
1. Click **"Select as Primary"** on the "Singapore MRT Stations (Demo)" dataset
   - Wait for map to update
   - Point out the green markers/features on the map

2. Scroll to status bar
   - Show the confirmation message: "✅ Primary dataset selected"

**Script:**
> "Now I'll select the Residential Zones as our secondary dataset. This appears in blue on the map."

**Action:**
3. Click **"Select as Secondary"** on the "Singapore Residential Zones (Demo)" dataset
   - Wait for map to update with blue zones
   - Point out both green (MRT) and blue (residential) features visible together

---

### **Scene 4: Buffer Analysis - Find Nearby Areas (1.5 minutes)**

**Script:**
> "Let's start with a buffer analysis. This creates a coverage zone around each MRT station. We'll use a 500-meter radius to represent walkable distance."

**Action:**
1. Change the **radius input** from 0.01 to **0.5** (500 meters in map units)

2. Click **"🎯 Find Nearby Area"** button

3. Wait for **loading indicator**:
   - Point out: "Notice the processing indicator – our cloud backend is performing the GIS calculation"

4. When complete, highlight the **Result Summary Panel** (purple banner):
   - Point out the operation name
   - Show feature count
   - Read the insight: "Created X buffer zones around the selected features"

5. On the map:
   - Point out the **orange semi-transparent circles** around each MRT station
   - Zoom in to show the coverage areas

**Script:**
> "These orange areas represent 500-meter walking distance from each MRT station. This helps identify which residential areas have good MRT access."

---

### **Scene 5: Intersection Analysis - Find Overlapping Areas (1.5 minutes)**

**Script:**
> "Next, let's find which residential zones actually overlap with these MRT coverage areas. This tells us which neighborhoods are well-served by public transport."

**Action:**
1. Ensure both primary (MRT) and secondary (Residential) are still selected

2. Click **"🔗 Find Overlapping Areas"** button

3. Wait for processing

4. When complete, show the **Result Summary Panel**:
   - Highlight: "Found X overlapping areas between the two datasets"
   - Explain: "This means X residential zones have MRT access within 500 meters"

5. On the map:
   - Point out the **purple areas** (intersection results)
   - Explain: "The purple zones are residential areas that fall within our MRT buffer zones"

**Script:**
> "The areas NOT highlighted in purple are residential zones that may be underserved by MRT access. Urban planners could use this information to prioritize new MRT station locations."

---

### **Scene 6: Nearest Feature Analysis (1 minute)**

**Script:**
> "Finally, let's identify the nearest MRT station for each residential zone. This helps quantify accessibility."

**Action:**
1. Click **"📍 Find Closest Location"** button

2. Wait for processing

3. Show the **Result Summary Panel**:
   - Read: "Identified X nearest location relationships"

4. On the map:
   - Point out the **red dashed lines** connecting features
   - Explain: "Each line connects a residential zone to its nearest MRT station"

**Script:**
> "These connections help identify which stations serve the most residential areas, useful for capacity planning."

---

### **Scene 7: Uploading Custom Data (30 seconds - Optional)**

**Script:**
> "Users can also upload their own GeoJSON files. Let me demonstrate."

**Action:**
1. Scroll to the upload section
2. Click **"Choose File"**
3. Select a file (or just show the dialog)
4. Explain: "After uploading, the new dataset would appear in our dataset manager and be available for analysis"

**Note:** Only do this if you have extra time and a prepared file. Otherwise, skip to conclusion.

---

### **Scene 8: Conclusion (30 seconds)**

**Script:**
> "To summarize, our Cloud-Powered GIS Analytics Platform demonstrates key cloud computing principles:
> - **Functional**: Fully operational web-based GIS analysis
> - **Scalable**: Backend processes can handle multiple concurrent users
> - **Reliable**: Built on robust cloud infrastructure (Render/AWS)
> - **User-friendly**: Intuitive interface with clear guidance and visual feedback
>
> This platform can be used for urban planning, environmental analysis, logistics optimization, and many other spatial decision-making scenarios. Thank you!"

**Action:**
- Return to the full map view showing all results
- Optionally show the browser tab with the project documentation

---

## 🎨 Visual Highlights to Emphasize

During the demo, point out these UI/UX features:

### Kang Ting's Work:
- ✅ **Instruction Panel** - Clear 3-step guide
- ✅ **Improved Button Labels** - User-friendly names instead of technical terms
- ✅ **Tooltips** - Hover descriptions on all operation buttons
- ✅ **Loading Indicator** - Processing feedback with spinner

### Sam's Work:
- ✅ **Dataset Manager** - Visual dataset selector with metadata
- ✅ **Color Coding** - Green for primary, blue for secondary
- ✅ **Demo Datasets** - Pre-loaded Singapore data

### Steven's Work:
- ✅ **Result Summary Panel** - Clear insights after each operation
- ✅ **Visual Highlighting** - Different colors for different operations:
  - Orange (buffer)
  - Purple (intersection)
  - Red dashed lines (nearest)
- ✅ **Professional Layout** - Clean spacing and color coordination

### Bryan's Work (Backend):
- ✅ **Error Handling** - Graceful failure messages
- ✅ **File Validation** - Size limits and format checks
- ✅ **Performance** - Fast processing with cloud infrastructure

---

## ⚠️ Troubleshooting

### If datasets don't load:
- Check browser console for errors
- Verify backend is running
- Refresh the page

### If analysis fails:
- Ensure both datasets are selected (for intersection/nearest)
- Check radius value is reasonable (0.001 - 10)
- Look at the status bar for specific error messages

### If map doesn't display results:
- Check browser zoom level
- Try zooming out to see full extent
- Refresh the page and retry

---

## 📊 Expected Results

### Buffer Analysis:
- **Feature Count:** ~10-30 buffer zones (depending on MRT dataset size)
- **Visual:** Orange circles/polygons around MRT stations
- **Insight:** Coverage area visualization

### Intersection Analysis:
- **Feature Count:** ~5-20 overlapping zones
- **Visual:** Purple highlighted areas where residential zones meet MRT buffers
- **Insight:** Well-served residential areas identified

### Nearest Feature Analysis:
- **Feature Count:** Equal to number of residential zones
- **Visual:** Red dashed lines connecting zones to nearest MRT
- **Insight:** Accessibility relationships mapped

---

## 🎥 Recording Tips

1. **Practice** the demo 2-3 times before recording
2. **Speak clearly** and at a moderate pace
3. **Zoom in** on important UI elements when explaining them
4. **Pause briefly** after clicking buttons to let processing complete
5. **Use mouse cursor** to highlight features on screen
6. **Keep it under 7 minutes** for best engagement

---

## 📝 Alternative Demo Scenarios

If you want to show different use cases:

### Scenario 2: School Proximity Analysis
- Primary: Schools dataset
- Secondary: Residential zones
- Analysis: Find neighborhoods within 1km of schools

### Scenario 3: Park Access Study
- Primary: Parks/green spaces
- Secondary: High-density residential areas
- Analysis: Identify underserved areas

### Scenario 4: Emergency Services Coverage
- Primary: Fire stations
- Secondary: Buildings
- Analysis: Response time analysis (buffer = 5-minute drive radius)

---

## ✅ Demo Completion Checklist

After demo, verify you've shown:
- [ ] All three operation types (buffer, intersection, nearest)
- [ ] Dataset selection workflow
- [ ] Result visualization on map
- [ ] Result summary panel with insights
- [ ] Loading indicators and status messages
- [ ] Mentioned cloud computing principles
- [ ] Highlighted team contributions (optional)

---

**Good luck with your demo! 🚀**
