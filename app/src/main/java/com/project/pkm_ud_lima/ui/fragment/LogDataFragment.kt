package com.project.pkm_ud_lima.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.*
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
    private lateinit var paginationContainer: LinearLayout
    private var currentPage = 0
    private val itemsPerPage = 10
    private var totalData = 0
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
        paginationContainer = view.findViewById(R.id.paginationContainer)

        getFlameData()
    }

    private fun getFlameData() {
        val offset = currentPage * itemsPerPage
        ApiConfig.getFlameService().getFlamePaginated(itemsPerPage, offset)
            .enqueue(object : Callback<FlameResponse> {
                override fun onResponse(call: Call<FlameResponse>, response: Response<FlameResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            fullData = body.data
                            totalData = body.totalData
                            displayPage()
                        }
                    } else {
                        Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<FlameResponse>, t: Throwable) {
                    Toast.makeText(context, "Gagal koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayPage() {
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        if (fullData.isEmpty()) {
            val row = TableRow(context)
            row.addView(createCell("Data tidak ditemukan"))
            tableLayout.addView(row)
            return
        }

        for (data in fullData) {
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

        setupPaginationButtons()
    }

    private fun setupPaginationButtons() {
        paginationContainer.removeAllViews()

        val totalPages = (totalData + itemsPerPage - 1) / itemsPerPage
        val maxPagesToShow = 5

        var startPage = currentPage - 2
        var endPage = currentPage + 3

        if (startPage < 0) {
            endPage += -startPage
            startPage = 0
        }
        if (endPage > totalPages) {
            startPage -= endPage - totalPages
            endPage = totalPages
            if (startPage < 0) startPage = 0
        }

        // Tombol Previous
        if (currentPage > 0) {
            val btnPrev = Button(requireContext())
            btnPrev.text = "<"
            btnPrev.setOnClickListener {
                currentPage--
                getFlameData()
            }
            paginationContainer.addView(btnPrev)
        }

        // Tombol Halaman
        for (i in startPage until endPage) {
            val btnPage = Button(requireContext())
            btnPage.text = (i + 1).toString()
            btnPage.setOnClickListener {
                currentPage = i
                getFlameData()
            }

            if (i == currentPage) {
                btnPage.setBackgroundResource(R.drawable.active_page_background)
            }

            paginationContainer.addView(btnPage)
        }

        // Tombol Next
        if (currentPage < totalPages - 1) {
            val btnNext = Button(requireContext())
            btnNext.text = ">"
            btnNext.setOnClickListener {
                currentPage++
                getFlameData()
            }
            paginationContainer.addView(btnNext)
        }
    }

    private fun createCell(text: String): TextView {
        val textView = TextView(context)
        textView.apply {
            setPadding(16, 12, 16, 12)
            textSize = 14f
            setText(text)
        }

        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(12, 12, 12, 12)
        textView.layoutParams = params

        return textView
    }
}
