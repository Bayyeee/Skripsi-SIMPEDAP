package com.project.pkm_ud_lima.ui.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.project.pkm_ud_lima.R
import com.project.pkm_ud_lima.data.response.FlameDataItem
import com.project.pkm_ud_lima.data.response.FlameResponse
import com.project.pkm_ud_lima.data.retrofit.ApiConfig
import com.project.pkm_ud_lima.ui.custome.CustomMarkerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Grafikfragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var spinnerRange: Spinner
    private lateinit var loadingLayout: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private var autoRefreshRunnable: Runnable? = null
    private lateinit var conversionTable: TableLayout

    private var selectedRangeInDays = 7 // Default 1 minggu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grafik, container, false)

        lineChart = view.findViewById(R.id.lineChart)
        spinnerRange = view.findViewById(R.id.spinnerRange)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        conversionTable = view.findViewById(R.id.conversionTable)

        setupSpinner()
        setupAutoRefresh()
        loadData()

        val backButton = view.findViewById<View>(R.id.backButton)
        backButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment_activity_main, HomeFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun setupSpinner() {
        val spinnerItems = listOf("1 Minggu Terakhir", "2 Minggu Terakhir", "3 Minggu Terakhir", "1 Bulan Terakhir")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRange.adapter = adapter

        spinnerRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedRangeInDays = when (position) {
                    0 -> 7
                    1 -> 14
                    2 -> 21
                    3 -> 30
                    else -> 7
                }
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupAutoRefresh() {
        autoRefreshRunnable = object : Runnable {
            override fun run() {
                loadData()
                handler.postDelayed(this, 3 * 60 * 1000L) // setiap 3 menit
            }
        }
        handler.post(autoRefreshRunnable!!)
    }

    private fun loadData() {
        showLoading(true)

        ApiConfig.getFlameService().getFlamePaginated(16000, 0)
            .enqueue(object : Callback<FlameResponse> {
                override fun onResponse(call: Call<FlameResponse>, response: Response<FlameResponse>) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        val data = response.body()?.data ?: emptyList()
                        if (data.isNotEmpty()) {
                            setupChart(data)
                            setupConversionTable(data)
                        } else {
                            Toast.makeText(requireContext(), "Data kosong.", Toast.LENGTH_SHORT).show()
                            lineChart.clear()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengambil data: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<FlameResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupChart(dataList: List<FlameDataItem>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val displaySdf = SimpleDateFormat("EEE HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()
        val minDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -selectedRangeInDays)
        }

        // Filter data
        val filteredData = dataList
            .mapNotNull { item ->
                try {
                    val date = sdf.parse(item.timestamp ?: "")?.let { date -> Pair(date, item) }
                    date
                } catch (e: Exception) {
                    null
                }
            }
            .filter { it.first.after(minDate.time) }
            .sortedBy { it.first }

        // Sampling setiap 4 jam
        val sampledData = mutableListOf<Pair<Date, FlameDataItem>>()
        var lastSampleTime: Calendar? = null

        for ((date, item) in filteredData) {
            val cal = Calendar.getInstance().apply { time = date }
            if (lastSampleTime == null || cal.timeInMillis - lastSampleTime.timeInMillis >= 4 * 60 * 60 * 1000L) {
                sampledData.add(Pair(date, item))
                lastSampleTime = cal
            }
        }

        if (sampledData.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada data untuk ditampilkan.", Toast.LENGTH_SHORT).show()
            lineChart.clear()
            return
        }

        // Siapkan Entry dan XAxis label
        val entries = sampledData.mapIndexed { index, pair ->
            val analogValue = pair.second.analogValue?.toFloatOrNull() ?: 0f
            Entry(index.toFloat(), analogValue)
        }

        val xAxisLabels = sampledData.map { pair ->
            displaySdf.format(pair.first)
        }

        // Setup LineDataSet
        val dataSet = LineDataSet(entries, "Analog Value").apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue)
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient_fill)
            highLightColor = ContextCompat.getColor(requireContext(), R.color.purple_500)
            setDrawHighlightIndicators(true)
        }

        // Setup Chart
        lineChart.apply {
            data = LineData(dataSet)

            val markerView = CustomMarkerView(requireContext())
            markerView.chartView = this
            marker = markerView

            description = Description().apply { text = "" }
            legend.apply {
                form = Legend.LegendForm.LINE
                textColor = Color.DKGRAY
                textSize = 12f
                isEnabled = true
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.DKGRAY
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in xAxisLabels.indices) xAxisLabels[index] else ""
                    }
                }
                labelRotationAngle = -45f
            }

            axisLeft.apply {
                textColor = Color.DKGRAY
                setDrawGridLines(true)
            }

            axisRight.isEnabled = false

            setTouchEnabled(true)
            setPinchZoom(true)
            isDragEnabled = true
            setScaleEnabled(true)

            animateX(1000)
            invalidate()
        }
    }

    private fun setupConversionTable(dataList: List<FlameDataItem>) {
        val conversionTable = requireView().findViewById<TableLayout>(R.id.conversionTable)
        conversionTable.removeAllViews()

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val displayTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayData = dataList.mapNotNull { item ->
            try {
                val date = sdf.parse(item.timestamp ?: "")
                if (date != null && date.after(today.time)) {
                    Pair(date, item)
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.first }
            .take(16)
            .reversed()

        if (todayData.isEmpty()) {
            val emptyRow = TableRow(requireContext())
            val emptyText = TextView(requireContext()).apply {
                id = View.generateViewId()
                text = "Tidak ada data hari ini."
                setPadding(8, 16, 8, 16)
                textSize = 14f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_border_grafik_tabel)
            }
            emptyRow.addView(emptyText)
            conversionTable.addView(emptyRow)
            return
        }

        // Header
        val headerRow = TableRow(requireContext()).apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_card))
        }
        val headers = listOf("ID Data", "Analog", "Jam", "Status")
        headers.forEach { header ->
            val tv = TextView(requireContext()).apply {
                id = View.generateViewId()
                text = header
                setPadding(16, 8, 16, 8)
                setTextColor(Color.WHITE)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_border_grafik_tabel)
            }
            headerRow.addView(tv)
        }
        conversionTable.addView(headerRow)

        // Data
        todayData.forEach { (date, item) ->
            val analogValue = item.analogValue?.toIntOrNull() ?: 0
            val idHasil = item.idHasil ?: "-"
            val time = displayTime.format(date)
            val status = if (analogValue in 1600..4095) "SIAGA" else "AMAN"

            val dataRow = TableRow(requireContext())

            val values = listOf(idHasil, "$analogValue", time, status)
            values.forEach { value ->
                val tv = TextView(requireContext()).apply {
                    id = View.generateViewId()
                    text = value
                    setPadding(16, 8, 16, 8)
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    gravity = Gravity.CENTER
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_border_grafik_tabel)
                }
                dataRow.addView(tv)
            }
            conversionTable.addView(dataRow)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoRefreshRunnable?.let { handler.removeCallbacks(it) }
    }
}
