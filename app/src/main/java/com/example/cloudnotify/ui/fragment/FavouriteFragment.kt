package com.example.cloudnotify.ui.fragment
import HomeFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudnotify.R
import com.example.cloudnotify.Utility.LocationSource
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.data.repo.BookmarkRepository
import com.example.cloudnotify.databinding.FragmentFavouriteBinding
import com.example.cloudnotify.ui.adapters.BookMarkedItemAdapter
import com.example.cloudnotify.ui.adapters.OnCardClickListener
import com.example.cloudnotify.ui.adapters.OnRemoveClickListener
import com.example.cloudnotify.viewmodel.LocationViewModel
import com.example.cloudnotify.viewmodel.LocationViewModelFactory
import com.example.cloudnotify.viewmodel.favourite.FavouriteViewModel
import com.example.cloudnotify.viewmodel.favourite.FavouriteViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import com.example.cloudnotify.Utility.DialogUtils
import com.example.cloudnotify.Utility.NetworkUtils

class FavouriteFragment : Fragment(), OnRemoveClickListener, OnCardClickListener {
    lateinit var binding: FragmentFavouriteBinding
    lateinit var bookMarkedItemAdapter: BookMarkedItemAdapter
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var favouriteViewModel: FavouriteViewModel
    private lateinit var networkUtils: NetworkUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize BookmarkLocationDao
        val bookmarkLocationDao = WeatherDataBase.getInstance(requireActivity()).bookmarkLocationDao

        // Initialize BookmarkRepository with dependencies
        val bookmarkRepository = BookmarkRepository(bookmarkLocationDao)

        // Initialize ViewModel for this fragment
        val favouriteViewModelFactory = FavouriteViewModelFactory(bookmarkRepository)
        favouriteViewModel = favouriteViewModelFactory.create(FavouriteViewModel::class.java)
// Initialize NetworkUtils
        networkUtils = NetworkUtils(requireContext())
        // Initialize LocationViewModel (shared)
        val locationViewModelFactory = LocationViewModelFactory(requireActivity().application)
        locationViewModel = locationViewModelFactory.create(LocationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookMarkedItemAdapter = BookMarkedItemAdapter(this, this)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = bookMarkedItemAdapter

        observeBookmarkList()
    }

    private fun observeBookmarkList() {
        // Collect and observe data from the ViewModel's StateFlow
        lifecycleScope.launchWhenStarted {
            favouriteViewModel.bookmarkList.collectLatest { bookmarkList ->
                bookMarkedItemAdapter.updateData(bookmarkList)
            }
        }
    }

    override fun onRemoveClick(bookmarkLocation: BookmarkLocation) {
        favouriteViewModel.deleteBookmark(bookmarkLocation)
    }

    override fun onCardClick(bookmarkLocation: BookmarkLocation) {
        if (networkUtils.hasNetworkConnection()) {
            // Update location in ViewModel and navigate to HomeFragment
            locationViewModel.updateLocation(bookmarkLocation.latitude.toLong(), bookmarkLocation.longitude.toLong())
            locationViewModel.upDateSource(LocationSource.SEARCH)

            val transaction = parentFragmentManager.beginTransaction()
            val homeFragment = HomeFragment()
            transaction.replace(R.id.fragment_container, homeFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        } else {
            // Show dialog with Lottie animation if no internet
            DialogUtils.showNoInternetDialog(requireContext())
        }
    }

}