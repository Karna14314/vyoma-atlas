# 🌌 Vyoma Atlas - Astronomy Explorer

**An immersive AR astronomy app for Android**

Explore the cosmos with real-time augmented reality sky mapping, comprehensive astronomical data, and beautiful imagery - all in your pocket.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ Features

### 🔭 AR Sky Mapping
- Real-time augmented reality view of the night sky
- Sensor-based orientation tracking
- Compass overlay for navigation
- Target indicator for selected objects
- Stable at all phone orientations

### 📚 Comprehensive Database
- **61 astronomical objects** across 6 categories
- Solar System: Planets, moons, dwarf planets
- Stars: Brightest stars visible from Earth
- Constellations: Major constellations
- Deep Sky: Nebulae and galaxies
- Exoplanets: Notable exoplanet systems
- Small Bodies: Asteroids and comets

### 🖼️ Image Gallery
- Multiple images per object
- 21 offline images for instant loading
- Beautiful gradient placeholders
- Automatic fallback system
- Works completely offline

### 🎨 Modern UI
- Material Design 3
- Dark space-themed gradients
- Smooth animations
- Intuitive navigation
- Professional design

---

## 📱 Screenshots

*Coming soon*

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 35
- Kotlin 1.9.0+
- Gradle 8.7+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/vyoma-atlas.git
cd vyoma-atlas
```

2. **Open in Android Studio**
- File → Open → Select project folder
- Wait for Gradle sync

3. **Run the app**
- Connect Android device or start emulator
- Click Run (▶️) or press Shift+F10

### Building Release AAB

```bash
./gradlew bundleRelease
```

The AAB will be generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 🏗️ Project Structure

```
vyoma-atlas/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/karnadigital/vyoma/atlas/
│   │   │   │   ├── core/          # Core utilities
│   │   │   │   ├── data/          # Data layer
│   │   │   │   ├── di/            # Dependency injection
│   │   │   │   └── ui/            # UI layer
│   │   │   ├── assets/            # App assets
│   │   │   │   ├── images/        # Offline images
│   │   │   │   ├── astronomy_objects.json
│   │   │   │   ├── image_gallery.json
│   │   │   │   └── categories.json
│   │   │   └── res/               # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── tools/                          # Python scripts
│   ├── comprehensive_data_migration.py
│   ├── download_and_prepare_images.py
│   ├── generate_placeholder_images.py
│   └── setup_all_images.py
├── docs/                           # Documentation
├── release/                        # Release builds
└── README.md
```

---

## 🛠️ Tech Stack

### Core
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt

### Libraries
- **Room**: Local database
- **Coil**: Image loading
- **Coroutines**: Async operations
- **Material 3**: UI components
- **Sensors**: AR tracking

---

## 📊 Data Sources

### Astronomical Data
- [NASA Science](https://science.nasa.gov) - Planetary data
- [NASA Photojournal](https://photojournal.jpl.nasa.gov) - Images
- [Wikimedia Commons](https://commons.wikimedia.org) - Public domain images
- [IAU](https://www.iau.org) - Constellation definitions
- [ESA](https://www.esa.int) - European Space Agency data

### Image Credits
- Real planet images: NASA/JPL/Wikimedia (Public Domain)
- Placeholder images: Generated (Gradient + Initials)

---

## 🔧 Configuration

### API Keys
No API keys required! All data is bundled with the app.

### Permissions
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 📝 Development

### Adding New Objects

1. **Update data file**
```bash
python tools/comprehensive_data_migration.py
```

2. **Download images**
```bash
python tools/download_all_gallery_images.py
```

3. **Generate placeholders**
```bash
python tools/generate_placeholder_images.py
```

### Running Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 🐛 Known Issues

- Planets show approximate positions (ephemeris not implemented)
- Some images are placeholders
- Constellation lines not yet drawn
- No search functionality yet

See [Issues](https://github.com/yourusername/vyoma-atlas/issues) for full list.

---

## 🗺️ Roadmap

### v1.1 (Planned)
- [ ] Real-time planet positions (ephemeris)
- [ ] Search and filter objects
- [ ] Favorites system
- [ ] Constellation line drawing

### v1.2 (Planned)
- [ ] Time travel feature
- [ ] More objects (200+ total)
- [ ] AR labels and info overlays
- [ ] Night mode improvements

### v2.0 (Future)
- [ ] Social features
- [ ] Observation logging
- [ ] Sky event notifications
- [ ] Telescope control integration

---

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) first.

### How to Contribute
1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📄 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file.

### Data Licenses
- NASA data: Public Domain
- ESA data: Check individual licenses
- Wikimedia images: CC-BY-SA or Public Domain

---

## 👥 Authors

- **Your Name** - *Initial work* - [YourGitHub](https://github.com/yourusername)

See [CONTRIBUTORS.md](CONTRIBUTORS.md) for full list.

---

## 🙏 Acknowledgments

- NASA for amazing planetary data and images
- ESA for European space mission data
- Wikimedia Commons for public domain images
- Android community for excellent libraries
- All contributors and testers

---

## 📞 Contact

- **Email**: your.email@example.com
- **GitHub**: [@yourusername](https://github.com/yourusername)
- **Issues**: [GitHub Issues](https://github.com/yourusername/vyoma-atlas/issues)

---

## 🌟 Star History

[![Star History Chart](https://api.star-history.com/svg?repos=yourusername/vyoma-atlas&type=Date)](https://star-history.com/#yourusername/vyoma-atlas&Date)

---

**Made with ❤️ and ☕ by astronomy enthusiasts**

*Explore the cosmos, one star at a time* ✨
