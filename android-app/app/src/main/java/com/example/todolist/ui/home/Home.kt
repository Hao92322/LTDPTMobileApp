package com.example.todolist.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.ui.component.BaseSearchBar
import com.example.todolist.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import com.example.todolist.ui.LocalAppState

// Screen
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel = viewModel(),
    onProfileClick: () -> Unit = {}
) {
    val tasks by viewModel.todoList.collectAsStateWithLifecycle()
    HomeContent(
        innerPadding = innerPadding,
        tasks = tasks,
        onProfileClick = onProfileClick,
        onTaskToggle = { task ->
            val globalIndex = tasks.indexOf(task)
            if (globalIndex != -1) {
                viewModel.toggleTodoStatus(globalIndex)
            }
        },
        onTaskUpdate = { globalIndex, updatedTask ->
            viewModel.updateTodo(globalIndex, updatedTask)
        },
        onTaskDelete = { globalIndex ->
            viewModel.deleteTodo(globalIndex)
        }
    )
}

@Composable
private fun HomeContent(
    innerPadding: PaddingValues,
    tasks: List<HomeUiState>,
    onProfileClick: () -> Unit,
    onTaskToggle: (HomeUiState) -> Unit,
    onTaskUpdate: (Int, HomeUiState) -> Unit,
    onTaskDelete: (Int) -> Unit
) {
    val appState = LocalAppState.current
    val isDark = appState.isDarkMode
    val lang = appState.language

    val today = remember { LocalDate.now() }
    var selectedYear by remember { mutableIntStateOf(today.year) }
    var selectedMonth by remember { mutableIntStateOf(today.monthValue) }
    var selectedDay by remember { mutableIntStateOf(today.dayOfMonth) }

    // View mode: 0=Day, 1=Week, 2=Month
    var viewMode by remember { mutableIntStateOf(0) }

    // Selected week number (ISO week)
    val todayWeek = remember {
        today.get(WeekFields.ISO.weekOfWeekBasedYear())
    }
    var selectedWeek by remember { mutableIntStateOf(todayWeek) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var editingTaskIndex by remember { mutableStateOf<Int?>(null) }

    val bgColor = if (isDark) Color(0xFF1A120B) else BackgroundCream
    val cardColor = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val textPrimary = if (isDark) Color(0xFFF5E6D3) else InkBrown

    val filteredTasks = remember(tasks, selectedYear, selectedMonth, selectedDay, searchQuery, selectedCategoryFilter) {
        tasks.filter { task ->
            val matchesDay = task.duedate.dayOfMonth == selectedDay &&
                             task.duedate.monthValue == selectedMonth &&
                             task.duedate.year == selectedYear
            val matchesQuery = task.title.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "All" || task.category == selectedCategoryFilter
            matchesDay && matchesQuery && matchesCategory
        }
    }

    val categories = remember(tasks) {
        listOf("All") + tasks.map { it.category }.distinct()
    }

    val tasksByCategory = remember(filteredTasks) {
        filteredTasks.groupBy { it.category }
    }

    val emptyText = if (lang == "vi") "Bạn đã rảnh rỗi không còn việc nào ngày hôm nay !!" else "No tasks for today — enjoy your free time!"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Greeting + date header
        item { GreetingHeader(name = "Hao", tasks = filteredTasks, textPrimary = textPrimary, onProfileClick = onProfileClick) }

        // ── Date header bar
        item {
            DateHeaderBar(
                today = today,
                selectedDay = selectedDay,
                viewMode = viewMode,
                selectedWeek = selectedWeek,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                onViewModeChange = { viewMode = it },
                onWeekSelected = { selectedWeek = it },
                onYearSelected = { selectedYear = it },
                onMonthSelected = { selectedMonth = it },
                isDark = isDark,
                lang = lang
            )
        }

        // Day mode: show date strip
        if (viewMode == 0) {
            item {
                WeekDaySelector(
                    selectedDay = selectedDay,
                    onSelect = { selectedDay = it },
                    isDark = isDark
                )
            }
        }

        // Week mode: show Mon-Sun row for selected week
        if (viewMode == 1) {
            item {
                WeekView(
                    today = today,
                    selectedWeek = selectedWeek,
                    selectedDay = selectedDay,
                    onSelect = { selectedDay = it },
                    isDark = isDark
                )
            }
        }

        // Month mode: show monthly calendar grid
        if (viewMode == 2) {
            item {
                MonthCalendarView(
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    selectedDay = selectedDay,
                    onDaySelect = { selectedDay = it },
                    tasks = tasks,
                    isDark = isDark,
                    lang = lang
                )
            }
        }

        item { ReminderCard(onSetReminder = { /* TODO */ }, isDark = isDark, lang = lang) }

        item {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                categories = categories,
                selectedCategory = selectedCategoryFilter,
                onCategorySelected = { selectedCategoryFilter = it },
                lang = lang,
                cardColor = cardColor,
                textPrimary = textPrimary
            )
        }

        if (filteredTasks.isEmpty() || filteredTasks.all { it.isDone }) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyText,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        tasksByCategory.forEach { (category, categoryTasks) ->
            item {
                SectionHeader(
                    title = category,
                    onSeeAll = { /* TODO */ },
                    allTaskDone = categoryTasks.all { it.isDone },
                    textPrimary = textPrimary
                )
            }
            itemsIndexed(categoryTasks) { _, task ->
                val globalIndex = tasks.indexOf(task)
                RoutineTaskRow(
                    task = task,
                    onItemClick = { onTaskToggle(task) },
                    isEditMode = editingTaskIndex == globalIndex,
                    onEnterEditMode = { editingTaskIndex = globalIndex },
                    onCancelEdit = { editingTaskIndex = null },
                    onSaveEdit = { title, subtitle, priority, duedate ->
                        onTaskUpdate(globalIndex, task.copy(title = title, subtitle = subtitle, priority = priority, duedate = duedate))
                        editingTaskIndex = null
                    },
                    onDelete = { onTaskDelete(globalIndex) },
                    cardColor = cardColor,
                    textPrimary = textPrimary
                )
            }
        }
    }
}

