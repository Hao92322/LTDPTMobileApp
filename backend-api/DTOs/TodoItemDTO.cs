namespace ToDoApp_BackEnd.DTOs
{
    public class TodoItemDTO
    {
        // dữ liệu gửi lên app 
        public int Id { get; set; }
        public string Title { get; set; }
        public string? Description { get; set; }
        public bool IsCompleted { get; set; }
        public DateTime? DueDate { get; set; }
        public int Priority { get; set; }
        public int CategoryId { get; set; }
    }
}
