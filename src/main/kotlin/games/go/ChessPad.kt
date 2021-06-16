package games.go

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.at
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage


class ChessPad(
    val blackPlayer: Player,
    val whitePlayer: Player
    ) {
    /**
     * 声明Player类存储棋手下棋顺序
     * 声明落子绘图类用于绘制棋子
     * 声明teNum类用于绘制手数
     * 声明highLight高亮最后一手
     * 声明19*19 move数组，存储已落子的信息
     * 声明teNum记录手数
     * 声明move_teNum 记录每一个坐标的棋子是第几手棋
     * 声明上一手的坐标last_coordinate_x，last_coordinate_y
     */

    val schedule = mutableListOf<IntArray>()

    var currentImage: BufferedImage = BufferedImage(560, 600, BufferedImage.TYPE_INT_RGB)
    var graphics: Graphics2D = currentImage.createGraphics()
    var blackMoving = true
    var lastIllogical = false
    private var move: Array<Array<StoneColor>> = Array(19) { Array(19) { StoneColor.None } }
    private var teNum: Int = 1
    private var moveTeNum: Array<Array<Int>> = Array(19) { Array(19) { -1 } }
    private var lastCoordinateX: Int
    private var lastCoordinateY: Int

    fun paint() {
        graphics.background = Color.ORANGE
        graphics.clearRect(0, 0, 560, 600)
        var x = 0
        var y = 0

        graphics.color = Color.BLACK
        run {
            var i = 45
            while (i <= 495) {
                graphics.drawString(Utils.getVectorMar(x), i, 30)
                graphics.drawLine(i, 45, i, 495)
                i += 25
                x++
            }
        }
        var i = 45
        while (i <= 495) {
            graphics.drawString(y.toString(), 30, i)
            graphics.drawLine(45, i, 495, i)
            i += 25
            y++
        }
        //D16
        graphics.fillOval(116, 116, 8, 8)
        //Q4
        graphics.fillOval(416, 416, 8, 8)
        //D4
        graphics.fillOval(116, 416, 8, 8)
        //Q16
        graphics.fillOval(416, 116, 8, 8)
        //D10
        graphics.fillOval(116, 266, 8, 8)
        //K16
        graphics.fillOval(266, 116, 8, 8)
        //Q10
        graphics.fillOval(416, 266, 8, 8)
        //K4
        graphics.fillOval(266, 416, 8, 8)
        //天元
        graphics.fillOval(266, 266, 8, 8)
    }

    suspend fun placeStone(
        coordinateX: Int,
        coordinateY: Int,
        contact: Contact,
        sender: Member
    ): Boolean {

        schedule.add(intArrayOf(coordinateX, coordinateY))

        // 这里用棋盘坐标乘以棋盘每路之间的宽度 -- 25
        // 再加上棋子的宽度、高度的一半 -- 10
        // 得到的是落子类绘图方法需要的坐标
        val placeX = (coordinateX + 1) * 25 + 10
        val placeY = (coordinateY + 1) * 25 + 10

        var redraw = false

        suspend fun place(player: Player): Boolean {
            if(sender.id != player.qqNumber) {
                contact.sendMessage(sender.at() + "现在不是你下棋!")
                return false
            }

            val move2 = move.copyOf()
            move2[coordinateX][coordinateY] = player.color
            if (TakeRules.takeStones(move2, coordinateX, coordinateY)) {
                if(!takeStones(move2, sender, contact, coordinateX, coordinateY)) return false
                redraw = true
                println("提子")
            } else {
                println("落子")
            }
            // 落子、绘图
            Utils.placeStone(player, placeX, placeY, this.graphics.create())
            // 绘制手数
            Utils.drawTeNum(
                placeX,
                placeY,
                teNum,
                player.color.awt,
                this.graphics.create()
            )
            // 设置有子
            moveTeNum[coordinateX][coordinateY] = teNum
            teNum++
            move[coordinateX][coordinateY] = player.color
            if(redraw) redraw()
            return true

        }


        // 判断是否在棋盘内
        if (isInBoard(coordinateX, coordinateY)) {
            if (!isAlreadyHadStone(move, coordinateX, coordinateY)) {
                if(!place(if(blackMoving) blackPlayer else whitePlayer)) return false


                // 高亮最后一手，并将倒数第二手的高亮去除
                Utils.highLightLastStone(
                    coordinateX,
                    coordinateY,
                    lastCoordinateX,
                    lastCoordinateY,
                    move,
                    teNum - 1,
                    this.graphics
                )

                lastCoordinateX = coordinateX
                lastCoordinateY = coordinateY
                // 两级反转.表明包
                blackMoving = !blackMoving
                return true
            } else {
                contact.sendMessage("该位置已有子。请换一个位置")
                return false
            }
        } else {
            contact.sendMessage("请输入正确的坐标")
            return false
        }
    }

    // 提子
    private suspend fun takeStones(
        allStone: Array<Array<StoneColor>>,
        sender: Member,
        contact: Contact,
        x: Int,
        y: Int
    ): Boolean {
        var coordinateX: Int
        var coordinateY: Int
        var removeX: Int
        var removeY: Int
        // 获得提子数量
        val length: Array<IntArray> = TakeRules.length
        // 获得提子坐标

        val takeStones: Array<Array<IntArray>> = TakeRules.takeStones
        var illogical = 0
        val cacheIllogical = lastIllogical
        for(i in 0..3) {
            illogical += length[i][0]
        }

        for (i in 0..3) {
            // 如果记录的数量不为0，有子可提
            if (length[i][0] != 0) {
                for (j in 0 until length[i][0]) {
                    // 获得要提的子的坐标
                    coordinateX = takeStones[i][j][0]
                    coordinateY = takeStones[i][j][1]
                    // 将坐标转换为绘图坐标
                    removeX = (coordinateX + 1) * 25 + 10
                    removeY = (coordinateY + 1) * 25 + 10

                    val lastStone = allStone[coordinateX][coordinateY]


                    allStone[coordinateX][coordinateY] = StoneColor.None
                    // 提子
                    Utils.takeStone(removeX, removeY, this.graphics)
                    if(illogical == 1) {
                        if (coordinateX == lastCoordinateX && coordinateY == lastCoordinateY) {
                            if(lastIllogical) {
                                contact.sendMessage("触发打劫，请换一个位置下棋")
                                return false
                            } else {
                                lastIllogical = true
                            }
                        }
                        println("上一次提子: $lastStone x: $coordinateX y: $coordinateY")
                    }
                }
            }
        }

        if(allStone[x][y] == StoneColor.None) {
            contact.sendMessage("触发自杀，请换一个位置下棋")
            return false
        }

        if(cacheIllogical == lastIllogical) lastIllogical = false

        return true
    }

    private fun redraw() {
        graphics.dispose()
        currentImage = BufferedImage(560, 600, BufferedImage.TYPE_INT_RGB)
        graphics = currentImage.createGraphics()
        paint()
        // 重绘仍在棋盘上的棋子
        println("正在重新绘制棋盘")
        for (i in 0..18) {
            for (j in 0..18) {
                if (move[i][j] == StoneColor.Black) {
                    println("绘制黑$i $j")
                    Utils.placeStone(blackPlayer, (i + 1) * 25 + 10, (j + 1) * 25 + 10, this.graphics.create())
                    Utils.drawTeNum(
                        (i + 1) * 25 + 10, (j + 1) * 25 + 10,
                        moveTeNum[i][j].also { println("teNum$it") }, move[i][j].awt, this.graphics.create()
                    )
                }
                if (move[i][j] == StoneColor.White) {
                    println("绘制白$i $j")
                    Utils.placeStone(whitePlayer, (i + 1) * 25 + 10, (j + 1) * 25 + 10, this.graphics.create())
                    Utils.drawTeNum(
                        (i + 1) * 25 + 10, (j + 1) * 25 + 10,
                        moveTeNum[i][j].also { println("teNum$it") }, move[i][j].awt, this.graphics.create()
                    )
                }
                if (moveTeNum[i][j] == teNum) {
                    Utils.highLightLastStone(i, j, 0, 0, move, 0, graphics)
                }
            }
        }
    }

    /**
     * 构造棋盘大小、背景、鼠标监听器
     */
    init {
        // 初始化棋谱数组、手数数组

        //初始化手数、最后一手的坐标
        lastCoordinateX = 0
        lastCoordinateY = 0
    }
}
