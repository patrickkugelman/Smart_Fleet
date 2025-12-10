import axios from 'axios'

// Configure base URL
// Asigură-te că portul 8080 este cel pe care rulează Spring Boot
axios.defaults.baseURL = 'http://localhost:8080'
axios.defaults.headers.common['Content-Type'] = 'application/json'

// Add request interceptor to automatically attach token
axios.interceptors.request.use(
  (config) => {
    let token = localStorage.getItem('token')
    
    if (token) {
      // REPARAȚIA: Curățăm token-ul de paranteze [] sau ghilimele "" care pot cauza eroarea 403
      token = token.replace(/['"\[\]]+/g, '');
      
      config.headers.Authorization = `Bearer ${token}`
    }

    // Logare pentru debugging (poți șterge asta în producție)
    console.log('🔵 Axios request:', config.method?.toUpperCase(), config.url)
    // Afișăm doar primele 10 caractere din token pentru siguranță
    console.log('🔵 Token trimis:', token ? token.substring(0, 10) + '...' : 'MISSING')
    
    return config
  },
  (error) => {
    console.error('❌ Request error:', error)
    return Promise.reject(error)
  }
)

// Add response interceptor for better error handling
axios.interceptors.response.use(
  (response) => {
    // Logăm succesul doar dacă e nevoie
    // console.log('✅ Response:', response.status, response.config.url)
    return response
  },
  (error) => {
    console.error('❌ Response error:', error.response?.status, error.config?.url)
    
    // Nu vrem să umplem consola cu obiectul întreg decât dacă e critic
    if (error.response?.data) {
        console.error('❌ Error data:', error.response.data)
    }
    
    if (error.response?.status === 401) {
      console.warn('⚠️ Unauthorized - Token might be expired or invalid')
      // Opțional: Șterge token-ul invalid și redirecționează
      // localStorage.removeItem('token');
      // window.location.href = '/login';
    }
    
    return Promise.reject(error)
  }
)

export default axios