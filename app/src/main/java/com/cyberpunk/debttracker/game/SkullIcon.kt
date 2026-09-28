package com.cyberpunk.debttracker.game

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders a [SkullDesign] as a pure-white skull with hollow sockets.
 * Used for every penalty in the game. Drawn in a 100x100 unit space.
 */
class SkullIcon(val design: SkullDesign) : Drawable() {

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val rect = RectF()
    private val mtx = Matrix()

    private val top = design.craniumTop
    private val craniumH = design.craniumHeight
    private val midY = top + craniumH
    private val chinY = design.jawBottom
    private val cw = design.craniumWidth
    private val jw = design.jawWidth
    private val cy = 50f

    private val skullPath = Path()
    private val socketPath = Path()
    private val accPath = Path()

    private var alphaValue = 255
    private var filterValue: ColorFilter? = null

    init {
        buildSkull()
        stroke.strokeWidth = 1.8f
    }

    private fun buildSkull() {
        skullPath.reset()
        val cheekY = midY + craniumH * 0.9f
        skullPath.moveTo(cy, top)
        // right dome
        skullPath.cubicTo(cy + cw * 0.66f, top, cy + cw, top + craniumH * 0.26f, cy + cw, midY)
        // right cheek down to jaw
        skullPath.cubicTo(
            cy + cw * 1.02f, midY + craniumH * 0.46f,
            cy + jw * 1.1f, cheekY - craniumH * 0.1f,
            cy + jw, cheekY,
        )
        // jaw to chin
        skullPath.cubicTo(cy + jw * 0.74f, chinY, cy + jw * 0.36f, chinY, cy, chinY)
        // mirror
        skullPath.cubicTo(cy - jw * 0.36f, chinY, cy - jw * 0.74f, chinY, cy - jw, cheekY)
        skullPath.cubicTo(
            cy - jw * 1.1f, cheekY - craniumH * 0.1f,
            cy - cw * 1.02f, midY + craniumH * 0.46f,
            cy - cw, midY,
        )
        skullPath.cubicTo(cy - cw, top + craniumH * 0.26f, cy - cw * 0.66f, top, cy, top)
        skullPath.close()
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return

        val layer = canvas.saveLayerAlpha(
            b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat(), alphaValue,
        )
        canvas.save()
        canvas.translate(b.left.toFloat(), b.top.toFloat())
        canvas.scale(b.width() / 100f, b.height() / 100f)

        fill.colorFilter = filterValue
        stroke.colorFilter = filterValue

        drawBehindAccessory(canvas)
        drawCranium(canvas)
        drawSockets(canvas)
        drawNose(canvas)
        drawTeeth(canvas)
        drawCrack(canvas)
        drawFrontAccessory(canvas)

        canvas.restore()
        canvas.restoreToCount(layer)
    }

