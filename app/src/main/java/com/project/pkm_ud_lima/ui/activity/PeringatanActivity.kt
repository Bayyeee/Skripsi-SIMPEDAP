package com.project.pkm_ud_lima.ui.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.project.pkm_ud_lima.R
import com.project.pkm_ud_lima.data.response.FlameResponse
import com.project.pkm_ud_lima.data.retrofit.ApiConfig
import com.project.pkm_ud_lima.databinding.ActivityPeringatanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PeringatanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPeringatanBinding
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeringatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = ApiConfig.getFlameService()

        apiService.getFlamePaginated(
            limit = 10,
            offset = 0
        ).enqueue(object : Callback<FlameResponse> {
            override fun onResponse(call: Call<FlameResponse>, response: Response<FlameResponse>) {
                if (response.isSuccessful) {
                    val flameResponse = response.body()
                    val flameDataItem = flameResponse?.data?.firstOrNull()

                    if (flameDataItem != null) {
                        val value = flameDataItem.analogValue ?: "N/A"
                        val keputusan = flameDataItem.keputusan ?: "Tidak diketahui"

                        binding.tvValue.text = "Nilai Analog: $value"
                        binding.tvJarak.text = "Status: $keputusan"
                    } else {
                        Toast.makeText(this@PeringatanActivity, "Data kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PeringatanActivity, "Data sensor tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FlameResponse>, t: Throwable) {
                Toast.makeText(this@PeringatanActivity, "Tidak terhubung ke sensor", Toast.LENGTH_SHORT).show()
            }
        })

        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.tvSOS.startAnimation(pulseAnimation)

        mediaPlayer = MediaPlayer.create(this, R.raw.sirene)
        mediaPlayer.isLooping = true // agar berulang
        mediaPlayer.start()

        binding.btnMatikan.setOnClickListener {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
            finish() // tutup activity jika perlu
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}
