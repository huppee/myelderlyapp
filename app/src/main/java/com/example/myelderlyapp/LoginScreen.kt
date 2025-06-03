package com.example.myelderlyapp

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // 自动登录：如果用户已登录，直接跳转到主界面
    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser != null) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("登录/注册") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 注册按钮
            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    if (!isValidEmail(trimmedEmail)) {
                        errorMessage = "请输入有效的邮箱地址"
                        return@Button
                    }
                    if (trimmedPassword.length < 6) {
                        errorMessage = "密码至少需要6个字符"
                        return@Button
                    }
                    if (!isNetworkAvailable(context)) {
                        errorMessage = "无网络连接"
                        return@Button
                    }
                    isLoading = true
                    auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                val exception = task.exception
                                errorMessage = when {
                                    exception is FirebaseAuthUserCollisionException -> "该邮箱已被注册"
                                    exception is FirebaseAuthWeakPasswordException -> "密码太弱"
                                    exception is FirebaseAuthInvalidCredentialsException -> "邮箱格式无效"
                                    else -> exception?.message ?: "注册失败"
                                }
                                Log.e("LoginScreen", "注册失败: ${exception?.message}")
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("注册")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 登录按钮
            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
                        errorMessage = "邮箱或密码不能为空"
                        return@Button
                    }
                    if (!isNetworkAvailable(context)) {
                        errorMessage = "无网络连接"
                        return@Button
                    }
                    isLoading = true
                    auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                val exception = task.exception
                                errorMessage = when {
                                    exception is FirebaseAuthInvalidUserException -> "用户不存在"
                                    exception is FirebaseAuthInvalidCredentialsException -> "邮箱或密码错误"
                                    else -> exception?.message ?: "登录失败"
                                }
                                Log.e("LoginScreen", "登录失败: ${exception?.message}")
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("登录")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 错误信息
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// 辅助函数：验证邮箱格式
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// 辅助函数：检查网络连接
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnected == true
}