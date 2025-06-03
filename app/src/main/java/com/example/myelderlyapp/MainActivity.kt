package com.example.myelderlyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat               // ← 新增
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.myelderlyapp.ui.theme.CinnabarRed
import com.example.myelderlyapp.ui.theme.Ivory
import com.example.myelderlyapp.ui.theme.MyElderlyAppTheme
import com.example.myelderlyapp.ui.theme.QinghuaBlue
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyElderlyAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login")        { LoginScreen(navController) }
        composable("main")         { MainScreenImproved(navController) }
        composable("quickTravel")  { QuickTravelScreen(navController) }
        composable("health")       { HealthScreen(navController) }
        composable("artCommunity"){ ArtCommunityScreen(navController) }
        composable("chess")        { ChessScreen(navController) }
        composable("Gogame")       { GoGameScreen(navController) }
        composable("gomoku")       { GomokuScreen(navController) }
        composable("mahjong")      { MahjongScreen(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenImproved(navController: NavHostController) {
    // ← 明确泛型为 BottomTab，防止类型被推断为 BottomTab.Home
    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }

    Scaffold(
        topBar    = { TopBanner() },
        bottomBar = { BottomNavigationBar(selectedTab) { selectedTab = it } }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            FeatureGrid(navController)
        }
    }
}

@Composable
fun TopBanner() {
    val transition = rememberInfiniteTransition()
    val offsetX by transition.animateFloat(      // ← 现在可以正确识别
        initialValue = 0f,
        targetValue  = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val brush = Brush.horizontalGradient(
        colors = listOf(QinghuaBlue, Ivory),
        startX = offsetX,
        endX   = offsetX + 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(brush)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Text(
                text  = "怡亭",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text  = "欢迎，张先生",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun FeatureGrid(navController: NavHostController) {
    val features = listOf(
        FeatureData("快捷出行",   Icons.Filled.DirectionsBus)   { navController.navigate("quickTravel") },
        FeatureData("健康管理",   Icons.Filled.HealthAndSafety) { navController.navigate("health") },
        FeatureData("文艺社区",   Icons.Filled.Brush)           { navController.navigate("artCommunity") },
        FeatureData("退出登录",   Icons.Filled.ExitToApp)       {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login") { popUpTo("main") { inclusive = true } }
        }
    )

    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        contentPadding        = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement   = Arrangement.spacedBy(16.dp),
        modifier              = Modifier.fillMaxSize()
    ) {
        items(features) { feature ->
            FeatureCircleItem(feature.title, feature.icon, feature.onClick)
        }
    }
}

@Composable
fun FeatureCircleItem(
    title:   String,
    icon:    ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = title,
                modifier           = Modifier.size(36.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

data class FeatureData(
    val title:   String,
    val icon:    ImageVector,
    val onClick: () -> Unit
)

sealed class BottomTab(val title: String, val icon: ImageVector) {
    object Home      : BottomTab("首页",    Icons.Filled.Home)
    object Community : BottomTab("社区",    Icons.Filled.Brush)
    object Messages  : BottomTab("消息",    Icons.Filled.Message)
    object Profile   : BottomTab("我的",    Icons.Filled.Person)
}

@Composable
fun BottomNavigationBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        listOf(
            BottomTab.Home,
            BottomTab.Community,
            BottomTab.Messages,
            BottomTab.Profile
        ).forEach { tab ->
            NavigationBarItem(
                icon            = { Icon(tab.icon, contentDescription = tab.title) },
                label           = { Text(tab.title) },
                selected        = tab == selectedTab,
                onClick         = { onTabSelected(tab) },
                colors          = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = CinnabarRed
                ),
                alwaysShowLabel = false
            )
        }
    }
}
