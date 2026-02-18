package com.karnadigital.vyoma.atlas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject
import com.karnadigital.vyoma.atlas.data.repository.AstronomyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AstronomyRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _allObjects = repository.getAllObjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val objects: StateFlow<List<AstronomicalObject>> = combine(_allObjects, _selectedCategory) { list, category ->
        when (category) {
            "All" -> list
            "Planets" -> list.filter { it.type == "PLANET" }
            "Stars" -> list.filter { it.type == "STAR" }
            "Solar System" -> list.filter { it.type == "PLANET" || it.id == "sun" || it.type == "MOON" || it.parentId == "sun" }
            "Deep Sky" -> list.filter { it.type == "GALAXY" || it.type == "NEBULA" || it.type == "BLACK_HOLE" }
            "Galaxies" -> list.filter { it.type == "GALAXY" }
            "Nebulae" -> list.filter { it.type == "NEBULA" }
            "Black Holes" -> list.filter { it.type == "BLACK_HOLE" }
            else -> list.filter { it.type == category.uppercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterByCategory(category: String) {
        _selectedCategory.value = category
    }
}
