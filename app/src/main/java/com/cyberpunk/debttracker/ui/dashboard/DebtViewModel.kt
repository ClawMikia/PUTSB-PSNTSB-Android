package com.cyberpunk.debttracker.ui.dashboard

import androidx.lifecycle.*
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import com.cyberpunk.debttracker.data.repository.DebtRepository
import com.cyberpunk.debttracker.data.repository.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val repository: DebtRepository,
) : ViewModel() {

    // ─── Sort state ───────────────────────────────────────────────────────────

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_NEWEST)

    // ─── All debts (sorted) ───────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    val allDebts: StateFlow<List<Debt>> = _sortOrder
        .flatMapLatest { sort -> repository.getAllSorted(sort) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Filtered views ───────────────────────────────────────────────────────

    val owedDebts: StateFlow<List<Debt>> = repository.getActiveOwed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lentDebts: StateFlow<List<Debt>> = repository.getActiveLent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Summary figures ──────────────────────────────────────────────────────

    val totalOwed: StateFlow<Double> = repository.getTotalOwed()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLent: StateFlow<Double> = repository.getTotalLent()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netBalance: StateFlow<Double> = combine(totalLent, totalOwed) { lent, owed ->
        lent - owed
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activeCount: StateFlow<Int> = repository.getActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overdueCount: StateFlow<Int> = repository.getOverdueCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val settledCount: StateFlow<Int> = repository.getSettledCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount: StateFlow<Int> = repository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ─── Actions ──────────────────────────────────────────────────────────────

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    // ─── Analytics data ───────────────────────────────────────────────────────

    suspend fun getTopOwedContacts() = repository.getTopOwedContacts()
    suspend fun getTopLentContacts() = repository.getTopLentContacts()
    suspend fun getDebtsFrom(from: Long) = repository.getDebtsFrom(from)
}
