package com.example.myapplication.location

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class AdminLocationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationAdapter
    private lateinit var addLocationButton: Button
    private var userId: Int = -1
    private val locations = mutableListOf<Location>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_locations)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.locationsRecyclerView)
        addLocationButton = findViewById(R.id.addLocationButton)

        // Set up RecyclerView
        adapter = LocationAdapter(locations, true) { location ->
            // Edit location when clicked
            showLocationDialog(location)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Add new location button
        addLocationButton.setOnClickListener {
            showLocationDialog()
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
                        this@AdminLocationsActivity,
                        "Error loading locations: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showLocationDialog(existingLocation: Location? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_location, null)

        // Get references to dialog fields
        val nameField = dialogView.findViewById<EditText>(R.id.locationNameEditText)
        val latitudeField = dialogView.findViewById<EditText>(R.id.latitudeEditText)
        val longitudeField = dialogView.findViewById<EditText>(R.id.longitudeEditText)
        val addressField = dialogView.findViewById<EditText>(R.id.addressEditText)
        val descriptionField = dialogView.findViewById<EditText>(R.id.descriptionEditText)

        // If editing an existing location, populate the fields
        existingLocation?.let {
            nameField.setText(it.name)
            latitudeField.setText(it.latitude.toString())
            longitudeField.setText(it.longitude.toString())
            addressField.setText(it.address)
            descriptionField.setText(it.description)
        }

        // Create and show the dialog
        val dialogTitle = if (existingLocation == null) "Add New Location" else "Edit Location"

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                try {
                    val name = nameField.text.toString().trim()
                    val latitude = latitudeField.text.toString().toDouble()
                    val longitude = longitudeField.text.toString().toDouble()
                    val address = addressField.text.toString().trim()
                    val description = descriptionField.text.toString().trim()

                    if (name.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "Name and address are required", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    saveLocation(existingLocation, name, latitude, longitude, address, description)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid latitude or longitude format", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .apply {
                // Add delete button if editing an existing location
                existingLocation?.let {
                    this.setNeutralButton("Delete") { _, _ ->
                        deleteLocation(it)
                    }
                }
            }
            .show()
    }

    private fun saveLocation(
        existingLocation: Location?,
        name: String,
        latitude: Double,
        longitude: Double,
        address: String,
        description: String
    ) {
        val locationDAO = AppDatabase.getInstance(applicationContext).locationDAO()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (existingLocation == null) {
                    // Create new location
                    val newLocation = Location(
                        name = name,
                        latitude = latitude,
                        longitude = longitude,
                        address = address,
                        description = description,
                        addedBy = userId
                    )
                    locationDAO.insert(newLocation)
                } else {
                    // Update existing location
                    val updatedLocation = existingLocation.copy(
                        name = name,
                        latitude = latitude,
                        longitude = longitude,
                        address = address,
                        description = description
                    )
                    locationDAO.update(updatedLocation)
                }

                // Reload the locations
                val updatedLocations = locationDAO.getAll()

                withContext(Dispatchers.Main) {
                    locations.clear()
                    locations.addAll(updatedLocations)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(
                        this@AdminLocationsActivity,
                        "Location saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AdminLocationsActivity,
                        "Error saving location: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun deleteLocation(location: Location) {
        val locationDAO = AppDatabase.getInstance(applicationContext).locationDAO()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                locationDAO.delete(location)

                // Reload the locations
                val updatedLocations = locationDAO.getAll()

                withContext(Dispatchers.Main) {
                    locations.clear()
                    locations.addAll(updatedLocations)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(
                        this@AdminLocationsActivity,
                        "Location deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AdminLocationsActivity,
                        "Error deleting location: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}