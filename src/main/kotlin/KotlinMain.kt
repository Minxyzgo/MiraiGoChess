@file:Suppress("RedundantSuspendModifier")

import BasePluginData.allGroup
import games.go.GoControl
import games.go.KatagoUtils
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText
import java.io.File

object KotlinMain : KotlinPlugin(
    JvmPluginDescription(
        id = "minxyzgo.goChess",
        name = "一个围棋插件",
        version = "0.0.1"
    )
) {
    var masterId = 0L
    var enableKatago = false
    var katagoPath: String? = null
    var configPath: String? = null
    val allGoChess = mutableMapOf<Long, GoControl>()

    override fun onEnable() {
        baseLoad()

        logger.info("test loaded")
        val cache = File("${System.getProperty("user.dir")}/cache")
        if (!cache.exists()) cache.mkdirs()
        if(enableKatago) {
            KatagoUtils.initKatagoSituationAnalysis(katagoPath!!, configPath!!)
        }

        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            if (group.id !in allGroup) return@subscribeAlways
            message.forEach {
                if(it is PlainText && it.content.startsWith(".")) {
                    val goControl = allGoChess[group.id]!!
                    goControl.main(it.content.removePrefix("."), group, sender)
                }
            }
        }
    }

    private fun baseLoad() {
        registerCommand()
        BasePluginData.reload()

        for(group in allGroup) {
            allGoChess[group] = GoControl()
        }
    }

    override fun onDisable() {
        if(enableKatago) {
            KatagoUtils.shutDown()

            for(chess in allGoChess.values) {
                chess.close()
            }
        }
    }

    @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
    fun registerCommand() {
        CommandManager.registerCommand(
            object: CompositeCommand(
                this,
                "enableGroup",
                description = "使某个群可以使用插件功能"
            ) {
                @SubCommand
                suspend fun CommandSender.add(groupId: Long) {
                    if(groupId in allGroup) {
                        KotlinMain.logger.error("群: $groupId 已经被添加!")
                        return
                    }
                    allGroup.add(groupId)
                    KotlinMain.logger.info("成功添加群: $groupId")
                }

                @SubCommand
                suspend fun CommandSender.list() {
                    KotlinMain.logger.info("开始列出所有群")
                    allGroup.forEach {
                        KotlinMain.logger.info("群: $it")
                    }
                }

                @SubCommand
                suspend fun CommandSender.remove(groupId: Long) {
                    if(groupId !in allGroup) {
                        KotlinMain.logger.error("群: $groupId 未被添加!")
                        return
                    }

                    allGroup.remove(groupId)
                }
            }
        )

    }
}