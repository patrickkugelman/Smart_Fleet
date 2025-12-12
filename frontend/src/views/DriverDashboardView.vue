<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <h2 class="text-3xl font-bold text-gray-800 mb-6">👤 My Driver Dashboard</h2>

    <div v-if="loading" class="text-center py-10">
      <div class="animate-spin inline-block w-8 h-8 border-4 border-current border-t-transparent text-blue-600 rounded-full"></div>
      <p class="mt-2 text-gray-600">Loading dashboard data...</p>
    </div>

    <div v-else-if="assignedVehicle" class="space-y-6">
      
      <div class="bg-white p-6 rounded-lg shadow-lg mb-6 border-l-4 border-blue-500">
        <h3 class="font-bold text-xl text-gray-800 mb-4">🚀 Active Assignments ({{ activeJobs.length }})</h3>

        <div v-if="activeJobs.length > 0" class="space-y-4">
          <div v-for="job in activeJobs" :key="job.id" class="bg-blue-50 p-4 rounded-lg border border-blue-100 relative">
            
            <div class="flex justify-between items-start mb-2">
              <div>
                <span class="text-xs font-bold px-2 py-1 rounded uppercase tracking-wide" 
                      :class="job.status === 'ON_TRIP' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'">
                  {{ job.status }}
                </span>
                <h4 class="text-lg font-bold text-gray-800 mt-1">
                  {{ job.startLocation }} ➝ {{ job.endLocation }}
                </h4>
              </div>
              <div class="text-right">
                <p class="text-xl font-bold text-blue-600">{{ job.distance || job.km }} km</p>
                <p class="text-xs text-gray-500">Distance</p>
              </div>
            </div>

            <div class="mt-4 pt-4 border-t border-blue-200 flex justify-end gap-3">
               <button 
                  @click="startNavigation(job)"
                  class="text-blue-600 hover:text-blue-800 font-medium text-sm flex items-center gap-1">
                  <span>🗺️</span> Maps
               </button>

               <button 
                  v-if="job.status === 'ASSIGNED'"
                  @click="updateJobStatus(job.id, 'start')"
                  class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 font-bold text-sm shadow transition hover:scale-105">
                  ▶️ Start Trip
               </button>

               <button 
                  v-if="job.status === 'ON_TRIP'"
                  @click="updateJobStatus(job.id, 'complete')"
                  class="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-900 font-bold text-sm shadow transition hover:scale-105">
                  ✅ Complete Job
               </button>
            </div>
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
          <div class="bg-gray-50 p-3 rounded">
            <p class="text-xs text-gray-500 uppercase">Brand</p>
            <p class="font-bold">{{ assignedVehicle.brand || 'N/A' }}</p>
          </div>
          <div class="bg-gray-50 p-3 rounded">
            <p class="text-xs text-gray-500 uppercase">Type</p>
            <p class="font-bold">{{ assignedVehicle.type }}</p>
          </div>
          <div class="bg-gray-50 p-3 rounded">
            <p class="text-xs text-gray-500 uppercase">Status</p>
            <p class="font-bold" :class="getStatusClass(assignedVehicle.status)">{{ assignedVehicle.status }}</p>
          </div>
        </div>
      </div>

    </div>

    <div v-else class="bg-yellow-50 border-l-4 border-yellow-600 p-6 rounded-lg mt-10">
      <p class="text-yellow-800 font-semibold">⚠️ No Vehicle Assigned</p>
      <p class="text-yellow-700 mt-2">Contact your fleet manager.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const assignedVehicle = ref(null)
const activeJobs = ref([]) // Lista, nu doar unul
const loading = ref(true)

// Load Data
const loadDashboard = async () => {
  loading.value = true
  try {
    const driverRes = await axios.get('/api/drivers/me')
    const driver = driverRes.data

    // 1. Get Vehicle
    if (driver.vehicleId) {
       const vehicleRes = await axios.get(`/api/vehicles/${driver.vehicleId}`)
       assignedVehicle.value = vehicleRes.data
    }

    // 2. Get All Active Trips
    if (driver.id) {
       const tripsRes = await axios.get(`/api/trips/driver/${driver.id}`)
       
       // Filtrăm toate care sunt ASSIGNED sau ON_TRIP
       activeJobs.value = tripsRes.data.filter(t => 
          ['ASSIGNED', 'ON_TRIP'].includes(t.status)
       )
    }
  } catch (error) {
    console.error('Error loading dashboard:', error)
  } finally {
    loading.value = false
  }
}

// Acțiune Butoane (Start / Complete)
const updateJobStatus = async (tripId, action) => {
  if (!confirm(`Are you sure you want to ${action} this trip?`)) return;

  try {
    // Apelăm endpoint-urile noi din Backend
    await axios.post(`/api/trips/${tripId}/${action}`, {}, {
       headers: { Authorization: `Bearer ${authStore.token}` }
    })
    
    // Reîncărcăm lista pentru a vedea noul status
    await loadDashboard()
    alert(`Trip ${action}d successfully!`)
    
  } catch (error) {
    console.error(error)
    alert('Failed to update trip status.')
  }
}

const startNavigation = (job) => {
  const origin = encodeURIComponent(job.startLocation)
  const destination = encodeURIComponent(job.endLocation)
  const url = `https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${destination}&travelmode=driving`
  window.open(url, '_blank')
}

const getStatusClass = (status) => {
  switch (status) {
    case 'ON_TRIP': return 'text-blue-600'
    case 'AVAILABLE': return 'text-green-600'
    case 'MAINTENANCE': return 'text-red-600'
    default: return 'text-gray-600'
  }
}

onMounted(() => {
  loadDashboard()
})
</script>