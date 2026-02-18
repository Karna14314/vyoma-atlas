#!/usr/bin/env python3
"""
Generate Placeholder Images for Missing Astronomy Objects
Creates beautiful gradient placeholders with object initials
"""

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("❌ Pillow not installed. Run: pip install Pillow")
    exit(1)

from pathlib import Path
import math

# Objects that need placeholders
PLACEHOLDERS = {
    "sun": {"name": "Sun", "colors": [(255, 200, 0), (255, 150, 0), (255, 100, 0)]},
    "moon": {"name": "Moon", "colors": [(200, 200, 200), (150, 150, 150), (100, 100, 100)]},
    "pluto": {"name": "Pluto", "colors": [(180, 150, 120), (140, 110, 90), (100, 80, 60)]},
    "sirius": {"name": "Sirius", "colors": [(200, 220, 255), (150, 180, 255), (100, 140, 255)]},
    "canopus": {"name": "Canopus", "colors": [(255, 240, 220), (255, 220, 180), (255, 200, 140)]},
    "arcturus": {"name": "Arcturus", "colors": [(255, 180, 100), (255, 140, 60), (255, 100, 20)]},
    "vega": {"name": "Vega", "colors": [(220, 230, 255), (180, 200, 255), (140, 170, 255)]},
    "rigel": {"name": "Rigel", "colors": [(200, 220, 255), (160, 190, 255), (120, 160, 255)]},
    "betelgeuse": {"name": "Betelgeuse", "colors": [(255, 100, 50), (255, 60, 20), (200, 40, 10)]},
    "polaris": {"name": "Polaris", "colors": [(240, 240, 255), (200, 200, 255), (160, 160, 255)]},
    "andromeda_galaxy": {"name": "Andromeda", "colors": [(150, 100, 200), (100, 60, 150), (60, 30, 100)]},
    "orion_nebula": {"name": "Orion Nebula", "colors": [(255, 100, 150), (200, 60, 100), (150, 30, 60)]},
    "pleiades": {"name": "Pleiades", "colors": [(180, 200, 255), (140, 160, 255), (100, 120, 255)]},
}

def create_radial_gradient(size, colors):
    """Create a radial gradient image"""
    img = Image.new('RGB', size)
    draw = ImageDraw.Draw(img)
    
    center_x, center_y = size[0] // 2, size[1] // 2
    max_radius = math.sqrt(center_x**2 + center_y**2)
    
    for y in range(size[1]):
        for x in range(size[0]):
            # Calculate distance from center
            dx = x - center_x
            dy = y - center_y
            distance = math.sqrt(dx*dx + dy*dy)
            
            # Normalize distance (0 to 1)
            ratio = distance / max_radius
            
            # Interpolate between colors
            if ratio < 0.33:
                # Between color 0 and 1
                t = ratio / 0.33
                r = int(colors[0][0] * (1-t) + colors[1][0] * t)
                g = int(colors[0][1] * (1-t) + colors[1][1] * t)
                b = int(colors[0][2] * (1-t) + colors[1][2] * t)
            elif ratio < 0.66:
                # Between color 1 and 2
                t = (ratio - 0.33) / 0.33
                r = int(colors[1][0] * (1-t) + colors[2][0] * t)
                g = int(colors[1][1] * (1-t) + colors[2][1] * t)
                b = int(colors[1][2] * (1-t) + colors[2][2] * t)
            else:
                # Fade to color 2
                r, g, b = colors[2]
            
            img.putpixel((x, y), (r, g, b))
    
    return img

def add_text_to_image(img, text, font_size=200):
    """Add text overlay to image"""
    draw = ImageDraw.Draw(img)
    
    # Try to use a nice font, fall back to default
    try:
        font = ImageFont.truetype("arial.ttf", font_size)
    except:
        try:
            font = ImageFont.truetype("Arial.ttf", font_size)
        except:
            font = ImageFont.load_default()
    
    # Get text bounding box
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    # Center text
    x = (img.width - text_width) // 2
    y = (img.height - text_height) // 2
    
    # Draw text with shadow for better visibility
    shadow_offset = 4
    draw.text((x + shadow_offset, y + shadow_offset), text, font=font, fill=(0, 0, 0, 128))
    draw.text((x, y), text, font=font, fill=(255, 255, 255, 230))
    
    return img

def generate_placeholder(obj_id, name, colors, size=(800, 800), output_dir=Path("app/src/main/assets/images")):
    """Generate a placeholder image"""
    output_file = output_dir / f"{obj_id}.webp"
    
    # Skip if already exists
    if output_file.exists():
        return False
    
    # Create gradient
    img = create_radial_gradient(size, colors)
    
    # Get initials
    words = name.split()
    initials = ''.join(word[0].upper() for word in words[:2])
    
    # Add text
    img = add_text_to_image(img, initials)
    
    # Save as WebP
    img.save(output_file, 'WEBP', quality=75)
    
    return True

def main():
    """Main function"""
    print("=" * 70)
    print("Vyoma Placeholder Image Generator")
    print("=" * 70)
    
    output_dir = Path("app/src/main/assets/images")
    output_dir.mkdir(parents=True, exist_ok=True)
    
    print(f"\n📂 Output directory: {output_dir}")
    print(f"\n🎨 Generating placeholder images...")
    print(f"{'Object ID':<20s} {'Name':<20s} {'Status':<10s}")
    print("-" * 70)
    
    generated = 0
    skipped = 0
    
    for obj_id, data in PLACEHOLDERS.items():
        name = data['name']
        colors = data['colors']
        
        if generate_placeholder(obj_id, name, colors, output_dir=output_dir):
            print(f"{obj_id:<20s} {name:<20s} {'CREATED':<10s}")
            generated += 1
        else:
            print(f"{obj_id:<20s} {name:<20s} {'SKIP':<10s} (exists)")
            skipped += 1
    
    print("-" * 70)
    print(f"\n📊 Summary:")
    print(f"  ✓ Generated: {generated}")
    print(f"  ⊘ Skipped:   {skipped}")
    
    if generated > 0:
        print(f"\n✅ Placeholder images created!")
        print(f"\nThese will be used as fallbacks until real images are downloaded.")

if __name__ == "__main__":
    main()
