package com.example.sesion7gps

import android.R
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.sesion7gps.ui.theme.Sesion7GPSTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {

    lateinit var locationClient : FusedLocationProviderClient
    val locationRequest = createLocationRequest()
    lateinit var locationCallback : LocationCallback

    val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback{
            if(it ){
                //Hay Permiso
                startLocationUpdates()
            }else{
                //No hay permiso
            }
        }
    )

    fun startLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    fun stopLocationUpdates(){
        locationClient.removeLocationUpdates(locationCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        locationClient =  LocationServices.getFusedLocationProviderClient(this)
        val viewModel = LocationViewModel()
        locationCallback = createLocationCallback(viewModel)
        //askpermission
        setContent {
            GPSDataUpdates(viewModel)
        }
    }

    @Composable
    fun GPSDataUpdates(viewModel: LocationViewModel){
        val state = viewModel.state.collectAsState()
        var active by remember { mutableStateOf(true) }
        Column(modifier = Modifier.padding(16.dp)){
            MyRow("Latitude:", state.value.latitude.toString())
            MyRow("Longitude:", state.value.longitude.toString())

            if(active) {
                Button(onClick = {
                    stopLocationUpdates()
                    active = false
                }) { Text("Stop Location Updates", color = Color.Red ) }
            }else{
                Button(onClick = {
                    startLocationUpdates()
                    active = true
                }) { Text("Start Location Updates", color = Color.Green) }
            }
        }
    }

    fun createLocationCallback(viewModel: LocationViewModel) : LocationCallback {
        val callback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val location = result.lastLocation
                location?.let{
                    Log.i("LocationApp", location.latitude.toString())
                    Log.i("LocationApp", location.longitude.toString())
                    viewModel.update(location.latitude, location.longitude)
                }
            }
        }
        return callback
    }

    fun createLocationRequest() : LocationRequest {
        val request = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .build()
        return request
    }

    @Composable
    fun MyRow(label : String, value : String){
        Row(modifier = Modifier.padding(10.dp)){
            Text(label, color = Color.Blue, fontSize = 25.sp)
            Text(value,  fontSize = 25.sp)
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        Sesion7GPSTheme {
           // GPSDataUpdates()
        }
    }
}
//
//@Composable
//fun GPSData(){
//    val context = LocalContext.current
//    var latitude by remember { mutableStateOf(0.0) }
//    var longitude by remember { mutableStateOf(0.0) }
//    var altitude by remember { mutableStateOf(0.0) }
//    val locationClient = LocationServices.getFusedLocationProviderClient(context)
//    if(ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
//        == PackageManager.PERMISSION_GRANTED){
//        locationClient.lastLocation.addOnSuccessListener {
//            location ->
//            location?.let{
//                latitude = location.latitude
//                longitude = location.longitude
//                altitude = location.altitude
//            }
//        }
//    }
//
//    Column(modifier = Modifier.padding(16.dp)){
//        MyRow("Latitude:", latitude.toString())
//        MyRow("Longitude:", longitude.toString())
//        MyRow("Altitude:", altitude.toString())
//    }
//}

data class LocationState(val latitude : Double =0.0, val longitude : Double =0.0)
class LocationViewModel : ViewModel(){
    private val _uiState = MutableStateFlow(LocationState())
    val state : StateFlow<LocationState> = _uiState
    fun update(lat : Double, long : Double){
        _uiState.update { it.copy(lat, long) }
    }
}