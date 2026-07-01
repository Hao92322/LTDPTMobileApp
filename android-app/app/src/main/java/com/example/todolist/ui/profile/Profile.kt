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
import androidx.compose.ui.window.Dialog
import com.example.todolist.ui.AppState
import com.example.todolist.ui.LocalAppState
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
 val appState = LocalAppState.current
 val isDark = appState.isDarkMode

 val bgColor = if (isDark) Color(0xFF1A120B) else BackgroundCream
 val cardColor = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
 val textPrimary = if (isDark) Color(0xFFF5E6D3) else InkBrown

 Scaffold(containerColor = bgColor, topBar = { ProfileTopBar(isDark = isDark) }) { innerPadding ->
  Column(
   modifier = Modifier
    .fillMaxSize()
    .padding(innerPadding)
    .verticalScroll(rememberScrollState())
    .padding(horizontal = 20.dp),
   verticalArrangement = Arrangement.spacedBy(22.dp)
  ) {
   Spacer(Modifier.height(4.dp))
   ProfileHeader(profile, onEditProfile, textPrimary)
   WeeklyActivityCard(profile.weeklyCompletion, cardColor, textPrimary)
   CategoryBreakdownCard(profile.categoryStats, cardColor, textPrimary, bgColor)
   SettingsSection(
    onItemClick = onSettingsItemClick,
    onLogout = onLogout,
    cardColor = cardColor,
    bgColor = bgColor,
    textPrimary = textPrimary
   )
   Spacer(Modifier.height(50.dp))
  }
 }
}
@Composable
private fun ProfileTopBar(isDark: Boolean = false) {
 val bg = if (isDark) Color(0xFF1A120B) else BackgroundCream
 val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown
 Row(
  modifier = Modifier
   .fillMaxWidth()
   .background(bg)
   .windowInsetsPadding(WindowInsets.statusBars)
   .padding(horizontal = 20.dp, vertical = 16.dp),
  verticalAlignment = Alignment.CenterVertically
 ) {
  Text(
   text = "Profile",
   modifier = Modifier.weight(1f),
   textAlign = TextAlign.Center,
   fontSize = 20.sp,
   fontWeight = FontWeight.Bold,
   color = textColor
  )
  Spacer(Modifier.size(40.dp))
 }
}


@Composable
private fun ProfileHeader(
 profile: UserProfile,
 onEditProfile: () -> Unit,
 textPrimary: Color = InkBrown
) {
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
  Text(profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
  Spacer(Modifier.height(2.dp))
  Text(
   text = "${profile.handle} · Since ${profile.joinedLabel}",
   fontSize = 13.sp,
   color = TextMuted
  )
 }
}

@Composable
private fun StatsRow(
 profile: UserProfile,
 cardColor: Color = SurfaceWhite,
 textPrimary: Color = InkBrown
) {
 Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
  StatCard(Icons.Filled.TaskAlt, profile.totalTasksDone.toString(), "Tasks done", Modifier.weight(1f), cardColor, textPrimary)
  StatCard(Icons.Filled.LocalFireDepartment, "${profile.currentStreak}d", "Current streak", Modifier.weight(1f), cardColor, textPrimary)
  StatCard(Icons.Filled.EmojiEvents, "${profile.bestStreak}d", "Best streak", Modifier.weight(1f), cardColor, textPrimary)
 }
}

