package com.proyecto.user_service.util;

public class RutUtils {

    private static final int RUT_MAXIMO_CHILE = 99999999;

    public static String formatearRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return rut;
        }

        String rutLimpio = rut.replace(".", "").replace("-", "").toUpperCase();

        if (rutLimpio.length() < 2) {
            return rutLimpio; // No se puede formatear un RUT tan corto
        }

        String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
        String dv = rutLimpio.substring(rutLimpio.length() - 1);
        return cuerpo + "-" + dv;
    }

    public static boolean validarRut(String rut) {
        if (rut == null || rut.isEmpty()) return false;

        rut = rut.replace(".", "").replace("-", "");
        if (rut.length() < 2) return false;

        try {
            String dvIngresado = rut.substring(rut.length() -1).toUpperCase();
            String numeroStr = rut.substring(0, rut.length() -1);
            int numero = Integer.parseInt(numeroStr);

            if (!esRutValidoEnChile(numero)) {
                return false;
            }

            int m = 0, s = 1;
            for (; numero !=0; numero /=10) {
                s = (s + numero %10 * (9 - m++ %6)) %11;
            }
            String dvCalculado = (char) (s != 0 ? s +47 : 75) + "";
            return dvCalculado.equals(dvIngresado);

        }catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean esRutValidoEnChile(int numero) {
        if (numero <= 0 || numero > RUT_MAXIMO_CHILE) {
            return false;
        }

        String numeroStr = String.valueOf(numero);
        if (numeroStr.length() < 2) {
            return true;
        }

        char primerDigito = numeroStr.charAt(0);
        for (int i = 1; i < numeroStr.length(); i++) {
            if (numeroStr.charAt(i) != primerDigito) {
                return true;
            }
        }
        return false;
    }
}
