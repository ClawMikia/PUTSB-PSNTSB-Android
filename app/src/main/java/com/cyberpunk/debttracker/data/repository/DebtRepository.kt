package com.cyberpunk.debttracker.data.repository

import com.cyberpunk.debttracker.data.db.ContactSummary
import com.cyberpunk.debttracker.data.db.DebtDao
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: DebtDao
) {

    // ─── Flows ─────────────────────────────────────────────────────────────────

    fun getAllDebts(): Flow<List<Debt>> = debtDao.getAllDebts()
    fun getActiveOwed(): Flow<List<Debt>> = debtDao.getActiveOwed()
    fun getActiveLent(): Flow<List<Debt>> = debtDao.getActiveLent()
    fun getTotalOwed(): Flow<Double?> = debtDao.getTotalOwed()
    fun getTotalLent(): Flow<Double?> = debtDao.getTotalLent()
    fun getActiveCount(): Flow<Int> = debtDao.getActiveCount()
    fun getOverdueCount(): Flow<Int> = debtDao.getOverdueCount()
    fun getSettledCount(): Flow<Int> = debtDao.getSettledCount()
    fun getTotalCount(): Flow<Int> = debtDao.getTotalCount()

    fun getAllSorted(sortOrder: SortOrder): Flow<List<Debt>> = when (sortOrder) {
        SortOrder.DATE_NEWEST -> debtDao.getAllSortedByDateDesc()
        SortOrder.DATE_OLDEST -> debtDao.getAllSortedByDateAsc()
        SortOrder.AMOUNT_HIGH -> debtDao.getAllSortedByAmountDesc()
        SortOrder.AMOUNT_LOW  -> debtDao.getAllSortedByAmountAsc()
        SortOrder.NAME_AZ     -> debtDao.getAllSortedByNameAsc()
        SortOrder.OVERDUE_FIRST -> debtDao.getAllSortedOverdueFirst()
    }

    // ─── Suspend ───────────────────────────────────────────────────────────────

    suspend fun insert(debt: Debt): Long = debtDao.insert(debt)

    suspend fun update(debt: Debt) = debtDao.update(
        debt.copy(updatedAt = System.currentTimeMillis())
    )

    suspend fun delete(debt: Debt) = debtDao.delete(debt)
    suspend fun deleteById(id: Long) = debtDao.deleteById(id)

    suspend fun getDebtById(id: Long): Debt? = debtDao.getDebtById(id)

    suspend fun markSettled(debt: Debt) {
        debtDao.update(
            debt.copy(
                paidAmount = debt.amount,
                status = DebtStatus.SETTLED,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addPayment(debt: Debt, payment: Double) {
        val newPaid = (debt.paidAmount + payment).coerceAtMost(debt.amount)
        val newStatus = if (newPaid >= debt.amount) DebtStatus.SETTLED else DebtStatus.PARTIAL
        debtDao.update(
            debt.copy(
                paidAmount = newPaid,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // ─── Analytics ────────────────────────────────────────────────────────────

    suspend fun getTopOwedContacts(): List<ContactSummary> =
        debtDao.getTopContactsByType(DebtType.I_OWE)

    suspend fun getTopLentContacts(): List<ContactSummary> =
        debtDao.getTopContactsByType(DebtType.OWES_ME)

    suspend fun getDebtsFrom(from: Long): List<Debt> =
        debtDao.getDebtsFrom(from)
}

enum class SortOrder {
    DATE_NEWEST,
    DATE_OLDEST,
    AMOUNT_HIGH,
    AMOUNT_LOW,
    NAME_AZ,
    OVERDUE_FIRST
}
