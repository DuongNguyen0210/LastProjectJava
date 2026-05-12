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
- Chuột phải vào file run_crawler.bat và chọn Edit. Mày sẽ thấy dòng lệnh khởi chạy có dạng các thuộc tính -D. Hãy chỉnh sửa nội dung trong ngoặc kép cho phù hợp với nhu cầu:

- DbotUser: Nhập tên đăng nhập tài khoản dùng để cào (Codeforces).

- DbotPass: Nhập mật khẩu của tài khoản dùng để cào.

- DcrawlDays: Số ngày muốn lùi về quá khứ để cào (Ví dụ: 7 là cào các bài trong 1 tuần qua).

- DcrawlUsers: Danh sách Username mục tiêu cần cào. Lưu ý: Các tên phải cách nhau bằng dấu phẩy , (Ví dụ: tourist,jiangly,benq).

2. Chỉ định phiên bản Java (JDK 11+)
- Nếu máy tính báo lỗi về phiên bản Java, hãy thay chữ java ở đầu lệnh bằng đường dẫn tuyệt đối đến file java.exe của JDK 11 trở lên.
3. Ví dụ nội dung file .bat hoàn chỉnh:
```
DOS
@echo off
cd /d "%~dp0"
"C:\Program Files\Java\jdk-11\bin\java.exe" -Dfile.encoding=UTF-8 -DbotUser="nick_bot_cua_tao" -DbotPass="mat_khau_123" -DcrawlDays="10" -DcrawlUsers="tourist,jiangly,benq" -jar "LastProjectJava-1.0-SNAPSHOT-jar-with-dependencies.jar"
pause
```