package com.project.pkm_ud_lima.ui.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.project.pkm_ud_lima.R
import com.project.pkm_ud_lima.ui.custome.CustomMarkerView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast


class Grafikfragment : Fragment() {

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grafik, container, false)

        lineChart = view.findViewById(R.id.lineChart)

        // 1. Set adapter spinner
        val spinnerRange = view.findViewById<Spinner>(R.id.spinnerRange)
        val spinnerItems = listOf("1 Minggu Terakhir", "2 Minggu Terakhir", "3 Minggu Terakhir", "1 Bulan Terakhir")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRange.adapter = adapter

        // 2. Set listener spinner
        spinnerRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = spinnerItems[position]
                // TODO: Update grafik berdasarkan pilihan
                Toast.makeText(requireContext(), "Kamu pilih: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        setupDummyChart()

        // TODO: BACK BUTTON
        val backButton = view.findViewById<View>(R.id.backButton)
        backButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment_activity_main, HomeFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun setupDummyChart() {
        val entries = listOf(
            Entry(0f, 1000f),
            Entry(1f, 1500f),
            Entry(2f, 1200f),
            Entry(3f, 1800f),
            Entry(4f, 1700f),
            Entry(5f, 2000f)
        )

        val dataSet = LineDataSet(entries, "Data Analog Dummy").apply {
            // Warna garis
            color = ContextCompat.getColor(requireContext(), R.color.blue)
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 5f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient_fill)
            // Warna garis highlight
            highLightColor = ContextCompat.getColor(requireContext(), R.color.purple_500)
            setDrawHighlightIndicators(true)
        }

        lineChart.apply {
            data = LineData(dataSet)

            // Custom Marker
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

            animateX(5000)
            invalidate()
        }

    }
}
