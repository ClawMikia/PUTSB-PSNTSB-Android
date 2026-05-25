package com.cyberpunk.debttracker.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cyberpunk.debttracker.data.db.ContactSummary
import com.cyberpunk.debttracker.databinding.ItemTopContactBinding
import com.cyberpunk.debttracker.util.setAvatarBackground
import com.cyberpunk.debttracker.util.toCurrencyString
import androidx.core.content.ContextCompat
import com.cyberpunk.debttracker.R

class TopContactAdapter :
    ListAdapter<ContactSummary, TopContactAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class ViewHolder(private val binding: ItemTopContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: ContactSummary, rank: Int) {
            val ctx = binding.root.context

            binding.tvRank.text = "#$rank"

            val initial = contact.person_name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            binding.tvAvatar.text = initial
            binding.tvAvatar.setAvatarBackground(
                ContextCompat.getColor(ctx, R.color.cyber_gold)
            )

            binding.tvName.text = contact.person_name
            binding.tvAmount.text = contact.total.toCurrencyString()
            binding.tvAmount.setTextColor(
                ContextCompat.getColor(ctx, R.color.cyber_gold)
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ContactSummary>() {
            override fun areItemsTheSame(a: ContactSummary, b: ContactSummary) =
                a.person_name == b.person_name
            override fun areContentsTheSame(a: ContactSummary, b: ContactSummary) = a == b
        }
    }
}
