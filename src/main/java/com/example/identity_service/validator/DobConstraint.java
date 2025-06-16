package com.example.identity_service.validator;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

//Class này dùng để tự define 1 annotation
//Từ khóa @interface dùng để khởi tạo 1 annotation tự định nghĩa
//Annotation @Target ta chỉ muốn apply(áp dụng) cho 1 biến nên ta chỉ cần ElementType.FIELD là đủ
//Annotation này mới chỉ có tác dụng khai báo còn xử lý annotation này như thế nào thì ta cần có 1 class validatedBy chính là validator cho annotation này
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME) //Annotation này sẽ được xử lý lúc nào: ta sẽ chọn lúc runtime tức là khi runtime thì annotation này sễ được involve vào
@Constraint(
        validatedBy = {DobValidator.class}
) // Annotation này có chức năng là class sẽ chịu trách nhiệm validate cho annotation @interface này
public @interface DobConstraint {
    //Đây là 3 properties quan trọng mà 1 annotation phải có đối với việc validation
    String message() default "Invalid date of birth";

    int min();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
