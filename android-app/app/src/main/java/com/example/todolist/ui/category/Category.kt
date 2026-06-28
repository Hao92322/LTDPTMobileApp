package com.example.todolist.ui.category
/*
 * Compose "Manage Categories" screen — same cream/terracotta style as
 * MorningRoutineScreen.kt and AddTodoScreen.kt. Reuses the public
 * `RoutineTask` data class from MorningRoutineScreen.kt, so keep this file
 * in the same package as that one.
 *
 * Needs: material3 + material-icons-extended (for LocalDrink,
 * SelfImprovement, Accessibility, DirectionsWalk, DeleteOutline, SearchOff).
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.home.HomeUiState
import com.example.todolist.ui.theme.*
import java.time.format.DateTimeFormatter

// ----------------------------------------------------------------------------
// Data
// ----------------------------------------------------------------------------
data class CategoryUi(
    val id: String,
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val todos: List<HomeUiState>
)

@Composable
fun CategoryManageScreen(
    categories: List<CategoryUi> = remember { emptyList()},
    onBack: () -> Unit = {},
    onCreateCategory: () -> Unit = {},
    onRenameCategory: (CategoryUi, String) -> Unit = { _, _ -> },
    onDeleteCategory: (CategoryUi) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }
    var pendingDelete by remember { mutableStateOf<CategoryUi?>(null) }

    val filtered = remember(categories, query) {
        if (query.isBlank()) categories
        else categories.filter { cat ->
            cat.name.contains(query, ignoreCase = true) ||
                    cat.todos.any { it.title.contains(query, ignoreCase = true) }
        }
    }

    Scaffold(containerColor = BackgroundCream, topBar = { ManageTopBar(onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            SearchRow(query = query, onQueryChange = { query = it }, onCreateClick = onCreateCategory)
            Spacer(Modifier.height(18.dp))

            if (filtered.isEmpty()) {
                EmptyState(query)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filtered, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            expanded = expandedIds.contains(category.id),
                            onToggleExpand = {
                                expandedIds = if (expandedIds.contains(category.id))
                                    expandedIds - category.id else expandedIds + category.id
                            },
                            onRename = { newName -> onRenameCategory(category, newName) },
                            onDeleteRequest = { pendingDelete = category }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        DeleteConfirmDialog(
            categoryName = target.name,
            onConfirm = { onDeleteCategory(target); pendingDelete = null },
            onDismiss = { pendingDelete = null }
        )
    }
}

// ----------------------------------------------------------------------------
// Top bar
// ----------------------------------------------------------------------------
@Composable
private fun ManageTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundCream)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceWhite)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = InkBrown, modifier = Modifier.size(18.dp))
        }
        Text(
            text = "Manage Categories",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkBrown
        )
        Spacer(Modifier.size(40.dp))
    }
}

// ----------------------------------------------------------------------------
// Search row with inline create button
// ----------------------------------------------------------------------------
@Composable
private fun SearchRow(query: String, onQueryChange: (String) -> Unit, onCreateClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .border(1.dp, InputBorder, RoundedCornerShape(16.dp)),
            placeholder = { Text("Search categories or todos", color = TextMuted, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = InkBrown),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AccentTerracotta
            )
        )

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AccentTerracotta, AccentTerracottaDeep)))
                .clickable { onCreateClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Create category", tint = SurfaceWhite)
        }
    }
}

// ----------------------------------------------------------------------------
// Category card — header (color tile, name, edit/delete, expand chevron)
// plus an animated list of that category's todos.
// ----------------------------------------------------------------------------
@Composable
private fun CategoryCard(
    category: CategoryUi,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onRename: (String) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(category.name) { mutableStateOf(category.name) }
    val chevronRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevron")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isEditing) { onToggleExpand() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(category.color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(category.icon, contentDescription = null, tint = InkBrown, modifier = Modifier.size(22.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isEditing) {
                        BasicTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkBrown),
                            singleLine = true,
                            cursorBrush = SolidColor(AccentTerracotta),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(category.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkBrown)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${category.todos.size} ${if (category.todos.size == 1) "todo" else "todos"}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(Modifier.width(8.dp))

                if (isEditing) {
                    IconActionButton(icon = Icons.Filled.Check, tint = StreakOrange) {
                        onRename(editText.ifBlank { category.name })
                        isEditing = false
                    }
                    Spacer(Modifier.width(6.dp))
                    IconActionButton(icon = Icons.Filled.Close, tint = TextMuted) {
                        editText = category.name
                        isEditing = false
                    }
                } else {
                    IconActionButton(icon = Icons.Filled.Edit, tint = TextMuted) { isEditing = true }
                    Spacer(Modifier.width(6.dp))
                    IconActionButton(icon = Icons.Filled.DeleteOutline, tint = AccentTerracotta, onClick = onDeleteRequest)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = chevronRotation }
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                    HorizontalDivider(color = BackgroundCream, thickness = 1.dp, modifier = Modifier.padding(bottom = 10.dp))
                    if (category.todos.isEmpty()) {
                        Text(
                            "No todos in this category yet.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            category.todos.forEach { todo -> MiniTodoRow(todo) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconActionButton(icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(BackgroundCream)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun MiniTodoRow(todo: HomeUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCream)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (todo.isDone) StreakOrange else SurfaceWhite)
                .border(1.dp, if (todo.isDone) StreakOrange else TextMuted.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (todo.isDone) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(10.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = todo.title,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = InkBrown,
            textDecoration = if (todo.isDone) TextDecoration.LineThrough else null
        )
        Text(todo.duedate.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 11.sp, color = TextMuted)
    }
}

// ----------------------------------------------------------------------------
// Delete confirmation
// ----------------------------------------------------------------------------
@Composable
private fun DeleteConfirmDialog(categoryName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Delete \"$categoryName\"?", fontWeight = FontWeight.Bold, color = InkBrown) },
        text = {
            Text(
                "Its todos won't be deleted, but they'll lose this category.",
                color = TextMuted,
                fontSize = 13.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = AccentTerracotta, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

// ----------------------------------------------------------------------------
// Empty state
// ----------------------------------------------------------------------------
@Composable
private fun EmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.SearchOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (query.isBlank()) "No categories yet" else "Nothing matches \"$query\"",
            color = TextMuted,
            fontSize = 14.sp
        )
    }
}

// ----------------------------------------------------------------------------
// Preview
// ----------------------------------------------------------------------------
@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun CategoryManageScreenPreview() {
    MaterialTheme {
        CategoryManageScreen()
    }
}