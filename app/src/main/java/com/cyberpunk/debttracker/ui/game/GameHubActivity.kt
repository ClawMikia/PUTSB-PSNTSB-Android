package com.cyberpunk.debttracker.ui.game

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.game.PlayerProfile
import com.cyberpunk.debttracker.databinding.ActivityGameHubBinding
import com.cyberpunk.debttracker.game.Cycles
import com.cyberpunk.debttracker.game.GameEngine
import com.cyberpunk.debttracker.game.LevelCurve
import com.cyberpunk.debttracker.game.QuestCycle
import com.cyberpunk.debttracker.util.applySystemBarInsets
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.cyberpunk.debttracker.util.enableCyberEdgeToEdge

/**
 * The single-player campaign screen: player hero card, quest tabs, the 100
 * achievement wall, the NPC roster and the event log.
 */
@AndroidEntryPoint
class GameHubActivity : AppCompatActivity() {

    @Inject
    lateinit var game: GameEngine

    private lateinit var binding: ActivityGameHubBinding
    private lateinit var banner: EventBanner

    private val questAdapter = QuestAdapter()
    private val achievementAdapter = AchievementAdapter()
    private val logAdapter = LogAdapter()
    private lateinit var npcAdapter: NpcAdapter

    private var tab: Tab = Tab.DAILY
    private var resetTicker: Runnable? = null
    private var tabJob: Job? = null

    private enum class Tab(val titleRes: Int, val emptyRes: Int) {
        DAILY(R.string.game_tab_daily, R.string.game_no_log),
        MONTHLY(R.string.game_tab_monthly, R.string.game_no_log),
        ANNUAL(R.string.game_tab_annual, R.string.game_no_log),
        FEATS(R.string.game_tab_achievements, R.string.game_no_log),
        CONTACTS(R.string.game_tab_npcs, R.string.game_no_contacts),
        LOG(R.string.game_tab_log, R.string.game_no_log),
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableCyberEdgeToEdge()
        binding = ActivityGameHubBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()
        banner = EventBanner(binding.overlayHost)

        setupToolbar()
        setupTabs()
        setupRecycler()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (tab != Tab.DAILY) {
                    binding.tabLayout.getTabAt(0)?.select()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        observeGame()
        startResetTicker()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnPurge.setOnClickListener { confirmPurge() }
    }

