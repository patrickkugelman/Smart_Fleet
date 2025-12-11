<template>
  <div class="relative w-full h-full">
    <div id="map" class="w-full h-full z-0"></div>

    <div class="absolute top-4 right-4 bg-white p-4 rounded-lg shadow-xl z-10 max-w-sm border-l-4 border-blue-600">
      <h3 class="font-bold text-gray-800 text-lg mb-1">🗺️ Live Fleet Map</h3>
      <p class="text-sm text-gray-500 mb-3">Real-time GPS tracking</p>
      
      <div v-if="vehicles.length > 0" class="space-y-2 max-h-60 overflow-y-auto">
        <div v-for="v in vehicles" :key="v.id" class="flex justify-between items-center text-sm p-2 bg-gray-50 rounded hover:bg-blue-50 transition cursor-pointer" @click="focusVehicle(v)">
          <div>
            <span class="font-bold text-blue-700">{{ v.plate }}</span>
            <span class="text-gray-500 text-xs ml-2">({{ v.brand }})</span>
          </div>
          <span :class="getStatusClass(v.status)" class="px-2 py-0.5 rounded text-xs font-bold">{{ v.status }}</span>
        </div>
      </div>
      <div v-else class="text-gray-400 text-sm italic">No active vehicles found...</div>
      
      <div class="mt-3 text-xs text-gray-400 text-right">
        Auto-refreshing every 2s ⏳
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const vehicles = ref([])
let map = null
let markers = {} // Obiect pentru a ține evidența markerelor: { id: markerObj }
let intervalId = null

// Iconiță personalizată pentru camioane
const truckIcon = L.icon({
  iconUrl: 'https://cdn-icons-png.flaticon.com/512/741/741407.png', // Iconiță generică de camion
  iconSize: [40, 40],
  iconAnchor: [20, 20],
  popupAnchor: [0, -20]
})

const initMap = () => {
  // Centrăm harta pe România/Europa Centrală
  map = L.map('map').setView([46.7712, 23.5889], 7) 

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(map)
}

const fetchVehicles = async () => {
  try {
    // Admin vede toate, Driver vede doar pe a lui (dar driver-ul e pe dashboard de obicei)
    const endpoint = authStore.userRole === 'ROLE_ADMIN' ? '/api/vehicles' : '/api/vehicles/available'
    
    const response = await axios.get(endpoint, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    
    vehicles.value = response.data
    updateMarkers()
  } catch (error) {
    console.error('Error fetching locations:', error)
  }
}

const updateMarkers = () => {
  vehicles.value.forEach(v => {
    // Verificăm dacă vehiculul are coordonate valide
    if (v.lat && v.lng && v.lat !== 0 && v.lng !== 0) {
      
      // 1. Dacă markerul există deja, doar îi actualizăm poziția (Mișcare fluidă)
      if (markers[v.id]) {
        const newLatLng = new L.LatLng(v.lat, v.lng)
        markers[v.id].setLatLng(newLatLng)
        
        // Actualizăm și conținutul popup-ului dacă s-a schimbat statusul
        markers[v.id].setPopupContent(`
          <div class="text-center">
            <b class="text-blue-600 text-lg">${v.plate}</b><br>
            ${v.brand} (${v.type})<br>
            <span class="text-xs font-bold ${v.status === 'ACTIVE' ? 'text-green-600' : 'text-gray-600'}">${v.status}</span>
          </div>
        `)
      } 
      // 2. Dacă markerul nu există, îl creăm
      else {
        const marker = L.marker([v.lat, v.lng], { icon: truckIcon })
          .addTo(map)
          .bindPopup(`
            <div class="text-center">
              <b class="text-blue-600 text-lg">${v.plate}</b><br>
              ${v.brand} (${v.type})<br>
              <span class="text-xs font-bold ${v.status === 'ACTIVE' ? 'text-green-600' : 'text-gray-600'}">${v.status}</span>
            </div>
          `)
        
        markers[v.id] = marker
      }
    }
  })
}

const focusVehicle = (v) => {
  if (v.lat && v.lng && map) {
    map.flyTo([v.lat, v.lng], 14, { duration: 1.5 })
    if (markers[v.id]) {
      markers[v.id].openPopup()
    }
  } else {
    alert('Acest vehicul nu are locație GPS activă.')
  }
}

const getStatusClass = (status) => {
  switch(status) {
    case 'ACTIVE': return 'bg-green-100 text-green-800'
    case 'IDLE': return 'bg-yellow-100 text-yellow-800'
    case 'ON_TRIP': return 'bg-blue-100 text-blue-800'
    default: return 'bg-gray-100 text-gray-600'
  }
}

onMounted(() => {
  initMap()
  fetchVehicles() // Prima încărcare
  
  // Polling: Actualizare la fiecare 2 secunde
  intervalId = setInterval(fetchVehicles, 2000)
})

onUnmounted(() => {
  // Curățăm intervalul când părăsim pagina pentru a nu consuma resurse
  if (intervalId) clearInterval(intervalId)
})
</script>

<style>
/* Stil pentru popup-uri Leaflet */
.leaflet-popup-content-wrapper {
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}
</style>