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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.cloudnotify.R
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.viewmodel.LocationViewModel
import com.example.cloudnotify.viewmodel.LocationViewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay



class MapFragment : Fragment(), LocationListener {

    private var mapView: MapView? = null
    private var currentLocationMarker: Marker? = null
    private var clickedLocationMarker: Marker? = null
    private lateinit var locationViewModel: LocationViewModel
    private var lastClickedLocation: GeoPoint? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the library
        Configuration.getInstance().load(
            requireActivity(),
            PreferenceManager.getDefaultSharedPreferences(requireActivity())
        )
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
        Log.d("MapFragment", "MapView initialized: $mapView")

        // Initialize the map
        mapView?.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView?.setBuiltInZoomControls(true)
        mapView?.setMultiTouchControls(true)

        // Default location (e.g., Alexandria)
        val startPoint = GeoPoint(31.2001, 29.9187)
        mapView?.controller?.setZoom(15.0)
        mapView?.controller?.setCenter(startPoint)

        // Initialize the location view model
            val  localViewModelFactory = LocationViewModelFactory(requireActivity().application)
        locationViewModel = localViewModelFactory.create(LocationViewModel::class.java)

        // Get current location and add marker
        getCurrentLocation()

        // Set up map click listener
        setupMapClickListener()
    }

    private fun setupMapClickListener() {
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                // Check if the clicked location is the same as the last one
                if (lastClickedLocation == null || p != lastClickedLocation) {
                    // Update last clicked location
                    lastClickedLocation = p
                    // Add a marker for the new location
                    addClickedLocationMarker(p)
                }

                // Always show the bottom sheet, even if it's the same location
                showLocationBottomSheet(p)

                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                // Handle long press if needed
                return false
            }

            private fun showLocationBottomSheet(point: GeoPoint) {
                val locationDetails = "Lat: ${point.latitude}, Lon: ${point.longitude}"

                // Save the clicked location details
                locationViewModel.updateLocation(point.latitude, point.longitude)
                locationViewModel.upDateSource(LocationSource.MAP)

                // Create and show the bottom sheet
                val bottomSheetFragment = LocationBottomSheetFragment.newInstance(locationDetails)
                bottomSheetFragment.show(parentFragmentManager, "LocationBottomSheet")
            }
        }

        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView?.overlays?.add(mapEventsOverlay)
    }

    private fun addClickedLocationMarker(point: GeoPoint) {
        // Remove the previous clicked location marker if it exists
        clickedLocationMarker?.let {
            mapView?.overlays?.remove(it)
        }

        clickedLocationMarker = Marker(mapView).apply {
            position = point
            icon = resources.getDrawable(R.drawable.add_location) // Your clicked location marker icon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView?.overlays?.add(clickedLocationMarker)
        mapView?.invalidate()
    }

    private fun getCurrentLocation() {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
    }

    override fun onLocationChanged(location: Location) {
        mapView?.let { map ->
            val currentPoint = GeoPoint(location.latitude, location.longitude)

            // Remove the previous location marker if it exists
            currentLocationMarker?.let {
                map.overlays.remove(it)
            }

            // Add a marker for the current location
            currentLocationMarker = Marker(map).apply {
                position = currentPoint
                icon = resources.getDrawable(R.drawable.location_on)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            map.overlays.add(currentLocationMarker)
            map.controller.setCenter(currentPoint)
            map.invalidate()
        } ?: run {
            Log.e("MapFragment", "MapView is null, cannot update location")
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()  // Enable map view
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()  // Disable map view
        // Remove location updates to prevent null pointer on mapView
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDetach()  // Clean up map view
        mapView = null
    }
}