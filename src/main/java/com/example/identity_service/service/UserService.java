package com.example.identity_service.service;

import com.example.identity_service.constant.PredefinedRole;
import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.request.UserUpdateRequest;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.entity.Role;
import com.example.identity_service.entity.User;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.UserMapper;
import com.example.identity_service.repository.RoleRepository;
import com.example.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;

    RoleRepository roleRepository;

    //Tạo method với chức năng create user
    //Class này có chức năng lấy về thông tin User đã đăng nhập và sẽ tạo ra User với Id tương ứng
    public UserResponse createUser(UserCreationRequest request){

//        if(userRepository.existsByUsername(request.getUsername())){
//            throw new AppException(ErrorCode.USER_EXISTED);//Gọi AppException vì nó đang chứa Errorcode mà ErrorCode lại là enum chứa USER_EXISTED
//        }

        User user = userMapper.toUser(request); //Map từ UserCreationRequest sang User bằng userMapper
        user.setPassword(passwordEncoder.encode(request.getPassword())); //Truyền vào password và mã hóa nó bằng passwordEncoder mà user đã gửi xuống

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(role -> roles.add(role));
        user.setRoles(roles);

        try{
            user = userRepository.save(user);
        }catch(DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')") //Spring sẽ tạo ra 1 proxy trước method getUsers(). Spring sẽ kiểm tra trước lúc gọi hàm là phải có role ADMIN thì mới truy cập được
    public List<UserResponse> getUsers(){

        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    public UserResponse getMyInfo(){
        //Trong Spring security khi 1 request được xác thực thành công thì thông tin của user đăng nhập sẽ được lưu trư trong SecurityContextHolder
        var context = SecurityContextHolder.getContext(); //Lấy được thông tin user hiện tại
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST));

        return userMapper.toUserResponse(user);
    }

    @PostAuthorize("hasRole('ADMIN')") //Sẽ kiểm tra sau khi method được thực hiên xong. Nếu thỏa điều kiện thì kêt quả method được return về. Néu không thì sẽ bị chặn lại
    public UserResponse findUser(String userId){
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.updateUser(user, request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());

        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId){
        userRepository.deleteById(userId);
    }
}
