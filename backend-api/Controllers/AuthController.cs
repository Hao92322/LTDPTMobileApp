using Microsoft.AspNetCore.Authorization;
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
                var result = await _authService.Register(model);
                return OkResponse(result);
            }
            catch (InvalidOperationException ex)
            {
                return ErrorResponse(ex.Message, 400);
            }
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginDTO model)
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
                var result = await _authService.Login(model);
                return OkResponse(result);
            }
            catch (UnauthorizedAccessException ex)
            {
                return ErrorResponse(ex.Message, 401);
            }
        }

        // ✅ ENDPOINT MỚI: Dùng RefreshToken để lấy AccessToken mới
        [HttpPost("refresh-token")]
        public async Task<IActionResult> RefreshToken([FromBody] RefreshTokenRequestDTO model)
        {
            try
            {
                var result = await _authService.RefreshToken(model.RefreshToken);
                return OkResponse(result);
            }
            catch (UnauthorizedAccessException ex)
            {
                return ErrorResponse(ex.Message, 401);
            }
        }

        // ✅ ENDPOINT MỚI: Thu hồi RefreshToken khi logout
        [Authorize]
        [HttpPost("revoke-token")]
        public async Task<IActionResult> RevokeToken([FromBody] RefreshTokenRequestDTO model)
        {
            await _authService.RevokeRefreshToken(model.RefreshToken);
            return OkResponse("Token đã được thu hồi.");
        }
    }
}
