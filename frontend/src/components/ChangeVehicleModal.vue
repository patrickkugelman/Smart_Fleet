<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="$emit('close')">
    <div class="bg-white rounded-lg shadow-xl p-6 w-96 max-h-[80vh] flex flex-col" @click.stop>
      <h2 class="text-xl font-bold mb-4 text-gray-800">Change Vehicle</h2>
      <p class="text-sm text-gray-600 mb-4">Select a new vehicle for <b>{{ driverName }}</b>:</p>

      <div v-if="loading" class="text-center py-4">
        <span class="text-gray-500">Loading available vehicles...</span>
      </div>

      <div v-else-if="vehicles.length > 0" class="overflow-y-auto flex-1 space-y-2 mb-4">
        <div 
          v-for="v in vehicles" 
          :key="v.id"
          @click="selectedVehicleId = v.id"
          class="p-3 border rounded-lg cursor-pointer transition flex justify-between items-center"
          :class="selectedVehicleId === v.id ? 'border-blue-500 bg-blue-50 ring-1 ring-blue-500' : 'border-gray-200 hover:bg-gray-50'"
        >
          <div>
            <p class="font-bold text-gray-800">{{ v.plate }}</p>
            <p class="text-xs text-gray-500">{{ v.brand }} - {{ v.type }}</p>
          </div>
          <span class="text-xs bg-green-100 text-green-800 px-2 py-1 rounded">Free</span>
        </div>
      </div>

      <div v-else class="text-center py-6 bg-gray-50 rounded mb-4">
        <p class="text-gray-500">No available vehicles found.</p>
      </div>

      <div class="flex gap-3 mt-auto">
        <button 
          @click="$emit('close')" 
          class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 font-medium"
        >
          Cancel
        </button>
        <button 
          @click="assignVehicle" 
          :disabled="!selectedVehicleId || processing"
          class="flex-1 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ processing ? 'Saving...' : 'Assign Vehicle' }}
        </button>
      </div>
      
      <button 
        v-if="currentVehicleId"
        @click="unassignVehicle"
        class="mt-3 text-red-600 text-sm hover:underline text-center w-full"
      >
        Unassign current vehicle
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

const props = defineProps({
  driverId: Number,
  driverName: String,
  currentVehicleId: Number
})

const emit = defineEmits(['close', 'saved'])
const authStore = useAuthStore()

const vehicles = ref([])
const loading = ref(true)
const processing = ref(false)
const selectedVehicleId = ref(null)

const fetchAvailableVehicles = async () => {
  try {
    const res = await axios.get('/api/vehicles/available', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    vehicles.value = res.data
  } catch (error) {
    console.error('Error fetching vehicles:', error)
  } finally {
    loading.value = false
  }
}

const assignVehicle = async () => {
  if (!selectedVehicleId.value) return
  processing.value = true

  try {
    // Folosim endpoint-ul de update driver
    await axios.put(`/api/drivers/${props.driverId}`, {
      vehicleId: selectedVehicleId.value,
      status: 'AVAILABLE' 
    }, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    
    emit('saved')
    emit('close')
  } catch (error) {
    alert('Failed to assign vehicle: ' + (error.response?.data?.message || error.message))
  } finally {
    processing.value = false
  }
}

const unassignVehicle = async () => {
  if(!confirm('Remove vehicle from this driver?')) return
  
  try {
    // Trimitem vehicleId: null pentru a-l scoate
    await axios.put(`/api/drivers/${props.driverId}`, {
      vehicleId: null
    }, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    emit('saved')
    emit('close')
  } catch (error) {
    alert('Failed to unassign vehicle')
  }
}

onMounted(() => {
  fetchAvailableVehicles()
})
</script>