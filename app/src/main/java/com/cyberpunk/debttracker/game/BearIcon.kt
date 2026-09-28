package com.cyberpunk.debttracker.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Renders a [BearDesign] as a cute teddy-bear head.
 * Everything is drawn inside a 100x100 unit space then scaled to bounds.
 */
class BearIcon(val design: BearDesign) : Drawable() {

    // ─── Paints ───────────────────────────────────────────────────────────────

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val shadow = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rect = RectF()
    private val mtx = Matrix()

    // ─── Geometry derived from the design ─────────────────────────────────────

    private val cx = 50f
    private val cy = design.headCenterY
    private val rx = design.headRadius * design.headScaleX
    private val ry = design.headRadius * design.headScaleY
    private val earX = rx * 0.74f
    private val earY = cy - ry * 0.66f
    private val muzzleRx = rx * 0.47f * design.muzzleScale
    private val muzzleRy = ry * 0.37f * design.muzzleScale
    private val noseH = muzzleRy * 0.62f
    private val noseW = muzzleRy * 0.72f

    // ─── Paths built once ─────────────────────────────────────────────────────

    private val headPath = Path()
    private val earPath = Path()
    private val innerEarPath = Path()
    private val muzzlePath = Path()
    private val nosePath = Path()
    private val accPath = Path()

    private var alphaValue = 255
    private var filterValue: ColorFilter? = null

    init {
        buildHead()
        buildEar()
        buildMuzzle()
        buildNose()
        stroke.strokeWidth = design.lineWidth
    }

    private fun buildHead() {
        headPath.reset()
        when (design.headShape) {
            HeadShape.CIRCLE -> headPath.addOval(cx - rx, cy - ry, cx + rx, cy + ry, Path.Direction.CW)
            HeadShape.SUPERELLIPSE -> headPath.addRoundRect(
                cx - rx, cy - ry, cx + rx, cy + ry,
                minOf(rx, ry) * 0.95f, minOf(rx, ry) * 0.95f, Path.Direction.CW,
            )
            HeadShape.WIDE_SQUASH -> headPath.addRoundRect(
                cx - rx, cy - ry, cx + rx, cy + ry,
                minOf(rx, ry) * 0.72f, minOf(rx, ry) * 0.72f, Path.Direction.CW,
            )
            HeadShape.EGG -> {
                headPath.moveTo(cx - rx, cy)
                headPath.cubicTo(cx - rx, cy - ry * 1.16f, cx + rx, cy - ry * 1.16f, cx + rx, cy)
                headPath.cubicTo(cx + rx, cy + ry * 0.74f, cx + rx * 0.55f, cy + ry * 1.16f, cx, cy + ry * 1.16f)
                headPath.cubicTo(cx - rx * 0.55f, cy + ry * 1.16f, cx - rx, cy + ry * 0.74f, cx - rx, cy)
                headPath.close()
            }
            HeadShape.TAPERED -> {
                headPath.moveTo(cx, cy - ry)
                headPath.cubicTo(cx + rx * 0.96f, cy - ry, cx + rx, cy - ry * 0.24f, cx + rx * 0.9f, cy + ry * 0.16f)
                headPath.cubicTo(cx + rx * 0.8f, cy + ry * 0.8f, cx + rx * 0.42f, cy + ry * 1.1f, cx, cy + ry * 1.1f)
                headPath.cubicTo(cx - rx * 0.42f, cy + ry * 1.1f, cx - rx * 0.8f, cy + ry * 0.8f, cx - rx * 0.9f, cy + ry * 0.16f)
                headPath.cubicTo(cx - rx, cy - ry * 0.24f, cx - rx * 0.96f, cy - ry, cx, cy - ry)
                headPath.close()
            }
        }
    }

