package com.example.identity_service.exception;

import com.example.identity_service.dto.request.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
//Annotation @ControllerAdvice sẽ bắt toàn bộ những lỗi có trong class UserController
@Slf4j
public class GlobalExceptionHandler{

    private static final String MIN_ATTRIBUTE = "min";
    //Tương ứng từng exception ta sẽ khai báo cho từng method và sử dụng annotation @ExceptionHandler kèm theo đó là RuntimeException (exception mà ta muốn bắt)
    @ExceptionHandler(value = RuntimeException.class) //Annotation này dùng để catch 1 exception
    //Method này dùng để bắt tất cả các exception chung chưa được định nghĩa cụ thể
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception /*Khi khai báo parameter này vào method thì Java sẽ inject exception vào parameter này*/){
        //Thông thường những lỗi liên quan đến dữ liệu từ người dùng gây ra thì ta sẽ trả về lỗi 400 tương ứng method badRequest()
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(1001);
        apiResponse.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class) //Annotation này dùng để catch 1 exception
    //Method này dùng để bắt tất cả exception chung được định nghĩa trong AppException(AppException extends từ RuntimeException).
    //AppException chứa enum ErrorCode. Trong enum ErrorCode chứa các thông tin exception đã được xác định.
    //Tóm lại Method này bắt tất cả các exception trong ứng dụng(đã được định nghĩa sẵn trong enum ErrorCode)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception /*Khi khai báo parameter này vào method thì Java sẽ inject exception vào parameter này*/){
        //Ta cần lấy ra 1 errorCode dựa vào AppException vì AppException kế thừa RuntimeException
        //Bên trong AppException lại chứa ErrorCode và method getErrorCode() nên để lấy ra giá trị errorCode ta phải thông qua exception.getErrorCode()
        //ApiResponse sẽ trả về bao gồm code, message
        //Thày vì ta khai báo ErrorCode errorCode = new ErrorCode() thì ta có thể gán trực tiếp thông qua AppException(vì AppException có method getErrorCode() trả về toàn bộ thông tin của ErrorCode())
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(apiResponse);
    }

    //AccessDeniedException là 1 exception tách riêng chỉ có trong Spring security. Method này dùng để xử lý riêng cho trường hợp phân quyền(khi user không có quyền truy cập)
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception){
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    //Trong spring MethodArgumentNotValidException cung cấp khả năng chúng ta có thể lấy được cái attribute mà chúng ta truyền vào annotation
    //Bắt tất cả các exception liên quan đến validation(từ annotation @Valid trong Controller)
    @ExceptionHandler(value = MethodArgumentNotValidException.class) //Annotation này dùng để catch 1 exception
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception /*Khi khai báo parameter này vào method thì Java sẽ inject exception vào parameter này*/){
        //Lấy ra message từ annotation (@Size, @DobConstraint)
        //getFieldError() Trả về 1 đối tượng FieldError chứa thông tin lỗi của field trong validate fail. Mỗi FieldError đại diện cho 1 field cụt thể (ví dụ: field: username, field: password,...)
        //getDefaultMessage() trả về thông báo lỗi được định nghĩa trong annotation validation(nếu không tự định nghĩa message Spring dùng message mặc định)
        /*Ví dụ: Ở class UserCreationRequest field username đã được sử dụng annotation @Size(min = 3, message = "USERNAME_INVALID")
        * Khi đó nếu xảy ra lỗi liên quan đến nhập thông tin username thì sẽ được bắt exception ở đây. getFieldError() sẽ lấy ra field đang bị lỗi (cụ thể là username) và getDefaultMessage() sẽ lấy message của field bị lỗi đó trong trường hợp này là "USERNAME_INVALID"*/
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY; //Gán mặc định giá trị errorCode là INVALID_KEY
        Map<String, Object> attributes = null; //Tạo map với key là String và value là object
        try{
            errorCode = ErrorCode.valueOf(enumKey); //Chuyển message thành ErrorCode errorCode

            //Lấy thông tin constraint violation (ví dụ: Lấy min = 8 trong @Size(min = 8, message = "USER_INVALID"))
            var constraintViolation = exception.getBindingResult() //getBindingResult() là những cái error mà cái MethodArgumentNotValidException này wrap lại
                    .getAllErrors().iterator().next().unwrap(ConstraintViolation.class); //Khi chúng ta unwrap thông tin này ra chúng ta có được 1 object trong đó sẽ chứa thông tin chúng ta mong muốn ở đây chính là attribute
            //Trong constraintViolation này chúng ta sẽ có được 1 số cái như sau: getConstraintDescriptor() đây chính là nội dung của annotation của chúng ta
            //getAttributes() đây chính là 1 cái map attributes mà từ đó chúng ta có thể lấy được cái thông tin chi tiết của từng param mà chúng ta truyền vào
            attributes = constraintViolation.getConstraintDescriptor().getAttributes(); //Ví dụ: {min = 8}

            log.info(attributes.toString());

        }catch(IllegalArgumentException e){

        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes) ?
                mapAttribute(errorCode.getMessage(), attributes) //Thay thế {min} bằng giá trị thực
                : errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes){
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
