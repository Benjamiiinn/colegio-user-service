package com.proyecto.user_service.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RutUtilsTest {

    //Validar Rut 

    @ParameterizedTest
    @CsvSource({
        "25041654-7",
        "12.345.678-5",
        "21719226-9",
        "9829566-6",
        "21624212-2",
        "18221856-1"
    })
    void validarRut_returnsTrue_forValidRuts(String rut) {
        assertThat(RutUtils.validarRut(rut)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678-5", "12.345.678-5", "123456785"})
    void validarRut_returnsTrue_withAndWithoutFormat(String rut) {
        assertThat(RutUtils.validarRut(rut)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "13505299-k",
        "6232497-K",
        "11278456-K"
    })
    void validarRut_returnsTrue_forRutEndingInK(String rut) {
        assertThat(RutUtils.validarRut(rut)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12345678-0",
        "1-9",
        "12.345.678-0",
        "0-0",
        "abcd"
    })
    void validarRut_returnsFalse_forInvalidRuts(String rut) {
        assertThat(RutUtils.validarRut(rut)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "a"})
    void validarRut_returnsFalse_forNullOrEmpty(String rut) {
        assertThat(RutUtils.validarRut(rut)).isFalse();
    }

    //Formatear Rut

    @ParameterizedTest
    @CsvSource({
        "123456785, 12345678-5",
        "12.345.678-5, 12345678-5",
        "12345678-5, 12345678-5",
        "87654321k, 87654321-K"
    })
    void formatearRut_formatsCorrectly(String input, String expected) {
        assertThat(RutUtils.formatearRut(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void formatearRut_returnsInput_forNullOrEmpty(String input) {
        assertThat(RutUtils.formatearRut(input)).isEqualTo(input);
    }

    @Test
    void formatearRut_returnsSame_forTooShort() {
        assertThat(RutUtils.formatearRut("a")).isEqualTo("A");
    }
}
