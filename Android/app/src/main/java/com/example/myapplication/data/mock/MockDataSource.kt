package com.example.todoapp.data.mock

import com.example.todoapp.models.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MockDataSource {

    const val CURRENT_USER_ID = "user-123-abc"

    private val categories = mutableListOf(
        Category(id = 1, name = "Học tập", userId = CURRENT_USER_ID),
        Category(id = 2, name = "Công việc", userId = CURRENT_USER_ID),
        Category(id = 3, name = "Cá nhân", userId = CURRENT_USER_ID)
    )

    private val todoItems = mutableListOf(
        TodoItem(
            id = 1,
            title = "Làm bài tập Android",
            description = "Code CRUD với Mock Data",
            isCompleted = false,
            priority = 1,
            createdAt = "2026-06-21T10:00:00",
            categoryId = 1
        ),
        TodoItem(
            id = 2,
            title = "Ôn thi CSDL",
            description = "Ôn tập Entity Framework",
            isCompleted = true,
            priority = 2,
            createdAt = "2026-06-20T08:00:00",
            categoryId = 1
        ),
        TodoItem(
            id = 3,
            title = "Họp nhóm",
            description = "Discuss về project ToDo",
            isCompleted = false,
            priority = 0,
            createdAt = "2026-06-19T14:30:00",
            categoryId = 2
        ),
        TodoItem(
            id = 4,
            title = "Đi gym",
            description = null,
            isCompleted = false,
            priority = 0,
            createdAt = "2026-06-18T07:00:00",
            categoryId = 3
        )
    )

    private var nextCategoryId = 4
    private var nextTodoId = 5

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private fun getCurrentDateTime(): String {
        return dateFormat.format(Date())
    }

    private suspend fun <T> simulateNetwork(call: suspend () -> T): T {
        delay(300L)
        return call()
    }

    suspend fun getCategories(): List<Category> = simulateNetwork {
        categories.filter { it.userId == CURRENT_USER_ID }
    }

    suspend fun createCategory(name: String, userId: String = CURRENT_USER_ID): Category = simulateNetwork {
        Category(nextCategoryId++, name, userId).also { categories.add(it) }
    }

    suspend fun updateCategory(id: Int, newName: String): Boolean = simulateNetwork {
        val index = categories.indexOfFirst { it.id == id }
        if (index != -1) {
            categories[index] = categories[index].copy(name = newName)
            true
        } else false
    }

    suspend fun deleteCategory(id: Int): Boolean = simulateNetwork {
        todoItems.removeAll { it.categoryId == id }
        categories.removeAll { it.id == id }
    }

    suspend fun getTodoByCategory(categoryId: Int): List<TodoItem> = simulateNetwork {
        todoItems.filter { it.categoryId == categoryId }
    }

    suspend fun createTodo(title: String, description: String?, categoryId: Int): TodoItem = simulateNetwork {
        TodoItem(
            id = nextTodoId++,
            title = title,
            description = description,
            isCompleted = false,
            dueDate = null,
            priority = 0,
            createdAt = getCurrentDateTime(),
            categoryId = categoryId
        ).also { todoItems.add(it) }
    }

    suspend fun updateTodo(id: Int, title: String, description: String?, isCompleted: Boolean): Boolean = simulateNetwork {
        val index = todoItems.indexOfFirst { it.id == id }
        if (index != -1) {
            todoItems[index] = todoItems[index].copy(
                title = title,
                description = description,
                isCompleted = isCompleted
            )
            true
        } else false
    }

    suspend fun toggleTodo(id: Int): Boolean = simulateNetwork {
        val index = todoItems.indexOfFirst { it.id == id }
        if (index != -1) {
            todoItems[index] = todoItems[index].copy(
                isCompleted = !todoItems[index].isCompleted
            )
            true
        } else false
    }

    suspend fun deleteTodo(id: Int): Boolean = simulateNetwork {
        todoItems.removeAll { it.id == id }
    }
}