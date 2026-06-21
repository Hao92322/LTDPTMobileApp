using System.ComponentModel.DataAnnotations;

namespace ToDoApp_BackEnd.DTOs
{
    public class CreateTodoDTO
    {
        //  dữ liệu đc nhận, check validation ở đây
        [Required(ErrorMessage = "Tiêu đề là bắt buộc")]
        [StringLength(200, ErrorMessage = "Tiêu đề không quá 200 ký tự")]
        public string Title { get; set; }
        [StringLength(500, ErrorMessage = "Mô tả không quá 500 ký tự")]
        public string? Description { get; set; }
        public DateTime? DueDate { get; set; }
        [Range(0, 2, ErrorMessage = "Độ ưu tiên phải nằm trong khoảng 0-2")]
        public int Priority { get; set; } = 0;
        [Required(ErrorMessage = "Phải chọn danh mục cho công việc này")]
        public int CategoryId { get; set; }
    }
}
