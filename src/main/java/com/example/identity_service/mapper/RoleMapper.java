package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.PermissionRequest;
import com.example.identity_service.dto.request.RoleRequest;
import com.example.identity_service.dto.response.PermissionResponse;
import com.example.identity_service.dto.response.RoleResponse;
import com.example.identity_service.entity.Permission;
import com.example.identity_service.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

//Interface này có nhiệm vụ map dữ liệu của class UserCreationRequest thành dữ liệu object User
@Mapper(componentModel = "spring") //Define như này để thông báo mapstruct biết ta sẽ generate mapper này để sử dụng trong spring theo kiểu dependencies injection
public interface RoleMapper {
    @Mapping(target = "permission", ignore = true) //Khi map sẽ bỏ qua attribute permission
    Role toRole(RoleRequest request); //Method này nhận về 1 parameter là request theo kiểu PermissionRequest và sẽ trả về là 1 class Permission
    RoleResponse toRoleResponse(Role request);

}
