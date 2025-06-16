package com.example.identity_service.dto.request;

import com.example.identity_service.validator.DobConstraint;
import com.example.identity_service.validator.EmailConstraint;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/*Class này dùng để xử lý các thông tin mà User sẽ đăng nhập(điền thông tin). Có nghĩa là khi User đăng nhậo
* họ sẽ gửi request về và CLass này sẽ tiếp nhận request đó.*/
@Data //Annotation của lombok dùng để generate(bao gồm getter, setter, constructor và hàm ToString) các field có trong class
@NoArgsConstructor //Annotation này có chức năng tạo constructor mà không cần đầy đủ các thông tin
@AllArgsConstructor //Annotation này có chức năng tạo constructor với đầy đủ các thông tin
@Builder //Annotation này có chức năng tối ưu hóa việc sử dụng các method giúp tạo 1 Object nhanh hơn cho phép gọi trực tiếp đến các field có trong class và gán trực tiếp giá trị lên chúng
@FieldDefaults(level = AccessLevel.PRIVATE) //Annotation này cho phép tự động gán các field với mức độ là private
public class UserCreationRequest {
    @Size(min = 3, message = "USERNAME_INVALID")
    String username;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;
    String firstName;
    String lastName;

    @EmailConstraint
    String email;

    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;

}
