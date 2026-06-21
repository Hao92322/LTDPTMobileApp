package com.example.todoapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.repository.CategoryRepository
import com.example.todoapp.models.Category
import com.example.todoapp.models.Result
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    private val repository = CategoryRepository()

    private val _categories = MutableLiveData<Result<List<Category>>>()
    val categories: LiveData<Result<List<Category>>> = _categories

    private val _operationResult = MutableLiveData<Result<String>>()
    val operationResult: LiveData<Result<String>> = _operationResult

    init {
        loadCategories()
    }

    fun loadCategories() {
        _categories.value = Result.Loading
        viewModelScope.launch {
            _categories.value = repository.getCategories()
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) {
            _operationResult.value = Result.Error("Tên không được để trống")
            return
        }
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            when (val result = repository.createCategory(name)) {
                is Result.Success -> {
                    _operationResult.value = Result.Success("Đã thêm danh mục")
                    loadCategories()
                }
                is Result.Error -> _operationResult.value = result
                is Result.Loading -> {}
            }
        }
    }

    fun updateCategory(id: Int, newName: String) {
        if (newName.isBlank()) {
            _operationResult.value = Result.Error("Tên không được để trống")
            return
        }
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            when (val result = repository.updateCategory(id, newName)) {
                is Result.Success -> {
                    _operationResult.value = Result.Success("Đã cập nhật")
                    loadCategories()
                }
                is Result.Error -> _operationResult.value = result
                is Result.Loading -> {}
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            when (val result = repository.deleteCategory(id)) {
                is Result.Success -> {
                    _operationResult.value = Result.Success("Đã xóa")
                    loadCategories()
                }
                is Result.Error -> _operationResult.value = result
                is Result.Loading -> {}
            }
        }
    }
}