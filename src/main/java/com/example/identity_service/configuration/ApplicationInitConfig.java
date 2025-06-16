package com.example.identity_service.configuration;

import com.example.identity_service.entity.User;
import com.example.identity_service.enums.Role;
import com.example.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration //Sử dụng annotation này chú thích class này sẽ quản lý các Bean
@RequiredArgsConstructor //Lombok sẽ generate 1 constructor và Bean sẽ tự inject vào constructor đó nên không cần dùng annotation @Autowired
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    //ApplicationRunner sẽ được khởi chạy mỗi khi application start lên
    //PasswordEncoder được khai báo ở field có thể được sử dụng ở các method khác
    PasswordEncoder passwordEncoder;

    @Bean //Bean này chỉ được init khi value: spring.datasource.driverClassName là com.mysql.cj.jdbc.Driver
    @ConditionalOnProperty(prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver") //Điều kiện dựa theo properties là gì: Khi test không muốn init cái Bean này lên. Chỉ khi nào chạy ứng dụng thực tế thì mới init này lên
    //Method tự khởi tạo user: admin
    //Method gồm tham số truyền vào là UserRepository và chỉ được sử dụng trong phạm vi của method applicationRunner còn được gọi là Method injection
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args -> {
            //userRepository làm việc với database. Nó sẽ kiểm tra trong database có tồn tại username là admin không

            if(userRepository.findByUsername("admin").isEmpty()){
                //Khởi tạo biến roles với kiểu dữ liệu trả về là var(kiểu trả về tùy biến dùng để xác định giá trị nào được gán vào biến và phân loại biến đó với kiểu được xác định) trong trường hợp này var sẽ là 1 HashSet lưu giá trị String
                var roles = new HashSet<String>();

                //Biến roles lúc này là 1 HashSet sẽ thêm vào ADMIN từ enum Role
                roles.add(Role.ADMIN.name());

                //Khởi tạo 1 user. Với username là: admin. Password được khởi tạo là: "admin" và được mã hóa bằng passwordEncoder
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
//                        .roles(roles)
                        .build();

                //userRepository sẽ hoạt động với database, lưu thông tin user được tạo ở trên vào database
                userRepository.save(user);

                //In ra dòng cảnh báo khi chương trình start lên
                log.warn("admin user has been created with default password: admin, please change it");
            }
        };
    }
}
