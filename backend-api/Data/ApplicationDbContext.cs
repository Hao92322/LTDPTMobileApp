using ToDoApp_BackEnd.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;

namespace ToDoApp_BackEnd.Data
{
    public class ApplicationDbContext:IdentityDbContext<User>
    {
        // constructor
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> option) : base(option) { }
        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            if (!optionsBuilder.IsConfigured)
            {
                // chuỗi kết nối này giống hệt trong file appsettings.json 
                optionsBuilder.UseSqlServer("Server=.\\SQLEXPRESS;Database=TodoDb;Trusted_Connection=True;TrustServerCertificate=True;");
            }
        }
        //public DbSet<User> Users { get; set; } identity auto quan ly 
        public DbSet<Category> Categories { get; set; }
        public DbSet<TodoItem> TodoItems { get; set; }

    }
}
