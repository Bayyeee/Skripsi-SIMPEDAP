package com.project.pkm_ud_lima.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.pkm_ud_lima.adapter.RiwayatAdapter
import com.project.pkm_ud_lima.data.response.FlameResponse
import com.project.pkm_ud_lima.data.retrofit.ApiConfig
import com.project.pkm_ud_lima.databinding.FragmentRiwayatBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatFragment : Fragment() {
    private lateinit var binding: FragmentRiwayatBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val api = ApiConfig.getFlameService()
        api.getFlamePaginated(100, 0).enqueue(object : Callback<FlameResponse> {
            override fun onResponse(call: Call<FlameResponse>, response: Response<FlameResponse>) {
                if (response.isSuccessful) {
                    val listData = response.body()?.data ?: emptyList()

                    // Filter hanya data dengan keputusan "Bahaya"
                    val filteredData = listData.filter {
                        it.keputusan.equals("Bahaya", ignoreCase = true)
                    }

                    // Format tanggal sekarang
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val now = System.currentTimeMillis()
                    val tigaHariMillis = 3 * 24 * 60 * 60 * 1000L

                    // Filter hanya 3 hari terakhir
                    val recentData = filteredData.filter {
                        val tanggal = it.timestamp?.substring(0, 10)
                        tanggal != null && run {
                            val dataTime = sdf.parse(tanggal)?.time ?: 0
                            now - dataTime <= tigaHariMillis
                        }
                    }

                    // Ambil maksimal 10 data terbaru
                    val limitedData = recentData.sortedByDescending { it.timestamp }.take(10)

                    // Grup berdasarkan tanggal
                    val grouped = limitedData.groupBy {
                        it.timestamp?.substring(0, 10) ?: "Tidak diketahui"
                    }.map { (tanggal, listItem) ->
                        RiwayatAdapter.FlameDataItemGrouped(
                            tanggal = formatTanggalIndonesia(tanggal),
                            listItem = listItem
                        )
                    }

                    val adapter = RiwayatAdapter(grouped)
                    binding.recyclerViewRiwayat.adapter = adapter
                    binding.recyclerViewRiwayat.layoutManager = LinearLayoutManager(requireContext())
                }
            }


            override fun onFailure(call: Call<FlameResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Gagal ambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatTanggalIndonesia(input: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
            val date = sdfInput.parse(input)
            sdfOutput.format(date!!)
        } catch (e: Exception) {
            input
        }
    }

}