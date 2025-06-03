package com.example.myelderlyapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*

/** 灯笼图标，用于 AppBar 左右点缀 */
@Composable
fun LanternIcon(modifier: Modifier = Modifier.size(32.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // 灯笼主体
        drawRoundRect(
            color = Color(0xFF8A0000),
            topLeft = Offset(w * 0.1f, 0f),
            size = Size(w * 0.8f, h * 0.8f),
            cornerRadius = CornerRadius(w * 0.2f, h * 0.2f),
            style = Fill
        )
        // 灯笼顶部
        drawRect(
            color = Color(0xFFB71C1C),
            topLeft = Offset(w * 0.3f, -h * 0.1f),
            size = Size(w * 0.4f, h * 0.2f)
        )
        // 流苏
        drawRect(
            color = Color(0xFFFFD700),
            topLeft = Offset(w * 0.45f, h * 0.8f),
            size = Size(w * 0.1f, h * 0.3f)
        )
    }
}

/** 祥云装饰，用于卡片顶部画装饰波浪 */
@Composable
fun CloudDecoration(modifier: Modifier = Modifier
    .fillMaxWidth()
    .height(12.dp)
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            val w = size.width
            moveTo(0f, size.height)
            cubicTo(w * 0.25f, 0f, w * 0.75f, size.height, w, 0f)
            lineTo(w, size.height)
            close()
        }
        drawPath(path, color = Color(0xFFFFD700), style = Fill)
    }
}

/** 单个功能项：包含 Lottie 动画、显示文字与路由 */
private data class Feature(
    val rawRes: Int,    // Lottie 动画资源ID
    val title: String,  // 卡片上展示的文字
    val route: String   // 实际导航路由（必须在 NavHost 中注册）
)

/** 文艺社区页面（优化版），点击卡片可跳转到对应功能 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtCommunityScreen(navController: NavController) {

    // 这里列出所有功能对应的动画、显示文字与路由名称
    val features = listOf(
        Feature(R.raw.anim_dancing, "广场舞", "dance"),         // TODO：可自行实现 DanceScreen
        Feature(R.raw.anim_gomoku,  "五子棋", "gomoku"),        // 单机版五子棋
        Feature(R.raw.anim_chess,   "象棋",   "chess"),         // 单机象棋
        Feature(R.raw.anim_baduk,   "围棋",   "Gogame"),        // 单机围棋
        Feature(R.raw.anim_mahjong, "麻将",   "mahjong")       // 单机麻将
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LanternIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "文艺社区",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF8A0000)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LanternIcon()
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0x80F5F5DC)  // 半透明米色背景
                )
            )
        },
        containerColor = Color(0xFFF5F5DC)  // 页面整体背景：浅米色
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 将 features 列表按 2 列分组
            features.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { feature ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700)),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable {
                                    // 使用真正路由导航，避免写成中文或未注册路由
                                    navController.navigate(feature.route)
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF5F5DC))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // 卡片顶部祥云装饰
                                CloudDecoration()
                                Spacer(modifier = Modifier.height(8.dp))

                                // Lottie 动画播放
                                val composition by rememberLottieComposition(
                                    LottieCompositionSpec.RawRes(feature.rawRes)
                                )
                                LottieAnimation(
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever,
                                    modifier = Modifier.size(64.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                // 卡片中部显示功能名称
                                Text(
                                    text = feature.title,
                                    color = Color(0xFF8A0000),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    // 如果这一行只有一个元素，则占位一个 Spacer 保持两列布局
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 返回主页面按钮
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A0000)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "返回主页面",
                    color = Color(0xFFFFD700),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
