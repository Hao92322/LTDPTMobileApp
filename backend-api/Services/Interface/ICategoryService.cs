using ToDoApp_BackEnd.DTOs;

namespace ToDoApp_BackEnd.Services.Interface
{
    public interface ICategoryService
    {
        // khai bao function da viet ben class service o day
        Task<List<CategoryDTO>> GetListCategories(
        string userId,
        int page = 1,
        int pageSize = 20,
        string? search = null);
        Task<CategoryDTO> CreateCategory(CategoryRequestDTO model, string userid);
        Task<CategoryDTO> FindCategoryById(int id, string userid);
        Task<CategoryDTO> EditCategory(CategoryRequestDTO model, int id,string userid);
        Task<bool> DeleteCategory(int id, string userid);
    }
}
