using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;
using Microsoft.Extensions.Configuration;
using System.IO;

namespace ToDoApp_BackEnd.Data
{
    public class ApplicationDbContextFactory : IDesignTimeDbContextFactory<ApplicationDbContext>
    {
        public ApplicationDbContext CreateDbContext(string[] args)
        {
            // 1. Đọc trực tiếp appsettings.json tại thư mục gốc project
            var configuration = new ConfigurationBuilder()
                .SetBasePath(Directory.GetCurrentDirectory())
                .AddJsonFile("appsettings.json")
                .Build();

            // 2. Lấy connection string Aiven mới nhất
            var connectionString = configuration.GetConnectionString("DefaultConnection");

            // 3. Khai báo cứng MySQL version để tránh lỗi AutoDetect khi design-time
            var serverVersion = new MySqlServerVersion(new Version(8, 0, 36));

            var optionsBuilder = new DbContextOptionsBuilder<ApplicationDbContext>();
            optionsBuilder.UseMySql(connectionString, serverVersion);

            return new ApplicationDbContext(optionsBuilder.Options);
        }
    }
}