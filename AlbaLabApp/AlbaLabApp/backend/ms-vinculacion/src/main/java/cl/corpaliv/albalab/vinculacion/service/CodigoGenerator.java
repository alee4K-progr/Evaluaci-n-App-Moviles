package cl.corpaliv.albalab.vinculacion.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/** Codigo corto y legible (sin caracteres ambiguos) para consultar el estado: AL-XXXXXXXX. */
@Component
public class CodigoGenerator {
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom azar = new SecureRandom();

    public String nuevo() {
        StringBuilder sb = new StringBuilder("AL-");
        for (int i = 0; i < 8; i++) sb.append(ALFABETO.charAt(azar.nextInt(ALFABETO.length())));
        return sb.toString();
    }
}
