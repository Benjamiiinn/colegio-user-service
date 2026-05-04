package com.proyecto.user_service.validation;

import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StrongPassword {
    String message() default "Debe contener al menos 8 caracteres y ser una combinación de letras mayúsculas, minúsculas, números y caracteres especiales";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
