package com.project.pkm_ud_lima.data.response

import com.google.gson.annotations.SerializedName

data class ControlStatusResponse(
    @SerializedName("relay2_status") val relay2Status: String,
    @SerializedName("keputusan") val keputusan: String,
    @SerializedName("relay1_status") val relay1Status: String
)