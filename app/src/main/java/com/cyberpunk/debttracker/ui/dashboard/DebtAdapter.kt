package com.cyberpunk.debttracker.ui.dashboard

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import com.cyberpunk.debttracker.databinding.ItemDebtBinding
import com.cyberpunk.debttracker.util.*

class DebtAdapter(
    private val onItemClick: (Debt) -> Unit,
) : ListAdapter<Debt, DebtAdapter.DebtViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val binding = ItemDebtBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DebtViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DebtViewHolder(
        private val binding: ItemDebtBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(debt: Debt) {
            val ctx = binding.root.context

            // Avatar
            val avatarColor = debt.getAvatarBgColor(ctx)
            binding.tvAvatar.text = debt.avatarLetter
            (binding.tvAvatar.background as? GradientDrawable)?.setColor(avatarColor)
                ?: binding.tvAvatar.setAvatarBackground(avatarColor)

            // Left stripe color
            binding.viewTypeStripe.setBackgroundColor(debt.getTypeColor(ctx))

            // Person name
            binding.tvPersonName.text = debt.personName

            // Description
            if (debt.description.isNotBlank()) {
                binding.tvDescription.text = debt.description
                binding.tvDescription.visible()
            } else {
                binding.tvDescription.gone()
            }

            // Due date
            debt.dueDate?.let { dueTs ->
                binding.tvDueDate.text = DateFormatter.dueDateLabel(dueTs)
                binding.tvDueDate.visible()
                binding.tvDueDate.setTextColor(
                    if (DateFormatter.isOverdue(dueTs))
                        ContextCompat.getColor(ctx, R.color.neon_red_alert)
                    else
                        ContextCompat.getColor(ctx, R.color.text_tertiary)
                )
            } ?: run { binding.tvDueDate.gone() }

            // Amount
            binding.tvAmount.text = debt.amount.toCurrencyString()
            binding.tvAmount.setTextColor(
                when (debt.debtType) {
                    DebtType.I_OWE   -> ContextCompat.getColor(ctx, R.color.debt_owed)
                    DebtType.OWES_ME -> ContextCompat.getColor(ctx, R.color.debt_lent)
                }
            )

            // Status badge
            val statusLabel = debt.getStatusLabel()
            binding.tvStatusBadge.text = statusLabel
            val (badgeBg, badgeTextColor) = when {
                debt.isSettled -> R.drawable.bg_status_badge to R.color.debt_settled
                debt.isOverdue -> R.drawable.bg_status_badge_red to R.color.neon_red_alert
                debt.status == DebtStatus.PARTIAL ->
                    R.drawable.bg_status_badge_orange to R.color.debt_partial
                else -> R.drawable.bg_status_badge_green to R.color.neon_green_ok
            }
            binding.tvStatusBadge.setBackgroundResource(badgeBg)
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, badgeTextColor))

            // Card border color by type
            binding.cardRoot.strokeColor = when (debt.debtType) {
                DebtType.I_OWE   -> ContextCompat.getColor(ctx, R.color.debt_owed_dim)
                DebtType.OWES_ME -> ContextCompat.getColor(ctx, R.color.debt_lent_dim)
            }

            // Click
            binding.root.setOnClickListener {
                it.animateScalePop(150)
                onItemClick(debt)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Debt>() {
            override fun areItemsTheSame(old: Debt, new: Debt) = old.id == new.id
            override fun areContentsTheSame(old: Debt, new: Debt) = old == new
        }
    }
}