    private fun buildEar() {
        val s = design.earSize
        earPath.reset()
        innerEarPath.reset()
        when (design.earShape) {
            EarShape.ROUND -> {
                earPath.addCircle(0f, 0f, s, Path.Direction.CW)
                innerEarPath.addCircle(0f, 0f, s * 0.48f, Path.Direction.CW)
            }
            EarShape.TEARDROP -> {
                rect.set(-s * 0.95f, -s * 0.85f, s * 0.95f, s * 0.85f)
                earPath.addOval(rect, Path.Direction.CW)
                rect.set(-s * 0.48f, -s * 0.4f, s * 0.48f, s * 0.4f)
                innerEarPath.addOval(rect, Path.Direction.CW)
            }
            EarShape.POINTED -> {
                rect.set(-s * 0.88f, -s * 0.82f, s * 0.88f, s * 0.82f)
                earPath.addOval(rect, Path.Direction.CW)
                earPath.moveTo(s * 0.55f, -s * 0.7f)
                earPath.lineTo(s * 1.22f, -s * 0.95f)
                earPath.lineTo(s * 0.78f, 0f)
                earPath.close()
                rect.set(-s * 0.44f, -s * 0.38f, s * 0.44f, s * 0.38f)
                innerEarPath.addOval(rect, Path.Direction.CW)
            }
            EarShape.FOLDED -> {
                rect.set(-s * 1.02f, -s * 0.7f, s * 1.02f, s * 0.7f)
                earPath.addOval(rect, Path.Direction.CW)
                rect.set(-s * 0.52f, -s * 0.34f, s * 0.52f, s * 0.34f)
                innerEarPath.addOval(rect, Path.Direction.CW)
            }
        }
    }

    private fun buildMuzzle() {
        muzzlePath.reset()
        muzzlePath.addOval(
            cx - muzzleRx, cy + (design.muzzleY - cy) - muzzleRy,
            cx + muzzleRx, cy + (design.muzzleY - cy) + muzzleRy,
            Path.Direction.CW,
        )
    }

    private fun buildNose() {
        val ny = design.muzzleY - muzzleRy * 0.34f
        nosePath.reset()
        when (design.noseShape) {
            NoseShape.ROUNDED_TRIANGLE -> {
                nosePath.moveTo(cx, ny + noseH)
                nosePath.quadTo(cx - noseW * 0.9f, ny + noseH * 0.15f, cx - noseW, ny - noseH * 0.55f)
                nosePath.quadTo(cx - noseW * 0.5f, ny - noseH * 1.0f, cx, ny - noseH * 0.95f)
                nosePath.quadTo(cx + noseW * 0.5f, ny - noseH * 1.0f, cx + noseW, ny - noseH * 0.55f)
                nosePath.quadTo(cx + noseW * 0.9f, ny + noseH * 0.15f, cx, ny + noseH)
                nosePath.close()
            }
            NoseShape.HEART -> {
                nosePath.moveTo(cx, ny + noseH * 0.9f)
                nosePath.cubicTo(cx - noseW * 1.5f, ny - noseH * 0.3f, cx - noseW * 0.85f, ny - noseH * 1.15f, cx, ny - noseH * 0.35f)
                nosePath.cubicTo(cx + noseW * 0.85f, ny - noseH * 1.15f, cx + noseW * 1.5f, ny - noseH * 0.3f, cx, ny + noseH * 0.9f)
                nosePath.close()
            }
            NoseShape.BUTTON -> {
                rect.set(cx - noseW, ny - noseH * 0.9f, cx + noseW, ny + noseH * 0.75f)
                nosePath.addRoundRect(rect, noseH * 0.8f, noseH * 0.8f, Path.Direction.CW)
            }
            NoseShape.SNUB -> {
                rect.set(cx - noseW * 1.15f, ny - noseH * 0.7f, cx + noseW * 1.15f, ny + noseH * 0.85f)
                nosePath.addRoundRect(rect, noseH * 0.6f, noseH * 0.9f, Path.Direction.CW)
            }
        }
    }

    // ─── Drawable ─────────────────────────────────────────────────────────────

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
        shadow.colorFilter = filterValue

        drawBehindAccessory(canvas)
        drawEars(canvas)
        drawHead(canvas)
        drawBehindHeadShadow(canvas)
        drawMuzzleAndFace(canvas)
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

