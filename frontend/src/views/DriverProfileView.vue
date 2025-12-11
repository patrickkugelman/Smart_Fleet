<template>
  <div class="p-6 bg-gray-100 min-h-screen flex justify-center">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl overflow-hidden mt-10 h-fit">
      
      <div class="h-32 bg-gradient-to-r from-blue-600 to-purple-600"></div>

      <div class="px-8 pb-8">
        <div class="relative -mt-16 mb-6 flex flex-col items-center">
          <div class="relative group">
            <img 
              :src="profileImage" 
              class="w-32 h-32 rounded-full border-4 border-white shadow-lg object-cover bg-white"
              alt="Profile"
            />
            <label class="absolute bottom-0 right-0 bg-blue-600 p-2 rounded-full cursor-pointer hover:bg-blue-700 shadow transition transform hover:scale-110">
              <span class="text-white text-xl leading-none">📷</span>
              <input type="file" class="hidden" @change="handleFileUpload" accept="image/png">
            </label>
          </div>
          
          <h2 class="text-2xl font-bold text-gray-800 mt-4">{{ driverInfo.name }}</h2>
          <p class="text-gray-500">{{ driverInfo.license ? 'License: ' + driverInfo.license : 'No License' }}</p>
          <span class="px-3 py-1 rounded-full text-xs font-bold mt-2" 
                :class="driverInfo.status === 'AVAILABLE' ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800'">
            {{ driverInfo.status }}
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
          <div class="p-4 bg-gray-50 rounded-lg border border-gray-100">
            <p class="text-xs text-gray-500 uppercase font-bold mb-1">Email</p>
            <p class="text-gray-800 font-medium break-all">{{ driverInfo.email }}</p>
          </div>
          <div class="p-4 bg-gray-50 rounded-lg border border-gray-100">
            <p class="text-xs text-gray-500 uppercase font-bold mb-1">Username</p>
            <p class="text-gray-800 font-medium">@{{ driverInfo.username }}</p>
          </div>
          <div class="p-4 bg-gray-50 rounded-lg border border-gray-100">
            <p class="text-xs text-gray-500 uppercase font-bold mb-1">Assigned Vehicle</p>
            <p class="text-blue-600 font-bold" v-if="driverInfo.vehicleId">
              Vehicle ID: {{ driverInfo.vehicleId }}
            </p>
            <p class="text-gray-400 italic" v-else>No vehicle assigned</p>
          </div>
          <div class="p-4 bg-gray-50 rounded-lg border border-gray-100">
            <p class="text-xs text-gray-500 uppercase font-bold mb-1">Joined Date</p>
            <p class="text-gray-800">{{ driverInfo.createdAt }}</p>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const driverInfo = ref({})
// Default placeholder image
const profileImage = ref('https://via.placeholder.com/150?text=No+Image')

const fetchProfile = async () => {
  try {
    const res = await axios.get('/api/drivers/me')
    driverInfo.value = res.data
    
    // Daca avem avatar de la backend, il afisam
    if (res.data.avatarUrl) {
      profileImage.value = `http://localhost:8080${res.data.avatarUrl}`
    }
  } catch (error) {
    console.error('Error fetching profile:', error)
  }
}

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 1. Validare Frontend: Doar PNG
  if (file.type !== 'image/png') {
    alert('❌ Format neacceptat! Te rugăm să încarci doar imagini PNG.')
    // Reset input
    event.target.value = ''
    return
  }

  // 2. Preview instant
  const oldImage = profileImage.value
  profileImage.value = URL.createObjectURL(file)

  // 3. Upload Backend
  const formData = new FormData()
  formData.append('file', file)

  try {
    await axios.post(`/api/drivers/${driverInfo.value.id}/avatar`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    alert('✅ Poză de profil actualizată cu succes!')
  } catch (error) {
    console.error('Upload failed:', error)
    alert(error.response?.data?.message || 'Failed to upload image')
    // Revert la imaginea veche in caz de eroare
    profileImage.value = oldImage
  }
}

onMounted(() => {
  fetchProfile()
})
</script>