package com.cyberpunk.debttracker.ui.game

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.game.AchievementDef
import com.cyberpunk.debttracker.game.GameOverlay
import com.cyberpunk.debttracker.game.IconForge
import com.cyberpunk.debttracker.game.QuestDef

/**
 * Bottom-of-screen event feed. Rewards pop in with their own teddy bear,
 * penalties with their own white skull, everything else with the neutral bear.
 */
class EventBanner(private val host: ViewGroup) {

    fun show(overlay: GameOverlay) {
        val inflater = LayoutInflater.from(host.context)
        val binding = com.cyberpunk.debttracker.databinding.ItemEventBannerBinding.inflate(
            inflater, host, false,
        )

        val (seed, penalty) = when (overlay) {
            is GameOverlay.Reward -> overlay.code to false
            is GameOverlay.Penalty -> overlay.code to true
            is GameOverlay.Achievement -> overlay.code to false
            is GameOverlay.QuestDone -> overlay.id to false
            is GameOverlay.LevelUp -> "LVL_${overlay.to}_${overlay.title}" to false
            is GameOverlay.NpcLevelUp -> "NPC_${overlay.name}_LV${overlay.level}" to false
            is GameOverlay.NpcMoodShift -> "NPC_${overlay.name}_${overlay.mood}" to true
        }
        binding.ivBannerIcon.setImageDrawable(
            if (penalty) IconForge.skull(seed) else IconForge.bear(seed),
        )
        binding.bannerRoot.setBackgroundResource(
            if (penalty) R.drawable.bg_game_penalty_banner else R.drawable.bg_game_reward_banner,
        )

        binding.tvBannerTitle.text = when (overlay) {
            is GameOverlay.LevelUp -> overlay.title
            is GameOverlay.NpcLevelUp -> overlay.title
            is GameOverlay.NpcMoodShift -> overlay.title
            else -> overlay.title
        }
        binding.tvBannerBody.text = when (overlay) {
            is GameOverlay.Reward -> buildString {
                append(overlay.blurb)
                val parts = buildList {
                    if (overlay.xp != 0) add("+${overlay.xp} XP")
                    if (overlay.coins != 0) add("${if (overlay.coins > 0) "+" else ""}${GameFormat.short(overlay.coins)}₱")
                    if (overlay.bolts != 0) add("+${overlay.bolts}⚡")
                    if (overlay.nerve != 0) add("${if (overlay.nerve > 0) "+" else ""}${overlay.nerve} nerve")
                }
                if (parts.isNotEmpty()) append("  ").append(parts.joinToString("  "))
            }
            is GameOverlay.Penalty -> buildString {
                append(overlay.blurb)
                val parts = buildList {
                    if (overlay.coins != 0) add("${if (overlay.coins > 0) "+" else ""}${GameFormat.short(overlay.coins)}₱")
                    if (overlay.nerve != 0) add("${if (overlay.nerve > 0) "+" else ""}${overlay.nerve} nerve")
                }
                if (parts.isNotEmpty()) append("  ").append(parts.joinToString("  "))
            }
            else -> overlay.blurb
        }

        host.addView(binding.root)
        // Cap the stack so a big settlement spree cannot bury the screen.
        while (host.childCount > MAX_BANNERS) host.removeViewAt(0)

        binding.root.alpha = 0f
        binding.root.translationY = SLIDE_PX
        binding.root.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(180L)
            .withEndAction {
                binding.root.animate()
                    .alpha(0f)
                    .translationY(-SLIDE_PX / 2f)
                    .setStartDelay(HOLD_MS)
                    .setDuration(260L)
                    .withEndAction { host.removeView(binding.root) }
                    .start()
            }
            .start()
    }

    private companion object {
        const val MAX_BANNERS = 3
        const val HOLD_MS = 2200L
        const val SLIDE_PX = 48f
    }
}
