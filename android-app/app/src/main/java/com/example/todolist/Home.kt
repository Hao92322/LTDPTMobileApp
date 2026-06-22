package com.example.todolist
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val BackgroundCream = Color(0xFFF7EFE6)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val InkBrown = Color(0xFF2B1D14)
private val TextMuted = Color(0xFF9C8C7E)
private val AccentTerracotta = Color(0xFFB5611E)
private val AccentTerracottaDeep = Color(0xFF7A3E10)
private val PeachStart = Color(0xFFFBCBA0)
private val PeachEnd = Color(0xFFF4925A)
private val StreakOrange = Color(0xFFFF7A30)
private val RingTrack = Color(0xFFEADFD2)
private val SandBg = Color(0xFFFBE3C3)
private val MintBg = Color(0xFFDCEFD9)
private val LavenderBg = Color(0xFFE7DEF7)
private val SkyBg = Color(0xFFDBEBF6)

// Data
data class RoutineTask(
    val title: String,
    val subtitle: String,
    val time: String,
    val icon: ImageVector,
    val iconBg: Color,
    val streakDays: Int,
    val isDone: Boolean
)
private fun sampleTasks() = listOf(
    RoutineTask("Uống Nước Sông", "Streak 3 ngày", "5 phút", Icons.Filled.LocalDrink, SandBg, 3, true),
    RoutineTask("Thiền tích nội công", "Streak 6 ngày", "15 phút", Icons.Filled.SelfImprovement, MintBg, 6, true),
    RoutineTask("Dãn cơ", "Streak 5 ngày", "10 phút", Icons.Filled.Accessibility, LavenderBg, 5, false),
    RoutineTask("Đi Bộ", "Streak 3 ngày", "20 phút", Icons.AutoMirrored.Filled.DirectionsWalk, SkyBg, 3, false),
)
// Screen
@Composable
fun HomeScreen(innerPadding: PaddingValues) {
    val tasks = remember { sampleTasks() }
    val today = remember { LocalDate.now() }
    var selectedDay by remember { mutableIntStateOf(today.dayOfMonth) }
    val formattedDate = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
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
        item { SectionHeader(onSeeAll = { /* TODO */ }) }
        itemsIndexed(tasks) { index, task ->
            RoutineTaskRow(task = task, isLast = index == tasks.lastIndex)
        }
    }
}

// Header — signature element: a progress ring grown from today's completion,
@Composable
private fun GreetingHeader(name: String, date: String, tasks: List<RoutineTask>) {
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
                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
                )
                drawArc(
                    color = StreakOrange,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = stroke,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
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
private fun SectionHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Daily routine", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkBrown)
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
private fun RoutineTaskRow(task: RoutineTask, isLast: Boolean) {
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
            shape = RoundedCornerShape(18.dp),
            color = SurfaceWhite,
            shadowElevation = 1.dp,
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
                        .background(task.iconBg),
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
        MainScreen()
    }
}
