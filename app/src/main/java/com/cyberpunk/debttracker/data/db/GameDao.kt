package com.cyberpunk.debttracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.cyberpunk.debttracker.data.model.game.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    // ─── Player ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM player_profile WHERE slot = :slot LIMIT 1")
    fun observeProfile(slot: Int = PlayerProfile.SINGLETON): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE slot = :slot LIMIT 1")
    suspend fun getProfile(slot: Int = PlayerProfile.SINGLETON): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfileIfAbsent(profile: PlayerProfile)

    @Upsert
    suspend fun upsertProfile(profile: PlayerProfile)

    @Query("SELECT * FROM rollover_state WHERE id = :id LIMIT 1")
    suspend fun getRollover(id: Int = RolloverState.SINGLETON): RolloverState?

    @Upsert
    suspend fun upsertRollover(state: RolloverState)

    // ─── Counters ─────────────────────────────────────────────────────────────

    @Query("SELECT value FROM stat_counters WHERE stat_key = :key")
    suspend fun getCounter(key: String): Int?

    @Query("SELECT * FROM stat_counters")
    fun observeAllCounters(): Flow<List<StatCounter>>

    @Query("SELECT * FROM stat_counters")
    suspend fun getAllCounters(): List<StatCounter>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCounterIfAbsent(counter: StatCounter)

    @Query("UPDATE stat_counters SET value = :value WHERE stat_key = :key")
    suspend fun setCounter(key: String, value: Int)

    @Query("UPDATE stat_counters SET value = value + :delta WHERE stat_key = :key")
    suspend fun bumpCounter(key: String, delta: Int)

    @Query("UPDATE stat_counters SET value = MAX(value, :floor) WHERE stat_key = :key")
    suspend fun raiseCounterFloor(key: String, floor: Int)

    @Transaction
    suspend fun addToCounter(key: String, delta: Int, floor: Int? = null) {
        insertCounterIfAbsent(StatCounter(key, 0))
        if (delta != 0) bumpCounter(key, delta)
        if (floor != null) raiseCounterFloor(key, floor)
    }

    @Transaction
    suspend fun maxCounter(key: String, value: Int) {
        insertCounterIfAbsent(StatCounter(key, 0))
        raiseCounterFloor(key, value)
    }

    /** Absolute assignment — used for derived / recomputed counters only. */
    @Transaction
    suspend fun setCounterValue(key: String, value: Int) {
        insertCounterIfAbsent(StatCounter(key, 0))
        setCounter(key, value)
    }

    // ─── Gauges ───────────────────────────────────────────────────────────────

    @Query("SELECT value FROM stat_gauges WHERE stat_key = :key")
    suspend fun getGauge(key: String): Int?

    @Query("SELECT * FROM stat_gauges")
    fun observeAllGauges(): Flow<List<StatGauge>>

    @Upsert
    suspend fun upsertGauge(gauge: StatGauge)

    @Query("SELECT * FROM stat_gauges")
    suspend fun getAllGauges(): List<StatGauge>

    @Transaction
    suspend fun setGauges(gauges: List<StatGauge>) {
        gauges.forEach { upsertGauge(it) }
    }

    // ─── Cycle baselines ──────────────────────────────────────────────────────

    @Query("SELECT value FROM cycle_baseline WHERE cycle_key = :cycle AND stat_key = :key")
    suspend fun getBaseline(cycle: String, key: String): Int?

    @Query("SELECT * FROM cycle_baseline WHERE cycle_key = :cycle")
    suspend fun getBaselines(cycle: String): List<CycleBaseline>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBaselineIfAbsent(baseline: CycleBaseline)

    // ─── Achievements ─────────────────────────────────────────────────────────

    @Query("SELECT * FROM achievement_state")
    fun observeAchievementStates(): Flow<List<AchievementState>>

    @Query("SELECT * FROM achievement_state WHERE unlocked = 0")
    suspend fun getLockedAchievements(): List<AchievementState>

    @Upsert
    suspend fun upsertAchievementState(state: AchievementState)

    @Query("SELECT COUNT(*) FROM achievement_state WHERE unlocked = 1")
    fun observeUnlockedCount(): Flow<Int>

    @Query("SELECT * FROM achievement_state")
    suspend fun getAchievementStates(): List<AchievementState>

    @Query("SELECT COUNT(*) FROM achievement_state WHERE unlocked = 1")
    suspend fun getUnlockedCount(): Int

    // ─── Quests ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM quest_state WHERE cycle_key = :cycle")
    fun observeQuestStates(cycle: String): Flow<List<QuestState>>

    @Query("SELECT * FROM quest_state WHERE cycle_key = :cycle")
    suspend fun getQuestStates(cycle: String): List<QuestState>

    @Upsert
    suspend fun upsertQuestState(state: QuestState)

    @Query("SELECT COUNT(*) FROM quest_state WHERE cycle_key = :cycle AND completed = 1")
    fun observeQuestCompletedCount(cycle: String): Flow<Int>

    @Query("SELECT * FROM quest_state WHERE cycle_key = :cycle AND completed = 1")
    suspend fun getCompletedQuests(cycle: String): List<QuestState>

    // ─── NPCs ─────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM npcs ORDER BY level DESC, name COLLATE NOCASE ASC")
    fun observeAllNpcs(): Flow<List<Npc>>

    @Query("SELECT * FROM npcs WHERE name_key = :nameKey LIMIT 1")
    suspend fun getNpc(nameKey: String): Npc?

    @Query("SELECT * FROM npcs WHERE id = :id LIMIT 1")
    fun observeNpc(id: Long): Flow<Npc?>

    @Query("SELECT * FROM npcs")
    suspend fun getAllNpcs(): List<Npc>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNpcIfAbsent(npc: Npc): Long

    @Upsert
    suspend fun upsertNpc(npc: Npc)

    @Query("DELETE FROM npcs")
    suspend fun clearNpcs()

    // ─── Battle log ───────────────────────────────────────────────────────────

    @Query("SELECT * FROM game_log ORDER BY at DESC LIMIT :limit")
    fun observeRecentLog(limit: Int = 60): Flow<List<GameLogEntry>>

    @Query("SELECT * FROM game_log ORDER BY at DESC LIMIT :limit")
    suspend fun getRecentLog(limit: Int = 60): List<GameLogEntry>

    @Insert
    suspend fun insertLog(entry: GameLogEntry): Long

    @Query("DELETE FROM game_log WHERE id NOT IN (SELECT id FROM game_log ORDER BY at DESC LIMIT :keep)")
    suspend fun trimLog(keep: Int = 400)

    @Query("SELECT COUNT(*) FROM game_log WHERE read = 0")
    fun observeUnreadLogCount(): Flow<Int>

    @Query("UPDATE game_log SET read = 1")
    suspend fun markLogRead()

    // ─── Campaign purge (in-app restart, mirrors a fresh install) ─────────────

    @Query("DELETE FROM game_log")
    suspend fun clearLog()

    @Query("DELETE FROM achievement_state")
    suspend fun clearAchievementStates()

    @Query("DELETE FROM quest_state")
    suspend fun clearQuestStates()

    @Query("DELETE FROM cycle_baseline")
    suspend fun clearBaselines()

    @Query("DELETE FROM stat_counters")
    suspend fun clearCounters()

    @Query("DELETE FROM stat_gauges")
    suspend fun clearGauges()

    @Transaction
    suspend fun purgeCampaign() {
        clearNpcs()
        clearLog()
        clearAchievementStates()
        clearQuestStates()
        clearBaselines()
        clearCounters()
        clearGauges()
    }
}