    override fun setAlpha(alpha: Int) {
        alphaValue = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        filterValue = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Drawable")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    // ─── Layers ───────────────────────────────────────────────────────────────

    private fun drawCranium(canvas: Canvas) {
        fill.shader = LinearGradient(
            cy, top, cy, chinY, design.boneTint, design.boneShade, Shader.TileMode.CLAMP,
        )
        fill.style = Paint.Style.FILL
        canvas.drawPath(skullPath, fill)
        fill.shader = null

        // Temporal nubs give the silhouette its skull read.
        fill.color = design.boneTint
        val nubY = midY + craniumH * 0.16f
        canvas.drawCircle(cy - cw * 0.98f, nubY, craniumH * 0.17f, fill)
        canvas.drawCircle(cy + cw * 0.98f, nubY, craniumH * 0.17f, fill)

        // Brow ridge.
        stroke.color = withAlpha(darken(design.boneShade, 0.86f), 190)
        stroke.strokeWidth = design.browThickness
        rect.set(cy - cw * 0.78f, design.socketY - craniumH * 0.5f, cy + cw * 0.78f, design.socketY - craniumH * 0.24f)
        canvas.drawArc(rect, 200f, 140f, false, stroke)

        // Cheekbone shading.
        fill.color = withAlpha(darken(design.boneShade, 0.9f), 120)
        for (side in intArrayOf(-1, 1)) {
            accPath.reset()
            val bx = cy + side * cw * 0.62f
            accPath.moveTo(bx, design.socketY + craniumH * 0.24f)
            accPath.lineTo(bx + side * cw * 0.3f, design.socketY + craniumH * 0.5f)
            accPath.lineTo(bx + side * cw * 0.16f, midY + craniumH * 0.78f)
            accPath.close()
            canvas.drawPath(accPath, fill)
        }
    }

    private fun drawSockets(canvas: Canvas) {
        val sy = design.socketY
        val w = design.socketWidth
        val h = design.socketHeight
        for (side in intArrayOf(-1, 1)) {
            val ex = cy + side * design.socketSpread
            buildSocket(accPath, ex, sy, w, h, side)
            fill.color = SKULL_HOLLOW
            canvas.drawPath(accPath, fill)

            // Inner bone lip so the socket reads on any background.
            stroke.color = withAlpha(darken(design.boneShade, 0.82f), 200)
            stroke.strokeWidth = 1.2f
            canvas.drawPath(accPath, stroke)

            if (design.socketShape == SocketShape.HOLLOW_CROSS) {
                stroke.color = withAlpha(design.boneTint, 235)
                stroke.strokeWidth = 1.9f
                canvas.drawLine(ex - w * 0.6f, sy, ex + w * 0.6f, sy, stroke)
                canvas.drawLine(ex, sy - h * 0.6f, ex, sy + h * 0.6f, stroke)
            }
        }
    }

    private fun buildSocket(out: Path, x: Float, y: Float, w: Float, h: Float, side: Int) {
        out.reset()
        when (design.socketShape) {
            SocketShape.ROUND -> {
                rect.set(x - w, y - h, x + w, y + h)
                out.addOval(rect, Path.Direction.CW)
            }
            SocketShape.ANGULAR -> {
                out.moveTo(x - w, y - h * 0.35f)
                out.lineTo(x - w * 0.45f, y - h)
                out.lineTo(x + w * 0.45f, y - h)
                out.lineTo(x + w, y - h * 0.35f)
                out.lineTo(x + w * 0.7f, y + h * 0.7f)
                out.lineTo(x - w * 0.7f, y + h * 0.7f)
                out.close()
            }
            SocketShape.SLANTED -> {
                rect.set(x - w, y - h, x + w, y + h)
                out.addOval(rect, Path.Direction.CW)
                mtx.reset()
                mtx.setRotate(design.socketTilt * side, x, y)
                out.transform(mtx, socketPath)
                out.set(socketPath)
            }
            SocketShape.SQUARE -> {
                rect.set(x - w * 0.92f, y - h * 0.92f, x + w * 0.92f, y + h * 0.92f)
                out.addRoundRect(rect, w * 0.36f, w * 0.36f, Path.Direction.CW)
            }
            SocketShape.HOLLOW_CROSS -> {
                rect.set(x - w, y - h, x + w, y + h)
                out.addOval(rect, Path.Direction.CW)
                mtx.reset()
                mtx.setRotate(design.socketTilt * side, x, y)
                out.transform(mtx, socketPath)
                out.set(socketPath)
            }
        }
    }

    private fun drawNose(canvas: Canvas) {
        val ny = design.socketY + design.socketHeight + craniumH * 0.34f
        val nw = design.socketWidth * 0.52f
        val nh = design.socketHeight * 0.72f
        accPath.reset()
        when (design.noseShape) {
            NoseShape2.TRIANGLE -> {
                accPath.moveTo(cy, ny + nh)
                accPath.lineTo(cy - nw, ny - nh * 0.6f)
                accPath.quadTo(cy, ny - nh * 0.1f, cy + nw, ny - nh * 0.6f)
                accPath.close()
            }
            NoseShape2.HEART -> {
                accPath.moveTo(cy, ny + nh)
                accPath.cubicTo(cy - nw * 1.7f, ny - nh * 0.2f, cy - nw * 0.8f, ny - nh * 1.1f, cy, ny - nh * 0.25f)
                accPath.cubicTo(cy + nw * 0.8f, ny - nh * 1.1f, cy + nw * 1.7f, ny - nh * 0.2f, cy, ny + nh)
                accPath.close()
            }
            NoseShape2.OVAL -> {
                rect.set(cy - nw, ny - nh, cy + nw, ny + nh)
                accPath.addOval(rect, Path.Direction.CW)
            }
            NoseShape2.FLAME -> {
                accPath.moveTo(cy, ny + nh)
                accPath.cubicTo(cy - nw * 1.3f, ny, cy - nw * 0.9f, ny - nh * 0.9f, cy - nw * 0.2f, ny - nh * 1.2f)
                accPath.cubicTo(cy + nw * 0.5f, ny - nh * 0.6f, cy + nw * 0.4f, ny - nh * 0.3f, cy, ny + nh)
                accPath.close()
            }
        }
        fill.color = SKULL_HOLLOW
        canvas.drawPath(accPath, fill)
    }

    private fun drawTeeth(canvas: Canvas) {
        val topY = design.socketY + design.socketHeight + craniumH * 1.02f
        val botY = chinY - 2.2f
        if (botY <= topY) return
        val halfW = jw * 0.72f
        val n = design.teethCount

        stroke.color = withAlpha(darken(design.boneShade, 0.74f), 220)
        stroke.strokeWidth = design.teethWidth
        for (i in 1 until n) {
            val x = cy - halfW + (2 * halfW) * i / n
            canvas.drawLine(x, topY, x, botY, stroke)
        }
        stroke.strokeWidth = design.teethWidth * 1.15f
        canvas.drawLine(cy - halfW, topY, cy + halfW, topY, stroke)
    }

    private fun drawCrack(canvas: Canvas) {
        if (design.crackDepth <= 0f) return
        stroke.color = withAlpha(darken(design.boneShade, 0.62f), 210)
        stroke.strokeWidth = 1.5f
        val rng = Rng(seedHash(design.name) xor 0xA5A5A5A5L)
        accPath.reset()
        val startSide = if (rng.chance(0.5f)) -1 else 1
        var px = cy + startSide * cw * 0.82f
        var py = top + craniumH * 0.24f
        accPath.moveTo(px, py)
        var dx = -startSide * design.crackDepth * 0.4f
        var dy = design.crackDepth * 0.5f
        repeat(3) {
            px += dx + rng.nextFloat(-2.5f, 2.5f)
            py += dy + rng.nextFloat(-1.2f, 1.2f)
            accPath.lineTo(px, py)
        }
        canvas.drawPath(accPath, stroke)
    }

    // ─── Accessories ──────────────────────────────────────────────────────────

    private fun drawBehindAccessory(canvas: Canvas) {
        when (design.accessory) {
            SkullAccessory.HALO -> {
                stroke.color = design.accent
                stroke.strokeWidth = 3f
                rect.set(cy - cw * 0.5f, top - craniumH * 0.5f, cy + cw * 0.5f, top - craniumH * 0.1f)
                canvas.drawOval(rect, stroke)
                fill.color = withAlpha(design.accent, 55)
                canvas.drawOval(rect, fill)
            }
            SkullAccessory.HORNS -> {
                fill.color = design.boneTint
                for (side in intArrayOf(-1, 1)) {
                    accPath.reset()
                    val bx = cy + side * cw * 0.72f
                    val by = top + craniumH * 0.36f
                    accPath.moveTo(bx, by)
                    accPath.cubicTo(
                        bx + side * craniumH * 0.6f, by - craniumH * 0.1f,
                        bx + side * craniumH * 0.5f, by - craniumH * 0.8f,
                        bx - side * craniumH * 0.06f, by - craniumH * 0.86f,
                    )
                    accPath.cubicTo(
                        bx + side * craniumH * 0.06f, by - craniumH * 0.5f,
                        bx - side * craniumH * 0.02f, by - craniumH * 0.2f,
                        bx, by,
                    )
                    accPath.close()
                    canvas.drawPath(accPath, fill)
                }
            }
            SkullAccessory.SPIKES -> {
                fill.color = design.boneTint
                for (i in -2..2) {
                    val bx = cy + i * cw * 0.34f
                    val h = (1f - kotlin.math.abs(i) / 2.6f) * craniumH * 0.6f
                    accPath.reset()
                    accPath.moveTo(bx - cw * 0.09f, top + craniumH * 0.16f)
                    accPath.lineTo(bx, top - h)
                    accPath.lineTo(bx + cw * 0.09f, top + craniumH * 0.16f)
                    accPath.close()
                    canvas.drawPath(accPath, fill)
                }
            }
            SkullAccessory.BONE_CROWN -> {
                fill.color = design.boneTint
                for (i in 0 until 7) {
                    val a = Math.toRadians(200.0 + i * 23.0)
                    val bx = cy + cos(a).toFloat() * cw * 0.98f
                    val by = midY + sin(a).toFloat() * craniumH * 1.0f
                    canvas.drawCircle(bx, by, craniumH * 0.16f, fill)
                    canvas.drawCircle(bx, by - craniumH * 0.2f, craniumH * 0.1f, fill)
                }
            }
            SkullAccessory.HEADPHONES -> {
                stroke.color = darken(design.accent, 0.4f)
                stroke.strokeWidth = 3.4f
                rect.set(cy - cw * 1.1f, top - craniumH * 0.2f, cy + cw * 1.1f, midY + craniumH * 0.6f)
                canvas.drawArc(rect, 195f, 150f, false, stroke)
            }
            else -> Unit
        }
    }

    private fun drawFrontAccessory(canvas: Canvas) {
        when (design.accessory) {

            SkullAccessory.NONE, SkullAccessory.HALO, SkullAccessory.HORNS,
            SkullAccessory.SPIKES, SkullAccessory.BONE_CROWN -> Unit

            SkullAccessory.TOP_HAT -> {
                fill.color = 0xFF1A1A1A.toInt()
                rect.set(cy - cw * 1.12f, top - 1.5f, cy + cw * 1.12f, top + 4f)
                canvas.drawRoundRect(rect, 2f, 2f, fill)
                rect.set(cy - cw * 0.66f, top - craniumH * 0.95f, cy + cw * 0.66f, top + 2f)
                canvas.drawRoundRect(rect, 3f, 3f, fill)
                fill.color = design.accent
                rect.set(cy - cw * 0.66f, top - craniumH * 0.2f, cy + cw * 0.66f, top - craniumH * 0.02f)
                canvas.drawRect(rect, fill)
            }

            SkullAccessory.BANDAGE -> {
                fill.color = 0xFFF6F1E6.toInt()
                accPath.reset()
                accPath.moveTo(cy - cw * 1.0f, top + craniumH * 0.42f)
                accPath.lineTo(cy + cw * 1.0f, top - craniumH * 0.1f)
                accPath.lineTo(cy + cw * 1.0f, top + craniumH * 0.24f)
                accPath.lineTo(cy - cw * 1.0f, top + craniumH * 0.76f)
                accPath.close()
                canvas.drawPath(accPath, fill)
                stroke.color = withAlpha(0xFFB9AF9C.toInt(), 200)
                stroke.strokeWidth = 1.1f
                for (i in -3..3) {
                    val t = i / 3f
                    val bx = cy + t * cw * 0.86f
                    val by = top + craniumH * 0.16f - t * craniumH * 0.3f
                    canvas.drawLine(bx - 2f, by + 2f, bx + 2f, by - 2f, stroke)
                }
            }

            SkullAccessory.BOLTS -> {
                for (side in intArrayOf(-1, 1)) {
                    val bx = cy + side * cw * 0.72f
                    val by = midY - craniumH * 0.1f
                    fill.color = 0xFF9AA3AD.toInt()
                    rect.set(bx - 6f, by - 6f, bx + 6f, by + 6f)
                    canvas.drawRoundRect(rect, 2f, 2f, fill)
                    canvas.drawCircle(bx, by, 3.2f, fill)
                    stroke.color = 0xFF6E757D.toInt()
                    stroke.strokeWidth = 1.1f
                    canvas.drawLine(bx - 3.4f, by, bx + 3.4f, by, stroke)
                    canvas.drawLine(bx, by - 3.4f, bx, by + 3.4f, stroke)
                }
            }

            SkullAccessory.EYEPATCH -> {
                fill.color = 0xFF141414.toInt()
                val ex = cy - design.socketSpread
                rect.set(ex - design.socketWidth * 1.5f, design.socketY - design.socketHeight * 1.2f,
                    ex + design.socketWidth * 1.5f, design.socketY + design.socketHeight * 1.1f)
                canvas.drawRoundRect(rect, 4f, 4f, fill)
                stroke.color = darken(design.accent, 0.2f)
                stroke.strokeWidth = 2.2f
                canvas.drawLine(
                    ex - design.socketWidth * 1.4f, design.socketY - design.socketHeight * 0.4f,
                    cy + cw * 0.98f, midY + craniumH * 0.1f, stroke,
                )
            }

            SkullAccessory.BLINDFOLD -> {
                fill.color = darken(design.accent, 0.2f)
                rect.set(cy - cw * 0.98f, design.socketY - design.socketHeight * 0.95f,
                    cy + cw * 0.98f, design.socketY - design.socketHeight * 0.1f)
                canvas.drawRoundRect(rect, 2.5f, 2.5f, fill)
                stroke.color = 0xFFFFFFFF.toInt()
                stroke.strokeWidth = 1.2f
                canvas.drawLine(cy - cw * 0.9f, design.socketY - design.socketHeight * 0.5f,
                    cy + cw * 0.9f, design.socketY - design.socketHeight * 0.5f, stroke)
            }

            SkullAccessory.ANTENNA -> {
                stroke.color = darken(design.accent, 0.3f)
                stroke.strokeWidth = 2.4f
                canvas.drawLine(cy, top + 2f, cy + cw * 0.14f, top - craniumH * 0.46f, stroke)
                fill.color = design.accent
                canvas.drawCircle(cy + cw * 0.14f, top - craniumH * 0.56f, craniumH * 0.14f, fill)
            }

            SkullAccessory.KNIFE -> {
                fill.color = 0xFFB9C2CC.toInt()
                accPath.reset()
                accPath.moveTo(cy - 2.4f, top + 3f)
                accPath.lineTo(cy + 2.4f, top + 3f)
                accPath.lineTo(cy + 1.2f, top - craniumH * 0.95f)
                accPath.lineTo(cy - 1.2f, top - craniumH * 0.95f)
                accPath.close()
                canvas.drawPath(accPath, fill)
                fill.color = 0xFF3A2A1A.toInt()
                rect.set(cy - 7f, top + 3f, cy + 7f, top + 5.6f)
                canvas.drawRect(rect, fill)
            }

            SkullAccessory.CANDLE -> {
                fill.color = 0xFFF3EAD6.toInt()
                rect.set(cy - 3.6f, top - craniumH * 0.62f, cy + 3.6f, top + 3f)
                canvas.drawRoundRect(rect, 1.5f, 1.5f, fill)
                fill.color = 0xFFFFB300.toInt()
                accPath.reset()
                accPath.moveTo(cy, top - craniumH * 1.05f)
                accPath.cubicTo(cy + 4.2f, top - craniumH * 0.86f, cy + 2.6f, top - craniumH * 0.68f, cy, top - craniumH * 0.62f)
                accPath.cubicTo(cy - 2.6f, top - craniumH * 0.68f, cy - 4.2f, top - craniumH * 0.86f, cy, top - craniumH * 1.05f)
                accPath.close()
                canvas.drawPath(accPath, fill)
            }

            SkullAccessory.MANDIBLE_TUSKS -> {
                fill.color = design.boneTint
                for (side in intArrayOf(-1, 1)) {
                    accPath.reset()
                    val bx = cy + side * jw * 0.72f
                    val by = midY + craniumH * 0.86f
                    accPath.moveTo(bx, by)
                    accPath.lineTo(bx + side * 2.2f, by + craniumH * 0.5f)
                    accPath.lineTo(bx - side * 1.4f, by + craniumH * 0.22f)
                    accPath.close()
                    canvas.drawPath(accPath, fill)
                }
            }

            SkullAccessory.SCAR -> {
                stroke.color = darken(design.boneShade, 0.55f)
                stroke.strokeWidth = 2.6f
                val sx = cy + cw * 0.42f
                val sy = midY + craniumH * 0.3f
                canvas.drawLine(sx - 6f, sy - 8f, sx + 6f, sy + 8f, stroke)
                stroke.strokeWidth = 1.5f
                for (i in 0 until 4) {
                    val t = i / 4f
                    val px = sx - 6f + 12f * t
                    val py = sy - 8f + 16f * t
                    canvas.drawLine(px - 2.4f, py - 2.4f, px + 2.4f, py + 2.4f, stroke)
                }
            }

            SkullAccessory.HEADPHONES -> {
                fill.color = darken(design.accent, 0.25f)
                for (side in intArrayOf(-1, 1)) {
                    rect.set(
                        cy + side * cw * 1.02f - 5f,
                        midY - craniumH * 0.42f,
                        cy + side * cw * 1.02f + 5f,
                        midY + craniumH * 0.3f,
                    )
                    canvas.drawRoundRect(rect, 3f, 3f, fill)
                }
            }
        }
    }
}
