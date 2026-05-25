package com.cyberpunk.debttracker.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import com.google.android.material.snackbar.Snackbar

// ─── View Extensions ──────────────────────────────────────────────────────────

fun View.visible() { visibility = View.VISIBLE }
fun View.gone()    { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.animateIn(duration: Long = 300L) {
    alpha = 0f
    visible()
    animate().alpha(1f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).start()
}

fun View.animateScalePop(duration: Long = 200L) {
    AnimatorSet().apply {
        playTogether(
            ObjectAnimator.ofFloat(this@animateScalePop, "scaleX", 0.9f, 1.05f, 1f),
            ObjectAnimator.ofFloat(this@animateScalePop, "scaleY", 0.9f, 1.05f, 1f)
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
    isSettled                                        -> "SETTLED"
    isOverdue                                        -> "OVERDUE"
    status == DebtStatus.PARTIAL && paidAmount > 0  -> "PARTIAL"
    else                                             -> "ACTIVE"
}

fun Debt.getStatusColor(context: Context): Int = when {
    isSettled  -> ContextCompat.getColor(context, R.color.debt_settled)
    isOverdue  -> ContextCompat.getColor(context, R.color.neon_red_alert)
    status == DebtStatus.PARTIAL -> ContextCompat.getColor(context, R.color.debt_partial)
    else       -> ContextCompat.getColor(context, R.color.neon_green_ok)
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

// ─── Fragment Extensions ──────────────────────────────────────────────────────

fun Fragment.snack(message: String, isError: Boolean = false) {
    view?.showCyberSnack(message, isError)
}

// ─── Number Extensions ────────────────────────────────────────────────────────

fun Double.toCurrencyString(): String = CurrencyFormatter.format(this)
fun Double.toCurrencyShort(): String = CurrencyFormatter.formatShort(this)
