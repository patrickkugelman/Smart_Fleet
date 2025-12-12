<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-6 text-gray-800">Driver Management</h1>

    <button @click="fetchDrivers" class="mb-6 px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 flex items-center gap-2">
      🔄 Refresh Drivers
    </button>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full text-left border-collapse">
        <thead class="bg-gray-50 border-b">
          <tr>
            <th class="p-4 text-gray-600 font-semibold">Name</th>
            <th class="p-4 text-gray-600 font-semibold">Details</th>
            <th class="p-4 text-gray-600 font-semibold">Vehicle</th>
            <th class="p-4 text-gray-600 font-semibold">Status</th>
            <th class="p-4 text-gray-600 font-semibold">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="drivers.length === 0">
            <td colspan="5" class="p-6 text-center text-gray-500">No drivers found.</td>
          </tr>
          <tr v-for="driver in drivers" :key="driver.id" class="border-b hover:bg-gray-50 transition">
            
            <td class="p-4 flex items-center gap-3">
              <img 
                :src="driver.avatarUrl ? 'http://localhost:8080' + driver.avatarUrl : 'https://via.placeholder.com/40'" 
                class="w-10 h-10 rounded-full border border-gray-300 object-cover"
              />
              <div>
                <p class="font-bold text-gray-800">{{ driver.name }}</p>
                <p class="text-xs text-gray-500">@{{ driver.username }}</p>
              </div>
            </td>

            <td class="p-4">
              <p class="text-sm text-gray-600">{{ driver.email }}</p>
              <p class="text-xs text-gray-400">Lic: {{ driver.license }}</p>
            </td>

            <td class="p-4">
              <span v-if="driver.vehiclePlate" class="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-bold">
                {{ driver.vehicleBrand }} - {{ driver.vehiclePlate }}
              </span>
              <span v-else class="text-gray-400 text-sm italic">Unassigned</span>
            </td>

            <td class="p-4">
              <span :class="getStatusClass(driver.status)" class="px-3 py-1 rounded-full text-xs font-bold">
                {{ driver.status }}
              </span>
            </td>

            <td class="p-4 space-x-2">
              <button @click="openModal('vehicle', driver)" class="text-blue-600 hover:text-blue-800 font-medium text-sm">
                Change Vehicle
              </button>
              <button @click="viewTrips(driver)" class="text-green-600 hover:text-green-800 font-medium text-sm">
                Trips
              </button>
              <button @click="openModal('trip', driver)" class="text-purple-600 hover:text-purple-800 font-medium text-sm">
                Assign Job
              </button>
              <button @click="deleteDriver(driver.id)" class="text-red-500 hover:text-red-700 font-bold text-sm bg-red-50 px-2 py-1 rounded">
                Delete 🗑️
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <ChangeVehicleModal 
      v-if="modals.vehicle"
      :driverId="selectedDriver.id"
      :driverName="selectedDriver.name"
      :currentVehicleId="selectedDriver.vehicleId"
      @close="modals.vehicle = false"
      @saved="fetchDrivers" 
    />

    <div v-if="modals.trip" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white p-6 rounded-lg w-96">
            <h3 class="text-lg font-bold mb-4">Assign Trip to {{ selectedDriver?.name }}</h3>
            <input v-model="tripForm.startLocation" placeholder="Start Location" class="w-full border p-2 mb-2 rounded">
            <input v-model="tripForm.endLocation" placeholder="End Location" class="w-full border p-2 mb-2 rounded">
            <input v-model="tripForm.distance" placeholder="Distance (km)" type="number" class="w-full border p-2 mb-4 rounded">
            <div class="flex justify-end gap-2">
                <button @click="modals.trip = false" class="px-4 py-2 bg-gray-200 rounded">Cancel</button>
                <button @click="assignTrip" class="px-4 py-2 bg-purple-600 text-white rounded">Assign</button>
            </div>
        </div>
    </div>

    <div v-if="modals.history" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white p-6 rounded-lg w-2/3 max-h-[80vh] overflow-y-auto">
            <h3 class="text-lg font-bold mb-4">Trips History: {{ selectedDriver?.name }}</h3>
            <div v-for="trip in driverTrips" :key="trip.id" class="border-b p-2">
                {{ trip.startLocation }} -> {{ trip.endLocation }} ({{ trip.distance }} km) - {{ trip.status }}
            </div>
            <button @click="modals.history = false" class="mt-4 px-4 py-2 bg-gray-200 rounded">Close</button>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'
import ChangeVehicleModal from '@/components/ChangeVehicleModal.vue' // Importam noua componenta

const authStore = useAuthStore()
const drivers = ref([])
const selectedDriver = ref(null)
const driverTrips = ref([])

const modals = reactive({
    trip: false,
    history: false,
    vehicle: false
})

const tripForm = reactive({
    startLocation: '',
    endLocation: '',
    distance: ''
})

const fetchDrivers = async () => {
  try {
    const res = await axios.get('/api/drivers', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    drivers.value = res.data
  } catch (error) {
    console.error('Error fetching drivers:', error)
  }
}

const deleteDriver = async (id) => {
  if (!confirm('ATENȚIE: Această acțiune va șterge șoferul și contul său de utilizator definitiv. Continui?')) return

  try {
    await axios.delete(`/api/drivers/${id}`, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    alert('Driver deleted successfully.')
    fetchDrivers() 
  } catch (error) {
    console.error('Delete error:', error)
    alert('Failed to delete driver.')
  }
}

const openModal = (type, driver) => {
    selectedDriver.value = driver
    
    // Deschidem modala corecta bazat pe tip
    if (type === 'trip') modals.trip = true
    if (type === 'vehicle') modals.vehicle = true // Aici era problema inainte!
}

const assignTrip = async () => {
    try {
        await axios.post(`/api/drivers/${selectedDriver.value.id}/assign-trip`, tripForm, {
            headers: { Authorization: `Bearer ${authStore.token}` }
        })
        alert('Trip assigned!')
        modals.trip = false
        fetchDrivers()
    } catch (e) {
        alert('Failed to assign trip')
    }
}

const viewTrips = async (driver) => {
    selectedDriver.value = driver
    const res = await axios.get(`/api/trips/driver/${driver.id}`, {
        headers: { Authorization: `Bearer ${authStore.token}` }
    })
    driverTrips.value = res.data
    modals.history = true
}

const getStatusClass = (status) => {
  switch(status) {
    case 'ON_TRIP': return 'bg-blue-100 text-blue-800'
    case 'AVAILABLE': return 'bg-green-100 text-green-800'
    default: return 'bg-gray-100 text-gray-600'
  }
}

onMounted(() => {
  fetchDrivers()
})
</script>