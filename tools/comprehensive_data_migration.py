#!/usr/bin/env python3
"""
Comprehensive Data Migration for Vyoma
Extracts all astronomy data from Data folder, handles duplicates,
creates gallery system with multiple images per object
"""

import json
import os
from pathlib import Path
from collections import defaultdict
from typing import Dict, List, Any, Set

# Coordinate mappings for objects (RA in degrees, Dec in degrees)
COORDINATES = {
    # Planets (approximate - will need ephemeris for real-time)
    "mercury": {"ra": 0, "dec": 0},
    "venus": {"ra": 0, "dec": 0},
    "mars": {"ra": 0, "dec": 0},
    "jupiter": {"ra": 0, "dec": 0},
    "saturn": {"ra": 0, "dec": 0},
    "uranus": {"ra": 0, "dec": 0},
    "neptune": {"ra": 0, "dec": 0},
    
    # Bright Stars
    "sirius": {"ra": 101.25, "dec": -16.71},
    "canopus": {"ra": 96.0, "dec": -52.7},
    "arcturus": {"ra": 213.9, "dec": 19.19},
    "vega": {"ra": 279.3, "dec": 38.78},
    "rigel": {"ra": 78.6, "dec": -8.2},
    "betelgeuse": {"ra": 88.8, "dec": 7.41},
    "polaris": {"ra": 37.95, "dec": 89.26},
    "aldebaran": {"ra": 68.98, "dec": 16.51},
    "antares": {"ra": 247.35, "dec": -26.43},
    "spica": {"ra": 201.3, "dec": -11.16},
    "altair": {"ra": 297.7, "dec": 8.87},
    "deneb": {"ra": 310.4, "dec": 45.28},
    
    # Deep Sky Objects
    "andromeda_galaxy": {"ra": 10.68, "dec": 41.27},
    "m31": {"ra": 10.68, "dec": 41.27},
    "orion_nebula": {"ra": 83.82, "dec": -5.39},
    "m42": {"ra": 83.82, "dec": -5.39},
    "pleiades": {"ra": 56.75, "dec": 24.11},
    "m45": {"ra": 56.75, "dec": 24.11},
    "crab_nebula": {"ra": 83.63, "dec": 22.01},
    "m1": {"ra": 83.63, "dec": 22.01},
    "whirlpool_galaxy": {"ra": 202.47, "dec": 47.20},
    "m51": {"ra": 202.47, "dec": 47.20},
    "ring_nebula": {"ra": 283.4, "dec": 33.03},
    "m57": {"ra": 283.4, "dec": 33.03},
    "sombrero_galaxy": {"ra": 189.99, "dec": -11.62},
    "m104": {"ra": 189.99, "dec": -11.62},
}

