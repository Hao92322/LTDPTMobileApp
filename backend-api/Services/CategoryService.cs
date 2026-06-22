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
        };
        //Get list
        public async Task<List<CategoryDTO>> GetListCategories(
        int page = 1,
        int pageSize = 20,
        string? search = null)
        {
            var query = _context.Categories.AsQueryable();

            // search theo tên, không phân biệt hoa thường
            if (!string.IsNullOrWhiteSpace(search))
                query = query.Where(e => e.Name.Contains(search));

            var items = await query
                .OrderBy(e => e.Name)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

            return items.Select(MapToDTO).ToList();
        }
        // post Create 
        public async Task<CategoryDTO> CreateCategory(CategoryRequestDTO model)
        {
            var entity = new Category { Name = model.Name };

            _context.Categories.Add(entity);
            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }
        public async Task<CategoryDTO> FindCategoryById(int id)
        {
            var entity = await _context.Categories.FindAsync(id)
                ?? throw new KeyNotFoundException($"Category with ID {id} not found.");

            return MapToDTO(entity);
        }

        // POST EDIT
        public async Task<CategoryDTO> EditCategory(CategoryRequestDTO model, int id)
        {
            var entity = await _context.Categories.FindAsync(id)
                ?? throw new KeyNotFoundException($"Category with ID {id} not found.");

            entity.Name = model.Name;
            await _context.SaveChangesAsync();

            return MapToDTO(entity);
        }
        // Post Detele
        public async Task<bool> DeleteCategory(int id)
        {
            var entity = await _context.Categories.FindAsync(id);
            if (entity == null) return false;
            _context.Categories.Remove(entity);
            await _context.SaveChangesAsync();
            return true;
        }

    }
}
