package cl.corpaliv.albalab.admin.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;

/** Hash de contraseñas con PBKDF2-HMAC-SHA256 y sal aleatoria. Formato: pbkdf2$iteraciones$sal$hash. */
@Service
public class HashService {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int LARGO_SAL = 16;
    private static final int LARGO_HASH_BITS = 256;
    private final SecureRandom azar = new SecureRandom();

    public String hash(String clave) {
        byte[] sal = new byte[LARGO_SAL];
        azar.nextBytes(sal);
        byte[] h = derivar(clave, sal, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$" + b64(sal) + "$" + b64(h);
    }

    public boolean coincide(String clave, String almacenado) {
        if (clave == null || almacenado == null) return false;
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) return false;
        try {
            int iter = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, derivar(clave, sal, iter));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] derivar(String clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, LARGO_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash", e);
        }
    }

    private static String b64(byte[] b) {
        return Base64.getEncoder().encodeToString(b);
    }
}
