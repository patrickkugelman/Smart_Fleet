import requests
import time
import sys
import os

# --- IMPORT SECURE SIMULATOR ---
# Încercăm să importăm simulatorul. Dacă fișierul lipsește, nu crăpăm tot clientul.
try:
    from smart_truck_client import SmartTruck
except ImportError:
    SmartTruck = None

# --- CONFIGURARE ---
BASE_URL = "http://localhost:8080"
USERNAME = "eric"       
PASSWORD = "utcn"

# --- CULORI (Definite local) ---
CYAN = '\033[96m'
GREEN = '\033[92m'
YELLOW = '\033[93m'
RED = '\033[91m'
BLUE = '\033[94m'
RESET = '\033[0m'
BOLD = '\033[1m'

class DesktopApp:
    def __init__(self):
        self.token = None
        
    def clear_screen(self):
        os.system('cls' if os.name == 'nt' else 'clear')

    # --- AUTHENTICATION ---
    def login(self):
        print(f"🔐 Logging in as {BOLD}{USERNAME}{RESET}...")
        try:
            res = requests.post(f"{BASE_URL}/api/auth/login", json={"username": USERNAME, "password": PASSWORD})
            if res.status_code == 200:
                self.token = res.json()['token']
                print(f"{GREEN}✅ Login Successful!{RESET}")
                time.sleep(1)
                return True
            print(f"{RED}❌ Login Failed: {res.text}{RESET}")
            return False
        except Exception as e:
            print(f"{RED}❌ Connection Error: Is the server running?{RESET}")
            return False

    # --- OP 1: SIMULATION ---
    def start_simulation(self):
        self.clear_screen()
        if SmartTruck is None:
            print(f"{RED}❌ Error: 'smart_truck_client.py' not found in the same folder.{RESET}")
            input("Press Enter...")
            return

        print(f"{CYAN}Starting Driving Simulation Module...{RESET}")
        print("Press Ctrl+C to stop simulation and return to menu.")
        time.sleep(1)
        
        try:
            # Pornim simulatorul direct aici (mult mai stabil decât os.system)
            truck = SmartTruck()
            truck.run_simulation() 
        except KeyboardInterrupt:
            print(f"\n{YELLOW}Simulation stopped by user.{RESET}")
        except Exception as e:
            print(f"\n{RED}Simulation error: {e}{RESET}")
        
        input(f"\n{BOLD}Press Enter to return to menu...{RESET}")

    # --- OP 2: GET ITEMS (FILTERED) ---
    def view_active_fleet(self):
        self.clear_screen()
        print(f"{BOLD}=== 🚛 ACTIVE FLEET MONITORING (Filtered) ==={RESET}")
        try:
            headers = {"Authorization": f"Bearer {self.token}"}
            res = requests.get(f"{BASE_URL}/api/vehicles", headers=headers)
            
            if res.status_code == 200:
                vehicles = res.json()
                # Requirements: View items filtered (ON_TRIP only)
                active_trucks = [v for v in vehicles if v['status'] == 'ON_TRIP' or v['status'] == 'ACTIVE']
                
                if not active_trucks:
                    print(f"\n{YELLOW}No vehicles are currently on a trip.{RESET}")
                else:
                    print(f"\nFound {len(active_trucks)} active vehicles on the road:\n")
                    print(f"{'PLATE':<15} {'BRAND':<15} {'TYPE':<10} {'COORDS'}")
                    print("-" * 60)
                    for v in active_trucks:
                        coords = f"{v.get('lat', 0):.4f}, {v.get('lng', 0):.4f}"
                        print(f"{GREEN}{v['plate']:<15} {v['brand']:<15} {v['type']:<10} {coords}{RESET}")
            else:
                print(f"{RED}Error fetching data: {res.status_code}{RESET}")
        except Exception as e:
            print(f"{RED}Error: {e}{RESET}")
        
        input(f"\n{BOLD}Press Enter to return to menu...{RESET}")

    # --- OP 3: GET OWNERS AND ITEMS ---
    def view_driver_directory(self):
        self.clear_screen()
        print(f"{BOLD}=== 👥 DRIVER & VEHICLE DIRECTORY (Owners & Items) ==={RESET}")
        try:
            headers = {"Authorization": f"Bearer {self.token}"}
            res = requests.get(f"{BASE_URL}/api/drivers", headers=headers)
            
            if res.status_code == 200:
                drivers = res.json()
                print(f"\n{'DRIVER NAME':<20} {'STATUS':<12} {'ASSIGNED VEHICLE (ITEM)'}")
                print("-" * 65)
                for d in drivers:
                    vehicle_info = f"{d.get('vehicleBrand', '')} ({d.get('vehiclePlate', 'Unassigned')})"
                    if not d.get('vehicleId'):
                        vehicle_info = f"{YELLOW}No Vehicle Assigned{RESET}"
                    
                    status_col = GREEN if d['status'] == 'AVAILABLE' else BLUE
                    print(f"{d['name']:<20} {status_col}{d['status']:<12}{RESET} {vehicle_info}")
            else:
                print(f"{RED}Access Denied (403). Backend permissions need update for /api/drivers.{RESET}")
        except Exception as e:
            print(f"{RED}Error: {e}{RESET}")

        input(f"\n{BOLD}Press Enter to return to menu...{RESET}")

    # --- OP 4: SEND DATA (INCIDENT) ---
    def send_incident_report(self):
        self.clear_screen()
        print(f"{BOLD}=== 🚨 SEND INCIDENT REPORT (Update Item) ==={RESET}")
        
        confirm = input("Send 'MAINTENANCE' alert for your vehicle? (y/n): ")
        if confirm.lower() != 'y': return

        try:
            headers = {"Authorization": f"Bearer {self.token}"}
            me_res = requests.get(f"{BASE_URL}/api/drivers/me", headers=headers)
            my_data = me_res.json()
            
            if not my_data.get('vehicleId'):
                print(f"{RED}No vehicle assigned to report on!{RESET}")
                time.sleep(2)
                return

            vehicle_id = my_data['vehicleId']
            # Trimitem datele (Schimbăm statusul în MAINTENANCE)
            payload = {
                "plate": my_data['vehiclePlate'],
                "brand": my_data['vehicleBrand'],
                "type": "Truck", 
                "status": "MAINTENANCE" 
            }
            
            res = requests.put(f"{BASE_URL}/api/vehicles/{vehicle_id}", headers=headers, json=payload)
            
            if res.status_code == 200:
                print(f"\n{GREEN}✅ REPORT SENT! Vehicle status updated to MAINTENANCE.{RESET}")
            else:
                print(f"{RED}Failed: {res.status_code}. Backend permissions might block PUT.{RESET}")

        except Exception as e:
            print(f"{RED}Error: {e}{RESET}")
        
        input(f"\n{BOLD}Press Enter to return to menu...{RESET}")

    def run(self):
        if not self.login(): return

        while True:
            self.clear_screen()
            print(f"{BOLD}╔════════════════════════════════════════╗{RESET}")
            print(f"║    🖥️  SMART FLEET DESKTOP CLIENT      ║")
            print(f"╠════════════════════════════════════════╣")
            print(f"║ 1. 🚛 Start Driving Simulation         ║")
            print(f"║ 2. 🗺️  View Active Fleet (Filtered)    ║")
            print(f"║ 3. 👥 Driver Directory (Owners+Items)  ║")
            print(f"║ 4. 🚨 Send Incident Report (Update)    ║")
            print(f"║ 5. 🚪 Exit                             ║")
            print(f"╚════════════════════════════════════════╝")
            
            choice = input("\nSelect option: ")

            if choice == '1':
                self.start_simulation()
            elif choice == '2':
                self.view_active_fleet()
            elif choice == '3':
                self.view_driver_directory()
            elif choice == '4':
                self.send_incident_report()
            elif choice == '5':
                print("Goodbye!")
                sys.exit()
            else:
                print("Invalid option.")
                time.sleep(1)

if __name__ == "__main__":
    app = DesktopApp()
    app.run()