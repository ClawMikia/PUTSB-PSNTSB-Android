package com.cyberpunk.debttracker.ui.settings

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.repository.DebtRepository
import com.cyberpunk.debttracker.databinding.FragmentSettingsBinding
import com.cyberpunk.debttracker.util.JsonBackupUtil
import com.cyberpunk.debttracker.util.ReminderManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repository: DebtRepository

    private var pendingDebts: List<Debt> = emptyList()

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            saveBackupToUri(uri)
        } else {
            toast(R.string.backup_save_cancelled)
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            readBackup(uri)
        } else {
            toast(R.string.backup_import_cancelled)
        }
    }

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

        binding.btnExportJson.setOnClickListener {
            exportBackup()
        }

        binding.btnImportJson.setOnClickListener {
            importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        }
    }

    private fun exportBackup() {
        viewLifecycleOwner.lifecycleScope.launch {
            val debts = repository.getAllDebtsForExport()
            if (debts.isEmpty()) {
                toast(R.string.backup_no_data)
                return@launch
            }
            pendingDebts = debts
            exportLauncher.launch(JsonBackupUtil.getDefaultFileName())
        }
    }

    private fun saveBackupToUri(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val success = requireContext().contentResolver.openOutputStream(uri)?.use { os ->
                JsonBackupUtil.exportDebtsToJson(os, pendingDebts)
            } ?: false
            pendingDebts = emptyList()

            if (success) {
                toast(R.string.backup_export_success)
            } else {
                toast(R.string.backup_export_failed)
            }
        }
    }

    private fun readBackup(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val debts = try {
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    JsonBackupUtil.parseDebts(input)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (debts == null) {
                toast(R.string.backup_import_failed)
                return@launch
            }
            if (debts.isEmpty()) {
                toast(R.string.backup_file_empty)
                return@launch
            }

            confirmImport(debts)
        }
    }

    private fun confirmImport(debts: List<Debt>) {
        AlertDialog.Builder(requireContext(), R.style.Theme_DebtTracker_Dialog)
            .setTitle(getString(R.string.backup_import_title))
            .setMessage(getString(R.string.backup_import_message, debts.size))
            .setPositiveButton(getString(R.string.dialog_confirm)) { _, _ ->
                importDebts(debts)
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun importDebts(debts: List<Debt>) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.importAll(debts)
            toast(getString(R.string.backup_import_success, debts.size))
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun toast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
