package com.project.pkm_ud_lima.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.pkm_ud_lima.R
import com.project.pkm_ud_lima.data.retrofit.ApiConfig
import com.project.pkm_ud_lima.data.response.CuacaResponse
import com.project.pkm_ud_lima.databinding.FragmentHomeBinding
import com.project.pkm_ud_lima.ui.activity.MainActivity
import com.project.pkm_ud_lima.ui.activity.PeringatanActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private val weatherMap = hashMapOf(
        "Thunderstorm" to "Badai Petir",
        "Drizzle" to "Gerimis",
        "Rain" to "Hujan",
        "Snow" to "Salju",
        "Mist" to "Kabut",
        "Smoke" to "Asap",
        "Haze" to "Kabut",
        "Dust" to "Debu",
        "Fog" to "Kabut",
        "Sand" to "Pasir",
        "Ash" to "Abu",
        "Squall" to "Angin Kencang",
        "Tornado" to "Puting Beliung",
        "Clear" to "Cerah",
        "Clouds" to "Berawan"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiConfig.getWeatherService()
        val lat = -3.101074
        val lon = 114.717258

        apiService.getForecast(lat, lon).enqueue(object : Callback<CuacaResponse> {
            override fun onResponse(call: Call<CuacaResponse>, response: Response<CuacaResponse>) {
                if (response.isSuccessful) {
                    val cuacaResponse = response.body()
                    val forecastList = cuacaResponse?.list

//                    val oldFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val newFormat = SimpleDateFormat("d MMMM yyyy", Locale("in", "ID"))
                    val dayFormat = SimpleDateFormat("EEEE", Locale("in", "ID"))
//                    val hourFormat = SimpleDateFormat("HH:mm", Locale("in", "ID"))

                    val oldFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    oldFormat.timeZone = TimeZone.getTimeZone("UTC")

                    val hourFormat = SimpleDateFormat("HH:mm", Locale("in", "ID"))
                    hourFormat.timeZone = TimeZone.getTimeZone("Asia/Makassar")


                    // Ambil data cuaca pertama untuk header utama
                    val firstForecast = forecastList?.get(0)
                    val weather = firstForecast?.weather?.get(0)?.main
                    val translatedWeather = weatherMap[weather] ?: weather
                    val temperature = firstForecast?.main?.temp?.let { Math.round(it.toString().toDouble()).toInt() }
                    val date = firstForecast?.dtTxt
                    val parsedDate = oldFormat.parse(date)
                    val formattedDate = newFormat.format(parsedDate)
                    val dayDate = dayFormat.format(parsedDate)
                    val iconCode = firstForecast?.weather?.get(0)?.icon
                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

                    // Load ke ImageView pakai Glide
                    Glide.with(requireContext())
                        .load(iconUrl)
                        .into(binding.cuacautama)


                    binding.tvCuacahari.text = "$dayDate\n$formattedDate\n$translatedWeather"
                    binding.tvCuacasuhu.text = "$temperature"

                    // Ambil data 4 jam ke depan
                    if (forecastList != null && forecastList.size >= 4) {
                        for (i in 0..3) {
                            val forecast = forecastList[i]
                            val dtTxt = forecast?.dtTxt
                            val parsed = oldFormat.parse(dtTxt)
                            val jam = hourFormat.format(parsed!!)
                            val suhu = forecast?.main?.temp?.let { Math.round(it.toString().toDouble()).toInt() }
                            val iconCode = forecast?.weather?.get(0)?.icon
                            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

                            when (i) {
                                0 -> {
                                    binding.tvSuhusmallcardviewcuaca1.text = "$suhu°C"
                                    binding.prediksi1.text = "$jam"
                                    Glide.with(requireContext()).load(iconUrl).into(binding.ivCuacasmallcardviewcuaca1)
                                }
                                1 -> {
                                    binding.tvSuhusmallcardviewcuaca2.text = "$suhu°C"
                                    binding.prediksi2.text = "$jam"
                                    Glide.with(requireContext()).load(iconUrl).into(binding.ivCuacasmallcardviewcuaca2)
                                }
                                2 -> {
                                    binding.tvSuhusmallcardviewcuaca3.text = "$suhu°C"
                                    binding.prediksi3.text = "$jam"
                                    Glide.with(requireContext()).load(iconUrl).into(binding.ivCuacasmallcardviewcuaca3)
                                }
                                3 -> {
                                    binding.tvSuhusmallcardviewcuaca4.text = "$suhu°C"
                                    binding.prediksi4.text = "$jam"
                                    Glide.with(requireContext()).load(iconUrl).into(binding.ivCuacasmallcardviewcuaca4)
                                }
                            }

                        }
                    }

                } else {
                    Toast.makeText(context, "Gagal mengambil data cuaca", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CuacaResponse>, t: Throwable) {
                Toast.makeText(context, "Koneksi ke server gagal", Toast.LENGTH_SHORT).show()
            }
        })

        val mainActivity = activity as MainActivity
        val bottomNavView: BottomNavigationView = mainActivity.binding.navView

        val cardCatatan: CardView = binding.CardCatatan
        cardCatatan.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.nav_host_fragment_activity_main, CatatanFragment())
            transaction?.addToBackStack(null)
            transaction?.commit()

            bottomNavView.selectedItemId = R.id.item_catatan
        }

        val cardPeringatan: CardView = binding.CardPeringatan
        cardPeringatan.setOnClickListener {
            val intent = Intent(context, PeringatanActivity::class.java)
            startActivity(intent)
        }
    }
}
