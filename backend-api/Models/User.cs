using Microsoft.AspNetCore.Identity;
namespace ToDoApp_BackEnd.Models
{
    public class User: IdentityUser
    {
        //public string? UserName { get; set; } trong identity framework co san 
        public string? AvartarUrl { get; set; }
        public DateTime CreateAt { get; set; }
        public ICollection<Category>?Categories { get; set; }    
        public ICollection<TodoItem>? TodoItems { get; set; }


    }
}
