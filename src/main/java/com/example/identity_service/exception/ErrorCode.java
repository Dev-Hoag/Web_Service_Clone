package com.example.identity_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized excception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 8 character", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You don't have permission", HttpStatus.FORBIDDEN), //Trả về Http status là 403 forbidden
    //Đối với error 401 sẽ khác với error 403 ở điểm error 401 không thể được xử lý bởi globalExceptionHandler vì error xảy ra trên các tầng filter trước khi vào service nên globalExceptionHandler không thể xử lí được tình huống đó nên chúng ta sẽ xử lý nó ở class SecurityConfig
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ;

    private int code;
    private HttpStatusCode statusCode;
    private String message;

    ErrorCode(int code, String message, HttpStatusCode statusCode){
        this.code = code;
        this.message = message;
        this.statusCode = statusCode; //Chuẩn hóa về mã lỗi HTTP từ server đến client. Đại diện cho trạng thái HTTP(ví dụ: 400, 401, 403, 404)
        //Tóm lại HttpStatusCode sẽ trả về trạng thái lỗi.
    }


}
