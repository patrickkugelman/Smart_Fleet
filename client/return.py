import requests
import time

# --- CONFIGURARE ---
BASE_URL = "http://localhost:8080"
USERNAME = "eric"       
PASSWORD = "utcn"

# Coordonate Cluj-Napoca (Garajul Central)
HOME_LAT = 46.7712
HOME_LNG = 23.5889

# Culori
GREEN = '\033[92m'
CYAN = '\033[96m'
RED = '\033[91m'
RESET = '\033[0m'
BOLD = '\033[1m'

class FleetManager:
    def __init__(self):
        self.token = None

    def login(self):
        print(f"🔐 Authenticating as {BOLD}{USERNAME}{RESET}...")
        try:
            res = requests.post(f"{BASE_URL}/api/auth/login", json={"username": USERNAME, "password": PASSWORD})
            if res.status_code == 200:
                self.token = res.json()['token']
                return True
            print(f"{RED}Login Failed: {res.text}{RESET}")
            return False
        except Exception as e:
            print(f"{RED}Connection Error: {e}{RESET}")
            return False

    def return_all_vehicles(self):
        if not self.login(): return

        print(f"\n{CYAN}📡 Scanning fleet status...{RESET}")
        try:
            headers = {"Authorization": f"Bearer {self.token}"}
            # 1. Obținem lista tuturor vehiculelor
            res = requests.get(f"{BASE_URL}/api/vehicles", headers=headers)
            
            if res.status_code == 200:
                vehicles = res.json()
                print(f"Found {len(vehicles)} vehicles. Sending 'RETURN TO BASE' command...\n")

                for v in vehicles:
                    # 2. Trimitem comanda de mutare pentru fiecare vehicul
                    try:
                        # Resetăm locația la Cluj
                        update_res = requests.put(
                            f"{BASE_URL}/api/vehicles/{v['id']}/location",
                            headers=headers,
                            params={"lat": HOME_LAT, "lng": HOME_LNG}
                        )

                        # Opțional: Le punem și pe 'AVAILABLE' dacă vrei să oprești cursele
                        status_res = requests.put(
                            f"{BASE_URL}/api/vehicles/{v['id']}", 
                            headers=headers, 
                            json={"plate": v['plate'], "brand": v['brand'], "type": v['type'], "status": "AVAILABLE"}
                        )

                        if update_res.status_code == 200:
                            print(f"✅ Vehicle {BOLD}{v['plate']}{RESET} ({v['brand']}) -> Returned to Cluj-Napoca.")
                        else:
                            print(f"{RED}❌ Failed to move {v['plate']}{RESET}")
                            
                        time.sleep(0.1) # Mică pauză să nu spamăm serverul
                    except Exception as e:
                        print(f"{RED}Error updating {v['plate']}: {e}{RESET}")
                
                print(f"\n{GREEN}✨ All vehicles have been recalled to base!{RESET}")
            else:
                print(f"{RED}Failed to fetch vehicle list.{RESET}")

        except Exception as e:
            print(f"{RED}Critical Error: {e}{RESET}")

if __name__ == "__main__":
    manager = FleetManager()
    manager.return_all_vehicles()