package com.example.identity_service.controller;

import com.example.identity_service.dto.request.ApiResponse;
import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.request.UserUpdateRequest;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.entity.User;
import com.example.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users") //Được sử dụng để mặc định phương thức HTTP của RESTful API là /users
@Slf4j
public class UserController /*Layer Controller sẽ gọi xuống layer Service thông qua class UserService*/{
    @Autowired //Tự động tìm kiếm Bean tương ứng với UserService để autowired
    private UserService userService;

    @PostMapping
    //Xử lý request của User và tạo 1 User tương ứng
    /*Annotation @Valid sẽ hoạt động với annotation @Size và @DobConstraint có trong class UserCreationRequest.
    Nếu không sử dụng @Valid chương trình sẽ vẫn tiếp tục chạy và không thông báo lỗi khi user đăng nhập không hợp lệ*/
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request)); //Thay vì trả thảng về Object User ta sẽ wrapper Object user này vào Result của ApiResponse

        return apiResponse;
    }

    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @GetMapping
    List<UserResponse> getUsers(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return userService.getUsers();
    }


    @GetMapping("/{userId}")
    UserResponse getUser(@PathVariable String userId /*Tự động match userId trên GetMapping xuống đây*/){
        return userService.findUser(userId);
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
//        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return "User has been deleted";
    }
}
