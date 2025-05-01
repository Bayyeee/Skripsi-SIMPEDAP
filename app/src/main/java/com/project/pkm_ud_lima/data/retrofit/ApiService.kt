package com.project.pkm_ud_lima.data.retrofit

import com.project.pkm_ud_lima.data.response.ControlStatusResponse
import com.project.pkm_ud_lima.data.response.CuacaResponse
import com.project.pkm_ud_lima.data.response.FlameDataItem
import com.project.pkm_ud_lima.data.response.FlameResponse
import com.project.pkm_ud_lima.data.response.UpdateRelayResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


// Interface class untuk berinteraksi dengan API
interface ApiService {
    @GET("data/2.5/forecast")
    fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        // API Key dari OpenWeatherMap, rencana akan dipindah ke secrets.properties agar lebih aman
        @Query("appid") appid: String = "93c3958c60e0d866237792c3bc451681",
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "id",
    ) : Call<CuacaResponse>

    // Flame Sensor
    @GET("get_api.php")
    fun getFlamePaginated(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("date") date: String? = null
    ): Call<FlameResponse>

    // RELAY STATUS
    @GET("controller_api.php")
    fun getControlStatus(): Call<ControlStatusResponse>

    @FormUrlEncoded
    @POST("update_relay2_api.php")
    fun updateRelay2(
        @Field("relay_status") relayStatus: String
    ): Call<UpdateRelayResponse>

}
