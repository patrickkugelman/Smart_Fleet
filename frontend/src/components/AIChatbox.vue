<template>
  <div class="fixed bottom-6 right-6 z-50">
    <!-- Chatbox Container -->
    <div
      v-if="isOpen"
      class="bg-white rounded-lg shadow-2xl w-96 max-h-96 flex flex-col border border-gray-200"
    >
      <!-- Header -->
      <div class="bg-gradient-to-r from-blue-600 to-purple-600 text-white p-4 rounded-t-lg flex justify-between items-center">
        <div>
          <p class="font-bold">🤖 Smart Fleet AI Assistant</p>
          <p class="text-xs text-blue-100">Always here to help</p>
        </div>
        <button @click="isOpen = false" class="text-xl hover:text-blue-200">✕</button>
      </div>

      <!-- Messages -->
      <div class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
        <div v-for="msg in messages" :key="msg.id" :class="msg.sender === 'user' ? 'text-right' : 'text-left'">
          <div
            :class="msg.sender === 'user'
              ? 'bg-blue-600 text-white rounded-lg rounded-tr-none'
              : 'bg-gray-200 text-gray-800 rounded-lg rounded-tl-none'"
            class="px-3 py-2 max-w-xs inline-block text-sm"
          >
            {{ msg.text }}
          </div>
        </div>
      </div>

      <!-- Input -->
      <div class="border-t p-3 bg-white rounded-b-lg">
        <div class="flex space-x-2">
          <input
            v-model="userInput"
            @keyup.enter="sendMessage"
            type="text"
            placeholder="Ask me anything..."
            class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm"
          />
          <button
            @click="sendMessage"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            Send
          </button>
        </div>
      </div>
    </div>

    <!-- Toggle Button -->
    <button
      v-if="!isOpen"
      @click="isOpen = true"
      class="w-14 h-14 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-full shadow-lg hover:shadow-xl transition flex items-center justify-center text-2xl hover:scale-110"
    >
      💬
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const isOpen = ref(false)
const userInput = ref('')
const messages = ref([
  {
    id: 1,
    text: 'Hi! 👋 I\'m your Smart Fleet AI Assistant. Ask me about vehicles, fuel consumption, routes, or fleet statistics.',
    sender: 'bot'
  }
])

const sendMessage = () => {
  if (!userInput.value.trim()) return

  // Add user message
  messages.value.push({
    id: messages.value.length + 1,
    text: userInput.value,
    sender: 'user'
  })

  // Simulate AI response
  setTimeout(() => {
    const responses = [
      'Smart response! Your fleet is operating efficiently. Keep up the good work! 🚗',
      'I can help with that! Check your vehicle metrics and fuel analytics for more details.',
      'That\'s a great question! Your AI route planner can optimize this for you.',
      'Fleet status is optimal! All vehicles are running smoothly. 📊',
      'You\'re saving fuel costs with our AI optimization. Great job! 💰'
    ]
    const randomResponse = responses[Math.floor(Math.random() * responses.length)]
    
    messages.value.push({
      id: messages.value.length + 1,
      text: randomResponse,
      sender: 'bot'
    })
  }, 500)

  userInput.value = ''
}
</script>
