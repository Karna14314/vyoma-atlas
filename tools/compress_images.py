#!/usr/bin/env python3
"""
Image Compression Script for Vyoma
Converts images to WebP format with compression
"""

try:
    from PIL import Image
except ImportError:
    print("❌ Pillow not installed. Run: pip install Pillow")
    exit(1)

import os
from pathlib import Path

def compress_image(input_path, output_path, max_width=800, quality=75):
    """
    Compress and convert image to WebP format
    
    Args:
        input_path: Source image file
        output_path: Destination WebP file
        max_width: Maximum width in pixels
        quality: WebP quality (0-100)
    """
    try:
        img = Image.open(input_path)
        
        # Get original size
        original_size = os.path.getsize(input_path) / 1024  # KB
        
        # Resize if needed
        if img.width > max_width:
            ratio = max_width / img.width
            new_height = int(img.height * ratio)
            img = img.resize((max_width, new_height), Image.LANCZOS)
        
        # Convert to RGB if necessary (WebP doesn't support all modes)
        if img.mode in ('RGBA', 'LA'):
            # Create white background for transparency
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
        
        # Get compressed size
        compressed_size = os.path.getsize(output_path) / 1024  # KB
        reduction = ((original_size - compressed_size) / original_size) * 100
        
        print(f"✓ {output_path.name:30s} {original_size:6.1f}KB → {compressed_size:6.1f}KB ({reduction:5.1f}% reduction)")
        
    except Exception as e:
        print(f"✗ {input_path.name}: {e}")

def main():
    """Main compression function"""
    # Define paths
    input_dir = Path("Data/images")
    output_dir = Path("app/src/main/assets/images")
    
    # Check if input directory exists
    if not input_dir.exists():
        print(f"❌ Input directory not found: {input_dir}")
        print("\nPlease create 'Data/images/' and add your images there.")
        print("Supported formats: JPG, JPEG, PNG, WebP")
        return
    
    # Create output directory
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Supported image formats
    image_extensions = {'.jpg', '.jpeg', '.png', '.webp', '.bmp', '.gif'}
    
    # Find all images
    image_files = [
        f for f in input_dir.iterdir()
        if f.is_file() and f.suffix.lower() in image_extensions
    ]
    
    if not image_files:
        print(f"❌ No images found in {input_dir}")
        print(f"\nSupported formats: {', '.join(image_extensions)}")
        return
    
    print(f"Found {len(image_files)} images to compress\n")
    print(f"{'Filename':<30s} {'Original':>10s} {'Compressed':>10s} {'Reduction':>10s}")
    print("-" * 70)
    
    # Process each image
    total_original = 0
    total_compressed = 0
    
    for img_file in sorted(image_files):
        output_file = output_dir / f"{img_file.stem}.webp"
        
        original_size = os.path.getsize(img_file) / 1024
        total_original += original_size
        
        compress_image(img_file, output_file)
        
        if output_file.exists():
            compressed_size = os.path.getsize(output_file) / 1024
            total_compressed += compressed_size
    
    # Summary
    print("-" * 70)
    total_reduction = ((total_original - total_compressed) / total_original) * 100
    print(f"\n{'Total:':<30s} {total_original:6.1f}KB → {total_compressed:6.1f}KB ({total_reduction:5.1f}% reduction)")
    print(f"\n✓ Compressed {len(image_files)} images")
    print(f"✓ Saved to {output_dir}")
    print(f"\nNext steps:")
    print("1. Build the app")
    print("2. Images will load from assets (offline)")
    print("3. Test in Airplane Mode to verify")

if __name__ == "__main__":
    main()
