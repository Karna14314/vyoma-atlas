#!/usr/bin/env python3
"""
Astronomy Data Preparation Script
Converts comprehensive JSON data into Android-compatible format
"""

import json
import os
from pathlib import Path

# Coordinate data for major objects (RA in degrees, Dec in degrees)
COORDINATES = {
    # Planets - approximate current positions (will need ephemeris for accuracy)
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
    
    # Deep Sky Objects
    "andromeda_galaxy": {"ra": 10.68, "dec": 41.27},
    "orion_nebula": {"ra": 83.82, "dec": -5.39},
    "pleiades": {"ra": 56.75, "dec": 24.11},
}

def load_json_file(filepath):
    """Load JSON file"""
    with open(filepath, 'r', encoding='utf-8') as f:
        return json.load(f)

def convert_planet_to_object(planet, category="Solar System"):
    """Convert planet data to AstronomicalObject format"""
    coords = COORDINATES.get(planet['id'], {"ra": None, "dec": None})
    
    # Get best image URL
    image_url = None
    if 'image_urls' in planet:
        image_url = planet['image_urls'].get('wikimedia') or planet['image_urls'].get('nasa')
    
    # Calculate magnitude (approximate for planets)
    magnitude_map = {
        "mercury": -0.4, "venus": -4.6, "earth": -3.99, "mars": -2.94,
        "jupiter": -2.94, "saturn": 0.46, "uranus": 5.68, "neptune": 7.78
    }
    
    description = planet.get('description', '')
    if 'interesting_facts' in planet and planet['interesting_facts']:
        description += "\n\n" + "\n".join(f"• {fact}" for fact in planet['interesting_facts'][:3])
    
    return {
        "id": planet['id'],
        "name": planet['name'],
        "type": "PLANET",
        "category": category,
        "description": description,
        "distanceAu": planet.get('distance_from_sun_au'),
        "radiusKm": planet.get('diameter_km', 0) / 2,
        "magnitude": magnitude_map.get(planet['id']),
        "imageUrl": image_url,
        "rightAscension": coords['ra'],
        "declination": coords['dec'],
        "parentId": "sun"
    }

def convert_star_to_object(star, category="Stars"):
    """Convert star data to AstronomicalObject format"""
    coords = COORDINATES.get(star['id'], {"ra": None, "dec": None})
    
    description = star.get('description', '')
    if 'interesting_facts' in star and star['interesting_facts']:
        description += "\n\n" + "\n".join(f"• {fact}" for fact in star['interesting_facts'][:3])
    
    return {
        "id": star['id'],
        "name": star['name'],
        "type": "STAR",
        "category": category,
        "description": description,
        "distanceLy": star.get('distance_ly'),
        "magnitude": star.get('magnitude'),
        "constellation": star.get('constellation'),
        "imageUrl": star.get('image_url'),
        "rightAscension": coords['ra'],
        "declination": coords['dec']
    }

def convert_nebula_to_object(nebula, category="Deep Sky"):
    """Convert nebula data to AstronomicalObject format"""
    coords = COORDINATES.get(nebula['id'], {"ra": None, "dec": None})
    
    return {
        "id": nebula['id'],
        "name": nebula['name'],
        "type": "NEBULA",
        "category": category,
        "description": nebula.get('description'),
        "distanceLy": nebula.get('distance_ly'),
        "magnitude": nebula.get('magnitude'),
        "constellation": nebula.get('constellation'),
        "imageUrl": nebula.get('image_url'),
        "rightAscension": coords['ra'],
        "declination": coords['dec']
    }

def convert_galaxy_to_object(galaxy, category="Deep Sky"):
    """Convert galaxy data to AstronomicalObject format"""
    coords = COORDINATES.get(galaxy['id'], {"ra": None, "dec": None})
    
    return {
        "id": galaxy['id'],
        "name": galaxy['name'],
        "type": "GALAXY",
        "category": category,
        "description": galaxy.get('description'),
        "distanceLy": galaxy.get('distance_ly'),
        "magnitude": galaxy.get('magnitude'),
        "constellation": galaxy.get('constellation'),
        "imageUrl": galaxy.get('image_url'),
        "rightAscension": coords['ra'],
        "declination": coords['dec']
    }

def main():
    """Main conversion function"""
    data_dir = Path("Data/astronomy_data")
    output_file = Path("app/src/main/assets/initial_data.json")
    
    all_objects = []
    
    # Add Sun
    all_objects.append({
        "id": "sun",
        "name": "Sun",
        "type": "STAR",
        "category": "Solar System",
        "description": "The star at the center of our Solar System, containing 99.86% of the system's mass. A G-type main-sequence star.",
        "imageUrl": "https://science.nasa.gov/wp-content/uploads/2023/05/sun-800.jpg",
        "radiusKm": 696350.0,
        "magnitude": -26.74,
        "rightAscension": 0.0,
        "declination": 0.0
    })
    
    # Process planets
    if (data_dir / "planets.json").exists():
        planets_data = load_json_file(data_dir / "planets.json")
        for planet in planets_data.get('planets', []):
            all_objects.append(convert_planet_to_object(planet))
        
        # Add dwarf planets
        for dwarf in planets_data.get('dwarf_planets', []):
            obj = convert_planet_to_object(dwarf, "Solar System")
            obj['type'] = "DWARF_PLANET"
            all_objects.append(obj)
    
    # Process stars
    if (data_dir / "stars.json").exists():
        stars_data = load_json_file(data_dir / "stars.json")
        for star in stars_data.get('stars', [])[:50]:  # Top 50 brightest
            all_objects.append(convert_star_to_object(star))
    
    # Process nebulae
    if (data_dir / "nebulae.json").exists():
        nebulae_data = load_json_file(data_dir / "nebulae.json")
        for nebula in nebulae_data.get('nebulae', [])[:20]:
            all_objects.append(convert_nebula_to_object(nebula))
    
    # Process galaxies
    if (data_dir / "galaxies.json").exists():
        galaxies_data = load_json_file(data_dir / "galaxies.json")
        for galaxy in galaxies_data.get('galaxies', [])[:20]:
            all_objects.append(convert_galaxy_to_object(galaxy))
    
    # Write output
    output_file.parent.mkdir(parents=True, exist_ok=True)
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(all_objects, f, indent=2, ensure_ascii=False)
    
    print(f"✓ Generated {len(all_objects)} objects")
    print(f"✓ Saved to {output_file}")
    
    # Print summary
    categories = {}
    for obj in all_objects:
        cat = obj.get('category', 'Unknown')
        categories[cat] = categories.get(cat, 0) + 1
    
    print("\nCategory Summary:")
    for cat, count in sorted(categories.items()):
        print(f"  {cat}: {count} objects")

if __name__ == "__main__":
    main()