    private fun drawEars(canvas: Canvas) {
        val grad = LinearGradient(
            cx, cy - ry, cx, cy + ry,
            design.furTop, design.furBottom, Shader.TileMode.CLAMP,
        )
        fill.shader = grad
        fill.style = Paint.Style.FILL
        fill.color = design.furBottom

        mtx.reset()
        mtx.setRotate(design.earAngle, cx - earX, earY)
        earPath.transform(mtx, accPath)
        fill.shader = grad
        canvas.drawPath(accPath, fill)

        mtx.reset()
        mtx.setRotate(-design.earAngle, cx + earX, earY)
        earPath.transform(mtx, accPath)
        canvas.drawPath(accPath, fill)

        fill.shader = null
        fill.color = design.innerEarColor
        mtx.reset()
        mtx.setRotate(design.earAngle, cx - earX, earY)
        innerEarPath.transform(mtx, accPath)
        canvas.drawPath(accPath, fill)

        mtx.reset()
        mtx.setRotate(-design.earAngle, cx + earX, earY)
        innerEarPath.transform(mtx, accPath)
        canvas.drawPath(accPath, fill)
    }

    private fun drawHead(canvas: Canvas) {
        fill.shader = LinearGradient(
            cx, cy - ry, cx, cy + ry,
            design.furTop, design.furBottom, Shader.TileMode.CLAMP,
        )
        canvas.drawPath(headPath, fill)
        fill.shader = null

        // Soft top-left shine keeps the head looking plush.
        fill.color = withAlpha(Color.WHITE, 46)
        rect.set(cx - rx * 0.66f, cy - ry * 0.78f, cx - rx * 0.06f, cy - ry * 0.16f)
        canvas.drawOval(rect, fill)
    }

    private fun drawBehindHeadShadow(canvas: Canvas) {
        if (design.accessory != BearAccessory.SCARF) return
        fill.color = withAlpha(Color.BLACK, 34)
        rect.set(cx - rx * 0.98f, cy + ry * 0.42f, cx + rx * 0.98f, cy + ry * 1.16f)
        canvas.drawRoundRect(rect, rx * 0.5f, rx * 0.5f, fill)
    }

    private fun drawMuzzleAndFace(canvas: Canvas) {
        // Muzzle
        fill.color = design.muzzleColor
        canvas.drawPath(muzzlePath, fill)
        fill.color = withAlpha(darken(design.muzzleColor, 0.86f), 255)
        muzzlePath.reset()
        muzzlePath.addOval(
            cx - muzzleRx * 1.04f, design.muzzleY - muzzleRy * 1.12f,
            cx + muzzleRx * 1.04f, design.muzzleY + muzzleRy * 1.12f,
            Path.Direction.CW,
        )
        stroke.color = withAlpha(darken(design.muzzleColor, 0.8f), 70)
        stroke.strokeWidth = design.lineWidth * 0.5f
        canvas.drawPath(muzzlePath, stroke)
        buildMuzzle()

        // Blush
        if (design.blush) {
            fill.color = withAlpha(0xFFFF6B8A.toInt(), 96)
            rect.set(cx - rx * 0.86f, design.eyeY + ry * 0.16f, cx - rx * 0.48f, design.eyeY + ry * 0.46f)
            canvas.drawOval(rect, fill)
            rect.set(cx + rx * 0.48f, design.eyeY + ry * 0.16f, cx + rx * 0.86f, design.eyeY + ry * 0.46f)
            canvas.drawOval(rect, fill)
        }

        drawEyes(canvas)
        drawNoseAndMouth(canvas)
    }

