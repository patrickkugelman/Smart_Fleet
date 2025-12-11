<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <h2 class="text-3xl font-bold text-gray-800 mb-6">👤 My Driver Dashboard</h2>

    <div v-if="loading" class="text-center py-10">
      <div class="animate-spin inline-block w-8 h-8 border-4 border-current border-t-transparent text-blue-600 rounded-full"></div>
      <p class="mt-2 text-gray-600">Loading dashboard data...</p>
    </div>

    <div v-else-if="assignedVehicle" class="space-y-6">
      
      <div class="bg-white p-6 rounded-lg shadow-lg mb-6 border-l-4" 
           :class="currentJob ? 'border-blue-500' : 'border-gray-300'">
        <div class="flex justify-between items-center mb-4">
          <h3 class="font-bold text-xl text-gray-800">🚀 Current Job Status</h3>
          <span class="px-4 py-1 rounded-full text-sm font-bold shadow-sm" 
                :class="currentJob ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-600'">
            {{ currentJob ? currentJob.status : 'IDLE' }}
          </span>
        </div>

        <div v-if="currentJob" class="bg-blue-50 p-4 rounded-lg">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-500 uppercase font-semibold">Route</p>
              <p class="text-lg font-bold text-gray-800">{{ currentJob.startLocation }} ➝ {{ currentJob.endLocation }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500 uppercase font-semibold">Distance</p>
              <p class="text-lg font-bold text-gray-800">{{ currentJob.distance || currentJob.km }} km</p>
            </div>
          </div>
          
          <div class="mt-4 pt-4 border-t border-blue-200 flex justify-end">
             <button 
                @click="startNavigation"
                class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 font-semibold shadow transition transform hover:scale-105 flex items-center gap-2">
                <span>🗺️</span> Start Navigation
             </button>
          </div>
        </div>

        <div v-else class="border-2 border-dashed border-gray-200 rounded-lg p-8 text-center bg-gray-50">
          <p class="text-gray-500 text-lg">No active jobs assigned.</p>
          <p class="text-sm text-gray-400">Relax and wait for the fleet manager.</p>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-lg p-6">
        <h3 class="text-2xl font-bold mb-4">🚗 My Vehicle: {{ assignedVehicle.plate }}</h3>

        <div class="grid grid-cols-3 gap-4 mb-6">
          <div class="bg-blue-50 p-4 rounded-lg">
            <p class="text-sm text-gray-600">Brand</p>
            <p class="text-lg font-bold text-blue-600">{{ assignedVehicle.brand || 'N/A' }}</p>
          </div>
          <div class="bg-blue-50 p-4 rounded-lg">
            <p class="text-sm text-gray-600">Type</p>
            <p class="text-lg font-bold text-blue-600">{{ assignedVehicle.type }}</p>
          </div>
          <div class="bg-blue-50 p-4 rounded-lg">
            <p class="text-sm text-gray-600">Status</p>
            <p :class="getStatusClass(assignedVehicle.status)" class="text-lg font-bold">
              {{ assignedVehicle.status }}
            </p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-lg p-6">
        <h4 class="text-xl font-bold mb-4">📊 Driving Statistics</h4>
        <div class="grid grid-cols-3 gap-4 mb-6">
          <div class="bg-blue-50 p-4 rounded-lg text-center border border-blue-200">
            <p class="text-sm text-gray-600">Total KM</p>
            <p class="text-3xl font-bold text-blue-600">{{ totalKm }}</p>
          </div>
          <div class="bg-green-50 p-4 rounded-lg text-center border border-green-200">
            <p class="text-sm text-gray-600">Today</p>
            <p class="text-3xl font-bold text-green-600">{{ todayKm }}</p>
          </div>
          <div class="bg-purple-50 p-4 rounded-lg text-center border border-purple-200">
            <p class="text-sm text-gray-600">This Week</p>
            <p class="text-3xl font-bold text-purple-600">{{ weekKm }}</p>
          </div>
        </div>
        <div class="bg-gray-50 p-6 rounded-lg">
          <canvas ref="chartCanvas"></canvas>
        </div>
      </div>
    </div>

    <div v-else class="bg-yellow-50 border-l-4 border-yellow-600 p-6 rounded-lg mt-10">
      <p class="text-yellow-800 font-semibold">⚠️ No Vehicle Assigned</p>
      <p class="text-yellow-700 mt-2">Contact your fleet manager to get a vehicle assigned.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { Chart } from 'chart.js/auto'

const assignedVehicle = ref(null)
const currentJob = ref(null)
const loading = ref(true)

// Stats Demo Data
const totalKm = ref(1250)
const todayKm = ref(85)
const weekKm = ref(450)
const chartCanvas = ref(null)

onMounted(async () => {
  console.log('🔄 Dashboard mounting...')
  try {
    // 1. Get Driver Profile
    const driverRes = await axios.get('/api/drivers/me')
    const driver = driverRes.data
    console.log('👤 Driver loaded:', driver)

    // 2. Load Vehicle
    if (driver.vehicleId) {
       console.log('🚗 Fetching vehicle:', driver.vehicleId)
       const vehicleRes = await axios.get(`/api/vehicles/${driver.vehicleId}`)
       assignedVehicle.value = vehicleRes.data
    } else {
       console.warn('⚠️ Driver has no vehicleId')
    }

    // 3. Load Active Jobs
    if (driver.id) {
       console.log('📋 Fetching trips for driver:', driver.id)
       const tripsRes = await axios.get(`/api/trips/driver/${driver.id}`)
       
       // Căutăm joburi active
       const activeTrip = tripsRes.data.find(t => 
          ['ASSIGNED', 'ON_TRIP', 'IN_PROGRESS'].includes(t.status)
       )
       
       if (activeTrip) {
          console.log('✅ Active job found:', activeTrip)
          currentJob.value = activeTrip
       } else {
          console.log('ℹ️ No active jobs found.')
       }
    }

  } catch (error) {
    console.error('❌ Error loading dashboard data:', error)
    if(error.response) console.error('Response:', error.response.data)
  } finally {
    loading.value = false
    setTimeout(initChart, 200)
  }
})

const startNavigation = () => {
  if (!currentJob.value) return
  const origin = encodeURIComponent(currentJob.value.startLocation)
  const destination = encodeURIComponent(currentJob.value.endLocation)
  const url = `https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${destination}&travelmode=driving`
  window.open(url, '_blank')
}

const initChart = () => {
  if (chartCanvas.value) {
    new Chart(chartCanvas.value, {
      type: 'line',
      data: {
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        datasets: [{
          label: 'Distance (km)',
          data: [65, 72, 58, 91, 78, 45, 87],
          borderColor: 'rgb(59, 130, 246)',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          tension: 0.4,
          fill: true
        }]
      },
      options: { responsive: true }
    })
  }
}

const getStatusClass = (status) => {
  switch (status) {
    case 'ACTIVE': return 'text-green-600'
    case 'IDLE': return 'text-yellow-600'
    case 'MAINTENANCE': return 'text-red-600'
    default: return 'text-gray-600'
  }
}
</script>