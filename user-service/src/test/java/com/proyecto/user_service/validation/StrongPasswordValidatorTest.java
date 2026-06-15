package com.proyecto.user_service.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {
        "Password123!",
        "Test@12345",
        "Aa1$bcde#",
        "COMPLEJA*pass1",
        "Segura%789+"
    })
    void isValid_returnsTrue_forValidPassword(String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }

    @Test
    void isValid_returnsTrue_withAllSpecialChars() {
        String allSpecialChars = "Pass" + new String(new char[]{'@', '#', '$', '%', '^', '&', '+', '=', '!', '*', '(', ')'}) + "12";
        assertThat(validator.isValid(allSpecialChars, null)).isTrue();
    }

    @Test
    void isValid_returnsFalse_whenTooShort() {
        assertThat(validator.isValid("Aa1$", null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenMissingUppercase() {
        assertThat(validator.isValid("password123!", null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenMissingLowercase() {
        assertThat(validator.isValid("PASSWORD123!", null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenMissingDigit() {
        assertThat(validator.isValid("Password@!", null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenMissingSpecialChar() {
        assertThat(validator.isValid("Password123", null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forEmpty() {
        assertThat(validator.isValid("", null)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forBlank() {
        assertThat(validator.isValid("        ", null)).isFalse();
    }
}
