package com.cyberpunk.debttracker.data.db

import androidx.room.*
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtType
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    // ─── Insert / Update / Delete ──────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: Debt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(debts: List<Debt>)

    @Update
    suspend fun update(debt: Debt)

    @Delete
    suspend fun delete(debt: Debt)

    // ─── Queries ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    fun getDebtByIdFlow(id: Long): Flow<Debt?>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): Debt?

    @Query("SELECT * FROM debts WHERE debt_type = 'I_OWE' AND status != 'SETTLED' AND is_archived = 0 ORDER BY created_at DESC")
    fun getActiveOwed(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE debt_type = 'OWES_ME' AND status != 'SETTLED' AND is_archived = 0 ORDER BY created_at DESC")
    fun getActiveLent(): Flow<List<Debt>>

    // ─── Aggregates ────────────────────────────────────────────────────────────

    @Query("SELECT SUM(amount - paid_amount) FROM debts WHERE debt_type = 'I_OWE' AND status != 'SETTLED' AND is_archived = 0")
    fun getTotalOwed(): Flow<Double?>

    @Query("SELECT SUM(amount - paid_amount) FROM debts WHERE debt_type = 'OWES_ME' AND status != 'SETTLED' AND is_archived = 0")
    fun getTotalLent(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM debts WHERE status != 'SETTLED' AND is_archived = 0")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM debts WHERE due_date < :now AND status = 'ACTIVE' AND is_archived = 0")
    fun getOverdueCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM debts WHERE status = 'SETTLED' AND is_archived = 0")
    fun getSettledCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM debts WHERE is_archived = 0")
    fun getTotalCount(): Flow<Int>

    // ─── Sort Variants ─────────────────────────────────────────────────────────

    @Query("SELECT * FROM debts WHERE is_archived = 0 ORDER BY created_at DESC")
    fun getAllSortedByDateDesc(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE is_archived = 0 ORDER BY created_at ASC")
    fun getAllSortedByDateAsc(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE is_archived = 0 ORDER BY amount DESC")
    fun getAllSortedByAmountDesc(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE is_archived = 0 ORDER BY amount ASC")
    fun getAllSortedByAmountAsc(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE is_archived = 0 ORDER BY person_name ASC")
    fun getAllSortedByNameAsc(): Flow<List<Debt>>

    @Query(
        """
        SELECT * FROM debts 
        WHERE is_archived = 0
        ORDER BY 
            CASE WHEN due_date < :now AND status = 'ACTIVE' THEN 0 ELSE 1 END,
            created_at DESC
        """,
    )
    fun getAllSortedOverdueFirst(now: Long = System.currentTimeMillis()): Flow<List<Debt>>

    // ─── Analytics ────────────────────────────────────────────────────────────

    @Query(
        """
        SELECT person_name, SUM(amount - paid_amount) as total
        FROM debts
        WHERE debt_type = :type AND status != 'SETTLED' AND is_archived = 0
        GROUP BY person_name
        ORDER BY total DESC
        LIMIT 5
        """,
    )
    suspend fun getTopContactsByType(type: DebtType): List<ContactSummary>

    @Query("SELECT * FROM debts WHERE created_at >= :from AND is_archived = 0 ORDER BY created_at ASC")
    suspend fun getDebtsFrom(from: Long): List<Debt>

    @Query("SELECT * FROM debts WHERE is_archived = 1 ORDER BY updated_at DESC")
    fun getArchivedDebts(): Flow<List<Debt>>

    @Query("UPDATE debts SET is_archived = 1 WHERE id = :id")
    suspend fun archiveDebt(id: Long)

    @Query("SELECT * FROM debts")
    suspend fun getAllDebtsForExport(): List<Debt>
}

data class ContactSummary(
    @ColumnInfo(name = "person_name")
    val personName: String,
    val total: Double,
)
