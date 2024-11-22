@file:Suppress("DEPRECATION")

package com.example.myplaceszadanie

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    navController: NavController,
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }


    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }


    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

        // 50.294421895739013  57.1554396674037 ekeb кординаты
    val initialPosition = remember { LatLng(50.294421895739013, 57.1554396674037) }
    var cameraPosition by remember {
        mutableStateOf(CameraPosition.Builder()
            .target(currentLocation ?: initialPosition)
            .zoom(15f)
            .build()
        )}

    val mapProperties = MapProperties(
        isMyLocationEnabled = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выберите место") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        selectedLocation?.let {
                            onLocationSelected(it.latitude, it.longitude)
                            navController.navigateUp()
                        }
                    },
                    enabled = selectedLocation != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Сохранить")
                    Text("Сохранить")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = mapProperties,
                cameraPositionState = rememberCameraPositionState {
                    position = cameraPosition
                },
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15f)
                        .build()
                }
            ) {

                currentLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Моя позиция"
                    )
                }


                selectedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Выбранное место"
                    )
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 50.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "🏁",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
    }
}