package com.example.identity_service.service;

import com.example.identity_service.dto.request.PermissionRequest;
import com.example.identity_service.dto.response.PermissionResponse;
import com.example.identity_service.entity.Permission;
import com.example.identity_service.mapper.PermissionMapper;
import com.example.identity_service.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor //Annotation này dùng để Autowire các Bean
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableMethodSecurity
//Phân quyền trên method. Nên khi muốn phân quyền trên method nào thì chỉ cần đặt annotation trên method đó
@Slf4j
public class PermissionService {

    //Cả 2 object PermissionRepository và PermissionMapper đều được khai báo ở field vì chúng được sử dụng xuyên suôt các method trong class PermissionService
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    //Trả về cho user là 1 PermissionResponse
    //PermissionRequest là 1 tham số đầu vào của method create() và chỉ được sử dụng trong method này
    public PermissionResponse create(PermissionRequest /*Nhận vào là PermissionRequest*/ request){ //Để tạo ra permission thì request chỉ cần permission name và description
        Permission permission = permissionMapper.toPermission(request); //permission ta cần map data từ request vào dữ liệu permission nên ta cần tạo thêm 1 cái mapper cho nó
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll(){
        var permissions = permissionRepository.findAll();

        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void deletePermission(String permission){
        permissionRepository.deleteById(permission);
    }
}
