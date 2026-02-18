#!/usr/bin/env python3
"""
Download All Gallery Images for Vyoma
Downloads all images from image_gallery.json with deduplication
"""

import json
import os
import sys
import time
import hashlib
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
DOWNLOAD_DELAY = 1.5  # seconds between downloads
MAX_RETRIES = 2

def download_image(url, output_path, timeout=30):
    """Download image from URL"""
    try:
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
        return False
    except Exception as e:
        return False

def compress_and_convert(input_path, output_path, max_width=MAX_WIDTH, quality=WEBP_QUALITY):
    """Compress and convert image to WebP"""
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
        return False, 0, 0

def get_url_hash(url):
    """Get short hash of URL for unique filename"""
    return hashlib.md5(url.encode()).hexdigest()[:8]

def main():
    """Main function"""
    print("=" * 70)
    print("🖼️  Vyoma Gallery Image Downloader")
    print("=" * 70)
    
    # Setup directories
    gallery_file = Path("app/src/main/assets/image_gallery.json")
    temp_dir = Path("Data/temp_gallery")
    output_dir = Path("app/src/main/assets/images")
    
    temp_dir.mkdir(parents=True, exist_ok=True)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Load gallery data
    if not gallery_file.exists():
        print(f"\n❌ Gallery file not found: {gallery_file}")
        print("Run: python tools/comprehensive_data_migration.py first")
        return
    
    with open(gallery_file, 'r', encoding='utf-8') as f:
        gallery_data = json.load(f)
    
    # Collect all unique URLs
    url_to_objects = {}  # url -> list of object_ids
    for obj_id, urls in gallery_data.items():
        for url in urls:
            if url not in url_to_objects:
                url_to_objects[url] = []
            url_to_objects[url].append(obj_id)
    
    print(f"\n📊 Gallery Statistics:")
    print(f"  Objects with images: {len(gallery_data)}")
    print(f"  Total image URLs: {sum(len(urls) for urls in gallery_data.values())}")
    print(f"  Unique URLs: {len(url_to_objects)}")
    
    # Download and process images
    print(f"\n📥 Downloading and compressing images...")
    print(f"{'URL Hash':<12s} {'Objects':<30s} {'Status':<10s} {'Size':<15s}")
    print("-" * 70)
    
    success_count = 0
    skip_count = 0
    fail_count = 0
    total_original = 0
    total_compressed = 0
    
    # Track which objects got images
    object_image_map = {}  # obj_id -> list of image filenames
    
    for url, obj_ids in url_to_objects.items():
        url_hash = get_url_hash(url)
        obj_names = ', '.join(obj_ids[:2])
        if len(obj_ids) > 2:
            obj_names += f" +{len(obj_ids)-2}"
        
        # Generate filename: first_object_id_hash.webp
        primary_obj = obj_ids[0]
        filename = f"{primary_obj}_{url_hash}.webp"
        output_file = output_dir / filename
        
        # Skip if already exists
        if output_file.exists():
            print(f"{url_hash:<12s} {obj_names:<30s} {'SKIP':<10s} (exists)")
            skip_count += 1
            # Track for all objects
            for obj_id in obj_ids:
                if obj_id not in object_image_map:
                    object_image_map[obj_id] = []
                object_image_map[obj_id].append(filename)
            continue
        
        print(f"{url_hash:<12s} {obj_names:<30s} ", end='', flush=True)
        
        # Download to temp
        temp_file = temp_dir / f"{url_hash}_temp{Path(url).suffix}"
        
        downloaded = False
        for attempt in range(MAX_RETRIES):
            if download_image(url, temp_file):
                downloaded = True
                break
            time.sleep(1)
        
        if downloaded:
            # Compress and convert
            success, orig_size, comp_size = compress_and_convert(temp_file, output_file)
            
            if success:
                total_original += orig_size
                total_compressed += comp_size
                reduction = ((orig_size - comp_size) / orig_size) * 100 if orig_size > 0 else 0
                print(f"{'OK':<10s} {orig_size:>6.1f}KB→{comp_size:>5.1f}KB")
                success_count += 1
                
                # Track for all objects
                for obj_id in obj_ids:
                    if obj_id not in object_image_map:
                        object_image_map[obj_id] = []
                    object_image_map[obj_id].append(filename)
                
                # Clean up temp file
                temp_file.unlink()
            else:
                print(f"{'FAIL':<10s} (compression)")
                fail_count += 1
        else:
            print(f"{'FAIL':<10s} (download)")
            fail_count += 1
        
        # Be nice to servers
        time.sleep(DOWNLOAD_DELAY)
    
    # Save object-to-images mapping
    mapping_file = output_dir.parent / "object_images.json"
    with open(mapping_file, 'w', encoding='utf-8') as f:
        json.dump(object_image_map, f, indent=2)
    
    # Summary
    print("-" * 70)
    if total_original > 0:
        total_reduction = ((total_original - total_compressed) / total_original) * 100
        print(f"\n{'Total:':<42s} {total_original:>8.1f}KB → {total_compressed:>8.1f}KB ({total_reduction:>5.1f}%)")
    
    print(f"\n📊 Summary:")
    print(f"  ✓ Successfully processed: {success_count}")
    print(f"  ⊘ Skipped (existing):    {skip_count}")
    print(f"  ✗ Failed:                {fail_count}")
    print(f"  📁 Output directory:     {output_dir}")
    print(f"  📋 Image mapping:        {mapping_file}")
    
    # Clean up temp directory
    if temp_dir.exists():
        for f in temp_dir.iterdir():
            f.unlink()
        temp_dir.rmdir()
    
    if success_count > 0 or skip_count > 0:
        print(f"\n✅ Gallery images ready!")
        print(f"\n🎯 Next Steps:")
        print(f"  1. Build app: ./gradlew assembleDebug")
        print(f"  2. Test gallery feature")
        print(f"  3. All objects now have multiple images!")

if __name__ == "__main__":
    main()
