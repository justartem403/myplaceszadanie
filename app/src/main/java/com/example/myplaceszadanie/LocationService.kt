package com.example.myplaceszadanie

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    sealed class Error : LocationResult() {
        object PermissionDenied : Error()
        object LocationDisabled : Error()
        data class Exception(val message: String) : Error()
    }
}

class LocationService(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    suspend fun getCurrentLocation(): LocationResult = withContext(Dispatchers.IO) {
        try {
            Log.d("LocationService", "Getting current location...")

            if (!hasLocationPermission()) {
                Log.d("LocationService", "Location permission denied")
                return@withContext LocationResult.Error.PermissionDenied
            }

            if (!isLocationEnabled()) {
                Log.d("LocationService", "Location is disabled")
                return@withContext LocationResult.Error.LocationDisabled
            }

            try {
                @Suppress("MissingPermission")
                val location = Tasks.await(fusedLocationClient.lastLocation)

                return@withContext if (location != null) {
                    Log.d("LocationService", "Location obtained: ${location.latitude}, ${location.longitude}")
                    LocationResult.Success(location)
                } else {
                    Log.d("LocationService", "Location is null, requesting new location")
                    requestNewLocation()
                }
            } catch (e: Exception) {
                Log.e("LocationService", "Error getting location: ${e.message}")
                return@withContext LocationResult.Error.Exception(
                    e.message ?: "Неизвестная ошибка получения местоположения"
                )
            }
        } catch (e: SecurityException) {
            Log.e("LocationService", "Security exception: ${e.message}")
            return@withContext LocationResult.Error.PermissionDenied
        } catch (e: Exception) {
            Log.e("LocationService", "General exception: ${e.message}")
            return@withContext LocationResult.Error.Exception(
                e.message ?: "Неизвестная ошибка получения местоположения"
            )
        }
    }

    private fun requestNewLocation(): LocationResult {
        return try {

            Log.d("LocationService", "Requesting new location")
            LocationResult.Error.LocationDisabled
        } catch (e: Exception) {
            Log.e("LocationService", "Error requesting new location: ${e.message}")
            LocationResult.Error.Exception(
                e.message ?: "Ошибка при запросе нового местоположения"
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    private fun isLocationEnabled(): Boolean {
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.e("LocationService", "Error checking location enabled: ${e.message}")
            false
        }
    }
}