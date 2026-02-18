#!/usr/bin/env python3
"""
Download and Prepare Astronomy Images for Vyoma
Downloads images from data sources, compresses them, and places in assets folder
"""

import json
import os
import sys
import time
from pathlib import Path
from urllib.request import urlopen, Request
from urllib.error import URLError, HTTPError

try:
    from PIL import Image
except ImportError:
    print("❌ Pillow not installed. Run: pip install Pillow")
    sys.exit(1)

# Configuration
MAX_WIDTH = 800
WEBP_QUALITY = 75
DOWNLOAD_DELAY = 1  # seconds between downloads (be nice to servers)

def download_image(url, output_path, timeout=30):
    """
    Download image from URL
    
    Args:
        url: Image URL
        output_path: Where to save the image
        timeout: Request timeout in seconds
    
    Returns:
        bool: True if successful
    """
    try:
        # Add user agent to avoid blocks
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        request = Request(url, headers=headers)
        
        with urlopen(request, timeout=timeout) as response:
            data = response.read()
            
        with open(output_path, 'wb') as f:
            f.write(data)
            
        return True
        
    except (URLError, HTTPError) as e:
        print(f"  ✗ Download failed: {e}")
        return False
    except Exception as e:
        print(f"  ✗ Error: {e}")
        return False

def compress_and_convert(input_path, output_path, max_width=MAX_WIDTH, quality=WEBP_QUALITY):
    """
    Compress and convert image to WebP
    
    Args:
        input_path: Source image
        output_path: Destination WebP file
        max_width: Maximum width in pixels
        quality: WebP quality (0-100)
    
    Returns:
        tuple: (success, original_size_kb, compressed_size_kb)
    """
    try:
        img = Image.open(input_path)
        original_size = os.path.getsize(input_path) / 1024
        
        # Resize if needed
        if img.width > max_width:
            ratio = max_width / img.width
            new_height = int(img.height * ratio)
            img = img.resize((max_width, new_height), Image.LANCZOS)
        
        # Convert to RGB if necessary
        if img.mode in ('RGBA', 'LA'):
            background = Image.new('RGB', img.size, (255, 255, 255))
            if img.mode == 'RGBA':
                background.paste(img, mask=img.split()[3])
            else:
                background.paste(img, mask=img.split()[1])
            img = background
        elif img.mode == 'P':
            img = img.convert('RGB')
        elif img.mode not in ('RGB', 'L'):
            img = img.convert('RGB')
        
        # Save as WebP
        img.save(output_path, 'WEBP', quality=quality, method=6)
        compressed_size = os.path.getsize(output_path) / 1024
        
        return True, original_size, compressed_size
        
    except Exception as e:
        print(f"  ✗ Compression failed: {e}")
        return False, 0, 0

def extract_image_urls_from_json(json_file):
    """
    Extract image URLs from JSON data files
    
    Args:
        json_file: Path to JSON file
    
    Returns:
        list: List of (object_id, image_url) tuples
    """
    urls = []
    
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Handle different JSON structures
        if 'planets' in data:
            for planet in data['planets']:
                if 'image_urls' in planet:
                    # Prefer wikimedia, then nasa
                    url = planet['image_urls'].get('wikimedia') or planet['image_urls'].get('nasa')
                    if url:
                        urls.append((planet['id'], url))
            
            # Dwarf planets
            if 'dwarf_planets' in data:
                for dwarf in data['dwarf_planets']:
                    if 'image_urls' in dwarf:
                        url = dwarf['image_urls'].get('wikimedia') or dwarf['image_urls'].get('nasa')
                        if url:
                            urls.append((dwarf['id'], url))
        
        elif 'brightest_stars' in data:
            for star in data['brightest_stars']:
                if 'image_urls' in star:
                    url = star['image_urls'].get('wikimedia') or star['image_urls'].get('nasa')
                    if url:
                        urls.append((star['id'], url))
        
        elif 'nebulae' in data:
            for nebula in data['nebulae']:
                if 'image_urls' in nebula:
                    url = nebula['image_urls'].get('wikimedia') or nebula['image_urls'].get('nasa')
                    if url:
                        urls.append((nebula['id'], url))
        
        elif 'galaxies' in data:
            for galaxy in data['galaxies']:
                if 'image_urls' in galaxy:
                    url = galaxy['image_urls'].get('wikimedia') or galaxy['image_urls'].get('nasa')
                    if url:
                        urls.append((galaxy['id'], url))
        
        elif 'moons' in data:
            for moon in data['moons']:
                if 'image_urls' in moon:
                    url = moon['image_urls'].get('wikimedia') or moon['image_urls'].get('nasa')
                    if url:
                        urls.append((moon['id'], url))
        
        # Handle Sun separately
        if 'the_sun' in data:
            sun = data['the_sun']
            if 'image_urls' in sun:
                url = sun['image_urls'].get('wikimedia') or sun['image_urls'].get('nasa')
                if url:
                    urls.append((sun['id'], url))
    
    except Exception as e:
        print(f"  ✗ Error reading {json_file}: {e}")
    
    return urls

