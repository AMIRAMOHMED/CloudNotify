package com.example.cloudnotify.ui.fragment
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.cloudnotify.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay

class MapFragment : Fragment(), LocationListener {

    private lateinit var mapView: MapView
    private var currentLocationMarker: Marker? = null
    private var clickedLocationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the library
        Configuration.getInstance().load(requireActivity(), PreferenceManager.getDefaultSharedPreferences(requireActivity()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapview)

        // Initialize the map
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Default location (e.g., Alexandria)
        val startPoint = GeoPoint(31.2001, 29.9187)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(startPoint)

        // Get current location and add marker
        getCurrentLocation()

        // Set up map click listener
        setupMapClickListener()
    }

    private fun setupMapClickListener() {
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                // Handle the map click and add a marker
                addClickedLocationMarker(p)
                showLocationBottomSheet(p)

                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                // Handle long press if needed
                return false
            }
            private fun showLocationBottomSheet(point: GeoPoint) {
                val locationDetails = "Lat: ${point.latitude}, Lon: ${point.longitude}"

                // Create and show the bottom sheet
                val bottomSheetFragment = LocationBottomSheetFragment.newInstance(locationDetails)
                bottomSheetFragment.show(parentFragmentManager, "LocationBottomSheet")
            }
        }


        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)
    }

    private fun addClickedLocationMarker(point: GeoPoint) {
        // Remove the previous clicked location marker if it exists
        clickedLocationMarker?.let {
            mapView.overlays.remove(it)
        }

        // Add a new marker for the clicked location with a different icon
        clickedLocationMarker = Marker(mapView).apply {
            position = point
            icon = resources.getDrawable(R.drawable.add_location) // Your clicked location marker icon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView.overlays.add(clickedLocationMarker)
        mapView.invalidate()
    }

    private fun getCurrentLocation() {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onLocationChanged(location: Location) {
        val currentPoint = GeoPoint(location.latitude, location.longitude)

        // Remove the previous location marker if it exists
        currentLocationMarker?.let {
            mapView.overlays.remove(it)
        }

        // Add a marker for the current location
        currentLocationMarker = Marker(mapView).apply {
            position = currentPoint
            icon = resources.getDrawable(R.drawable.location_on) // Your current location marker icon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView.overlays.add(currentLocationMarker)
        mapView.controller.setCenter(currentPoint)
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()  // Enable map view
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Disable map view
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()  // Clean up map view
    }
}