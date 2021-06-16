package games.go

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D


object Utils {
    private val strokeLine = BasicStroke(1.5f)

    fun getVectorMar(index: Int) = ('A'.toInt() + index).toChar().toString()

    fun placeStone(player: Player, x: Int, y: Int, graphics: Graphics) {
        graphics.color = player.color.awt
        graphics.fillOval(x, y, 20, 20)
    }

    fun takeStone(x: Int, y: Int, graphics: Graphics) = graphics.clearRect(x, y, 20, 20)

    fun drawTeNum(x: Int, y: Int, teNum: Int, color: Color, graphics: Graphics) {
        val font = graphics.font
        val metrics = graphics.getFontMetrics(font)
        val teX = x + (20 - metrics.stringWidth(teNum.toString())) / 2
        val teY = y + ((20 - metrics.height) / 2) + metrics.ascent
        graphics.color = Color.ORANGE
        graphics.font = font
        graphics.drawString(teNum.toString(), teX, teY)
    }

    fun highLightLastStone(
        coordinate_x: Int, coordinate_y: Int,
        last_coordinate_x: Int, last_coordinate_y: Int,
        move: Array<Array<StoneColor>>, teNum: Int, graphics: Graphics
    ) {
        graphics.color = Color.RED
        val drawX = (coordinate_x + 1) * 25 + 10
        val drawY = (coordinate_y + 1) * 25 + 10
        //
        val g = graphics as Graphics2D
        g.stroke = strokeLine
        g.drawOval(drawX, drawY, 20, 20)
        // 如果手数大于1，把倒数第二手的红色边框去除
        if (teNum > 1) {
            removeLastButOneLight(last_coordinate_x, last_coordinate_y, move, g)
        }
    }



    private fun removeLastButOneLight(
        last_coordinate_x: Int,
        last_coordinate_y: Int,
        move: Array<Array<StoneColor>>,
        g: Graphics
    ) {
        val drawX = (last_coordinate_x + 1) * 25 + 10
        val drawY = (last_coordinate_y + 1) * 25 + 10
        if (move[last_coordinate_x][last_coordinate_y] == StoneColor.Black) {
            g.color = Color.BLACK
        }
        if (move[last_coordinate_x][last_coordinate_y] == StoneColor.White) {
            g.color = Color.WHITE
        }
        g.drawOval(drawX, drawY, 20, 20)
        g.color = Color.ORANGE
        g.drawOval(drawX, drawY, 20, 20)
    }
}