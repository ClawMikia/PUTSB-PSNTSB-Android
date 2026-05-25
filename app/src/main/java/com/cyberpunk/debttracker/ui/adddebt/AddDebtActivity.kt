package com.cyberpunk.debttracker.ui.adddebt

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtType
import com.cyberpunk.debttracker.databinding.ActivityAddDebtBinding
import com.cyberpunk.debttracker.util.DateFormatter
import com.cyberpunk.debttracker.util.showCyberSnack
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AddDebtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDebtBinding
    private val viewModel: AddDebtViewModel by viewModels()

    private var existingDebt: Debt? = null
    private var selectedDueDateMs: Long? = null

    companion object {
        const val EXTRA_DEBT = "extra_debt"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDebtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        existingDebt = IntentCompat.getParcelableExtra(intent, EXTRA_DEBT, Debt::class.java)

        setupToolbar()
        setupClickListeners()
        populateForEdit()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvToolbarTitle.text = if (existingDebt != null)
            getString(R.string.edit_debt_title)
        else
            getString(R.string.add_debt_title)
    }

    private fun setupClickListeners() {
        binding.etDueDate.setOnClickListener { showDatePicker() }

        binding.btnSave.setOnClickListener {
            val debtType = when (binding.chipGroupType.checkedChipId) {
                R.id.chip_owes_me -> DebtType.OWES_ME
                else              -> DebtType.I_OWE
            }
            viewModel.saveDebt(
                existingDebt   = existingDebt,
                personName     = binding.etPersonName.text?.toString() ?: "",
                amountStr      = binding.etAmount.text?.toString() ?: "",
                description    = binding.etDescription.text?.toString() ?: "",
                debtType       = debtType,
                dueDateMs      = selectedDueDateMs,
            )
        }

        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun populateForEdit() {
        existingDebt?.let { debt ->
            binding.etPersonName.setText(debt.personName)
            binding.etAmount.setText(debt.amount.toString())
            binding.etDescription.setText(debt.description)

            when (debt.debtType) {
                DebtType.I_OWE   -> binding.chipGroupType.check(R.id.chip_i_owe)
                DebtType.OWES_ME -> binding.chipGroupType.check(R.id.chip_owes_me)
            }

            debt.dueDate?.let { ts ->
                selectedDueDateMs = ts
                binding.etDueDate.setText(DateFormatter.formatInput(ts))
            }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        selectedDueDateMs?.let { cal.timeInMillis = it }

        DatePickerDialog(
            this,
            R.style.Theme_DebtTracker_Dialog,
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day, 23, 59, 59)
                }
                selectedDueDateMs = selected.timeInMillis
                binding.etDueDate.setText(
                    DateFormatter.formatInput(selected.timeInMillis)
                )
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.result.collect { result ->
                    when (result) {
                        is AddDebtResult.Saved -> {
                            binding.root.showCyberSnack(getString(R.string.success_debt_added))
                            finish()
                        }
                        is AddDebtResult.Updated -> {
                            binding.root.showCyberSnack(getString(R.string.success_debt_updated))
                            finish()
                        }
                        is AddDebtResult.ValidationError -> {
                            when (result.field) {
                                "name" -> {
                                    binding.tilPersonName.error = getString(R.string.error_name_required)
                                    binding.tilPersonName.requestFocus()
                                }
                                "amount_invalid" -> {
                                    binding.tilAmount.error = getString(R.string.error_amount_invalid)
                                    binding.tilAmount.requestFocus()
                                }
                                "amount_zero" -> {
                                    binding.tilAmount.error = getString(R.string.error_amount_zero)
                                    binding.tilAmount.requestFocus()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
