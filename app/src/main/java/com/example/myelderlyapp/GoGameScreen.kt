package com.example.myelderlyapp

// 导入必要的库
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoGameScreen(navController: NavController) {
    // 保存棋盘对象，方便调用悔棋和重置功能
    val goBoardView = remember { mutableStateOf<GoBoardView?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("围棋", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("返回")
                    }
                }
            )
        },
        bottomBar = {
            // 底部按钮区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { goBoardView.value?.undoMove() }) {
                    Text("悔棋")
                }
                Button(onClick = { goBoardView.value?.resetBoard() }) {
                    Text("再来一局")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 显示棋盘区域
            AndroidView(
                factory = { context ->
                    GoBoardView(context).also { goBoardView.value = it }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}

// 自定义棋盘视图
class GoBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 设置各种画笔
    private val boardPaint = Paint().apply {
        color = Color.parseColor("#D2B48C") // 棋盘背景颜色
        style = Paint.Style.FILL
    }
    private val linePaint = Paint().apply {
        color = Color.BLACK // 棋盘线条颜色
        strokeWidth = 3f
        isAntiAlias = true
    }
    private val stonePaint = Paint().apply {
        isAntiAlias = true // 画圆更平滑
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER // 居中文字
    }

    // 棋盘参数
    private val boardSize = 19 // 19x19标准围棋盘
    private var cellSize = 0f // 单个格子的大小
    private var boardStartX = 0f // 棋盘起始 X 位置
    private var boardStartY = 0f // 棋盘起始 Y 位置
    private var board = Array(boardSize) { Array(boardSize) { 0 } } // 棋盘数组 0=空 1=黑 2=白
    private var currentPlayer = 1 // 当前玩家（1黑 2白）
    private var lastCaptured: Pair<Int, Int>? = null // 上一次是否吃子

    // 保存每一步，包括落子和吃掉的子
    private val moveHistory = mutableListOf<Move>()

    // 定义一步的结构：位置、玩家、吃掉的子
    data class Move(val row: Int, val col: Int, val player: Int, val captured: List<Pair<Int, Int>>)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padding = 40f
        val size = min(width, height) - padding * 2
        cellSize = size / (boardSize - 1)
        boardStartX = (width - cellSize * (boardSize - 1)) / 2f
        boardStartY = (height - cellSize * (boardSize - 1)) / 2f

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boardPaint) // 背景
        drawBoardLines(canvas) // 棋盘格线
        drawStones(canvas)     // 棋子
        drawPlayerTurn(canvas) // 玩家提示
    }

    // 绘制横竖线
    private fun drawBoardLines(canvas: Canvas) {
        repeat(boardSize) { i ->
            val pos = i * cellSize
            canvas.drawLine(boardStartX, boardStartY + pos, boardStartX + cellSize * (boardSize - 1), boardStartY + pos, linePaint)
            canvas.drawLine(boardStartX + pos, boardStartY, boardStartX + pos, boardStartY + cellSize * (boardSize - 1), linePaint)
        }
    }

    // 绘制所有棋子
    private fun drawStones(canvas: Canvas) {
        board.forEachIndexed { rowIdx, row ->
            row.forEachIndexed { colIdx, stone ->
                if (stone != 0) {
                    stonePaint.color = if (stone == 1) Color.BLACK else Color.WHITE
                    val cx = boardStartX + colIdx * cellSize
                    val cy = boardStartY + rowIdx * cellSize
                    canvas.drawCircle(cx, cy, cellSize * 0.45f, stonePaint)
                }
            }
        }
    }

    // 显示提示轮到谁
    private fun drawPlayerTurn(canvas: Canvas) {
        val playerText = if (currentPlayer == 1) "黑方落子" else "白方落子"
        canvas.drawText(playerText, width / 2f, boardStartY / 2f, textPaint)
    }

    // 响应用户触摸事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x - boardStartX
            val y = event.y - boardStartY
            if (x < 0 || y < 0) return false
            val row = ((y / cellSize).roundToInt()).coerceIn(0, boardSize - 1)
            val col = ((x / cellSize).roundToInt()).coerceIn(0, boardSize - 1)
            if (isValidMove(row, col)) {
                processMove(row, col)
            }
        }
        performClick()
        return true
    }

    // 检查该点是否可落子
    private fun isValidMove(row: Int, col: Int): Boolean {
        if (board[row][col] != 0) return false
        val tempBoard = cloneBoard().apply { this[row][col] = currentPlayer }
        val opponent = 3 - currentPlayer
        val opponentCaptured = checkCapturedStones(tempBoard, opponent)
        opponentCaptured.forEach { (r, c) -> tempBoard[r][c] = 0 }
        if (isKoViolation(opponentCaptured, row, col)) return false
        return hasLiberties(tempBoard, row, col, currentPlayer)
    }

    // 落子并记录历史
    private fun processMove(row: Int, col: Int) {
        board[row][col] = currentPlayer
        val opponent = 3 - currentPlayer
        val captured = checkCapturedStones(board, opponent)
        captured.forEach { (r, c) -> board[r][c] = 0 }
        moveHistory.add(Move(row, col, currentPlayer, captured))
        lastCaptured = if (captured.size == 1) captured[0] else null
        currentPlayer = opponent
        invalidate()
    }

    // 劫争判断
    private fun isKoViolation(captured: List<Pair<Int, Int>>, row: Int, col: Int): Boolean {
        return captured.size == 1 && lastCaptured != null && lastCaptured == captured[0] && lastCaptured == (row to col)
    }

    // 重置整个棋盘
    fun resetBoard() {
        board = Array(boardSize) { Array(boardSize) { 0 } }
        currentPlayer = 1
        lastCaptured = null
        moveHistory.clear()
        invalidate()
    }

    // 悔棋操作：移除上一步并还原被提子
    fun undoMove() {
        if (moveHistory.isNotEmpty()) {
            val lastIndex = moveHistory.size - 1
            val lastMove = moveHistory.removeAt(lastIndex)
            board[lastMove.row][lastMove.col] = 0 // 撤回落子
            lastMove.captured.forEach { (r, c) -> board[r][c] = 3 - lastMove.player } // 恢复被吃子
            currentPlayer = lastMove.player // 回到上一个玩家
            invalidate()
        }
    }

    // 克隆当前棋盘
    private fun cloneBoard(): Array<Array<Int>> = Array(boardSize) { row -> board[row].clone() }

    // 判断是否有气（还有空格）
    private fun hasLiberties(board: Array<Array<Int>>, row: Int, col: Int, player: Int): Boolean {
        val visited = Array(boardSize) { BooleanArray(boardSize) }
        val group = mutableListOf<Pair<Int, Int>>()
        collectGroup(board, row, col, player, visited, group)
        return group.any { (r, c) ->
            listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1).any { (dr, dc) ->
                val nr = r + dr
                val nc = c + dc
                nr in 0 until boardSize && nc in 0 until boardSize && board[nr][nc] == 0
            }
        }
    }

    // 收集连通棋子
    private fun collectGroup(
        board: Array<Array<Int>>,
        row: Int,
        col: Int,
        player: Int,
        visited: Array<BooleanArray>,
        group: MutableList<Pair<Int, Int>>
    ) {
        if (row !in 0 until boardSize || col !in 0 until boardSize) return
        if (visited[row][col]) return
        if (board[row][col] != player) return

        visited[row][col] = true
        group.add(row to col)

        listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1).forEach { (dr, dc) ->
            collectGroup(board, row + dr, col + dc, player, visited, group)
        }
    }

    // 查找所有无气的棋块（对手被吃）
    private fun checkCapturedStones(board: Array<Array<Int>>, player: Int): List<Pair<Int, Int>> {
        val captured = mutableListOf<Pair<Int, Int>>()
        val visited = Array(boardSize) { BooleanArray(boardSize) }

        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                if (board[row][col] == player && !visited[row][col]) {
                    val group = mutableListOf<Pair<Int, Int>>()
                    collectGroup(board, row, col, player, visited, group)
                    if (!group.any { (r, c) ->
                            listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1).any { (dr, dc) ->
                                val nr = r + dr
                                val nc = c + dc
                                nr in 0 until boardSize && nc in 0 until boardSize && board[nr][nc] == 0
                            }
                        }) {
                        captured.addAll(group)
                    }
                }
            }
        }
        return captured
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // 把浮点数四舍五入为整数（用于触控转换）
    private fun Float.roundToInt(): Int = (this + 0.5f).toInt()
}