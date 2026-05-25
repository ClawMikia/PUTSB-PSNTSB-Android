package com.cyberpunk.debttracker.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.databinding.FragmentAnalyticsBinding
import com.cyberpunk.debttracker.util.DateFormatter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DebtViewModel by activityViewModels()
    private lateinit var topContactAdapter: TopContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        setupRecycler()
        observeViewModel()
    }

    private fun setupCharts() {
        // Pie chart style
        binding.pieChart.apply {
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 55f
            transparentCircleRadius = 60f
            setDrawEntryLabels(false)
            legend.isEnabled = false
            description.isEnabled = false
            setUsePercentValues(true)
            isRotationEnabled = true
            setTouchEnabled(false)
        }

        // Bar chart style
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            axisRight.isEnabled = false
            xAxis.apply {
                setDrawGridLines(false)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_tertiary)
                textSize = 10f
                granularity = 1f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.divider_color)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_tertiary)
                textSize = 10f
                axisLineColor = Color.TRANSPARENT
            }
            setTouchEnabled(false)
        }
    }

    private fun setupRecycler() {
        topContactAdapter = TopContactAdapter()
        binding.recyclerTopContacts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topContactAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.totalCount.collect { total ->
                        binding.tvStatTotal.text = total.toString()
                    }
                }

                launch {
                    viewModel.settledCount.collect { settled ->
                        binding.tvStatSettled.text = settled.toString()
                    }
                }

                launch {
                    viewModel.allDebts.collect { _ ->
                        loadChartData()
                        loadTopContacts()
                    }
                }
            }
        }
    }

    private fun loadChartData() {
        val owedColor  = ContextCompat.getColor(requireContext(), R.color.debt_owed)
        val lentColor  = ContextCompat.getColor(requireContext(), R.color.debt_lent)
        val goldColor  = ContextCompat.getColor(requireContext(), R.color.cyber_gold)

        val owed = viewModel.totalOwed.value.toFloat()
        val lent = viewModel.totalLent.value.toFloat()

        // Pie chart
        val entries = mutableListOf<PieEntry>()
        if (owed > 0) entries.add(PieEntry(owed, "I Owe"))
        if (lent > 0) entries.add(PieEntry(lent, "Owes Me"))

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(owedColor, lentColor)
                sliceSpace = 3f
                selectionShift = 5f
                valueTextColor = Color.TRANSPARENT
            }
            binding.pieChart.data = PieData(dataSet)
            binding.pieChart.animateY(1000, Easing.EaseInOutQuad)
            binding.pieChart.invalidate()
        }

        // Bar chart: last 6 months debt added
        viewLifecycleOwner.lifecycleScope.launch {
            val sixMonthsAgo = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
            val debts = viewModel.getDebtsFrom(sixMonthsAgo)
            val monthlyMap = debts.groupBy { DateFormatter.monthLabel(it.createdAt) }
                .mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }

            val months = monthlyMap.keys.toList()
            val barEntries = months.mapIndexed { i, month ->
                BarEntry(i.toFloat(), monthlyMap[month] ?: 0f)
            }

            if (barEntries.isNotEmpty()) {
                val dataSet = BarDataSet(barEntries, "").apply {
                    color = goldColor
                    valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_tertiary)
                    valueTextSize = 9f
                }
                binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(months)
                binding.barChart.data = BarData(dataSet).apply { barWidth = 0.6f }
                binding.barChart.animateY(800)
                binding.barChart.invalidate()
            }
        }
    }

    private fun loadTopContacts() {
        viewLifecycleOwner.lifecycleScope.launch {
            val owedContacts = viewModel.getTopOwedContacts()
            val lentContacts = viewModel.getTopLentContacts()
            val all = (owedContacts + lentContacts)
                .asSequence()
                .sortedByDescending { it.total }
                .take(5)
                .toList()
            topContactAdapter.submitList(all)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
