package com.karnadigital.vyoma.atlas.ui.skymap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karnadigital.vyoma.atlas.core.location.LocationRepository
import com.karnadigital.vyoma.atlas.core.math.LatLong
import com.karnadigital.vyoma.atlas.core.math.Matrix3x3
import com.karnadigital.vyoma.atlas.core.sensors.OrientationSensor
import com.karnadigital.vyoma.atlas.core.sensors.PointingCalculator
import com.karnadigital.vyoma.atlas.data.repository.AstronomyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SkyMapViewModel @Inject constructor(
    private val repository: AstronomyRepository,
    private val orientationSensor: OrientationSensor,
    private val locationRepository: LocationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // Default fallback location (London)
    private val defaultLocation = LatLong(51.5074f, -0.1278f) 
    
    private val _targetId = MutableStateFlow<String?>(savedStateHandle["targetId"])
    
    private val _state = MutableStateFlow(SkyMapState())
    val state: StateFlow<SkyMapState> = _state.asStateFlow()
    
    private val _cachedObjects = repository.getAllObjects()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _screenSize = MutableStateFlow(androidx.compose.ui.geometry.Size(1080f, 2400f))

    private val pointingCalculator = PointingCalculator()
    
    // Location Flow
    private val _currentLocation = locationRepository.getCurrentLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            combine(
                orientationSensor.getSensorUpdates(),
                _cachedObjects,
                _screenSize,
                _targetId,
                _currentLocation
            ) { rotationMatrix, objects, screenSize, targetId, location ->
                try {
                    if (screenSize.width <= 0f || screenSize.height <= 0f) {
                        return@combine SkyMapState()
                    }

                    // Use real location or fallback
                    val userLocation = location ?: defaultLocation

                    val now = Date()
                    val phoneToCelestial = pointingCalculator.calculatePointingMatrix(
                        rotationMatrix, userLocation, now
                    )

                    val pointing = Matrix3x3(
                        phoneToCelestial.xx, phoneToCelestial.yx, phoneToCelestial.zx,
                        phoneToCelestial.xy, phoneToCelestial.yy, phoneToCelestial.zy,
                        phoneToCelestial.xz, phoneToCelestial.yz, phoneToCelestial.zz
                    )

                    // 1. All visible objects (Stars, Planets, Nebulae, Galaxies)
                    val visiblePoints = objects
                        .filter { (it.magnitude ?: 10.0) < 7.0 } // Increased from 6.0 to show more objects
                        .mapNotNull { obj ->
                            SkyMapProjector.project(obj, pointing, screenSize, 45f)
                        }

                    // 2. Compass
                    val horizon = pointingCalculator.getHorizonVectors(now, userLocation)
                    val compassPoints = listOf(
                        SkyMapProjector.projectCompassPoint(horizon.north, "N", pointing, screenSize, 45f),
                        SkyMapProjector.projectCompassPoint(horizon.east, "E", pointing, screenSize, 45f),
                        SkyMapProjector.projectCompassPoint(horizon.south, "S", pointing, screenSize, 45f),
                        SkyMapProjector.projectCompassPoint(horizon.west, "W", pointing, screenSize, 45f)
                    ).filterNotNull()

                    // 3. Target
                    var targetIndicator: TargetIndicator? = null
                    if (targetId != null) {
                        val targetObj = objects.find { it.id == targetId }
                        if (targetObj != null) {
                            targetIndicator = SkyMapProjector.calculateTargetIndicator(
                                targetObj, pointing, screenSize, 45f
                            )
                        }
                    }

                    SkyMapState(
                        points = visiblePoints,
                        compassPoints = compassPoints,
                        target = targetIndicator
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    SkyMapState()
                }
            }
            .flowOn(kotlinx.coroutines.Dispatchers.Default)
            .collect { newState ->
                _state.value = newState
            }
        }
    }

    fun updateScreenSize(width: Float, height: Float) {
        _screenSize.value = androidx.compose.ui.geometry.Size(width, height)
    }
    
    fun setTarget(id: String?) {
        _targetId.value = id
    }
}
