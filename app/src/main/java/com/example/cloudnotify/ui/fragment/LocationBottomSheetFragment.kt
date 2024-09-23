package com.example.cloudnotify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cloudnotify.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LocationBottomSheetFragment : BottomSheetDialogFragment() {

    private var locationDetails: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve arguments (location details) if available
        locationDetails = arguments?.getString("LOCATION_DETAILS")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_bottom_sheet, container, false)
    }

    // You can update UI components here with the location details
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//         For example, you can set a TextView with the location details
//         val locationTextView = view.findViewById<TextView>(R.id.location_details_text_view)
//         locationTextView.text = "dfsfd"
    }

    companion object {
        fun newInstance(locationDetails: String): LocationBottomSheetFragment {
            val fragment = LocationBottomSheetFragment()
            val args = Bundle().apply {
                putString("LOCATION_DETAILS", locationDetails)
            }
            fragment.arguments = args
            return fragment
        }
    }
}