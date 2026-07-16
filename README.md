# 📝 Advanced Todo & Reminders App (Android & C# WebAPI)

Một ứng dụng quản lý công việc và nhắc nhở thời gian thực (Todo & Reminders) cao cấp, được phát triển với hiệu ứng chuyển động lò xo mượt mà, cấu trúc máy chủ động và tính năng báo động thông minh vượt qua các giới hạn chạy ngầm trên Android.

---

## 🚀 Tính năng nổi bật

### 🎨 UI/UX & Micro-interactions
*   **Hiệu ứng nhấn co nẩy lò xo (Spring Bounce Click):** Từng cú chạm vào nút bấm, tab điều hướng, checkmark hay danh mục đều cho phản hồi lún/nẩy đàn hồi cực kỳ sinh động.
*   **Thẻ việc làm phát sáng (Urgency Glow Cards):** Thiết kế thẻ dựa trên 3 mức độ khẩn cấp (Không quan trọng, Cảnh báo, Khẩn cấp) với màu nền pastel dịu nhẹ và viền phát sáng mờ.
*   **Thanh chỉ thị Gradient:** Các thẻ được thiết kế thanh pill chỉ thị màu sắc gradient dọc sang trọng.
*   **Tối giản hóa màn hình Đăng nhập:** Thiết kế tinh giản, tối giản hết mức tạo cảm giác tập trung.

### ⏰ Nhắc nhở & Báo thức thông minh
*   **Full-screen Alarm Overlay:** Màn hình báo thức toàn màn hình cao cấp tự động đánh thức thiết bị và reo chuông ngay cả khi điện thoại đang khóa hoặc tắt màn hình.
*   **Hỗ trợ chạy ngầm tối đa:** Sử dụng `fullScreenIntent` và quyền hạn cao để chống lại các cơ chế đóng băng app (như `OplusHansManager` trên máy Realme/OPPO).
*   **Cơ chế bù trễ 3 phút (Grace Period):** Tự động phát chuông nhắc nhở ngay lập tức nếu tác vụ đồng bộ kết thúc muộn hơn giờ đặt một vài giây.
*   **Hủy báo thức thông minh:** Tự động xóa sạch mọi báo thức chờ của bạn khi thực hiện đăng xuất (Logout).

### ⚙️ Kiến trúc Hệ thống linh hoạt
*   **Cấu hình IP máy chủ động:** Dễ dàng thay đổi địa chỉ IP của Server API trực tiếp từ màn hình ứng dụng mà không cần thay đổi mã nguồn hay build lại app.
*   **Database đồng bộ hóa:** Đồng bộ hóa dữ liệu thời gian thực giữa thiết bị Android và SQL Server/SQLite thông qua RESTful API.

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ / Thư viện sử dụng |
| --- | --- |
| **Android App** | Kotlin, Jetpack Compose, Retrofit2, OkHttp3, Material 3, Android Jetpack Work/Alarm, EncryptedSharedPreferences |
| **Backend API** | C# .NET Core WebAPI, Entity Framework Core, ASP.NET Core Identity (JWT Bearer) |
| **Database** | SQLite / SQL Server |

---

## 📦 Hướng dẫn cài đặt & Chạy ứng dụng

### 1. Khởi động Backend API (C#)
1. Cài đặt **.NET 8.0 SDK** trở lên trên máy tính của bạn.
2. Di chuyển vào thư mục backend:
   ```bash
   cd backend-api
   ```
3. Chạy các lệnh migration để khởi tạo database:
   ```bash
   dotnet ef database update
   ```
4. Khởi động server (chạy trên tất cả IP mạng nội bộ):
   ```bash
   dotnet run --urls "http://0.0.0.0:5158"
   ```

### 2. Cài đặt và Chạy App Android
1. Mở thư mục `android-app` bằng **Android Studio**.
2. Kết nối điện thoại Android của bạn ở chế độ **Gỡ lỗi USB (USB Debugging)**.
3. Build và cài đặt trực tiếp lên thiết bị thông qua Gradle:
   ```powershell
   ./gradlew.bat installDebug
   ```
4. Cấu hình IP máy chủ khi đăng nhập để app kết nối được với máy tính chạy backend của bạn.

---

## 👥 Đóng góp dự án
Nâng tầm quản lý công việc và thói quen hàng ngày với giao diện tối giản, đầy cảm hứng. Dự án được phát triển theo hướng mã nguồn mở—mọi phản hồi, ý tưởng đóng góp hay báo lỗi từ cộng đồng đều là mảnh ghép quan trọng giúp chúng tôi hoàn thiện sản phẩm. Hãy cùng đồng hành bằng cách mở Issue hoặc gửi Pull Request cho dự án.
## 👥 Development Team
| Tên thành viên | Vai trò | Trách nhiệm chính |
| :--- | :--- | :--- |
| **Phan Gia Bảo** | Backend Lead & Architect | Kiến trúc hệ thống, Thiết kế Database, Core Backend API & Security. |
| **Huỳnh Khánh** | Full-stack Developer | Phát triển toàn bộ Frontend (UI/UX), hỗ trợ tối ưu Backend API & Tài liệu báo cáo. |
| **Trần Văn Nam** | Leader | Quản lý dự án, điều phối tiến độ, phân tích yêu cầu, Quản lý tiến độ dự án, Soạn thảo báo cáo & Tài liệu thuyết trình.. |
| **Nguyễn Kha Gia Hào** | Frontend Developer | Hỗ trợ thiết kế giao diện và xây dựng các thành phần Frontend. |
| **Lục Vinh Khang** | QA Engineer | Kiểm thử tích hợp (Integration Testing), đảm bảo độ ổn định hệ thống. |



