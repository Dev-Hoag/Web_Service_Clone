package com.example.identity_service.configuration;

import com.example.identity_service.dto.request.ApiResponse;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

//Class AuthenticationEntryPoint có tác dụng gửi phản hồi HTTP yêu cầu thông tin xác thực từ phía client
//Class này dùng để triển khai interface AuthenticationEntryPoint để xử lý các yêu cầu chưa được xác thực
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    //Object HttpServletResponse là object để chúng ta response về những cái nội dung mà chúng ta mong muốn
    //Khi người dùng truy cập 1 endpoint yêu cầu xác thực mà không có token hoặc token không hợp lệ, Spring Security sẽ gọi phương thức commence() của class này
    //HttpServletRequest chứa thông tin yêu cầu từ client
    //HttpServletResponse chứa thông tin phản hồi từ client
    //AuthenticationException chứa thông tin lỗi xác thực
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException)
            throws IOException, ServletException {

        //ErrorCode để lấy ra lỗi không xác thực
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        //response gán giá trị status trên HTTP là lỗi 401 Unauthorized
        response.setStatus(errorCode.getStatusCode().value());

        //Trả về 1 body với contentType là JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        //Tạo phản hồi JSON với mã code và message
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        //Chuyển đổi sang JSON và gửi về cho client
        //ObjectMapper chuyển đổi object java thành JSON
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse)); //Chuyển từ object apiResponse về 1 String
        response.flushBuffer(); //đẩy dữ liệu về cho client ngay lập tức
    }
}
