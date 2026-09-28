package com.cyberpunk.debttracker.util

import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.cyberpunk.debttracker.R

// ─── System bar insets ────────────────────────────────────────────────────────
//
//  targetSdk is 36, so Android 15+ forces edge-to-edge and the system bars are
//  drawn *over* the app window. Every screen therefore has to keep its own
//  content clear of the status/navigation bar, otherwise toolbars and buttons
//  end up underneath them.
//
//  The helpers below capture the view's original padding once and then add the
//  insets on top, so they are safe to call before or after the view has been
//  laid out, and they compose cleanly with layouts that already declare their
//  own padding.

/**
 * Opts this activity into edge-to-edge on *every* API level, not just the ones
 * where the platform forces it, so spacing is identical across devices.
 *
 * Call before `setContentView`, then pair with [applySystemBarInsets] on the
 * content root. The existing bar colors are reused as the pre-API-29 scrim so
 * older devices keep the exact look they have today.
 */
fun ComponentActivity.enableCyberEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(ContextCompat.getColor(this, R.color.status_bar_color)),
        navigationBarStyle = SystemBarStyle.dark(ContextCompat.getColor(this, R.color.nav_bar_color)),
    )
}

/**
 * Pads this view by the system bar (and display cutout) insets, preserving any
 * padding already declared in the layout.
 *
 * @param top keep clear of the status bar / notification bar.
 * @param bottom keep clear of the navigation bar / gesture handle.
 * @param start apply the left inset in landscape with a cutout or 3-button nav.
 * @param end apply the right inset in landscape with a cutout or 3-button nav.
 */
fun View.applySystemBarInsets(
    top: Boolean = true,
    bottom: Boolean = true,
    start: Boolean = true,
    end: Boolean = true,
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(
            left = initialLeft + if (start) bars.left else 0,
            top = initialTop + if (top) bars.top else 0,
            right = initialRight + if (end) bars.right else 0,
            bottom = initialBottom + if (bottom) bars.bottom else 0,
        )
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

/**
 * Same as [applySystemBarInsets] but also lifts the view above the soft keyboard,
 * for screens using `adjustResize` where the IME would otherwise cover inputs.
 */
fun View.applySystemBarAndImeInsets(
    top: Boolean = true,
    bottom: Boolean = true,
    start: Boolean = true,
    end: Boolean = true,
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
        view.updatePadding(
            left = initialLeft + if (start) bars.left else 0,
            top = initialTop + if (top) bars.top else 0,
            right = initialRight + if (end) bars.right else 0,
            bottom = initialBottom + if (bottom) maxOf(bars.bottom, ime.bottom) else 0,
        )
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

/**
 * Grows a container by the bottom inset instead of padding it, for hosts that
 * paint their own background edge-to-edge (bottom nav bars, toolbars).
 */
fun View.padBottomByNavBar() {
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(bottom = initialBottom + bars.bottom)
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

/**
 * Pushes the bottom edge of this view up by the navigation bar inset using a
 * margin, so scrolling content still reaches the true bottom of the window.
 */
fun View.marginBottomByNavBar() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = bars.bottom
        }
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

/** Requests insets now if attached, or as soon as it attaches. */
private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}
