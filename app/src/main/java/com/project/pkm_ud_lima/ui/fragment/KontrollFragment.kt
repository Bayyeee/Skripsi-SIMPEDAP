package com.project.pkm_ud_lima.ui.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.project.pkm_ud_lima.R
import com.project.pkm_ud_lima.data.response.ControlStatusResponse
import com.project.pkm_ud_lima.data.response.UpdateRelayResponse
import com.project.pkm_ud_lima.data.retrofit.ApiConfig
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [KontrollFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class KontrollFragment : Fragment() {

    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var relay1OnButton: Button
    private lateinit var relay1OffButton: Button
    private lateinit var relay2OnButton: Button
    private lateinit var relay2OffButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kontroll, container, false)

        // Initialize
        startTimeTextView = view.findViewById(R.id.startTime)
        endTimeTextView = view.findViewById(R.id.endTime)
        relay1OnButton = view.findViewById(R.id.relay1OnButton)
        relay1OffButton = view.findViewById(R.id.relay1OffButton)
        relay2OnButton = view.findViewById(R.id.relay2OnButton)
        relay2OffButton = view.findViewById(R.id.relay2OffButton)

        setupTimePickers()
        loadTimes()
        setupRelayControls()
        loadControlStatus()
        setupSaveButton(view)


        return view
    }

    private fun loadControlStatus() {
        val apiService = ApiConfig.getFlameService()
        apiService.getControlStatus().enqueue(object : retrofit2.Callback<ControlStatusResponse> {
            override fun onResponse(
                call: Call<ControlStatusResponse>,
                response: retrofit2.Response<ControlStatusResponse>
            ) {
                if (response.isSuccessful) {
                    val controlStatus = response.body()
                    controlStatus?.let {
                        // Update UI berdasarkan status relay yang diterima
                        updateRelayUI(it)
                        Toast.makeText(requireContext(), "Berhasil mengambil status kontrol", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil status kontrol", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ControlStatusResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRelayUI(status: ControlStatusResponse) {
        if (status.relay1Status == "ON") {
            relay1OnButton.isEnabled = false
            relay1OffButton.isEnabled = true
        } else {
            relay1OnButton.isEnabled = true
            relay1OffButton.isEnabled = false
        }

        if (status.relay2Status == "ON") {
            relay2OnButton.isEnabled = false
            relay2OffButton.isEnabled = true
        } else {
            relay2OnButton.isEnabled = true
            relay2OffButton.isEnabled = false
        }
    }

    private fun setupTimePickers() {
        startTimeTextView.setOnClickListener {
            showTimePicker { hour, minute ->
                startTimeTextView.text = formatTime(hour, minute)
            }
        }

        endTimeTextView.setOnClickListener {
            showTimePicker { hour, minute ->
                endTimeTextView.text = formatTime(hour, minute)
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(requireContext(),
            { _, hourOfDay, minute -> onTimeSelected(hourOfDay, minute) },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timePicker.show()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun setupRelayControls() {
        // Relay 1
        relay1OnButton.setOnClickListener {
            animateButton(it)
            relay1OnButton.isEnabled = false
            relay1OffButton.isEnabled = true
            showToast("Relay Sistem 1 dinyalakan")
            // TODO: Tambahkan logic nyalakan Relay 1
        }

        relay1OffButton.setOnClickListener {
            animateButton(it)
            relay1OffButton.isEnabled = false
            relay1OnButton.isEnabled = true
            showToast("Relay Sistem 1 dimatikan")
            // TODO: Tambahkan logic matikan Relay 1
        }

        relay2OnButton.setOnClickListener {
            animateButton(it)
            relay2OnButton.isEnabled = false
            relay2OffButton.isEnabled = true
            showToast("Relay Sistem 2 dinyalakan")
            updateRelayStatus("ON")
        }

        relay2OffButton.setOnClickListener {
            animateButton(it)
            relay2OffButton.isEnabled = false
            relay2OnButton.isEnabled = true
            showToast("Relay Sistem 2 dimatikan")
            updateRelayStatus("OFF")
        }


        // Default kondisi awal
        relay1OnButton.isEnabled = true
        relay1OffButton.isEnabled = true
        relay2OnButton.isEnabled = true
        relay2OffButton.isEnabled = true
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateRelayStatus(status: String) {
        val apiService = ApiConfig.getFlameService()

        apiService.updateRelay2(status).enqueue(object : retrofit2.Callback<UpdateRelayResponse> {
            override fun onResponse(
                call: Call<UpdateRelayResponse>,
                response: retrofit2.Response<UpdateRelayResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), body?.message ?: "Gagal update", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateRelayResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun setupSaveButton(view: View) {
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener { v ->
            v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(OvershootInterpolator())
                        .withEndAction {
                            saveTimes()
                            Toast.makeText(requireContext(), "Waktu berhasil disimpan", Toast.LENGTH_SHORT).show()
                        }
                        .start()
                }
                .start()
        }
    }

    private fun saveTimes() {
        val sharedPref = requireContext().getSharedPreferences("TimeSettings", 0)
        val editor = sharedPref.edit()
        editor.putString("startTime", startTimeTextView.text.toString())
        editor.putString("endTime", endTimeTextView.text.toString())
        editor.apply()
    }

    private fun loadTimes() {
        val sharedPref = requireContext().getSharedPreferences("TimeSettings", 0)
        val savedStartTime = sharedPref.getString("startTime", "00:00")
        val savedEndTime = sharedPref.getString("endTime", "00:00")

        startTimeTextView.text = savedStartTime
        endTimeTextView.text = savedEndTime
    }
}
