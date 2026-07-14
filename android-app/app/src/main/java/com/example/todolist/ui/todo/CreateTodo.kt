package com.example.todolist.ui.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.api.Category
import com.example.todolist.ui.component.BaseSearchBar
import com.example.todolist.ui.LocalAppState
import com.example.todolist.ui.home.HomeViewModel
import com.example.todolist.ui.home.HomeUiState
import com.example.todolist.ui.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


private data class PriorityOption(val level: Int, val label: String, val color: Color)

private val priorityOptions = listOf(
    PriorityOption(0, "Không quan trọng", Color(0xFF7C9A6D)),
    PriorityOption(1, "Cảnh báo", Color(0xFFD9A441)),
    PriorityOption(2, "Khẩn cấp", Color(0xFFC1543F))
)

@Composable
fun CreateTodoScreen(
    onBack: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val appState = LocalAppState.current
    val isDark = appState.isDarkMode
    val lang = appState.language
    val bgColor = if (isDark) Color(0xFF1A120B) else BackgroundCream
    val cardColor = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown

    // ✅ Load danh mục thật từ API
    val categories by homeViewModel.categoryList.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        homeViewModel.loadCategories()
    }

    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var duedate by remember { mutableStateOf(LocalDateTime.now()) }
    var selectedCategory by remember { mutableStateOf<com.example.todolist.data.api.Category?>(null) }
    var selectedPriority by remember { mutableIntStateOf(0) }
    var titleError by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val categoriesFiltered = categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var titleNewCategory by remember { mutableStateOf("") }

    val labelNewCategory = if (lang == "vi") "Tên danh mục" else "Category name"
    val labelClose = if (lang == "vi") "Đóng" else "Close"
    val labelCreate = if (lang == "vi") "Tạo Danh Mục" else "Create Category"

    if (showAddCategoryDialog) {
        Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
            Card(colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = titleNewCategory,
                        onValueChange = { titleNewCategory = it },
                        label = { Text(labelNewCategory, color = textColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = cardColor,
                            unfocusedContainerColor = cardColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { showAddCategoryDialog = false }) { Text(labelClose) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (titleNewCategory.isNotBlank()) {
                                // ✅ Gọi API tạo danh mục mới
                                homeViewModel.createCategory(titleNewCategory)
                                titleNewCategory = ""
                            }
                            showAddCategoryDialog = false
                        }) { Text(labelCreate) }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = { AddTodoTopBar(onBack = onBack, isDark = isDark, lang = lang) },
        bottomBar = {
            SaveBar(
                onSave = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        titleError = false

                        // ✅ Gọi API tạo TODO với categoryId thật
                        val newTask = HomeUiState(
                            id = 0,
                            title = title,
                            subtitle = subtitle,
                            createdate = LocalDateTime.now(),
                            duedate = duedate,
                            priority = selectedPriority,
                            isDone = false,
                            category = selectedCategory?.name ?: "Default",
                            categoryId = selectedCategory?.id ?: (categories.firstOrNull()?.id ?: 0)
                        )
                        homeViewModel.addTodo(newTask)

                        onBack() // Quay lại màn hình Home
                    }
                },
                isDark = isDark,
                lang = lang
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
            val labelTitle = if (lang == "vi") "Tiêu đề" else "Title"
            val placeholderTitle = if (lang == "vi") "Ví dụ: Uống một cốc nước" else "e.g. Drink a glass of water"
            LabeledField(label = labelTitle, textColor = textColor) {
                RoundedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    placeholder = placeholderTitle,
                    isError = titleError,
                    cardColor = cardColor,
                    textColor = textColor
                )
            }

            val labelSubtitle = if (lang == "vi") "Mô tả / Ghi chú" else "Subtitle / Note"
            val placeholderSubtitle = if (lang == "vi") "Thêm ghi chú ngắn (tùy chọn)" else "Add a short note (optional)"
            LabeledField(label = labelSubtitle, textColor = textColor) {
                RoundedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    placeholder = placeholderSubtitle,
                    cardColor = cardColor,
                    textColor = textColor
                )
            }

            val labelDueDate = if (lang == "vi") "Hạn chót" else "Due Date"
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(labelDueDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(Modifier.height(10.dp))
                DateDueRow(
                    dateTime = duedate,
                    onDateTimeChange = { duedate = it },
                    cardColor = cardColor,
                    textColor = textColor,
                    lang = lang
                )
            }

            val labelPriority = if (lang == "vi") "Mức độ ưu tiên" else "Priority"
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(labelPriority, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(Modifier.height(10.dp))
                PrioritySelector(
                    selected = selectedPriority,
                    onSelect = { selectedPriority = it },
                    cardColor = cardColor,
                    textColor = textColor,
                    lang = lang
                )
            }

            // ✅ ĐÃ ĐỔI TÊN THÀNH CategorySearchBar ĐỂ TRÁNH XUNG ĐỘT VỚI MATERIAL3
            CategorySearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onClickAddCategory = { showAddCategoryDialog = true },
                cardColor = cardColor,
                lang = lang
            )

            val labelCategory = if (lang == "vi") "Danh mục" else "Category"
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(labelCategory, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoriesFiltered) { category ->
                        CategoryChip(
                            categoryName = category.name,
                            selected = category == selectedCategory,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            cardColor = cardColor,
                            textColor = textColor
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

// ✅ ĐÃ ĐỔI TÊN HÀM SEARCHBAR
@Composable
private fun CategorySearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClickAddCategory: () -> Unit,
    cardColor: Color = SurfaceWhite,
    lang: String = "vi"
) {
    val placeholder = if (lang == "vi") "Tìm kiếm danh mục ..." else "Search categories..."
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BaseSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
                .border(1.dp, InputBorder, RoundedCornerShape(16.dp)),
            placeholderText = placeholder
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AccentTerracotta, AccentTerracottaDeep)))
                .clickable { onClickAddCategory() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "addCategory", tint = SurfaceWhite)
        }
    }
}

