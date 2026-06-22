using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore.Metadata.Conventions;
using Microsoft.Extensions.Validation;
using ToDoApp_BackEnd.DTOs;
using ToDoApp_BackEnd.Services.Interface;

namespace ToDoApp_BackEnd.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")] //  này để truy cập vào /api/ToDoItem
    
    public class ToDoItemController : BaseController
    {
        private readonly IToDoItemService _ToDoItemService;
        //constructor
        public ToDoItemController(IToDoItemService ToDoItemService) => _ToDoItemService = ToDoItemService;

        // xử lý xong return lại json  OkResponse , ErrorResponse t đã set up bên BaseController
        [HttpGet]
        public async Task<IActionResult> GetList(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20,
        [FromQuery] int? categoryId = null,
        [FromQuery] bool? isCompleted = null,
        [FromQuery] int? priority = null)
        {
            var result = await _ToDoItemService.GetList(page, pageSize, categoryId, isCompleted, priority);
            return OkResponse(result);
        }
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] TodoRequestDTO model)
        {
            if (!ModelState.IsValid)
            {
                var errors = ModelState.Values
                    .SelectMany(v => v.Errors)
                    .Select(e => e.ErrorMessage)
                    .ToList();
                return ErrorResponse(string.Join(", ", errors), 400);
            }

            // service không bao giờ return null, nó throw exception
            // nên bỏ check null đi
            try
            {
                var result = await _ToDoItemService.CreateTodo(model);
                return OkResponse(result);
            }
            catch (Exception ex)
            {
                return ErrorResponse(ex.Message, 400);
            }
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetDetails(int id)
        {
            try
            {
                var result = await _ToDoItemService.FindToDoById(id);
                return OkResponse(result);
            }
            catch (KeyNotFoundException ex)
            {
                return ErrorResponse(ex.Message, 404);
            }
        }


        [HttpPatch("{id}/toggle")]
        public async Task<IActionResult> Toggle(int id)
        {
            try
            {
                var result = await _ToDoItemService.ToggleComplete(id);
                return OkResponse(result);
            }
            catch (KeyNotFoundException ex)
            {
                return ErrorResponse(ex.Message, 404);
            }
        }


        [HttpPut("{id}")]
        public async Task<IActionResult> Edit(TodoRequestDTO model,int id)
        {
            if (!ModelState.IsValid)
            {
                var errors = ModelState.Values.SelectMany(v => v.Errors).Select(e => e.ErrorMessage).ToList();// get error from model
                return ErrorResponse(string.Join(", ", errors), 400);// tra ve danh sach loi cua tung proverty
            }
            var tao = await _ToDoItemService.EditTodo(model, id);
            if (tao == null)
            {
                return ErrorResponse("Cannot edit ", 400);
            }
            return OkResponse(tao);
        }
        // load du lieu cho details 



        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var deleted = await _ToDoItemService.DeleteToDoItem(id);
            if (!deleted) return ErrorResponse("Cannot delete this", 400);
            return OkResponse(deleted, "Delete Success");
        }
    }
}
