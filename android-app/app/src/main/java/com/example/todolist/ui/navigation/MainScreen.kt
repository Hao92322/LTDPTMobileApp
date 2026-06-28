package com.example.todolist.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.category.CategoryManageScreen
import com.example.todolist.ui.home.HomeScreen
import com.example.todolist.ui.theme.*
import com.example.todolist.ui.todo.CreateTodoScreen
data class NavItem(val icon: ImageVector, val label: String)
val navItems = listOf(
    NavItem(Icons.Filled.Home, "Home"),
    NavItem(Icons.Filled.CalendarMonth, "Calendar"),
    NavItem(Icons.Filled.Add, "Add"), // center, drawn raised
    NavItem(Icons.Filled.Insights, "Insights"),
    NavItem(Icons.Filled.Person, "Profile"),
)
@Composable
fun MainScreen() {
    var selectedNav by remember { mutableIntStateOf(0) }
    var showCreateTodo by remember { mutableStateOf(false) }
    if (showCreateTodo) {
        CreateTodoScreen(
            onBack = { showCreateTodo = false },
            onSave = {
                showCreateTodo = false 
            }
        )
    } else {
        Scaffold(
            containerColor = BackgroundCream,
            bottomBar = {
                CurvedBottomNav(
                    selectedIndex = selectedNav,
                    onSelect = { selectedNav = it },
                    onAddClick = { showCreateTodo = true }
                )
            }
        ) { innerPadding ->
            when (selectedNav) {
                0 -> HomeScreen(innerPadding = innerPadding)
                1 -> CategoryManageScreen()
                3 -> Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { Text("Insights") }
                4 -> Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { Text("Profile") }
            }
        }
    }
}
@Composable
private fun CurvedBottomNav(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
    ) {
        Surface(
            color = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                navItems.forEachIndexed { index, item ->
                    if (index == 2) {
                        // reserve empty space under the raised center button
                        Spacer(modifier = Modifier.width(56.dp))
                    } else {
                        NavIconButton(
                            item = item,
                            selected = selectedIndex == index,
                            onClick = { onSelect(index) }
                        )
                    }
                }
            }
        }
        // Raised center button
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
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add new habit",
                tint = SurfaceWhite,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavIconButton(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) AccentTerracotta else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (selected) AccentTerracotta else Color.Transparent)
        )
    }
}
@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun MorningRoutineScreenPreview() {
    MaterialTheme {
        MainScreen()
    }
}

