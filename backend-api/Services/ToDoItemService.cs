using Microsoft.EntityFrameworkCore;
using Microsoft.Identity.Client;
using ToDoApp_BackEnd.Data;
using ToDoApp_BackEnd.DTOs;
using ToDoApp_BackEnd.Models;
using ToDoApp_BackEnd.Services.Interface;
namespace ToDoApp_BackEnd.Services
{
    public class ToDoItemService : IToDoItemService
    {
        // code o day 
        // construcotr khoi tao
        private readonly ApplicationDbContext _context;
        public ToDoItemService(ApplicationDbContext context)
        {
            _context= context;
        }

        //get list 
        
        private static TodoItemDTO MapToDTO(TodoItem entity) => new TodoItemDTO
        {
            Id = entity.Id,
            CategoryId = entity.CategoryId,
            Title = entity.Title,
            Description = entity.Description,
            DueDate = entity.DueDate,
            IsCompleted = entity.IsCompleted,
            Priority = entity.Priority
        };
        public async Task<List<TodoItemDTO>> GetList(
        int page = 1,
        int pageSize = 20,
        int? categoryId = null,
        bool? isCompleted = null,
        int? priority = null)
        {
            var query = _context.TodoItems.AsQueryable();

            if (categoryId.HasValue)
                query = query.Where(m => m.CategoryId == categoryId.Value);

            if (isCompleted.HasValue)
                query = query.Where(m => m.IsCompleted == isCompleted.Value);

            if (priority.HasValue)
                query = query.Where(m => m.Priority == priority.Value);

            var items = await query
                .OrderByDescending(m => m.DueDate)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

            return items.Select(MapToDTO).ToList();
        }

        // create 
        public async Task<TodoItemDTO> CreateTodo(TodoRequestDTO model)
        {
            var entity = new TodoItem
            {
                Title = model.Title,
                Description = model.Description,
                CategoryId = model.CategoryId,
                DueDate = model.DueDate,
                Priority = model.Priority,
                IsCompleted = false
            };

            _context.TodoItems.Add(entity);
            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }
        // get data from id specific 
        public async Task<TodoItemDTO> FindToDoById(int id)
        {
            var entity = await _context.TodoItems.FindAsync(id)
                ?? throw new KeyNotFoundException($"Todo with ID {id} not found.");

            return MapToDTO(entity);
        }



        // edit todo 
        public async Task<TodoItemDTO> EditTodo(TodoRequestDTO model, int id)
        {
            var entity = await _context.TodoItems.FindAsync(id)
                ?? throw new KeyNotFoundException($"Todo with ID {id} not found.");

            entity.Title = model.Title;
            entity.Description = model.Description;
            entity.CategoryId = model.CategoryId;
            entity.DueDate = model.DueDate;
            entity.Priority = model.Priority;
            entity.IsCompleted = model.IsCompleted ?? false; // có thì map ko thì false ez  

            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }


        public async Task<TodoItemDTO> ToggleComplete(int id)
        {
            var entity = await _context.TodoItems.FindAsync(id)
                ?? throw new KeyNotFoundException($"Todo with ID {id} not found.");

            entity.IsCompleted = !entity.IsCompleted;
            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }


        public async Task<bool> DeleteToDoItem(int id)
        {
            var entity = await _context.TodoItems.FindAsync(id);
            if (entity == null) return false;
            _context.TodoItems.Remove(entity);
            await _context.SaveChangesAsync();
            return true;
        }
    }
}
