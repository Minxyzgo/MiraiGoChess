package com.github.minxyzgo.miraiGoChess.go

import com.github.minxyzgo.miraiGoChess.PluginConfig
import net.mamoe.mirai.contact.Member


class Player(
    val color: StoneColor,
    val qqNumber: Long,
    val user: Member? = null
) {
    var regretChance = PluginConfig.maxRegretChance
}