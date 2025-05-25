package utility;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class SecureHashing {
    private SecretKeyFactory keyFactory;
    private SecureRandom random;
    private final int ITERATIONS = 100000;
    public SecureHashing(){
        try {
            keyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA512");
            random = new SecureRandom();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String hash(String input) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] hashed = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashed);
    }

    public SecretKey hashWithSalt(String password, byte[] salt) {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, ITERATIONS, 512);
        byte[] hash;
        try {
            hash = keyFactory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        return new SecretKeySpec(hash, "AES");
    }

    public String encrypt(String input) {
        return "";
    }

    public String decrypt(String input) {
        return "";
    }

    public byte[] generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);

        int paddingLength = (array.length * 2) - hex.length();

        if(paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }

        return hex;
    }
}
