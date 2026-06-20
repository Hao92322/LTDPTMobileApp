namespace ToDoApp_BackEnd.Models
{
    public class ApiResponse<T>
    {
        public bool Success { get; set; } = true;
        public string Message { get; set; } = "Thành công";
        public T? Data { get; set; }
    }
}
