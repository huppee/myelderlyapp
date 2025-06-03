package com.example.myelderlyapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ChessScreen(navController: NavController) {
    val context = LocalContext.current
    val chessBoardView = remember { ChessBoardView(context) }

    // 定义禅诗列表
    val philosophies = listOf(
        "楚河汉界起纷争，棋子无声战未停。世事如棋皆有定，何须胜负论输赢。",
        "局中天地小，世事若棋枰。进退皆由己，输赢一笑轻。",
        "观棋不语静无言，坐看风云变幻间。胜负无常皆是幻，心平气和悟禅关。",
        "弈棋似斗智，妙算蕴机谋。心定乾坤稳，神闲岁月悠。",
        "棋逢对手乐无穷，楚汉相争韵味浓。落子如飞心不乱，闲云野鹤任西东。",
        "深山古寺静，竹下弈棋声。黑白分明处，禅心悟此生。",
        "棋道通禅道，方圆法自然。攻防皆妙法，胜负亦随缘。",
        "对弈闲庭下，清风拂面来。心中无挂碍，棋子任徘徊。",
        "手谈心会意，棋动意先行。静虑观全局，深思悟世情。",
        "残局见真章，危中寻妙方。人生如逆旅，何处不风光。"
    )
    // 随机选择一首禅诗展示，也可以通过按钮切换
    val randomPoem = remember { philosophies.random() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("中国象棋") },
                actions = {
                    Row {
                        Button(
                            onClick = { chessBoardView.resetGame() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("重置")
                        }
                        Button(onClick = { navController.popBackStack() }) {
                            Text("返回")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // 棋盘显示区域
                AndroidView(
                    factory = { chessBoardView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(8f / 9f)
                )
            }
            // 当前回合提示
            Text(
                text = "当前回合: ${if (chessBoardView.currentPlayer == Player.RED) "红方" else "黑方"}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            // 显示禅诗，作为下棋界面的一部分
            Text(
                text = randomPoem,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 交叉点数（注意：棋盘实际有 9 列和 10 行交叉点）
    private val numCols = 9
    private val numRows = 10
    // 为了便于绘制，计算间隔距离（cellSize）基于“间隔数”而非格子数
    private var cellSize = 0f
    // 外边距
    private val padding = 30f

    // 绘制工具
    private val boardPaint = Paint().apply {
        color = Color.parseColor("#F0D9B5") // 木质棋盘背景
        style = Paint.Style.FILL
    }
    private val linePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 4f
        isAntiAlias = true
    }
    private val riverPaint = Paint().apply {
        color = Color.BLACK
        textSize = 48f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    private val piecePaint = Paint().apply {
        textSize = 48f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    private val selectPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val pieceBackgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 棋盘状态：二维数组，行数为 numRows，列数为 numCols
    private var board = Array(numRows) { Array<ChessPiece?>(numCols) { null } }
    var currentPlayer = Player.RED
        private set
    private var selectedPiece: ChessPiece? = null
    private var selectedPosition: Pair<Int, Int>? = null
    private var gameOver = false

    init {
        initChessBoard()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 根据可用宽度计算 cellSize：横向有 (numCols - 1) 个间隔
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = width - 2 * padding
        cellSize = availableWidth / (numCols - 1)
        // 纵向间隔数为 (numRows - 1)，外加上下边距
        val height = ((numRows - 1) * cellSize + 2 * padding).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制棋盘背景
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boardPaint)
        canvas.save()
        // 平移画布，预留外边距
        canvas.translate(padding, padding)
        drawBoardGrid(canvas)
        drawRiverText(canvas)
        drawPalaceDiagonals(canvas)
        drawPieces(canvas)
        drawSelection(canvas)
        canvas.restore()
    }

    /**
     * 绘制棋盘网格：
     * - 横线：所有行均绘制（共 numRows 条线，连接所有交叉点）
     * - 竖线：最左和最右的竖线全长，其余竖线在“楚河汉界”区域（交叉点 4 和 5 之间）断开
     */
    private fun drawBoardGrid(canvas: Canvas) {
        // 绘制横线（每一行的交叉点，从第 0 行到第 numRows-1 行）
        for (row in 0 until numRows) {
            val y = row * cellSize
            canvas.drawLine(0f, y, (numCols - 1) * cellSize, y, linePaint)
        }
        // 绘制竖线
        for (col in 0 until numCols) {
            val x = col * cellSize
            if (col == 0 || col == numCols - 1) {
                // 左右边界：竖线全长
                canvas.drawLine(x, 0f, x, (numRows - 1) * cellSize, linePaint)
            } else {
                // 非边界：上半段（0 ~ 第4行）和下半段（第5行 ~ 最后一行）分别绘制
                canvas.drawLine(x, 0f, x, 4 * cellSize, linePaint)
                canvas.drawLine(x, 5 * cellSize, x, (numRows - 1) * cellSize, linePaint)
            }
        }
    }

    /**
     * 绘制楚河汉界文字，放在横向中点和上下分界（第4行与第5行之间）的中间位置
     */
    private fun drawRiverText(canvas: Canvas) {
        val centerX = ((numCols - 1) * cellSize) / 2
        val centerY = (4 * cellSize + 5 * cellSize) / 2
        // 可根据需要微调文字位置，此处加了 24 像素的偏移
        canvas.drawText("楚河   汉界", centerX, centerY + 24f, riverPaint)
    }

    /**
     * 绘制宫内对角线：
     * 红方宫位于上方（交叉点行 0～2，列 3～5），黑方宫位于下方（交叉点行 7～9，列 3～5）
     */
    private fun drawPalaceDiagonals(canvas: Canvas) {
        // 红方宫
        canvas.drawLine(3 * cellSize, 0f, 5 * cellSize, 2 * cellSize, linePaint)
        canvas.drawLine(5 * cellSize, 0f, 3 * cellSize, 2 * cellSize, linePaint)
        // 黑方宫
        canvas.drawLine(3 * cellSize, 7 * cellSize, 5 * cellSize, 9 * cellSize, linePaint)
        canvas.drawLine(5 * cellSize, 7 * cellSize, 3 * cellSize, 9 * cellSize, linePaint)
    }

    private fun drawPieces(canvas: Canvas) {
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val piece = board[row][col] ?: continue
                drawPiece(canvas, row, col, piece)
            }
        }
    }

    /**
     * 在交叉点处绘制棋子
     */
    private fun drawPiece(canvas: Canvas, row: Int, col: Int, piece: ChessPiece) {
        val cx = col * cellSize
        val cy = row * cellSize
        val radius = cellSize * 0.4f
        pieceBackgroundPaint.color = if (piece.player == Player.RED) {
            Color.parseColor("#D32F2F")
        } else {
            Color.parseColor("#212121")
        }
        // 添加阴影效果
        pieceBackgroundPaint.setShadowLayer(8f, 2f, 2f, Color.parseColor("#80000000"))
        canvas.drawCircle(cx, cy, radius, pieceBackgroundPaint)
        pieceBackgroundPaint.clearShadowLayer()
        piecePaint.color = Color.WHITE
        val textBounds = Rect()
        piecePaint.getTextBounds(piece.display, 0, piece.display.length, textBounds)
        // 计算文字基线，使文字在圆内垂直居中
        val textY = cy - (textBounds.top + textBounds.bottom) / 2
        canvas.drawText(piece.display, cx, textY, piecePaint)
    }

    /**
     * 绘制选中标记，同样绘制在对应交叉点处
     */
    private fun drawSelection(canvas: Canvas) {
        selectedPosition?.let { (row, col) ->
            val cx = col * cellSize
            val cy = row * cellSize
            canvas.drawCircle(cx, cy, cellSize * 0.45f, selectPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver) return true
        // 将触摸点坐标转换到棋盘区域（减去边距）
        val rawX = event.x - padding
        val rawY = event.y - padding
        // 根据 cellSize 计算最近的交叉点（这里加上半个 cellSize 以便四舍五入）
        val col = ((rawX + cellSize / 2) / cellSize).toInt().coerceIn(0, numCols - 1)
        val row = ((rawY + cellSize / 2) / cellSize).toInt().coerceIn(0, numRows - 1)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleClick(row, col)
                performClick()
            }
        }
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun handleClick(row: Int, col: Int) {
        if (selectedPiece == null) {
            selectPiece(row, col)
        } else {
            movePiece(row, col)
        }
    }

    private fun selectPiece(row: Int, col: Int) {
        board[row][col]?.takeIf { it.player == currentPlayer }?.let {
            selectedPiece = it
            selectedPosition = row to col
        }
    }

    private fun movePiece(toRow: Int, toCol: Int) {
        val (fromRow, fromCol) = selectedPosition ?: return
        val piece = selectedPiece ?: return
        if (isValidMove(piece, fromRow to fromCol, toRow to toCol)) {
            val targetPiece = board[toRow][toCol]
            board[fromRow][fromCol] = null
            board[toRow][toCol] = piece
            if (targetPiece?.type == PieceType.KING) {
                gameOver = true
                Toast.makeText(
                    context,
                    "${if (currentPlayer == Player.RED) "红方" else "黑方"} 获胜！",
                    Toast.LENGTH_LONG
                ).show()
            }
            currentPlayer = currentPlayer.opposite
        }
        selectedPiece = null
        selectedPosition = null
        invalidate()
    }

    private fun isValidMove(piece: ChessPiece, from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        val (toRow, toCol) = to
        if (from == to) return false
        // 若目标位置已有己方棋子，不可走
        if (board[toRow][toCol]?.player == piece.player) return false
        return when (piece.type) {
            PieceType.CAR -> isValidCarMove(from, to)
            PieceType.HORSE -> isValidHorseMove(from, to)
            PieceType.ELEPHANT -> isValidElephantMove(from, to, piece.player)
            PieceType.ADVISOR -> isValidAdvisorMove(from, to, piece.player)
            PieceType.KING -> isValidKingMove(from, to, piece.player)
            PieceType.CANNON -> isValidCannonMove(from, to)
            PieceType.SOLDIER -> isValidSoldierMove(from, to, piece.player)
        }
    }

    private fun isValidCarMove(from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        val (fromRow, fromCol) = from
        val (toRow, toCol) = to

        if (fromRow != toRow && fromCol != toCol) return false

        if (fromRow == toRow) {
            val range = if (toCol > fromCol) (fromCol + 1) until toCol else (toCol + 1) until fromCol
            for (col in range) {
                if (board[fromRow][col] != null) return false
            }
        } else {
            val range = if (toRow > fromRow) (fromRow + 1) until toRow else (toRow + 1) until fromRow
            for (row in range) {
                if (board[row][fromCol] != null) return false
            }
        }

        return true
    }


    private fun isValidHorseMove(from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        val (fromRow, fromCol) = from
        val (toRow, toCol) = to
        val dx = abs(toCol - fromCol)
        val dy = abs(toRow - fromRow)
        if (!((dx == 1 && dy == 2) || (dx == 2 && dy == 1))) return false
        // 检查“蹩马腿”
        val blockRow = if (dy == 2) fromRow + (toRow - fromRow) / 2 else fromRow
        val blockCol = if (dx == 2) fromCol + (toCol - fromCol) / 2 else fromCol
        return board[blockRow][blockCol] == null
    }

    private fun isValidElephantMove(from: Pair<Int, Int>, to: Pair<Int, Int>, player: Player): Boolean {
        val (fromRow, fromCol) = from
        val (toRow, toCol) = to
        val dx = abs(toCol - fromCol)
        val dy = abs(toRow - fromRow)
        if (dx != 2 || dy != 2) return false
        // 检查象眼
        val blockRow = (fromRow + toRow) / 2
        val blockCol = (fromCol + toCol) / 2
        if (board[blockRow][blockCol] != null) return false
        // 象不能过河
        return if (player == Player.RED) toRow <= 4 else toRow >= 5
    }

    private fun isValidAdvisorMove(from: Pair<Int, Int>, to: Pair<Int, Int>, player: Player): Boolean {
        val (fromRow, fromCol) = from
        val (toRow, toCol) = to
        if (abs(toCol - fromCol) != 1 || abs(toRow - fromRow) != 1) return false
        val palaceCols = 3..5
        val palaceRows = if (player == Player.RED) 0..2 else 7..9
        return toCol in palaceCols && toRow in palaceRows
    }

    private fun isValidKingMove(from: Pair<Int, Int>, to: Pair<Int, Int>, player: Player): Boolean {
        val (fromRow, fromCol) = from
        val (toRow, toCol) = to
        if (!( (abs(toCol - fromCol) == 1 && abs(toRow - fromRow) == 0) ||
                    (abs(toCol - fromCol) == 0 && abs(toRow - fromRow) == 1) )) return false
        val palaceCols = 3..5
        val palaceRows = if (player == Player.RED) 0..2 else 7..9
        // 特殊规则：将帅见面（仅当中间无棋子阻挡时允许移动）
        if (fromCol == toCol && board[toRow][toCol]?.type == PieceType.KING) {
            val minRow = min(fromRow, toRow)
            val maxRow = max(fromRow, toRow)
            if ((minRow + 1 until maxRow).none { board[it][fromCol] != null }) return true
        }
        return toCol in palaceCols && toRow in palaceRows
    }

    private fun isValidCannonMove(from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        val (fromRow, fromCol) = from
        val (toRow, toCol) = to
        val target = board[toRow][toCol]
        var count = 0
        if (fromRow != toRow && fromCol != toCol) return false
        if (fromRow == toRow) {
            val minCol = min(fromCol, toCol)
            val maxCol = max(fromCol, toCol)
            for (col in minCol + 1 until maxCol) {
                if (board[fromRow][col] != null) count++
            }
        } else {
            val minRow = min(fromRow, toRow)
            val maxRow = max(fromRow, toRow)
            for (row in minRow + 1 until maxRow) {
                if (board[row][fromCol] != null) count++
            }
        }
        return if (target == null) count == 0 else count == 1
    }

    private fun isValidSoldierMove(from: Pair<Int, Int>, to: Pair<Int, Int>, player: Player): Boolean {
        val (fromRow, fromCol) = from
        val (toRow, toCol) = to
        val forward = if (player == Player.RED) 1 else -1
        val crossedRiver = if (player == Player.RED) fromRow >= 5 else fromRow <= 4
        return if (!crossedRiver) {
            // 未过河只能直进
            (toRow - fromRow) == forward && toCol == fromCol
        } else {
            // 过河后可横走，但不能后退
            ((toRow - fromRow) == forward && toCol == fromCol) ||
                    (toRow == fromRow && abs(toCol - fromCol) == 1)
        }
    }

    private fun initChessBoard() {
        // 初始化红方棋子（上方）
        board[0][0] = ChessPiece.CAR_RED
        board[0][8] = ChessPiece.CAR_RED
        board[0][1] = ChessPiece.HORSE_RED
        board[0][7] = ChessPiece.HORSE_RED
        board[0][2] = ChessPiece.ELEPHANT_RED
        board[0][6] = ChessPiece.ELEPHANT_RED
        board[0][3] = ChessPiece.ADVISOR_RED
        board[0][5] = ChessPiece.ADVISOR_RED
        board[0][4] = ChessPiece.KING_RED
        board[2][1] = ChessPiece.CANNON_RED
        board[2][7] = ChessPiece.CANNON_RED
        arrayOf(0, 2, 4, 6, 8).forEach { col ->
            board[3][col] = ChessPiece.SOLDIER_RED
        }
        // 初始化黑方棋子（下方）
        board[9][0] = ChessPiece.CAR_BLACK
        board[9][8] = ChessPiece.CAR_BLACK
        board[9][1] = ChessPiece.HORSE_BLACK
        board[9][7] = ChessPiece.HORSE_BLACK
        board[9][2] = ChessPiece.ELEPHANT_BLACK
        board[9][6] = ChessPiece.ELEPHANT_BLACK
        board[9][3] = ChessPiece.ADVISOR_BLACK
        board[9][5] = ChessPiece.ADVISOR_BLACK
        board[9][4] = ChessPiece.KING_BLACK
        board[7][1] = ChessPiece.CANNON_BLACK
        board[7][7] = ChessPiece.CANNON_BLACK
        arrayOf(0, 2, 4, 6, 8).forEach { col ->
            board[6][col] = ChessPiece.SOLDIER_BLACK
        }
    }

    fun resetGame() {
        board = Array(numRows) { Array(numCols) { null } }
        currentPlayer = Player.RED
        selectedPiece = null
        selectedPosition = null
        gameOver = false
        initChessBoard()
        invalidate()
    }
}

enum class PieceType {
    CAR,       // 车
    HORSE,     // 马
    ELEPHANT,  // 象/相
    ADVISOR,   // 士/仕
    KING,      // 将/帅
    CANNON,    // 炮
    SOLDIER    // 兵/卒
}

enum class Player {
    RED, BLACK;
    val opposite: Player
        get() = if (this == RED) BLACK else RED
}

enum class ChessPiece(
    val display: String,
    val player: Player,
    val type: PieceType
) {
    KING_RED("帅", Player.RED, PieceType.KING),
    ADVISOR_RED("仕", Player.RED, PieceType.ADVISOR),
    ELEPHANT_RED("相", Player.RED, PieceType.ELEPHANT),
    HORSE_RED("马", Player.RED, PieceType.HORSE),
    CAR_RED("车", Player.RED, PieceType.CAR),
    CANNON_RED("炮", Player.RED, PieceType.CANNON),
    SOLDIER_RED("兵", Player.RED, PieceType.SOLDIER),

    KING_BLACK("将", Player.BLACK, PieceType.KING),
    ADVISOR_BLACK("士", Player.BLACK, PieceType.ADVISOR),
    ELEPHANT_BLACK("象", Player.BLACK, PieceType.ELEPHANT),
    HORSE_BLACK("馬", Player.BLACK, PieceType.HORSE),
    CAR_BLACK("車", Player.BLACK, PieceType.CAR),
    CANNON_BLACK("砲", Player.BLACK, PieceType.CANNON),
    SOLDIER_BLACK("卒", Player.BLACK, PieceType.SOLDIER);
}
