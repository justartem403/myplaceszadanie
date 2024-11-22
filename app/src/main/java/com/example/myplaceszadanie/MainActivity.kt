package com.example.myplaceszadanie

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myplaceszadanie.ui.theme.MyPlacesZadanieTheme

class MainActivity : ComponentActivity() {
    private val locationService by lazy { LocationService(this) }

    private val placeViewModel: PlaceViewModel by viewModels {
        PlaceViewModel.PlaceViewModelFactory(
            (application as MyPlacesApplication).database.placeDao(),
            locationService
        )
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

                placeViewModel.onPermissionsGranted()
                Log.d("MainActivity", "Location permissions granted")
            }
            else -> {

                Toast.makeText(
                    this,
                    "Для работы приложения необходим доступ к местоположению",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("MainActivity", "Location permissions denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")


        requestLocationPermissions()

        setContent {
            MyPlacesZadanieTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlacesApp(placeViewModel)
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        try {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "Error requesting permissions: ${e.message}")
            Toast.makeText(
                this,
                "Ошибка при запросе разрешений",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyPlacesZadanieTheme {
        Greeting("Android")
    }
}