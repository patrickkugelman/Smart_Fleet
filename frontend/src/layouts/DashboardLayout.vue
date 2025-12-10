<template>
  <div class="flex h-screen bg-gray-100">
    <!-- Sidebar Navigation -->
    <div class="w-64 bg-gray-800 text-white shadow-lg overflow-y-auto">
      <div class="p-6">
        <h1 class="text-2xl font-bold">🚗 SmartFleet</h1>
        <p class="text-gray-400 text-sm mt-1">Fleet Management System</p>
      </div>

      <nav class="mt-8 space-y-2 px-4">
        <router-link
          to="/"
          active-class="bg-blue-600"
          class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
        >
          <span class="text-xl">🗺️</span>
          <span>Live Map</span>
        </router-link>

        <!-- CRITICAL: Conditional navigation for Admin -->
        <router-link
          v-if="authStore.userRole === 'ROLE_ADMIN'"
          to="/vehicles"
          active-class="bg-blue-600"
          class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
        >
          <span class="text-xl">🚗</span>
          <span>Vehicles</span>
        </router-link>

        <!-- NEW: Manage Drivers (Admin only) -->
        <router-link
          v-if="authStore.userRole === 'ROLE_ADMIN'"
          to="/drivers-management"
          active-class="bg-blue-600"
          class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
        >
          <span class="text-xl">👥</span>
          <span>Manage Drivers</span>
        </router-link>

        <!-- CRITICAL: Conditional navigation for Driver -->
        <router-link
          v-if="authStore.userRole === 'ROLE_DRIVER'"
          to="/driver-dashboard"
          active-class="bg-blue-600"
          class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
        >
          <span class="text-xl">👤</span>
          <span>My Dashboard</span>
        </router-link>

        <router-link
          to="/ai-planner"
          active-class="bg-blue-600"
          class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
        >
          <span class="text-xl">🤖</span>
          <span>AI Route Planner</span>
        </router-link>

        <router-link
          to="/analytics"
          active-class="bg-blue-600"
          class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
        >
          <span class="text-xl">📊</span>
          <span>Fuel Analytics</span>
        </router-link>
      </nav>

      <!-- User Section -->
      <div class="absolute bottom-0 w-64 p-4 border-t border-gray-700">
        <div class="bg-gray-700 rounded-lg p-4 mb-4">
          <p class="text-sm text-gray-400">Logged in as:</p>
          <p class="font-semibold text-white">{{ authStore.username }}</p>
          <p class="text-xs text-gray-400 mt-1">{{ getRoleLabel(authStore.userRole) }}</p>
        </div>

        <button
          @click="handleLogout"
          class="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-semibold"
        >
          🚪 Logout
        </button>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="flex-1 overflow-auto">
      <router-view />
    </div>

    <!-- AI Chatbox -->
    <AIChatbox />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AIChatbox from '@/components/AIChatbox.vue'

const router = useRouter()
const authStore = useAuthStore()

const getRoleLabel = (role) => {
  switch (role) {
    case 'ROLE_ADMIN':
      return 'Administrator'
    case 'ROLE_DRIVER':
      return 'Driver'
    default:
      return 'User'
  }
}

const handleLogout = () => {
  if (confirm('Are you sure you want to logout?')) {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.router-link-active {
  background-color: rgb(37, 99, 235);
}
</style>