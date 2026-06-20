using Microsoft.AspNetCore.Mvc;
using ToDoApp_BackEnd.Services.Interface;

namespace ToDoApp_BackEnd.Controllers
{
    [Route("api/[controller]")] //  này để truy cập vào /api/ToDoItem
    public class ToDoItemController : BaseController
    {
        private readonly IToDoItemService _ToDoItemService;
        //constructor
        public ToDoItemController(IToDoItemService ToDoItemService) => _ToDoItemService = ToDoItemService;

         // xử lý xong return lại json  OkResponse , ErrorResponse t đã set up bên BaseController
    }
}
