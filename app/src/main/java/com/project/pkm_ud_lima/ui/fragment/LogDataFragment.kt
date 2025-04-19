package com.project.pkm_ud_lima.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.project.pkm_ud_lima.R
import com.project.pkm_ud_lima.data.response.FlameDataItem
import com.project.pkm_ud_lima.data.response.FlameResponse
import com.project.pkm_ud_lima.data.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogDataFragment : Fragment() {

    private lateinit var tableLayout: TableLayout
    private var currentPage = 0
    private val itemsPerPage = 10
    private var fullData: List<FlameDataItem> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tableLayout = view.findViewById(R.id.tableLayout)

        val btnPrev = view.findViewById<Button>(R.id.btnPrev)
        val btnNext = view.findViewById<Button>(R.id.btnNext)

        btnPrev.setOnClickListener {
            if ((currentPage + 1) * itemsPerPage < fullData.size) {
                currentPage++
                displayPage(currentPage)
            }
        }

        btnNext.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                displayPage(currentPage)
            }
        }
        getFlameData()
    }

    private fun getFlameData() {
        ApiConfig.getFlameService().getFlame().enqueue(object : Callback<FlameResponse> {
            override fun onResponse(call: Call<FlameResponse>, response: Response<FlameResponse>) {
                if (response.isSuccessful) {
                    fullData = response.body()?.data ?: emptyList()
                    displayPage(currentPage)
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FlameResponse>, t: Throwable) {
                Toast.makeText(context, "Gagal koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayPage(page: Int) {
        tableLayout.removeViews(1, tableLayout.childCount - 1) // hapus baris kecuali header

        val totalItems = fullData.size
        val start = totalItems - (page + 1) * itemsPerPage
        val end = totalItems - page * itemsPerPage

        if (start < 0) return // Jika sudah habis, tidak tampilkan apa-apa

        val pageItems = fullData.subList(maxOf(0, start), minOf(end, totalItems))

        for (data in pageItems.reversed()) { // reversed supaya data terbaru di atas
            val row = TableRow(context)

            val voltase = data.analogValue?.toIntOrNull()?.let {
                val volt = (it.toFloat() / 4095f) * 3.3f
                String.format("%.2fV", volt)
            } ?: "-"

            row.addView(createCell(data.idHasil ?: "-"))
            row.addView(createCell(voltase))
            row.addView(createCell(data.analogValue ?: "-"))
            row.addView(createCell(data.keputusan ?: "-"))
            row.addView(createCell(data.timestamp ?: "-"))

            tableLayout.addView(row)
        }

        updatePaginationButtons()
    }


    private fun updatePaginationButtons() {
        val totalItems = fullData.size
        val maxPages = totalItems / itemsPerPage

        view?.findViewById<Button>(R.id.btnPrev)?.isEnabled = currentPage < maxPages
        view?.findViewById<Button>(R.id.btnNext)?.isEnabled = currentPage > 0
    }



    private fun createCell(text: String): TextView {
        val textView = TextView(context)
        textView.apply {
            setPadding(16, 12, 16, 12) // padding dalam sel
            textSize = 14f
            setText(text)
        }

        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(12, 12, 12, 12) // jarak antar sel
        textView.layoutParams = params

        return textView
    }

}
