using Microsoft.AspNetCore.Mvc;
using ToDoApp_BackEnd.DTOs;
using ToDoApp_BackEnd.Services.Interface;

namespace ToDoApp_BackEnd.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : BaseController
    {
        private readonly IAuthService _authService;
        public AuthController(IAuthService authService)
        {
            _authService = authService;
        }
        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterDTO model)
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
                var token = await _authService.Register(model);
                return OkResponse(new {token});
            } catch (InvalidOperationException ex)
            {
                return ErrorResponse(ex.Message, 400);
            }
         }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody]  LoginDTO model) // frombody la model binding
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
                var token = await _authService.Login(model);
                return OkResponse(new { token });
            }
            catch (UnauthorizedAccessException ex)
            {
                return ErrorResponse(ex.Message, 401);
            }

        }
    }
}
