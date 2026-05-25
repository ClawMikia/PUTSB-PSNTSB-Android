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
    private val repository: DebtRepository
) : ViewModel() {

    // ─── Sort state ───────────────────────────────────────────────────────────

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

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

    // ─── UI events ────────────────────────────────────────────────────────────

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // ─── Actions ──────────────────────────────────────────────────────────────

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun insertDebt(debt: Debt) = viewModelScope.launch {
        repository.insert(debt)
        _uiEvent.emit(UiEvent.DebtAdded)
    }

    fun updateDebt(debt: Debt) = viewModelScope.launch {
        repository.update(debt)
        _uiEvent.emit(UiEvent.DebtUpdated)
    }

    fun deleteDebt(debt: Debt) = viewModelScope.launch {
        repository.delete(debt)
        _uiEvent.emit(UiEvent.DebtDeleted)
    }

    fun markSettled(debt: Debt) = viewModelScope.launch {
        repository.markSettled(debt)
        _uiEvent.emit(UiEvent.DebtSettled)
    }

    fun addPayment(debt: Debt, amount: Double) = viewModelScope.launch {
        if (amount <= 0 || amount > debt.remaining) {
            _uiEvent.emit(UiEvent.Error("Invalid payment amount"))
            return@launch
        }
        repository.addPayment(debt, amount)
        _uiEvent.emit(UiEvent.PaymentRecorded)
    }

    // ─── Analytics data ───────────────────────────────────────────────────────

    suspend fun getTopOwedContacts() = repository.getTopOwedContacts()
    suspend fun getTopLentContacts() = repository.getTopLentContacts()
    suspend fun getDebtsFrom(from: Long) = repository.getDebtsFrom(from)
}

sealed class UiEvent {
    object DebtAdded       : UiEvent()
    object DebtUpdated     : UiEvent()
    object DebtDeleted     : UiEvent()
    object DebtSettled     : UiEvent()
    object PaymentRecorded : UiEvent()
    data class Error(val message: String) : UiEvent()
}
