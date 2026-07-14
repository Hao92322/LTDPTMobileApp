package com.example.todolist.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.todolist.data.repository.TokenManager
import com.example.todolist.ui.AppState
import com.example.todolist.ui.LocalAppState
import com.example.todolist.ui.auth.AuthScreen
import com.example.todolist.ui.category.CategoryManageScreen
import com.example.todolist.ui.home.HomeScreen
import com.example.todolist.ui.home.HomeViewModel
import com.example.todolist.ui.profile.ProfileScreen
import com.example.todolist.ui.theme.*
import com.example.todolist.ui.todo.CreateTodoScreen

data class NavItem(val icon: ImageVector, val label: String)

val navItems = listOf(
    NavItem(Icons.Filled.Home, "Home"),
    NavItem(Icons.Filled.CalendarMonth, "Calendar"),
    NavItem(Icons.Filled.Add, "Add"),
    NavItem(Icons.Filled.Insights, "Insights"),
    NavItem(Icons.Filled.Person, "Profile"),
)

@Composable
fun MainScreen() {
    val appState = remember { AppState() }

    CompositionLocalProvider(LocalAppState provides appState) {
        val isDark = appState.isDarkMode

        val bgColor by animateColorAsState(
            targetValue = if (isDark) Color(0xFF1A120B) else BackgroundCream,
            animationSpec = tween(400),
            label = "bg"
        )

        var isAuthenticated by remember { mutableStateOf(false) } // ✅ Đổi thành false để test login
        var selectedNav by remember { mutableIntStateOf(0) }
        var showCreateTodo by remember { mutableStateOf(false) }

        BackHandler(enabled = isAuthenticated && (selectedNav != 0 || showCreateTodo)) {
            if (showCreateTodo) {
                showCreateTodo = false
            } else {
                selectedNav = 0
            }
        }

        val homeViewModel: HomeViewModel = viewModel()

        if (!isAuthenticated) {
            // ✅ ĐÃ SỬA: AuthScreen giờ chỉ nhận onLoginSuccess
            AuthScreen(
                onLoginSuccess = { isAuthenticated = true }
            )
        } else if (showCreateTodo) {
            // ✅ ĐÃ SỬA: CreateTodoScreen giờ không cần onSave callback nữa
            // Nó tự gọi homeViewModel.addTodo() bên trong
            CreateTodoScreen(
                onBack = { showCreateTodo = false },
                homeViewModel = homeViewModel
            )
        } else {
            Scaffold(
                containerColor = bgColor,
                bottomBar = {
                    CurvedBottomNav(
                        selectedIndex = selectedNav,
                        onSelect = { selectedNav = it },
                        onAddClick = { showCreateTodo = true },
                        isDark = isDark
                    )
                }
            ) { innerPadding ->
                when (selectedNav) {
                    0 -> HomeScreen(
                        innerPadding = innerPadding,
                        viewModel = homeViewModel,
                        onProfileClick = { selectedNav = 4 }
                    )
                    1 -> CategoryManageScreen(
                        onBack = { selectedNav = 0 },
                        homeViewModel = homeViewModel
                    )
                    3 -> InsightsScreen(
                        innerPadding = innerPadding,
                        homeViewModel = homeViewModel
                    )
                    4 -> {
                        val context = LocalContext.current
                        ProfileScreen(
                            onLogout = {
                                TokenManager.clearToken(context)
                                isAuthenticated = false
                            },
                            homeViewModel = homeViewModel
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────── Insights Screen ────────────────────────────────

@Composable
fun InsightsScreen(
    innerPadding: PaddingValues,
    homeViewModel: HomeViewModel = viewModel()
) {
    val appState = LocalAppState.current
    val isDark = appState.isDarkMode

    val cardBg = if (isDark) Color(0xFF2C1F14) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFF5E6D3) else InkBrown
    val textSecondary = if (isDark) Color(0xFF9C8C7E) else TextMuted

    // ✅ Quan sát danh sách công việc và danh mục thực tế từ Database
    val todos by homeViewModel.todoList.collectAsStateWithLifecycle()
    val categories by homeViewModel.categoryList.collectAsStateWithLifecycle()

    // Tải lại dữ liệu khi vào tab Thống kê
    LaunchedEffect(Unit) {
        homeViewModel.loadTodos()
        homeViewModel.loadCategories()
    }

    // 1️⃣ Tính số nhiệm vụ đã hoàn thành
    val totalCompleted = todos.count { it.isDone }

    // 2️⃣ Tính chuỗi ngày liên tiếp (Streaks)
    val completedDates = todos.filter { it.isDone }
        .map { it.duedate.toLocalDate() }
        .distinct()
        .sortedDescending()

    var currentStreak = 0
    var bestStreak = 0

    if (completedDates.isNotEmpty()) {
        val todayLocalDate = java.time.LocalDate.now()
        val yesterday = todayLocalDate.minusDays(1)
        if (completedDates.contains(todayLocalDate) || completedDates.contains(yesterday)) {
            var checkDate = if (completedDates.contains(todayLocalDate)) todayLocalDate else yesterday
            while (completedDates.contains(checkDate)) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            }
        }
        val sortedDatesAsc = completedDates.sorted()
        var tempStreak = 0
        var prevDate: java.time.LocalDate? = null
        for (date in sortedDatesAsc) {
            if (prevDate == null) {
                tempStreak = 1
            } else {
                if (date == prevDate.plusDays(1)) {
                    tempStreak++
                } else if (date != prevDate) {
                    if (tempStreak > bestStreak) {
                        bestStreak = tempStreak
                    }
                    tempStreak = 1
                }
            }
            prevDate = date
        }
        if (tempStreak > bestStreak) {
            bestStreak = tempStreak
        }
    }
    if (bestStreak < currentStreak) {
        bestStreak = currentStreak
    }

    // 3️⃣ Tính toán dữ liệu tuần này theo ngày (T2..CN)
    val today = java.time.LocalDate.now()
    val dayOfWeekVal = today.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val startOfWeek = today.minusDays((dayOfWeekVal - 1).toLong())

    val weeklyData = (0..6).map { index ->
        val targetDate = startOfWeek.plusDays(index.toLong())
        todos.count { it.isDone && it.duedate.toLocalDate() == targetDate }
    }
    val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val maxVal = weeklyData.maxOrNull()?.coerceAtLeast(1) ?: 1
    val thisWeekTotal = weeklyData.sum()

    // Tính tuần trước
    val startOfLastWeek = startOfWeek.minusWeeks(1)
    val lastWeekTotal = todos.count { todo ->
        todo.isDone && !todo.duedate.toLocalDate().isBefore(startOfLastWeek) && todo.duedate.toLocalDate().isBefore(startOfWeek)
    }

    val percentChange = if (lastWeekTotal > 0) {
        ((thisWeekTotal - lastWeekTotal) / lastWeekTotal.toFloat() * 100).toInt()
    } else {
        if (thisWeekTotal > 0) 100 else 0
    }

    // 4️⃣ Tính toán theo danh mục
    val themeColors = listOf(Color(0xFFFBE3C3), Color(0xFFDCEFD9), Color(0xFFE7DEF7), Color(0xFFDBEBF6))
    val categoryInsights = categories.mapIndexed { index, category ->
        val color = themeColors[index % themeColors.size]
        val count = todos.count { it.categoryId == category.id && it.isDone }
        Triple(category.name, color, count)
    }
    val topCategory = categoryInsights.maxByOrNull { it.third }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp, end = 20.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (appState.language == "vi") "Thống kê" else "Insights",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )

        // Stats Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val labelDone = if (appState.language == "vi") "Nhiệm vụ xong" else "Tasks done"
            val labelCurrent = if (appState.language == "vi") "Chuỗi hiện tại" else "Current streak"
            val labelBest = if (appState.language == "vi") "Chuỗi kỷ lục" else "Best streak"

            StatCard(
                icon = Icons.Filled.TaskAlt, value = "$totalCompleted", label = labelDone,
                modifier = Modifier.weight(1f), cardColor = cardBg, textPrimary = textPrimary
            )
            StatCard(
                icon = Icons.Filled.LocalFireDepartment, value = "${currentStreak}d", label = labelCurrent,
                modifier = Modifier.weight(1f), cardColor = cardBg, textPrimary = textPrimary
            )
            StatCard(
                icon = Icons.Filled.EmojiEvents, value = "${bestStreak}d", label = labelBest,
                modifier = Modifier.weight(1f), cardColor = cardBg, textPrimary = textPrimary
            )
        }

        // Weekly Overview
        Surface(
            shape = RoundedCornerShape(20.dp), color = cardBg,
            shadowElevation = if (isDark) 0.dp else 2.dp, modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (appState.language == "vi") "Tuần này" else "This Week",
                    fontSize = 13.sp, color = textSecondary
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "$thisWeekTotal", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (appState.language == "vi") "nhiệm vụ hoàn thành" else "tasks done",
                        fontSize = 13.sp, color = textSecondary, modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                val isUp = percentChange >= 0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isUp) Color(0xFFDCEFD9) else Color(0xFFFFE0E0))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // ✅ FIX DEPRECATED: Dùng AutoMirrored
                    Icon(
                        imageVector = if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isUp) Color(0xFF4CAF50) else Color(0xFFE53935),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${if (isUp) "+" else ""}$percentChange% ${if (appState.language == "vi") "so với tuần trước" else "vs last week"}",
                        fontSize = 12.sp,
                        color = if (isUp) Color(0xFF2E7D32) else Color(0xFFB71C1C),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bar Chart
        Surface(
            shape = RoundedCornerShape(20.dp), color = cardBg,
            shadowElevation = if (isDark) 0.dp else 2.dp, modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (appState.language == "vi") "Hoạt động theo ngày" else "Daily Activity",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weeklyData.forEachIndexed { index, count ->
                        val fraction = count / maxVal.toFloat()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.height(80.dp).width(22.dp), contentAlignment = Alignment.BottomCenter) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((80 * fraction).dp.coerceAtLeast(6.dp))
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.verticalGradient(listOf(PeachEnd, AccentTerracotta)))
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(days[index], fontSize = 11.sp, color = textSecondary)
                        }
                    }
                }
            }
        }

        // Top Category
        if (topCategory != null) {
            Surface(shape = RoundedCornerShape(20.dp), color = topCategory.second, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = AccentTerracottaDeep, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (appState.language == "vi") "Danh mục nổi bật" else "Top Category",
                            fontSize = 12.sp, color = InkBrown.copy(alpha = 0.6f)
                        )
                        Text(text = topCategory.first, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkBrown)
                        Text(
                            text = "${topCategory.third} ${if (appState.language == "vi") "nhiệm vụ" else "tasks"}",
                            fontSize = 12.sp, color = InkBrown.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Category Breakdown
        Surface(
            shape = RoundedCornerShape(20.dp), color = cardBg,
            shadowElevation = if (isDark) 0.dp else 2.dp, modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (appState.language == "vi") "Theo danh mục" else "By Category",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary
                )
                Spacer(Modifier.height(14.dp))
                val total = categoryInsights.sumOf { it.third }.coerceAtLeast(1)
                categoryInsights.forEachIndexed { index, (name, color, count) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.size(10.dp).clip(CircleShape).background(
                                Color(
                                    (color.red * 0.7f).coerceIn(0f, 1f),
                                    (color.green * 0.7f).coerceIn(0f, 1f),
                                    (color.blue * 0.7f).coerceIn(0f, 1f)
                                )
                            )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(name, modifier = Modifier.weight(1f), fontSize = 13.sp, color = textPrimary)
                        Text("$count", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(Modifier.height(6.dp))
                    val fraction = count / total.toFloat()
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            .background(if (isDark) Color(0xFF3D2B1F) else BackgroundCream)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(3.dp)).background(color)
                        )
                    }
                    if (index != categoryInsights.lastIndex) Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Dynamic Smart Tip calculation
        val smartTipText = remember(categoryInsights, appState.language) {
            if (categoryInsights.isNotEmpty()) {
                val leastCompleted = categoryInsights.minByOrNull { it.third }
                val catName = leastCompleted?.first ?: ""
                val count = leastCompleted?.third ?: 0
                if (appState.language == "vi") {
                    "Bạn đã hoàn thành $count nhiệm vụ trong danh mục '$catName'. Hãy tiếp tục phát huy và tạo thêm mục tiêu mới nhé!"
                } else {
                    "You completed $count tasks in '$catName'. Keep up the momentum and define new goals!"
                }
            } else {
                if (appState.language == "vi") {
                    "Hãy tạo danh mục và công việc mới để hệ thống theo dõi tiến độ của bạn nhé!"
                } else {
                    "Create your first tasks and categories so we can track your activity!"
                }
            }
        }

        // Smart Tip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF2C1F14) else Color(0xFFFFF8F0),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = StreakOrange, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (appState.language == "vi") "Gợi ý" else "Tip",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StreakOrange
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = smartTipText,
                        fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────── Bottom Navigation ──────────────────────────────

@Composable
private fun CurvedBottomNav(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAddClick: () -> Unit,
    isDark: Boolean
) {
    val navBg = if (isDark) Color(0xFF2C1F14) else SurfaceWhite

    Box(modifier = Modifier.fillMaxWidth().height(86.dp)) {
        Surface(
            color = navBg, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().height(72.dp).align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                navItems.forEachIndexed { index, item ->
                    if (index == 2) {
                        Spacer(modifier = Modifier.width(56.dp))
                    } else {
                        NavIconButton(item = item, selected = selectedIndex == index, onClick = { onSelect(index) }, isDark = isDark)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(58.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AccentTerracotta, AccentTerracottaDeep)))
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add new habit", tint = SurfaceWhite, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun NavIconButton(item: NavItem, selected: Boolean, onClick: () -> Unit, isDark: Boolean) {
    val mutedColor = if (isDark) Color(0xFF6B5C52) else TextMuted
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(
            imageVector = item.icon, contentDescription = item.label,
            tint = if (selected) AccentTerracotta else mutedColor, modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier.size(4.dp).clip(CircleShape)
                .background(if (selected) AccentTerracotta else Color.Transparent)
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector, value: String, label: String,
    modifier: Modifier = Modifier, cardColor: Color = SurfaceWhite, textPrimary: Color = InkBrown
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(cardColor)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = StreakOrange, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun MainScreenPreview() {
    MaterialTheme {
        MainScreen()
    }
}