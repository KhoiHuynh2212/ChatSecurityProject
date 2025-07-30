package sendfile.client;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;

/**
 * Comprehensive crypto manager for secure messaging
 * Supports both 56-bit (DES) and 128-bit (AES) encryption
 */
public class CryptoManager {

    public enum KeySize {
        BITS_56("DES", 56),
        BITS_128("AES", 128);

        private final String algorithm;
        private final int bits;

        KeySize(String algorithm, int bits) {
            this.algorithm = algorithm;
            this.bits = bits;
        }

        public String getAlgorithm() { return algorithm; }
        public int getBits() { return bits; }
    }

    private SecretKey secretKey;
    private KeySize keySize;
    private byte[] iv;
    private static final String CHARSET = "UTF-8";

    /**
     * Initialize crypto manager with password and key size
     */
    public CryptoManager(String password, KeySize keySize) throws Exception {
        this.keySize = keySize;
        this.secretKey = deriveKeyFromPassword(password, keySize);
        this.iv = generateIV();
    }

    /**
     * Derive encryption key from password using SHA-256
     */
    private SecretKey deriveKeyFromPassword(String password, KeySize keySize) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(CHARSET));

        byte[] keyBytes;
        if (keySize == KeySize.BITS_56) {
            // DES uses 56-bit effective key (8 bytes with parity bits)
            keyBytes = Arrays.copyOf(hash, 8);
        } else {
            // AES-128 uses 16 bytes
            keyBytes = Arrays.copyOf(hash, 16);
        }

        return new SecretKeySpec(keyBytes, keySize.getAlgorithm());
    }

    /**
     * Generate initialization vector
     */
    private byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        if (keySize == KeySize.BITS_56) {
            byte[] iv = new byte[8]; // DES block size
            random.nextBytes(iv);
            return iv;
        } else {
            byte[] iv = new byte[16]; // AES block size
            random.nextBytes(iv);
            return iv;
        }
    }

    /**
     * Encrypt message
     */
    public String encrypt(String plaintext) throws Exception {
        // Generate new IV for each message
        byte[] newIV = generateIV();

        Cipher cipher;

        if (keySize == KeySize.BITS_56) {
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(newIV));
        } else {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(newIV));
        }

        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(CHARSET));

        // Combine IV and encrypted data
        byte[] combined = new byte[newIV.length + encryptedBytes.length];
        System.arraycopy(newIV, 0, combined, 0, newIV.length);
        System.arraycopy(encryptedBytes, 0, combined, newIV.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypt message
     */
    public String decrypt(String ciphertext) throws Exception {
        byte[] combined = Base64.getDecoder().decode(ciphertext);

        // Determine IV length based on algorithm
        int ivLength = (keySize == KeySize.BITS_56) ? 8 : 16;

        // Extract IV and encrypted data
        byte[] extractedIV = new byte[ivLength];
        byte[] encryptedData = new byte[combined.length - ivLength];

        System.arraycopy(combined, 0, extractedIV, 0, ivLength);
        System.arraycopy(combined, ivLength, encryptedData, 0, encryptedData.length);

        Cipher cipher;

        if (keySize == KeySize.BITS_56) {
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(extractedIV));
        } else {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(extractedIV));
        }

        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes, CHARSET);
    }

    /**
     * Encrypt file data
     */
    public byte[] encryptFile(byte[] fileData) throws Exception {
        byte[] newIV = generateIV();

        Cipher cipher;

        if (keySize == KeySize.BITS_56) {
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(newIV));
        } else {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(newIV));
        }

        byte[] encryptedData = cipher.doFinal(fileData);

        // Combine IV and encrypted data
        byte[] combined = new byte[newIV.length + encryptedData.length];
        System.arraycopy(newIV, 0, combined, 0, newIV.length);
        System.arraycopy(encryptedData, 0, combined, newIV.length, encryptedData.length);

        return combined;
    }

    /**
     * Decrypt file data
     */
    public byte[] decryptFile(byte[] encryptedFileData) throws Exception {
        // Determine IV length based on algorithm
        int ivLength = (keySize == KeySize.BITS_56) ? 8 : 16;

        // Extract IV and encrypted data
        byte[] extractedIV = new byte[ivLength];
        byte[] encryptedData = new byte[encryptedFileData.length - ivLength];

        System.arraycopy(encryptedFileData, 0, extractedIV, 0, ivLength);
        System.arraycopy(encryptedFileData, ivLength, encryptedData, 0, encryptedData.length);

        Cipher cipher;

        if (keySize == KeySize.BITS_56) {
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(extractedIV));
        } else {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(extractedIV));
        }

        return cipher.doFinal(encryptedData);
    }

    /**
     * Generate message authentication code
     */
    public String generateMAC(String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String keyedMessage = message + Base64.getEncoder().encodeToString(secretKey.getEncoded());
        return Base64.getEncoder().encodeToString(digest.digest(keyedMessage.getBytes(CHARSET)));
    }

    /**
     * Verify message authentication code
     */
    public boolean verifyMAC(String message, String mac) throws Exception {
        String computedMAC = generateMAC(message);
        return computedMAC.equals(mac);
    }

    /**
     * Get key information
     */
    public String getKeyInfo() {
        return String.format("Algorithm: %s, Key Size: %d bits",
                keySize.getAlgorithm(), keySize.getBits());
    }

    /**
     * Refresh IV for new session
     */
    public void refreshIV() {
        this.iv = generateIV();
    }

    /**
     * Get current algorithm name
     */
    public String getAlgorithm() {
        return keySize.getAlgorithm();
    }

    /**
     * Get key size in bits
     */
    public int getKeyBits() {
        return keySize.getBits();
    }
}