package com.example.myelderlyapp

import android.graphics.Point
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import kotlin.math.roundToInt
import kotlin.random.Random

data class Data(val point: Point, val isBlack: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (other is Data) {
            return point.x == other.point.x && point.y == other.point.y
        }
        return false
    }

    override fun hashCode(): Int {
        return point.hashCode()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GomokuScreen(navController: NavController) {
    var board by remember { mutableStateOf<List<Data>>(listOf()) }
    var isBlackTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<Boolean?>(null) }

    // 棋局哲理列表
    val philosophies = listOf(
        // 基础哲理
        "棋如人生，落子无悔。",
        "胜负在心，不在棋盘。",
        "一步失算，满盘皆输。",
        "以静制动，后发制人。",
        "棋局虽小，智慧无穷。",

        // 战略思维
        "舍小就大，弃子争先。",
        "逢危须弃，遇险则谋。",
        "势孤取和，力强则战。",
        "入界宜缓，攻彼顾我。",
        "彼强自保，势弱侵分。",

        // 心态修养
        "胜固欣然，败亦可喜。",
        "心若冰清，天塌不惊。",
        "躁者多败，慎者常胜。",
        "观棋不语，君子之风。",
        "败后复盘，方得真谛。",

        // 人生隐喻
        "局中十九路，路路通人生。",
        "黑白交错处，恰似得失间。",
        "争地不如顺势，求胜先修己身。",
        "角落可弃全局在，眼前得失莫挂怀。",
        "棋终子落归棋罐，人生起落终归平。",

        // 高手境界
        "善弈者通盘无妙手。",
        "流水不争先，争的是滔滔不绝。",
        "高手无定式，随机应万变。",
        "不战而屈人之兵，善之善者也。",
        "胸有全局者，不拘一城一地。",

        // 文化典故
        "烂柯一局千年过，世上繁华几度新。",
        "当湖十局今犹在，不见当年对弈人。",
        "棋道合天道，方圆法阴阳。",
        "三百六十一路，一子参透禅机。",
        "古谱今用，薪火相传。"
    )
    var currentPhilosophy by remember { mutableStateOf(philosophies[Random.nextInt(philosophies.size)]) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val padding = 16.dp
    val boardSize = 15

    val availableWidth = with(LocalDensity.current) { (screenWidth - padding * 2).toPx() }
    val availableHeight = with(LocalDensity.current) { (screenHeight - padding * 2 - 150.dp).toPx() }
    val cellSize = minOf(availableWidth, availableHeight) / boardSize

    // 重置游戏状态的函数
    fun resetGame() {
        board = emptyList()
        isBlackTurn = true
        winner = null
        currentPhilosophy = philosophies[Random.nextInt(philosophies.size)] // 随机更新哲理
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("五子棋") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .size(with(LocalDensity.current) { (cellSize * boardSize).toDp() })
                    .padding(padding)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val x = (offset.x / cellSize).roundToInt().coerceIn(0, boardSize - 1)
                            val y = (offset.y / cellSize).roundToInt().coerceIn(0, boardSize - 1)

                            val newData = Data(Point(x, y), isBlackTurn)
                            if (!board.any { it.point == newData.point } && winner == null) {
                                board = board + newData
                                winner = checkWinner(board, x, y, isBlackTurn)
                                isBlackTurn = !isBlackTurn
                            }
                        }
                    }
            ) {
                drawRect(color = Color(0xFFC38E4B), size = size)
                drawBoard(boardSize, cellSize)
                drawPieces(board, cellSize)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (winner == null) {
                Text(
                    text = "轮到 ${if (isBlackTurn) "黑棋" else "白棋"} 下棋",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = "玩家 ${if (winner == true) "黑棋" else "白棋"} 胜利!",
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text(text = "返回主页面")
                }
                if (winner != null) {
                    Button(onClick = { resetGame() }) {
                        Text(text = "再来一局")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 显示棋局哲理
            Text(
                text = currentPhilosophy,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

fun DrawScope.drawBoard(boardSize: Int, cellSize: Float) {
    for (i in 0 until boardSize) {
        drawLine(
            color = Color.Black,
            start = Offset(0f, i * cellSize),
            end = Offset(cellSize * (boardSize - 1), i * cellSize),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Black,
            start = Offset(i * cellSize, 0f),
            end = Offset(i * cellSize, cellSize * (boardSize - 1)),
            strokeWidth = 2f
        )
    }
}

fun DrawScope.drawPieces(board: List<Data>, cellSize: Float) {
    for (data in board) {
        drawCircle(
            color = if (data.isBlack) Color.Black else Color.White,
            radius = cellSize / 2.5f,
            center = Offset(data.point.x * cellSize, data.point.y * cellSize)
        )
    }
}

fun checkWinner(board: List<Data>, x: Int, y: Int, isBlack: Boolean): Boolean? {
    val directions = listOf(
        Pair(1, 0), Pair(0, 1), Pair(1, 1), Pair(1, -1)
    )

    fun countInDirection(dx: Int, dy: Int): Int {
        var count = 1
        var nx = x + dx
        var ny = y + dy
        while (nx in 0..14 && ny in 0..14) {
            val piece = board.find { it.point.x == nx && it.point.y == ny }
            if (piece == null || piece.isBlack != isBlack) break
            count++
            nx += dx
            ny += dy
        }
        nx = x - dx
        ny = y - dy
        while (nx in 0..14 && ny in 0..14) {
            val piece = board.find { it.point.x == nx && it.point.y == ny }
            if (piece == null || piece.isBlack != isBlack) break
            count++
            nx -= dx
            ny -= dy
        }
        return count
    }

    for ((dx, dy) in directions) {
        if (countInDirection(dx, dy) >= 5) return isBlack
    }
    return null
}