package com.example.todolist.ui.todo

import com.example.todolist.data.repository.MockTodoRepositoryImpl
import com.example.todolist.data.repository.TodoRepository

class CreateTodoViewModel(private val repository : TodoRepository = MockTodoRepositoryImpl()) {

}