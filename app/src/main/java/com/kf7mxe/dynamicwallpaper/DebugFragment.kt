package com.kf7mxe.dynamicwallpaper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kf7mxe.dynamicwallpaper.databinding.FragmentDebugBinding
import com.kf7mxe.dynamicwallpaper.models.TriggerByWeather

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DebugFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DebugFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var binding: FragmentDebugBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentDebugBinding.inflate(inflater, container, false)

        binding!!.testSettingUpWeatherPredictionTriggers.setOnClickListener {
            // call funtion on different thread
            Thread(Runnable {
                testSettingUpWeatherPredictionTriggers()
            }).start()

        }

        // Inflate the layout for this fragment
        return binding!!.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DebugFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                DebugFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


    fun testSettingUpWeatherPredictionTriggers() {
        val weatherTrigger = TriggerByWeather()
        weatherTrigger.setWhenTempretureIsGreaterThan("80")
        weatherTrigger.setLocationType("currentLocation")

        val latLon = weatherTrigger.getLatLonLocationFromGps(context)
        val url = weatherTrigger.getWeatherUrl(latLon)
        val jsonArray = weatherTrigger.getWeatherForcast(url)
        weatherTrigger.setUpWeatherPredictionTriggers(jsonArray, context,0,0)

    }
}