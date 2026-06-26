using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ToDoApp_BackEnd.Models
{
    public class TodoItem
    {
        public int Id { get; set; }
        // Tên công việc (Bắt buộc)
        [Required(ErrorMessage = "Tiêu đề không được để trống")]
        [StringLength(200, ErrorMessage = "Tiêu đề quá dài")]
        public string Title { get; set; }

        // Mô tả 
        [StringLength(500, ErrorMessage = "Mô tả không được vượt quá 500 ký tự")]
        public string? Description { get; set; }

        // Trạng thái hoàn thành
        public bool IsCompleted { get; set; } = false;

        // Thời hạn công việc (Deadline - Cực kỳ quan trọng)
        public DateTime? DueDate { get; set; }

        // Mức độ ưu tiên: 0-Thấp, 1-Trung bình, 2-Cao
        [Range(0, 2, ErrorMessage = "Độ ưu tiên phải từ 0 đến 2")]
        public int Priority { get; set; } = 0;

        // Thời điểm tạo (để sắp xếp theo ngày)
        public DateTime CreatedAt { get; set; } = DateTime.Now;
        [Required]
        public int? CategoryId { get; set; } // vì todo có thể ko cần todo vẫn tồn tại dựa trên user 
        [ForeignKey(nameof(CategoryId))]
        public Category? Category { get; set; }
        
        public string UserId { get; set; }
        [ForeignKey(nameof(UserId))]
        public User User { get; set; }
    }
}
