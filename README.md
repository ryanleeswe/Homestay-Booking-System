
# Chạy Homestay Booking System bằng Docker

## Hai file chính

- `Dockerfile`: dùng Maven và Java 21 để build JAR, sau đó đóng gói backend
  vào image Java runtime. Không cần cài Java/Maven trên máy để build bằng Docker.
- `compose.yaml`: chạy backend và MySQL 8.4 cùng nhau, cấu hình kết nối,
  ánh xạ cổng và volume lưu database. Backend đợi MySQL healthcheck thành công.

Các file hỗ trợ: `.dockerignore` loại cấu hình cá nhân và mật khẩu khỏi build
context; `.env.example` là mẫu biến môi trường. `.env` được Git bỏ qua.

## 1. Chuẩn bị

Cài và mở Docker Desktop, sử dụng Linux containers. Trong terminal tại thư mục
dự án, kiểm tra:

```powershell
docker --version
docker compose version
```

Nếu chưa có `.env`, tạo từ mẫu:

```powershell
Copy-Item .env.example .env
```

Mở `.env`, thay `DOCKER_MYSQL_ROOT_PASSWORD` và `DOCKER_MYSQL_PASSWORD` bằng hai
mật khẩu riêng cho database Docker. Dùng mật khẩu chữ/số để dễ cấu hình ban đầu.
Đây không phải mật khẩu của MySQL Windows đang chạy ở cổng 3306.

Tạo JWT secret ngẫu nhiên và ghi vào `.env` bằng PowerShell:

```powershell
$jwtBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
$rng.Dispose()
$jwtSecret = [Convert]::ToBase64String($jwtBytes)
$envText = (Get-Content .env -Raw) -replace '(?m)^JWT_SECRET=.*$', "JWT_SECRET=$jwtSecret"
Set-Content -LiteralPath .env -Value $envText -Encoding ascii
```

Giữ secret này cho các lần chạy sau. Nếu terminal đã có biến `JWT_SECRET`, Compose
sẽ ưu tiên biến của terminal hơn `.env`. Không commit `.env` lên Git.

## 2. Build và khởi động

```powershell
docker compose config --quiet
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

Lần đầu cần Internet để tải image và dependency Maven. Khi log có dòng
`Started HomestayBookingSystemApplication`, backend đã khởi động.
Nhấn Ctrl+C để thoát xem log; container vẫn chạy ở chế độ nền.

Image build bỏ qua test cần môi trường database. Chạy test riêng trước khi triển khai.

## 3. Thử trên Postman

Gọi `GET http://localhost:8081/` với Authorization = No Auth.
Các API khác dùng cùng cổng 8081, ví dụ `POST /users`, `POST /auth/login`.
Sau đăng nhập, dùng `data.accessToken` với Bearer Token cho API cần xác thực.

| Thành phần | Địa chỉ truy cập từ máy bạn | Bên trong Docker |
|---|---|---|
| Backend | localhost:8081 | backend:8080 |
| MySQL | 127.0.0.1:3307 | mysql:3306 |

Backend kết nối MySQL bằng hostname `mysql`, không dùng `localhost` vì hai
service chạy trong hai container khác nhau. Đổi `BACKEND_PORT` hoặc `MYSQL_PORT`
trong `.env` nếu cổng trên máy đã bị chiếm, rồi chạy lại `docker compose up -d`.

## 4. Xem database bằng Workbench

Tạo kết nối TCP với host `127.0.0.1`, port `3307`, username `homestay`, password
là giá trị `DOCKER_MYSQL_PASSWORD` trong `.env`, schema `homestay_db`.

Database Docker tách biệt database MySQL Windows tại cổng 3306. Dữ liệu user cũ
không tự chuyển sang Docker; tạo tài khoản thử mới qua API cổng 8081.

## 5. Dừng và chạy lại

```powershell
docker compose down
docker compose up -d
```

`down` giữ nguyên volume database. Không thêm `-v` nếu cần giữ dữ liệu.
Khi sửa code, dùng `docker compose up -d --build` để build lại backend.
Đổi mật khẩu trong `.env` không tự đổi mật khẩu của database đã khởi tạo trong
volume; cần đổi mật khẩu trong MySQL tương ứng trước khi dùng giá trị mới.

Cấu hình này phục vụ chạy thử local, các cổng chỉ mở trên máy bạn.
Tham khảo thứ tự khởi động và healthcheck tại
[Docker Compose documentation](https://docs.docker.com/compose/how-tos/startup-order/).
