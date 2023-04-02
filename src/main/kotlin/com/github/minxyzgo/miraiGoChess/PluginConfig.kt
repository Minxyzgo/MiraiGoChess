package com.github.minxyzgo.miraiGoChess

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PluginConfig : AutoSavePluginConfig("GoChess") {
    @ValueDescription("设置kataGo执行程序路径。留空代表不启用")
    var kataGoPath by value("")

    @ValueDescription("设置kataGo配置文件路径。启用必填")
    var configPath by value("")

    @ValueDescription("设置kataGo训练文件路径。默认为default_model.bin.gz")
    var modelPath by value("")

    @ValueDescription("设置玩家最大悔棋次数")
    val maxRegretChance by value(3)

    @ValueDescription("设置katago并行分析线程数")
    val analysisThreads by value(2)
}