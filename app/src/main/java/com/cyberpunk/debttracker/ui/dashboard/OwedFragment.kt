package com.cyberpunk.debttracker.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.repository.SortOrder
import com.cyberpunk.debttracker.databinding.FragmentOwedBinding
import com.cyberpunk.debttracker.ui.debtdetail.DebtDetailActivity
import com.cyberpunk.debttracker.util.gone
import com.cyberpunk.debttracker.util.toCurrencyString
import com.cyberpunk.debttracker.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OwedFragment : Fragment() {

    private var _binding: FragmentOwedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DebtViewModel by activityViewModels()
    private lateinit var adapter: DebtAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnSort.setOnClickListener { showSortMenu(it) }
    }

    private fun showSortMenu(anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.menu_debt_list, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.sort_date_newest   -> viewModel.setSortOrder(SortOrder.DATE_NEWEST)
                    R.id.sort_date_oldest   -> viewModel.setSortOrder(SortOrder.DATE_OLDEST)
                    R.id.sort_amount_high   -> viewModel.setSortOrder(SortOrder.AMOUNT_HIGH)
                    R.id.sort_amount_low    -> viewModel.setSortOrder(SortOrder.AMOUNT_LOW)
                    R.id.sort_name_az       -> viewModel.setSortOrder(SortOrder.NAME_AZ)
                    R.id.sort_overdue_first -> viewModel.setSortOrder(SortOrder.OVERDUE_FIRST)
                }
                true
            }
            show()
        }
    }

    private fun setupRecycler() {
        adapter = DebtAdapter { debt ->
            startActivity(
                Intent(requireContext(), DebtDetailActivity::class.java).apply {
                    putExtra(DebtDetailActivity.EXTRA_DEBT, debt)
                }
            )
        }
        binding.recyclerDebts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@OwedFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.owedDebts.collect { debts ->
                        adapter.submitList(debts)
                        if (debts.isEmpty()) {
                            binding.layoutEmpty.visible()
                            binding.recyclerDebts.gone()
                        } else {
                            binding.layoutEmpty.gone()
                            binding.recyclerDebts.visible()
                        }
                    }
                }

                launch {
                    viewModel.totalOwed.collect { total ->
                        binding.tvTotal.text = getString(R.string.total_amount_label, total.toCurrencyString())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
