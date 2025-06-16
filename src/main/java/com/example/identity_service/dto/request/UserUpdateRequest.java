package com.example.identity_service.dto.request;

import com.example.identity_service.validator.DobConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

/*Class này dùng để xử lý các thông tin mà User sẽ đăng nhập(điền thông tin). Có nghĩa là khi User đăng nhậo
* họ sẽ gửi request về và CLass này sẽ tiếp nhận request đó.*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    String password;
    String firstName;
    String lastName;

    @DobConstraint(min = 2, message = "INVALID_DOB")
    LocalDate dob;
    
    List<String> roles;
}