class AstronomyDataMigrator:
    def __init__(self):
        self.objects = {}  # id -> object data
        self.duplicates = defaultdict(list)  # Track duplicates
        self.image_gallery = defaultdict(list)  # id -> list of image URLs
        self.categories = {
            "Solar System": [],
            "Stars": [],
            "Constellations": [],
            "Deep Sky": [],
            "Exoplanets": [],
            "Small Bodies": []
        }
        
    def load_json(self, filepath: Path) -> Dict:
        """Load JSON file safely"""
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            print(f"  ⚠️  Error loading {filepath.name}: {e}")
            return {}
    
    def extract_images(self, data: Dict) -> List[str]:
        """Extract all image URLs from various formats"""
        images = []
        
        # Handle image_urls dict
        if 'image_urls' in data:
            img_urls = data['image_urls']
            if isinstance(img_urls, dict):
                for key, url in img_urls.items():
                    if url and isinstance(url, str):
                        images.append(url)
            elif isinstance(img_urls, str):
                images.append(img_urls)
        
        # Handle single imageUrl
        if 'imageUrl' in data and data['imageUrl']:
            images.append(data['imageUrl'])
        
        # Handle image field
        if 'image' in data and data['image']:
            images.append(data['image'])
        
        return list(set(images))  # Remove duplicates
    
    def normalize_id(self, obj_id: str) -> str:
        """Normalize object ID"""
        return obj_id.lower().replace(' ', '_').replace('-', '_')
    
    def merge_object_data(self, existing: Dict, new: Dict) -> Dict:
        """Merge two object data dicts, keeping the most complete info"""
        merged = existing.copy()
        
        # Merge descriptions
        if new.get('description') and len(new.get('description', '')) > len(existing.get('description', '')):
            merged['description'] = new['description']
        
        # Merge interesting facts
        existing_facts = existing.get('interesting_facts', [])
        new_facts = new.get('interesting_facts', [])
        all_facts = list(set(existing_facts + new_facts))
        if all_facts:
            merged['interesting_facts'] = all_facts
        
        # Merge numeric fields (prefer non-null)
        for field in ['magnitude', 'distanceLy', 'distanceAu', 'radiusKm', 'mass_kg', 'diameter_km']:
            if field in new and new[field] is not None:
                if field not in existing or existing[field] is None:
                    merged[field] = new[field]
        
        # Merge coordinates
        if 'rightAscension' in new and new['rightAscension'] is not None:
            merged['rightAscension'] = new['rightAscension']
        if 'declination' in new and new['declination'] is not None:
            merged['declination'] = new['declination']
        
        return merged
    
    def process_planets(self, data: Dict):
        """Process planets.json"""
        print("  📍 Processing planets...")
        
        # Process main planets
        for planet in data.get('planets', []):
            obj_id = self.normalize_id(planet['id'])
            
            coords = COORDINATES.get(obj_id, {"ra": None, "dec": None})
            
            # Build description
            desc = planet.get('description', '')
            facts = planet.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            obj = {
                "id": obj_id,
                "name": planet['name'],
                "type": "PLANET",
                "category": "Solar System",
                "description": desc,
                "distanceAu": planet.get('distance_from_sun_au'),
                "radiusKm": planet.get('diameter_km', 0) / 2 if planet.get('diameter_km') else None,
                "magnitude": self.get_planet_magnitude(obj_id),
                "rightAscension": coords['ra'],
                "declination": coords['dec'],
                "parentId": "sun",
                "metadata": {
                    "mass_kg": planet.get('mass_kg'),
                    "orbital_period_days": planet.get('orbital_period_days'),
                    "rotation_period_hours": planet.get('rotation_period_hours'),
                    "moons_count": planet.get('moons_count'),
                    "has_rings": planet.get('has_rings'),
                    "atmosphere": planet.get('atmosphere')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Solar System"].append(obj_id)
            
            # Extract images
            images = self.extract_images(planet)
            self.image_gallery[obj_id].extend(images)
        
        # Process dwarf planets
        for dwarf in data.get('dwarf_planets', []):
            obj_id = self.normalize_id(dwarf['id'])
            
            obj = {
                "id": obj_id,
                "name": dwarf['name'],
                "type": "DWARF_PLANET",
                "category": "Solar System",
                "description": dwarf.get('description', ''),
                "distanceAu": dwarf.get('distance_from_sun_au'),
                "radiusKm": dwarf.get('diameter_km', 0) / 2 if dwarf.get('diameter_km') else None,
                "magnitude": 14.0,
                "parentId": "sun",
                "metadata": {
                    "moons_count": dwarf.get('moons_count')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Solar System"].append(obj_id)
            
            images = self.extract_images(dwarf)
            self.image_gallery[obj_id].extend(images)
        
        print(f"    ✓ Processed {len(data.get('planets', []))} planets, {len(data.get('dwarf_planets', []))} dwarf planets")
    
    def process_stars(self, data: Dict):
        """Process stars.json"""
        print("  ⭐ Processing stars...")
        
        # Process Sun
        if 'the_sun' in data:
            sun = data['the_sun']
            obj_id = self.normalize_id(sun['id'])
            
            desc = "The star at the center of our Solar System."
            facts = sun.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            obj = {
                "id": obj_id,
                "name": sun['name'],
                "type": "STAR",
                "category": "Solar System",
                "description": desc,
                "radiusKm": sun.get('diameter_km', 0) / 2 if sun.get('diameter_km') else None,
                "magnitude": -26.74,
                "rightAscension": 0.0,
                "declination": 0.0,
                "metadata": {
                    "spectral_class": sun.get('spectral_class'),
                    "surface_temp_c": sun.get('surface_temp_c'),
                    "age_billion_years": sun.get('age_billion_years')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Solar System"].append(obj_id)
            
            images = self.extract_images(sun)
            self.image_gallery[obj_id].extend(images)
        
        # Process bright stars
        for star in data.get('brightest_stars', []):
            obj_id = self.normalize_id(star['id'])
            coords = COORDINATES.get(obj_id, {"ra": None, "dec": None})
            
            desc = star.get('description', '')
            facts = star.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            obj = {
                "id": obj_id,
                "name": star['name'],
                "type": "STAR",
                "category": "Stars",
                "description": desc,
                "magnitude": star.get('apparent_magnitude'),
                "distanceLy": star.get('distance_ly'),
                "constellation": star.get('constellation'),
                "rightAscension": coords['ra'],
                "declination": coords['dec'],
                "metadata": {
                    "spectral_type": star.get('spectral_type'),
                    "mass_solar": star.get('mass_solar'),
                    "radius_solar": star.get('radius_solar'),
                    "luminosity_solar": star.get('luminosity_solar')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Stars"].append(obj_id)
            
            images = self.extract_images(star)
            self.image_gallery[obj_id].extend(images)
        
        print(f"    ✓ Processed Sun + {len(data.get('brightest_stars', []))} stars")
    
    def process_moons(self, data: Dict):
        """Process moons.json"""
        print("  🌙 Processing moons...")
        
        for moon in data.get('major_moons', []):
            obj_id = self.normalize_id(moon['id'])
            
            desc = moon.get('description', '')
            facts = moon.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            parent_planet = self.normalize_id(moon.get('planet', 'unknown'))
            
            obj = {
                "id": obj_id,
                "name": moon['name'],
                "type": "MOON",
                "category": "Solar System",
                "description": desc,
                "radiusKm": moon.get('diameter_km', 0) / 2 if moon.get('diameter_km') else None,
                "magnitude": moon.get('magnitude'),
                "parentId": parent_planet,
                "metadata": {
                    "mass_kg": moon.get('mass_kg'),
                    "orbital_period_days": moon.get('orbital_period_days'),
                    "discovered": moon.get('discovered'),
                    "distance_from_planet_km": moon.get('distance_from_planet_km')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Solar System"].append(obj_id)
            
            images = self.extract_images(moon)
            self.image_gallery[obj_id].extend(images)
        
        print(f"    ✓ Processed {len(data.get('major_moons', []))} moons")
    
    def process_nebulae(self, data: Dict):
        """Process nebulae.json"""
        print("  🌌 Processing nebulae...")
        
        for nebula in data.get('notable_nebulae', []):
            obj_id = self.normalize_id(nebula['id'])
            coords = COORDINATES.get(obj_id, {"ra": None, "dec": None})
            
            desc = nebula.get('description', '')
            facts = nebula.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            obj = {
                "id": obj_id,
                "name": nebula['name'],
                "type": "NEBULA",
                "category": "Deep Sky",
                "description": desc,
                "magnitude": nebula.get('magnitude'),
                "distanceLy": nebula.get('distance_ly'),
                "constellation": nebula.get('constellation'),
                "rightAscension": coords['ra'],
                "declination": coords['dec'],
                "metadata": {
                    "nebula_type": nebula.get('type'),
                    "size_ly": nebula.get('size_ly')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Deep Sky"].append(obj_id)
            
            images = self.extract_images(nebula)
            self.image_gallery[obj_id].extend(images)
        
        print(f"    ✓ Processed {len(data.get('notable_nebulae', []))} nebulae")
    
    def process_galaxies(self, data: Dict):
        """Process galaxies.json"""
        print("  🌀 Processing galaxies...")
        
        for galaxy in data.get('notable_galaxies', []):
            obj_id = self.normalize_id(galaxy['id'])
            coords = COORDINATES.get(obj_id, {"ra": None, "dec": None})
            
            desc = galaxy.get('description', '')
            facts = galaxy.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            obj = {
                "id": obj_id,
                "name": galaxy['name'],
                "type": "GALAXY",
                "category": "Deep Sky",
                "description": desc,
                "magnitude": galaxy.get('magnitude'),
                "distanceLy": galaxy.get('distance_ly'),
                "constellation": galaxy.get('constellation'),
                "rightAscension": coords['ra'],
                "declination": coords['dec'],
                "metadata": {
                    "galaxy_type": galaxy.get('type'),
                    "diameter_ly": galaxy.get('diameter_ly')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Deep Sky"].append(obj_id)
            
            images = self.extract_images(galaxy)
            self.image_gallery[obj_id].extend(images)
        
        print(f"    ✓ Processed {len(data.get('notable_galaxies', []))} galaxies")
    
    def process_constellations(self, data: Dict):
        """Process constellations.json"""
        print("  ✨ Processing constellations...")
        
        count = 0
        for constellation in data.get('major_constellations', []):
            obj_id = self.normalize_id(constellation['id'])
            
            # Get RA/Dec from brightest star if available
            coords = {"ra": None, "dec": None}
            if 'brightest_star' in constellation:
                star_data = constellation['brightest_star']
                if isinstance(star_data, dict):
                    star_name = self.normalize_id(star_data.get('name', ''))
                    coords = COORDINATES.get(star_name, coords)
            
            desc = constellation.get('description', '')
            facts = constellation.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:3])
            
            obj = {
                "id": obj_id,
                "name": constellation['name'],
                "type": "CONSTELLATION",
                "category": "Constellations",
                "description": desc,
                "rightAscension": coords['ra'],
                "declination": coords['dec'],
                "metadata": {
                    "abbreviation": constellation.get('abbreviation'),
                    "area_sq_deg": constellation.get('area_sq_deg'),
                    "brightest_star": constellation.get('brightest_star', {}).get('name') if isinstance(constellation.get('brightest_star'), dict) else None,
                    "mythology": constellation.get('mythology')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Constellations"].append(obj_id)
            
            images = self.extract_images(constellation)
            self.image_gallery[obj_id].extend(images)
            count += 1
        
        print(f"    ✓ Processed {count} constellations")
    
    def process_small_bodies(self, data: Dict):
        """Process small_bodies.json"""
        print("  ☄️  Processing small bodies...")
        
        # Asteroids
        asteroids_data = data.get('asteroids', {})
        for asteroid in asteroids_data.get('notable_asteroids', []):
            obj_id = self.normalize_id(asteroid['id'])
            
            desc = asteroid.get('description', '')
            facts = asteroid.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            obj = {
                "id": obj_id,
                "name": asteroid['name'],
                "type": "ASTEROID",
                "category": "Small Bodies",
                "description": desc,
                "radiusKm": asteroid.get('diameter_km', 0) / 2 if asteroid.get('diameter_km') else None,
                "metadata": {
                    "discovery_year": asteroid.get('discovery_year'),
                    "discoverer": asteroid.get('discoverer'),
                    "location": asteroid.get('location')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Small Bodies"].append(obj_id)
            
            images = self.extract_images(asteroid)
            self.image_gallery[obj_id].extend(images)
        
        # Comets
        comets_data = data.get('comets', {})
        for comet in comets_data.get('notable_comets', []):
            obj_id = self.normalize_id(comet['id'])
            
            desc = comet.get('description', '')
            facts = comet.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            obj = {
                "id": obj_id,
                "name": comet['name'],
                "type": "COMET",
                "category": "Small Bodies",
                "description": desc,
                "metadata": {
                    "orbital_period_years": comet.get('orbital_period_years'),
                    "discovery_year": comet.get('discovery_year'),
                    "discoverer": comet.get('discoverer')
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Small Bodies"].append(obj_id)
            
            images = self.extract_images(comet)
            self.image_gallery[obj_id].extend(images)
        
        asteroid_count = len(asteroids_data.get('notable_asteroids', []))
        comet_count = len(comets_data.get('notable_comets', []))
        print(f"    ✓ Processed {asteroid_count} asteroids, {comet_count} comets")
    
    def process_exoplanets(self, data: Dict):
        """Process exoplanets.json"""
        print("  🪐 Processing exoplanets...")
        
        for system in data.get('notable_systems', []):
            obj_id = self.normalize_id(system['id'])
            
            desc = system.get('description', '')
            facts = system.get('interesting_facts', [])
            if facts:
                desc += "\n\n" + "\n".join(f"• {fact}" for fact in facts[:5])
            
            # Add planet count info
            planet_count = system.get('planets_count', 0)
            if planet_count:
                desc = f"System with {planet_count} known planets.\n\n" + desc
            
            obj = {
                "id": obj_id,
                "name": system['name'],
                "type": "EXOPLANET_SYSTEM",
                "category": "Exoplanets",
                "description": desc,
                "distanceLy": system.get('distance_ly'),
                "constellation": system.get('constellation'),
                "metadata": {
                    "star_type": system.get('star_type'),
                    "discovery_year": system.get('discovery_year'),
                    "planets_count": planet_count
                }
            }
            
            self.objects[obj_id] = obj
            self.categories["Exoplanets"].append(obj_id)
            
            images = self.extract_images(system)
            self.image_gallery[obj_id].extend(images)
        
        print(f"    ✓ Processed {len(data.get('notable_systems', []))} exoplanet systems")
    
    def get_planet_magnitude(self, planet_id: str) -> float:
        """Get approximate magnitude for planets"""
        magnitudes = {
            "mercury": -0.4, "venus": -4.6, "earth": -3.99, "mars": -2.94,
            "jupiter": -2.94, "saturn": 0.46, "uranus": 5.68, "neptune": 7.78
        }
        return magnitudes.get(planet_id)
    
    def migrate_all_data(self):
        """Main migration function"""
        print("=" * 70)
        print("🚀 Comprehensive Astronomy Data Migration")
        print("=" * 70)
        
        data_dir = Path("Data/astronomy_data")
        
        if not data_dir.exists():
            print(f"\n❌ Data directory not found: {data_dir}")
            return
        
        print(f"\n📂 Scanning {data_dir}...")
        
        # Process each data file
        files_to_process = {
            'planets.json': self.process_planets,
            'stars.json': self.process_stars,
            'moons.json': self.process_moons,
            'nebulae.json': self.process_nebulae,
            'galaxies.json': self.process_galaxies,
            'constellations.json': self.process_constellations,
            'small_bodies.json': self.process_small_bodies,
            'exoplanets.json': self.process_exoplanets,
        }
        
        for filename, processor in files_to_process.items():
            filepath = data_dir / filename
            if filepath.exists():
                data = self.load_json(filepath)
                if data:
                    processor(data)
        
        # Generate output
        self.generate_output()
    
    def generate_output(self):
        """Generate final output files"""
        print("\n" + "=" * 70)
        print("📝 Generating Output Files")
        print("=" * 70)
        
        output_dir = Path("app/src/main/assets")
        output_dir.mkdir(parents=True, exist_ok=True)
        
        # 1. Main objects data
        objects_list = list(self.objects.values())
        objects_file = output_dir / "astronomy_objects.json"
        
        with open(objects_file, 'w', encoding='utf-8') as f:
            json.dump(objects_list, f, indent=2, ensure_ascii=False)
        
        print(f"\n✓ Created {objects_file}")
        print(f"  Total objects: {len(objects_list)}")
        
        # 2. Image gallery data
        gallery_data = {
            obj_id: images 
            for obj_id, images in self.image_gallery.items() 
            if images
        }
        gallery_file = output_dir / "image_gallery.json"
        
        with open(gallery_file, 'w', encoding='utf-8') as f:
            json.dump(gallery_data, f, indent=2, ensure_ascii=False)
        
        print(f"\n✓ Created {gallery_file}")
        print(f"  Objects with images: {len(gallery_data)}")
        total_images = sum(len(imgs) for imgs in gallery_data.values())
        print(f"  Total image URLs: {total_images}")
        
        # 3. Categories index
        categories_file = output_dir / "categories.json"
        
        with open(categories_file, 'w', encoding='utf-8') as f:
            json.dump(self.categories, f, indent=2, ensure_ascii=False)
        
        print(f"\n✓ Created {categories_file}")
        for cat, items in self.categories.items():
            print(f"  {cat}: {len(items)} objects")
        
        # 4. Summary
        print("\n" + "=" * 70)
        print("📊 Migration Summary")
        print("=" * 70)
        print(f"\n✅ Successfully migrated {len(objects_list)} objects")
        print(f"✅ Collected {total_images} image URLs")
        print(f"✅ Organized into {len(self.categories)} categories")
        
        print(f"\n📁 Output files:")
        print(f"  • {objects_file}")
        print(f"  • {gallery_file}")
        print(f"  • {categories_file}")

def main():
    migrator = AstronomyDataMigrator()
    migrator.migrate_all_data()
    
    print(f"\n🎯 Next Steps:")
    print(f"  1. Run: python tools/download_all_gallery_images.py")
    print(f"  2. Build app with new data")
    print(f"  3. Enjoy comprehensive astronomy database!")

if __name__ == "__main__":
    main()
