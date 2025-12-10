<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-6">Driver Management</h1>

    <!-- DEBUG Panel -->
    <div class="bg-yellow-100 p-4 rounded mb-4">
      <p><strong>Token:</strong> {{ authStore.token ? 'EXISTS ✅' : 'MISSING ❌' }}</p>
      <p><strong>Role:</strong> {{ authStore.userRole }}</p>
      <p><strong>Drivers Count:</strong> {{ drivers.length }}</p>
    </div>

    <!-- Refresh Button -->
    <button 
      @click="fetchDrivers"
      class="mb-4 px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">
      🔄 Refresh Drivers
    </button>

    <!-- Drivers Table -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Username</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">License</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Vehicle</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trips</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-if="drivers.length === 0">
            <td colspan="8" class="px-6 py-8 text-center text-gray-500">
              No drivers found. Create a driver account via registration.
            </td>
          </tr>
          <tr v-for="driver in drivers" :key="driver.id">
            <td class="px-6 py-4">
              <div class="font-medium">{{ driver.name }}</div>
            </td>
            <td class="px-6 py-4 text-sm">{{ driver.username }}</td>
            <td class="px-6 py-4 text-sm text-gray-500">{{ driver.email }}</td>
            <td class="px-6 py-4 text-sm">{{ driver.license }}</td>
            <td class="px-6 py-4 text-sm">
              <span v-if="driver.vehiclePlate" class="text-blue-600">
                {{ driver.vehicleBrand }} - {{ driver.vehiclePlate }}
              </span>
              <span v-else class="text-gray-400">No vehicle assigned</span>
            </td>
            <td class="px-6 py-4">
              <span :class="statusClass(driver.status)" class="px-2 py-1 rounded text-xs">
                {{ driver.status }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm">{{ driver.tripCount || 0 }}</td>
            <td class="px-6 py-4 text-sm space-x-2">
              <button 
                @click="assignVehicle(driver)"
                class="text-blue-600 hover:text-blue-800"
                :disabled="loading">
                {{ driver.vehicleId ? 'Change Vehicle' : 'Assign Vehicle' }}
              </button>
              <button 
                @click="viewDriverTrips(driver)"
                class="text-green-600 hover:text-green-800">
                View Trips
              </button>
              <button 
                @click="openAssignTripModal(driver)"
                class="text-purple-600 hover:text-purple-800"
                :disabled="!driver.vehicleId">
                Assign Trip
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Assign Vehicle Modal -->
    <div v-if="showAssignVehicleModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-96">
        <h2 class="text-xl font-bold mb-4">Assign Vehicle to {{ selectedDriver?.name }}</h2>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-2">Select Vehicle</label>
            <select v-model="selectedVehicleId" class="w-full border rounded px-3 py-2">
              <option value="">-- Select Vehicle --</option>
              <option v-for="vehicle in availableVehicles" :key="vehicle.id" :value="vehicle.id">
                {{ vehicle.brand }} - {{ vehicle.plate }} ({{ vehicle.type }})
              </option>
            </select>
          </div>
        </div>

        <div class="flex justify-end space-x-2 mt-6">
          <button @click="closeAssignVehicleModal" class="px-4 py-2 border rounded">Cancel</button>
          <button 
            @click="confirmAssignVehicle" 
            :disabled="!selectedVehicleId || loading"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400">
            Assign
          </button>
        </div>
      </div>
    </div>

    <!-- Assign Trip Modal -->
    <div v-if="showAssignTripModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-96">
        <h2 class="text-xl font-bold mb-4">Assign Trip to {{ selectedDriver?.name }}</h2>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-1">Start Location *</label>
            <input v-model="tripForm.startLocation" class="w-full border rounded px-3 py-2" />
          </div>
          
          <div>
            <label class="block text-sm font-medium mb-1">End Location *</label>
            <input v-model="tripForm.endLocation" class="w-full border rounded px-3 py-2" />
          </div>
          
          <div>
            <label class="block text-sm font-medium mb-1">Distance (km)</label>
            <input v-model.number="tripForm.distance" type="number" class="w-full border rounded px-3 py-2" />
          </div>
          
          <div>
            <label class="block text-sm font-medium mb-1">Start Time</label>
            <input v-model="tripForm.startTime" type="datetime-local" class="w-full border rounded px-3 py-2" />
          </div>
        </div>

        <div class="flex justify-end space-x-2 mt-6">
          <button @click="showAssignTripModal = false" class="px-4 py-2 border rounded">Cancel</button>
          <button @click="assignTrip" class="px-4 py-2 bg-blue-600 text-white rounded">Assign</button>
        </div>
      </div>
    </div>

    <!-- View Trips Modal -->
    <div v-if="showTripsModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-3/4 max-h-96 overflow-y-auto">
        <h2 class="text-xl font-bold mb-4">Trips for {{ selectedDriver?.name }}</h2>
        
        <div v-if="driverTrips.length === 0" class="text-gray-500 text-center py-4">
          No trips found for this driver.
        </div>
        
        <table v-else class="min-w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-2 text-left text-xs">Start</th>
              <th class="px-4 py-2 text-left text-xs">End</th>
              <th class="px-4 py-2 text-left text-xs">Time</th>
              <th class="px-4 py-2 text-left text-xs">Status</th>
              <th class="px-4 py-2 text-left text-xs">Distance</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="trip in driverTrips" :key="trip.id" class="border-t">
              <td class="px-4 py-2 text-sm">{{ trip.startLocation }}</td>
              <td class="px-4 py-2 text-sm">{{ trip.endLocation }}</td>
              <td class="px-4 py-2 text-sm">{{ formatDate(trip.startTime) }}</td>
              <td class="px-4 py-2 text-sm">{{ trip.status }}</td>
              <td class="px-4 py-2 text-sm">{{ trip.distance }} km</td>
            </tr>
          </tbody>
        </table>

        <button @click="showTripsModal = false" class="mt-4 px-4 py-2 bg-gray-600 text-white rounded">
          Close
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const drivers = ref([])
const availableVehicles = ref([])
const selectedDriver = ref(null)
const selectedVehicleId = ref('')
const showAssignVehicleModal = ref(false)
const showAssignTripModal = ref(false)
const showTripsModal = ref(false)
const driverTrips = ref([])
const loading = ref(false)

const tripForm = ref({
  startLocation: '',
  endLocation: '',
  distance: 0,
  startTime: ''
})

const fetchDrivers = async () => {
  try {
    console.log('📡 Fetching drivers...')
    const response = await axios.get('/api/drivers', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    drivers.value = response.data
    console.log('✅ Drivers loaded:', drivers.value)
  } catch (error) {
    console.error('❌ Error fetching drivers:', error)
    alert('Failed to load drivers: ' + (error.response?.data?.message || error.message))
  }
}

const fetchAvailableVehicles = async () => {
  try {
    const response = await axios.get('/api/vehicles', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    availableVehicles.value = response.data.filter(v => v.status === 'AVAILABLE' || v.status === 'IDLE')
  } catch (error) {
    console.error('Error fetching vehicles:', error)
  }
}

const assignVehicle = async (driver) => {
  selectedDriver.value = driver
  await fetchAvailableVehicles()
  selectedVehicleId.value = driver.vehicleId || ''
  showAssignVehicleModal.value = true
}

const closeAssignVehicleModal = () => {
  showAssignVehicleModal.value = false
  selectedDriver.value = null
  selectedVehicleId.value = ''
}

const confirmAssignVehicle = async () => {
  if (!selectedVehicleId.value) {
    alert('Please select a vehicle')
    return
  }

  loading.value = true
  try {
    // Simple approach: Update driver with vehicle_id
    await axios.put(
      `/api/drivers/${selectedDriver.value.id}`,
      { vehicleId: selectedVehicleId.value },
      { headers: { Authorization: `Bearer ${authStore.token}` } }
    )
    
    alert('Vehicle assigned successfully!')
    closeAssignVehicleModal()
    fetchDrivers()
  } catch (error) {
    console.error('Error assigning vehicle:', error)
    alert('Failed to assign vehicle: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const openAssignTripModal = (driver) => {
  if (!driver.vehicleId) {
    alert('Driver must have a vehicle assigned first!')
    return
  }
  selectedDriver.value = driver
  tripForm.value = {
    startLocation: '',
    endLocation: '',
    distance: 0,
    startTime: new Date().toISOString().slice(0, 16)
  }
  showAssignTripModal.value = true
}

const assignTrip = async () => {
  try {
    await axios.post(
      `/api/drivers/${selectedDriver.value.id}/assign-trip`,
      tripForm.value,
      { headers: { Authorization: `Bearer ${authStore.token}` } }
    )
    alert('Trip assigned successfully!')
    showAssignTripModal.value = false
    fetchDrivers()
  } catch (error) {
    console.error('Error assigning trip:', error)
    alert('Failed to assign trip: ' + (error.response?.data?.message || error.message))
  }
}

const viewDriverTrips = async (driver) => {
  selectedDriver.value = driver
  try {
    const response = await axios.get(
      `/api/drivers/${driver.id}/trips`,
      { headers: { Authorization: `Bearer ${authStore.token}` } }
    )
    driverTrips.value = response.data
    showTripsModal.value = true
  } catch (error) {
    console.error('Error fetching trips:', error)
    alert('Failed to load trips')
  }
}

const statusClass = (status) => {
  return {
    'AVAILABLE': 'bg-green-100 text-green-800',
    'ON_TRIP': 'bg-blue-100 text-blue-800',
    'BUSY': 'bg-yellow-100 text-yellow-800'
  }[status] || 'bg-gray-100 text-gray-800'
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

onMounted(() => {
  console.log('🚀 DriversManagement mounted')
  fetchDrivers()
})
</script>