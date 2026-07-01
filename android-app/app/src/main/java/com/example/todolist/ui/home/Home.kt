package com.example.todolist.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import java.util.Locale

// Screen
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel = viewModel()
) {
    val tasks by viewModel.todoList.collectAsStateWithLifecycle()
    HomeContent(
        innerPadding = innerPadding,
        tasks = tasks,
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
    onTaskToggle: (HomeUiState) -> Unit,
    onTaskUpdate: (Int, HomeUiState) -> Unit,
    onTaskDelete: (Int) -> Unit
) {
    val today = remember { LocalDate.now() }
    var selectedDay by remember { mutableIntStateOf(today.dayOfMonth) }
    val formattedDate = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var editingTaskIndex by remember { mutableStateOf<Int?>(null) }

    val filteredTasks = remember(tasks, selectedDay, searchQuery, selectedCategoryFilter) {
        tasks.filter { task ->
            val matchesDay = task.duedate.dayOfMonth == selectedDay
            //Lay ra nhung cai task co bao gom cac ky tu trong searchQuery khong phan biet chu hoa chu thuong
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item { GreetingHeader(name = "Hao", date = formattedDate, tasks = filteredTasks) }
        item {
            WeekDaySelector(
                selectedDay = selectedDay,
                onSelect = { selectedDay = it }
            )
        }
        item { ReminderCard(onSetReminder = { /* TODO */ }) }

        item {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                categories = categories,
                selectedCategory = selectedCategoryFilter,
                onCategorySelected = { selectedCategoryFilter = it }
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
                        text = "Bạn đã rảnh rỗi không còn việc nào ngày hôm nay !!",
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
                    allTaskDone = categoryTasks.all { it.isDone }
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
                    onDelete = {
                        onTaskDelete(globalIndex)
                    }
                )
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
    onCategorySelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Search Bar
        BaseSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceWhite),
            placeholderText = "Tìm kiếm việc cần làm ..."
        )

        // Category Chips
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
                        labelColor = InkBrown,
                        containerColor = SurfaceWhite
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
private fun GreetingHeader(name: String, date: String, tasks: List<HomeUiState>) {
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
                color = InkBrown
            )
            Spacer(Modifier.height(4.dp))
            Text(text = date, fontSize = 14.sp, color = TextMuted)
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
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
private fun WeekDaySelector(selectedDay: Int, onSelect: (Int) -> Unit) {
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
                    .background(Color.Transparent) // Force transparent to fix gray block
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
private fun ReminderCard(onSetReminder: () -> Unit) {
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
                    text = "Set the reminder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBrown
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Never miss your morning routine! Set a reminder to stay on track.",
                    fontSize = 13.sp,
                    color = InkBrown.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onSetReminder,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTerracottaDeep)
                ) {
                    Text("Set Now", color = SurfaceWhite, fontWeight = FontWeight.SemiBold)
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
private fun SectionHeader(title: String, onSeeAll: () -> Unit, allTaskDone: Boolean) {
    AnimatedVisibility(visible = !allTaskDone, exit = fadeOut() + shrinkVertically()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkBrown)
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

@Composable
private fun RoutineTaskRow(
    task: HomeUiState,
    onItemClick: () -> Unit,
    isEditMode: Boolean,
    onEnterEditMode: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String, String, Int, LocalDateTime) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteAlert by remember { mutableStateOf(false) }

    var editTitle by remember(isEditMode) { mutableStateOf(task.title) }
    var editSubtitle by remember(isEditMode) { mutableStateOf(task.subtitle) }
    var editPriority by remember(isEditMode) { mutableIntStateOf(task.priority) }
    var editDueDate by remember(isEditMode) { mutableStateOf(task.duedate) }

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
                // Checkmark circle
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (task.isDone) StreakOrange else SurfaceWhite)
                        .border(1.dp, if (task.isDone) Color.Transparent else TextMuted.copy(alpha = 0.4f), CircleShape)
                        .clickable(onClick = onItemClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isDone) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = SurfaceWhite,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Task card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (task.isDone) Color(0xFFE8F5E9) else SurfaceWhite,
                shadowElevation = if (task.isDone) 0.dp else 1.dp,
                tonalElevation = 0.dp,
                modifier = Modifier.weight(1f)
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
                            Text(task.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkBrown)
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
                            IconButton(onClick = { expanded = true }) {
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
            onTaskToggle = {},
            onTaskUpdate = { _, _ -> },
            onTaskDelete = {}
        )
    }
}
