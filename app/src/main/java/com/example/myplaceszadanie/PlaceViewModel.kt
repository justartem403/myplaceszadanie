package com.example.myplaceszadanie

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SaveLocationState {
    object Idle : SaveLocationState()
    object Loading : SaveLocationState()
    object Success : SaveLocationState()
    data class Error(val message: String) : SaveLocationState()
}

class PlaceViewModel(
    private val placeDao: PlaceDao,
    private val locationService: LocationService
) : ViewModel() {
    val places: Flow<List<Place>> = placeDao.getAllPlaces()

    private val _saveLocationState = MutableStateFlow<SaveLocationState>(SaveLocationState.Idle)
    val saveLocationState: StateFlow<SaveLocationState> = _saveLocationState.asStateFlow()

    
    fun saveLocationWithCoordinates(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            Log.d("PlaceViewModel", "Saving location with name: $name, lat: $latitude, lon: $longitude")
            _saveLocationState.value = SaveLocationState.Loading

            try {
                val place = Place(
                    name = name,
                    latitude = latitude,
                    longitude = longitude
                )
                placeDao.insertPlace(place)
                _saveLocationState.value = SaveLocationState.Success
                Log.d("PlaceViewModel", "Location saved successfully")
            } catch (e: Exception) {
                Log.e("PlaceViewModel", "Error saving place: ${e.message}")
                _saveLocationState.value = SaveLocationState.Error("Ошибка сохранения места: ${e.message}")
            }
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            try {
                placeDao.deletePlace(place)
                Log.d("PlaceViewModel", "Place deleted successfully: ${place.id}")
            } catch (e: Exception) {
                Log.e("PlaceViewModel", "Error deleting place: ${e.message}")
            }
        }
    }

    fun onPermissionsGranted() {
        viewModelScope.launch {
            _saveLocationState.value = SaveLocationState.Idle
            Log.d("PlaceViewModel", "Permissions granted, state reset to Idle")
        }
    }

    class PlaceViewModelFactory(
        private val placeDao: PlaceDao,
        private val locationService: LocationService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlaceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlaceViewModel(placeDao, locationService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}