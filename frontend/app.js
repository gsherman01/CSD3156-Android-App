// Minimal Leaflet UI for testing backend GIS endpoints.
const API_BASE_URL = (
  window.location.hostname.includes('localhost') ||
  window.location.hostname.includes('127.0.0.1') ||
  window.location.hostname.includes('onrender.com')
)
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

  const res = await fetch(`${API_BASE_URL}/api/upload`, { method: 'POST', body: formData });
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

  const res = await fetch(`${API_BASE_URL}/api/analyze?${params.toString()}`);
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
