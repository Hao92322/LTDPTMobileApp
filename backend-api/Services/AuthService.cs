using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using ToDoApp_BackEnd.Data;
using ToDoApp_BackEnd.DTOs;
using ToDoApp_BackEnd.Models;
using ToDoApp_BackEnd.Services.Interface;

namespace ToDoApp_BackEnd.Services
{
    public class AuthService : IAuthService
    {
        private readonly UserManager<User> _userManager;
        private readonly IConfiguration _config;
        private readonly ApplicationDbContext _context;

        public AuthService(UserManager<User> userManager, IConfiguration config, ApplicationDbContext context)
        {
            _userManager = userManager;
            _config = config;
            _context = context;
        }

        // ===== TẠO ACCESS TOKEN (JWT ngắn hạn) =====
        private string GenerateJwtToken(User user)
        {
            var jwtSettings = _config.GetSection("JwtSettings");
            var secretKey = jwtSettings["SecretKey"]!;
            var expiryMinutes = int.Parse(jwtSettings["ExpiryMinutes"]!);

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id),
                new Claim(ClaimTypes.Email, user.Email!),
                new Claim(ClaimTypes.Name, user.UserName!)
            };

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
                issuer: jwtSettings["Issuer"],
                audience: jwtSettings["Audience"],
                claims: claims,
                expires: DateTime.UtcNow.AddMinutes(expiryMinutes),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }

        // ===== TẠO REFRESH TOKEN (chuỗi ngẫu nhiên dài hạn) =====
        private async Task<string> GenerateRefreshToken(User user)
        {
            var jwtSettings = _config.GetSection("JwtSettings");
            var refreshExpiryDays = int.Parse(jwtSettings["RefreshTokenExpiryDays"] ?? "7");

            // Tạo chuỗi token ngẫu nhiên 64 byte
            var randomBytes = new byte[64];
            using var rng = RandomNumberGenerator.Create();
            rng.GetBytes(randomBytes);
            var tokenString = Convert.ToBase64String(randomBytes);

            // Xóa các refresh token cũ đã hết hạn hoặc bị thu hồi của user này
            var oldTokens = await _context.RefreshTokens
                .Where(rt => rt.UserId == user.Id && (rt.IsRevoked || rt.ExpiresAt <= DateTime.UtcNow))
                .ToListAsync();
            _context.RefreshTokens.RemoveRange(oldTokens);

            // Lưu refresh token mới vào database
            var refreshToken = new RefreshToken
            {
                Token = tokenString,
                UserId = user.Id,
                CreatedAt = DateTime.UtcNow,
                ExpiresAt = DateTime.UtcNow.AddDays(refreshExpiryDays),
                IsRevoked = false
            };

            _context.RefreshTokens.Add(refreshToken);
            await _context.SaveChangesAsync();

            return tokenString;
        }

        // ===== TẠO RESPONSE DTO (gồm cả 2 token) =====
        private async Task<AuthResponseDTO> GenerateAuthResponse(User user)
        {
            var accessToken = GenerateJwtToken(user);
            var refreshToken = await GenerateRefreshToken(user);

            return new AuthResponseDTO
            {
                AccessToken = accessToken,
                RefreshToken = refreshToken
            };
        }

        // ===== ĐĂNG KÝ =====
        public async Task<AuthResponseDTO> Register(RegisterDTO model)
        {
            var existingUser = await _userManager.FindByEmailAsync(model.Email);
            if (existingUser != null)
                throw new InvalidOperationException("Email already exists.");

            var user = new User
            {
                Email = model.Email,
                UserName = model.UserName
            };

            var result = await _userManager.CreateAsync(user, model.Password);
            if (!result.Succeeded)
            {
                var errors = string.Join(", ", result.Errors.Select(e => e.Description));
                throw new InvalidOperationException(errors);
            }

            return await GenerateAuthResponse(user);
        }

        // ===== ĐĂNG NHẬP =====
        public async Task<AuthResponseDTO> Login(LoginDTO model)
        {
            // Tìm theo email trước, nếu không thấy thì tìm theo username
            var user = await _userManager.FindByEmailAsync(model.Email)
                ?? await _userManager.FindByNameAsync(model.Email)
                ?? throw new UnauthorizedAccessException("Invalid email or password.");

            var isValidPassword = await _userManager.CheckPasswordAsync(user, model.Password);
            if (!isValidPassword)
                throw new UnauthorizedAccessException("Invalid email or password.");

            return await GenerateAuthResponse(user);
        }

        // ===== LÀM MỚI TOKEN (dùng RefreshToken cũ để lấy AccessToken mới) =====
        public async Task<AuthResponseDTO> RefreshToken(string refreshToken)
        {
            var storedToken = await _context.RefreshTokens
                .Include(rt => rt.User)
                .FirstOrDefaultAsync(rt => rt.Token == refreshToken && !rt.IsRevoked && rt.ExpiresAt > DateTime.UtcNow)
                ?? throw new UnauthorizedAccessException("Refresh token không hợp lệ hoặc đã hết hạn.");

            // Thu hồi token cũ (mỗi refresh token chỉ dùng 1 lần)
            storedToken.IsRevoked = true;
            await _context.SaveChangesAsync();

            // Tạo cặp token mới
            return await GenerateAuthResponse(storedToken.User);
        }

        // ===== THU HỒI REFRESH TOKEN (dùng khi logout) =====
        public async Task RevokeRefreshToken(string refreshToken)
        {
            var storedToken = await _context.RefreshTokens
                .FirstOrDefaultAsync(rt => rt.Token == refreshToken);

            if (storedToken != null)
            {
                storedToken.IsRevoked = true;
                await _context.SaveChangesAsync();
            }
        }
    }
}