@Composable
private fun StatCard(
 icon: ImageVector,
 value: String,
 label: String,
 modifier: Modifier = Modifier,
 cardColor: Color = SurfaceWhite,
 textPrimary: Color = InkBrown
) {
 Column(
  modifier = modifier
   .clip(RoundedCornerShape(16.dp))
   .background(cardColor)
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

@Composable
private fun WeeklyActivityCard(
 weekly: List<Int>,
 cardColor: Color = SurfaceWhite,
 textPrimary: Color = InkBrown
) {
 val maxVal = (weekly.maxOrNull() ?: 1).coerceAtLeast(1)
 val days = listOf("M", "T", "W", "T", "F", "S", "S")

 Column(
  modifier = Modifier
   .fillMaxWidth()
   .clip(RoundedCornerShape(20.dp))
   .background(cardColor)
   .padding(18.dp)
 ) {
  Text("This week", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
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
private fun CategoryBreakdownCard(
 stats: List<CategoryStat>,
 cardColor: Color = SurfaceWhite,
 textPrimary: Color = InkBrown,
 bgColor: Color = BackgroundCream
) {
 val total = stats.sumOf { it.count }.coerceAtLeast(1)
 Column(
  modifier = Modifier
   .fillMaxWidth()
   .clip(RoundedCornerShape(20.dp))
   .background(cardColor)
   .padding(18.dp)
 ) {
  Text("By category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
  Spacer(Modifier.height(14.dp))
  stats.forEachIndexed { index, stat ->
   CategoryStatRow(stat, total, textPrimary, bgColor)
   if (index != stats.lastIndex) Spacer(Modifier.height(12.dp))
  }
 }
}

@Composable
private fun CategoryStatRow(
 stat: CategoryStat,
 total: Int,
 textPrimary: Color = InkBrown,
 bgColor: Color = BackgroundCream
) {
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
   Text(stat.name, modifier = Modifier.weight(1f), fontSize = 13.sp, color = textPrimary)
   Text("${stat.count}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
  }
  Spacer(Modifier.height(6.dp))
  val fraction = (stat.count / total.toFloat()).coerceIn(0f, 1f)
  Box(
   modifier = Modifier
    .fillMaxWidth()
    .height(6.dp)
    .clip(RoundedCornerShape(3.dp))
    .background(bgColor)
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
private fun SettingsSection(
 onItemClick: (String) -> Unit,
 onLogout: () -> Unit,
 cardColor: Color = SurfaceWhite,
 bgColor: Color = BackgroundCream,
 textPrimary: Color = InkBrown
) {
 val appState = LocalAppState.current
 var showLanguageDialog by remember { mutableStateOf(false) }

 // Language labels based on current language
 val labelNotifications = if (appState.language == "vi") "Thông báo" else "Notifications"
 val labelReminders = if (appState.language == "vi") "Nhắc nhở" else "Reminders"
 val labelHelp = if (appState.language == "vi") "Trợ giúp & Hỗ trợ" else "Help & Support"
 val labelLanguage = if (appState.language == "vi") "Ngôn ngữ" else "Language"
 val labelAppearance = if (appState.language == "vi") "Giao diện" else "Appearance"
 val labelLogout = if (appState.language == "vi") "Đăng xuất" else "Log Out"
 val labelDark = if (appState.language == "vi") "Tối" else "Dark"
 val labelLight = if (appState.language == "vi") "Sáng" else "Light"

 if (showLanguageDialog) {
  Dialog(onDismissRequest = { showLanguageDialog = false }) {
   Surface(
    shape = RoundedCornerShape(20.dp),
    color = cardColor
   ) {
    Column(modifier = Modifier.padding(24.dp)) {
     Text(
      text = labelLanguage,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = textPrimary
     )
     Spacer(Modifier.height(16.dp))
     // Vietnamese option
     Row(
      modifier = Modifier
       .fillMaxWidth()
       .clip(RoundedCornerShape(12.dp))
       .background(
        if (appState.language == "vi") AccentTerracotta.copy(alpha = 0.12f)
        else Color.Transparent
       )
       .clickable {
        appState.language = "vi"
        showLanguageDialog = false
       }
       .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
     ) {
      Text("🇻🇳", fontSize = 20.sp)
      Spacer(Modifier.width(12.dp))
      Text("Tiếng Việt", fontSize = 15.sp, color = textPrimary, modifier = Modifier.weight(1f))
      if (appState.language == "vi") {
       Icon(
        Icons.Filled.Check,
        contentDescription = null,
        tint = AccentTerracotta,
        modifier = Modifier.size(18.dp)
       )
      }
     }
     Spacer(Modifier.height(8.dp))
     // English option
     Row(
      modifier = Modifier
       .fillMaxWidth()
       .clip(RoundedCornerShape(12.dp))
       .background(
        if (appState.language == "en") AccentTerracotta.copy(alpha = 0.12f)
        else Color.Transparent
       )
       .clickable {
        appState.language = "en"
        showLanguageDialog = false
       }
       .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
     ) {
      Text("🇺🇸", fontSize = 20.sp)
      Spacer(Modifier.width(12.dp))
      Text("English", fontSize = 15.sp, color = textPrimary, modifier = Modifier.weight(1f))
      if (appState.language == "en") {
       Icon(
        Icons.Filled.Check,
        contentDescription = null,
        tint = AccentTerracotta,
        modifier = Modifier.size(18.dp)
       )
      }
     }
    }
   }
  }
 }

 Column {
  Surface(
   shape = RoundedCornerShape(20.dp),
   color = cardColor,
   shadowElevation = 1.dp,
   modifier = Modifier.fillMaxWidth()
  ) {
   Column {
    SettingsRow(Icons.Filled.NotificationsNone, labelNotifications, textPrimary) { onItemClick("notifications") }
    RowDivider(bgColor)
    SettingsRow(Icons.Filled.Alarm, labelReminders, textPrimary) { onItemClick("reminders") }
    RowDivider(bgColor)
    SettingsRow(Icons.Filled.HelpOutline, labelHelp, textPrimary) { onItemClick("help") }
    RowDivider(bgColor)
    // Language row
    Row(
     modifier = Modifier
      .fillMaxWidth()
      .clickable { showLanguageDialog = true }
      .padding(16.dp),
     verticalAlignment = Alignment.CenterVertically
    ) {
     Icon(
      Icons.Filled.Language,
      contentDescription = null,
      tint = TextMuted,
      modifier = Modifier.size(18.dp)
     )
     Spacer(Modifier.width(12.dp))
     Text(labelLanguage, modifier = Modifier.weight(1f), fontSize = 14.sp, color = textPrimary)
     // Current language badge
     Box(
      modifier = Modifier
       .clip(RoundedCornerShape(8.dp))
       .background(AccentTerracotta.copy(alpha = 0.1f))
       .padding(horizontal = 8.dp, vertical = 4.dp)
     ) {
      Text(
       text = if (appState.language == "vi") "VI" else "EN",
       fontSize = 12.sp,
       fontWeight = FontWeight.Bold,
       color = AccentTerracotta
      )
     }
     Spacer(Modifier.width(8.dp))
     Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
    RowDivider(bgColor)
    // Appearance (dark mode) row
    Row(
     modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 10.dp),
     verticalAlignment = Alignment.CenterVertically
    ) {
     Icon(
      imageVector = if (appState.isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
      contentDescription = null,
      tint = TextMuted,
      modifier = Modifier.size(18.dp)
     )
     Spacer(Modifier.width(12.dp))
     Text(labelAppearance, modifier = Modifier.weight(1f), fontSize = 14.sp, color = textPrimary)
     Text(
      text = if (appState.isDarkMode) labelDark else labelLight,
      fontSize = 12.sp,
      color = TextMuted
     )
     Spacer(Modifier.width(8.dp))
     Switch(
      checked = appState.isDarkMode,
      onCheckedChange = { appState.isDarkMode = it },
      colors = SwitchDefaults.colors(
       checkedThumbColor = SurfaceWhite,
       checkedTrackColor = AccentTerracotta,
       uncheckedThumbColor = SurfaceWhite,
       uncheckedTrackColor = TextMuted.copy(alpha = 0.4f)
      )
     )
    }
   }
  }
  Spacer(Modifier.height(14.dp))
  Row(
   modifier = Modifier
    .fillMaxWidth()
    .clip(RoundedCornerShape(20.dp))
    .background(cardColor)
    .clickable { onLogout() }
    .padding(16.dp),
   verticalAlignment = Alignment.CenterVertically
  ) {
   Icon(Icons.Filled.Logout, contentDescription = null, tint = AccentTerracotta, modifier = Modifier.size(18.dp))
   Spacer(Modifier.width(10.dp))
   Text(labelLogout, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AccentTerracotta)
  }
 }
}

@Composable
private fun SettingsRow(
 icon: ImageVector,
 label: String,
 textPrimary: Color = InkBrown,
 onClick: () -> Unit
) {
 Row(
  modifier = Modifier
   .fillMaxWidth()
   .clickable { onClick() }
   .padding(16.dp),
  verticalAlignment = Alignment.CenterVertically
 ) {
  Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
  Spacer(Modifier.width(12.dp))
  Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = textPrimary)
  Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
 }
}
@Composable
private fun RowDivider(bgColor: Color = BackgroundCream) {
 HorizontalDivider(color = bgColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
}
@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun ProfileScreenPreview() {
 MaterialTheme {
  ProfileScreen()
 }
}