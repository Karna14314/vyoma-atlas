package com.karnadigital.vyoma.atlas.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject
import com.karnadigital.vyoma.atlas.data.repository.AstronomyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: AstronomyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val objectId: String = checkNotNull(savedStateHandle["objectId"])

    private val _astronomicalObject = MutableStateFlow<AstronomicalObject?>(null)
    val astronomicalObject: StateFlow<AstronomicalObject?> = _astronomicalObject.asStateFlow()

    private val _children = MutableStateFlow<List<AstronomicalObject>>(emptyList())
    val children: StateFlow<List<AstronomicalObject>> = _children.asStateFlow()

    init {
        Log.d("DetailViewModel", "Navigating to object with ID: $objectId")
        viewModelScope.launch {
            val obj = repository.getObjectById(objectId)
            Log.d("DetailViewModel", "Loaded object: ${obj?.name ?: "null"}")
            _astronomicalObject.value = obj
            
            if (obj != null) {
                repository.getObjectsByParentId(obj.id).collect {
                    Log.d("DetailViewModel", "Loaded ${it.size} children for ${obj.name}")
                    _children.value = it
                }
            }
        }
    }
}
