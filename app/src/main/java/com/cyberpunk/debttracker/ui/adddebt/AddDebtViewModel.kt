package com.cyberpunk.debttracker.ui.adddebt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import com.cyberpunk.debttracker.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddDebtViewModel @Inject constructor(
    private val repository: DebtRepository
) : ViewModel() {

    private val _result = MutableSharedFlow<AddDebtResult>()
    val result: SharedFlow<AddDebtResult> = _result

    fun saveDebt(
        existingDebt: Debt?,
        personName: String,
        amountStr: String,
        description: String,
        debtType: DebtType,
        dueDateMs: Long?
    ) = viewModelScope.launch {

        // Validation
        val name = personName.trim()
        if (name.isEmpty()) {
            _result.emit(AddDebtResult.ValidationError("name"))
            return@launch
        }

        val amount = amountStr.trim().toDoubleOrNull()
        if (amount == null) {
            _result.emit(AddDebtResult.ValidationError("amount_invalid"))
            return@launch
        }
        if (amount <= 0) {
            _result.emit(AddDebtResult.ValidationError("amount_zero"))
            return@launch
        }

        if (existingDebt != null) {
            // Update mode — preserve paidAmount and status unless amount changed
            val newStatus = when {
                existingDebt.paidAmount >= amount -> DebtStatus.SETTLED
                existingDebt.paidAmount > 0       -> DebtStatus.PARTIAL
                else                              -> DebtStatus.ACTIVE
            }
            val updated = existingDebt.copy(
                personName  = name,
                amount      = amount,
                description = description.trim(),
                debtType    = debtType,
                dueDate     = dueDateMs,
                status      = newStatus,
                updatedAt   = System.currentTimeMillis()
            )
            repository.update(updated)
            _result.emit(AddDebtResult.Updated)
        } else {
            // Insert mode
            val debt = Debt(
                personName  = name,
                amount      = amount,
                description = description.trim(),
                debtType    = debtType,
                dueDate     = dueDateMs,
                status      = DebtStatus.ACTIVE
            )
            repository.insert(debt)
            _result.emit(AddDebtResult.Saved)
        }
    }
}

sealed class AddDebtResult {
    object Saved : AddDebtResult()
    object Updated : AddDebtResult()
    data class ValidationError(val field: String) : AddDebtResult()
}
