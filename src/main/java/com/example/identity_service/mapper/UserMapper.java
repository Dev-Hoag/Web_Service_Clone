package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.request.UserUpdateRequest;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

//Interface này có nhiệm vụ map dữ liệu của class UserCreationRequest thành dữ liệu object User
@Mapper(componentModel = "spring") //Define như này để thông báo mapstruct biết ta sẽ generate mapper này để sử dụng trong spring theo kiểu dependencies injection
public interface UserMapper {
    User toUser(UserCreationRequest request); //Method này nhận về 1 parameter là request theo kiểu UserCreationRequest và sẽ trả về là 1 class User
    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request); //Annotation @MappingTarget dùng để map data từ class UserUpdateRequest sang class User
}
