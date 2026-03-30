// Minimal Leaflet UI for testing backend GIS endpoints.
const configuredApiBaseUrl = window.__APP_CONFIG__?.API_BASE_URL || '';
const isLocalOrRender =
  window.location.hostname.includes('localhost') ||
  window.location.hostname.includes('127.0.0.1') ||
  window.location.hostname.includes('onrender.com');

const API_BASE_URL = configuredApiBaseUrl
  ? configuredApiBaseUrl.replace(/\/$/, '')
  : isLocalOrRender
    ? ''
    : 'https://YOUR_API_GATEWAY_URL_HERE';

console.log('Using API base URL:', API_BASE_URL || '(relative)');

const map = L.map('map').setView([1.3521, 103.8198], 11);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '&copy; OpenStreetMap contributors',
}).addTo(map);

let activeLayer = null;
let primaryKey = null;
let secondaryKey = null;
let primaryLayer = null;
let secondaryLayer = null;
let resultLayer = null;

const statusEl = document.getElementById('status');
const loadingEl = document.getElementById('loading');
const resultSummaryEl = document.getElementById('result-summary');
const summaryContentEl = document.getElementById('summary-content');
const setStatus = (text) => (statusEl.textContent = text);

// Loading indicator functions (Task 4)
function showLoading() {
  loadingEl.classList.add('active');
  // Disable all buttons during loading
  document.querySelectorAll('button').forEach(btn => btn.disabled = true);
}

function hideLoading() {
  loadingEl.classList.remove('active');
  // Re-enable all buttons
  document.querySelectorAll('button').forEach(btn => btn.disabled = false);
}

// Dataset Manager Functions (Sam's Tasks)
async function loadDatasets() {
  try {
    const res = await fetch(`${API_BASE_URL}/api/datasets`);
    const data = await res.json();
    if (data.success && data.datasets) {
      displayDatasets(data.datasets);
    } else {
      document.getElementById('dataset-list').innerHTML =
        '<div style="grid-column: 1/-1; color: #999;">No datasets available</div>';
    }
  } catch (error) {
    console.error('Failed to load datasets:', error);
    document.getElementById('dataset-list').innerHTML =
      '<div style="grid-column: 1/-1; color: #f44336;">Failed to load datasets</div>';
  }
}

function displayDatasets(datasets) {
  const listEl = document.getElementById('dataset-list');
  if (datasets.length === 0) {
    listEl.innerHTML = '<div style="grid-column: 1/-1; color: #999;">No datasets available. Upload one to get started!</div>';
    return;
  }

  listEl.innerHTML = datasets.map(dataset => `
    <div class="dataset-item" data-key="${dataset.storage_key}" data-id="${dataset.dataset_id}">
      <div class="dataset-name">
        ${dataset.filename}
        ${dataset.is_demo ? '<span class="dataset-badge">DEMO</span>' : ''}
      </div>
      <div class="dataset-meta">
        📍 ${dataset.feature_count || 0} features
        ${dataset.crs ? `• ${dataset.crs}` : ''}
      </div>
      <div class="dataset-selection-buttons">
        <button class="btn-primary" onclick="selectDataset('${dataset.storage_key}', 'primary', this)">
          Select as Primary
        </button>
        <button class="btn-secondary" onclick="selectDataset('${dataset.storage_key}', 'secondary', this)">
          Select as Secondary
        </button>
      </div>
    </div>
  `).join('');
}

function selectDataset(storageKey, type, buttonEl) {
  if (type === 'primary') {
    primaryKey = storageKey;
    // Update visual selection
    document.querySelectorAll('.dataset-item').forEach(el => el.classList.remove('selected-primary'));
    buttonEl.closest('.dataset-item').classList.add('selected-primary');
    setStatus(`✅ Primary dataset selected and displayed in green`);

    // Load and display on map with green color
    loadDatasetOnMap(storageKey, 'primary');
  } else {
    secondaryKey = storageKey;
    document.querySelectorAll('.dataset-item').forEach(el => el.classList.remove('selected-secondary'));
    buttonEl.closest('.dataset-item').classList.add('selected-secondary');
    setStatus(`✅ Secondary dataset selected and displayed in blue`);

    // Load and display on map with blue color
    loadDatasetOnMap(storageKey, 'secondary');
  }

  // Update requirement checkboxes and button states
  updateDatasetRequirements();
}

