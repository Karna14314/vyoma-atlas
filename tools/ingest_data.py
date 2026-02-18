import json
import re
import os
import sys

# Paths
ROOT_DIR = r"c:\Users\chait\Projects\Astronomy"
DATA_DIR = os.path.join(ROOT_DIR, "Data")
DATASETS_DIR = os.path.join(DATA_DIR, "datasets", "astronomy_data")
STARDROID_DIR = os.path.join(DATA_DIR, "stardroid", "tools", "data")
COMPLETE_DATA_FILE = os.path.join(DATA_DIR, "astronomy_data_complete.json")
OUTPUT_FILE = os.path.join(ROOT_DIR, "app", "src", "main", "assets", "initial_data.json")

# Ensure output directory exists
os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)

class DataIngestion:
    def __init__(self):
        self.objects = {}  # id -> { ... }

    def normalize_id(self, raw_id):
        if not raw_id: return None
        return raw_id.lower().replace(" ", "_").replace("*", "_star").strip()

    def process_complete_json(self):
        print("Processing astronomy_data_complete.json...")
        if not os.path.exists(COMPLETE_DATA_FILE): return
        with open(COMPLETE_DATA_FILE, 'r', encoding='utf-8') as f:
            data = json.load(f)
            
            # 1. Solar System
            ss = data.get("solar_system", {})
            sun = ss.get("sun")
            if sun:
                self.add_object({
                    "id": "sun",
                    "name": sun.get("name", "Sun"),
                    "type": "STAR",
                    "description": sun.get("description"),
                    "imageUrl": sun.get("image_url"),
                    "radiusKm": sun.get("diameter_km", 0) / 2
                })
            
            for p in ss.get("planets", []):
                pid = self.normalize_id(p.get("name"))
                self.add_object({
                    "id": pid,
                    "name": p.get("name"),
                    "type": "PLANET",
                    "description": p.get("description"),
                    "imageUrl": p.get("image_url"),
                    "radiusKm": p.get("diameter_km", 0) / 2,
                    "distanceAu": p.get("distance_from_sun_au"),
                    "parentId": "sun"
                })
                
            for dp in ss.get("dwarf_planets", []):
                dpid = self.normalize_id(dp.get("name"))
                self.add_object({
                    "id": dpid,
                    "name": dp.get("name"),
                    "type": "PLANET",
                    "description": dp.get("description"),
                    "imageUrl": dp.get("image_url"),
                    "radiusKm": dp.get("diameter_km", 0) / 2,
                    "parentId": "sun"
                })

            # 2. Stars
            for s in data.get("stars", {}).get("brightest", []):
                self.merge_object(self.normalize_id(s.get("name")), {
                    "name": s.get("name"),
                    "type": "STAR",
                    "description": s.get("description"),
                    "imageUrl": s.get("image_url"),
                    "magnitude": s.get("apparentMagnitude"),
                    "distanceLy": s.get("distanceLightYears"),
                    "constellation": s.get("constellation")
                })

            # 3. Galaxies
            for g in data.get("galaxies", []):
                self.add_object({
                    "id": self.normalize_id(g.get("name")),
                    "name": g.get("name"),
                    "type": "GALAXY",
                    "description": g.get("notableFeatures"),
                    "imageUrl": g.get("image_url"),
                    "distanceLy": g.get("distanceLightYears"),
                    "constellation": g.get("constellation")
                })

            # 4. Nebulae
            for n in data.get("nebulae", []):
                self.add_object({
                    "id": self.normalize_id(n.get("name")),
                    "name": n.get("name"),
                    "type": "NEBULA",
                    "description": n.get("description"),
                    "imageUrl": n.get("image_url"),
                    "distanceLy": n.get("distanceLightYears"),
                    "constellation": n.get("constellation")
                })

            # 5. Black Holes
            for bh in data.get("black_holes", []):
                self.add_object({
                    "id": self.normalize_id(bh.get("name")),
                    "name": bh.get("name"),
                    "type": "BLACK_HOLE",
                    "description": bh.get("description"),
                    "imageUrl": bh.get("image_url"),
                    "distanceLy": bh.get("distanceLightYears")
                })

    def process_moons_json(self):
        print("Processing moons.json...")
        path = os.path.join(DATASETS_DIR, "moons.json")
        if not os.path.exists(path): return
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            for m in data.get("major_moons", []):
                mid = self.normalize_id(m.get("id"))
                if not mid: continue
                
                parent_planet = self.normalize_id(m.get("planet"))
                
                desc = f"A moon of {m.get('planet')}."
                if m.get("interesting_facts"):
                    desc += " " + m.get("interesting_facts")[0]

                obj = {
                    "id": mid,
                    "name": m.get("name"),
                    "type": "MOON",
                    "description": desc,
                    "imageUrl": m.get("image_urls", {}).get("nasa") or m.get("image_urls", {}).get("wikimedia"),
                    "radiusKm": m.get("diameter_km", 0) / 2 if m.get("diameter_km") else 0,
                    "parentId": parent_planet,
                    "rightAscension": 0.0,
                    "declination": 0.0
                }
                self.merge_object(mid, obj)

    def process_stars_ascii(self):
        print("Processing stars.ascii...")
        stars_file = os.path.join(STARDROID_DIR, "stars.ascii")
        if not os.path.exists(stars_file): return
        current_source = {}
        level = 0
        with open(stars_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line: continue
                if level == 0 and line.startswith("source"):
                    current_source = {"type": "STAR"}
                open_count = line.count('{')
                close_count = line.count('}')
                level += open_count
                if level >= 1:
                    if "right_ascension:" in line:
                        try: current_source["rightAscension"] = float(line.split(":")[1].strip())
                        except: pass
                    if "declination:" in line:
                        try: current_source["declination"] = float(line.split(":")[1].strip())
                        except: pass
                    if "strings_str_id:" in line:
                        val = line.split(":")[1].strip().replace('"', '')
                        current_source["id"] = self.normalize_id(val)
                level -= close_count
                if level == 0 and close_count > 0 and current_source:
                    if "id" in current_source:
                        self.merge_object(current_source["id"], current_source)
                    current_source = {}

    def add_object(self, obj):
        oid = obj.get("id")
        if not oid: return
        self.objects[oid] = obj
    
    def merge_object(self, oid, updates):
        if oid in self.objects:
            self.objects[oid].update(updates)
        else:
            updates["id"] = oid
            self.objects[oid] = updates

    def save(self):
        results = list(self.objects.values())
        print(f"Saving {len(results)} objects to {OUTPUT_FILE}")
        with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
            json.dump(results, f, indent=2)

if __name__ == "__main__":
    di = DataIngestion()
    di.process_complete_json()
    di.process_moons_json() # Call moons
    di.process_stars_ascii()
    di.save()
