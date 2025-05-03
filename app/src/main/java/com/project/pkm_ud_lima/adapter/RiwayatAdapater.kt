package com.project.pkm_ud_lima.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.pkm_ud_lima.R
import com.project.pkm_ud_lima.data.response.FlameDataItem

class RiwayatAdapter(private val listData: List<FlameDataItemGrouped>) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    data class FlameDataItemGrouped(
        val tanggal: String,
        val listItem: List<FlameDataItem>
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val container: LinearLayout = view.findViewById(R.id.containerRiwayat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_grouped, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val groupedItem = listData[position]
        holder.tvTanggal.text = groupedItem.tanggal

        holder.container.removeAllViews()
        for (item in groupedItem.listItem) {
            val itemView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_riwayat, holder.container, false)

            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            val tvJam = itemView.findViewById<TextView>(R.id.tvJam)

            tvStatus.text = if (item.keputusan.equals("Bahaya", true)) "Api Terdeteksi" else "Aman"
            tvJam.text = item.timestamp?.substring(11, 16) ?: "-"

            holder.container.addView(itemView)
        }
    }
}
