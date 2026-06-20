using Microsoft.AspNetCore.Mvc;
using ToDoApp_BackEnd.Services.Interface;

namespace ToDoApp_BackEnd.Controllers
{
    [Route("api/[controller]")] //  dòng này để truy cập vào /api/category
    public class CategoryController : BaseController
    {
        private readonly ICategoryService _service;
        //constructor
        public CategoryController(ICategoryService service) => _service = service;

        // xử lý xong return lại OkResponse , ErrorResponse t đã set up bên BaseController 
        [HttpGet]
        public IActionResult GetAll()
        {
            // Tạo dữ liệu giả (Hardcode)
            var data = new List<string> { "Học code", "Đá bóng", "Chơi game" };

            // Gọi hàm từ BaseController của ông
            return OkResponse(data, "Đây là danh sách test");
        }

    }
}
