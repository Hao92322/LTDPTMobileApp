package com.example.todolist.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.theme.*

data class UserProfile(
 val name: String,
 val handle: String,
 val joinedLabel: String,
 val totalTasksDone: Int,
 val currentStreak: Int,
 val bestStreak: Int,
 val weeklyCompletion: List<Int>, // Mon..Sun
 val categoryStats: List<CategoryStat>,
 val badges: List<BadgeUi>
)

data class CategoryStat(val name: String, val color: Color, val icon: ImageVector, val count: Int)
data class BadgeUi(val label: String, val icon: ImageVector, val unlocked: Boolean)

private fun sampleUserProfile() = UserProfile(
 name = "Budi Santoso",
 handle = "@budi.santoso",
 joinedLabel = "Mar 2025",
 totalTasksDone = 128,
 currentStreak = 6,
 bestStreak = 21,
 weeklyCompletion = listOf(3, 4, 2, 4, 3, 1, 2),
 categoryStats = listOf(
  CategoryStat("Hydration", SandBg, Icons.Filled.LocalDrink, 34),
  CategoryStat("Mindfulness", MintBg, Icons.Filled.SelfImprovement, 41),
  CategoryStat("Fitness", LavenderBg, Icons.Filled.Accessibility, 28),
  CategoryStat("Outdoor", SkyBg, Icons.Filled.DirectionsWalk, 25),
 ),
 badges = listOf(
  BadgeUi("7-Day Streak", Icons.Filled.LocalFireDepartment, true),
  BadgeUi("Early Bird", Icons.Filled.WbSunny, true),
  BadgeUi("100 Tasks", Icons.Filled.EmojiEvents, true),
  BadgeUi("30-Day Streak", Icons.Filled.WorkspacePremium, false),
  BadgeUi("Night Owl", Icons.Filled.NightsStay, false),
 )
)

@Composable
fun ProfileScreen(
 profile: UserProfile = remember { sampleUserProfile() },
 onEditProfile: () -> Unit = {},
 onSettingsItemClick: (String) -> Unit = {},
 onLogout: () -> Unit = {}
) {
 Scaffold(containerColor = BackgroundCream, topBar = { ProfileTopBar() }) { innerPadding ->
  Column(
   modifier = Modifier
    .fillMaxSize()
    .padding(innerPadding)
    .verticalScroll(rememberScrollState())
    .padding(horizontal = 20.dp),
   verticalArrangement = Arrangement.spacedBy(22.dp)
  ) {
   Spacer(Modifier.height(4.dp))
   ProfileHeader(profile, onEditProfile)
   StatsRow(profile)
   WeeklyActivityCard(profile.weeklyCompletion)
   CategoryBreakdownCard(profile.categoryStats)
   SettingsSection(onItemClick = onSettingsItemClick, onLogout = onLogout)
   Spacer(Modifier.height(50.dp))
  }
 }
}
@Composable
private fun ProfileTopBar() {
 Row(
  modifier = Modifier
   .fillMaxWidth()
   .background(BackgroundCream)
   .padding(horizontal = 20.dp, vertical = 16.dp),
  verticalAlignment = Alignment.CenterVertically
 ) {
  Text(
   text = "Profile",
   modifier = Modifier.weight(1f),
   textAlign = TextAlign.Center,
   fontSize = 20.sp,
   fontWeight = FontWeight.Bold,
   color = InkBrown
  )
  Spacer(Modifier.size(40.dp))
 }
}


@Composable
private fun ProfileHeader(profile: UserProfile, onEditProfile: () -> Unit) {
 Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
  Box(contentAlignment = Alignment.BottomEnd) {
   Box(
    modifier = Modifier
     .size(96.dp)
     .clip(CircleShape)
     .background(Brush.linearGradient(listOf(PeachStart, PeachEnd))),
    contentAlignment = Alignment.Center
   ) {
    Icon(
     imageVector = Icons.Filled.Person,
     contentDescription = "Avatar",
     tint = SurfaceWhite,
     modifier = Modifier.size(48.dp)
    )
   }
   Box(
    modifier = Modifier
     .size(30.dp)
     .clip(CircleShape)
     .background(SurfaceWhite)
     .border(2.dp, BackgroundCream, CircleShape)
     .clickable { onEditProfile() },
    contentAlignment = Alignment.Center
   ) {
    Icon(
     imageVector = Icons.Filled.Edit,
     contentDescription = "Edit profile",
     tint = AccentTerracotta,
     modifier = Modifier.size(14.dp)
    )
   }
  }
  Spacer(Modifier.height(12.dp))
  Text(profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = InkBrown)
  Spacer(Modifier.height(2.dp))
  Text(
   text = "${profile.handle} · Since ${profile.joinedLabel}",
   fontSize = 13.sp,
   color = TextMuted
  )
 }
}

@Composable
private fun StatsRow(profile: UserProfile) {
 Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
  StatCard(Icons.Filled.TaskAlt, profile.totalTasksDone.toString(), "Tasks done", Modifier.weight(1f))
  StatCard(Icons.Filled.LocalFireDepartment, "${profile.currentStreak}d", "Current streak", Modifier.weight(1f))
  StatCard(Icons.Filled.EmojiEvents, "${profile.bestStreak}d", "Best streak", Modifier.weight(1f))
 }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
 Column(
  modifier = modifier
   .clip(RoundedCornerShape(16.dp))
   .background(SurfaceWhite)
   .padding(vertical = 16.dp, horizontal = 8.dp),
  horizontalAlignment = Alignment.CenterHorizontally
 ) {
  Icon(icon, contentDescription = null, tint = StreakOrange, modifier = Modifier.size(20.dp))
  Spacer(Modifier.height(6.dp))
  Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkBrown)
  Spacer(Modifier.height(2.dp))
  Text(label, fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center)
 }
}

