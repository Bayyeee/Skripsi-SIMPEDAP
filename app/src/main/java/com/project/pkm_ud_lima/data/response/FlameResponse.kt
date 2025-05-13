package com.project.pkm_ud_lima.data.response

import com.google.gson.annotations.SerializedName

// Data class untuk tiap item
data class FlameDataItem(
	@SerializedName("id_hasil") val idHasil: String?,
	@SerializedName("analog_value") val analogValue: String?,
	@SerializedName("timestamp") val timestamp: String?,
	@SerializedName("hasil_perhitungan") val hasilPerhitungan: String?,
	@SerializedName("keputusan") val keputusan: String?,
	@SerializedName("relay1_status") val relay1Status: String?
)

// Wrapper
data class FlameResponse(
	@SerializedName("data") val data: List<FlameDataItem>,
	@SerializedName("total_data") val totalData: Int,
	@SerializedName("limit") val limit: Int,
	@SerializedName("offset") val offset: Int
)
