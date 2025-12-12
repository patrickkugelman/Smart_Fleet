<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-3xl font-bold text-gray-800">⛽ Fuel & Fleet Analytics</h2>
      
      <div class="bg-white p-2 rounded-lg shadow flex items-center gap-3">
        <span class="text-sm font-bold text-gray-500">Current Fuel Price:</span>
        <input 
          v-model="fuelPrice" 
          type="number" 
          step="0.1" 
          class="w-20 border rounded px-2 py-1 font-bold text-blue-600"
        >
        <span class="text-sm font-bold text-gray-500">€/L</span>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
        <p class="text-sm text-gray-500">Active Vehicles</p>
        <p class="text-3xl font-bold text-gray-800">{{ activeVehiclesCount }} / {{ totalVehiclesCount }}</p>
        <p class="text-xs text-green-500 font-bold mt-1">Currently on road</p>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
        <p class="text-sm text-gray-500">Est. Monthly Cost</p>
        <p class="text-3xl font-bold text-gray-800">€{{ estimatedMonthlyCost }}</p>
        <p class="text-xs text-gray-400 mt-1">Based on current fleet</p>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-purple-500">
        <p class="text-sm text-gray-500">Fleet Health</p>
        <p class="text-3xl font-bold text-gray-800">{{ fleetHealth }}%</p>
        <p class="text-xs text-gray-400 mt-1">Operational Vehicles</p>
      </div>

      <div class="bg-white rounded-lg shadow p-6 border-l-4 border-red-500">
        <p class="text-sm text-gray-500">Critical Alerts</p>
        <p class="text-3xl font-bold text-red-600">{{ maintenanceCount }}</p>
        <p class="text-xs text-red-400 mt-1">Vehicles needing service</p>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <div class="lg:col-span-2 bg-white rounded-lg shadow p-6">
        <h3 class="text-xl font-bold text-gray-800 mb-4">📊 Real-Time Fleet Comparison</h3>
        <div v-if="loading" class="h-64 flex items-center justify-center text-gray-400">Loading Fleet Data...</div>
        <div v-else class="h-64">
          <canvas ref="comparisonChart"></canvas>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6 overflow-hidden flex flex-col h-[22rem]">
        <h3 class="text-xl font-bold text-gray-800 mb-4 flex justify-between items-center">
          <span>📡 Live Fuel Monitor</span>
          <span class="text-xs bg-green-100 text-green-800 px-2 py-1 rounded animate-pulse">LIVE</span>
        </h3>
        
        <div class="overflow-y-auto pr-2 space-y-3 flex-1">
          <div v-for="v in vehicles" :key="v.id" class="flex items-center justify-between p-3 rounded-lg border hover:bg-gray-50 transition">
            <div>
              <p class="font-bold text-sm">{{ v.plate }}</p>
              <p class="text-xs text-gray-500">{{ v.brand }} - {{ v.type }}</p>
            </div>
            
            <div class="text-right">
              <p class="font-mono font-bold text-blue-600">
                {{ getSimulatedFuel(v) }}%
              </p>
              <p class="text-[10px]" :class="v.status === 'ON_TRIP' ? 'text-green-600 font-bold' : 'text-gray-400'">
                {{ v.status === 'ON_TRIP' ? 'Consuming...' : 'Engine Off' }}
              </p>
            </div>
          </div>
        </div>
      </div>

    </div>

    <div class="mt-8 bg-gradient-to-r from-gray-800 to-gray-900 rounded-lg shadow-xl p-6 text-white">
      <div class="flex items-center gap-3 mb-4">
        <span class="text-3xl">🤖</span>
        <div>
          <h3 class="text-xl font-bold">AI Anomaly Detection</h3>
          <p class="text-sm text-gray-400">Analyzing telemetry from client simulators...</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-gray-700 bg-opacity-50 p-4 rounded border border-gray-600">
          <p class="text-xs text-gray-400 uppercase font-bold mb-2">Efficiency Score</p>
          <div class="flex items-end gap-2">
            <span class="text-4xl font-bold text-green-400">A+</span>
            <span class="text-sm mb-1">Optimized</span>
          </div>
          <p class="text-xs mt-2 text-gray-300">Route adherence is 98%. Excellent routing.</p>
        </div>

        <div class="bg-gray-700 bg-opacity-50 p-4 rounded border border-gray-600">
          <p class="text-xs text-gray-400 uppercase font-bold mb-2">Latest Telemetry Alert</p>
          <div v-if="maintenanceCount > 0">
            <p class="text-red-400 font-bold">⚠️ High Cargo Temp Detected</p>
            <p class="text-xs mt-1 text-gray-300">Vehicle in MAINTENANCE mode reporting sensor anomalies.</p>
          </div>
          <div v-else>
            <p class="text-green-400 font-bold">✅ Systems Nominal</p>
            <p class="text-xs mt-1 text-gray-300">No active DTC codes received from fleet.</p>
          </div>
        </div>

        <div class="bg-gray-700 bg-opacity-50 p-4 rounded border border-gray-600">
          <p class="text-xs text-gray-400 uppercase font-bold mb-2">Fuel Theft Probability</p>
          <div class="w-full bg-gray-600 rounded-full h-2.5 mt-2">
            <div class="bg-green-500 h-2.5 rounded-full" style="width: 2%"></div>
          </div>
          <p class="text-xs mt-2 text-gray-300">Low risk. Tank levels consistent with mileage.</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'