    private fun setupTabs() {
        Tab.values().forEach { entry ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(entry.titleRes).setTag(entry))
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                this@GameHubActivity.tab = tab.tag as Tab
                showCurrentTab()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun setupRecycler() {
        npcAdapter = NpcAdapter(
            onNudge = { npc ->
                lifecycleScope.launch {
                    val ok = game.nudgeNpc(npc)
                    if (!ok) toast(getString(R.string.game_nudged_already))
                }
            },
            onPacify = { npc ->
                lifecycleScope.launch {
                    val ok = game.pacifyNpc(npc)
                    if (!ok) {
                        toast(getString(R.string.game_pacify_poor, GameEngine.PACIFY_COST, npcAdapter.coins))
                    }
                }
            },
            onConfront = { npc ->
                lifecycleScope.launch { game.confrontNpc(npc) }
            },
        )
        binding.recyclerGame.layoutManager = LinearLayoutManager(this)
        binding.recyclerGame.adapter = questAdapter
        binding.recyclerGame.setHasFixedSize(false)
    }

    private fun observeGame() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { game.profile.collectLatest { renderProfile(it) } }
                launch { game.overlays.collect { banner.show(it) } }
                attachTabCollector()
            }
        }
    }

    /** Swaps the collector for whichever tab is active, cancelling the old one. */
    private fun attachTabCollector() {
        tabJob?.cancel()
        tabJob = lifecycleScope.launch { collectCurrentTab() }
    }

    private suspend fun collectCurrentTab() {
        when (tab) {
            Tab.DAILY -> game.questStates(QuestCycle.DAILY).collectLatest {
                questAdapter.submitList(questRows(QuestCycle.DAILY, it))
                updateEmpty()
            }
            Tab.MONTHLY -> game.questStates(QuestCycle.MONTHLY).collectLatest {
                questAdapter.submitList(questRows(QuestCycle.MONTHLY, it))
                updateEmpty()
            }
            Tab.ANNUAL -> game.questStates(QuestCycle.ANNUAL).collectLatest {
                questAdapter.submitList(questRows(QuestCycle.ANNUAL, it))
                updateEmpty()
            }
            Tab.FEATS -> game.achievements.collectLatest {
                achievementAdapter.submitList(achievementRows(it))
                updateEmpty()
            }
            Tab.CONTACTS -> game.npcs.collectLatest {
                npcAdapter.submitList(it)
                updateEmpty()
            }
            Tab.LOG -> game.logEntries.collectLatest {
                logAdapter.submitList(it)
                updateEmpty()
                game.markLogRead()
            }
        }
    }

    private fun showCurrentTab() {
        val adapter = when (tab) {
            Tab.DAILY, Tab.MONTHLY, Tab.ANNUAL -> questAdapter
            Tab.FEATS -> achievementAdapter
            Tab.CONTACTS -> npcAdapter
            Tab.LOG -> logAdapter
        }
        binding.recyclerGame.adapter = adapter
        binding.tvEmpty.setText(tab.emptyRes)
        attachTabCollector()
        startResetTicker()
    }

    private fun updateEmpty() {
        val adapter = binding.recyclerGame.adapter
        val count = when (adapter) {
            is QuestAdapter -> adapter.itemCount
            is AchievementAdapter -> adapter.itemCount
            is NpcAdapter -> adapter.itemCount
            is LogAdapter -> adapter.itemCount
            else -> 0
        }
        binding.tvEmpty.isVisible = count == 0
    }

    private fun renderProfile(profile: PlayerProfile?) {
        if (profile == null) {
            binding.tvLevel.text = getString(R.string.game_level_format, 1)
            binding.tvRank.text = LevelCurve.rankTitle(1)
            npcAdapter.coins = 0
            return
        }
        val curve = LevelCurve.evaluate(profile.xp, profile.level)

        binding.tvLevel.text = getString(R.string.game_level_format, profile.level)
        binding.tvRank.text = profile.rankTitle.ifEmpty { LevelCurve.rankTitle(profile.level) }

        binding.tvCurrency.text = GameFormat.peso(profile.coins)
        binding.tvBolts.text = getString(R.string.game_bolts_format, profile.bolts)
        // Keeps the PACIFY button's affordability in sync with the wallet.
        npcAdapter.coins = profile.coins

        binding.progressXp.progress = curve.progressPercent
        binding.tvXp.text = getString(
            R.string.game_xp_format,
            curve.xpIntoLevel, curve.needed.coerceAtLeast(1),
        )

        binding.progressNerve.progress = profile.nerve
        binding.tvNerve.text = getString(R.string.game_nerve_format, profile.nerve, PlayerProfile.MAX_NERVE)

        binding.tvStreak.text = if (profile.streakDays > 0) {
            getString(R.string.game_streak_format, profile.streakDays)
        } else {
            "NO ACTIVE STREAK"
        }
    }

    private fun startResetTicker() {
        resetTicker?.let { binding.root.removeCallbacks(it) }
        val cycle = when (tab) {
            Tab.MONTHLY -> QuestCycle.MONTHLY
            Tab.ANNUAL -> QuestCycle.ANNUAL
            else -> QuestCycle.DAILY
        }
        val ticker = object : Runnable {
            override fun run() {
                binding.tvReset.text = getString(
                    R.string.game_resets_in,
                    Cycles.formatDuration(Cycles.millisUntilReset(cycle)),
                )
                binding.root.postDelayed(this, 30_000L)
            }
        }
        resetTicker = ticker
        binding.tvReset.text = getString(
            R.string.game_resets_in,
            Cycles.formatDuration(Cycles.millisUntilReset(cycle)),
        )
    }

    private fun confirmPurge() {
        AlertDialog.Builder(this, R.style.Theme_DebtTracker_Dialog)
            .setTitle(R.string.game_purge_title)
            .setMessage(R.string.game_purge_message)
            .setPositiveButton(R.string.game_purge_confirm) { _, _ ->
                lifecycleScope.launch {
                    game.purgeCampaign()
                    toast(getString(R.string.game_purge_done))
                }
            }
            .setNegativeButton(R.string.game_purge_cancel, null)
            .show()
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        resetTicker?.let { binding.root.removeCallbacks(it) }
        super.onDestroy()
    }
}
