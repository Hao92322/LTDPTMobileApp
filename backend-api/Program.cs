
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using ToDoApp_BackEnd.Data;
using ToDoApp_BackEnd.Models;
using ToDoApp_BackEnd.Services;
using ToDoApp_BackEnd.Services.Interface;

namespace ToDoApp_BackEnd
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Add services to the container. 
            //cấu hình chạy test thử swager 
            builder.Services.AddControllers();
            builder.Services.AddEndpointsApiExplorer(); // Bắt buộc phải có để Swagger hiểu API
            builder.Services.AddSwaggerGen();
            builder.Services.AddCors(options =>
            {
                options.AddPolicy("AllowAll", builder =>
                    builder.AllowAnyOrigin()
                           .AllowAnyMethod()
                           .AllowAnyHeader());
            });

            // 1. Cấu hình kết nối SQL Server (đọc từ appsettings.json)
            var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
            builder.Services.AddDbContext<ApplicationDbContext>(options =>
                options.UseSqlServer(connectionString));
            Console.WriteLine("Chuỗi kết nối lấy được là: " + connectionString);

         
            // set identity pas 
           builder.Services.AddDefaultIdentity<User>(options =>
           {
               options.SignIn.RequireConfirmedAccount = false;
               // Tùy chỉnh độ phức tạp mật khẩu 
               options.Password.RequireDigit = true;
               options.Password.RequireNonAlphanumeric = false;
           })
           .AddEntityFrameworkStores<ApplicationDbContext>();

            // dang ki pham vi service 

            builder.Services.AddScoped<ICategoryService, CategoryService>();
            builder.Services.AddScoped<IToDoItemService, ToDoItemService>();

            
            // Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
            //builder.Services.AddOpenApi();
            var app = builder.Build();

            app.UseCors("AllowAll"); // Cho phép mọi yêu cầu từ bất cứ đâu trong môi trường dev
            // Configure the HTTP request pipeline.
            if (app.Environment.IsDevelopment())
            {
                //app.MapOpenApi(); //
                app.UseSwagger();
                app.UseSwaggerUI(); // Nó tự động map vào đường dẫn /swagger/index.html

            }

            app.UseHttpsRedirection();

            app.UseAuthorization();

            
            app.MapControllers();

            app.Run();
        }
    }
}
