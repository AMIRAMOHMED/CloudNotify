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
import com.example.cloudnotify.data.local.db.BookmarkLocationDao
import com.example.cloudnotify.data.local.db.WeatherDataBase
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.data.repo.BookmarkRepository
import com.example.cloudnotify.databinding.FragmentFavouriteBinding
import com.example.cloudnotify.ui.adapters.BookMarkedItemAdapter
import com.example.cloudnotify.ui.adapters.OnCardClickListener
import com.example.cloudnotify.ui.adapters.OnRemoveClickListener
import com.example.cloudnotify.viewmodel.LocationViewModel
import com.example.cloudnotify.viewmodel.LocationViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext




class FavouriteFragment : Fragment(), OnRemoveClickListener , OnCardClickListener {
    lateinit var  binding: FragmentFavouriteBinding
    lateinit var bookMarkedItemAdapter: BookMarkedItemAdapter
    private lateinit var bookmarkLocationDao: BookmarkLocationDao
    private lateinit var bookmarkRepository: BookmarkRepository
    private  lateinit var locationViewModelFactory: LocationViewModelFactory
    private  lateinit var locationViewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize BookmarkLocationDao
        bookmarkLocationDao = WeatherDataBase.getInstance(requireActivity()).bookmarkLocationDao
        // Initialize BookmarkRepository with dependencies
        bookmarkRepository = BookmarkRepository(
            bookmarkLocationDao,

            )
        // Initialize ViewModel
        locationViewModelFactory = LocationViewModelFactory( requireActivity().application)
        locationViewModel = locationViewModelFactory.create(LocationViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFavouriteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookMarkedItemAdapter= BookMarkedItemAdapter(this,this)
        binding.recyclerView.layoutManager=
            LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.recyclerView.adapter=bookMarkedItemAdapter

        updatedBookmarkList()

    }
private fun updatedBookmarkList() {
    lifecycleScope.launch(Dispatchers.IO) {

        bookmarkRepository.getAllBookmarkLocations().collect() { bookmarkList ->
            withContext(Dispatchers.Main) {
                bookMarkedItemAdapter.updateData(bookmarkList)

            }
        }
    }

}

    override fun onRemoveClick(bookmarkLocation: BookmarkLocation) {

        lifecycleScope.launch(Dispatchers.IO) {

            bookmarkRepository.deleteBookmarkById(bookmarkLocation.id)


        }

    }

    override fun onCardClick(bookmarkLocation: BookmarkLocation) {
        locationViewModel.updateLocation(bookmarkLocation.latitude.toLong(),bookmarkLocation.longitude.toLong())
        locationViewModel.upDateSource(LocationSource.SEARCH)
        val transaction = parentFragmentManager.beginTransaction()
        val homeFragment=HomeFragment()

        transaction.replace(R.id.fragment_container, homeFragment)
        transaction.addToBackStack(null)
        transaction.commit()


    }
}

