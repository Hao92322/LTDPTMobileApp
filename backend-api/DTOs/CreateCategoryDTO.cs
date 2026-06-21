using System.ComponentModel.DataAnnotations;

namespace ToDoApp_BackEnd.DTOs
{
    public class CreateCategoryDTO
    {
        // hứng dữ liệu từ app xuống , check validation ở đây 
        [Required(ErrorMessage = "Tên danh mục không được để trống")]
        [StringLength(50, MinimumLength = 3, ErrorMessage = "Tên phải từ 3 đến 50 ký tự")]
        public string Name { get; set; }
    }
}
