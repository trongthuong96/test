package com.example.appointmentscheduler.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrentPasswordMatchesValidator.class)
public @interface CurrentPasswordMatches {

    String message() default "Mật khẩu hiện tại sai lè rồi!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
