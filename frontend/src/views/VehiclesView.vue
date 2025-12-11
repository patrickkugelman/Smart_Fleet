<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">Vehicle Management</h1>
    
    <button
      @click="openModal('create')"
      class="px-4 py-2 bg-blue-600 text-white rounded mb-4 hover:bg-blue-700 flex items-center gap-2 shadow-sm transition"
    >
      <span>➕</span> Add Vehicle
    </button>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full text-left border-collapse">
        <thead class="bg-gray-100 border-b">
          <tr>
            <th class="p-4 font-semibold text-gray-600">Plate</th>
            <th class="p-4 font-semibold text-gray-600">Brand & Type</th>
            <th class="p-4 font-semibold text-gray-600">Status</th>
            <th class="p-4 font-semibold text-gray-600 text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="vehicles.length === 0">
            <td colspan="4" class="p-6 text-center text-gray-500 italic">No vehicles found in the fleet.</td>
          </tr>
          <tr v-for="v in vehicles" :key="v.id" class="border-b hover:bg-gray-50 transition">
            <td class="p-4">
              <div class="font-bold text-gray-800">{{ v.plate }}</div>
              <div class="text-xs text-gray-400" v-if="v.lat && v.lng">GPS Active</div>
            </td>
            
            <td class="p-4 text-gray-600">
              {{ v.brand }} 
              <span class="ml-2 text-xs bg-gray-200 px-2 py-1 rounded text-gray-700 font-medium">{{ v.type }}</span>
            </td>
            
            <td class="p-4">
              <span :class="getStatusClass(v.status)" class="px-3 py-1 rounded-full text-xs font-bold border">
                {{ v.status }}
              </span>
            </td>
            
            <td class="p-4 text-right space-x-2">
              <button 
                @click="openModal('edit', v)" 
                class="px-3 py-1 bg-yellow-100 text-yellow-700 rounded hover:bg-yellow-200 text-sm font-medium transition"
              >
                Edit
              </button>
              <button 
                @click="deleteVehicle(v.id)" 
                class="px-3 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200 text-sm font-medium transition"
              >
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 backdrop-blur-sm">
      <div class="bg-white rounded-lg p-6 w-96 shadow-2xl transform transition-all scale-100">
        <h2 class="text-xl font-bold mb-4 text-gray-800">
          {{ modalMode === 'create' ? 'Add New Vehicle' : 'Edit Vehicle' }}
        </h2>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Plate Number</label>
            <input 
              v-model="form.plate" 
              placeholder="Ex: CJ-01-ABC" 
              class="w-full border border-gray-300 p-2 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none" 
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Brand</label>
            <input 
              v-model="form.brand" 
              placeholder="Ex: Volvo" 
              class="w-full border border-gray-300 p-2 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none" 
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Type</label>
            <select v-model="form.type" class="w-full border border-gray-300 p-2 rounded focus:ring-2 focus:ring-blue-500 outline-none bg-white">
              <option value="Sedan">Sedan</option>
              <option value="Truck">Truck</option>
              <option value="Van">Van</option>
              <option value="SUV">SUV</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select v-model="form.status" class="w-full border border-gray-300 p-2 rounded focus:ring-2 focus:ring-blue-500 outline-none bg-white">
              <option value="AVAILABLE">AVAILABLE (Free)</option>
              <option value="ON_TRIP">ON_TRIP (Busy)</option>
              <option value="MAINTENANCE">MAINTENANCE (Service)</option>
            </select>
          </div>
        </div>

        <div class="flex gap-3 mt-8">
          <button 
            @click="showModal = false" 
            class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 font-medium transition"
          >
            Cancel
          </button>
          <button 
            @click="saveVehicle" 
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 font-medium transition shadow-md"
          >
            {{ modalMode === 'create' ? 'Create' : 'Update' }}
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
const modalMode = ref('create') 
const editId = ref(null)

// Formular cu valori default
const form = ref({
  plate: '',
  brand: '',
  type: 'Sedan',
  status: 'AVAILABLE',
  lat: 46.7712,
  lng: 23.5889
})

// 1. Fetch
const fetchVehicles = async () => {
  try {
    const response = await axios.get('/api/vehicles', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    vehicles.value = response.data
  } catch (error) {
    console.error('Error fetching vehicles:', error)
  }
}

// 2. Open Modal (Cu logică de reparare date vechi)
const openModal = (mode, vehicle = null) => {
  modalMode.value = mode
  
  if (mode === 'edit' && vehicle) {
    editId.value = vehicle.id
    form.value = { ...vehicle }
    
    // --- FIX PENTRU DATE VECHI ---
    // Dacă mașina are status vechi, îl schimbăm vizual în cel nou
    if (form.value.status === 'IDLE') form.value.status = 'AVAILABLE';
    if (form.value.status === 'ACTIVE') form.value.status = 'ON_TRIP';
    
    // Dacă mașina are tip vechi neacceptat
    if (form.value.type === 'Car') form.value.type = 'Sedan';

  } else {
    // Reset pentru Create
    editId.value = null
    form.value = {
      plate: '',
      brand: '',
      type: 'Sedan',
      status: 'AVAILABLE', // Default Corect
      lat: 46.7712,
      lng: 23.5889
    }
  }
  showModal.value = true
}

// 3. Save (Create & Update)
const saveVehicle = async () => {
  // Validare simplă
  if (!form.value.plate || !form.value.brand) {
    alert('Please fill in Plate and Brand')
    return
  }

  try {
    const config = { headers: { Authorization: `Bearer ${authStore.token}` } }
    
    // Asigurăm coordonate default dacă lipsesc
    const payload = {
        ...form.value,
        lat: form.value.lat || 46.7712,
        lng: form.value.lng || 23.5889
    }

    if (modalMode.value === 'create') {
      await axios.post('/api/vehicles', payload, config)
      alert('Vehicle created successfully!')
    } else {
      await axios.put(`/api/vehicles/${editId.value}`, payload, config)
      alert('Vehicle updated successfully!')
    }
    
    showModal.value = false
    fetchVehicles()
  } catch (error) {
    console.error('Save error:', error)
    alert('Error: ' + (error.response?.data?.message || 'Operation failed'))
  }
}

// 4. Delete
const deleteVehicle = async (id) => {
  if (!confirm('Are you sure you want to delete this vehicle?')) return

  try {
    await axios.delete(`/api/vehicles/${id}`, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    alert('Vehicle deleted!')
    fetchVehicles()
  } catch (error) {
    alert('Error deleting vehicle. It might be assigned to a driver.')
  }
}

// 5. Culori Status
const getStatusClass = (status) => {
  switch(status) {
    case 'ON_TRIP': 
    case 'ACTIVE': // Compatibilitate veche
      return 'bg-blue-100 text-blue-800 border-blue-200'
      
    case 'AVAILABLE': 
    case 'IDLE': // Compatibilitate veche
      return 'bg-green-100 text-green-800 border-green-200'
      
    case 'MAINTENANCE': 
      return 'bg-red-100 text-red-800 border-red-200'
      
    default: 
      return 'bg-gray-100 text-gray-600 border-gray-200'
  }
}

onMounted(() => {
  fetchVehicles()
})
</script>