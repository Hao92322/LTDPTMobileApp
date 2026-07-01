package com.example.todolist.ui.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.todolist.ui.component.BaseSearchBar
import com.example.todolist.ui.theme.*
import com.example.todolist.ui.home.HomeUiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class TodoCategory(val id : Int,val label: String)


// Mức độ ưu tiên: 0 = không quan trọng, 1 = cảnh báo, 2 = khẩn cấp
private data class PriorityOption(val level: Int, val label: String, val color: Color)

private val priorityOptions = listOf(
    PriorityOption(0, "Không quan trọng", Color(0xFF7C9A6D)), // cờ xanh
    PriorityOption(1, "Cảnh báo", Color(0xFFD9A441)),          // cờ vàng
    PriorityOption(2, "Khẩn cấp", Color(0xFFC1543F))           // cờ đỏ
)

// Screen
@Composable
fun CreateTodoScreen(
    onBack: () -> Unit = {},
    onSave: (HomeUiState) -> Unit = {},
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    var categories = remember { mutableStateListOf(
        TodoCategory(1,"Hydration"),
        TodoCategory(2,"Hydration78")
    )}
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var duedate by remember { mutableStateOf(LocalDateTime.now()) }
    var selectedCategory by remember { mutableStateOf<TodoCategory?>(null) }
    var selectedPriority by remember { mutableStateOf(0) }
    var titleError by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var categoriesFiltered = categories.filter { it.label.contains(searchQuery, ignoreCase = true) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var TitleNewCategory by remember { mutableStateOf("") }
    if(showAddCategoryDialog){
        Dialog(
            onDismissRequest = { showAddCategoryDialog = false }
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    TextField(
                        value = TitleNewCategory,
                        onValueChange = {TitleNewCategory = it},
                        label = { Text("Tên danh mục") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                Row (modifier = Modifier.fillMaxWidth(),Arrangement.End){
                    Button(
                        onClick = {
                            showAddCategoryDialog = false
                        }
                        ) {
                            Text("Đóng")
                        }
                    Button(
                        onClick = {
                            categories.add(TodoCategory(categories.size+1,TitleNewCategory))
                            showAddCategoryDialog = false
                        }
                        ) {
                            Text("Tạo Danh Mục")
                        }
                    }
                }
            }
        }
    }
    Scaffold(
        containerColor = BackgroundCream,
        topBar = { AddTodoTopBar(onBack = onBack) },
        bottomBar = {
            SaveBar(
                onSave = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        titleError = false
                        onSave(
                            HomeUiState(
                                title = title,
                                subtitle = subtitle,
                                createdate = LocalDateTime.now(),
                                duedate = duedate,
                                priority = selectedPriority,
                                isDone = false
                            )
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            LabeledField(label = "Title") {
                RoundedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    placeholder = "e.g. Drink a glass of water",
                    isError = titleError
                )
            }

            LabeledField(label = "Subtitle") {
                RoundedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    placeholder = "Add a short note (optional)"
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Due Date", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBrown)
                Spacer(Modifier.height(10.dp))
                DateDueRow(
                    dateTime = duedate,
                    onDateTimeChange = { duedate = it }
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Priority", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBrown)
                Spacer(Modifier.height(10.dp))
                PrioritySelector(
                    selected = selectedPriority,
                    onSelect = { selectedPriority = it }
                )
            }
            SearchBar(searchQuery,
                onSearchQueryChange = {
                searchQuery = it},
                onClickAddCategory = {
                    showAddCategoryDialog = true
                })
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBrown)
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoriesFiltered) { category ->
                        CategoryChip(
                            category = category,
                            selected = category == selectedCategory,
                            onClick = {
                                if(selectedCategory == category)
                                    selectedCategory = null
                                else
                                    selectedCategory = category
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClickAddCategory: () -> Unit
) {
    Row (modifier = Modifier.fillMaxWidth().height(55.dp)){
        Column {
            BaseSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceWhite),
                placeholderText = "Tìm kiếm danh mục ..."
            )
        }
        Box (
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {onClickAddCategory()},
            contentAlignment = Alignment.Center
        )
        {
            Icon(Icons.Default.Add, contentDescription = "addCategory", tint = InkBrown)
        }
    }
}

// Top bar
@Composable
private fun AddTodoTopBar(onBack: () -> Unit) {
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = InkBrown,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = "New Todo",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkBrown
        )
        Spacer(Modifier.size(40.dp))
    }
}

// Fixed bottom save bar
@Composable
private fun SaveBar(onSave: () -> Unit) {
    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(AccentTerracotta, AccentTerracottaDeep)))
                .clickable { onSave() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Create Todo",
                color = SurfaceWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// Form pieces
@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBrown)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun RoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    singleLine: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isError) MaterialTheme.colorScheme.error else InputBorder, RoundedCornerShape(16.dp)),
        placeholder = { Text(placeholder, color = TextMuted, fontSize = 15.sp) },
        singleLine = singleLine,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        textStyle = TextStyle(fontSize = 15.sp, color = InkBrown),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite,
            errorContainerColor = SurfaceWhite,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = AccentTerracotta
        )
    )
}

// Due date + due time, side by side trên cùng 1 dòng, dùng chung 1 ô RoundedTextField-style
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PickerField(
            icon = Icons.Filled.DateRange,
            text = dateTime.format(dateFormatter),
            modifier = Modifier.weight(1f),
            onClick = { showDatePicker = true }
        )
        PickerField(
            icon = Icons.Filled.AccessTime,
            text = dateTime.format(timeFormatter),
            modifier = Modifier.weight(1f),
            onClick = { showTimePicker = true }
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
                }) { Text("OK", color = AccentTerracotta, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy", color = TextMuted) }
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

// Ô bấm để chọn ngày/giờ, đồng bộ style với RoundedTextField cho hợp tông
@Composable
private fun PickerField(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(1.dp, InputBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentTerracotta,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 15.sp, color = InkBrown, fontWeight = FontWeight.Medium)
    }
}

// Dialog chọn giờ dạng Material3 TimePicker, bọc theme cho đồng bộ với app
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
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Chọn giờ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBrown,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                content()
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Hủy", color = TextMuted)
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = onConfirm) {
                        Text("OK", color = AccentTerracotta, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Bộ chọn mức độ ưu tiên: 0 = Không quan trọng (xanh), 1 = Cảnh báo (vàng), 2 = Khẩn cấp (đỏ)
@Composable
private fun PrioritySelector(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        priorityOptions.forEach { option ->
            val isSelected = option.level == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) option.color.copy(alpha = 0.16f) else SurfaceWhite)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) option.color else InputBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(option.level) }
                    .padding(vertical = 12.dp, horizontal = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = option.label,
                    tint = option.color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = option.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = InkBrown,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(category: TodoCategory, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if(selected) AccentTerracotta.copy(alpha = 0.7f) else SurfaceWhite)
            .border(
                width = 1.5.dp,
                color = Color.Black,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable{ onClick() }
            .height(40.dp)
    ) {
        Spacer(Modifier.width(8.dp))
        Text(category.label, fontSize = 13.sp, color = if(selected) Color.White else InkBrown, fontWeight = FontWeight.Bold)
    }
}
@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun AddTodoScreenPreview() {
    MaterialTheme {
        CreateTodoScreen()
    }
}