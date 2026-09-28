package com.cyberpunk.debttracker.game

import android.graphics.Color
import android.util.LruCache

// ═══════════════════════════════════════════════════════════════════════════
//  ICON FORGE
//
//  Every reward in the game is a unique cute teddy-bear head.
//  Every penalty in the game is a unique pure-white skull.
//
//  Instead of shipping hundreds of hand-written vector XML files, each icon
//  is *designed* procedurally: a stable string seed (the reward/penalty
//  code) is hashed, and a whole vector of parameters — fur palette, head
//  silhouette, ear shape, eye style, muzzle, nose, plus the accessory —
//  is derived from it. Same seed always yields the exact same bear, on
//  every device, forever, and the two design vectors are plain data so
//  they can be unit tested for uniqueness.
//
//  The drawings themselves are rendered with Canvas primitives into a
//  100x100 unit space, so they stay razor sharp at any size.
// ═══════════════════════════════════════════════════════════════════════════

// ─── Deterministic RNG ───────────────────────────────────────────────────────

class Rng(seed: Long) {

    private var state: Long = if (seed == 0L) 0x2545F4914F6CDD1DL else seed

    fun nextLong(): Long {
        state += -0x61c8864680b583ebL // 0x9E3779B97F4A7C15
        var z = state
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L // 0x94D049BB133111EB
        return z xor (z ushr 31)
    }

    fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        return ((nextLong() ushr 33) % bound.toLong()).toInt()
    }

    /** Inclusive range. Returns [min] when the range is empty after clamping. */
    fun nextInt(min: Int, maxInclusive: Int): Int {
        val lo = min.coerceAtLeast(0)
        val hi = maxInclusive.coerceAtLeast(lo)
        return lo + ((nextLong() ushr 33) % (hi - lo + 1).toLong()).toInt()
    }

    fun nextFloat(): Float = ((nextLong() ushr 40).toDouble() / 16777216.0).toFloat()

    fun nextFloat(min: Float, max: Float): Float = min + nextFloat() * (max - min)

    fun nextFloat(min: Float, max: Float, decimals: Int): Float {
        val f = nextFloat(min, max)
        var m = 1f
        repeat(decimals) { m *= 10f }
        return Math.round(f * m) / m
    }

    fun chance(probability: Float): Boolean = nextFloat() < probability

    fun <T> pick(items: List<T>): T = items[nextInt(items.size)]
}

// ─── Palettes ────────────────────────────────────────────────────────────────

/** Cute teddy-bear fur palettes: head gradient bottom, top, muzzle, nose. */
internal val BEAR_PALETTES: List<IntArray> = listOf(
    intArrayOf(0xFFE8BE8C.toInt(), 0xFFF6D9B4.toInt(), 0xFFFFF1DC.toInt(), 0xFF6B4A32.toInt()), // cream
    intArrayOf(0xFFB87A3B.toInt(), 0xFFD9A05B.toInt(), 0xFFF7DCB4.toInt(), 0xFF5A3A22.toInt()), // caramel
    intArrayOf(0xFF83573A.toInt(), 0xFFA9744C.toInt(), 0xFFD9B48C.toInt(), 0xFF4A2E1A.toInt()), // chocolate
    intArrayOf(0xFFD9A02F.toInt(), 0xFFF2C14E.toInt(), 0xFFFFF0C4.toInt(), 0xFF7A4A18.toInt()), // honey
    intArrayOf(0xFFA3A8B2.toInt(), 0xFFC9CDD4.toInt(), 0xFFE8EBF0.toInt(), 0xFF5C616B.toInt()), // ash
    intArrayOf(0xFFDE8FA4.toInt(), 0xFFF3B7C3.toInt(), 0xFFFFE1E8.toInt(), 0xFF8A4A5C.toInt()), // rose
    intArrayOf(0xFF8FCBA8.toInt(), 0xFFB7E4C7.toInt(), 0xFFDFF5E7.toInt(), 0xFF3F6B52.toInt()), // mint
    intArrayOf(0xFF85B8DC.toInt(), 0xFFAFD8F0.toInt(), 0xFFDCF0FB.toInt(), 0xFF33607D.toInt()), // sky
    intArrayOf(0xFFAC9BD8.toInt(), 0xFFCFC2F0.toInt(), 0xFFEBE4FB.toInt(), 0xFF5A4A7D.toInt()), // lavender
    intArrayOf(0xFFA66B43.toInt(), 0xFFC98B5E.toInt(), 0xFFEFC9A8.toInt(), 0xFF5C3620.toInt()), // cinnamon
    intArrayOf(0xFFD06A5A.toInt(), 0xFFEE8B7A.toInt(), 0xFFFFDDD6.toInt(), 0xFF7A3328.toInt()), // terracotta
    intArrayOf(0xFF9AA84B.toInt(), 0xFFC4D377.toInt(), 0xFFEFF5D4.toInt(), 0xFF4E5A1E.toInt()), // moss
)

