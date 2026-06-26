
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.Text;
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
            //var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
            //builder.Services.AddDbContext<ApplicationDbContext>(options =>
            //    options.UseSqlServer(connectionString));
            //cấu hình Mysql
            builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseMySql(
        builder.Configuration.GetConnectionString("DefaultConnection"),
        ServerVersion.AutoDetect(
            builder.Configuration.GetConnectionString("DefaultConnection"))));


            //Console.WriteLine("Chuỗi kết nối lấy được là: " + connectionString);

         
            //declare for Identity use whole project 
            builder.Services.AddIdentity<User, IdentityRole>(options =>
            {
                options.Password.RequireDigit = true;
                options.Password.RequiredLength = 6;
                options.Password.RequireNonAlphanumeric = false;
                options.Password.RequireUppercase = false;
            })
            .AddEntityFrameworkStores<ApplicationDbContext>()
            .AddDefaultTokenProviders();

            // JWT
            var jwtSettings = builder.Configuration.GetSection("JwtSettings");
            var secretKey = jwtSettings["SecretKey"]?? throw new Exception("Missing JwtSettings:SecretKey");

            builder.Services.AddAuthentication(options =>
            {
                options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme; // dung JWT de kiem tra danh tinh
                options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme; // neu ma doi api thi se ep no login 
            })
            .AddJwtBearer(options =>
            {
                options.TokenValidationParameters = new TokenValidationParameters
                {
                    ValidateIssuer = true, // kiem tra xen dung token  my server create ko
                    ValidateAudience = true, // kiem tra xe mdung app ko 
                    ValidateLifetime = true, // check vong doi cua cookie 
                    ValidateIssuerSigningKey = true, // check code token 
                    ValidIssuer = jwtSettings["Issuer"],
                    ValidAudience = jwtSettings["Audience"],
                    IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey))
                };
            });

            // dang ki pham vi service 

            builder.Services.AddScoped<ICategoryService, CategoryService>();
            builder.Services.AddScoped<IToDoItemService, ToDoItemService>();
            builder.Services.AddScoped<IAuthService, AuthService>();


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

            //app.UseHttpsRedirection(); // kiem tra dang nhap chua -> Create Oject 
            app.UseAuthentication();   // 🔴 PHẢI có
            app.UseAuthorization();// roi moi phan quyen-> wwho arre u 
            app.MapControllers();

            app.Run();
        }
    }
}
