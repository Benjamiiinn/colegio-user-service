package com.proyecto.user_service.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RutValidatorTest {

    private final RutValidator validator = new RutValidator();

    @Test
    void isValid_returnsTrue_forValidRut() {
        assertThat(validator.isValid("21719226-9", null)).isTrue();
    }

    @Test
    void isValid_returnsFalse_forInvalidRut() {
        assertThat(validator.isValid("12345678-0", null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forEmpty() {
        assertThat(validator.isValid("", null)).isFalse();
    }
}