    private fun drawEyes(canvas: Canvas) {
        val lx = cx - design.eyeSpacing
        val rx2 = cx + design.eyeSpacing
        val ey = design.eyeY
        val er = design.eyeRadius

        when (design.eyeStyle) {
            EyeStyle.DOT -> {
                fill.color = design.eyeColor
                canvas.drawCircle(lx, ey, er, fill)
                canvas.drawCircle(rx2, ey, er, fill)
                fill.color = withAlpha(Color.WHITE, 220)
                canvas.drawCircle(lx + er * 0.32f, ey - er * 0.36f, er * 0.3f, fill)
                canvas.drawCircle(rx2 + er * 0.32f, ey - er * 0.36f, er * 0.3f, fill)
            }
            EyeStyle.SPARKLE -> {
                fill.color = design.eyeColor
                star(canvas, lx, ey, er * 1.5f, er * 0.55f)
                star(canvas, rx2, ey, er * 1.5f, er * 0.55f)
                fill.color = Color.WHITE
                canvas.drawCircle(lx, ey, er * 0.24f, fill)
                canvas.drawCircle(rx2, ey, er * 0.24f, fill)
            }
            EyeStyle.HAPPY_ARC -> {
                stroke.color = design.eyeColor
                stroke.strokeWidth = design.lineWidth * 1.15f
                rect.set(lx - er * 1.5f, ey - er * 1.2f, lx + er * 1.5f, ey + er * 1.8f)
                canvas.drawArc(rect, 200f, 140f, false, stroke)
                rect.set(rx2 - er * 1.5f, ey - er * 1.2f, rx2 + er * 1.5f, ey + er * 1.8f)
                canvas.drawArc(rect, 200f, 140f, false, stroke)
            }
            EyeStyle.SLEEPY -> {
                stroke.color = design.eyeColor
                stroke.strokeWidth = design.lineWidth * 1.1f
                rect.set(lx - er * 1.5f, ey - er * 1.4f, lx + er * 1.5f, ey + er * 1.6f)
                canvas.drawArc(rect, 20f, 140f, false, stroke)
                rect.set(rx2 - er * 1.5f, ey - er * 1.4f, rx2 + er * 1.5f, ey + er * 1.6f)
                canvas.drawArc(rect, 20f, 140f, false, stroke)
                fill.color = design.eyeColor
                canvas.drawCircle(lx + er * 1.7f, ey - er * 1.5f, er * 0.26f, fill)
                canvas.drawCircle(rx2 - er * 1.7f, ey - er * 1.5f, er * 0.26f, fill)
            }
            EyeStyle.WIDE -> {
                fill.color = Color.WHITE
                canvas.drawCircle(lx, ey, er * 1.28f, fill)
                canvas.drawCircle(rx2, ey, er * 1.28f, fill)
                fill.color = design.eyeColor
                canvas.drawCircle(lx + er * 0.12f, ey + er * 0.1f, er * 0.86f, fill)
                canvas.drawCircle(rx2 + er * 0.12f, ey + er * 0.1f, er * 0.86f, fill)
                fill.color = withAlpha(Color.WHITE, 200)
                canvas.drawCircle(lx + er * 0.42f, ey - er * 0.3f, er * 0.28f, fill)
                canvas.drawCircle(rx2 + er * 0.42f, ey - er * 0.3f, er * 0.28f, fill)
            }
        }
    }

    private fun drawNoseAndMouth(canvas: Canvas) {
        fill.color = design.noseColor
        canvas.drawPath(nosePath, fill)
        fill.color = withAlpha(Color.WHITE, 90)
        canvas.drawCircle(cx - noseW * 0.28f, design.muzzleY - muzzleRy * 0.66f, noseW * 0.24f, fill)

        stroke.color = darken(design.noseColor, 0.9f)
        stroke.strokeWidth = design.lineWidth * 0.9f
        val my = design.muzzleY + muzzleRy * 0.12f

        when (design.mouthStyle) {
            MouthStyle.DOUBLE_W -> {
                val w = muzzleRx * 0.42f
                rect.set(cx - w, my - muzzleRy * 0.3f, cx, my + muzzleRy * 0.5f)
                canvas.drawArc(rect, 0f, 200f, false, stroke)
                rect.set(cx, my - muzzleRy * 0.3f, cx + w, my + muzzleRy * 0.5f)
                canvas.drawArc(rect, 200f, 160f, false, stroke)
            }
            MouthStyle.STRAIGHT -> {
                canvas.drawLine(cx, my - muzzleRy * 0.28f, cx, my + muzzleRy * 0.34f, stroke)
                canvas.drawLine(cx, my + muzzleRy * 0.34f, cx + muzzleRx * 0.4f, my + muzzleRy * 0.48f, stroke)
            }
            MouthStyle.SMILE -> {
                rect.set(cx - muzzleRx * 0.55f, my - muzzleRy * 0.5f, cx + muzzleRx * 0.55f, my + muzzleRy * 0.5f)
                canvas.drawArc(rect, 10f, 160f, false, stroke)
            }
            MouthStyle.OPEN_SMILE -> {
                fill.color = darken(design.noseColor, 0.78f)
                rect.set(cx - muzzleRx * 0.42f, my - muzzleRy * 0.15f, cx + muzzleRx * 0.42f, my + muzzleRy * 0.62f)
                canvas.drawArc(rect, 0f, 180f, true, fill)
                fill.color = 0xFFFF8FA3.toInt()
                rect.set(cx - muzzleRx * 0.2f, my + muzzleRy * 0.2f, cx + muzzleRx * 0.2f, my + muzzleRy * 0.55f)
                canvas.drawArc(rect, 0f, 180f, true, fill)
            }
            MouthStyle.GRIN -> {
                fill.color = darken(design.noseColor, 0.78f)
                rect.set(cx - muzzleRx * 0.5f, my - muzzleRy * 0.12f, cx + muzzleRx * 0.5f, my + muzzleRy * 0.5f)
                canvas.drawArc(rect, 0f, 180f, true, fill)
                stroke.color = Color.WHITE
                stroke.strokeWidth = design.lineWidth * 0.55f
                for (i in -1..1) {
                    val x = cx + i * muzzleRx * 0.24f
                    val drop = muzzleRy * (0.5f - kotlin.math.abs(i) * 0.18f)
                    canvas.drawLine(x, my - muzzleRy * 0.02f, x, my + drop, stroke)
                }
            }
        }
    }

