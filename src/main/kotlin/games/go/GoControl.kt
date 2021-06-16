package games.go

import KotlinMain
import com.beust.klaxon.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.at
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.text.DecimalFormat
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

/**
 * 棋盘与Mirai交互类 每次发送请使用 main方法
 * 释放资源请使用 close
 */
class GoControl : Closeable {
    var chessPad: ChessPad? = null
    var cachePlayer: Player? = null
    var ready = false
    var isStartingGame = false
    var timeOutJob: Job? = null
    var canCount = false
    var lastReadyNumber = 0L
    private val format = DecimalFormat("#.0000")
    private fun Double.format() = format.format(this)
    suspend fun main(
        base: String,
        contact: Contact,
        sender: Member
    ) {
        //下一回合
        fun nextTurn() {
            chessPad!!.blackMoving = !chessPad!!.blackMoving
        }
        //检查游戏否开始 并且发送者是否已经加入游戏
        fun checkPlayer(): Boolean {
            return chessPad != null && (sender.id == chessPad!!.blackPlayer.qqNumber || sender.id == chessPad!!.whitePlayer.qqNumber)
        }

        //根据颜色选择合适的玩家
        fun choosePlayer(player1: Player, player2: Player, color: StoneColor): Player {
            return if(color == player1.color) player1 else player2
        }

        //检查是否是发送者的回合
        suspend fun isYourRound() = (checkPlayer() && if(chessPad!!.blackMoving) sender.id == chessPad!!.blackPlayer.qqNumber else sender.id == chessPad!!.whitePlayer.qqNumber).also {
            if(!it) contact.sendMessage("这不是你的回合!")
        }
        //发送围棋图片
        suspend fun sendImage() = contact.launch(Dispatchers.IO) {
            val os = ByteArrayOutputStream()
            ImageIO.write(chessPad!!.currentImage, "png", os)
            val input = ByteArrayInputStream(os.toByteArray())
            contact.sendImage(input)
        }

        //初始化游戏
        suspend fun initGame() {
            isStartingGame = true
            chessPad!!.paint()
            cachePlayer = null
            sendImage()
        }

        //结束游戏，重载数据
        fun exitGame() {
            isStartingGame = false
            chessPad = null
            cachePlayer = null
            canCount = false
            timeOutJob?.let { if(it.isActive) it.cancel() }
            timeOutJob = null
        }

        //使发送者根据颜色加入游戏
        suspend fun acceptPlayer(color: StoneColor) {
            if (chessPad != null) {
                if (checkPlayer()) {
                    contact.sendMessage(sender.at() + "你已经参加了游戏, 不能再参加了!")
                    return
                } else {
                    contact.sendMessage(sender.at() + "游戏已经开始，你不能再参加了!")
                    return
                }
            }

            if (cachePlayer != null && cachePlayer!!.color == color){
                contact.sendMessage(sender.at() + "这个颜色已经被占用了, 快去换一种颜色吧")
                return
            }

            val player = Player(color, sender.id, user = sender)
            if (cachePlayer == null) {
                cachePlayer = player
                contact.sendMessage(sender.at() + "你已经成功加入游戏了! 快去找个小伙伴一起玩吧")
                return
            } else {
//                if(cachePlayer!!.qqNumber == sender.id) {
//                    contact.sendMessage(sender.at() + "你已经参加了游戏，不能再参加了!")
//                    return
//                }
                chessPad = ChessPad(
                    blackPlayer = choosePlayer(player, cachePlayer!!, StoneColor.Black),
                    whitePlayer = choosePlayer(player, cachePlayer!!, StoneColor.White)
                )

                contact.sendMessage("玩家准备就绪! 请双方都在30s内发送 '.就绪' 以确保双方在线后开始游戏。")
                timeOutJob = contact.launch {
                    delay(30000)
                    contact.sendMessage("游戏开始失败, 双方并未就绪")
                    exitGame()
                }

                return
            }
        }



        when(base) {
            "帮助" -> {
                contact.sendMessage("""
                    欢迎使用帮助! 下面是公开功能
                    .执黑或白，启动一个新的游戏。
                    .就绪 准备游戏。
                    .过 跳过回合
                    .投降 认输。
                    .下棋(坐标) 根据棋面坐标决定 如 .下棋J9 将会下棋在天元位置
                    当支持katago时，你可以使用
                    .数子 双方都同意后进行判断胜负，未启用时不可使用。
                    Powered by Minxyzgo
                    Katago 提供计算支持
                """.trimIndent())
            }
            "执黑" -> acceptPlayer(StoneColor.Black)
            "执白" -> acceptPlayer(StoneColor.White)
            "就绪" -> {
                if(timeOutJob == null && checkPlayer() && !isStartingGame) {
                    contact.sendMessage("玩家并未到齐!")
                } else {
                    if(checkPlayer() && !isStartingGame) {
                        if(lastReadyNumber == 0L || lastReadyNumber != sender.id) {
                            ready = if (ready) {
                                initGame()
                                timeOutJob?.cancel()
                                timeOutJob = null
                                false
                            } else {
                                true
                            }
                        } else {
                            contact.sendMessage("你已经就绪了!")
                        }
                    } else {
                        contact.sendMessage(sender.at() + "你没有参加游戏")
                    }
                }
            }

            "数子" -> {
                if(checkPlayer() && isStartingGame) {
                    if (canCount && KotlinMain.enableKatago && isYourRound()) {
                        lateinit var json: JsonObject
                        val time = measureTimeMillis {
                            json = KatagoUtils.analysis(chessPad!!.schedule, "check@${sender.id}", true)!!
                        }
                        val root = json.obj("rootInfo")!!
                        println("查询id: ${json.string("id")}")
                        val scoreLead = root.double("scoreLead")!!
                        val blackWin = scoreLead >= 0
                        //设置你的katago 查询黑棋
                        contact.sendMessage("""
                            查询成功!结果: 黑棋领先${ scoreLead.format() }
                            ${if(blackWin) "黑" else "白"}棋胜利，游戏结束!
                            查询用时: ${time / 1000}s
                        """.trimIndent())
                        exitGame()
                    } else if(!KotlinMain.enableKatago) {
                        contact.sendMessage("当前未启用katago!")
                    } else if(!canCount && isYourRound()) {
                        nextTurn()
                        contact.sendMessage("对方请求数子，双方都输入'.数子'即可判断胜负")
                        canCount = true
                    }
                }
            }

            "过" -> {
                if(checkPlayer() && isStartingGame && isYourRound()) {
                    contact.sendMessage("你已跳过回合")
                    timeOutJob?.cancel()
                    nextTurn()
                }
            }

            "投降" -> {
                if(checkPlayer() && isStartingGame && isYourRound()) {
                    contact.sendMessage("你已投子认负，游戏结束")
                    exitGame()
                }
            }

            "查询" -> {
                if(checkPlayer() && isStartingGame && sender.id == KotlinMain.masterId) {
                    if(KotlinMain.enableKatago) {

                        lateinit var json: JsonObject
                        val time = measureTimeMillis {
                            json = KatagoUtils.analysis(chessPad!!.schedule, "check@${sender.id}", true)!!
                        }
                        val root = json.obj("rootInfo")!!
                        val scoreLead = root.double("scoreLead")!!
                        val scoreSelfplay = root.double("scoreSelfplay")!!
                        val scoreStdev = root.double("scoreStdev")!!
                        val winrate = root.double("winrate")!!

                        contact.sendMessage("""
                            你的scoreLead为${scoreLead.format()}
                            scoreStdev为${scoreStdev.format()}
                            scoreSelfplay为${scoreSelfplay.format()}
                            胜率为${winrate.format()}
                            总用时${time / 1000}s
                        """.trimIndent())
                    } else {
                        contact.sendMessage("katago未启用!")
                    }
                } else {
                    contact.sendMessage("你没有权利")
                }
            }

            "预览" -> {
                if(sender.id == KotlinMain.masterId) {
                    chessPad = ChessPad(
                        Player(StoneColor.Black, 0),
                        Player(StoneColor.White, 1)
                    )

                    initGame()
                }
            }

            "结束" -> {
                if(sender.id == KotlinMain.masterId) {
                    exitGame()
                }
            }
        }

        if(base.startsWith("下棋") && isStartingGame && checkPlayer() && isYourRound()) {
            try {


                val remove = base.removePrefix("下棋")
                val xstr = remove.first()
                val x = (xstr.toInt() - 'A'.toInt())
                val y = remove.removePrefix(xstr.toString()).toInt()
                if(!chessPad!!.placeStone(x, y, contact, sender)) return
                timeOutJob?.cancel()
                sendImage()

                val timeOutPlayer = if(chessPad!!.blackMoving) chessPad!!.whitePlayer else chessPad!!.blackPlayer
                timeOutJob = contact.launch {
                    delay(50000)
                    contact.sendMessage(timeOutPlayer.user!!.at() + "请尽快下棋，否则因为超时而判负!")
                    delay(50000)
                    exitGame()
                    try {
                        contact.sendMessage("玩家${timeOutPlayer.user.nameCard}因为超时而失败，游戏结束")
                    } catch(e: Exception) {
                        //有时候会抛出，暂不知道原因
                        contact.sendMessage("有玩家超时判负，游戏结束")
                    }
                }
            } catch (e: NumberFormatException) {
                contact.sendMessage("请输入正确的坐标")
            }
        }
    }

    override fun close() {
        chessPad?.graphics?.dispose()
    }
}