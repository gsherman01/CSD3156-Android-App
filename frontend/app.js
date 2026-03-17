// Minimal Leaflet UI for testing backend GIS endpoints.
const map = L.map('map').setView([1.3521, 103.8198], 11);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '&copy; OpenStreetMap contributors',
}).addTo(map);

let activeLayer = null; // currently displayed GeoJSON layer
let uploadedKey = null; // storage key/path returned by backend

const statusEl = document.getElementById('status');
const setStatus = (text) => (statusEl.textContent = text);

async function uploadGeoJSON() {
  const fileInput = document.getElementById('geojsonFile');
  if (!fileInput.files.length) {
    setStatus('Choose a GeoJSON file first.');
    return;
  }

  // Send file to POST /api/upload for validation + storage.
  const formData = new FormData();
  formData.append('file', fileInput.files[0]);

  const res = await fetch('/api/upload', { method: 'POST', body: formData });
  const data = await res.json();
  if (!res.ok || !data.success) {
    setStatus(`Upload failed: ${data.message || data.detail || 'Unknown error'}`);
    return;
  }

  uploadedKey = data.storage_key;
  setStatus(`Uploaded ${data.filename} (${data.feature_count} features)`);

  // Render the original uploaded file on the map.
  const text = await fileInput.files[0].text();
  const geojson = JSON.parse(text);
  if (activeLayer) map.removeLayer(activeLayer);
  activeLayer = L.geoJSON(geojson).addTo(map);
  map.fitBounds(activeLayer.getBounds());
}

async function runQuery(operation, radius = null) {
  if (!uploadedKey) {
    setStatus('Upload a dataset first.');
    return;
  }

  const params = new URLSearchParams({ dataset_path: uploadedKey, operation });
  if (radius !== null) params.set('radius', radius);

  const res = await fetch(`/api/spatial-query?${params.toString()}`);
  const data = await res.json();
  if (!res.ok) {
    setStatus(`Query failed: ${data.detail || 'Unknown error'}`);
    return;
  }

  setStatus(`${operation} complete.`);

  // Buffer returns a GeoJSON preview for quick visual inspection.
  if (operation === 'buffer' && data.result.preview_geojson) {
    if (activeLayer) map.removeLayer(activeLayer);
    activeLayer = L.geoJSON(JSON.parse(data.result.preview_geojson)).addTo(map);
    map.fitBounds(activeLayer.getBounds());
  }
}

document.getElementById('uploadBtn').addEventListener('click', uploadGeoJSON);
document.getElementById('bufferBtn').addEventListener('click', () => runQuery('buffer', 0.01));
document.getElementById('nearestBtn').addEventListener('click', () => runQuery('nearest'));