/** Cyberpunk accent palette used for accessories / eye sparkles. */
internal val ACCENT_COLORS: List<Int> = listOf(
    0xFFFFD700.toInt(), // cyber gold
    0xFF00E676.toInt(), // neon green
    0xFFFF1744.toInt(), // neon red
    0xFFFFC107.toInt(), // amber
    0xFF00E5FF.toInt(), // cyan
    0xFFFF4081.toInt(), // magenta
    0xFFB388FF.toInt(), // violet
    0xFFAEEA00.toInt(), // lime
    0xFFFF9100.toInt(), // orange
    0xFF1DE9B6.toInt(), // teal
    0xFFFFF176.toInt(), // pale gold
    0xFFF48FB1.toInt(), // pink
)

/** Subtle bone tints so no two skulls are literally the same white. */
internal val BONE_TINTS: List<Int> = listOf(
    0xFFFFFFFF.toInt(),
    0xFFF4F6FA.toInt(),
    0xFFFFF8EE.toInt(),
    0xFFF0FBFF.toInt(),
    0xFFF8F2FF.toInt(),
    0xFFF2FFF6.toInt(),
    0xFFFFF1F6.toInt(),
    0xFFFAF7F0.toInt(),
)

/** Hollow colour used for sockets / nasal cavity / cracks inside a skull. */
internal const val SKULL_HOLLOW: Int = 0xFF121212.toInt()

// ─── Bear design ─────────────────────────────────────────────────────────────

enum class HeadShape { CIRCLE, SUPERELLIPSE, EGG, WIDE_SQUASH, TAPERED }
enum class EarShape { ROUND, TEARDROP, POINTED, FOLDED }
enum class EyeStyle { DOT, SPARKLE, HAPPY_ARC, SLEEPY, WIDE }
enum class NoseShape { ROUNDED_TRIANGLE, HEART, BUTTON, SNUB }
enum class MouthStyle { DOUBLE_W, STRAIGHT, SMILE, OPEN_SMILE, GRIN }
enum class BearAccessory {
    NONE, CROWN, BOW, PARTY_HAT, CAP, BANDANA, ANTENNA, HALO, FLOWER,
    HEADPHONES, STAR_PIN, SCARF, MONOCLE, GLASSES, MOHAWK, HORNS
}

data class BearDesign(
    val headShape: HeadShape,
    val earShape: EarShape,
    val eyeStyle: EyeStyle,
    val noseShape: NoseShape,
    val mouthStyle: MouthStyle,
    val accessory: BearAccessory,
    val furBottom: Int,
    val furTop: Int,
    val muzzleColor: Int,
    val noseColor: Int,
    val innerEarColor: Int,
    val accent: Int,
    val eyeColor: Int,
    val blush: Boolean,
    val headScaleX: Float,
    val headScaleY: Float,
    val headCenterY: Float,
    val headRadius: Float,
    val earSize: Float,
    val earAngle: Float,
    val eyeSpacing: Float,
    val eyeRadius: Float,
    val eyeY: Float,
    val muzzleY: Float,
    val muzzleScale: Float,
    val lineWidth: Float,
    val name: String = "",
) {
    companion object {
        fun from(seed: String): BearDesign {
            val rng = Rng(seedHash(seed))
            val palette = BEAR_PALETTES[rng.nextInt(BEAR_PALETTES.size)]
            val accent = ACCENT_COLORS[rng.nextInt(ACCENT_COLORS.size)]
            return BearDesign(
                headShape = HeadShape.entries[rng.nextInt(HeadShape.entries.size)],
                earShape = EarShape.entries[rng.nextInt(EarShape.entries.size)],
                eyeStyle = EyeStyle.entries[rng.nextInt(EyeStyle.entries.size)],
                noseShape = NoseShape.entries[rng.nextInt(NoseShape.entries.size)],
                mouthStyle = MouthStyle.entries[rng.nextInt(MouthStyle.entries.size)],
                accessory = BearAccessory.entries[rng.nextInt(BearAccessory.entries.size)],
                furBottom = palette[0],
                furTop = palette[1],
                muzzleColor = palette[2],
                noseColor = palette[3],
                innerEarColor = darken(palette[3], 0.82f),
                accent = accent,
                eyeColor = if (rng.chance(0.28f)) accent else darken(palette[3], 0.45f),
                blush = rng.chance(0.62f),
                headScaleX = rng.nextFloat(0.95f, 1.08f, 3),
                headScaleY = rng.nextFloat(0.94f, 1.06f, 3),
                headCenterY = rng.nextFloat(53f, 57f, 2),
                headRadius = rng.nextFloat(27f, 30.5f, 2),
                earSize = rng.nextFloat(11.5f, 14.5f, 2),
                earAngle = rng.nextFloat(-16f, 16f, 2),
                eyeSpacing = rng.nextFloat(9.5f, 12.5f, 2),
                eyeRadius = rng.nextFloat(3.4f, 4.6f, 2),
                eyeY = rng.nextFloat(48f, 52f, 2),
                muzzleY = rng.nextFloat(63f, 68f, 2),
                muzzleScale = rng.nextFloat(0.9f, 1.1f, 3),
                lineWidth = rng.nextFloat(1.7f, 2.4f, 2),
                name = seed,
            )
        }
    }
}

