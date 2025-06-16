package com.example.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
//Class này dùng để kết nối trực tiếp với database
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) //Annotation này giúp Id user sẽ được tạo random ngẫu nhiên không bao giờ trùng lặp
    String id;

    //columnDefinition có chức năng mặc định các username sẽ không phân biệt in hoa, in thường
    @Column(name = "username", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci") //Mặc định cột của username có tên là username và field này là field unique trong database. DBMS đảm bảo field này sẽ không bị trùng(đảm bảo concurrent request sẽ không bị xảy ra)
    String username;
    String password;
    String firstName;
    String lastName;
    String email;
    LocalDate dob;

    @ManyToMany
    Set<Role> roles;
}
