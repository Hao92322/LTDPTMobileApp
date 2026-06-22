
using ToDoApp_BackEnd.DTOs;
namespace ToDoApp_BackEnd.Services.Interface
{
    public interface IAuthService
    {
        Task<string> Register(RegisterDTO model);
        Task<string> Login(LoginDTO model);
    }
}