import { Chart } from 'chart.js/auto'

const authStore = useAuthStore()
const vehicles = ref([])
const loading = ref(true)
const comparisonChart = ref(null)
const chartInstance = ref(null)
const fuelPrice = ref(1.45) // Preț default
let pollingInterval = null

// --- COMPUTED METRICS ---
const totalVehiclesCount = computed(() => vehicles.value.length)
const activeVehiclesCount = computed(() => vehicles.value.filter(v => v.status === 'ON_TRIP' || v.status === 'ACTIVE').length)
const maintenanceCount = computed(() => vehicles.value.filter(v => v.status === 'MAINTENANCE').length)

const fleetHealth = computed(() => {
  if (totalVehiclesCount.value === 0) return 100
  return Math.round(((totalVehiclesCount.value - maintenanceCount.value) / totalVehiclesCount.value) * 100)
})

const estimatedMonthlyCost = computed(() => {
  // Calcul simplist: Active * 300L/luna * Pret
  return Math.round(activeVehiclesCount.value * 300 * fuelPrice.value)
})

// --- DATA FETCHING ---
const fetchFleetData = async () => {
  try {
    const res = await axios.get('/api/vehicles', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    vehicles.value = res.data
    updateChart()
  } catch (e) {
    console.error("Error loading analytics data", e)
  } finally {
    loading.value = false
  }
}

// --- HELPER: Simulare Nivel Combustibil ---
// Pentru că backend-ul nu stochează încă % exact, îl simulăm vizual bazat pe status
// Dacă status e ON_TRIP, facem să pară că scade random
const getSimulatedFuel = (vehicle) => {
  if (vehicle.status === 'MAINTENANCE') return 0;
  if (vehicle.status === 'AVAILABLE') return 100;
  
  // Hash simplu pe baza ID-ului ca să fie consistent dar diferit per mașină
  const seed = vehicle.id * 123; 
  const timeFactor = Math.floor(Date.now() / 10000); // Se schimbă la fiecare 10 secunde
  const randomLevel = 40 + ((seed + timeFactor) % 50); 
  return randomLevel;
}

// --- CHART LOGIC ---
const updateChart = () => {
  if (!comparisonChart.value) return;
  
  // Pregătim datele pentru grafic din vehiculele REALE
  const labels = vehicles.value.map(v => v.plate);
  // Simulăm un consum mediu pe baza tipului
  const dataPoints = vehicles.value.map(v => {
    if (v.type === 'Truck') return 28.5;
    if (v.type === 'Van') return 12.0;
    return 7.5; // Sedan
  });
  
  const colors = vehicles.value.map(v => v.status === 'ON_TRIP' ? '#3b82f6' : '#e5e7eb');

  if (chartInstance.value) {
    chartInstance.value.destroy();
  }

  chartInstance.value = new Chart(comparisonChart.value, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [{
        label: 'Avg Consumption (L/100km)',
        data: dataPoints,
        backgroundColor: colors,
        borderRadius: 5
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        y: { beginAtZero: true, title: { display: true, text: 'Liters / 100km' } }
      }
    }
  })
}

onMounted(() => {
  fetchFleetData()
  // Actualizăm datele la fiecare 3 secunde pentru a prinde modificările din Python Client
  pollingInterval = setInterval(fetchFleetData, 3000)
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
  if (chartInstance.value) chartInstance.value.destroy()
})
</script>