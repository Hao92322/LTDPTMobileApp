using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ToDoApp_BackEnd.DTOs;
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
            var data = await _CategoryService.GetListCategories(page, pageSize, search);
            return OkResponse(data);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            try
            {
                var data = await _CategoryService.FindCategoryById(id);
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
                var result = await _CategoryService.CreateCategory(model);
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
                var result = await _CategoryService.EditCategory(model, id);
                return OkResponse(result);
            }
            catch (KeyNotFoundException ex)
            {
                return ErrorResponse(ex.Message, 404);
            }
            catch (Exception ex)
            {
                return ErrorResponse(ex.Message, 500);
            }
        }


        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {


            var xoa = await _CategoryService.DeleteCategory(id);
            if (!xoa)
            {
                return ErrorResponse("NotFound category to delete", 404);
            }
            return OkResponse("Delete Sucess");
        }
    }
}
