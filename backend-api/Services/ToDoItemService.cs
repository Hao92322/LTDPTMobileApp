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
            _context = context;
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
         string userId,
         int page = 1,
         int pageSize = 20,
         int? categoryId = null,
         bool? isCompleted = null,
         int? priority = null)
        {
            var query = _context.TodoItems
                .Include(x => x.Category)
                .Where(x=> x.UserId==userId);

            if (categoryId.HasValue)
                query = query.Where(x => x.CategoryId == categoryId.Value);

            if (isCompleted.HasValue)
                query = query.Where(x => x.IsCompleted == isCompleted.Value);

            if (priority.HasValue)
                query = query.Where(x => x.Priority == priority.Value);

            var items = await query
                .OrderByDescending(x => x.DueDate)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

            return items.Select(MapToDTO).ToList();
        }

        // create 
        public async Task<TodoItemDTO> CreateTodo(
                     TodoRequestDTO model,
                     string userId)
        {
            var category = await _context.Categories
                .FirstOrDefaultAsync(x =>
                    x.Id == model.CategoryId &&
                    x.UserId == userId);

            if (category == null)
                throw new KeyNotFoundException("Category not found.");
            // Bỏ validation ngày trong quá khứ - múi giờ khác nhau gây lỗi


            var entity = new TodoItem
            {
                Title = model.Title,
                Description = model.Description,
                CategoryId = model.CategoryId,
                DueDate = model.DueDate ?? model.Date.Date.AddHours(23).AddMinutes(59),
                Priority = model.Priority,
                CreatedAt = DateTime.UtcNow,
                IsCompleted = false,
                UserId = userId  // ✅ Gán UserId để filter đúng user
            };

            _context.TodoItems.Add(entity);
            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }
        // get data from id specific 
        public async Task<TodoItemDTO> FindToDoById(int id, string userId)
        {
            var entity = await _context.TodoItems
                .Include(x => x.Category)
                .FirstOrDefaultAsync(x =>
                    x.Id == id &&
                    x.Category!.UserId == userId || x.UserId==userId )
                ?? throw new KeyNotFoundException($"Todo with ID {id} not found.");

            return MapToDTO(entity);
        }



        // edit todo 
        public async Task<TodoItemDTO> EditTodo(
        TodoRequestDTO model,
        int id,
        string userId)
        {
            var entity = await _context.TodoItems
                .Include(x => x.Category)
                .FirstOrDefaultAsync(x =>
                    x.Id == id &&
                    x.Category!.UserId == userId )
                ?? throw new KeyNotFoundException($"Todo with ID {id} not found.");

            var category = await _context.Categories
                .FirstOrDefaultAsync(x =>
                    x.Id == model.CategoryId &&
                    x.UserId == userId)
                ?? throw new KeyNotFoundException("Category not found.");

            entity.Title = model.Title;
            entity.Description = model.Description;
            entity.CategoryId = model.CategoryId;
            entity.DueDate = model.DueDate;
            entity.Priority = model.Priority;
            entity.IsCompleted = model.IsCompleted ?? false;

            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }


        public async Task<TodoItemDTO> ToggleComplete(int id, string userId)
        {
            var entity = await _context.TodoItems
                .Include(x => x.Category)
                .FirstOrDefaultAsync(x =>
                    x.Id == id &&
                    x.Category!.UserId == userId || x.UserId == userId)
                ?? throw new KeyNotFoundException($"Todo with ID {id} not found.");

            entity.IsCompleted = !entity.IsCompleted;

            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }


        public async Task<bool> DeleteToDoItem(int id, string userId)
        {
            var entity = await _context.TodoItems
                .Include(x => x.Category)
                .FirstOrDefaultAsync(x =>
                    x.Id == id &&
                    x.Category!.UserId == userId || x.UserId==userId);

            if (entity == null)
                return false;

            _context.TodoItems.Remove(entity);

            await _context.SaveChangesAsync();

            return true;
        }
    }
}
