package com.example.identity_service.controller;


import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

//Test này được dùng để viết cho Controller
@SpringBootTest
@Slf4j
@AutoConfigureMockMvc
@TestPropertySource("/test.properties") //Là những cái properties mà nó sẽ đọc khi test. Ở đây sẽ trỏ về file test.properties
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserCreationRequest request; //Biến đầu vào (vì trong method createUser ở class Controller nhận param đầu vào là UserCreationRequest)

    private UserResponse userResponse; //Giá trị trả về (vì trong method createUser ở class Controller trả về giá trị là UserResponse)
    private LocalDate dob;

    @BeforeEach //Method sẽ được chạy trước sau khi xuống method bên dưới
    public void initData(){
        dob = LocalDate.of(1990, 1, 1);
        request = UserCreationRequest.builder()
                .username("john")
                .firstName("John")
                .lastName("Doe")
                .password("12345678")
                .dob(dob)
                .build();

        userResponse = UserResponse.builder()
                .username("john")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();
    }
    @Test
    //1 cái @Test bao gồm 3 phần
    void createUser_validRequest_success() throws Exception {
        //Given: Là những dữ liệu đầu vào mà chúng ta đã biết trước và dự đoán nó sẽ xảy ra như vậy
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(request); //Chuẩn hóa 1 object về chuỗi JSON

        Mockito.when(userService.createUser(ArgumentMatchers.any()))
                .thenReturn(userResponse); //Móc lại method createUser() trog class UserService

        //When: Khi chúng ta test cái gì. Ở đây when khi chúng ta request cái ApiResponse<UserResponse> createUser trong class UserController
        mockMvc.perform(MockMvcRequestBuilders
                .post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk()) //Expect status code HTTP là 200, đây cũng chính là then
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000)
        ); //Build method ở đây là method POST. POST tới endpoint "/users"
        //Then: Khi when xảy ra chúng ta sẽ expect điều gì
    }
}
