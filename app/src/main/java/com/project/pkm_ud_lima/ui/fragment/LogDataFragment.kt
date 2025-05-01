package com.project.pkm_ud_lima.ui.fragment

import android.app.DatePickerDialog
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
import java.util.*

class LogDataFragment : Fragment() {

    private lateinit var tableLayout: TableLayout
    private lateinit var paginationContainer: LinearLayout
    private lateinit var dateInput: EditText
    private lateinit var searchButton: Button

    private var currentPage = 0
    private val itemsPerPage = 10
    private var totalData = 0
    private var fullData: List<FlameDataItem> = emptyList()
    private var selectedDate: String? = null // format yyyy-MM-dd

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
        dateInput = view.findViewById(R.id.dateInput)
        searchButton = view.findViewById(R.id.searchButton)

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        searchButton.setOnClickListener {
            currentPage = 0
            getFlameData()
        }

        getFlameData()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                selectedDate = date
                dateInput.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun getFlameData() {
        val offset = currentPage * itemsPerPage
        val apiCall: Call<FlameResponse> = if (!selectedDate.isNullOrEmpty()) {
            ApiConfig.getFlameService().getFlamePaginated(itemsPerPage, offset, selectedDate!!)
        } else {
            ApiConfig.getFlameService().getFlamePaginated(itemsPerPage, offset, "")
        }

        apiCall.enqueue(object : Callback<FlameResponse> {
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

        if (currentPage > 0) {
            val btnPrev = Button(requireContext())
            btnPrev.text = "<"
            btnPrev.setOnClickListener {
                currentPage--
                getFlameData()
            }
            paginationContainer.addView(btnPrev)
        }

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
