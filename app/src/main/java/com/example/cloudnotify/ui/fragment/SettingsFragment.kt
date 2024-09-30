package com.example.cloudnotify.ui.fragment
import HomeFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cloudnotify.R
import com.example.cloudnotify.data.repo.SettingRepo
import com.example.cloudnotify.databinding.FragmentSettingsBinding
import com.example.cloudnotify.ui.activity.MainActivity

class SettingsFragment : Fragment() {
 private lateinit var  binding: FragmentSettingsBinding
 private lateinit var settingRepo: SettingRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingRepo = SettingRepo(requireActivity().application)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding = FragmentSettingsBinding.inflate(inflater, container, false)


binding.radioGroupLocation.setOnCheckedChangeListener { group, checkedId ->
    if (binding.rbGps.isChecked) {

        val homeFragment = HomeFragment()

        // Get the FragmentManager and start a transaction to replace the current fragment
        val transaction = parentFragmentManager.beginTransaction()

        // Replace the fragment and add the transaction to the back stack (so user can navigate back)
        transaction.replace(R.id.fragment_container, homeFragment)
        transaction.addToBackStack(null)  // Add this if you want the user to be able to go back

        // Commit the transaction
        transaction.commit()
    } else if (binding.rbMap.isChecked) {
        val mapFragment = MapFragment()

        // Get the FragmentManager and start a transaction to replace the current fragment
        val transaction = parentFragmentManager.beginTransaction()

        // Replace the fragment and add the transaction to the back stack (so user can navigate back)
        transaction.replace(R.id.fragment_container, mapFragment)
        transaction.addToBackStack(null)  // Add this if you want the user to be able to go back

        // Commit the transaction
        transaction.commit()    }
}

        binding.radioGroupLanguage.setOnCheckedChangeListener { group, checkedId ->
            if (binding.rbEnglish.isChecked) {
                settingRepo.saveLanguage("en")

                ( requireActivity()as MainActivity).checkAndChangLocality()

            } else if (binding.rbArabic.isChecked) {
                settingRepo.saveLanguage("ar")
                ( requireActivity()as MainActivity).checkAndChangLocality()

            }
        }

        binding.radioGroupTemperature.setOnCheckedChangeListener { group, checkedId ->
            if (binding.rbCelsius.isChecked) {
                settingRepo.saveUnit("metric")

            } else if (binding.rbFahrenheit.isChecked) {
                settingRepo.saveUnit("imperial")}

                else if (binding.rbKelvin.isChecked) {
                    settingRepo.saveUnit("")
                }

            }


        return binding.root



}
}