package com.example.identity_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
//Class này dùng để keep những token mà đã được log-out
public class InvalidatedToken {

    @Id
    String id; //Id này chính là jwtID(UUID.randomUUID().toString()) trong AuthenticationService

    Date expiryTime;

}
