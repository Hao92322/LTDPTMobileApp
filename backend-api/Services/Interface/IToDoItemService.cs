using ToDoApp_BackEnd.DTOs;

namespace ToDoApp_BackEnd.Services.Interface
{
    public interface IToDoItemService
    {
        // khai bao function da viet ben class service o day
        Task<List<TodoItemDTO>> GetList(
          string userId,
          int page = 1,
          int pageSize = 20,
          int? categoryId = null,
          bool? isCompleted = null,
          int? priority = null);
        Task<TodoItemDTO> CreateTodo(
                     TodoRequestDTO model,
                     string userId);
        Task<TodoItemDTO> FindToDoById(int id, string userId);
        Task<TodoItemDTO> EditTodo(
        TodoRequestDTO model,
        int id,
        string userId);
        Task<bool> DeleteToDoItem(int id, string userId);
        Task<TodoItemDTO> ToggleComplete(int id, string userId);


    }
}
