package com.example.identity_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {
    private int min;

    //Method initialize() này sẽ khởi tạo mỗi khi constraint được khởi tạo thì chúng ta sẽ get được thông số của annotation đó
    //Ví dụ: Khi initialize chúng ta cần biết cái value min() mà người ta muốn nhập là bao nhiêu thì ta có thể get ở bước initialize sẽ diễn ra trước khi bước validation xảy ra nên ta có thể có được cái value mà chsung ta đã config cho annotation này
    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min(); //Min này được lấy từ giá trị của min trong class DobConstraint

    }

    //Method isValid() là method sẽ xử lý data này đúng hay không
    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if(Objects.isNull(localDate)){ //Kiểm tra nếu localDate rỗng thì trả về true
            return true;
        }
        long years = ChronoUnit.YEARS.between(localDate, LocalDate.now()); //Method between sẽ xác định giữa localDate nhập vào và LocalDate hiện tại đã trải qua bao nhiêu năm

        return years >= min;
    }
}
