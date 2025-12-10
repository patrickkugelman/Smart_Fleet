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
import { useAuthStore } from '@/stores/authStore'
import axios from 'axios'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
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
      {
        path: '',
        name: 'Map',
        component: MapView
      },
      {
        path: 'vehicles',
        name: 'Vehicles',
        component: VehiclesView,
        meta: { requiresAdmin: true }
      },
      {
        path: 'drivers-management',
        name: 'Drivers Management',
        component: DriversManagementView,
        meta: { requiresAdmin: true }
      },
      {
        path: 'driver-dashboard',
        name: 'Driver Dashboard',
        component: DriverDashboardView,
        meta: { requiresDriver: true }
      },
      {
        path: 'ai-planner',
        name: 'AI Planner',
        component: AIRoutePlannerView
      },
      {
        path: 'analytics',
        name: 'Fuel Analytics',
        component: FuelAnalyticsView
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // Check basic auth requirements
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }
  
  // If already authenticated, don't go back to login
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
    return
  }

  // CRITICAL: If driver, check if vehicle is assigned
  if (to.meta.requiresDriver && authStore.userRole === 'ROLE_DRIVER') {
    try {
      const response = await axios.get('/api/drivers/me')
      const driver = response.data
      
      // If no vehicle assigned and not on vehicle selection page, redirect
      if (!driver.vehicleId && to.path !== '/select-vehicle') {
        next('/select-vehicle')
        return
      }
      // If has vehicle and trying to go to vehicle selection, allow dashboard
      if (driver.vehicleId && to.path === '/select-vehicle') {
        next('/')
        return
      }
    } catch (error) {
      console.error('Error checking driver profile:', error)
      // On error, allow to proceed
    }
  }

  // Check admin-only routes
  if (to.meta.requiresAdmin && authStore.userRole !== 'ROLE_ADMIN') {
    next('/')
    return
  }

  next()
})

export default router