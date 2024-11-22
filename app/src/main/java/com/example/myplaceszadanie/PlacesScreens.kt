package com.example.myplaceszadanie

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun PlacesApp(viewModel: PlaceViewModel) {
    val navController = rememberNavController()
    var locationName by remember { mutableStateOf("") }

    NavHost(navController = navController, startDestination = "placesList") {
        composable("placesList") {
            PlacesListScreen(
                viewModel = viewModel,
                navController = navController,
                locationName = locationName,
                onLocationNameChange = { locationName = it }
            )
        }
        composable("placesMap") {
            PlacesMapScreen(viewModel)
        }
        composable("mapPicker") {
            MapPickerScreen(
                navController = navController,
                onLocationSelected = { lat, lon ->
                    viewModel.saveLocationWithCoordinates(locationName, lat, lon)
                    locationName = ""
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesListScreen(
    viewModel: PlaceViewModel,
    navController: NavController,
    locationName: String,
    onLocationNameChange: (String) -> Unit
) {
    val context = LocalContext.current
    val places by viewModel.places.collectAsState(initial = emptyList())
    val saveLocationState by viewModel.saveLocationState.collectAsState()

    LaunchedEffect(saveLocationState) {
        when (saveLocationState) {
            is SaveLocationState.Error -> {
                Toast.makeText(
                    context,
                    (saveLocationState as SaveLocationState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            is SaveLocationState.Success -> {
                Toast.makeText(context, "Место успешно сохранено", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои места") },
                actions = {
                    IconButton(onClick = {
                        Log.d("PlacesListScreen", "Navigate to map")
                        navController.navigate("placesMap")
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Map")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (locationName.isNotBlank()) {
                        Log.d("PlacesListScreen", "Navigating to map picker")
                        navController.navigate("mapPicker")
                    } else {
                        Toast.makeText(
                            context,
                            "Введите название места",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Place")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = locationName,
                onValueChange = onLocationNameChange,
                label = { Text("Название места") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyColumn {
                items(places) { place ->
                    PlaceItem(
                        place = place,
                        onDelete = {
                            Log.d("PlacesListScreen", "Deleting place: ${place.id}")
                            viewModel.deletePlace(place)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceItem(place: Place, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = place.name)
                Text(text = "Широта: ${place.latitude}")
                Text(text = "Долгота: ${place.longitude}")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@Composable
fun PlacesMapScreen(viewModel: PlaceViewModel) {
    val places by viewModel.places.collectAsState(initial = emptyList())
    val lastPlace = places.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Последнее место",
            modifier = Modifier.padding(16.dp)
        )

        lastPlace?.let {
            Text(
                text = "Название: ${it.name}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Широта: ${it.latitude}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Долгота: ${it.longitude}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } ?: Text(
            text = "Нет сохраненных мест",
            modifier = Modifier.padding(16.dp)
        )
    }
}