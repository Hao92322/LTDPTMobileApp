namespace ToDoApp_BackEnd.DTOs
{
    public class AuthResponseDTO
    {
        // Trả về cho app cả 2 token
        public string AccessToken { get; set; } = string.Empty;
        public string RefreshToken { get; set; } = string.Empty;
    }

    public class RefreshTokenRequestDTO
    {
        // App gửi lên để xin AccessToken mới
        public string RefreshToken { get; set; } = string.Empty;
    }
}