    // ─── Accessories ──────────────────────────────────────────────────────────

    private fun drawBehindAccessory(canvas: Canvas) {
        val top = cy - ry
        when (design.accessory) {
            BearAccessory.HALO -> {
                stroke.color = design.accent
                stroke.strokeWidth = design.lineWidth * 1.5f
                rect.set(cx - rx * 0.5f, top - ry * 0.46f, cx + rx * 0.5f, top - ry * 0.06f)
                canvas.drawOval(rect, stroke)
                fill.color = withAlpha(design.accent, 60)
                canvas.drawOval(rect, fill)
            }
            BearAccessory.ANTENNA -> {
                stroke.color = design.accent
                stroke.strokeWidth = design.lineWidth * 1.1f
                canvas.drawLine(cx, top + ry * 0.1f, cx + rx * 0.12f, top - ry * 0.44f, stroke)
                fill.color = design.accent
                canvas.drawCircle(cx + rx * 0.12f, top - ry * 0.5f, ry * 0.11f, fill)
            }
            BearAccessory.MOHAWK -> {
                fill.color = darken(design.furBottom, 0.62f)
                for (i in -3..3) {
                    val t = i / 3f
                    val bx = cx + t * rx * 0.74f
                    val h = (1f - kotlin.math.abs(t) * 0.42f) * ry * 0.52f
                    accPath.reset()
                    accPath.moveTo(bx - rx * 0.1f, top + ry * 0.08f)
                    accPath.lineTo(bx, top - h)
                    accPath.lineTo(bx + rx * 0.1f, top + ry * 0.08f)
                    accPath.close()
                    canvas.drawPath(accPath, fill)
                }
            }
            BearAccessory.HORNS -> {
                fill.color = 0xFFF2E6CF.toInt()
                for (side in intArrayOf(-1, 1)) {
                    accPath.reset()
                    val bx = cx + side * rx * 0.6f
                    accPath.moveTo(bx, top + ry * 0.2f)
                    accPath.cubicTo(
                        bx + side * rx * 0.4f, top + ry * 0.05f,
                        bx + side * rx * 0.46f, top - ry * 0.36f,
                        bx + side * rx * 0.12f, top - ry * 0.44f,
                    )
                    accPath.cubicTo(
                        bx + side * rx * 0.16f, top - ry * 0.2f,
                        bx + side * rx * 0.12f, top - ry * 0.02f,
                        bx, top + ry * 0.2f,
                    )
                    accPath.close()
                    canvas.drawPath(accPath, fill)
                }
            }
            BearAccessory.HEADPHONES -> {
                stroke.color = darken(design.accent, 0.55f)
                stroke.strokeWidth = design.lineWidth * 1.6f
                rect.set(cx - rx * 1.06f, cy - ry * 1.2f, cx + rx * 1.06f, cy + ry * 0.4f)
                canvas.drawArc(rect, 190f, 160f, false, stroke)
            }
            BearAccessory.SCARF -> Unit
            else -> Unit
        }
    }

