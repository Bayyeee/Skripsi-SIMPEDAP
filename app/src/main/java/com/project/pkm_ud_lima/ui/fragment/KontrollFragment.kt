package com.project.pkm_ud_lima.ui.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.project.pkm_ud_lima.R
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
        setupRelayControls()

        return view
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
            false
        )
        timePicker.show()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
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

        // Relay 2
        relay2OnButton.setOnClickListener {
            animateButton(it)
            relay2OnButton.isEnabled = false
            relay2OffButton.isEnabled = true
            showToast("Relay Sistem 2 dinyalakan")
            // TODO: Tambahkan logic nyalakan Relay 2
        }

        relay2OffButton.setOnClickListener {
            animateButton(it)
            relay2OffButton.isEnabled = false
            relay2OnButton.isEnabled = true
            showToast("Relay Sistem 2 dimatikan")
            // TODO: Tambahkan logic matikan Relay 2
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
}
