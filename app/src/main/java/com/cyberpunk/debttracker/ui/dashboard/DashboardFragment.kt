package com.cyberpunk.debttracker.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.repository.SortOrder
import com.cyberpunk.debttracker.databinding.FragmentDashboardBinding
import com.cyberpunk.debttracker.ui.debtdetail.DebtDetailActivity
import com.cyberpunk.debttracker.util.gone
import com.cyberpunk.debttracker.util.toCurrencyString
import com.cyberpunk.debttracker.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DebtViewModel by activityViewModels()
    private lateinit var debtAdapter: DebtAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecycler() {
        debtAdapter = DebtAdapter { debt ->
            startActivity(
                Intent(requireContext(), DebtDetailActivity::class.java).apply {
                    putExtra(DebtDetailActivity.EXTRA_DEBT, debt)
                }
            )
        }
        binding.recyclerDebts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = debtAdapter
            setHasFixedSize(false)
        }
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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.allDebts.collect { debts ->
                        debtAdapter.submitList(debts)
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
                    viewModel.netBalance.collect { net ->
                        val symbol = if (net >= 0) "+" else ""
                        binding.tvNetBalance.text = getString(R.string.net_balance_format, symbol, net.toCurrencyString())
                        binding.tvNetBalance.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                if (net >= 0) R.color.debt_lent else R.color.debt_owed
                            )
                        )
                    }
                }

                launch {
                    viewModel.totalOwed.collect { owed ->
                        binding.tvTotalOwe.text = owed.toCurrencyString()
                    }
                }

                launch {
                    viewModel.totalLent.collect { lent ->
                        binding.tvTotalLent.text = lent.toCurrencyString()
                    }
                }

                launch {
                    viewModel.activeCount.collect { count ->
                        binding.tvActiveCount.text = getString(R.string.active_nodes_count, count)
                    }
                }

                launch {
                    viewModel.overdueCount.collect { count ->
                        if (count > 0) {
                            binding.tvOverdueCount.text = getString(R.string.overdue_nodes_count, count)
                            binding.tvOverdueCount.visible()
                        } else {
                            binding.tvOverdueCount.gone()
                        }
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
