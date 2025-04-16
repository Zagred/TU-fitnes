package com.example.myapplication.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.HomePage
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.location.Location as LocationEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserLocationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationAdapter
    private lateinit var btnSetMyLocation: Button
    private var userId: Int = -1
    private val locations = mutableListOf<LocationEntity>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var locationPermissionGranted = false
    private var useCurrentLocation = false

    // Fallback coordinates (Druzhba Sofia) in case location access is denied
    private val druzhbaLatitude = 42.666157
    private val druzhbaLongitude = 23.388249

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_locations)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.locationsRecyclerView)
        btnSetMyLocation = findViewById(R.id.btnSetMyLocation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        adapter = LocationAdapter(locations, false) { location ->
            if (useCurrentLocation && locationPermissionGranted) {
                navigateToLocation(
                    currentLatitude, currentLongitude,
                    location.latitude, location.longitude
                )
            } else {
                openGoogleMapsLocation(location.latitude, location.longitude)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnSetMyLocation.setOnClickListener {
            if (locationPermissionGranted) {
                getCurrentLocation()
                useCurrentLocation = true
                Toast.makeText(this, "Using your current location as starting point", Toast.LENGTH_SHORT).show()
            } else {
                requestLocationPermission()
            }
        }

        // Load locations
        loadLocations()
        val home = findViewById<Button>(R.id.btHome)
        home.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            getCurrentLocation()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
                getCurrentLocation()
                useCurrentLocation = true
                Toast.makeText(this, "Using your current location as starting point", Toast.LENGTH_SHORT).show()
            } else {
                locationPermissionGranted = false
                Toast.makeText(this, "Location permission denied. Using default location.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val cancellationToken = CancellationTokenSource().token

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                    useCurrentLocation = true
                } ?: run {
                    Toast.makeText(this, "Could not get current location. Using default.", Toast.LENGTH_SHORT).show()
                    currentLatitude = druzhbaLatitude
                    currentLongitude = druzhbaLongitude
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Location error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                currentLatitude = druzhbaLatitude
                currentLongitude = druzhbaLongitude
            }
    }

    private fun loadLocations() {
        val locationDAO = AppDatabase.getInstance(applicationContext).locationDAO()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val locationList = locationDAO.getAll()

                withContext(Dispatchers.Main) {
                    locations.clear()
                    locations.addAll(locationList)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UserLocationsActivity,
                        "Error loading locations: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToLocation(
        startLat: Double, startLng: Double,
        destLat: Double, destLng: Double
    ) {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1" +
                "&origin=$startLat,$startLng" +
                "&destination=$destLat,$destLng" +
                "&travelmode=driving")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        }
    }

    private fun openGoogleMapsLocation(lat: Double, lng: Double) {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            startActivity(browserIntent)
        }
    }
}