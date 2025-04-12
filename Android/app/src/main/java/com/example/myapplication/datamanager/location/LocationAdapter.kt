package com.example.myapplication.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.location.Location

class LocationAdapter(
    private val locations: List<Location>,
    private val isAdminView: Boolean,
    private val onItemClick: (Location) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.locationName)
        val addressText: TextView = view.findViewById(R.id.locationAddress)
        val descriptionText: TextView = view.findViewById(R.id.locationDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]

        holder.nameText.text = location.name
        holder.addressText.text = location.address

        // Show description if available
        if (location.description.isNotEmpty()) {
            holder.descriptionText.visibility = View.VISIBLE
            holder.descriptionText.text = location.description
        } else {
            holder.descriptionText.visibility = View.GONE
        }

        // Set item click listener
        holder.itemView.setOnClickListener {
            onItemClick(location)
        }
    }

    override fun getItemCount() = locations.size
}