package com.example.myelderlyapp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)  // 添加该注解来忽略API警告
@Composable
fun HealthScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("健康管理") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { navController.navigate("registration") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Text(text = "一键挂号")
            }

            Button(
                onClick = { navController.navigate("onlineConsult") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Text(text = "在线咨询")
            }

            Button(
                onClick = { navController.navigate("medicationReminder") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "用药提醒")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 返回按钮
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "返回主页面")
            }
        }
    }
}
