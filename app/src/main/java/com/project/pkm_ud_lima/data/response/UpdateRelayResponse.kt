package com.project.pkm_ud_lima.data.response

import com.google.gson.annotations.SerializedName

data class UpdateRelayResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
