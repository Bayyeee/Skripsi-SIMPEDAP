package com.project.pkm_ud_lima.ui.custome

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.project.pkm_ud_lima.R

class CustomMarkerView(context: Context) : MarkerView(context, R.layout.marker_view) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            tvContent.text = "X: ${e.x.toInt()}, Y: ${e.y.toInt()}"  // Bisa custom format sesuai kebutuhan
        }
        super.refreshContent(e, highlight)
    }

    fun getOffsetX(): Int {
        return -(width / 2)   // Biar posisi MarkerView di tengah
    }

    fun getOffsetY(): Int {
        return -height        // Biar posisi MarkerView di atas titik
    }
}