// ─── Skull design ────────────────────────────────────────────────────────────

enum class SocketShape { ROUND, ANGULAR, SLANTED, SQUARE, HOLLOW_CROSS }
enum class NoseShape2 { HEART, TRIANGLE, OVAL, FLAME }
enum class SkullAccessory {
    NONE, TOP_HAT, BANDAGE, HORNS, BOLTS, EYEPATCH, ANTENNA, BONE_CROWN,
    HALO, HEADPHONES, KNIFE, CANDLE, BLINDFOLD, SPIKES, MANDIBLE_TUSKS, SCAR
}

data class SkullDesign(
    val craniumWidth: Float,
    val craniumHeight: Float,
    val craniumTop: Float,
    val jawWidth: Float,
    val jawBottom: Float,
    val socketShape: SocketShape,
    val noseShape: NoseShape2,
    val accessory: SkullAccessory,
    val boneTint: Int,
    val boneShade: Int,
    val accent: Int,
    val socketWidth: Float,
    val socketHeight: Float,
    val socketSpread: Float,
    val socketTilt: Float,
    val socketY: Float,
    val teethCount: Int,
    val teethWidth: Float,
    val crackSeed: Int,
    val crackDepth: Float,
    val browThickness: Float,
    val cheekAngle: Float,
    val name: String = "",
) {
    companion object {
        fun from(seed: String): SkullDesign {
            val rng = Rng(seedHash(seed) xor 0x5DEECE66DL)
            return SkullDesign(
                craniumWidth = rng.nextFloat(37f, 43f, 2),
                craniumHeight = rng.nextFloat(25f, 29f, 2),
                craniumTop = rng.nextFloat(21f, 25f, 2),
                jawWidth = rng.nextFloat(24f, 30f, 2),
                jawBottom = rng.nextFloat(80f, 85f, 2),
                socketShape = SocketShape.entries[rng.nextInt(SocketShape.entries.size)],
                noseShape = NoseShape2.entries[rng.nextInt(NoseShape2.entries.size)],
                accessory = SkullAccessory.entries[rng.nextInt(SkullAccessory.entries.size)],
                boneTint = BONE_TINTS[rng.nextInt(BONE_TINTS.size)],
                boneShade = BONE_TINTS[rng.nextInt(BONE_TINTS.size)],
                accent = ACCENT_COLORS[rng.nextInt(ACCENT_COLORS.size)],
                socketWidth = rng.nextFloat(6.5f, 8.8f, 2),
                socketHeight = rng.nextFloat(6.0f, 8.4f, 2),
                socketSpread = rng.nextFloat(9f, 11.5f, 2),
                socketTilt = rng.nextFloat(-9f, 9f, 2),
                socketY = rng.nextFloat(50f, 53f, 2),
                teethCount = rng.nextInt(5, 9),
                teethWidth = rng.nextFloat(1.2f, 2.4f, 2),
                crackSeed = rng.nextInt(0, 6),
                crackDepth = if (rng.chance(0.7f)) rng.nextFloat(5f, 13f, 2) else 0f,
                browThickness = rng.nextFloat(1.4f, 2.8f, 2),
                cheekAngle = rng.nextFloat(-8f, 8f, 2),
                name = seed,
            )
        }
    }
}

// ─── Colour helpers ──────────────────────────────────────────────────────────

internal fun darken(color: Int, factor: Float): Int = Color.rgb(
    (Color.red(color) * factor).toInt().coerceIn(0, 255),
    (Color.green(color) * factor).toInt().coerceIn(0, 255),
    (Color.blue(color) * factor).toInt().coerceIn(0, 255),
)

internal fun withAlpha(color: Int, alpha: Int): Int =
    (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)

/** FNV-1a 64-bit over the seed, run through a splitmix64 finaliser. */
internal fun seedHash(seed: String): Long {
    var h = -0x340d631b7bdddcdbL // 0xCBF29CE484222325
    for (ch in seed) {
        h = h xor ch.code.toLong()
        h *= 0x100000001b3L
    }
    var z = h
    z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
    z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
    return z xor (z ushr 31)
}

// ─── Forge ───────────────────────────────────────────────────────────────────

object IconForge {

    private val cache = LruCache<String, android.graphics.drawable.Drawable>(512)

    private fun cached(key: String, create: () -> android.graphics.drawable.Drawable): android.graphics.drawable.Drawable {
        cache.get(key)?.let { return it }
        return create().also { cache.put(key, it) }
    }

    fun bear(seed: String): android.graphics.drawable.Drawable =
        cached("B:$seed") { BearIcon(BearDesign.from(seed)) }

    fun skull(seed: String): android.graphics.drawable.Drawable =
        cached("S:$seed") { SkullIcon(SkullDesign.from(seed)) }

    fun bearDesign(seed: String): BearDesign = BearDesign.from(seed)

    fun skullDesign(seed: String): SkullDesign = SkullDesign.from(seed)

    /** Pixel-tinted silhouette of a bear head, handy for backgrounds. */
    fun bearTint(seed: String): Int = BearDesign.from(seed).furBottom
}
