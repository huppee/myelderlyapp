package com.example.myelderlyapp
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class) // 如果你继续使用 Material3 的 TopAppBar
@Composable
fun QuickTravelScreen(navController: NavController) {
    Scaffold(
        topBar = {
            // 使用 Material3 的 TopAppBar 和 colors 参数来设置背景色
            TopAppBar(
                title = { Text("快捷出行") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary // 设置背景色
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // 打车功能按钮
            Button(
                onClick = {
                    val taxiUri = Uri.parse("didi://")
                    val taxiIntent = Intent(Intent.ACTION_VIEW, taxiUri)
                    if (taxiIntent.resolveActivity(navController.context.packageManager) != null) {
                        navController.context.startActivity(taxiIntent)
                    } else {
                        val marketUri = Uri.parse("market://details?id=com.sdu.didi.psnger")
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        navController.context.startActivity(marketIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Text(text = "打车")
            }

            // 公交查询按钮
            Button(
                onClick = {
                    val busUri = Uri.parse("https://m.8684.cn/")
                    val busIntent = Intent(Intent.ACTION_VIEW, busUri)
                    navController.context.startActivity(busIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "公交查询")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 返回按钮
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "返回主页面")
            }
        }
    }
}
