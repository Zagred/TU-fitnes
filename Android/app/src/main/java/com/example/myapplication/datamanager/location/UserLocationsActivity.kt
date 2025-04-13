package com.example.myapplication.location

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserLocationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationAdapter
    private lateinit var btnSetMyLocation: Button
    private var userId: Int = -1
    private val locations = mutableListOf<Location>()

    // Druzhba Sofia coordinates (default starting point)
    private val druzhbaLatitude = 42.666157
    private val druzhbaLongitude = 23.388249


    private var useCustomStartLocation = false

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

        // Set up RecyclerView
        adapter = LocationAdapter(locations, false) { location ->
            // Navigate to the selected location
            if (useCustomStartLocation) {
                navigateToLocation(
                    druzhbaLatitude, druzhbaLongitude,
                    location.latitude, location.longitude
                )
            } else {
                // Just open the destination without directions
                openGoogleMapsLocation(location.latitude, location.longitude)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Set up the "Set My Starting Location" button
        btnSetMyLocation.setOnClickListener {
            useCustomStartLocation = true
            Toast.makeText(this, "Starting location set to Druzhba Sofia", Toast.LENGTH_SHORT).show()
        }

        // Load locations
        loadLocations()
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
        // Open Google Maps with directions from start location to destination
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1" +
                "&origin=$startLat,$startLng" +
                "&destination=$destLat,$destLng" +
                "&travelmode=driving")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Google Maps app is not installed, open in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        }
    }

    private fun openGoogleMapsLocation(lat: Double, lng: Double) {
        // Just open the location in Google Maps without directions
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Google Maps app is not installed, open in browser
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            startActivity(browserIntent)
        }
    }
}