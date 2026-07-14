namespace ToDoApp_BackEnd.DTOs
{
    public class CategoryDTO
    {
        // hiển thị lên app 
        public int Id { get; set; }
        public string Name { get; set; }
        public List<TodoItemDTO>? TodoItems { get; set; }
    }
}
