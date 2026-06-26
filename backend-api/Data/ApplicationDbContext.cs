using ToDoApp_BackEnd.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;

namespace ToDoApp_BackEnd.Data
{
    public class ApplicationDbContext:IdentityDbContext<User>
    {
        // constructor
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> option) : base(option) { }
        
        //public DbSet<User> Users { get; set; } identity auto quan ly 
        public DbSet<Category> Categories { get; set; }
        public DbSet<TodoItem> TodoItems { get; set; }
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);  // use for idnetity auto configution 

            modelBuilder.Entity<TodoItem>().
                HasOne(t => t.User).
                WithMany(u => u.TodoItems).
                HasForeignKey(t => t.UserId).
                OnDelete(DeleteBehavior.Restrict);
            // cau hinh quan he cate va todo 
            modelBuilder.Entity<TodoItem>()
                .HasOne(t => t.Category).
                WithMany(c => c.TodoItems).
                HasForeignKey(t => t.CategoryId).
                OnDelete(DeleteBehavior.Cascade);

        }

    }
}
