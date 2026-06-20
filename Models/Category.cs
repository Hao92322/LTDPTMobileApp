using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ToDoApp_BackEnd.Models
{
    public class Category
    {
        public int Id { get; set; }
        [Required]
        [StringLength(50, MinimumLength = 3, ErrorMessage = "Tên danh mục phải từ 3-50 ký tự")]
        public string Name { get; set; }
        [Required]
        public string UserId { get; set; } // vi id trong idnetity user la stirng 
        [ForeignKey(nameof(UserId))]
        public User User { get; set; }
        // navigation
        public ICollection<TodoItem>? TodoItems { get; set; }
    }
}
