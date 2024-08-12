# Ứng dụng Barcode vào Web đặt lịch hẹn

Dự án này là một ứng dụng lịch hẹn được thiết kế kết hợp thiết bị IoT để quản lý các cuộc hẹn giữa bác sĩ tư nhân và khách hàng. Nó bao gồm các tính năng cho quản trị viên, bác sĩ và khách hàng để lập lịch, xem và quản lý các cuộc hẹn. Ứng dụng chạy trên IntelliJ IDEA và được container hóa bằng Docker.

## Loại Tài khoản

Ứng dụng hỗ trợ bốn loại tài khoản:

1. **Admin**: Quản trị viên có quyền truy cập vào tất cả các tính năng và có thể quản lý người dùng, cuộc hẹn và lập hóa đơn.
2. **Bác sĩ**: Bác sĩ cung cấp dịch vụ khám chữa bệnh và có thể xem, điều chỉnh lịch trình của mình, chọn dịch vụ, quản lý hồ sơ cá nhân và chấp nhận lịch hẹn của khách hàng
3. **Khách hàng (Cá nhân)**: Khách hàng cá nhân có thể lập lịch hẹn với bác sĩ, xem các cuộc hẹn của mình và quản lý hồ sơ cá nhân.
4. **Khách hàng (Tổ chức)**: Khách hàng doanh nghiệp đại diện cho các công ty/bệnh viện/tập thể và có các tính năng tương tự như khách hàng cá nhân nhưng có thể có các tính năng bổ sung cụ thể cho tài khoản thuộc tổ chức.

## Tính năng

- **Xác thực và Phân quyền**: Người dùng phải đăng nhập để truy cập vào ứng dụng. Các vai trò khác nhau có các quyền khác nhau.
- **JWT (JSON Web Token)**: tiêu chuẩn mã nguồn mở (RFC 7519) dùng để truyền tải thông tin an toàn, gọn nhẹ và khép kín giữa các bên tham gia dưới format JSON.
- **Lập lịch**: Bác sĩ và khách hàng có thể lập lịch hẹn, chỉ định dịch vụ khám/tư vấn, ngày và giờ.
- **Xem Cuộc hẹn**: Người dùng có thể xem các cuộc hẹn đã lập lịch của mình, bao gồm các chi tiết như dịch vụ, nhà cung cấp và trạng thái cuộc hẹn.
- **Chat:** Bác sĩ và khách hàng có thể chat trao đổi vấn đề trước khi tới buổi gặp.
- **Quản lý Cuộc hẹn**: Bác sĩ và khách hàng có thể quản lý các cuộc hẹn của mình, chẳng hạn như từ chối, hủy hoặc xác nhận cuộc hẹn.
- **Lập Hóa đơn**: Phía nhà quản trị sẽ tạo hóa đơn cho các cuộc hẹn đã hoàn thành, và khách hàng có thể xem và tải xuống hóa đơn của họ.
- **Quản lý Hồ sơ**: Người dùng có thể quản lý hồ sơ của mình, cập nhật thông tin cá nhân và thay đổi cài đặt tài khoản.
- **Quét Barcode xác nhận:** Khách hàng in phiếu thông tin lịch hẹn đem đến địa điểm để nhà quản trị/bác sĩ kiểm tra và xác thực thông tin nhanh chóng.

## Công nghệ Sử dụng

- **Backend**: Java, Spring Boot, Spring Security, Spring Data JPA
- **Frontend**: HTML, Thymeleaf, CSS
- **Cơ sở dữ liệu**: MySQL
- **Containerization**: Docker
- **IDE**: IntelliJ IDEA

## Thiết bị sử dụng

- Barcode reader Sumicor 1D.

## Cách Chạy

1. Clone dự án vào máy tính cục bộ.
2. Mở dự án trong IntelliJ IDEA.
3. Chạy file docker-compose.yml
4. Thiết lập kết nối cơ sở dữ liệu. (user: 'user', password: 'password');

   ![image](https://github.com/s2thuphuongs2/AppointmentScheduler/assets/76204441/2ba3b96d-8461-4896-9ecb-28767351c4ba)

   ![image](https://github.com/s2thuphuongs2/AppointmentScheduler/assets/76204441/383c092d-9f77-41c0-8323-f112c08aa1df)

   ![image](https://github.com/s2thuphuongs2/AppointmentScheduler/assets/76204441/05baeec8-7e3c-4e5f-bb1e-6d56dcd27fc5)

5. Thay đổi gmail trong application.properties:
   Thay đổi **spring.mail.username** bằng mail của bạn.
   Lấy **spring.mail.password** theo các bước như sau:

        - B1: Tài khoản Google -> Bảo mật -> Xác minh 2 bước (Phải xác minh mới có password 16 chữ) -> Nhập mật khẩu khi được hỏi -> BẬT (bạn có thể sử dụng SMS để lấy mã Gmail để kích hoạt Xác minh 2 bước)

        - B2: Tài khoản Google -> Bảo mật -> Mật khẩu ứng dụng (App Password) -> Nhập mật khẩu khi được hỏi -> Chọn ứng dụng và thiết bị... -> ví dụ: Khác(Tên -tùy chỉnh) -> Nhập tên ứng dụng, ví dụ: MyApp -> Tạo

        - B3: Sao chép mật khẩu 16 ký tự

        - B4: Sử dụng mật khẩu 16 ký tự (password) với tên người dùng (username) Gmail trong ứng dụng.


5. Build và chạy ứng dụng theo lệnh sau.

   - mvn clean install
   - mvn spring-boot:run

6. Truy cập ứng dụng trên trình duyệt web: `localhost:8080`
7. Đăng nhập tài khoản:


    | Tài khoản | Mật khẩu |
    | --- | --- |
    | admin | qwerty123 |
    | doctor | qwerty123 |
    | customer_r | qwerty123 |
    | customer_c | qwerty123 |

## Docker

Ứng dụng có thể được container hóa bằng Docker. Sử dụng Dockerfile cung cấp để xây dựng image Docker và chạy container.

## Người Đóng Góp

- Đỗ Thị Thu Phương
- Phạm Thị Thùy Dương

## Giấy Phép

Dự án này được cấp phép theo Giấy phép MIT - xem tệp [LICENSE.md](https://github.com/slabiak/AppointmentScheduler/blob/develop/LICENSE.md) để biết chi tiết

