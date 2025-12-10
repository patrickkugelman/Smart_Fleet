import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useDriverStore = defineStore('driver', () => {
  const drivers = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchDrivers = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await axios.get('/api/drivers')
      drivers.value = response.data
    } catch (err) {
      error.value = err.message
      console.error('Failed to fetch drivers:', err)
    } finally {
      loading.value = false
    }
  }

  const getDriverById = async (id) => {
    try {
      const response = await axios.get(`/api/drivers/${id}`)
      return response.data
    } catch (err) {
      error.value = err.message
      throw err
    }
  }

  const updateDriverStatus = async (id, status) => {
    try {
      const response = await axios.put(`/api/drivers/${id}/status`, null, {
        params: { status }
      })
      const index = drivers.value.findIndex(d => d.id === id)
      if (index > -1) {
        drivers.value[index] = response.data
      }
      return response.data
    } catch (err) {
      error.value = err.message
      throw err
    }
  }

  return {
    drivers,
    loading,
    error,
    fetchDrivers,
    getDriverById,
    updateDriverStatus
  }
})
