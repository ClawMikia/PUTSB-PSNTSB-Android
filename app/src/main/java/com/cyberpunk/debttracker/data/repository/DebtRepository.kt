package com.cyberpunk.debttracker.data.repository

import com.cyberpunk.debttracker.data.db.ContactSummary
import com.cyberpunk.debttracker.data.db.DebtDao
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import com.cyberpunk.debttracker.game.GameEngine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: DebtDao,
    private val game: GameEngine,
) {

    // ─── Flows ─────────────────────────────────────────────────────────────────

    fun getActiveOwed(): Flow<List<Debt>> = debtDao.getActiveOwed()
    fun getActiveLent(): Flow<List<Debt>> = debtDao.getActiveLent()
    fun getTotalOwed(): Flow<Double?> = debtDao.getTotalOwed()
    fun getTotalLent(): Flow<Double?> = debtDao.getTotalLent()
    fun getActiveCount(): Flow<Int> = debtDao.getActiveCount()
    fun getOverdueCount(): Flow<Int> = debtDao.getOverdueCount()
    fun getSettledCount(): Flow<Int> = debtDao.getSettledCount()
    fun getTotalCount(): Flow<Int> = debtDao.getTotalCount()
    fun getArchivedDebts(): Flow<List<Debt>> = debtDao.getArchivedDebts()

    fun getDebtById(id: Long): Flow<Debt?> = debtDao.getDebtByIdFlow(id)

    fun getAllSorted(sortOrder: SortOrder): Flow<List<Debt>> = when (sortOrder) {
        SortOrder.DATE_NEWEST -> debtDao.getAllSortedByDateDesc()
        SortOrder.DATE_OLDEST -> debtDao.getAllSortedByDateAsc()
        SortOrder.AMOUNT_HIGH -> debtDao.getAllSortedByAmountDesc()
        SortOrder.AMOUNT_LOW  -> debtDao.getAllSortedByAmountAsc()
        SortOrder.NAME_AZ     -> debtDao.getAllSortedByNameAsc()
        SortOrder.OVERDUE_FIRST -> debtDao.getAllSortedOverdueFirst()
    }

    // ─── Suspend ───────────────────────────────────────────────────────────────

    suspend fun insert(debt: Debt): Long {
        val id = debtDao.insert(debt)
        game.onDebtAdded(debt.copy(id = id))
        return id
    }

    suspend fun update(debt: Debt) {
        val wasSettled = debt.isSettled
        debtDao.update(debt.copy(updatedAt = System.currentTimeMillis()))
        if (!wasSettled && debt.isSettled) {
            game.onDebtSettled(debt)
        } else {
            game.syncNpcs()
        }
    }

    suspend fun delete(debt: Debt) {
        debtDao.delete(debt)
        game.onDebtDeleted(debt)
    }

    suspend fun archive(debt: Debt) {
        if (debt.isSettled) {
            debtDao.archiveDebt(debt.id)
            game.onDebtArchived(debt)
        }
    }

    suspend fun getAllDebtsForExport(): List<Debt> = debtDao.getAllDebtsForExport()

    suspend fun importAll(debts: List<Debt>) {
        debtDao.insertAll(debts)
        game.onImported(debts.size)
    }

    suspend fun markSettled(debt: Debt) {
        debtDao.update(
            debt.copy(
                paidAmount = debt.amount,
                status = DebtStatus.SETTLED,
                updatedAt = System.currentTimeMillis()
            )
        )
        game.onDebtSettled(debt)
    }

    suspend fun addPayment(debt: Debt, payment: Double) {
        val newPaid = (debt.paidAmount + payment).coerceAtMost(debt.amount)
        val settled = newPaid >= debt.amount
        debtDao.update(
            debt.copy(
                paidAmount = newPaid,
                status = if (settled) DebtStatus.SETTLED else DebtStatus.PARTIAL,
                updatedAt = System.currentTimeMillis()
            )
        )
        if (settled) game.onDebtSettled(debt) else game.onPaymentRecorded(debt, payment)
    }

    suspend fun exportSnapshot() = game.onExported(0)

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
