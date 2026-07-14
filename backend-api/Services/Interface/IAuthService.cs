
using ToDoApp_BackEnd.DTOs;
namespace ToDoApp_BackEnd.Services.Interface
{
    public interface IAuthService
    {
        Task<AuthResponseDTO> Register(RegisterDTO model);
        Task<AuthResponseDTO> Login(LoginDTO model);
        Task<AuthResponseDTO> RefreshToken(string refreshToken);
        Task RevokeRefreshToken(string refreshToken);
    }
}
