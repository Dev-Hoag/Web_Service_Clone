package com.example.identity_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL) //Annotation giúp loại bỏ các thành phần có null
@Data
//Annotation của lombok dùng để generate(bao gồm getter, setter, constructor và hàm ToString) các field có trong class
@NoArgsConstructor //Annotation này có chức năng tạo constructor mà không cần đầy đủ các thông tin
@AllArgsConstructor //Annotation này có chức năng tạo constructor với đầy đủ các thông tin
@Builder
//Annotation này có chức năng tối ưu hóa việc sử dụng các method giúp tạo 1 Object nhanh hơn cho phép gọi trực tiếp đến các field có trong class và gán trực tiếp giá trị lên chúng
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse <T>{
    int code = 1000; //Trả về 1 mã lỗi để user có thể biết đang bị lỗi gì
    //Mặc định code = 1000 tức là API có kết quả là thành công
    String message;
    T result; //Kêt quả trả về, thông thường khi gọi 1 API thì sẽ trả về 1 thông tin ví dụ như API tạo user sẽ trả về thông tin user đó
}