@Composable
private fun WeeklyActivityCard(weekly: List<Int>) {
 val maxVal = (weekly.maxOrNull() ?: 1).coerceAtLeast(1)
 val days = listOf("M", "T", "W", "T", "F", "S", "S")

 Column(
  modifier = Modifier
   .fillMaxWidth()
   .clip(RoundedCornerShape(20.dp))
   .background(SurfaceWhite)
   .padding(18.dp)
 ) {
  Text("This week", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkBrown)
  Spacer(Modifier.height(16.dp))
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
   weekly.forEachIndexed { index, count ->
    val fraction = count / maxVal.toFloat()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
     Box(
      modifier = Modifier
       .height(70.dp)
       .width(18.dp),
      contentAlignment = Alignment.BottomCenter
     ) {
      Box(
       modifier = Modifier
        .fillMaxWidth()
        .height((70 * fraction).dp.coerceAtLeast(6.dp))
        .clip(RoundedCornerShape(6.dp))
        .background(Brush.verticalGradient(listOf(PeachEnd, AccentTerracotta)))
      )
     }
     Spacer(Modifier.height(6.dp))
     Text(days[index], fontSize = 11.sp, color = TextMuted)
    }
   }
  }
 }
}
@Composable
private fun BadgeItem(badge: BadgeUi) {
 Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
  Box(
   modifier = Modifier
    .size(56.dp)
    .clip(CircleShape)
    .background(
     if (badge.unlocked) Brush.linearGradient(listOf(AccentTerracotta, AccentTerracottaDeep))
     else Brush.linearGradient(listOf(InputBorder, InputBorder))
    ),
   contentAlignment = Alignment.Center
  ) {
   Icon(
    imageVector = badge.icon,
    contentDescription = badge.label,
    tint = if (badge.unlocked) SurfaceWhite else TextMuted,
    modifier = Modifier.size(26.dp)
   )
  }
  Spacer(Modifier.height(6.dp))
  Text(
   text = badge.label,
   fontSize = 11.sp,
   color = if (badge.unlocked) InkBrown else TextMuted,
   textAlign = TextAlign.Center,
   maxLines = 2
  )
 }
}

@Composable
private fun CategoryBreakdownCard(stats: List<CategoryStat>) {
 val total = stats.sumOf { it.count }.coerceAtLeast(1)
 Column(
  modifier = Modifier
   .fillMaxWidth()
   .clip(RoundedCornerShape(20.dp))
   .background(SurfaceWhite)
   .padding(18.dp)
 ) {
  Text("By category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkBrown)
  Spacer(Modifier.height(14.dp))
  stats.forEachIndexed { index, stat ->
   CategoryStatRow(stat, total)
   if (index != stats.lastIndex) Spacer(Modifier.height(12.dp))
  }
 }
}

@Composable
private fun CategoryStatRow(stat: CategoryStat, total: Int) {
 Column {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
   Box(
    modifier = Modifier
     .size(28.dp)
     .clip(RoundedCornerShape(9.dp))
     .background(stat.color),
    contentAlignment = Alignment.Center
   ) {
    Icon(stat.icon, contentDescription = null, tint = InkBrown, modifier = Modifier.size(16.dp))
   }
   Spacer(Modifier.width(10.dp))
   Text(stat.name, modifier = Modifier.weight(1f), fontSize = 13.sp, color = InkBrown)
   Text("${stat.count}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBrown)
  }
  Spacer(Modifier.height(6.dp))
  val fraction = (stat.count / total.toFloat()).coerceIn(0f, 1f)
  Box(
   modifier = Modifier
    .fillMaxWidth()
    .height(6.dp)
    .clip(RoundedCornerShape(3.dp))
    .background(BackgroundCream)
  ) {
   Box(
    modifier = Modifier
     .fillMaxWidth(fraction)
     .height(6.dp)
     .clip(RoundedCornerShape(3.dp))
     .background(stat.color)
   )
  }
 }
}
@Composable
private fun SettingsSection(onItemClick: (String) -> Unit, onLogout: () -> Unit) {
 Column {
  Surface(
   shape = RoundedCornerShape(20.dp),
   color = SurfaceWhite,
   shadowElevation = 1.dp,
   modifier = Modifier.fillMaxWidth()
  ) {
   Column {
    SettingsRow(Icons.Filled.NotificationsNone, "Notifications") { onItemClick("notifications") }
    RowDivider()
    SettingsRow(Icons.Filled.Alarm, "Reminders") { onItemClick("reminders") }
    RowDivider()
    SettingsRow(Icons.Filled.HelpOutline, "Help & Support") { onItemClick("help") }
   }
  }
  Spacer(Modifier.height(14.dp))
  Row(
   modifier = Modifier
    .fillMaxWidth()
    .clip(RoundedCornerShape(20.dp))
    .background(SurfaceWhite)
    .clickable { onLogout() }
    .padding(16.dp),
   verticalAlignment = Alignment.CenterVertically
  ) {
   Icon(Icons.Filled.Logout, contentDescription = null, tint = AccentTerracotta, modifier = Modifier.size(18.dp))
   Spacer(Modifier.width(10.dp))
   Text("Log Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AccentTerracotta)
  }
 }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
 Row(
  modifier = Modifier
   .fillMaxWidth()
   .clickable { onClick() }
   .padding(16.dp),
  verticalAlignment = Alignment.CenterVertically
 ) {
  Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
  Spacer(Modifier.width(12.dp))
  Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = InkBrown)
  Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
 }
}
@Composable
private fun RowDivider() {
 HorizontalDivider(color = BackgroundCream, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
}
@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun ProfileScreenPreview() {
 MaterialTheme {
  ProfileScreen()
 }
}