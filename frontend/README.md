# Smart Fleet Tracking System - Frontend Setup Guide

## Overview
Vue 3 Composition API frontend with real-time GPS tracking, role-based dashboards, AI route planning, and fuel analytics powered by Geoapify maps and Chart.js.

## Prerequisites
- **Node.js 18+** (Download from [nodejs.org](https://nodejs.org/))
- **npm 9+** (Included with Node.js)
- **Smart Fleet Backend** running on `http://localhost:8080`

## Installation

### 1. Install Dependencies
```bash
cd frontend
npm install
```

This installs:
- Vue 3.4 - Progressive JavaScript framework
- Vite 5 - Next generation build tool
- Pinia - State management
- Axios - HTTP client
- Leaflet - Interactive maps
- Chart.js - Data visualization
- TailwindCSS - Utility-first CSS
- SockJS & Stompjs - WebSocket support

### 2. Configure Environment

Create `.env.local` file in frontend directory (optional):
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GEOAPIFY_API_KEY=7022be7caa6d4cdfa09c1e5a8d963359
```

## Running the Frontend

### Development Mode
```bash
npm run dev
```
Application runs on http://localhost:3000

### Production Build
```bash
npm run build
npm run preview
```

## Project Structure
```
frontend/
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
├── src/
│   ├── main.js
│   ├── style.css
│   ├── App.vue
│   ├── views/
│   │   ├── LoginView.vue          # Login/Register
│   │   ├── MapView.vue             # Live GPS tracking
│   │   ├── VehiclesView.vue        # Vehicle management (Admin)
│   │   ├── DriverDashboardView.vue # Driver dashboard
│   │   ├── AIRoutePlannerView.vue  # Route optimization
│   │   └── FuelAnalyticsView.vue   # Fuel analytics
│   ├── components/
│   │   ├── AIChatbox.vue           # AI Assistant
│   │   ├── AddVehicleModal.vue     # Add vehicle form
│   │   └── EditVehicleModal.vue    # Edit vehicle form
│   ├── layouts/
│   │   └── DashboardLayout.vue     # Main layout with sidebar
│   ├── stores/
│   │   ├── authStore.js           # Authentication state
│   │   ├── vehicleStore.js        # Vehicle data & WebSocket
│   │   └── driverStore.js         # Driver data
│   └── router/
│       └── index.js                # Vue Router configuration
└── public/
    └── favicon.ico
```

## Features

### 1. Authentication
- **Login Page**: Username/password authentication
- **Registration**: Driver self-registration with license
- **Role-Based UI**: Admin vs Driver views
- **JWT Token**: 24-hour token validity
- **Session Persistence**: Token saved in localStorage

### 2. Map View
- **Leaflet Maps**: Interactive map with zoom/pan
- **Geoapify Tiles**: Real-time OSM Bright tiles
- **Vehicle Markers**: Color-coded by status (Active/Idle/Maintenance)
- **Live Updates**: WebSocket real-time position updates
- **Popups**: Click markers for vehicle details (plate, brand, type, location)

### 3. Vehicle Management (Admin Only)
- **List View**: All vehicles with brand, type, status
- **Add Vehicle**: Create new vehicles with brand field
- **Edit Vehicle**: Update vehicle details
- **Delete Vehicle**: Remove vehicles (with confirmation)
- **Search**: Filter by plate
- **Filtering**: By status and type

### 4. Driver Dashboard
- **Vehicle Assignment**: Display assigned vehicle
- **Health Monitoring**: 5 vehicle health indicators
  - Tire Pressure
  - Engine Oil
  - Fuel Level
  - Battery Voltage
  - Engine Temperature
- **Statistics**: Total KM, Today, This Week
- **Chart.js Graph**: Weekly driving activity
- **Maintenance Schedule**: Service reminders
- **Trip History**: Recent trips

### 5. AI Route Planner
- **Route Input**: Start/end location
- **Optimization**: AI-suggested routes
- **Metrics**: Distance, time, fuel, cost
- **Tips**: Eco-driving recommendations

### 6. Fuel Analytics
- **Key Metrics**: Consumption, cost, efficiency, savings
- **Consumption Chart**: Weekly/monthly trends
- **Vehicle Ranking**: Fuel efficiency comparison
- **7-Day Forecast**: AI fuel predictions
- **Recommendations**: Driver training, maintenance, optimization

### 7. AI Chatbox
- **Floating Widget**: Bottom-right corner
- **Quick Responses**: Fleet statistics, status checks
- **Persistent**: Available on all pages
- **Lightweight**: ~200 lines of code

## Login Credentials

### Admin User
```
Username: admin
Password: admin123
```

### Test Driver (Create via Registration)
Use the register form to create new driver accounts.

## Customization

### Change API Base URL
Edit `src/router/index.js` and `vite.config.js`:
```javascript
// vite.config.js
proxy: {
  '/api': {
    target: 'http://your-api-server:8080',
    changeOrigin: true
  }
}
```

### Change Map Tile Provider
Edit `src/views/MapView.vue`:
```javascript
// Use different tile provider
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '© OpenStreetMap contributors'
}).addTo(map.value)
```

### Customize Colors
Edit `tailwind.config.js`:
```javascript
theme: {
  extend: {
    colors: {
      primary: '#your-color',
    }
  }
}
```

### Add New Views
1. Create component in `src/views/`
2. Register route in `src/router/index.js`
3. Add navigation link in `src/layouts/DashboardLayout.vue`

## State Management (Pinia)

### authStore
```javascript
import { useAuthStore } from '@/stores/authStore'
const authStore = useAuthStore()

