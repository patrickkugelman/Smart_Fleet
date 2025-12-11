import { createRouter, createWebHistory } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import LoginView from '@/views/LoginView.vue'
import VehicleSelectionView from '@/views/VehicleSelectionView.vue'
import MapView from '@/views/MapView.vue'
import VehiclesView from '@/views/VehiclesView.vue'
import DriverDashboardView from '@/views/DriverDashboardView.vue'
import AIRoutePlannerView from '@/views/AIRoutePlannerView.vue'
import FuelAnalyticsView from '@/views/FuelAnalyticsView.vue'
import DriversManagementView from '@/views/DriversManagementView.vue'
import DriverProfileView from '@/views/DriverProfileView.vue'
import { useAuthStore } from '@/stores/authStore'
import axios from 'axios'

const routes = [
  { path: '/login', name: 'Login', component: LoginView, meta: { requiresAuth: false } },
  { 
    path: '/select-vehicle', 
    name: 'Vehicle Selection', 
    component: VehicleSelectionView, 
    meta: { requiresAuth: true, requiresDriver: true } 
  },
  {
    path: '/',
    component: DashboardLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Map', component: MapView },
      { path: 'vehicles', name: 'Vehicles', component: VehiclesView, meta: { requiresAdmin: true } },
      { path: 'drivers-management', name: 'Drivers Management', component: DriversManagementView, meta: { requiresAdmin: true } },
      { path: 'driver-dashboard', name: 'Driver Dashboard', component: DriverDashboardView, meta: { requiresDriver: true } },
      { path: 'ai-planner', name: 'AI Planner', component: AIRoutePlannerView },
      { path: 'analytics', name: 'Fuel Analytics', component: FuelAnalyticsView },
      { path: 'profile', name: 'Profile', component: DriverProfileView }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
    return
  }

  // LOGICA DRIVER
  if (authStore.userRole === 'ROLE_DRIVER') {
    // Trimite driver-ul de pe home (map) pe dashboard
    if (to.path === '/' && to.name === 'Map') {
      next('/driver-dashboard')
      return
    }
    
    // Verifică vehiculul doar dacă nu e deja pe pagina de selecție
    if (to.path !== '/select-vehicle') {
        try {
            const response = await axios.get('/api/drivers/me')
            if (!response.data.vehicleId) {
                next('/select-vehicle')
                return
            }
        } catch (e) { console.error(e) }
    }
  }

  // LOGICA ADMIN
  if (to.meta.requiresAdmin && authStore.userRole !== 'ROLE_ADMIN') {
    next('/')
    return
  }

  next()
})

export default router