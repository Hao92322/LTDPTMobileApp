package com.example.todolist.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.ui.navigation.MainScreen
import com.example.todolist.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Screen
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel = viewModel()
) {
    val tasks by viewModel.todoList.collectAsState()
    
    HomeContent(
        innerPadding = innerPadding,
        tasks = tasks,
        onTaskToggle = { globalIndex ->
            viewModel.toggleTodoStatus(globalIndex)
        }
    )
}

@Composable
private fun HomeContent(
    innerPadding: PaddingValues,
    tasks: List<HomeUiState>,
    onTaskToggle: (Int) -> Unit
) {
    val today = remember { LocalDate.now() }
    var selectedDay by remember { mutableIntStateOf(today.dayOfMonth) }
    val formattedDate = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
    }

    val tasksByCategory = remember(tasks) {
        tasks.groupBy { it.category }
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
        item { GreetingHeader(name = "Hao", date = formattedDate, tasks = tasks) }
        item {
            WeekDaySelector(
                selectedDay = selectedDay,
                onSelect = { selectedDay = it }
            )
        }
        item { ReminderCard(onSetReminder = { /* TODO */ }) }
        
        tasksByCategory.forEach { (category, categoryTasks) ->
            item { SectionHeader(title = category, onSeeAll = { /* TODO */ }) }
            itemsIndexed(categoryTasks) { _, task ->
                val globalIndex = tasks.indexOf(task)
                RoutineTaskRow(
                    task = task, 
                    isLast = task == categoryTasks.last(), 
                    onItemClick = { onTaskToggle(globalIndex) }
                )
            }
        }
    }
}

// Header — Load thong tin nguoi dung ngay thang nam hien tai
//Load them tien trinh cua nguoi dung trong ngay
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
// Week day selector
@Composable
private fun WeekDaySelector(selectedDay: Int, onSelect: (Int) -> Unit) {
    val today = remember { LocalDate.now() }
    val daysInMonth = today.lengthOfMonth()
    val monthDays = remember(today) {
        (1..daysInMonth).map { day ->
            val date = today.withDayOfMonth(day)
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to day
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(46.dp)
                    .clickable { onSelect(day) }
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
            }
        }
    }
}

// Reminder card
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

// Section header
@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
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
// Routine task row with a dotted timeline connecting each step
@Composable
private fun RoutineTaskRow(task: HomeUiState, isLast: Boolean, onItemClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (task.isDone) StreakOrange else SurfaceWhite)
                    .then(
                        if (!task.isDone) Modifier else Modifier
                    ),
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
            if (!isLast) {
                Canvas(modifier = Modifier
                    .weight(1f)
                    .width(2.dp)
                    .padding(top = 4.dp)
                ) {
                    drawLine(
                        color = TextMuted.copy(alpha = 0.4f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        // Task card
        Surface(
            onClick = onItemClick,
            shape = RoundedCornerShape(18.dp),
            color = if (task.isDone) Color(0xFFE8F5E9) else SurfaceWhite,
            shadowElevation = if (task.isDone) 0.dp else 1.dp,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (task.isDone) Color.Transparent else task.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(task.icon, contentDescription = task.title, tint = InkBrown, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkBrown)
                    Spacer(Modifier.height(2.dp))
                    Text(task.subtitle, fontSize = 12.sp, color = TextMuted)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.height(2.dp))
                    Text(task.time, fontSize = 11.sp, color = TextMuted)
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
                HomeUiState("Morning Water", "Streak 3", "5m", Icons.Filled.LocalDrink, Color.Cyan, 3, true, "Morning"),
                HomeUiState("Check Email", "Inbox", "10m", Icons.Filled.Email, Color.Yellow, 0, false, "Work")
            ),
            onTaskToggle = {}
        )
    }
}
