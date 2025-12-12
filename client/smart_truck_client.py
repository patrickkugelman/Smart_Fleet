import requests
import time
import sys
import random
import os

# --- CONFIGURARE ---
BASE_URL = "http://localhost:8080"
USERNAME = "Dragos"       
PASSWORD = "utcn"   # <--- AICI AM SCHIMBAT PAROLA (era password123)


# Ruta (Cluj -> Hamburg simplificata)
ROUTE_WAYPOINTS = [
    {"name": "Cluj-Napoca, RO", "lat": 46.7712, "lng": 23.5889},
    {"name": "Budapest, HU",    "lat": 47.4979, "lng": 19.0402},
    {"name": "Vienna, AT",      "lat": 48.2082, "lng": 16.3738},
    {"name": "Milan, IT",       "lat": 45.4642, "lng": 9.1900},
    {"name": "Barcelona, ES",   "lat": 41.3851, "lng": 2.1734},
    {"name": "Madrid, ES",      "lat": 40.4168, "lng": -3.7038}
]

# Culori
GREEN = '\033[92m'
YELLOW = '\033[93m'
RED = '\033[91m'
RESET = '\033[0m'
BOLD = '\033[1m'

class SmartTruck:
    def __init__(self):
        self.token = None
        self.vehicle_id = None
        self.plate = "Unknown"
        self.speed = 0
        self.fuel_level = 100.0
        self.status = "IDLE"
        self.cargo_temp = -22.0
        
    def login(self):
        try:
            res = requests.post(f"{BASE_URL}/api/auth/login", json={"username": USERNAME, "password": PASSWORD})
            if res.status_code == 200:
                self.token = res.json()['token']
                return True
            return False
        except: return False

    def init_vehicle(self):
        try:
            res = requests.get(f"{BASE_URL}/api/drivers/me", headers={"Authorization": f"Bearer {self.token}"})
            if res.status_code == 200:
                data = res.json()
                self.vehicle_id = data.get('vehicleId')
                self.plate = data.get('vehiclePlate')
                return True
            return False
        except: return False

    def update_server(self, lat, lng):
        headers = {"Authorization": f"Bearer {self.token}"}
        try:
            # 1. Update GPS
            requests.put(
                f"{BASE_URL}/api/vehicles/{self.vehicle_id}/location",
                headers=headers,
                params={"lat": lat, "lng": lng}
            )
            # 2. Update Status (daca e nevoie)
            requests.put(f"{BASE_URL}/api/vehicles/{self.vehicle_id}", headers=headers, json={"status": self.status})
        except: pass

    def dashboard(self, location, event_msg=""):
        os.system('cls' if os.name == 'nt' else 'clear')
        state_color = GREEN if self.status == "ON_TRIP" else (RED if self.status == "MAINTENANCE" else YELLOW)
        
        print(f"{BOLD}╔══════════════════════════════════════════════════════╗{RESET}")
        print(f"║ 🚛  SMART TRUCK SIMULATOR (IoT)                      ║")
        print(f"╠══════════════════════════════════════════════════════╣")
        print(f"║ 🔢 PLATE: {self.plate.ljust(10)} 👤 DRIVER: {USERNAME.ljust(10)}        ║")
        print(f"║ 📍 LOC:   {location.ljust(20)}                       ║")
        print(f"╠══════════════════════════════════════════════════════╣")
        print(f"║ STATUS: {state_color}{self.status.center(12)}{RESET}  SPEED: {str(int(self.speed)).rjust(3)} km/h          ║")
        print(f"║ FUEL:   {str(int(self.fuel_level)).rjust(3)}%          TEMP:  {self.cargo_temp:.1f}°C            ║")
        print(f"╚══════════════════════════════════════════════════════╝")
        if event_msg:
            print(f"\n📢 {event_msg}")

    def run_simulation(self):
        if not self.login() or not self.init_vehicle():
            print(f"❌ Connection failed. Check password for user '{USERNAME}'.")
            return

        print("✅ Truck Systems Online. Cooling Cargo...")
        time.sleep(2)
        self.status = "ON_TRIP"

        for i in range(len(ROUTE_WAYPOINTS) - 1):
            start = ROUTE_WAYPOINTS[i]
            end = ROUTE_WAYPOINTS[i+1]
            
            steps = 50
            lat_step = (end['lat'] - start['lat']) / steps
            lng_step = (end['lng'] - start['lng']) / steps

            for step in range(steps):
                curr_lat = start['lat'] + (lat_step * step)
                curr_lng = start['lng'] + (lng_step * step)
                
                # --- SIMULARE ---
                if random.randint(0, 100) > 95: self.cargo_temp += 0.5 
                else: 
                    if self.cargo_temp > -22: self.cargo_temp -= 0.1

                alert_msg = ""
                if self.cargo_temp > -18.0:
                    self.status = "MAINTENANCE"
                    alert_msg = f"{RED}CRITICAL: CARGO TEMP HIGH!{RESET}"
                else:
                    self.status = "ON_TRIP"

                target_speed = 90
                if self.speed < target_speed: self.speed += 5
                self.fuel_level -= 0.05

                self.update_server(curr_lat, curr_lng)
                self.dashboard(f"{start['name']}->{end['name']}", alert_msg)
                time.sleep(0.5)

if __name__ == "__main__":
    try:
        truck = SmartTruck()
        truck.run_simulation()
    except KeyboardInterrupt:
        print("\n🛑 Simulation Stopped.")