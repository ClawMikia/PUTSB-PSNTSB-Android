package com.cyberpunk.debttracker.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import com.google.android.material.snackbar.Snackbar

// ─── View Extensions ──────────────────────────────────────────────────────────

fun View.visible() { visibility = View.VISIBLE }
fun View.gone()    { visibility = View.GONE }

fun View.animateScalePop(duration: Long = 200L) {
    AnimatorSet().apply {
        playTogether(
            ObjectAnimator.ofFloat(this@animateScalePop, "scaleX", 0.9f, 1.05f, 1f),
            ObjectAnimator.ofFloat(this@animateScalePop, "scaleY", 0.9f, 1.05f, 1f),
        )
        this.duration = duration
        interpolator = OvershootInterpolator()
        start()
    }
}

fun View.showCyberSnack(message: String, isError: Boolean = false) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).apply {
        setBackgroundTint(
            if (isError) 0xFF1A0505.toInt() else 0xFF0D0D0D.toInt()
        )
        setTextColor(
            if (isError) 0xFFFF1744.toInt() else 0xFFFFD700.toInt()
        )
        show()
    }
}

// ─── Debt Extensions ──────────────────────────────────────────────────────────

fun Debt.getStatusLabel(): String = when {
    isSettled                                         -> "SETTLED"
    isOverdue                                         -> "OVERDUE"
    (status == DebtStatus.PARTIAL) && (paidAmount > 0) -> "PARTIAL"
    else                                              -> "ACTIVE"
}

fun Debt.getTypeColor(context: Context): Int = when (debtType) {
    DebtType.I_OWE   -> ContextCompat.getColor(context, R.color.debt_owed)
    DebtType.OWES_ME -> ContextCompat.getColor(context, R.color.debt_lent)
}

fun Debt.getAvatarBgColor(context: Context): Int = when (debtType) {
    DebtType.I_OWE   -> ContextCompat.getColor(context, R.color.debt_owed)
    DebtType.OWES_ME -> ContextCompat.getColor(context, R.color.debt_lent)
}

fun TextView.setAvatarBackground(color: Int) {
    val drawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }
    background = drawable
}

// ─── Number Extensions ────────────────────────────────────────────────────────

fun Double.toCurrencyString(): String = CurrencyFormatter.format(this)
