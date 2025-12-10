import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

export const useVehicleStore = defineStore('vehicle', () => {
  const vehicles = ref([])
  const loading = ref(false)
  const error = ref(null)
  let stompClient = null

  const getVehicleCount = computed(() => vehicles.value.length)
  const activeVehicles = computed(() => 
    vehicles.value.filter(v => v.status === 'ACTIVE').length
  )

  const fetchVehicles = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await axios.get('/api/vehicles')
      vehicles.value = response.data
    } catch (err) {
      error.value = err.message
      console.error('Failed to fetch vehicles:', err)
    } finally {
      loading.value = false
    }
  }

  const createVehicle = async (vehicleData) => {
    try {
      const response = await axios.post('/api/vehicles', vehicleData)
      vehicles.value.push(response.data)
      return response.data
    } catch (err) {
      error.value = err.message
      throw err
    }
  }

  const updateVehicle = async (id, vehicleData) => {
    try {
      const response = await axios.put(`/api/vehicles/${id}`, vehicleData)
      const index = vehicles.value.findIndex(v => v.id === id)
      if (index > -1) {
        vehicles.value[index] = response.data
      }
      return response.data
    } catch (err) {
      error.value = err.message
      throw err
    }
  }

  const deleteVehicle = async (id) => {
    try {
      await axios.delete(`/api/vehicles/${id}`)
      vehicles.value = vehicles.value.filter(v => v.id !== id)
    } catch (err) {
      error.value = err.message
      throw err
    }
  }

  const updateVehicleLocation = async (id, lat, lng) => {
    try {
      await axios.put(`/api/vehicles/${id}/location`, null, {
        params: { lat, lng }
      })
      const vehicle = vehicles.value.find(v => v.id === id)
      if (vehicle) {
        vehicle.lat = lat
        vehicle.lng = lng
      }
    } catch (err) {
      console.error('Failed to update vehicle location:', err)
    }
  }

  const connectWebSocket = () => {
    try {
      const socket = new SockJS('http://localhost:8080/ws')
      stompClient = Stomp.over(socket)
      stompClient.connect({}, (frame) => {
        console.log('WebSocket connected:', frame)
        // Subscribe to vehicle updates
        stompClient.subscribe('/topic/vehicles', (message) => {
          const vehicle = JSON.parse(message.body)
          const index = vehicles.value.findIndex(v => v.id === vehicle.id)
          if (index > -1) {
            vehicles.value[index] = vehicle
          }
        })
      })
    } catch (err) {
      console.error('WebSocket connection failed:', err)
    }
  }

  const disconnectWebSocket = () => {
    if (stompClient && stompClient.connected) {
      stompClient.disconnect(() => {
        console.log('WebSocket disconnected')
      })
    }
  }

  return {
    vehicles,
    loading,
    error,
    getVehicleCount,
    activeVehicles,
    fetchVehicles,
    createVehicle,
    updateVehicle,
    deleteVehicle,
    updateVehicleLocation,
    connectWebSocket,
    disconnectWebSocket
  }
})
