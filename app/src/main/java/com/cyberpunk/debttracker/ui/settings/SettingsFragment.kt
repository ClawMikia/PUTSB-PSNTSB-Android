package com.cyberpunk.debttracker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.databinding.FragmentSettingsBinding
import com.cyberpunk.debttracker.util.ReminderManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val currentFreq = prefs.getString("overdue_reminder_frequency", "daily") ?: "daily"

        // Set initial selection
        val checkedId = when (currentFreq) {
            "none"   -> R.id.btn_freq_none
            "hourly" -> R.id.btn_freq_hourly
            "daily"  -> R.id.btn_freq_daily
            "weekly" -> R.id.btn_freq_weekly
            else     -> R.id.btn_freq_daily
        }
        binding.toggleFrequency.check(checkedId)

        binding.toggleFrequency.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val newFreq = when (checkedId) {
                    R.id.btn_freq_none   -> "none"
                    R.id.btn_freq_hourly -> "hourly"
                    R.id.btn_freq_daily  -> "daily"
                    R.id.btn_freq_weekly -> "weekly"
                    else                 -> "daily"
                }
                prefs.edit().putString("overdue_reminder_frequency", newFreq).apply()
                ReminderManager.scheduleReminders(requireContext(), newFreq)
            }
        }

        binding.cardAbout.setOnClickListener {
            findNavController().navigate(R.id.aboutFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
