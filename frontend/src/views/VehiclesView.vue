<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">Vehicle Management - DEBUG MODE</h1>
    
    <!-- DEBUG Panel -->
    <div class="bg-yellow-100 p-4 rounded mb-4 space-y-2">
      <p><strong>Auth Token:</strong> {{ authStore.token ? 'EXISTS ✅' : 'MISSING ❌' }}</p>
      <p><strong>User Role:</strong> {{ authStore.userRole }}</p>
      <p><strong>Vehicles Count:</strong> {{ vehicles.length }}</p>
      <p><strong>Modal Visible:</strong> {{ showModal ? 'YES' : 'NO' }}</p>
    </div>

    <!-- Simple Add Button -->
    <button
      @click="handleAddClick"
      class="px-4 py-2 bg-blue-600 text-white rounded mb-4"
    >
      🚗 Add Vehicle (Click Me!)
    </button>

    <!-- Vehicles List -->
    <div class="bg-white rounded shadow p-4">
      <h2 class="font-bold mb-2">Vehicles:</h2>
      <div v-if="vehicles.length === 0" class="text-gray-500">No vehicles found</div>
      <div v-else>
        <div v-for="v in vehicles" :key="v.id" class="border-b py-2">
          {{ v.plate }} - {{ v.brand }} ({{ v.type }})
        </div>
      </div>
    </div>

    <!-- Super Simple Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded p-6 w-96">
        <h2 class="text-xl font-bold mb-4">Add Vehicle</h2>
        
        <div class="space-y-3">
          <input v-model="newVehicle.plate" placeholder="Plate (CJ-01-ABC)" class="w-full border p-2 rounded" />
          <input v-model="newVehicle.brand" placeholder="Brand (BMW)" class="w-full border p-2 rounded" />
          <select v-model="newVehicle.type" class="w-full border p-2 rounded">
            <option value="">Select Type</option>
            <option value="Car">Car</option>
            <option value="Van">Van</option>
            <option value="Truck">Truck</option>
          </select>
        </div>

        <div class="flex gap-2 mt-4">
          <button @click="createVehicle" class="px-4 py-2 bg-blue-600 text-white rounded">
            Create
          </button>
          <button @click="showModal = false" class="px-4 py-2 bg-gray-300 rounded">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const vehicles = ref([])
const showModal = ref(false)

const newVehicle = ref({
  plate: '',
  brand: '',
  type: '',
  status: 'AVAILABLE',
  lat: 46.7712,
  lng: 23.5889
})

const handleAddClick = () => {
  console.log('🔵 Button clicked!')
  console.log('🔵 Token:', authStore.token?.substring(0, 20) + '...')
  showModal.value = true
  console.log('🔵 Modal should open now')
}

const fetchVehicles = async () => {
  try {
    console.log('📡 Fetching vehicles...')
    const response = await axios.get('http://localhost:8080/api/vehicles', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    vehicles.value = response.data
    console.log('✅ Vehicles loaded:', vehicles.value.length)
  } catch (error) {
    console.error('❌ Fetch error:', error.response?.status, error.response?.data)
  }
}

const createVehicle = async () => {
  try {
    console.log('📤 Creating vehicle:', newVehicle.value)
    
    const response = await axios.post(
      'http://localhost:8080/api/vehicles',
      newVehicle.value,
      {
        headers: {
          'Authorization': `Bearer ${authStore.token}`,
          'Content-Type': 'application/json'
        }
      }
    )
    
    console.log('✅ Vehicle created:', response.data)
    alert('Vehicle created successfully!')
    
    showModal.value = false
    newVehicle.value = {
      plate: '',
      brand: '',
      type: '',
      status: 'AVAILABLE',
      lat: 46.7712,
      lng: 23.5889
    }
    
    fetchVehicles()
  } catch (error) {
    console.error('❌ Create error:', error.response?.status, error.response?.data)
    alert('Failed to create vehicle: ' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  console.log('🚀 Component mounted')
  console.log('🔑 Token exists:', !!authStore.token)
  console.log('👤 Role:', authStore.userRole)
  fetchVehicles()
})
</script>