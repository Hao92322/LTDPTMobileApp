using System.ComponentModel.DataAnnotations;

namespace ToDoApp_BackEnd.DTOs
{
    public class RegisterDTO
    {
        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [Required]
        [MinLength(6)]
        public string Password { get; set; } = string.Empty;

        [Required]
        public string UserName { get; set; } = string.Empty;
        

        [Required]
        [Compare("Password", ErrorMessage = "Mật khẩu không khớp")]
        public string ConfirmPassword { get; set; } = string.Empty;// khong save db vi nhan request tu db -> sang so sanh thoi 
    }
    public class LoginDTO
    {
        [Required]
        public string Email { get; set; } = string.Empty; // Nhận Email hoặc Username

        [Required]
        public string Password { get; set; } = string.Empty;
    }
}
