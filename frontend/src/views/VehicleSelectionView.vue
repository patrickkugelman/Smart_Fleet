<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-6">
    <div class="max-w-6xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl font-bold text-gray-800 mb-3">🚗 Select Your Vehicle</h1>
        <p class="text-lg text-gray-600">Choose a vehicle from the available fleet to start your driving journey</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin">
          <div class="w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full"></div>
        </div>
        <p class="mt-4 text-gray-600">Loading available vehicles...</p>
      </div>

      <!-- Error State -->
      <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 mb-6">
        <p class="text-red-800 font-semibold">❌ Error: {{ error }}</p>
      </div>

      <!-- Available Vehicles Grid -->
      <div v-if="!loading && availableVehicles.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
          v-for="vehicle in availableVehicles" 
          :key="vehicle.id"
          class="bg-white rounded-lg shadow-lg hover:shadow-2xl transition-all duration-300 overflow-hidden cursor-pointer hover:-translate-y-1"
          @click="selectVehicle(vehicle)"
        >
          <!-- Vehicle Icon -->
          <div class="bg-gradient-to-r from-blue-500 to-indigo-600 p-6 text-center">
            <div class="text-6xl">🚗</div>
          </div>

          <!-- Vehicle Details -->
          <div class="p-6">
            <h3 class="text-2xl font-bold text-gray-800 mb-1">{{ vehicle.plate }}</h3>
            <p class="text-gray-600 text-sm mb-4">License Plate</p>

            <!-- Information Grid -->
            <div class="space-y-3 mb-6 pb-6 border-b border-gray-200">
              <div class="flex items-center justify-between">
                <span class="text-gray-600 font-medium">Brand:</span>
                <span class="font-bold text-gray-800">{{ vehicle.brand }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-gray-600 font-medium">Type:</span>
                <span class="font-bold text-gray-800">{{ vehicle.type }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-gray-600 font-medium">Status:</span>
                <span class="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-semibold">Available</span>
              </div>
            </div>

            <!-- Location -->
            <div class="text-sm text-gray-600 mb-6">
              <p>📍 Last Location: {{ vehicle.lat?.toFixed(4) }}, {{ vehicle.lng?.toFixed(4) }}</p>
            </div>

            <!-- Select Button -->
            <button 
              @click="selectVehicle(vehicle)"
              :disabled="selecting === vehicle.id"
              class="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-3 rounded-lg hover:from-blue-700 hover:to-indigo-700 font-semibold transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="selecting === vehicle.id" class="inline-block">✓ Selecting...</span>
              <span v-else>Select This Vehicle</span>
            </button>
          </div>
        </div>
      </div>

      <!-- No Vehicles Available -->
      <div v-if="!loading && availableVehicles.length === 0" class="bg-yellow-50 border-2 border-yellow-200 rounded-lg p-12 text-center">
        <p class="text-2xl mb-2">🚫</p>
        <p class="text-lg text-yellow-800 font-semibold">No vehicles available</p>
        <p class="text-yellow-700 mt-2">Please contact your fleet manager to assign a vehicle.</p>
        <button 
          @click="logout"
          class="mt-6 bg-yellow-600 text-white px-6 py-2 rounded-lg hover:bg-yellow-700"
        >
          Return to Login
        </button>
      </div>

      <!-- Selected Vehicle Info -->
      <div v-if="selectedVehicle" class="mt-12 bg-green-50 border-2 border-green-200 rounded-lg p-6">
        <h3 class="text-xl font-bold text-green-800 mb-4">✅ Vehicle Selected</h3>
        <p class="text-green-700">You have selected <strong>{{ selectedVehicle.plate }} ({{ selectedVehicle.brand }})</strong></p>
        <p class="text-green-700 text-sm mt-2">Redirecting to your dashboard...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref(null)
const availableVehicles = ref([])
const selectedVehicle = ref(null)
const selecting = ref(null)

onMounted(async () => {
  await loadAvailableVehicles()
})

const loadAvailableVehicles = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await axios.get('/api/vehicles/available')
    availableVehicles.value = response.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load available vehicles'
    console.error('Error loading vehicles:', err)
  } finally {
    loading.value = false
  }
}

const selectVehicle = async (vehicle) => {
  selecting.value = vehicle.id
  try {
    await axios.post('/api/drivers/assign-vehicle', {
      vehicleId: vehicle.id
    })
    
    selectedVehicle.value = vehicle
    
    // Show success message briefly
    setTimeout(() => {
      router.push('/driver-dashboard')
    }, 1000)
  } catch (err) {
    selecting.value = null
    error.value = err.response?.data?.message || 'Failed to assign vehicle. Please try again.'
    console.error('Error assigning vehicle:', err)
  }
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