@Composable
private fun DateHeaderBar(
    today: LocalDate,
    selectedDay: Int,
    viewMode: Int,
    selectedWeek: Int,
    selectedYear: Int,
    selectedMonth: Int,
    onViewModeChange: (Int) -> Unit,
    onWeekSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    isDark: Boolean = false,
    lang: String = "vi"
) {
    val cardColor = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown
    val tabBg = if (isDark) Color(0xFF3D2B1F) else BackgroundCream

    // Left side formatting
    val dayOfWeekShort = today.dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale("vi"))
        .replaceFirstChar { it.uppercase() }
    val dateFormatted = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    var showWeekPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    val maxWeek = remember(selectedYear) {
        LocalDate.of(selectedYear, 12, 28)
            .get(WeekFields.ISO.weekOfWeekBasedYear())
    }
    val weekList = remember(selectedYear) { (1..maxWeek).toList() }

    fun mondayOfWeek(week: Int): LocalDate =
        LocalDate.ofYearDay(selectedYear, 1)
            .with(WeekFields.ISO.weekOfWeekBasedYear(), week.toLong())
            .with(WeekFields.ISO.dayOfWeek(), 1)

    val viewLabels = if (lang == "vi") listOf("Ngày", "Tuần", "Tháng")
                    else listOf("Day", "Week", "Month")

    // Year selection dialog (for Week mode)
    if (showYearPicker) {
        Dialog(onDismissRequest = { showYearPicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardColor
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (lang == "vi") "Chọn năm" else "Select Year",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(Modifier.height(16.dp))
                    val yearsList = (2020..2030).toList()
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(yearsList.size) { idx ->
                            val y = yearsList[idx]
                            val isSelected = y == selectedYear
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF2979FF).copy(alpha = 0.12f) else Color.Transparent)
                                    .clickable {
                                        onYearSelected(y)
                                        showYearPicker = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$y",
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF2979FF) else textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF2979FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Week selection dialog (for Week mode)
    if (showWeekPicker) {
        Dialog(onDismissRequest = { showWeekPicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardColor
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        text = if (lang == "vi") "Chọn tuần" else "Select week",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(weekList.size) { idx ->
                            val wk = weekList[idx]
                            val mon = mondayOfWeek(wk)
                            val sun = mon.plusDays(6)
                            val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            val isSelected = wk == selectedWeek
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFF2979FF).copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onWeekSelected(wk)
                                        showWeekPicker = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == "vi") "Tuần $wk" else "Week $wk",
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF2979FF) else textColor
                                    )
                                    Text(
                                        text = "(${mon.format(fmt)} → ${sun.format(fmt)})",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF2979FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Month & Year Picker Dialog (for Month mode)
    if (showMonthYearPicker) {
        Dialog(onDismissRequest = { showMonthYearPicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardColor
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (lang == "vi") "Chọn Tháng & Năm" else "Select Month & Year",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (lang == "vi") "Năm" else "Year",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    val yearsList = (2020..2030).toList()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(yearsList.size) { index ->
                            val y = yearsList[index]
                            val isSelectedY = y == selectedYear
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelectedY) Color(0xFF2979FF) else tabBg)
                                    .clickable { onYearSelected(y) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "$y",
                                    fontSize = 14.sp,
                                    color = if (isSelectedY) Color.White else textColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (lang == "vi") "Tháng" else "Month",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    val monthsList = if (lang == "vi") {
                        listOf("Th 1", "Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "Th 8", "Th 9", "Th 10", "Th 11", "Th 12")
                    } else {
                        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (col in 0 until 3) {
                                    val mIdx = row * 3 + col
                                    val mVal = mIdx + 1
                                    val isSelectedM = mVal == selectedMonth
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelectedM) Color(0xFF2979FF) else tabBg)
                                            .clickable {
                                                onMonthSelected(mVal)
                                                showMonthYearPicker = false
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = monthsList[mIdx],
                                            fontSize = 14.sp,
                                            color = if (isSelectedM) Color.White else textColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (viewMode) {
                0 -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$dayOfWeekShort, $dateFormatted",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
                1 -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tabBg)
                                .clickable { showYearPicker = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$selectedYear",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tabBg)
                                .clickable { showWeekPicker = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (lang == "vi") "Tuần $selectedWeek" else "Week $selectedWeek",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tabBg)
                            .clickable { showMonthYearPicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val displayMonthText = if (lang == "vi") "Tháng $selectedMonth, $selectedYear"
                                              else "${LocalDate.of(selectedYear, selectedMonth, 1).month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}, $selectedYear"
                        Text(
                            text = displayMonthText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(tabBg)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                viewLabels.forEachIndexed { index, label ->
                    val isSelected = viewMode == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color(0xFF2979FF)
                                else Color.Transparent
                            )
                            .clickable { onViewModeChange(index) }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else textColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarView(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDaySelect: (Int) -> Unit,
    tasks: List<HomeUiState>,
    isDark: Boolean = false,
    lang: String = "vi"
) {
    val cardColor = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown
    val tabBg = if (isDark) Color(0xFF3D2B1F) else BackgroundCream
    val daysOfWeek = if (lang == "vi") listOf("Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN")
                     else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val firstOfMonth = remember(selectedYear, selectedMonth) {
        LocalDate.of(selectedYear, selectedMonth, 1)
    }
    val dayOfWeekOfFirst = firstOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val daysInMonth = firstOfMonth.lengthOfMonth()

    val emptySlots = dayOfWeekOfFirst - 1
    val totalSlots = emptySlots + daysInMonth

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Weekday Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    val isWeekend = index >= 5
                    Text(
                        text = day,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWeekend) Color(0xFFE53935) else TextMuted,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Grid of days
            val rows = (totalSlots + 6) / 7
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 7) {
                        val slotIndex = row * 7 + col
                        val dayNum = slotIndex - emptySlots + 1
                        val isValidDay = dayNum in 1..daysInMonth

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidDay) {
                                val isSelected = dayNum == selectedDay
                                val date = LocalDate.of(selectedYear, selectedMonth, dayNum)
                                val hasTasks = remember(tasks, date) {
                                    tasks.any { it.duedate.toLocalDate() == date }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF2979FF) else Color.Transparent)
                                        .clickable { onDaySelect(dayNum) }
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else textColor
                                    )
                                    if (hasTasks) {
                                        Spacer(Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color(0xFFFFC107))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (row < rows - 1) {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

// ─── Week View (Mon–Sun row for selected week) ───────────────────────────────

@Composable
private fun WeekView(
    today: LocalDate,
    selectedWeek: Int,
    selectedDay: Int,
    onSelect: (Int) -> Unit,
    isDark: Boolean = false
) {
    val cardColor = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown
    val currentYear = today.year

    // Monday of selected week
    val monday = remember(selectedWeek) {
        LocalDate.ofYearDay(currentYear, 1)
            .with(WeekFields.ISO.weekOfWeekBasedYear(), selectedWeek.toLong())
            .with(WeekFields.ISO.dayOfWeek(), 1)
    }
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }
    val dayLabels = listOf("Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN")
    val weekendIndices = setOf(5, 6) // Th 7, CN

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weekDays.forEachIndexed { idx, date ->
            val isSelected = date.dayOfMonth == selectedDay
            val isToday = date == today
            val isWeekend = idx in weekendIndices
            val dayNum = date.dayOfMonth

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF2979FF) else cardColor)
                    .clickable { onSelect(dayNum) }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = dayLabels[idx],
                    fontSize = 10.sp,
                    color = when {
                        isSelected -> Color.White
                        isWeekend -> Color(0xFFE53935)
                        else -> TextMuted
                    },
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$dayNum",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isSelected -> Color.White
                        isWeekend -> Color(0xFFE53935)
                        else -> textColor
                    }
                )
                if (isToday) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0xFFFFC107))
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    lang: String = "vi",
    cardColor: Color = SurfaceWhite,
    textPrimary: Color = InkBrown
) {
    val placeholder = if (lang == "vi") "Tìm kiếm việc cần làm ..." else "Search tasks..."
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BaseSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor),
            placeholderText = placeholder
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = InkBrown,
                        selectedLabelColor = SurfaceWhite,
                        labelColor = textPrimary,
                        containerColor = cardColor
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = InkBrown.copy(alpha = 0.2f),
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun GreetingHeader(
    name: String,
    tasks: List<HomeUiState>,
    textPrimary: Color = InkBrown,
    onProfileClick: () -> Unit
) {
    val done = tasks.count { it.isDone }
    val progress = if (tasks.isEmpty()) 0f else done / tasks.size.toFloat()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Morning, $name",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 5.dp.toPx()
                drawArc(
                    color = RingTrack,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(stroke)
                )
                drawArc(
                    color = StreakOrange,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(
                        width = stroke,
                        cap = StrokeCap.Round
                    )
                )
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PeachStart, PeachEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile avatar",
                    tint = SurfaceWhite,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun WeekDaySelector(
    selectedDay: Int,
    onSelect: (Int) -> Unit,
    isDark: Boolean = false
) {
    val today = remember { LocalDate.now() }
    val daysInMonth = today.lengthOfMonth()
    val monthDays = remember(today) {
        (1..daysInMonth).map { day ->
            val date = today.withDayOfMonth(day)
            val dayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
            dayName to day
        }
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedDay - 3).coerceAtLeast(0))
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(monthDays) { _, (label, day) ->
            val selected = day == selectedDay
            val isToday = day == today.dayOfMonth
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Transparent)
                    .clickable { onSelect(day) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) InkBrown else TextMuted
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (selected) InkBrown else SurfaceWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) SurfaceWhite else InkBrown
                    )
                }
                if (isToday && !selected) {
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AccentTerracotta))
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    onSetReminder: () -> Unit,
    isDark: Boolean = false,
    lang: String = "vi"
) {
    val textColor = if (isDark) Color(0xFF2B1D14) else InkBrown
    val titleText = if (lang == "vi") "Đặt lịch nhắc nhở" else "Set the reminder"
    val bodyText = if (lang == "vi") "Đừng bỏ lỡ thói quen buổi sáng! Đặt nhắc nhở để duy trì."
                   else "Never miss your morning routine! Set a reminder to stay on track."
    val btnText = if (lang == "vi") "Đặt ngay" else "Set Now"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(PeachStart, PeachEnd)))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = bodyText,
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onSetReminder,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTerracottaDeep)
                ) {
                    Text(btnText, color = SurfaceWhite, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = "Reminder bell",
                    tint = SurfaceWhite,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
    allTaskDone: Boolean,
    textPrimary: Color = InkBrown
) {
    AnimatedVisibility(visible = !allTaskDone, exit = fadeOut() + shrinkVertically()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Text(
                "See all",
                fontSize = 13.sp,
                color = AccentTerracotta,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineTaskRow(
    task: HomeUiState,
    onItemClick: () -> Unit,
    isEditMode: Boolean,
    onEnterEditMode: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String, String, Int, LocalDateTime) -> Unit,
    onDelete: () -> Unit,
    cardColor: Color = SurfaceWhite,
    textPrimary: Color = InkBrown
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteAlert by remember { mutableStateOf(false) }

    var editTitle by remember(isEditMode) { mutableStateOf(task.title) }
    var editSubtitle by remember(isEditMode) { mutableStateOf(task.subtitle) }
    var editPriority by remember(isEditMode) { mutableIntStateOf(task.priority) }
    var editDueDate by remember(isEditMode) { mutableStateOf(task.duedate) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (!isEditMode) {
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onItemClick()
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        showDeleteAlert = true
                        false
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    )

    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showDeleteAlert = false },
            title = { Text("Delete Task") },
            text = { Text("Do you want delete it?") },
            confirmButton = {
                TextButton(onClick = { 
                    onDelete()
                    showDeleteAlert = false
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlert = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AnimatedVisibility(visible = !task.isDone, exit = fadeOut() + shrinkVertically()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeline column
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight()
            ) {
        // Checkmark circle — green tick with fade animation
        var showGreenTick by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (task.isDone) StreakOrange else SurfaceWhite)
                .border(1.dp, if (task.isDone) Color.Transparent else TextMuted.copy(alpha = 0.4f), CircleShape)
                .clickable(
                    indication = ripple(bounded = true, radius = 14.dp),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    showGreenTick = true
                    onItemClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if (task.isDone) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completed",
                    tint = SurfaceWhite,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                // Green tick flash animation
                androidx.compose.animation.AnimatedVisibility(
                    visible = showGreenTick,
                    exit = fadeOut(animationSpec = tween(durationMillis = 600))
                ) {
                    LaunchedEffect(showGreenTick) {
                        if (showGreenTick) {
                            kotlinx.coroutines.delay(600)
                            showGreenTick = false
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Checked",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Task card
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = !isEditMode,
                enableDismissFromEndToStart = !isEditMode,
                backgroundContent = {
                    val direction = dismissState.dismissDirection
                    val color = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Green for toggle/complete
                        SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF5350) // Red for delete
                        else -> Color.Transparent
                    }
                    val alignment = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                    val icon = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.CheckCircle
                        SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                        else -> null
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp))
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (task.isDone) Color(0xFFE8F5E9) else cardColor,
                    shadowElevation = if (task.isDone) 0.dp else 1.dp,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                if (isEditMode) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            placeholder = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        TextField(
                            value = editSubtitle,
                            onValueChange = { editSubtitle = it },
                            placeholder = { Text("Subtitle") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        
                        // Date/Time Selection Row
                        DateDueRow(
                            dateTime = editDueDate,
                            onDateTimeChange = { editDueDate = it }
                        )
                        
                        // Priority Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Độ ưu tiên:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkBrown)
                            listOf(0, 1, 2).forEach { p ->
                                val color = when(p) {
                                    0 -> Color(0xFF7C9A6D)
                                    1 -> Color(0xFFD9A441)
                                    else -> Color(0xFFC1543F)
                                }
                                FilterChip(
                                    selected = editPriority == p,
                                    onClick = { editPriority = p },
                                    label = { Text("P$p") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color,
                                        selectedLabelColor = Color.White
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Flag,
                                            contentDescription = null,
                                            tint = if(editPriority != p) color else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onCancelEdit) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { onSaveEdit(editTitle, editSubtitle, editPriority, editDueDate) },
                                colors = ButtonDefaults.buttonColors(containerColor = InkBrown)
                            ) { 
                                Text("Accept", color = Color.White) 
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                            Text(task.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                            Spacer(Modifier.height(2.dp))
                            Text(task.subtitle, fontSize = 12.sp, color = TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.AccessTime, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.height(2.dp))
                            Text(task.duedate.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(Modifier.width(12.dp))
                        Box {
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            IconButton(
                                onClick = { expanded = true },
                                interactionSource = interactionSource,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isPressed) InkBrown.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreHoriz,
                                    contentDescription = "Options",
                                    tint = InkBrown,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                    onClick = { 
                                        expanded = false
                                        showDeleteAlert = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                    onClick = { 
                                        expanded = false
                                        onEnterEditMode()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDueRow(
    dateTime: LocalDateTime,
    onDateTimeChange: (LocalDateTime) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = { showDatePicker = true },
            label = { Text(dateTime.format(dateFormatter), fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp)) }
        )
        AssistChip(
            onClick = { showTimePicker = true },
            label = { Text(dateTime.format(timeFormatter), fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp)) }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val newDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateTimeChange(dateTime.toLocalTime().atDate(newDate))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = dateTime.hour,
            initialMinute = dateTime.minute,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                onDateTimeChange(dateTime.toLocalDate().atTime(newTime))
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun MorningRoutineScreenPreview() {
    MaterialTheme {
        HomeContent(
            innerPadding = PaddingValues(0.dp),
            tasks = listOf(
                HomeUiState(
                    "Uống Nước Sông", "Lượng nước: 500ml",
                    LocalDateTime.now(), LocalDateTime.now().withHour(8).withMinute(0),
                    1, false, "Morning Routine"
                ),
                HomeUiState(
                    "Thiền tích nội công", "Thời gian: 15 phút",
                    LocalDateTime.now(), LocalDateTime.now().withHour(9).withMinute(0),
                    2, false, "Morning Routine"
                ),
                HomeUiState(
                    "Check Email", "Xử lý inbox công việc",
                    LocalDateTime.now(), LocalDateTime.now().withHour(10).withMinute(30),
                    3, false, "Work"
                )
            ),
            onProfileClick = {},
            onTaskToggle = {},
            onTaskUpdate = { _, _ -> },
            onTaskDelete = {}
        )
    }
}
