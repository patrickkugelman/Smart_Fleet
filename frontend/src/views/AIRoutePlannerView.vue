<template>
  <div class="h-full flex flex-col p-4 bg-gray-100">
    <h2 class="text-2xl font-bold mb-4">🤖 AI Route Planner</h2>
    
    <div class="flex gap-4 h-full">
      <div class="w-1/3 bg-white p-4 rounded shadow-lg flex flex-col gap-4">
        <div>
          <label class="font-bold block mb-1">Start Location</label>
          <input v-model="startCity" class="w-full border p-2 rounded" placeholder="e.g. Arad">
        </div>
        <div>
          <label class="font-bold block mb-1">End Location</label>
          <input v-model="endCity" class="w-full border p-2 rounded" placeholder="e.g. Brasov">
        </div>
        
        <button @click="planRoute" :disabled="loading" class="bg-blue-600 text-white py-2 rounded font-bold hover:bg-blue-700 disabled:opacity-50">
          {{ loading ? 'Calculating...' : '🗺️ Plan Route' }}
        </button>

        <div v-if="stats" class="mt-4 p-4 bg-blue-50 rounded border border-blue-200">
          <h3 class="font-bold text-lg mb-2">Optimized Route</h3>
          <p>📏 Distance: <strong>{{ (stats.distance / 1000).toFixed(1) }} km</strong></p>
          <p>⏱️ Time: <strong>{{ (stats.time / 60).toFixed(0) }} min</strong></p>
          <p>⛽ Fuel Est: <strong>{{ (stats.distance / 1000 * 0.08).toFixed(1) }} L</strong></p>
        </div>
      </div>

      <div class="flex-1 bg-white rounded shadow-lg overflow-hidden relative">
        <div id="routeMap" class="w-full h-full"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const map = ref(null)
const startCity = ref('Arad')
const endCity = ref('Cluj-Napoca')
const loading = ref(false)
const stats = ref(null)
const routeLayer = ref(null)

const API_KEY = '7022be7caa6d4cdfa09c1e5a8d963359' // Cheia ta Geoapify

onMounted(() => {
  map.value = L.map('routeMap').setView([46.0, 24.0], 7)
  L.tileLayer(`https://maps.geoapify.com/v1/tile/osm-bright/{z}/{x}/{y}.png?apiKey=${API_KEY}`, {
    attribution: 'Geoapify', maxZoom: 20
  }).addTo(map.value)
})

// Funcție ajutătoare pentru fetch fără header de autorizare
const fetchGeoapify = async (url) => {
  // Folosim fetch nativ, care NU include token-ul de backend
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Geoapify Error: ${response.statusText}`)
  }
  return await response.json()
}

const getCoords = async (city) => {
  const url = `https://api.geoapify.com/v1/geocode/search?text=${encodeURIComponent(city)}&apiKey=${API_KEY}`
  const data = await fetchGeoapify(url)
  
  if (data.features && data.features.length > 0) {
    const coords = data.features[0].geometry.coordinates
    return { lat: coords[1], lng: coords[0] }
  }
  throw new Error(`City not found: ${city}`)
}

const planRoute = async () => {
  loading.value = true
  if(routeLayer.value) map.value.removeLayer(routeLayer.value)
  stats.value = null
  
  try {
    const start = await getCoords(startCity.value)
    const end = await getCoords(endCity.value)

    const routeUrl = `https://api.geoapify.com/v1/routing?waypoints=${start.lat},${start.lng}|${end.lat},${end.lng}&mode=drive&apiKey=${API_KEY}`
    const data = await fetchGeoapify(routeUrl)
    
    const routeData = data.features[0]
    stats.value = routeData.properties

    // Desenăm ruta (inversăm coordonatele pentru Leaflet: [lat, lng])
    const geometry = routeData.geometry.coordinates[0].map(c => [c[1], c[0]]) 
    routeLayer.value = L.polyline(geometry, { color: 'blue', weight: 5 }).addTo(map.value)
    
    // Zoom pe rută
    map.value.fitBounds(routeLayer.value.getBounds(), { padding: [50, 50] })

    // Adăugăm markeri
    L.marker([start.lat, start.lng]).addTo(map.value).bindPopup('Start: ' + startCity.value).openPopup()
    L.marker([end.lat, end.lng]).addTo(map.value).bindPopup('End: ' + endCity.value)

  } catch (error) {
    alert('Error planning route: ' + error.message)
  } finally {
    loading.value = false
  }
}
</script>