// Properties
authStore.token           // JWT token
authStore.user            // User info (username, email, role)
authStore.isAuthenticated // Boolean
authStore.userRole        // 'ROLE_ADMIN' or 'ROLE_DRIVER'

// Methods
await authStore.login(credentials)
await authStore.register(userData)
authStore.logout()
```

### vehicleStore
```javascript
import { useVehicleStore } from '@/stores/vehicleStore'
const vehicleStore = useVehicleStore()

// Properties
vehicleStore.vehicles        // Array of vehicles
vehicleStore.loading         // Loading state
vehicleStore.getVehicleCount // Computed vehicle count

// Methods
await vehicleStore.fetchVehicles()
await vehicleStore.createVehicle(data)
await vehicleStore.updateVehicle(id, data)
await vehicleStore.deleteVehicle(id)
vehicleStore.connectWebSocket()  // Real-time updates
vehicleStore.disconnectWebSocket()
```

## WebSocket Integration

Real-time vehicle updates via STOMP:
```javascript
// Auto-connected in MapView.vue
vehicleStore.connectWebSocket()

// Subscribes to /topic/vehicles
// Updates vehicle positions in real-time
// Marker positions update without page refresh
```

## API Integration

All API calls through Axios:
```javascript
import axios from 'axios'

// Automatically includes JWT token in headers
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

// GET
const vehicles = await axios.get('/api/vehicles')

// POST
await axios.post('/api/vehicles', vehicleData)

// PUT
await axios.put('/api/vehicles/1', updates)

// DELETE
await axios.delete('/api/vehicles/1')
```

## Styling with TailwindCSS

Pre-configured utility classes:
```vue
<!-- Colors -->
<div class="bg-blue-600 text-white">Content</div>

<!-- Spacing -->
<div class="p-6 mb-4 space-y-2">Content</div>

<!-- Layout -->
<div class="grid grid-cols-3 gap-4">Content</div>

<!-- Responsive -->
<div class="md:grid-cols-2 lg:grid-cols-4">Content</div>
```

## Chart.js Integration

Example from DriverDashboardView.vue:
```javascript
import { Chart } from 'chart.js/auto'

new Chart(chartCanvas.value, {
  type: 'line',
  data: {
    labels: ['Mon', 'Tue', 'Wed', ...],
    datasets: [{
      label: 'Distance (km)',
      data: [65, 72, 58, ...],
      borderColor: 'rgb(59, 130, 246)'
    }]
  },
  options: {
    responsive: true,
    plugins: { legend: { display: true } }
  }
})
```

## Troubleshooting

### Port 3000 Already in Use
```bash
# Change port in vite.config.js
server: {
  port: 3001
}
```

### CORS Errors
- Ensure backend is running on http://localhost:8080
- Check `vite.config.js` proxy configuration
- Verify `SecurityConfig.java` allows CORS

### WebSocket Connection Failed
- Backend WebSocket must be at `/ws`
- Check firewall settings
- Verify STOMP endpoint in `WebSocketConfig.java`

### Components Not Loading
- Clear node_modules: `rm -rf node_modules && npm install`
- Clear Vite cache: `rm -rf .vite`
- Hard refresh browser: `Ctrl+Shift+R`

### JWT Token Expired
- Auto-handled by login redirect
- Clear localStorage if needed: `localStorage.clear()`
- Re-login required

## Performance Tips

1. **Lazy Load Components**
```javascript
const MapView = () => import('@/views/MapView.vue')
```

2. **Image Optimization**: Use WebP format
3. **Code Splitting**: Vite handles automatically
4. **Bundle Analysis**: `npm run build -- --report`

## Building for Production

```bash
# Build optimized bundle
npm run build

# Output: dist/ folder (ready for deployment)

# Deploy to server
# Option 1: Static hosting (Netlify, Vercel)
# Option 2: Docker container
# Option 3: Traditional web server (Nginx, Apache)
```

### Docker Deployment
```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## Environment Variables

```env
# .env.development (default)
VITE_API_BASE_URL=http://localhost:8080

# .env.production
VITE_API_BASE_URL=https://api.smartfleet.com
```

## Keyboard Shortcuts

- `Ctrl+K`: Search features
- `Ctrl+/`: Toggle sidebar
- `Esc`: Close modals

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Contributing

1. Create feature branch
2. Follow Vue 3 Composition API style
3. Use TailwindCSS for styling
4. Test all routes and features
5. Submit pull request

## Deployment Checklist

- [ ] Backend running on production server
- [ ] Environment variables configured
- [ ] API base URL updated in config
- [ ] Geoapify API key is valid
- [ ] CORS enabled for production domain
- [ ] SSL/TLS certificates installed
- [ ] Database backups configured
- [ ] Monitoring and logging set up
- [ ] Performance tested
- [ ] Security audit completed

## Next Steps

1. Start backend: `cd backend && mvn spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Open http://localhost:3000
4. Login with demo credentials
5. Explore features!

## Support

For issues:
- Check browser console (F12)
- Review network requests (DevTools)
- Check backend logs
- Verify API connectivity

---

**Version**: 2.0.0  
**Last Updated**: December 2024
