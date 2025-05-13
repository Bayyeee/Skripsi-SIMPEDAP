package com.project.pkm_ud_lima.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.project.pkm_ud_lima.data.retrofit.ApiConfig
import com.project.pkm_ud_lima.data.response.UpdateRelayResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RelayControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getStringExtra("ACTION") ?: return
        val apiService = ApiConfig.getFlameService()

        apiService.updateRelay2(action).enqueue(object : Callback<UpdateRelayResponse> {
            override fun onResponse(
                call: Call<UpdateRelayResponse>,
                response: Response<UpdateRelayResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Relay2 $action berhasil dijalankan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal mengatur Relay2", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateRelayResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
