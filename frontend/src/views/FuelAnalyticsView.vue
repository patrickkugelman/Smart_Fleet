<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <h2 class="text-3xl font-bold text-gray-800 mb-6">📊 Fuel Analytics & Predictions</h2>

    <div class="space-y-6">
      <!-- Key Metrics -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg shadow-lg p-6">
          <p class="text-sm text-gray-600">Avg Fuel/100km</p>
          <p class="text-3xl font-bold text-blue-600">7.5L</p>
          <p class="text-xs text-gray-500 mt-1">↓ 2% vs last week</p>
        </div>
        <div class="bg-white rounded-lg shadow-lg p-6">
          <p class="text-sm text-gray-600">Total Fuel Cost</p>
          <p class="text-3xl font-bold text-green-600">$450</p>
          <p class="text-xs text-gray-500 mt-1">This month</p>
        </div>
        <div class="bg-white rounded-lg shadow-lg p-6">
          <p class="text-sm text-gray-600">Fleet Efficiency</p>
          <p class="text-3xl font-bold text-purple-600">92%</p>
          <p class="text-xs text-gray-500 mt-1">Above target</p>
        </div>
        <div class="bg-white rounded-lg shadow-lg p-6">
          <p class="text-sm text-gray-600">Est. Savings</p>
          <p class="text-3xl font-bold text-orange-600">$125</p>
          <p class="text-xs text-gray-500 mt-1">vs suboptimal driving</p>
        </div>
      </div>

      <!-- Fuel Consumption Chart -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h3 class="text-xl font-bold mb-4">📈 Fuel Consumption Trend</h3>
        <canvas ref="fuelChart"></canvas>
      </div>

      <!-- Vehicle Comparison -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h3 class="text-xl font-bold mb-4">🚗 Vehicle Fuel Efficiency Ranking</h3>

        <div class="space-y-3">
          <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div>
              <p class="font-semibold text-gray-800">Vehicle A (Plate: AB-123-CD)</p>
              <p class="text-sm text-gray-600">Volvo FH16</p>
            </div>
            <div class="text-right">
              <p class="font-bold text-green-600">6.8 L/100km</p>
              <p class="text-xs text-gray-600">Excellent</p>
            </div>
          </div>

          <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div>
              <p class="font-semibold text-gray-800">Vehicle B (Plate: EF-456-GH)</p>
              <p class="text-sm text-gray-600">Toyota Hiace</p>
            </div>
            <div class="text-right">
              <p class="font-bold text-yellow-600">8.2 L/100km</p>
              <p class="text-xs text-gray-600">Good</p>
            </div>
          </div>

          <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div>
              <p class="font-semibold text-gray-800">Vehicle C (Plate: IJ-789-KL)</p>
              <p class="text-sm text-gray-600">Mercedes Sprinter</p>
            </div>
            <div class="text-right">
              <p class="font-bold text-orange-600">9.1 L/100km</p>
              <p class="text-xs text-gray-600">Fair</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Predictions -->
      <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg shadow-lg p-6 border border-blue-200">
        <h3 class="text-xl font-bold mb-4">🔮 AI Fuel Predictions (Next 7 Days)</h3>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="bg-white p-4 rounded-lg text-center">
            <p class="text-xs text-gray-600">Mon</p>
            <p class="text-2xl font-bold text-blue-600">42L</p>
          </div>
          <div class="bg-white p-4 rounded-lg text-center">
            <p class="text-xs text-gray-600">Tue</p>
            <p class="text-2xl font-bold text-blue-600">38L</p>
          </div>
          <div class="bg-white p-4 rounded-lg text-center">
            <p class="text-xs text-gray-600">Wed</p>
            <p class="text-2xl font-bold text-blue-600">45L</p>
          </div>
          <div class="bg-white p-4 rounded-lg text-center">
            <p class="text-xs text-gray-600">Thu</p>
            <p class="text-2xl font-bold text-blue-600">41L</p>
          </div>
          <div class="bg-white p-4 rounded-lg text-center">
            <p class="text-xs text-gray-600">Fri</p>
            <p class="text-2xl font-bold text-green-600">35L</p>
          </div>
          <div class="bg-white p-4 rounded-lg text-center">
            <p class="text-xs text-gray-600">Sat</p>
            <p class="text-2xl font-bold text-green-600">28L</p>
          </div>
          <div class="bg-white p-4 rounded-lg text-center">
            <p class="text-xs text-gray-600">Sun</p>
            <p class="text-2xl font-bold text-green-600">25L</p>
          </div>
        </div>

        <p class="text-sm text-gray-600 mt-4">
          📌 <strong>Next Week Forecast:</strong> ~254L estimated consumption. Consider scheduling maintenance to optimize fuel efficiency.
        </p>
      </div>

      <!-- Recommendations -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h3 class="text-xl font-bold mb-4">💡 Personalized Recommendations</h3>

        <div class="space-y-3">
          <div class="flex items-start space-x-3 p-4 bg-green-50 rounded-lg border border-green-200">
            <span class="text-2xl">✓</span>
            <div>
              <p class="font-semibold text-gray-800">Driver Training Program</p>
              <p class="text-sm text-gray-600">Enroll drivers in eco-driving training to reduce fuel consumption by 5-15%</p>
            </div>
          </div>

          <div class="flex items-start space-x-3 p-4 bg-yellow-50 rounded-lg border border-yellow-200">
            <span class="text-2xl">⚠️</span>
            <div>
              <p class="font-semibold text-gray-800">Vehicle Maintenance</p>
              <p class="text-sm text-gray-600">Schedule regular maintenance for vehicles with increasing fuel consumption</p>
            </div>
          </div>

          <div class="flex items-start space-x-3 p-4 bg-blue-50 rounded-lg border border-blue-200">
            <span class="text-2xl">🚗</span>
            <div>
              <p class="font-semibold text-gray-800">Route Optimization</p>
              <p class="text-sm text-gray-600">Use AI Route Planner to reduce mileage and fuel costs on key routes</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Chart } from 'chart.js/auto'

const fuelChart = ref(null)

onMounted(() => {
  if (fuelChart.value) {
    new Chart(fuelChart.value, {
      type: 'bar',
      data: {
        labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5'],
        datasets: [
          {
            label: 'Fuel Consumption (L)',
            data: [280, 265, 290, 275, 250],
            backgroundColor: [
              'rgba(59, 130, 246, 0.8)',
              'rgba(59, 130, 246, 0.8)',
              'rgba(245, 158, 11, 0.8)',
              'rgba(59, 130, 246, 0.8)',
              'rgba(34, 197, 94, 0.8)'
            ],
            borderColor: [
              'rgb(59, 130, 246)',
              'rgb(59, 130, 246)',
              'rgb(245, 158, 11)',
              'rgb(59, 130, 246)',
              'rgb(34, 197, 94)'
            ],
            borderWidth: 2
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
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 400
          }
        }
      }
    })
  }
})
</script>
