package com.example.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
//Class này dùng để kết nối trực tiếp với database
//1 role sẽ có nhiều permission và 1 permission cũng có thể nằm trong nhiều role
public class Role {

    @Id
    String name;

    String description;

    @ManyToMany
    Set<Permission> permission;

}
