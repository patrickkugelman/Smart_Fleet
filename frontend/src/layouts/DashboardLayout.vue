<template>
  <div class="flex h-screen bg-gray-100">
    <div class="w-64 bg-gray-800 text-white shadow-lg overflow-y-auto flex flex-col">
      <div class="p-6">
        <h1 class="text-2xl font-bold">🚗 SmartFleet</h1>
        <p class="text-gray-400 text-sm mt-1">Fleet Management System</p>
      </div>

      <nav class="mt-4 space-y-2 px-4 flex-1">
        <router-link 
          to="/" 
          active-class="bg-blue-600" 
          class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
        >
          <span class="text-xl">🗺️</span>
          <span>Live Map</span>
        </router-link>

        <div v-if="authStore.userRole === 'ROLE_ADMIN'" class="space-y-2">
          <router-link 
            to="/vehicles" 
            active-class="bg-blue-600" 
            class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
          >
            <span class="text-xl">🚗</span>
            <span>Vehicles</span>
          </router-link>

          <router-link 
            to="/drivers-management" 
            active-class="bg-blue-600" 
            class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
          >
            <span class="text-xl">👥</span>
            <span>Manage Drivers</span>
          </router-link>
        </div>

        <div v-if="authStore.userRole === 'ROLE_DRIVER'" class="space-y-2">
          <router-link 
            to="/driver-dashboard" 
            active-class="bg-blue-600" 
            class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition"
          >
            <span class="text-xl">👤</span>
            <span>My Dashboard</span>
          </router-link>
        </div>

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

        <router-link 
          v-if="authStore.userRole === 'ROLE_DRIVER'"
          to="/profile" 
          active-class="bg-blue-600" 
          class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-200 hover:bg-gray-700 transition mt-4 border-t border-gray-700 pt-4"
        >
          <span class="text-xl">📷</span>
          <span>My Profile</span>
        </router-link>
      </nav>

      <div class="p-4 border-t border-gray-700 bg-gray-900">
        <div class="mb-4">
          <p class="text-sm text-gray-400">Logged in as:</p>
          <p class="font-semibold text-white truncate">{{ authStore.username }}</p>
          <p class="text-xs text-gray-400 mt-1">{{ getRoleLabel(authStore.userRole) }}</p>
        </div>

        <button 
          @click="handleLogout" 
          class="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-semibold flex items-center justify-center gap-2"
        >
          <span>🚪</span> Logout
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-auto relative">
      <router-view />
      
      <AIChatbox />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AIChatbox from '@/components/AIChatbox.vue'

const router = useRouter()
const authStore = useAuthStore()

// Helper pentru afișarea rolului frumos
const getRoleLabel = (role) => {
  switch (role) {
    case 'ROLE_ADMIN': return 'Administrator'
    case 'ROLE_DRIVER': return 'Professional Driver'
    default: return 'User'
  }
}

// Logica de logout
const handleLogout = () => {
  if (confirm('Are you sure you want to logout?')) {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
/* Stil pentru link-ul activ din meniu */
.router-link-active {
  background-color: rgb(37, 99, 235); /* Tailwind blue-600 */
  color: white;
}
</style>