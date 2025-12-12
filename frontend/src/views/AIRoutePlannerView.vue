<template>
  <div class="h-full flex flex-col p-6 bg-gray-100 min-h-screen">
    <div class="flex items-center gap-3 mb-6">
      <div class="p-3 bg-blue-600 rounded-lg text-white shadow-lg">
        <span class="text-2xl">🤖</span>
      </div>
      <div>
        <h2 class="text-3xl font-bold text-gray-800">AI Route Optimization</h2>
        <p class="text-gray-500 text-sm">Smart navigation & fuel prediction engine</p>
      </div>
    </div>
    
    <div class="flex flex-col lg:flex-row gap-6 h-full">
      <div class="w-full lg:w-1/3 space-y-6">
        
        <div class="bg-white p-6 rounded-xl shadow-lg border border-gray-100">
          <div class="space-y-4">
            <div>
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wide mb-1 block">Start Point</label>
              <div class="relative">
                <span class="absolute left-3 top-2.5 text-gray-400">📍</span>
                <input v-model="startCity" class="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition" placeholder="e.g. Cluj-Napoca">
              </div>
            </div>
            
            <div>
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wide mb-1 block">Destination</label>
              <div class="relative">
                <span class="absolute left-3 top-2.5 text-gray-400">🏁</span>
                <input v-model="endCity" class="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition" placeholder="e.g. Vienna">
              </div>
            </div>
            
            <button 
              @click="planRoute" 
              :disabled="loading" 
              class="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-3 rounded-lg font-bold shadow-md hover:shadow-xl hover:-translate-y-0.5 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex justify-center items-center gap-2"
            >
              <span v-if="loading" class="animate-spin">⏳</span>
              <span>{{ loading ? 'AI Computing...' : '✨ Generate Smart Route' }}</span>
            </button>
          </div>
        </div>

        <div v-if="stats" class="bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden animation-fade-in">
          <div class="bg-gray-50 p-4 border-b border-gray-100 flex justify-between items-center">
            <h3 class="font-bold text-gray-800 flex items-center gap-2">
              <span>📊</span> Trip Analytics
            </h3>
            <span class="bg-green-100 text-green-700 text-xs px-2 py-1 rounded-full font-bold">OPTIMIZED</span>
          </div>
          
          <div class="p-6 grid grid-cols-2 gap-4">
            <div class="text-center p-3 bg-blue-50 rounded-lg">
              <p class="text-xs text-gray-500 uppercase">Distance</p>
              <p class="text-xl font-bold text-blue-700">{{ (stats.distance / 1000).toFixed(1) }} <span class="text-sm">km</span></p>
            </div>
            <div class="text-center p-3 bg-purple-50 rounded-lg">
              <p class="text-xs text-gray-500 uppercase">Duration</p>
              <p class="text-xl font-bold text-purple-700">{{ formatTime(stats.time) }}</p>
            </div>
            <div class="text-center p-3 bg-orange-50 rounded-lg col-span-2">
              <p class="text-xs text-gray-500 uppercase">Est. Fuel Consumption</p>
              <p class="text-2xl font-bold text-orange-600">{{ calculateFuel(stats.distance) }} <span class="text-sm">Liters</span></p>
              <p class="text-xs text-orange-400 mt-1">(Avg 28L/100km for Truck)</p>
            </div>
          </div>

          <div class="px-6 pb-6 space-y-3">
            <div class="p-3 bg-gray-50 rounded border-l-4 border-blue-500 text-sm text-gray-600">
              <strong>💡 AI Tip:</strong> {{ getAiAdvice(stats.distance) }}
            </div>
            
            <button 
              @click="openGoogleMaps" 
              class="w-full mt-2 py-2 border-2 border-blue-600 text-blue-600 rounded-lg font-bold hover:bg-blue-50 transition flex items-center justify-center gap-2"
            >
              <span>🗺️</span> Open in Google Maps
            </button>
          </div>
        </div>

      </div>

      <div class="flex-1 bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden relative">
        <div id="routeMap" class="w-full h-full z-0"></div>
        
        <div v-if="loading" class="absolute inset-0 bg-white/80 z-10 flex flex-col items-center justify-center backdrop-blur-sm">
          <div class="text-4xl animate-bounce mb-2">🤖</div>
          <p class="font-bold text-gray-600">Analyzing traffic patterns...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const map = ref(null)
const startCity = ref('Cluj-Napoca')
const endCity = ref('Hamburg')
const loading = ref(false)
const stats = ref(null)
const routeLayer = ref(null)

const API_KEY = '7022be7caa6d4cdfa09c1e5a8d963359' // Geoapify Key

onMounted(() => {
  map.value = L.map('routeMap').setView([48.0, 15.0], 5)
  L.tileLayer(`https://maps.geoapify.com/v1/tile/osm-bright/{z}/{x}/{y}.png?apiKey=${API_KEY}`, {
    attribution: 'Geoapify', maxZoom: 18
  }).addTo(map.value)
})

const fetchGeoapify = async (url) => {
  const response = await fetch(url)
  if (!response.ok) throw new Error(`Geoapify Error: ${response.statusText}`)
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

    const routeUrl = `https://api.geoapify.com/v1/routing?waypoints=${start.lat},${start.lng}|${end.lat},${end.lng}&mode=truck&apiKey=${API_KEY}`
    const data = await fetchGeoapify(routeUrl)
    
    const routeData = data.features[0]
    stats.value = routeData.properties

    const geometry = routeData.geometry.coordinates[0].map(c => [c[1], c[0]]) 
    routeLayer.value = L.polyline(geometry, { color: '#2563eb', weight: 6, opacity: 0.8 }).addTo(map.value)
    
    map.value.fitBounds(routeLayer.value.getBounds(), { padding: [50, 50] })

    // Add markers
    L.marker([start.lat, start.lng]).addTo(map.value)
      .bindPopup(`<b style="color:green">START:</b> ${startCity.value}`).openPopup()
    
    L.marker([end.lat, end.lng]).addTo(map.value)
      .bindPopup(`<b style="color:red">END:</b> ${endCity.value}`)

  } catch (error) {
    alert('Error: ' + error.message)
  } finally {
    loading.value = false
  }
}

// === NEW FEATURES ===

// 1. Open Google Maps - Funcție corectată și adăugată
const openGoogleMaps = () => {
  if (!startCity.value || !endCity.value) return
  const url = `https://www.google.com/maps/dir/?api=1&origin=${encodeURIComponent(startCity.value)}&destination=${encodeURIComponent(endCity.value)}&travelmode=driving`
  window.open(url, '_blank')
}

// 2. Format Time (seconds to H:MM)
const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return `${h}h ${m}m`
}

// 3. Fuel Calculation (Truck Avg)
const calculateFuel = (distanceMeters) => {
  const km = distanceMeters / 1000
  // Presupunem un consum de 28L/100km pentru camion
  return (km * 0.28).toFixed(1)
}

// 4. AI Advice Logic
const getAiAdvice = (distanceMeters) => {
  const km = distanceMeters / 1000
  if (km > 500) return "Long haul detected. Recommend mandatory rest stop every 4.5 hours. Check tire pressure before departure."
  if (km > 100) return "Medium range trip. Traffic looks clear on main highways. Eco-mode recommended."
  return "Short trip. Direct route is optimal. No special maintenance required."
}
</script>

<style>
.animation-fade-in {
  animation: fadeIn 0.5s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>