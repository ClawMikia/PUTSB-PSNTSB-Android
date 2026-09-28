package com.cyberpunk.debttracker.data.db

import androidx.room.TypeConverter
import com.cyberpunk.debttracker.data.model.game.LogKind
import com.cyberpunk.debttracker.data.model.game.NpcMood

class GameConverters {

    @TypeConverter
    fun fromNpcMood(value: NpcMood): String = value.name

    @TypeConverter
    fun toNpcMood(value: String): NpcMood =
        runCatching { NpcMood.valueOf(value) }.getOrDefault(NpcMood.NEUTRAL)

    @TypeConverter
    fun fromLogKind(value: LogKind): String = value.name

    @TypeConverter
    fun toLogKind(value: String): LogKind =
        runCatching { LogKind.valueOf(value) }.getOrDefault(LogKind.SYSTEM)
}
