package de.szut.passkeeper.model;


import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Dennis Stumm on 17.02.15.
 */
public class Security {
    private static Security INSTANCE;

    /**
     * @return
     */
    public static Security getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Security();
        }
        return INSTANCE;
    }

    /**
     * @param password
     * @return
     */
    public String encryptPassword(String password) {
        StringBuffer result = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes("UTF-8"));
            byte[] digest = md.digest();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * @param password
     * @param hash
     * @return
     */
    public boolean checkPassword(String password, String hash) {
        String passwordHash = encryptPassword(password);
        return hash.equals(passwordHash);
    }

    /**
     * @param password
     * @param salt
     * @return
     */
    public SecretKey getSecret(String password, byte[] salt) {
        SecretKey secret = null;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return secret;
    }

    /**
     * @param value
     * @return
     */
    public String encryptValue(String value, SecretKey secret, byte[] iv) {
        String encryptedValueBase64 = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secret, ivSpec);
            byte[] encryptedValue = cipher.doFinal(value.getBytes("UTF-8"));
            encryptedValueBase64 = Base64.encodeToString(encryptedValue, Base64.NO_WRAP);
        } catch (NoSuchPaddingException
                | UnsupportedEncodingException
                | NoSuchAlgorithmException
                | IllegalBlockSizeException
                | BadPaddingException
                | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return encryptedValueBase64;
    }

    /**
     * @param value
     * @return
     */
    public String decryptValue(String value, SecretKey secret, byte[] iv) {
        String decryptedValue = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);
            byte[] decodedValue = Base64.decode(value, Base64.NO_WRAP);
            byte[] decryptedValueBytes = cipher.doFinal(decodedValue);
            decryptedValue = new String(decryptedValueBytes);
        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | IllegalBlockSizeException
                | BadPaddingException
                | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return decryptedValue;
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     */
    public byte[] generateSalt() {
        byte[] salt = new byte[8];
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return salt;
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     */
    public byte[] generateIV() {
        byte[] iv = null;
        try {
            iv = new byte[Cipher.getInstance("AES/CBC/PKCS5Padding").getBlockSize()];
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(iv);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return iv;
    }

    public String generatePassword() {
        String password = "";
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            password += (char) (random.nextInt(93) + 33);
        }
        return password;
    }
}