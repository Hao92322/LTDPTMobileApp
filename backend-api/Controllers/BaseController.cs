using ToDoApp_BackEnd.Models;
using Microsoft.AspNetCore.Mvc;

namespace ToDoApp_BackEnd.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public abstract class BaseController : ControllerBase
    {
        // protected only child can inherit 


        // trả về gói json thànhcoong
       protected IActionResult OkResponse<T>(T data ,string message="Thành công")
       {
            return Ok(new ApiResponse<T> {Success=true,Message=message,Data=data });
       }    

        // json thất bại 
        protected IActionResult ErrorResponse(string message,int StatusCode=400)
        {
            return BadRequest(new ApiResponse<string> {Success=false,Message=message,Data=null});
        }
    }
}
