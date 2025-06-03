package com.example.myelderlyapp

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MahjongScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val allTiles = remember {
        val wan = (1..9).flatMap { List(4) { it } }
        val tong = (11..19).flatMap { List(4) { it } }
        val suo = (21..29).flatMap { List(4) { it } }
        val winds = listOf(31, 33, 35, 37).flatMap { List(4) { it } }
        val dragons = listOf(41, 43, 45).flatMap { List(4) { it } }
        (wan + tong + suo + winds + dragons).shuffled().toMutableStateList()
    }

    data class Player(
        val hand: MutableList<Int> = mutableListOf(),
        val melds: MutableList<List<Int>> = mutableListOf(),
        var canAct: Boolean = false,
        var hasDrawn: Boolean = false
    )

    val players = remember { List(4) { Player() } }
    val discards = remember { mutableStateListOf<Pair<String, Int>>() }
    val lastDiscard = remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val winMessage = remember { mutableStateOf<String?>(null) }

    var currentTurn by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    var hasDealt by remember { mutableStateOf(false) }

    fun canWin(hand: List<Int>): Boolean {
        if (hand.size != 14) return false
        val sorted = hand.sorted()

        fun isValidCombo(tiles: List<Int>): Boolean {
            if (tiles.isEmpty()) return true
            if (tiles.size >= 3 && tiles[0] == tiles[1] && tiles[1] == tiles[2]) {
                return isValidCombo(tiles.drop(3))
            }
            if (tiles.size >= 3 &&
                tiles.contains(tiles[0] + 1) &&
                tiles.contains(tiles[0] + 2)
            ) {
                val temp = tiles.toMutableList()
                temp.remove(tiles[0])
                temp.remove(tiles[0] + 1)
                temp.remove(tiles[0] + 2)
                return isValidCombo(temp)
            }
            return false
        }

        for (i in 0 until sorted.size - 1) {
            if (sorted[i] == sorted[i + 1]) {
                val remaining = sorted.toMutableList()
                remaining.removeAt(i + 1)
                remaining.removeAt(i)
                if (isValidCombo(remaining)) return true
            }
        }
        return false
    }

    // 发牌
    LaunchedEffect(Unit) {
        if (!hasDealt) {
            repeat(13) {
                players.forEach { it.hand.add(allTiles.removeAt(0)) }
            }
            players[0].hand.add(allTiles.removeAt(0))
            players[0].hasDrawn = true
            hasDealt = true
        }
    }

    // 回合处理
    LaunchedEffect(currentTurn) {
        if (hasDealt && currentTurn != 0) {
            val player = players[currentTurn]
            delay(800)
            if (!player.hasDrawn && allTiles.isNotEmpty()) {
                player.hand.add(allTiles.removeAt(0))
                player.hasDrawn = true
            }
            delay(800)
            if (player.hand.isNotEmpty()) {
                val tile = player.hand.removeAt(0)
                discards.add(tileToEmoji(tile) to currentTurn)
                lastDiscard.value = tile to currentTurn
                player.hasDrawn = false
                currentTurn = (currentTurn + 1) % 4
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().width(42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("左家", color = Color.White, fontSize = 12.sp)
            repeat(players[1].hand.size) {
                MahjongTile("🀫", 36.dp, 48.dp, rotate = true)
            }
        }

        Spacer(Modifier.width(4.dp))

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("上家", color = Color.White, fontSize = 12.sp)
                LazyRow {
                    items(players[2].hand) {
                        MahjongTile("🀫", 32.dp, 44.dp)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("出牌区", color = Color.Yellow, fontSize = 14.sp)
                LazyRow {
                    items(discards) { (emoji, _) ->
                        MahjongTile(emoji, 40.dp, 60.dp)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("你", color = Color.White, fontSize = 14.sp)
                LazyRow {
                    items(players[0].hand) { tile ->
                        MahjongTile(tileToEmoji(tile), 44.dp, 64.dp) {
                            players[0].hand.remove(tile)
                            discards.add(tileToEmoji(tile) to 0)
                            lastDiscard.value = tile to 0

                            if (canWin(players[0].hand)) {
                                winMessage.value = "🎉 恭喜你胡牌啦！"
                            }

                            players[0].hasDrawn = false
                            coroutineScope.launch {
                                delay(500)
                                currentTurn = 1
                            }
                        }
                    }
                }

                // 胡牌提示
                winMessage.value?.let {
                    Text(it, color = Color.Yellow, fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                }

                // 碰 / 杠 按钮
                lastDiscard.value?.let { (tile, _) ->
                    val count = players[0].hand.count { it == tile }

                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        if (count >= 2) {
                            Button(onClick = {
                                repeat(2) { players[0].hand.remove(tile) }
                                players[0].melds.add(List(3) { tile })
                                lastDiscard.value = null
                            }) {
                                Text("碰")
                            }
                        }
                        if (count >= 3) {
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                repeat(3) { players[0].hand.remove(tile) }
                                players[0].melds.add(List(4) { tile })
                                lastDiscard.value = null
                            }) {
                                Text("杠")
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        Column(
            modifier = Modifier.fillMaxHeight().width(42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("右家", color = Color.White, fontSize = 12.sp)
            repeat(players[3].hand.size) {
                MahjongTile("🀫", 36.dp, 48.dp, rotate = true)
            }
        }
    }
}

@Composable
fun MahjongTile(
    emoji: String,
    width: Dp,
    height: Dp,
    rotate: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .padding(1.dp)
            .size(width, height)
            .shadow(1.dp, RoundedCornerShape(4.dp))
            .background(Color.White, RoundedCornerShape(4.dp))
            .let { if (rotate) it.graphicsLayer(rotationZ = 90f) else it }
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val fontScale = maxWidth.value * 0.65
            Text(text = emoji, fontSize = fontScale.sp, textAlign = TextAlign.Center)
        }
    }
}

fun tileToEmoji(num: Int): String {
    return when (num) {
        in 1..9 -> listOf("🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏")[num - 1]
        in 11..19 -> listOf("🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡")[num - 11]
        in 21..29 -> listOf("🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘")[num - 21]
        31 -> "🀀"; 33 -> "🀁"; 35 -> "🀂"; 37 -> "🀃"
        41 -> "🀄"; 43 -> "🀅"; 45 -> "🀆"
        else -> "?"
    }
}
