package com.cyberpunk.debttracker.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "person_name")
    val personName: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "paid_amount")
    val paidAmount: Double = 0.0,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "debt_type")
    val debtType: DebtType,

    @ColumnInfo(name = "status")
    val status: DebtStatus = DebtStatus.ACTIVE,

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {

    val remaining: Double get() = (amount - paidAmount).coerceAtLeast(0.0)
    val progressPercent: Int get() = if (amount <= 0) 0 else ((paidAmount / amount) * 100).toInt().coerceIn(0, 100)
    val isOverdue: Boolean get() = dueDate != null && dueDate < System.currentTimeMillis() && status == DebtStatus.ACTIVE
    val isSettled: Boolean get() = status == DebtStatus.SETTLED || paidAmount >= amount
    val avatarLetter: String get() = personName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

enum class DebtType {
    I_OWE,   // I owe someone money
    OWES_ME  // Someone owes me money
}

enum class DebtStatus {
    ACTIVE,
    PARTIAL,
    SETTLED,
    OVERDUE
}
