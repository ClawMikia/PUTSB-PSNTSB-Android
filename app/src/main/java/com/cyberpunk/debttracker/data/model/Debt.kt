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

    val amount: Double,

    @ColumnInfo(name = "paid_amount")
    val paidAmount: Double = 0.0,

    val description: String = "",

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    @ColumnInfo(name = "debt_type")
    val debtType: DebtType,

    val status: DebtStatus = DebtStatus.ACTIVE,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
) : Parcelable {

    val remaining: Double get() = amount - paidAmount
    val isSettled: Boolean get() = status == DebtStatus.SETTLED || paidAmount >= amount
    val isOverdue: Boolean
        get() = (dueDate != null) && (dueDate < System.currentTimeMillis()) && (status == DebtStatus.ACTIVE)

    val progressPercent: Int get() = if (amount > 0) ((paidAmount / amount) * 100).toInt() else 0
    val avatarLetter: String get() = if (personName.isNotEmpty()) personName.take(1).uppercase() else "?"
}

enum class DebtType {
    I_OWE,
    OWES_ME
}

enum class DebtStatus {
    ACTIVE,
    PARTIAL,
    SETTLED
}
