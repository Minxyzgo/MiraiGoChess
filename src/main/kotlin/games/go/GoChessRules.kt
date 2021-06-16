package games.go

fun isAlreadyHadStone(move: Array<Array<StoneColor>>, x: Int, y: Int): Boolean {
    return move[x][y] != StoneColor.None
}

fun isInBoard(x: Int, y: Int): Boolean {
    return (0..18).let { x in it && y in it }
}

object LibertyRules {
    private val visited = Array(19) { IntArray(19) }

    // 声明上下左右四个方向
    private val directions = arrayOf(intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, -1))

    // 声明记录提子的坐标的二维数组
    private val liberty_takeStones = Array(19) { IntArray(2) }

    // 声明记录二维数组的长度
    private var liberty_length = 0

    // 记录数组初始化函数
    private fun setUpVisited() {
        for (i in 0..18) {
            for (j in 0..18) {
                visited[i][j] = 0
            }
        }
    }

    private fun setUpTakeStones() {
        for (i in 0..18) {
            for (j in 0..1) {
                liberty_takeStones[i][j] = 0
            }
        }
    }

    private fun dfs(move: Array<Array<StoneColor>>, coordinate_x: Int, coordinate_y: Int): Boolean {
        var directionX: Int
        var directionY: Int
        // 设置已访问标志1
        visited[coordinate_x][coordinate_y] = 1
        // 将当前子的坐标存入提子数组，数组长度+1
        liberty_takeStones[liberty_length][0] = coordinate_x
        liberty_takeStones[liberty_length][1] = coordinate_y
        liberty_length++
        // 遍历上下左右四个方向
        for (i in 0..3) {
            directionX = coordinate_x + directions[i][0]
            directionY = coordinate_y + directions[i][1]
            // 判断是否在棋盘内
            if (!isInBoard(directionX, directionY)) {
                // 不在棋盘内就遍历下一个点
                continue
            } else if (visited[directionX][directionY] == 0) {
                // 如果该位置无子，则有气，返回true
                if (!isAlreadyHadStone(move, directionX, directionY)) {
                    // 这些输出是在debug的时候用的，可以删掉
                    println("有气： $directionX $directionY")
                    return true
                }
                // 如果该位置有子，且子的颜色不同，就遍历下一个点
                if (move[directionX][directionY] !== move[coordinate_x][coordinate_y]) {
                    println("不同色： $directionX $directionY")
                    continue
                }
                // 如果该位置有子，且颜色相同，递归遍历该子
                if (move[directionX][directionY] === move[coordinate_x][coordinate_y]) {
                    println("同色： $directionX $directionY")
                    //如果下一个子返回true
                    if (dfs(move, directionX, directionY)) {
                        return true
                    }
                }
            }
        }
        // 如果遍历完都没气，返回false
        return false
    }

    // 判断是否有气函数
    fun hasLiberty(move: Array<Array<StoneColor>>, coordinate_x: Int, coordinate_y: Int): Boolean {
        // 初始化遍历记录访问数组
        setUpVisited()
        setUpTakeStones()
        // 重置记录长度
        liberty_length = 0
        println("hasLiberty开始: $coordinate_x $coordinate_y")
        return if (dfs(move, coordinate_x, coordinate_y)) {
            println("hasLiberty结束，返回true")
            true
        } else {
            println("hasLiberty结束，返回false")
            false
        }
    }

    fun getTakeStones(): Array<IntArray> {
        return liberty_takeStones
    }

    fun getLength(): Int {
        return liberty_length
    }
}

object TakeRules {
    // 声明上下左右四个方向
    private val directions = arrayOf(intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, -1))

    // 记录上下左右四颗子的hasLiberty返回的长度
    val length = Array(4) { IntArray(1) }

    // 记录上下左右四颗子的hasLiberty返回的提子数组，这里感觉提的子不会很多，因此长度只有19
    // 4表示4个方向
    // 19表示第N个要提的子
    // 最后两位表示第N个要提的子的x、y坐标

    val takeStones = Array(4) {
        Array(19) {
            IntArray(
                2
            )
        }
    }

    // 初始化length
    private fun setUpLength() {
        for (i in 0..3) {
            length[i][0] = 0
        }
    }

    // 初始化takeStones
    private fun setUpTakeStones() {
        for (i in 0..3) {
            for (j in 0..18) {
                for (k in 0..1) {
                    takeStones[i][j][k] = 0
                }
            }
        }
    }

    // 提子函数
    fun takeStones(
        move: Array<Array<StoneColor>>,
        coordinate_x: Int,
        coordinate_y: Int
    ): Boolean {
        // flag为1则有子可提
        var flag = 0
        // 初始化记录数组
        setUpLength()
        setUpTakeStones()
        var directionX: Int
        var directionY: Int
        // 获得当前局面最后一手棋的颜色
        val color = move[coordinate_x][coordinate_y]
        // 判断该手棋上下左右四个方向的相领棋子
        for (i in 0..3) {
            directionX = coordinate_x + directions[i][0]
            directionY = coordinate_y + directions[i][1]
            // 如果不在棋盘内，继续下一个循环
            if (!isInBoard(directionX, directionY)) {
                continue
            } else if (move[directionX][directionY] !== color && move[directionX][directionY] !== StoneColor.None) {
                // 如果该棋子所在的块有气，继续下一个循环
                if (LibertyRules.hasLiberty(move, directionX, directionY)) {
                    continue
                } else {
                    flag = 1
                    // 记录第i个方向上的提子的数量
                    length[i][0] = LibertyRules.getLength()
                    // 记录第i个方向上的提子的坐标
                    val temp: Array<IntArray> = LibertyRules.getTakeStones()
                    for (j in 0..18) {
                        for (k in 0..1) {
                            takeStones[i][j][k] = temp[j][k]
                        }
                    }
                }
            }
        }
        // flag不为0，可提子，返回true
        return flag != 0
    }
}