@Composable
private fun AddTodoTopBar(onBack: () -> Unit, isDark: Boolean = false, lang: String = "vi") {
    val bg = if (isDark) Color(0xFF1A120B) else BackgroundCream
    val iconBg = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val textColor = if (isDark) Color(0xFFF5E6D3) else InkBrown
    val title = if (lang == "vi") "Công việc mới" else "New Todo"
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
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
private fun SaveBar(onSave: () -> Unit, isDark: Boolean = false, lang: String = "vi") {
    val barBg = if (isDark) Color(0xFF2C1F14) else SurfaceWhite
    val btnText = if (lang == "vi") "Tạo công việc" else "Create Todo"
    Surface(
        color = barBg,
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
                text = btnText,
                color = SurfaceWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    textColor: Color = InkBrown,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
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
    singleLine: Boolean = true,
    cardColor: Color = SurfaceWhite,
    textColor: Color = InkBrown
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
        textStyle = TextStyle(fontSize = 15.sp, color = textColor),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = cardColor,
            unfocusedContainerColor = cardColor,
            errorContainerColor = cardColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = AccentTerracotta
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDueRow(
    dateTime: LocalDateTime,
    onDateTimeChange: (LocalDateTime) -> Unit,
    cardColor: Color = SurfaceWhite,
    textColor: Color = InkBrown,
    lang: String = "vi"
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val cancelText = if (lang == "vi") "Hủy" else "Cancel"

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PickerField(
            icon = Icons.Filled.DateRange, text = dateTime.format(dateFormatter),
            modifier = Modifier.weight(1f), onClick = { showDatePicker = true },
            cardColor = cardColor, textColor = textColor
        )
        PickerField(
            icon = Icons.Filled.AccessTime, text = dateTime.format(timeFormatter),
            modifier = Modifier.weight(1f), onClick = { showTimePicker = true },
            cardColor = cardColor, textColor = textColor
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val newDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        onDateTimeChange(dateTime.toLocalTime().atDate(newDate))
                    }
                    showDatePicker = false
                }) { Text("OK", color = AccentTerracotta, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(cancelText, color = TextMuted) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = dateTime.hour, initialMinute = dateTime.minute, is24Hour = true
        )
        TimePickerDialogCustom(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                onDateTimeChange(dateTime.toLocalDate().atTime(newTime))
                showTimePicker = false
            },
            lang = lang
        ) { TimePicker(state = timePickerState) }
    }
}

@Composable
private fun PickerField(
    icon: ImageVector, text: String, modifier: Modifier = Modifier,
    onClick: () -> Unit, cardColor: Color = SurfaceWhite, textColor: Color = InkBrown
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, InputBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AccentTerracotta, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 15.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogCustom(
    onDismissRequest: () -> Unit, onConfirm: () -> Unit, lang: String = "vi", content: @Composable () -> Unit
) {
    val titleText = if (lang == "vi") "Chọn giờ" else "Select time"
    val cancelText = if (lang == "vi") "Hủy" else "Cancel"
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = RoundedCornerShape(24.dp), color = SurfaceWhite) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(titleText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkBrown, modifier = Modifier.padding(bottom = 12.dp))
                content()
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) { Text(cancelText, color = TextMuted) }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = onConfirm) { Text("OK", color = AccentTerracotta, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun PrioritySelector(
    selected: Int, onSelect: (Int) -> Unit, cardColor: Color = SurfaceWhite, textColor: Color = InkBrown, lang: String = "vi"
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        priorityOptions.forEach { option ->
            val isSelected = option.level == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f).heightIn(min = 90.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) option.color.copy(alpha = 0.16f) else cardColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) option.color else InputBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(option.level) }
                    .padding(vertical = 12.dp, horizontal = 6.dp)
            ) {
                val optionLabel = when (option.level) {
                    0 -> if (lang == "vi") "Không quan trọng" else "Low priority"
                    1 -> if (lang == "vi") "Cảnh báo" else "Medium priority"
                    else -> if (lang == "vi") "Khẩn cấp" else "High priority"
                }
                Icon(imageVector = Icons.Filled.Flag, contentDescription = optionLabel, tint = option.color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.height(6.dp))
                Text(
                    text = optionLabel, fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor, textAlign = TextAlign.Center, minLines = 2, maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    categoryName: String, selected: Boolean, onClick: () -> Unit, cardColor: Color = SurfaceWhite, textColor: Color = InkBrown
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(if (selected) AccentTerracotta.copy(alpha = 0.15f) else cardColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) AccentTerracotta else InputBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }.height(48.dp).padding(horizontal = 14.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Circle, contentDescription = null,
            tint = if (selected) AccentTerracotta else InputBorder, modifier = Modifier.size(8.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            categoryName, fontSize = 13.sp,
            color = if (selected) AccentTerracottaDeep else textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun AddTodoScreenPreview() {
    MaterialTheme {
        CreateTodoScreen()
    }
}