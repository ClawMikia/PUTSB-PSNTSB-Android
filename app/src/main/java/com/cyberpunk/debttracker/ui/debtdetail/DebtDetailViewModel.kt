package com.cyberpunk.debttracker.ui.debtdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtDetailViewModel @Inject constructor(
    private val repository: DebtRepository
) : ViewModel() {

    private val _event = MutableSharedFlow<DetailEvent>()
    val event: SharedFlow<DetailEvent> = _event

    fun markSettled(debt: Debt) = viewModelScope.launch {
        repository.markSettled(debt)
        _event.emit(DetailEvent.Settled)
    }

    fun addPayment(debt: Debt, payment: Double) = viewModelScope.launch {
        if (payment <= 0 || payment > debt.remaining) {
            _event.emit(DetailEvent.Error("Invalid amount"))
            return@launch
        }
        repository.addPayment(debt, payment)
        _event.emit(DetailEvent.PaymentRecorded)
    }

    fun delete(debt: Debt) = viewModelScope.launch {
        repository.delete(debt)
        _event.emit(DetailEvent.Deleted)
    }
}

sealed class DetailEvent {
    object Settled         : DetailEvent()
    object PaymentRecorded : DetailEvent()
    object Deleted         : DetailEvent()
    data class Error(val msg: String) : DetailEvent()
}
