// Minimal Leaflet UI for testing backend GIS endpoints.
const map = L.map('map').setView([1.3521, 103.8198], 11);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '&copy; OpenStreetMap contributors',
}).addTo(map);

let activeLayer = null;
let primaryKey = null;   // storage key/path returned for primary upload
let secondaryKey = null; // storage key/path returned for secondary upload

const statusEl = document.getElementById('status');
const setStatus = (text) => (statusEl.textContent = text);

function renderGeoJSON(geojson) {
  if (!geojson) return;
  if (activeLayer) map.removeLayer(activeLayer);
  activeLayer = L.geoJSON(geojson).addTo(map);
  if (activeLayer.getBounds && activeLayer.getBounds().isValid()) {
    map.fitBounds(activeLayer.getBounds());
  }
}

async function uploadGeoJSON(kind) {
  const fileInput = kind === 'primary'
    ? document.getElementById('geojsonFilePrimary')
    : document.getElementById('geojsonFileSecondary');

  if (!fileInput.files.length) {
    setStatus(`Choose a ${kind} GeoJSON file first.`);
    return;
  }

  const formData = new FormData();
  formData.append('file', fileInput.files[0]);

  // Prompt 5: integration with backend upload API.
  const res = await fetch('/api/upload', { method: 'POST', body: formData });
  const data = await res.json();
  if (!res.ok || !data.success) {
    setStatus(`Upload failed: ${data.message || data.detail || 'Unknown error'}`);
    return;
  }

  if (kind === 'primary') {
    primaryKey = data.storage_key;
  } else {
    secondaryKey = data.storage_key;
  }

  setStatus(`${kind} uploaded: ${data.filename} (${data.feature_count} features)`);

  // Render uploaded file for quick visual check.
  const text = await fileInput.files[0].text();
  renderGeoJSON(JSON.parse(text));
}

async function callAnalysis(operation) {
  if (!primaryKey) {
    setStatus('Upload primary dataset first.');
    return;
  }

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
      setStatus('Upload secondary dataset first for this operation.');
      return;
    }
    params.set('secondary_source', secondaryKey);
  }

  // Prompt 5: integration with backend analysis API.
  const res = await fetch(`/api/analyze?${params.toString()}`);
  const data = await res.json();
  if (!res.ok || !data.success) {
    setStatus(`Analysis failed: ${data.detail || data.message || 'Unknown error'}`);
    return;
  }

  setStatus(`${operation} done: ${data.feature_count} result features`);
  renderGeoJSON(data.geojson);
}

document.getElementById('uploadPrimaryBtn').addEventListener('click', () => uploadGeoJSON('primary'));
document.getElementById('uploadSecondaryBtn').addEventListener('click', () => uploadGeoJSON('secondary'));
document.getElementById('bufferBtn').addEventListener('click', () => callAnalysis('buffer'));
document.getElementById('spatialJoinBtn').addEventListener('click', () => callAnalysis('intersection'));
document.getElementById('nearestBtn').addEventListener('click', () => callAnalysis('nearest'));
