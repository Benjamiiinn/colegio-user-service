package com.proyecto.user_service.validation;

import com.proyecto.user_service.util.RutUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutValidator implements ConstraintValidator<ValidRut, String> {
    
    @Override
    public void initialize(ValidRut constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String rutValue, ConstraintValidatorContext context) {
        return RutUtils.validarRut(rutValue);
    }
}
