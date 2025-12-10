<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <h2 class="text-3xl font-bold text-gray-800 mb-6">👤 My Driver Dashboard</h2>

    <div v-if="assignedVehicle" class="space-y-6">
      <!-- Vehicle Card -->
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
            <p
              :class="getStatusClass(assignedVehicle.status)"
              class="text-lg font-bold"
            >
              {{ assignedVehicle.status }}
            </p>
          </div>
        </div>
      </div>

      <!-- Vehicle Health Status -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h4 class="text-xl font-bold mb-4">🔧 Vehicle Health Status</h4>

        <div class="space-y-3">
          <div class="flex items-center justify-between p-4 bg-green-50 rounded-lg border border-green-200">
            <div class="flex items-center space-x-3">
              <span class="text-2xl">🛞</span>
              <div>
                <p class="font-semibold text-gray-800">Tire Pressure</p>
                <p class="text-sm text-gray-600">2.4 bar (all)</p>
              </div>
            </div>
            <span class="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-bold">✓ OK</span>
          </div>

          <div class="flex items-center justify-between p-4 bg-green-50 rounded-lg border border-green-200">
            <div class="flex items-center space-x-3">
              <span class="text-2xl">🛢️</span>
              <div>
                <p class="font-semibold text-gray-800">Engine Oil</p>
                <p class="text-sm text-gray-600">Level good</p>
              </div>
            </div>
            <span class="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-bold">✓ Good</span>
          </div>

          <div class="flex items-center justify-between p-4 bg-yellow-50 rounded-lg border border-yellow-200">
            <div class="flex items-center space-x-3">
              <span class="text-2xl">⛽</span>
              <div>
                <p class="font-semibold text-gray-800">Fuel Level</p>
                <p class="text-sm text-gray-600">75% capacity</p>
              </div>
            </div>
            <span class="px-3 py-1 bg-yellow-600 text-white rounded-full text-sm font-bold">⚠ 75%</span>
          </div>

          <div class="flex items-center justify-between p-4 bg-green-50 rounded-lg border border-green-200">
            <div class="flex items-center space-x-3">
              <span class="text-2xl">🔋</span>
              <div>
                <p class="font-semibold text-gray-800">Battery Voltage</p>
                <p class="text-sm text-gray-600">13.8V</p>
              </div>
            </div>
            <span class="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-bold">✓ OK</span>
          </div>

          <div class="flex items-center justify-between p-4 bg-green-50 rounded-lg border border-green-200">
            <div class="flex items-center space-x-3">
              <span class="text-2xl">🌡️</span>
              <div>
                <p class="font-semibold text-gray-800">Engine Temperature</p>
                <p class="text-sm text-gray-600">90°C</p>
              </div>
            </div>
            <span class="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-bold">✓ Normal</span>
          </div>
        </div>
      </div>

      <!-- Driving Statistics -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h4 class="text-xl font-bold mb-4">📊 Driving Statistics</h4>

        <div class="grid grid-cols-3 gap-4 mb-6">
          <div class="bg-blue-50 p-4 rounded-lg text-center border border-blue-200">
            <p class="text-sm text-gray-600">Total KM</p>
            <p class="text-3xl font-bold text-blue-600">{{ totalKm }}</p>
            <p class="text-xs text-gray-500 mt-1">Career</p>
          </div>
          <div class="bg-green-50 p-4 rounded-lg text-center border border-green-200">
            <p class="text-sm text-gray-600">Today</p>
            <p class="text-3xl font-bold text-green-600">{{ todayKm }}</p>
            <p class="text-xs text-gray-500 mt-1">km</p>
          </div>
          <div class="bg-purple-50 p-4 rounded-lg text-center border border-purple-200">
            <p class="text-sm text-gray-600">This Week</p>
            <p class="text-3xl font-bold text-purple-600">{{ weekKm }}</p>
            <p class="text-xs text-gray-500 mt-1">km</p>
          </div>
        </div>

        <!-- Chart -->
        <div class="bg-gray-50 p-6 rounded-lg">
          <canvas ref="chartCanvas"></canvas>
        </div>
      </div>

      <!-- Maintenance Alerts -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h4 class="text-xl font-bold mb-4">🔔 Maintenance Reminders</h4>

        <div class="space-y-3">
          <div class="bg-blue-50 border-l-4 border-blue-600 p-4 rounded">
            <div class="flex items-center justify-between">
              <div>
                <p class="font-semibold text-gray-800">📅 Next Service Due</p>
                <p class="text-sm text-gray-600">March 15, 2025</p>
              </div>
              <span class="text-sm font-bold text-blue-600">In 95 days</span>
            </div>
          </div>

          <div class="bg-green-50 border-l-4 border-green-600 p-4 rounded">
            <div class="flex items-center justify-between">
              <div>
                <p class="font-semibold text-gray-800">🔧 Last Service</p>
                <p class="text-sm text-gray-600">December 2024</p>
              </div>
              <span class="text-sm font-bold text-green-600">✓ Done</span>
            </div>
          </div>

          <div class="bg-yellow-50 border-l-4 border-yellow-600 p-4 rounded">
            <div class="flex items-center justify-between">
              <div>
                <p class="font-semibold text-gray-800">🛞 Tire Rotation</p>
                <p class="text-sm text-gray-600">When: 8000+ km from last</p>
              </div>
              <span class="text-sm font-bold text-yellow-600">In 2000 km</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Trip Summary -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h4 class="text-xl font-bold mb-4">🚙 Recent Trips</h4>

        <div class="space-y-2 text-sm text-gray-600">
          <p>✓ Trip 1: Cluj → Sibiu (125 km) - 2.5L fuel</p>
          <p>✓ Trip 2: Cluj → Brașov (170 km) - 3.1L fuel</p>
          <p>✓ Trip 3: Cluj → Turda (80 km) - 1.8L fuel</p>
        </div>
      </div>
    </div>

    <div v-else class="bg-yellow-50 border-l-4 border-yellow-600 p-6 rounded-lg">
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
const totalKm = ref(1250)
const todayKm = ref(85)
const weekKm = ref(450)
const chartCanvas = ref(null)

onMounted(async () => {
  try {
    const response = await axios.get('/api/vehicles')
    if (response.data.length > 0) {
      assignedVehicle.value = response.data[0]
    }
  } catch (error) {
    console.error('Error loading vehicle:', error)
  }

  // Initialize chart
  if (chartCanvas.value) {
    new Chart(chartCanvas.value, {
      type: 'line',
      data: {
        labels: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'],
        datasets: [
          {
            label: 'Distance (km)',
            data: [65, 72, 58, 91, 78, 45, 87],
            borderColor: 'rgb(59, 130, 246)',
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            tension: 0.4,
            fill: true,
            pointRadius: 6,
            pointBackgroundColor: 'rgb(59, 130, 246)',
            pointBorderColor: '#fff',
            pointBorderWidth: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: true,
            position: 'bottom'
          },
          title: {
            display: true,
            text: 'Weekly Driving Activity'
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 100
          }
        }
      }
    })
  }
})

const getStatusClass = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'text-green-600'
    case 'IDLE':
      return 'text-yellow-600'
    case 'MAINTENANCE':
      return 'text-red-600'
    default:
      return 'text-gray-600'
  }
}
</script>