    private fun drawFrontAccessory(canvas: Canvas) {
        val top = cy - ry
        when (design.accessory) {

            BearAccessory.NONE -> Unit

            BearAccessory.CROWN -> {
                fill.color = 0xFFFFD700.toInt()
                accPath.reset()
                val bw = rx * 0.62f
                val by = top + ry * 0.1f
                accPath.moveTo(cx - bw, by)
                accPath.lineTo(cx - bw, by - ry * 0.3f)
                accPath.lineTo(cx - bw * 0.5f, by - ry * 0.1f)
                accPath.lineTo(cx, by - ry * 0.44f)
                accPath.lineTo(cx + bw * 0.5f, by - ry * 0.1f)
                accPath.lineTo(cx + bw, by - ry * 0.3f)
                accPath.lineTo(cx + bw, by)
                accPath.close()
                canvas.drawPath(accPath, fill)
                fill.color = 0xFFFF4081.toInt()
                canvas.drawCircle(cx, by - ry * 0.5f, ry * 0.09f, fill)
            }

            BearAccessory.BOW -> {
                val bx = cx + rx * 0.5f
                val by = top + ry * 0.3f
                val s = rx * 0.4f
                fill.color = design.accent
                accPath.reset()
                accPath.moveTo(bx, by)
                accPath.cubicTo(bx - s, by - s * 0.9f, bx - s * 1.1f, by + s * 0.5f, bx, by)
                accPath.cubicTo(bx + s, by - s * 0.9f, bx + s * 1.1f, by + s * 0.5f, bx, by)
                accPath.close()
                canvas.drawPath(accPath, fill)
                fill.color = darken(design.accent, 0.7f)
                canvas.drawCircle(bx, by, s * 0.3f, fill)
            }

            BearAccessory.PARTY_HAT -> {
                val bx = cx - rx * 0.34f
                fill.color = design.accent
                accPath.reset()
                accPath.moveTo(bx - rx * 0.32f, top + ry * 0.22f)
                accPath.lineTo(bx + rx * 0.06f, top - ry * 0.9f)
                accPath.lineTo(bx + rx * 0.34f, top + ry * 0.22f)
                accPath.close()
                canvas.drawPath(accPath, fill)
                fill.color = Color.WHITE
                canvas.drawCircle(bx + rx * 0.02f, top - ry * 0.66f, ry * 0.08f, fill)
                canvas.drawCircle(bx - rx * 0.1f, top - ry * 0.3f, ry * 0.06f, fill)
                fill.color = 0xFFFFD700.toInt()
                canvas.drawCircle(bx + rx * 0.06f, top - ry * 0.96f, ry * 0.1f, fill)
            }

            BearAccessory.CAP -> {
                fill.color = darken(design.accent, 0.62f)
                rect.set(cx - rx * 0.98f, top - ry * 0.3f, cx + rx * 0.98f, top + ry * 0.22f)
                canvas.drawArc(rect, 180f, 180f, true, fill)
                fill.color = darken(design.accent, 0.45f)
                accPath.reset()
                accPath.moveTo(cx + rx * 0.7f, top - ry * 0.06f)
                accPath.quadTo(cx + rx * 1.7f, top - ry * 0.2f, cx + rx * 1.5f, top + ry * 0.1f)
                accPath.lineTo(cx + rx * 0.7f, top + ry * 0.1f)
                accPath.close()
                canvas.drawPath(accPath, fill)
                fill.color = 0xFFFFD700.toInt()
                canvas.drawCircle(cx, top - ry * 0.32f, ry * 0.09f, fill)
            }

            BearAccessory.BANDANA -> {
                fill.color = design.accent
                accPath.reset()
                accPath.moveTo(cx - rx * 1.02f, cy - ry * 0.34f)
                accPath.quadTo(cx, cy - ry * 0.86f, cx + rx * 1.02f, cy - ry * 0.34f)
                accPath.quadTo(cx, cy - ry * 0.18f, cx - rx * 1.02f, cy - ry * 0.34f)
                accPath.close()
                canvas.drawPath(accPath, fill)
                accPath.reset()
                accPath.moveTo(cx - rx * 0.94f, cy - ry * 0.36f)
                accPath.lineTo(cx - rx * 1.5f, cy + ry * 0.12f)
                accPath.lineTo(cx - rx * 0.86f, cy - ry * 0.06f)
                accPath.close()
                canvas.drawPath(accPath, fill)
            }

            BearAccessory.HALO, BearAccessory.ANTENNA, BearAccessory.MOHAWK,
            BearAccessory.HORNS, BearAccessory.HEADPHONES -> Unit

            BearAccessory.FLOWER -> {
                val fx = cx - rx * 0.56f
                val fy = top + ry * 0.26f
                fill.color = design.accent
                for (i in 0 until 5) {
                    val a = Math.toRadians(i * 72.0)
                    canvas.drawCircle(
                        fx + kotlin.math.cos(a).toFloat() * ry * 0.2f,
                        fy + kotlin.math.sin(a).toFloat() * ry * 0.2f,
                        ry * 0.12f, fill,
                    )
                }
                fill.color = 0xFFFFD700.toInt()
                canvas.drawCircle(fx, fy, ry * 0.1f, fill)
            }

            BearAccessory.STAR_PIN -> {
                val sx = cx + earX
                fill.color = 0xFFFFD700.toInt()
                star(canvas, sx, earY, design.earSize * 0.9f, design.earSize * 0.4f)
            }

            BearAccessory.SCARF -> {
                fill.color = design.accent
                rect.set(cx - rx * 1.0f, cy + ry * 0.5f, cx + rx * 1.0f, cy + ry * 0.92f)
                canvas.drawRoundRect(rect, ry * 0.22f, ry * 0.22f, fill)
                accPath.reset()
                accPath.moveTo(cx + rx * 0.2f, cy + ry * 0.82f)
                accPath.lineTo(cx + rx * 0.95f, cy + ry * 1.1f)
                accPath.lineTo(cx + rx * 0.6f, cy + ry * 1.28f)
                accPath.lineTo(cx + rx * 0.05f, cy + ry * 0.98f)
                accPath.close()
                canvas.drawPath(accPath, fill)
                fill.color = withAlpha(Color.WHITE, 90)
                canvas.drawRoundRect(
                    (cx - rx).toFloat(), cy + ry * 0.56f, (cx + rx).toFloat(), cy + ry * 0.64f,
                    ry * 0.05f, ry * 0.05f, fill,
                )
            }

            BearAccessory.MONOCLE -> {
                val ex = cx + design.eyeSpacing
                val ey = design.eyeY
                val er = design.eyeRadius
                stroke.color = 0xFFFFD700.toInt()
                stroke.strokeWidth = design.lineWidth * 0.9f
                canvas.drawCircle(ex, ey, er * 2.5f, stroke)
                canvas.drawLine(ex + er * 2.5f, ey + er * 1.8f, ex + er * 4f, ey + er * 5f, stroke)
                fill.color = withAlpha(0xFF9FE8FF.toInt(), 60)
                canvas.drawCircle(ex, ey, er * 2.5f, fill)
            }

            BearAccessory.GLASSES -> {
                val er = design.eyeRadius * 2.7f
                val lx = cx - design.eyeSpacing
                val rxp = cx + design.eyeSpacing
                val ey = design.eyeY
                stroke.color = design.accent
                stroke.strokeWidth = design.lineWidth * 1.1f
                canvas.drawCircle(lx, ey, er, stroke)
                canvas.drawCircle(rxp, ey, er, stroke)
                canvas.drawLine(lx + er, ey, rxp - er, ey, stroke)
                canvas.drawLine(lx - er, ey, lx - rx * 1.02f, ey - design.earAngle * 0.1f, stroke)
                canvas.drawLine(rxp + er, ey, rxp + rx * 1.02f, ey - design.earAngle * 0.1f, stroke)
                fill.color = withAlpha(0xFFFFFFFF.toInt(), 42)
                canvas.drawCircle(lx, ey, er, fill)
                canvas.drawCircle(rxp, ey, er, fill)
            }
        }
    }

    private fun star(canvas: Canvas, x: Float, y: Float, outer: Float, inner: Float) {
        accPath.reset()
        for (i in 0 until 10) {
            val a = Math.toRadians(-90.0 + i * 36.0)
            val r = if (i % 2 == 0) outer else inner
            val px = x + kotlin.math.cos(a).toFloat() * r
            val py = y + kotlin.math.sin(a).toFloat() * r
            if (i == 0) accPath.moveTo(px, py) else accPath.lineTo(px, py)
        }
        accPath.close()
        canvas.drawPath(accPath, fill)
    }
}
