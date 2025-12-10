<template>
  <div class="h-full flex flex-col p-6 bg-gray-100">
    <div class="bg-white rounded-lg shadow-lg p-4 mb-4">
      <h2 class="text-2xl font-bold text-gray-800">🗺️ Live Fleet Tracking</h2>
      <p class="text-gray-600 mt-2">Real-time GPS tracking powered by Geoapify - {{ vehicleCount }} vehicles</p>
    </div>

    <div class="flex-1 bg-white rounded-lg shadow-lg p-4 overflow-hidden">
      <div ref="mapContainer" class="w-full h-full rounded-lg"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useVehicleStore } from '@/stores/vehicleStore'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const vehicleStore = useVehicleStore()
const mapContainer = ref(null)
const map = ref(null)
const markers = ref({})
const vehicleCount = computed(() => vehicleStore.getVehicleCount)

const GEOAPIFY_API_KEY = '7022be7caa6d4cdfa09c1e5a8d963359'
const CLUJ_LAT = 46.7712
const CLUJ_LNG = 23.5889

onMounted(async () => {
  // Initialize map
  map.value = L.map(mapContainer.value).setView([CLUJ_LAT, CLUJ_LNG], 13)

  // Add Geoapify OSM Bright tiles
  L.tileLayer(
    `https://maps.geoapify.com/v1/tile/osm-bright/{z}/{x}/{y}.png?apiKey=${GEOAPIFY_API_KEY}`,
    {
      attribution: '© Geoapify | © OpenStreetMap contributors',
      maxZoom: 20
    }
  ).addTo(map.value)

  // Load vehicles
  await vehicleStore.fetchVehicles()
  vehicleStore.vehicles.forEach(vehicle => {
    if (vehicle.lat && vehicle.lng) {
      addVehicleMarker(vehicle)
    }
  })

  // Connect WebSocket
  vehicleStore.connectWebSocket()
})

onUnmounted(() => {
  vehicleStore.disconnectWebSocket()
  if (map.value) {
    map.value.remove()
  }
})

const addVehicleMarker = (vehicle) => {
  const color = getStatusColor(vehicle.status)

  const icon = L.divIcon({
    html: `
      <div style="
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background-color: ${color};
        border: 3px solid white;
        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
      ">🚗</div>
    `,
    iconSize: [32, 32],
    className: 'custom-vehicle-marker'
  })

  const marker = L.marker([vehicle.lat, vehicle.lng], { icon })
    .bindPopup(`
      <div style="text-align: center; min-width: 150px;">
        <p style="font-weight: bold; margin: 8px 0; font-size: 14px;">${vehicle.plate}</p>
        <p style="font-size: 12px; margin: 4px 0;">Brand: ${vehicle.brand || 'N/A'}</p>
        <p style="font-size: 12px; margin: 4px 0;">Type: ${vehicle.type}</p>
        <p style="font-size: 12px; margin: 4px 0;">Status: <span style="color: ${getStatusColor(vehicle.status)}; font-weight: bold;">${vehicle.status}</span></p>
        <p style="font-size: 11px; margin: 8px 0; color: #666;">Lat: ${vehicle.lat?.toFixed(4) || 'N/A'}</p>
        <p style="font-size: 11px; color: #666;">Lng: ${vehicle.lng?.toFixed(4) || 'N/A'}</p>
      </div>
    `)
    .addTo(map.value)

  markers.value[vehicle.plate] = marker
}

const updateVehicleMarker = (vehicle) => {
  const marker = markers.value[vehicle.plate]
  if (marker) {
    marker.setLatLng([vehicle.lat, vehicle.lng])
  } else if (vehicle.lat && vehicle.lng) {
    addVehicleMarker(vehicle)
  }
}

const getStatusColor = (status) => {
  switch (status) {
    case 'ACTIVE': return '#10b981'
    case 'IDLE': return '#f59e0b'
    case 'MAINTENANCE': return '#ef4444'
    default: return '#6b7280'
  }
}

// Watch for vehicle updates
vehicleStore.$subscribe((mutation, state) => {
  state.vehicles.forEach(updateVehicleMarker)
})
</script>

<style scoped>
.leaflet-popup-content {
  margin: 0 !important;
}

:deep(.leaflet-marker-icon) {
  background: transparent !important;
  border: none !important;
}

:deep(.custom-vehicle-marker) {
  background: transparent !important;
}
</style>
