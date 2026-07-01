package com.example.todolist.ui.category
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.todolist.ui.component.SearchRowCategory
import com.example.todolist.ui.home.HomeUiState
import com.example.todolist.ui.LocalAppState
import com.example.todolist.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CategoryUi(
    val id: String,
    val name: String,
    val todos: List<HomeUiState>
)

@Composable
fun CategoryManageScreen(
    onBack: () -> Unit = {}
) {
    val appState = LocalAppState.current
    val isDark = appState.isDarkMode
    val bgColor = if (isDark) Color(0xFF1A120B) else BackgroundCream
    val cardColor = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown

    var categoriesState by remember { mutableStateOf(MockCategories) }
    var query by remember { mutableStateOf("") }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }
    var pendingDelete by remember { mutableStateOf<CategoryUi?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val filtered = remember(categoriesState, query) {
        if (query.isBlank()) categoriesState
        else categoriesState.filter { cat ->
            cat.name.contains(query, ignoreCase = true) ||
                    cat.todos.any { it.title.contains(query, ignoreCase = true) }
        }
    }

    Scaffold(containerColor = bgColor, topBar = { ManageTopBar(onBack, isDark) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            SearchRowCategory(
                query = query,
                onQueryChange = { query = it },
                onCreateClick = { showCreateDialog = true }
            )
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
                            onRename = { newName ->
                                categoriesState = categoriesState.map {
                                    if (it.id == category.id) it.copy(name = newName) else it
                                }
                            },
                            onDeleteRequest = { pendingDelete = category },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCategoryDialog(
            onConfirm = { name ->
                val newCategory = CategoryUi(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    todos = emptyList()
                )
                categoriesState = categoriesState + newCategory
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    pendingDelete?.let { target ->
        DeleteConfirmDialog(
            categoryName = target.name,
            onConfirm = {
                categoriesState = categoriesState.filter { it.id != target.id }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun ManageTopBar(onBack: () -> Unit, isDark: Boolean = false) {
    val bg = if (isDark) Color(0xFF1A120B) else BackgroundCream
    val iconBg = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor, modifier = Modifier.size(18.dp))
        }
        Text(
            text = "Manage Categories",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(Modifier.size(40.dp))
    }
}
@Composable
private fun CategoryCard(
    category: CategoryUi,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onRename: (String) -> Unit,
    onDeleteRequest: () -> Unit,
    isDark: Boolean = false
) {
    val cardColor = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val rowBg = if (isDark) Color(0xFF3D2B1F) else BackgroundCream
    val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(category.name) { mutableStateOf(category.name) }
    val chevronRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevron")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        shadowElevation = if (isDark) 0.dp else 1.dp,
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
                        .background(rowBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Addchart, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isEditing) {
                        BasicTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor),
                            singleLine = true,
                            cursorBrush = SolidColor(AccentTerracotta),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(category.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
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
                    IconActionButton(icon = Icons.Filled.Check, tint = StreakOrange, bg = rowBg) {
                        onRename(editText.ifBlank { category.name })
                        isEditing = false
                    }
                    Spacer(Modifier.width(6.dp))
                    IconActionButton(icon = Icons.Filled.Close, tint = TextMuted, bg = rowBg) {
                        editText = category.name
                        isEditing = false
                    }
                } else {
                    IconActionButton(icon = Icons.Filled.Edit, tint = TextMuted, bg = rowBg) { isEditing = true }
                    Spacer(Modifier.width(6.dp))
                    IconActionButton(icon = Icons.Filled.DeleteOutline, tint = AccentTerracotta, bg = rowBg, onClick = onDeleteRequest)
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
                    HorizontalDivider(color = rowBg, thickness = 1.dp, modifier = Modifier.padding(bottom = 10.dp))
                    if (category.todos.isEmpty()) {
                        Text(
                            "No todos in this category yet.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            category.todos.forEach { todo -> MiniTodoRow(todo, rowBg, textColor) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconActionButton(
    icon: ImageVector,
    tint: Color,
    bg: Color = BackgroundCream,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun MiniTodoRow(
    todo: HomeUiState,
    rowBg: Color = BackgroundCream,
    textColor: Color = InkBrown
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBg)
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
            color = textColor,
            textDecoration = if (todo.isDone) TextDecoration.LineThrough else null
        )
        Text(todo.duedate.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 11.sp, color = TextMuted)
    }
}

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

@Composable
private fun CreateCategoryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(20.dp),
        title = { Text("New Category", fontWeight = FontWeight.Bold, color = InkBrown) },
        text = {
            Column {
                Text("Enter category name:", color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Shopping", color = TextMuted) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundCream,
                        unfocusedContainerColor = BackgroundCream,
                        focusedIndicatorColor = AccentTerracotta,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (categoryName.isNotBlank()) onConfirm(categoryName) },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Create", color = AccentTerracotta, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

private val MockCategories = listOf(
    CategoryUi("1", "Work", listOf(
        HomeUiState(0, "Meeting with team", "Discuss project progress", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 2, false, "Work"),
        HomeUiState(0, "Email clients", "Send updates", LocalDateTime.now(), LocalDateTime.now().plusHours(2), 1, true, "Work")
    )),
    CategoryUi("2", "Personal", listOf(
        HomeUiState(0, "Buy groceries", "At the supermarket", LocalDateTime.now(), LocalDateTime.now().plusHours(3), 0, false, "Personal")
    )),
    CategoryUi("3", "Fitness", emptyList())
)

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun CategoryManageScreenPreview() {
    MaterialTheme {
        CategoryManageScreen()
    }
}