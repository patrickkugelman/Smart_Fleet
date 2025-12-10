<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40" @click="$emit('close')">
    <div class="bg-white rounded-lg shadow-2xl p-8 w-full max-w-md" @click.stop>
      <h2 class="text-2xl font-bold text-gray-800 mb-6">➕ Add New Vehicle</h2>

      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">License Plate *</label>
          <input
            v-model="form.plate"
            type="text"
            required
            placeholder="e.g., AB-123-CD"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
          />
        </div>

        <!-- CRITICAL: Brand field -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Brand *</label>
          <input
            v-model="form.brand"
            type="text"
            required
            placeholder="e.g., Volvo, Toyota, Mercedes"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Type *</label>
          <select
            v-model="form.type"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
          >
            <option value="">Select type</option>
            <option value="Truck">Truck</option>
            <option value="Van">Van</option>
            <option value="Sedan">Sedan</option>
            <option value="SUV">SUV</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Status</label>
          <select
            v-model="form.status"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
          >
            <option value="IDLE">IDLE</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="MAINTENANCE">MAINTENANCE</option>
          </select>
        </div>

        <div class="flex space-x-3 pt-4">
          <button
            @click="$emit('close')"
            type="button"
            class="flex-1 px-4 py-2 bg-gray-300 text-gray-800 rounded-lg hover:bg-gray-400 transition font-semibold"
          >
            Cancel
          </button>
          <button
            type="submit"
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-semibold"
          >
            Add Vehicle
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['close', 'save'])

const form = ref({
  plate: '',
  brand: '',
  type: '',
  status: 'IDLE'
})

const handleSubmit = () => {
  emit('save', form.value)
  form.value = { plate: '', brand: '', type: '', status: 'IDLE' }
}
</script>
