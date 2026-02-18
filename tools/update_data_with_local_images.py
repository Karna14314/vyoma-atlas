#!/usr/bin/env python3
"""
Update initial_data.json to use local image paths where available
"""

import json
from pathlib import Path

def main():
    """Update JSON data with local image paths"""
    
    # Paths
    data_file = Path("app/src/main/assets/initial_data.json")
    images_dir = Path("app/src/main/assets/images")
    
    if not data_file.exists():
        print(f"❌ Data file not found: {data_file}")
        return
    
    if not images_dir.exists():
        print(f"❌ Images directory not found: {images_dir}")
        return
    
    # Get list of available images
    available_images = {f.stem for f in images_dir.glob("*.webp")}
    
    print(f"✓ Found {len(available_images)} local images")
    
    # Load data
    with open(data_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Update image URLs
    updated_count = 0
    for obj in data:
        obj_id = obj['id']
        if obj_id in available_images:
            # Use local path (will be resolved by ImageLoader)
            old_url = obj.get('imageUrl', 'none')
            obj['imageUrl'] = f"images/{obj_id}.webp"
            if old_url != f"images/{obj_id}.webp":
                print(f"  ✓ {obj_id}: Updated to local image")
                updated_count += 1
    
    # Save updated data
    with open(data_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    
    print(f"\n✅ Updated {updated_count} objects with local image paths")
    print(f"📁 Saved to {data_file}")

if __name__ == "__main__":
    main()
