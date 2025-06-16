package com.example.identity_service.dto.response;

import com.example.identity_service.entity.Permission;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
//Annotation của lombok dùng để generate(bao gồm getter, setter, constructor và hàm ToString) các field có trong class
@NoArgsConstructor //Annotation này có chức năng tạo constructor mà không cần đầy đủ các thông tin
@AllArgsConstructor //Annotation này có chức năng tạo constructor với đầy đủ các thông tin
@Builder
//Annotation này có chức năng tối ưu hóa việc sử dụng các method giúp tạo 1 Object nhanh hơn cho phép gọi trực tiếp đến các field có trong class và gán trực tiếp giá trị lên chúng
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    String name;
    String description;
    Set<PermissionResponse> permissions;
}
