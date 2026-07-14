package com.example.todolist.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.ui.theme.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var isLogin by remember { mutableStateOf(true) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loginResult by viewModel.loginResult.collectAsState()
    val registerResult by viewModel.registerResult.collectAsState()

    // Kiểm tra đã login chưa
    LaunchedEffect(Unit) {
        if (viewModel.isLoggedIn()) {
            onLoginSuccess()
        }
    }

    // Xử lý kết quả login
    LaunchedEffect(loginResult) {
        loginResult?.let { result ->
            if (result.success) {
                viewModel.clearResults()
                onLoginSuccess()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(BackgroundCream)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(Modifier.height(16.dp))
            AuthHeader(isLogin)
            Spacer(Modifier.height(32.dp))
            AuthModeToggle(isLogin = isLogin, onToggle = { isLogin = it })
            Spacer(Modifier.height(28.dp))

            AnimatedContent(
                targetState = isLogin,
                transitionSpec = {
                    if (targetState) {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    } else {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    }
                },
                label = "authForm"
            ) { loginMode ->
                if (loginMode) {
                    LoginForm(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        errorMessage = loginResult?.let { if (!it.success) it.message else null }
                    )
                } else {
                    RegisterForm(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        successMessage = registerResult?.let { if (it.success) it.message else null },
                        errorMessage = registerResult?.let { if (!it.success) it.message else null },
                        onRegisterSuccess = { isLogin = true }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SwitchModeText(isLogin = isLogin, onSwitch = { isLogin = !isLogin })
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AuthHeader(isLogin: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PeachStart, PeachEnd))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (isLogin) "Welcome back" else "Create your account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = InkBrown
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isLogin) "Log in to keep your morning streak going"
            else "Start building better mornings today",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuthModeToggle(isLogin: Boolean, onToggle: (Boolean) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceWhite)
            .border(1.dp, InputBorder, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        val segmentWidth = this.maxWidth / 2
        val indicatorOffset by animateDpAsState(
            targetValue = if (isLogin) 0.dp else segmentWidth,
            label = "toggleIndicator"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(AccentTerracotta, AccentTerracottaDeep)))
        )

        Row(modifier = Modifier.fillMaxSize()) {
            ToggleLabel("Log In", selected = isLogin, modifier = Modifier.weight(1f)) { onToggle(true) }
            ToggleLabel("Register", selected = !isLogin, modifier = Modifier.weight(1f)) { onToggle(false) }
        }
    }
}

@Composable
private fun ToggleLabel(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) SurfaceWhite else TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// Login form - ĐÃ GẮN API
@Composable
private fun LoginForm(
    viewModel: AuthViewModel,
    isLoading: Boolean,
    errorMessage: String?
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var accountError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuthTextField(
            value = account,
            onValueChange = { account = it; accountError = null },
            label = "Account",
            placeholder = "Email or username",
            leadingIcon = Icons.Filled.Person,
            errorText = accountError
        )
        AuthPasswordField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            errorText = passwordError
        )

        // Hiển thị lỗi từ API
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = AccentTerracotta, uncheckedColor = TextMuted)
                )
                Text("Remember me", fontSize = 12.sp, color = TextMuted)
            }
        }

        Spacer(Modifier.height(4.dp))

        PrimaryButton(
            text = if (isLoading) "Đang đăng nhập..." else "Log In",
            enabled = !isLoading
        ) {
            var hasError = false
            if (account.isBlank()) { accountError = "Vui lòng nhập tài khoản hoặc email"; hasError = true }
            if (password.isBlank()) { passwordError = "Vui lòng nhập mật khẩu"; hasError = true }
            if (!hasError) {
                // ✅ GỌI API LOGIN
                viewModel.login(account, password)
            }
        }
    }
}

// Register form - ĐÃ GẮN API
@Composable
private fun RegisterForm(
    viewModel: AuthViewModel,
    isLoading: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var accountError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    // Xử lý khi đăng ký thành công
    LaunchedEffect(successMessage) {
        successMessage?.let {
            onRegisterSuccess()
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuthTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = "Full name",
            placeholder = "Your name",
            leadingIcon = Icons.Filled.Badge,
            errorText = nameError
        )
        AuthTextField(
            value = account,
            onValueChange = { account = it; accountError = null },
            label = "Account",
            placeholder = "Email or username",
            leadingIcon = Icons.Filled.Person,
            errorText = accountError
        )
        AuthPasswordField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            errorText = passwordError
        )
        AuthPasswordField(
            label = "Confirm password",
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = null },
            visible = confirmVisible,
            onToggleVisibility = { confirmVisible = !confirmVisible },
            errorText = confirmError
        )

        // Hiển thị lỗi từ API
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Hiển thị thông báo thành công
        if (successMessage != null) {
            Text(
                text = successMessage,
                color = Color(0xFF4CAF50),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        PrimaryButton(
            text = if (isLoading) "Đang đăng ký..." else "Create Account",
            enabled = !isLoading
        ) {
            var hasError = false
            if (name.isBlank()) { nameError = "Họ và tên không được để trống"; hasError = true }
            if (account.isBlank()) { accountError = "Tài khoản hoặc email không được để trống"; hasError = true }
            if (password.length < 6) { passwordError = "Mật khẩu phải dài ít nhất 6 ký tự"; hasError = true }
            if (confirmPassword != password) { confirmError = "Mật khẩu xác nhận không trùng khớp"; hasError = true }
            if (!hasError) {
                // ✅ GỌI API REGISTER
                viewModel.register(name, account, password, confirmPassword)
            }
        }
    }
}

// Shared field styles
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    errorText: String? = null
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBrown)
        Spacer(Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (errorText != null) MaterialTheme.colorScheme.error else InputBorder,
                    RoundedCornerShape(16.dp)
                ),
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            isError = errorText != null,
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = InkBrown),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                errorContainerColor = SurfaceWhite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = AccentTerracotta
            )
        )
        if (errorText != null) {
            Spacer(Modifier.height(4.dp))
            Text(errorText, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    label: String = "Password",
    errorText: String? = null
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBrown)
        Spacer(Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (errorText != null) MaterialTheme.colorScheme.error else InputBorder,
                    RoundedCornerShape(16.dp)
                ),
            placeholder = { Text("••••••••", color = TextMuted, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (visible) "Hide password" else "Show password",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            singleLine = true,
            isError = errorText != null,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = InkBrown),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                errorContainerColor = SurfaceWhite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = AccentTerracotta
            )
        )
        if (errorText != null) {
            Spacer(Modifier.height(4.dp))
            Text(errorText, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (enabled) Brush.linearGradient(listOf(AccentTerracotta, AccentTerracottaDeep))
                else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// Bottom switch link
@Composable
private fun SwitchModeText(isLogin: Boolean, onSwitch: () -> Unit) {
    Row {
        Text(
            text = if (isLogin) "Don't have an account? " else "Already have an account? ",
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            text = if (isLogin) "Register" else "Log In",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AccentTerracotta,
            modifier = Modifier.clickable { onSwitch() }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AuthScreenPreview() {
    MaterialTheme {
        AuthScreen(onLoginSuccess = {})
    }
}