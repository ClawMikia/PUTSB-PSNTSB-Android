package com.cyberpunk.debttracker.ui.debtdetail

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import com.cyberpunk.debttracker.databinding.ActivityDebtDetailBinding
import com.cyberpunk.debttracker.ui.adddebt.AddDebtActivity
import com.cyberpunk.debttracker.util.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DebtDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebtDetailBinding
    private val viewModel: DebtDetailViewModel by viewModels()

    private var debt: Debt? = null

    companion object {
        const val EXTRA_DEBT = "extra_debt"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        debt = IntentCompat.getParcelableExtra(intent, EXTRA_DEBT, Debt::class.java)

        setupToolbar()
        renderDebt()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnEdit.setOnClickListener {
            startActivity(
                Intent(this, AddDebtActivity::class.java).apply {
                    putExtra(AddDebtActivity.EXTRA_DEBT, debt)
                }
            )
        }
        binding.btnDelete.setOnClickListener { showDeleteDialog() }
    }

    private fun renderDebt() {
        val d = debt ?: return

        // Avatar circle
        val avatarColor = d.getAvatarBgColor(this)
        binding.tvAvatarDetail.text = d.avatarLetter
        val oval = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(avatarColor)
        }
        binding.tvAvatarDetail.background = oval

        // Person name & type
        binding.tvPersonName.text = d.personName
        binding.tvTypeLabel.text = when (d.debtType) {
            DebtType.I_OWE   -> "YOU OWE THIS PERSON"
            DebtType.OWES_ME -> "THIS PERSON OWES YOU"
        }
        binding.tvTypeLabel.setTextColor(d.getTypeColor(this))

        // Amount
        binding.tvAmount.text = d.amount.toCurrencyString()
        binding.tvAmount.setTextColor(
            when (d.debtType) {
                DebtType.I_OWE   -> ContextCompat.getColor(this, R.color.debt_owed)
                DebtType.OWES_ME -> ContextCompat.getColor(this, R.color.debt_lent)
            }
        )

        // Status
        val statusLabel = d.getStatusLabel()
        binding.tvStatus.text = statusLabel
        val (bgRes, textColorRes) = when {
            d.isSettled  -> R.drawable.bg_status_badge     to R.color.debt_settled
            d.isOverdue  -> R.drawable.bg_status_badge_red to R.color.neon_red_alert
            d.status == DebtStatus.PARTIAL -> R.drawable.bg_status_badge_orange to R.color.debt_partial
            else         -> R.drawable.bg_status_badge_green to R.color.neon_green_ok
        }
        binding.tvStatus.setBackgroundResource(bgRes)
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, textColorRes))

        // Progress
        binding.progressPayment.progress = d.progressPercent
        binding.tvProgressPct.text = getString(R.string.progress_percent, d.progressPercent)
        binding.tvPaidAmount.text = d.paidAmount.toCurrencyString()
        binding.tvRemaining.text = d.remaining.toCurrencyString()

        // Info
        binding.tvDescription.text = d.description.ifBlank { "—" }
        binding.tvCreatedDate.text = DateFormatter.formatDisplay(d.createdAt)

        d.dueDate?.let { dueTs ->
            binding.rowDueDate.visible()
            binding.tvDueDate.text = DateFormatter.dueDateLabel(dueTs)
            binding.tvDueDate.setTextColor(
                if (DateFormatter.isOverdue(dueTs))
                    ContextCompat.getColor(this, R.color.neon_red_alert)
                else
                    ContextCompat.getColor(this, R.color.text_primary)
            )
        } ?: run { binding.rowDueDate.gone() }

        // Hero card border
        binding.cardHero.strokeColor = when (d.debtType) {
            DebtType.I_OWE   -> ContextCompat.getColor(this, R.color.debt_owed_dim)
            DebtType.OWES_ME -> ContextCompat.getColor(this, R.color.debt_lent_dim)
        }

        // Disable action buttons if already settled
        if (d.isSettled) {
            binding.btnMarkSettled.isEnabled = false
            binding.btnPartialPay.isEnabled  = false
            binding.btnMarkSettled.alpha = 0.4f
            binding.btnPartialPay.alpha  = 0.4f
        }
    }

    private fun setupButtons() {
        binding.btnMarkSettled.setOnClickListener {
            showSettleDialog()
        }
        binding.btnPartialPay.setOnClickListener {
            showPartialPayDialog()
        }
        binding.btnDeleteDebt.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showSettleDialog() {
        AlertDialog.Builder(this, R.style.Theme_DebtTracker_Dialog)
            .setTitle(getString(R.string.dialog_settle_title))
            .setMessage(getString(R.string.dialog_settle_message))
            .setPositiveButton(getString(R.string.dialog_confirm)) { _, _ ->
                debt?.let { viewModel.markSettled(it) }
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun showPartialPayDialog() {
        val d = debt ?: return
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_partial_payment, null, false)
        val til = dialogView.findViewById<TextInputLayout>(R.id.til_payment)
        val et  = dialogView.findViewById<TextInputEditText>(R.id.et_payment)

        AlertDialog.Builder(this, R.style.Theme_DebtTracker_Dialog)
            .setTitle(getString(R.string.dialog_partial_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_confirm)) { _, _ ->
                val amount = et.text?.toString()?.toDoubleOrNull()
                if ((amount == null) || (amount <= 0)) {
                    til?.error = getString(R.string.error_amount_invalid)
                } else if (amount > d.remaining) {
                    til?.error = getString(R.string.error_payment_exceeds)
                } else {
                    viewModel.addPayment(d, amount)
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this, R.style.Theme_DebtTracker_Dialog)
            .setTitle(getString(R.string.dialog_delete_title))
            .setMessage(getString(R.string.dialog_delete_message))
            .setPositiveButton(getString(R.string.dialog_confirm)) { _, _ ->
                debt?.let { viewModel.delete(it) }
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is DetailEvent.Settled -> {
                            binding.root.showCyberSnack(getString(R.string.success_debt_settled))
                            finish()
                        }
                        is DetailEvent.PaymentRecorded -> {
                            binding.root.showCyberSnack(getString(R.string.success_payment_recorded))
                            finish()
                        }
                        is DetailEvent.Deleted -> {
                            binding.root.showCyberSnack(getString(R.string.success_debt_deleted))
                            finish()
                        }
                        is DetailEvent.Error -> {
                            binding.root.showCyberSnack(event.msg, isError = true)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-read debt from intent to catch edits
        debt = intent.getParcelableExtra(EXTRA_DEBT)
        renderDebt()
    }
}
