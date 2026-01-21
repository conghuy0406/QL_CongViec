# QL_CongViec - Ứng Dụng Quản Lý Công Việc

QL_CongViec là một ứng dụng di động trên nền tảng Android giúp người dùng tổ chức, theo dõi và quản lý các công việc hàng ngày một cách hiệu quả. Ứng dụng hỗ trợ lên lịch, phân loại công việc và nhắc nhở để bạn không bỏ lỡ bất kỳ nhiệm vụ quan trọng nào.

## Tính Năng Chính

- Hệ Thống Tài Khoản:
  - Đăng ký và Đăng nhập tài khoản người dùng cá nhân.

- Quản Lý Công Việc (Tasks):
  - Thêm mới công việc với tiêu đề, nội dung, ngày giờ.
  - Xem chi tiết, chỉnh sửa hoặc xóa công việc.
  - Đánh dấu hoàn thành công việc.

- Quản Lý Danh Mục (Categories):
  - Tạo các danh mục công việc (Ví dụ: Học tập, Làm việc, Giải trí...).
  - Gán icon cho từng danh mục để dễ nhận biết.

- Lịch & Nhắc Nhở:
  - Lịch (Calendar): Xem danh sách công việc theo ngày trên giao diện lịch trực quan.
  - Thông báo (Notification): Hệ thống tự động gửi thông báo nhắc nhở khi đến hạn công việc.

- Lưu Trữ:
  - Sử dụng cơ sở dữ liệu nội bộ (SQLite) để lưu trữ dữ liệu an toàn và offline.

## Công Nghệ Sử Dụng

- Ngôn ngữ: Java
- Nền tảng: Android SDK
- Database: SQLite (thông qua DBHelperDatabase)
- IDE: Android Studio
- Công cụ build: Gradle

## Cấu Trúc Dự Án

app/src/main/java/com/example/ql_congviec/
- Adapter/: Chứa các Adapter cho RecyclerView (TaskAdapter...)
- Database/: Xử lý kết nối SQLite (DBHelperDatabase.java)
- model/: Các model dữ liệu (Category, UserTask...)
- Activity/: Các màn hình chính (Activity)
  - Login.java: Màn hình đăng nhập
  - Register.java: Màn hình đăng ký
  - MainActivity.java: Màn hình chính
  - Add_Task.java: Thêm công việc
  - TaskCalendarActivity.java: Xem lịch công việc
- TaskReminderReceiver.java: BroadcastReceiver xử lý thông báo nhắc nhở

## Hướng Dẫn Cài Đặt (Installation)

Để chạy dự án này, bạn cần cài đặt Android Studio phiên bản mới nhất.

Bước 1: Clone dự án
git clone https://github.com/conghuy0406/ql_congviec.git

Bước 2: Mở trong Android Studio
1. Khởi động Android Studio.
2. Chọn Open và trỏ đến thư mục QL_CongViec vừa clone.
3. Chờ Gradle tải các thư viện và build project (Quá trình này có thể mất vài phút).

Bước 3: Cấu hình Máy ảo (AVD) hoặc Thiết bị thật
- Tạo một máy ảo Android (Emulator) hoặc kết nối điện thoại Android qua cáp USB (nhớ bật chế độ USB Debugging).

Bước 4: Chạy ứng dụng
- Nhấn nút Run (biểu tượng tam giác xanh) trên thanh công cụ của Android Studio.

## Bản Quyền
Dự án được phát triển bởi Nguyễn Công Huy.
