using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using ToDoApp_BackEnd.DTOs;
using ToDoApp_BackEnd.Services;
using ToDoApp_BackEnd.Services.Interface;

namespace ToDoApp_BackEnd.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")] //  dòng này để truy cập vào /api/category
    public class CategoryController : BaseController
    {
        private readonly ICategoryService _CategoryService;
        //constructor
        public CategoryController(ICategoryService service) => _CategoryService = service;

        // xử lý xong return lại OkResponse , ErrorResponse t đã set up bên BaseController 
        // pagination 
        [HttpGet]
        public async Task<IActionResult> GetAll(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20,
        [FromQuery] string? search = null)
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (string.IsNullOrEmpty(userId)) return Unauthorized(); // Kiểm tra null
            var data = await _CategoryService.GetListCategories(userId,page, pageSize, search);
            return OkResponse(data);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            try
            {
                var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value; // lay id tu user
                if (string.IsNullOrEmpty(userId)) return Unauthorized(); // Kiểm tra null
                var data = await _CategoryService.FindCategoryById(id,userId);
                return OkResponse(data);
            }
            catch (KeyNotFoundException ex)
            {
                return ErrorResponse(ex.Message, 404);
            }
        }


        // create 
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CategoryRequestDTO model)
        {
            if (!ModelState.IsValid)
            {
                var errors = ModelState.Values
                    .SelectMany(v => v.Errors)
                    .Select(e => e.ErrorMessage)
                    .ToList();
                return ErrorResponse(string.Join(", ", errors), 400);
            }

            try
            {
                var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
                if (string.IsNullOrEmpty(userId)) return Unauthorized(); // Kiểm tra null

                var result = await _CategoryService.CreateCategory(model, userId);
                return OkResponse(result);
            }
            catch (Exception ex)
            {
                return ErrorResponse(ex.Message, 400);
            }
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> Edit(int id, [FromBody] CategoryRequestDTO model)
        {
            if (!ModelState.IsValid)
            {
                var errors = ModelState.Values
                    .SelectMany(v => v.Errors)
                    .Select(e => e.ErrorMessage)
                    .ToList();
                return ErrorResponse(string.Join(", ", errors), 400);
            }

            try
            {
                var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
                if (string.IsNullOrEmpty(userId)) return Unauthorized(); // Kiểm tra null
                var result = await _CategoryService.EditCategory(model, id,userId);
                return OkResponse(result);
            }
            catch (KeyNotFoundException ex)
            {
                return ErrorResponse(ex.Message, 404);
            }
            catch (InvalidOperationException ex)
            {
                return ErrorResponse(ex.Message, 400); // Trả lỗi 400 khi sửa danh mục mặc định
            }
            catch (Exception ex)
            {
                return ErrorResponse(ex.Message, 500);
            }
        }


        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            try
            {
                var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
                if (string.IsNullOrEmpty(userId)) return Unauthorized(); // Kiểm tra null
                var xoa = await _CategoryService.DeleteCategory(id,userId);
                if (!xoa)
                {
                    return ErrorResponse("NotFound category to delete", 404);
                }
                return OkResponse("Delete Sucess");
            }
            catch (InvalidOperationException ex)
            {
                return ErrorResponse(ex.Message, 400); // Trả lỗi 400 khi xóa danh mục mặc định
            }
        }
    }
}
