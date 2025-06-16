package com.example.identity_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) //Annotation này chỉ apply cho 1 biến duy nhất là email trong class UserCreationRequest nên ta dùng annotation này
@Retention(RetentionPolicy.RUNTIME) //Annotation này được xử lý khi runtime
@Constraint(validatedBy = EmailValidator.class)
public @interface EmailConstraint {
    String message() default "Invalid email address";
    int min();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