def main():
    """Main function"""
    print("=" * 70)
    print("Vyoma Image Download & Preparation Tool")
    print("=" * 70)
    
    # Setup directories
    data_dir = Path("Data/astronomy_data")
    temp_dir = Path("Data/temp_images")
    output_dir = Path("app/src/main/assets/images")
    
    temp_dir.mkdir(parents=True, exist_ok=True)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Check if data directory exists
    if not data_dir.exists():
        print(f"\n❌ Data directory not found: {data_dir}")
        print("Please ensure astronomy data files are in Data/astronomy_data/")
        return
    
    # Collect all image URLs from JSON files
    print(f"\n📂 Scanning {data_dir} for image URLs...")
    all_urls = []
    
    json_files = list(data_dir.glob("*.json"))
    for json_file in json_files:
        if json_file.name == "_index.json":
            continue
        print(f"  • {json_file.name}")
        urls = extract_image_urls_from_json(json_file)
        all_urls.extend(urls)
    
    if not all_urls:
        print("\n❌ No image URLs found in data files")
        return
    
    print(f"\n✓ Found {len(all_urls)} images to download")
    
    # Download and process images
    print(f"\n📥 Downloading and compressing images...")
    print(f"{'Object ID':<20s} {'Status':<10s} {'Original':>10s} {'Compressed':>10s} {'Reduction':>10s}")
    print("-" * 70)
    
    success_count = 0
    skip_count = 0
    fail_count = 0
    total_original = 0
    total_compressed = 0
    
    for obj_id, url in all_urls:
        output_file = output_dir / f"{obj_id}.webp"
        
        # Skip if already exists
        if output_file.exists():
            print(f"{obj_id:<20s} {'SKIP':<10s} (already exists)")
            skip_count += 1
            continue
        
        print(f"{obj_id:<20s} ", end='', flush=True)
        
        # Download to temp
        temp_file = temp_dir / f"{obj_id}_temp{Path(url).suffix}"
        
        if download_image(url, temp_file):
            # Compress and convert
            success, orig_size, comp_size = compress_and_convert(temp_file, output_file)
            
            if success:
                total_original += orig_size
                total_compressed += comp_size
                reduction = ((orig_size - comp_size) / orig_size) * 100 if orig_size > 0 else 0
                print(f"{'OK':<10s} {orig_size:>8.1f}KB {comp_size:>8.1f}KB {reduction:>8.1f}%")
                success_count += 1
                
                # Clean up temp file
                temp_file.unlink()
            else:
                print(f"{'FAIL':<10s} (compression error)")
                fail_count += 1
        else:
            print(f"{'FAIL':<10s} (download error)")
            fail_count += 1
        
        # Be nice to servers
        time.sleep(DOWNLOAD_DELAY)
    
    # Summary
    print("-" * 70)
    if total_original > 0:
        total_reduction = ((total_original - total_compressed) / total_original) * 100
        print(f"\n{'Total:':<20s} {total_original:>8.1f}KB → {total_compressed:>8.1f}KB ({total_reduction:>5.1f}% reduction)")
    
    print(f"\n📊 Summary:")
    print(f"  ✓ Successfully processed: {success_count}")
    print(f"  ⊘ Skipped (existing):    {skip_count}")
    print(f"  ✗ Failed:                {fail_count}")
    print(f"  📁 Output directory:     {output_dir}")
    
    # Clean up temp directory
    if temp_dir.exists():
        for f in temp_dir.iterdir():
            f.unlink()
        temp_dir.rmdir()
    
    if success_count > 0:
        print(f"\n✅ Images ready! Build the app to use offline images.")
        print(f"\nNext steps:")
        print(f"1. Build the app: ./gradlew assembleDebug")
        print(f"2. Install on device")
        print(f"3. Test in Airplane Mode")
    else:
        print(f"\n⚠️  No new images were downloaded.")

if __name__ == "__main__":
    main()