// Update the requirement warning and enable/disable multi-dataset buttons
function updateDatasetRequirements() {
  const primaryCheck = document.getElementById('primary-check');
  const secondaryCheck = document.getElementById('secondary-check');
  const overlapBtn = document.getElementById('spatialJoinBtn');
  const nearestBtn = document.getElementById('nearestBtn');

  // Update checkboxes
  if (primaryCheck) {
    primaryCheck.textContent = primaryKey ? '✅' : '❌';
    primaryCheck.style.color = primaryKey ? '#4caf50' : '#f44336';
  }

  if (secondaryCheck) {
    secondaryCheck.textContent = secondaryKey ? '✅' : '❌';
    secondaryCheck.style.color = secondaryKey ? '#2196f3' : '#f44336';
  }

  // Enable/disable multi-dataset buttons
  const canDoMultiDataset = primaryKey && secondaryKey;
  if (overlapBtn) {
    overlapBtn.disabled = !canDoMultiDataset;
  }
  if (nearestBtn) {
    nearestBtn.disabled = !canDoMultiDataset;
  }

  // Update warning box styling
  const warningBox = document.getElementById('dataset-requirement-warning');
  if (warningBox) {
    if (canDoMultiDataset) {
      warningBox.style.background = '#e8f5e9';
      warningBox.style.color = '#2e7d32';
      warningBox.style.borderColor = '#4caf50';
    } else {
      warningBox.style.background = '#fff3e0';
      warningBox.style.color = '#f57c00';
      warningBox.style.borderColor = '#ff9800';
    }
  }
}

async function loadDatasetOnMap(storageKey, type) {
  try {
    showLoading();
    // Fetch the GeoJSON from storage - use buffer with 0 radius to just get the data
    const res = await fetch(`${API_BASE_URL}/api/analyze?source=${storageKey}&operation=buffer&radius=0`);
    const data = await res.json();

    if (data.success && data.geojson) {
      const color = type === 'primary' ? '#4caf50' : '#2196f3';

      // Create layer with both point and polygon styling
      const layer = L.geoJSON(data.geojson, {
        style: function(feature) {
          return {
            color: color,
            weight: 2,
            fillColor: color,
            fillOpacity: 0.3
          };
        },
        pointToLayer: function(feature, latlng) {
          return L.circleMarker(latlng, {
            radius: 6,
            fillColor: color,
            color: color,
            weight: 2,
            opacity: 1,
            fillOpacity: 0.6
          });
        }
      }).addTo(map);

      if (type === 'primary') {
        if (primaryLayer) map.removeLayer(primaryLayer);
        primaryLayer = layer;
      } else {
        if (secondaryLayer) map.removeLayer(secondaryLayer);
        secondaryLayer = layer;
      }

      // Update map legend
      updateMapLegend();

      // Fit bounds to show all layers
      const allLayers = [];
      if (primaryLayer) allLayers.push(primaryLayer);
      if (secondaryLayer) allLayers.push(secondaryLayer);
      if (allLayers.length > 0) {
        const group = L.featureGroup(allLayers);
        map.fitBounds(group.getBounds());
      }
    }
    hideLoading();
  } catch (error) {
    console.error('Failed to load dataset on map:', error);
    setStatus(`❌ Failed to load dataset: ${error.message}`);
    hideLoading();
  }
}

// Update map legend to show active datasets
function updateMapLegend() {
  let legendHTML = '<div style="font-weight: bold; margin-bottom: 8px;">📍 Active Datasets:</div>';

  if (primaryLayer) {
    legendHTML += '<div style="margin-bottom: 4px;"><span style="display: inline-block; width: 20px; height: 3px; background: #4caf50; margin-right: 6px;"></span>Primary Dataset</div>';
  }

  if (secondaryLayer) {
    legendHTML += '<div style="margin-bottom: 4px;"><span style="display: inline-block; width: 20px; height: 3px; background: #2196f3; margin-right: 6px;"></span>Secondary Dataset</div>';
  }

  if (!primaryLayer && !secondaryLayer) {
    legendHTML += '<div style="color: #999; font-size: 0.85rem;">No datasets selected</div>';
  }

  const legendEl = document.getElementById('map-legend');
  if (legendEl) {
    legendEl.innerHTML = legendHTML;
  }
}

function renderGeoJSON(geojson) {
  if (!geojson) return;
  if (activeLayer) map.removeLayer(activeLayer);
  activeLayer = L.geoJSON(geojson).addTo(map);
  if (activeLayer.getBounds && activeLayer.getBounds().isValid()) {
    map.fitBounds(activeLayer.getBounds());
  }
}

// Result Summary Functions (Steven's Task)
function showResultSummary(operation, featureCount, geojson) {
  resultSummaryEl.classList.add('visible');

  const operationNames = {
    buffer: 'Nearby Area Analysis',
    intersection: 'Overlapping Areas Analysis',
    nearest: 'Closest Location Analysis'
  };

  const insights = generateInsights(operation, featureCount, geojson);

  summaryContentEl.innerHTML = `
    <div class="summary-stat">
      <strong>Operation:</strong> ${operationNames[operation] || operation}
    </div>
    <div class="summary-stat">
      <strong>Features Found:</strong> ${featureCount}
    </div>
    ${insights}
  `;
}

function generateInsights(operation, featureCount, geojson) {
  if (featureCount === 0) {
    return '<p style="margin-top: 12px;">⚠️ No features found matching the criteria.</p>';
  }

  let insight = '<p style="margin-top: 12px;">';

  if (operation === 'buffer') {
    insight += `🎯 Created ${featureCount} buffer zone${featureCount > 1 ? 's' : ''} around the selected features.`;
  } else if (operation === 'intersection') {
    insight += `🔗 Found ${featureCount} overlapping area${featureCount > 1 ? 's' : ''} between the two datasets.`;
  } else if (operation === 'nearest') {
    insight += `📍 Identified ${featureCount} nearest location relationship${featureCount > 1 ? 's' : ''} between datasets.`;
  }

  insight += '</p>';
  return insight;
}

