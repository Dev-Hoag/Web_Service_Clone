package com.example.identity_service.repository;

import com.example.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/*Ở mỗi request ta sẽ đi qua 3 Layer bao gồm: Controller, Service và Repository
* Controller sẽ mapping các enpoint
* Service là nơi để xử lý các logic liên quan đến subdomain
* Repository sẽ tương tác trực tiếp với DBMS */
//Class này sẽ thao tác trực tiếp với cơ sở dữ liệu cho entity User
@Repository
//class này extends từ JpaRepository để kế thừa các phương thức CRUD cơ bản như; save, findById, delete,...
public interface UserRepository extends JpaRepository<User, String> { //JpaRepository<User, String> trong đó User là enity mà class này thao tác. String là kiểu dữ liệu của khóa chính của User ví dụ username là string
    boolean existsByUsername(String username);
    //Tương tự như mapstruct thì khi chúng ta define 1 method findByUsername thì StringJPA sẽ tự động generate code và tìm các field trong User có tên là username
    Optional<User> findByUsername(String username); //Trả về 1 user
}
