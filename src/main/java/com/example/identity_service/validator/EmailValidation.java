package com.example.identity_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


//ConstraintValidator<EmailConstraint, String>: String là kiểu dữ liệu được lấy từ UserCreationRequest
public class EmailValidation implements ConstraintValidator<EmailConstraint, String> {
    private int min;
    @Override
    public void initialize(EmailConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(s == null || s.length() < min){
            return false;
        }
        String reg = "^[\\w-_\\]";
        return false;
    }
}
