#!/bin/bash

# Script to auto-increment version for Android app

VERSION_FILE="version.properties"

if [ ! -f "$VERSION_FILE" ]; then
    echo "Error: $VERSION_FILE not found!"
    exit 1
fi

# Read current version
CURRENT_VERSION_CODE=$(grep "versionCode=" $VERSION_FILE | cut -d'=' -f2)
CURRENT_VERSION_NAME=$(grep "versionName=" $VERSION_FILE | cut -d'=' -f2)

# Increment version code
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

# Increment version name (patch version)
IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION_NAME"
MAJOR=${VERSION_PARTS[0]}
MINOR=${VERSION_PARTS[1]}
PATCH=${VERSION_PARTS[2]:-0}

NEW_PATCH=$((PATCH + 1))
NEW_VERSION_NAME="$MAJOR.$MINOR.$NEW_PATCH"

# Update version.properties
echo "versionCode=$NEW_VERSION_CODE" > $VERSION_FILE
echo "versionName=$NEW_VERSION_NAME" >> $VERSION_FILE

echo "Version updated:"
echo "  versionCode: $CURRENT_VERSION_CODE -> $NEW_VERSION_CODE"
echo "  versionName: $CURRENT_VERSION_NAME -> $NEW_VERSION_NAME"
