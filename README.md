# LastProjectJava - Hệ thống Cào Dữ Liệu Tự Động

Hệ thống tự động cào dữ liệu bài nộp từ Codeforces, lưu trữ vào cơ sở dữ liệu SQL Server và phân tích.

## 📋 Yêu cầu

- Java JDK 11+ trở lên
- Maven 3.6+ (Nếu cần build lại source code)
- SQL Server 2016+ (Windows Authentication)
- Trình duyệt Microsoft Edge và file `msedgedriver.exe` (phải đặt cùng thư mục với file chạy)

## 🚀 Cài đặt

1. Mở SQL Server Management Studio (SSMS).
2. Mở file script `database_setup.sql` đính kèm trong thư mục.
3. Nhấn **Execute (F5)** để hệ thống tự động khởi tạo Database và các bảng cần thiết.

## 🏃‍♂️ Hướng dẫn sử dụng

1. Chạy chương trình từ file `Launcher` trong gui. .
2. Nhập số ngày muốn cào (mặc định: 2).
3. Nhập username (có thể nhiều, cách nhau bằng dấu phẩy).
4. điền tài khoản mật khẩu để cào
5. bấm cào

## 📖 Hướng dẫn sử dụng file .bat
1. Cấu hình các tham số tự động 
- Chuột phải vào file run_crawler.bat và chọn Edit. sẽ thấy dòng lệnh khởi chạy có dạng các thuộc tính -D. Hãy chỉnh sửa nội dung trong ngoặc kép cho phù hợp với nhu cầu:

- DbotUser: Nhập tên đăng nhập tài khoản dùng để cào (Codeforces).

- DbotPass: Nhập mật khẩu của tài khoản dùng để cào.

- DcrawlDays: Số ngày muốn lùi về quá khứ để cào (Ví dụ: 7 là cào các bài trong 1 tuần qua).

- DcrawlUsers: Danh sách Username mục tiêu cần cào. Lưu ý: Các tên phải cách nhau bằng dấu phẩy , (Ví dụ: tourist,jiangly,benq).

2. Chỉ định phiên bản Java (JDK 11+)

- Nếu máy tính báo lỗi về phiên bản Java, hãy thay chữ java ở đầu lệnh bằng đường dẫn tuyệt đối đến file java.exe của JDK 11 trở lên.

3. Ví dụ nội dung file `.bat` hoàn chỉnh:
```
DOS
@echo off
cd /d "%~dp0"
"C:\Program Files\Java\jdk-11\bin\java.exe" -Dfile.encoding=UTF-8 -DbotUser="taikhoan" -DbotPass="mat_khau_123" -DcrawlDays="10" -DcrawlUsers="tourist,jiangly,benq" -jar "LastProjectJava-1.0-SNAPSHOT-jar-with-dependencies.jar"
pause
```

## 🕒 Hướng dẫn cấu hình Task Scheduler để chạy file .bat tự động

# Bước 1: Mở Task Scheduler
Nhấn phím Windows, gõ "Task Scheduler" và nhấn Enter.

# Bước 2: Tạo Task mới
Ở cột bên phải (Actions), chọn Create Basic Task...

Name: Đặt tên cho dễ nhớ (ví dụ: Auto_Crawl_Codeforces).

Description: Ghi chú gì đó (ví dụ: Tự động cào data lúc 12h đêm). Nhấn Next.

# Bước 3: Hẹn giờ (Trigger)

Task Trigger: Chọn tần suất (thường là Daily - Hàng ngày). Nhấn Next.

Start: Chọn ngày bắt đầu và thời gian cụ thể muốn tool tự chạy (ví dụ: 00:00:00).

Recur every: Để là 1 days (chạy mỗi ngày). Nhấn Next.

# Bước 4: Chọn hành động (Action)

- Action: Chọn Start a program. Nhấn Next.

- Program/script: Nhấn Browse... rồi tìm đến file run_crawler.bat.

- Start in (optional): CỰC KỲ QUAN TRỌNG!

- hãy copy đường dẫn thư mục chứa file .bat và dán vào đây (ví dụ: D:\Java\LastProjectJava).

-  này đảm bảo khi chạy, tool sẽ tìm thấy đúng file .jar và msedgedriver.exe nằm cùng chỗ.

# Bước 5: Hoàn tất

Kiểm tra lại thông tin một lần nữa rồi nhấn Finish.