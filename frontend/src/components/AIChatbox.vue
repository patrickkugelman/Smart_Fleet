<template>
  <div class="fixed bottom-6 right-6 z-50">
    <div
      v-if="isOpen"
      class="bg-white rounded-lg shadow-2xl w-96 flex flex-col border border-gray-200"
      style="height: 500px;"
    >
      <div class="bg-gradient-to-r from-blue-600 to-purple-600 text-white p-4 rounded-t-lg flex justify-between items-center shrink-0">
        <div>
          <p class="font-bold">🤖 Smart Fleet AI</p>
          <p class="text-xs text-blue-100">Powered by Llama 3</p>
        </div>
        <button @click="isOpen = false" class="text-xl hover:text-blue-200">×</button>
      </div>

      <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
        <div v-for="msg in messages" :key="msg.id" :class="msg.sender === 'user' ? 'text-right' : 'text-left'">
          <div
            :class="msg.sender === 'user'
              ? 'bg-blue-600 text-white rounded-lg rounded-tr-none'
              : 'bg-white border border-gray-200 text-gray-800 rounded-lg rounded-tl-none shadow-sm'"
            class="px-4 py-2 max-w-[85%] inline-block text-sm leading-relaxed"
          >
            <span v-if="msg.loading">Thinking... ⏳</span>
            <span v-else>{{ msg.text }}</span>
          </div>
        </div>
      </div>

      <div class="border-t p-3 bg-white rounded-b-lg shrink-0">
        <div class="flex space-x-2">
          <input
            v-model="userInput"
            @keyup.enter="sendMessage"
            type="text"
            placeholder="Ask about drivers, trips, fuel..."
            :disabled="isLoading"
            class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm disabled:bg-gray-100"
          />
          <button
            @click="sendMessage"
            :disabled="isLoading"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:bg-gray-400 font-medium"
          >
            Send
          </button>
        </div>
      </div>
    </div>

    <button
      v-if="!isOpen"
      @click="isOpen = true"
      class="w-14 h-14 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-full shadow-lg hover:shadow-xl transition flex items-center justify-center text-3xl hover:scale-110"
    >
      🤖
    </button>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const isOpen = ref(false)
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)

const messages = ref([
  {
    id: 1,
    text: 'Hello! I can see all your live fleet data. Ask me "Where is Dacian going?" or "Which truck is free?"',
    sender: 'bot',
    loading: false
  }
])

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  const question = userInput.value
  userInput.value = ''

  // 1. Add User Message
  messages.value.push({
    id: Date.now(),
    text: question,
    sender: 'user',
    loading: false
  })
  scrollToBottom()

  // 2. Add Bot "Thinking" Placeholder
  const botMsgId = Date.now() + 1
  messages.value.push({
    id: botMsgId,
    text: '',
    sender: 'bot',
    loading: true
  })
  scrollToBottom()
  isLoading.value = true

  try {
    // 3. Call Backend
    const response = await axios.post('/api/chat/ask', 
      { question: question },
      { headers: { Authorization: `Bearer ${authStore.token}` } }
    )

    // 4. Update Bot Message
    const botMsgIndex = messages.value.findIndex(m => m.id === botMsgId)
    if (botMsgIndex !== -1) {
      messages.value[botMsgIndex].loading = false
      messages.value[botMsgIndex].text = response.data.response
    }

  } catch (error) {
    console.error('AI Error:', error)
    const botMsgIndex = messages.value.findIndex(m => m.id === botMsgId)
    if (botMsgIndex !== -1) {
      messages.value[botMsgIndex].loading = false
      messages.value[botMsgIndex].text = "Sorry, connection error. Please verify that Ollama Llama 3 is running."
    }
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

watch(isOpen, (val) => {
  if (val) scrollToBottom()
})
</script>