<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <h2 class="text-3xl font-bold text-gray-800 mb-6">🤖 AI Route Planner</h2>

    <div class="space-y-6">
      <!-- Input Card -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h3 class="text-xl font-bold mb-4">📍 Plan Your Route</h3>

        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Start Location</label>
            <input
              v-model="startLocation"
              type="text"
              placeholder="e.g., Cluj-Napoca"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">End Location</label>
            <input
              v-model="endLocation"
              type="text"
              placeholder="e.g., Brașov"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>
        </div>

        <button
          @click="planRoute"
          class="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold transition"
        >
          🗺️ Plan Route
        </button>
      </div>

      <!-- Route Suggestion Card -->
      <div v-if="routeSuggestion" class="bg-white rounded-lg shadow-lg p-6">
        <h3 class="text-xl font-bold mb-4">✅ Optimized Route</h3>

        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div class="bg-blue-50 p-4 rounded-lg">
              <p class="text-sm text-gray-600">Distance</p>
              <p class="text-2xl font-bold text-blue-600">{{ routeSuggestion.distance }}</p>
            </div>
            <div class="bg-green-50 p-4 rounded-lg">
              <p class="text-sm text-gray-600">Estimated Time</p>
              <p class="text-2xl font-bold text-green-600">{{ routeSuggestion.time }}</p>
            </div>
            <div class="bg-orange-50 p-4 rounded-lg">
              <p class="text-sm text-gray-600">Fuel Consumption</p>
              <p class="text-2xl font-bold text-orange-600">{{ routeSuggestion.fuel }}</p>
            </div>
            <div class="bg-purple-50 p-4 rounded-lg">
              <p class="text-sm text-gray-600">Cost (Estimated)</p>
              <p class="text-2xl font-bold text-purple-600">{{ routeSuggestion.cost }}</p>
            </div>
          </div>

          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="font-semibold text-gray-800 mb-2">🛣️ Route Details</p>
            <ul class="text-sm text-gray-700 space-y-2">
              <li>✓ Route optimized for fuel efficiency</li>
              <li>✓ Avoids traffic congestion</li>
              <li>✓ Recommended stops: {{ routeSuggestion.stops }}</li>
              <li>✓ Road conditions: Good</li>
            </ul>
          </div>
        </div>
      </div>

      <!-- AI Tips Card -->
      <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg shadow-lg p-6 border border-blue-200">
        <h3 class="text-xl font-bold mb-4">💡 AI Optimization Tips</h3>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="flex space-x-3">
            <span class="text-2xl">⚡</span>
            <div>
              <p class="font-semibold text-gray-800">Smooth Acceleration</p>
              <p class="text-sm text-gray-600">Gradual speed changes save 5-10% fuel</p>
            </div>
          </div>

          <div class="flex space-x-3">
            <span class="text-2xl">🎯</span>
            <div>
              <p class="font-semibold text-gray-800">Optimal Speed</p>
              <p class="text-sm text-gray-600">50-90 km/h is most efficient</p>
            </div>
          </div>

          <div class="flex space-x-3">
            <span class="text-2xl">💨</span>
            <div>
              <p class="font-semibold text-gray-800">Reduce Air Drag</p>
              <p class="text-sm text-gray-600">Remove roof racks when not needed</p>
            </div>
          </div>

          <div class="flex space-x-3">
            <span class="text-2xl">⏰</span>
            <div>
              <p class="font-semibold text-gray-800">Avoid Rush Hours</p>
              <p class="text-sm text-gray-600">Plan trips during off-peak hours</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const startLocation = ref('')
const endLocation = ref('')
const routeSuggestion = ref(null)

const planRoute = () => {
  if (!startLocation.value || !endLocation.value) {
    alert('Please enter both start and end locations')
    return
  }

  // Simulate AI route planning
  routeSuggestion.value = {
    distance: '245 km',
    time: '3h 45m',
    fuel: '18.5L',
    cost: '$28.50',
    stops: '2 (180 km, 240 km)'
  }
}
</script>
