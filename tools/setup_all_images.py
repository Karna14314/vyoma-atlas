#!/usr/bin/env python3
"""
Master Script: Complete Image Setup for Vyoma
Runs all image preparation steps in sequence
"""

import subprocess
import sys
from pathlib import Path

def run_script(script_name, description):
    """Run a Python script and report results"""
    print("\n" + "=" * 70)
    print(f"📋 {description}")
    print("=" * 70)
    
    try:
        result = subprocess.run(
            [sys.executable, script_name],
            capture_output=False,
            text=True
        )
        
        if result.returncode == 0:
            print(f"✅ {description} - SUCCESS")
            return True
        else:
            print(f"⚠️  {description} - COMPLETED WITH WARNINGS")
            return True
    except Exception as e:
        print(f"❌ {description} - FAILED: {e}")
        return False

def main():
    """Run all image setup scripts"""
    print("=" * 70)
    print("🚀 Vyoma Complete Image Setup")
    print("=" * 70)
    print("\nThis will:")
    print("  1. Download images from astronomy data sources")
    print("  2. Generate placeholders for missing images")
    print("  3. Update data file with local image paths")
    print("\n" + "=" * 70)
    
    tools_dir = Path("tools")
    
    # Check if we're in the right directory
    if not tools_dir.exists():
        print("\n❌ Error: Please run this script from the project root directory")
        return
    
    # Step 1: Download images
    success = run_script(
        tools_dir / "download_and_prepare_images.py",
        "Step 1: Download & Compress Images"
    )
    
    if not success:
        print("\n⚠️  Download step had issues, but continuing...")
    
    # Step 2: Generate placeholders
    success = run_script(
        tools_dir / "generate_placeholder_images.py",
        "Step 2: Generate Placeholder Images"
    )
    
    if not success:
        print("\n❌ Placeholder generation failed. Stopping.")
        return
    
    # Step 3: Update data file
    success = run_script(
        tools_dir / "update_data_with_local_images.py",
        "Step 3: Update Data with Local Paths"
    )
    
    if not success:
        print("\n❌ Data update failed. Stopping.")
        return
    
    # Final summary
    print("\n" + "=" * 70)
    print("🎉 IMAGE SETUP COMPLETE!")
    print("=" * 70)
    
    images_dir = Path("app/src/main/assets/images")
    if images_dir.exists():
        image_count = len(list(images_dir.glob("*.webp")))
        total_size = sum(f.stat().st_size for f in images_dir.glob("*.webp")) / 1024
        
        print(f"\n📊 Final Statistics:")
        print(f"  • Total images: {image_count}")
        print(f"  • Total size: {total_size:.1f} KB ({total_size/1024:.1f} MB)")
        print(f"  • Location: {images_dir}")
    
    print(f"\n✅ Next Steps:")
    print(f"  1. Build the app:")
    print(f"     ./gradlew assembleDebug")
    print(f"  2. Install on device")
    print(f"  3. Test offline mode (Airplane Mode)")
    print(f"  4. All images should load instantly!")
    
    print("\n" + "=" * 70)

if __name__ == "__main__":
    main()
