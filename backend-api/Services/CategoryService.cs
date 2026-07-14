using Microsoft.EntityFrameworkCore;
using System.Runtime.InteropServices;
using ToDoApp_BackEnd.Data;
using ToDoApp_BackEnd.DTOs;
using ToDoApp_BackEnd.Models;
using ToDoApp_BackEnd.Services.Interface;

namespace ToDoApp_BackEnd.Services
{
    public class CategoryService: ICategoryService
    {
        // viet code o day
        private readonly ApplicationDbContext _context;
        public CategoryService(ApplicationDbContext context)
        {
            _context = context;
        }
        private static CategoryDTO MapToDTO(Category entity) => new CategoryDTO
        {
            Id = entity.Id,
            Name = entity.Name,
            TodoItems = entity.TodoItems?.Select(t => new TodoItemDTO
            {
                Id = t.Id,
                CategoryId = t.CategoryId,
                Title = t.Title,
                Description = t.Description,
                DueDate = t.DueDate,
                IsCompleted = t.IsCompleted,
                Priority = t.Priority
            }).ToList()
        };
        //Get list
        public async Task<List<CategoryDTO>> GetListCategories(
         string userId,
         int page = 1,
         int pageSize = 20,
         string? search = null)
        {
            var query = _context.Categories.Include(x=>x.TodoItems)
                .Where(x => x.UserId == userId);

            if (!string.IsNullOrWhiteSpace(search))
            {
                query = query.Where(x => x.Name.Contains(search) || 
                                         x.TodoItems!.Any(t => t.Title.Contains(search)));
            }

            var items = await query
                .OrderBy(x => x.Name)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

            return items.Select(MapToDTO).ToList();
        }
        // post Create 
        public async Task<CategoryDTO> CreateCategory(CategoryRequestDTO model,string userid)
        {
            
            var entity = new Category { Name = model.Name , UserId=userid};

            _context.Categories.Add(entity);
            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }
        public async Task<CategoryDTO> FindCategoryById(int id,string userid)
        {
            var entity = await _context.Categories.Include(x=>x.TodoItems).FirstOrDefaultAsync(x => x.Id == id && x.UserId == userid)
                ?? throw new KeyNotFoundException($"Category with ID {id} not found.");

            return MapToDTO(entity);
        }

        // POST EDIT
        public async Task<CategoryDTO> EditCategory(CategoryRequestDTO model, int id,string userid)
        {
            var entity = await _context.Categories.FirstOrDefaultAsync(x => x.Id == id && x.UserId == userid)
                ?? throw new KeyNotFoundException($"Category with ID {id} not found.");

            if (entity.Name == "Công việc chung")
            {
                throw new InvalidOperationException("Không thể chỉnh sửa danh mục mặc định.");
            }

            entity.Name = model.Name;
            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }
        // Post Detele
        public async Task<bool> DeleteCategory(int id,string userid)
        {
            var entity = await _context.Categories.FirstOrDefaultAsync(x => x.Id == id && x.UserId == userid);
            if (entity == null) return false;

            if (entity.Name == "Công việc chung")
            {
                throw new InvalidOperationException("Không thể xóa danh mục mặc định.");
            }

            _context.Categories.Remove(entity);
            await _context.SaveChangesAsync();
            return true;
        }

    }
}
