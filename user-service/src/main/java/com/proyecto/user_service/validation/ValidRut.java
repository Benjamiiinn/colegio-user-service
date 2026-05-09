package com.proyecto.user_service.validation;

import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = RutValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRut {
    String message() default "El RUT ingresado no es valido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