async function uploadGeoJSON(kind) {
  const fileInput = kind === 'primary'
    ? document.getElementById('geojsonFilePrimary')
    : document.getElementById('geojsonFileSecondary');

  if (!fileInput.files.length) {
    setStatus(`Choose a ${kind} GeoJSON file first.`);
    return;
  }

  showLoading();
  setStatus(`Uploading ${kind} dataset...`);

  try {
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    const res = await fetch(`${API_BASE_URL}/api/upload`, { method: 'POST', body: formData });
    const data = await res.json();
    if (!res.ok || !data.success) {
      setStatus(`❌ Upload failed: ${data.message || data.detail || 'Unknown error'}`);
      hideLoading();
      return;
    }

    if (kind === 'primary') {
      primaryKey = data.storage_key;
    } else {
      secondaryKey = data.storage_key;
    }

    setStatus(`✅ ${kind} uploaded: ${data.filename} (${data.feature_count} features)`);

    const text = await fileInput.files[0].text();
    renderGeoJSON(JSON.parse(text));

    // Reload dataset list to show newly uploaded dataset
    await loadDatasets();
  } catch (error) {
    setStatus(`Upload error: ${error.message}`);
  } finally {
    hideLoading();
  }
}

async function callAnalysis(operation) {
  if (!primaryKey) {
    setStatus('⚠️ Upload primary dataset first.');
    return;
  }

  showLoading();
  const operationNames = {
    buffer: 'Find Nearby Area',
    intersection: 'Find Overlapping Areas',
    nearest: 'Find Closest Location'
  };
  setStatus(`Running ${operationNames[operation]}...`);

  try {
    const params = new URLSearchParams({
      source: primaryKey,
      operation,
    });

    if (operation === 'buffer') {
      const radius = document.getElementById('bufferRadius').value || '0.01';
      params.set('radius', radius);
    }

    if (operation === 'intersection' || operation === 'nearest') {
      if (!secondaryKey) {
        setStatus('⚠️ Upload secondary dataset first for this operation.');
        hideLoading();
        return;
      }
      params.set('secondary_source', secondaryKey);
    }

    const res = await fetch(`${API_BASE_URL}/api/analyze?${params.toString()}`);
    const data = await res.json();
    if (!res.ok || !data.success) {
      setStatus(`❌ Analysis failed: ${data.detail || data.message || 'Unknown error'}`);
      hideLoading();
      return;
    }

    setStatus(`✅ ${operationNames[operation]} complete: ${data.feature_count} result features found`);

    // Show result summary panel (Steven's Task)
    showResultSummary(operation, data.feature_count, data.geojson);

    // Render result with visual highlighting (Steven's Task)
    renderResultWithStyle(data.geojson, operation);
  } catch (error) {
    setStatus(`❌ Analysis error: ${error.message}`);
  } finally {
    hideLoading();
  }
}

// Visual highlighting for different operations (Steven's Task 2)
function renderResultWithStyle(geojson, operation) {
  if (!geojson) return;

  // Remove previous result layer
  if (resultLayer) map.removeLayer(resultLayer);

  // Different colors and styles for different operations
  let style = {};
  if (operation === 'buffer') {
    style = {
      color: '#ff9800',
      weight: 2,
      fillColor: '#ff9800',
      fillOpacity: 0.2
    };
  } else if (operation === 'intersection') {
    style = {
      color: '#9c27b0',
      weight: 3,
      fillColor: '#9c27b0',
      fillOpacity: 0.4
    };
  } else if (operation === 'nearest') {
    style = {
      color: '#f44336',
      weight: 2,
      dashArray: '5, 10'
    };
  }

  resultLayer = L.geoJSON(geojson, {
    style: style,
    pointToLayer: (feature, latlng) => {
      return L.circleMarker(latlng, {
        radius: 6,
        ...style
      });
    }
  }).addTo(map);

  if (resultLayer.getBounds && resultLayer.getBounds().isValid()) {
    map.fitBounds(resultLayer.getBounds());
  }
}

document.getElementById('uploadPrimaryBtn').addEventListener('click', () => uploadGeoJSON('primary'));
document.getElementById('uploadSecondaryBtn').addEventListener('click', () => uploadGeoJSON('secondary'));
document.getElementById('bufferBtn').addEventListener('click', () => callAnalysis('buffer'));
document.getElementById('spatialJoinBtn').addEventListener('click', () => callAnalysis('intersection'));
document.getElementById('nearestBtn').addEventListener('click', () => callAnalysis('nearest'));

// Initialize: Load datasets and update UI on page load
loadDatasets();
updateDatasetRequirements();
updateMapLegend();
