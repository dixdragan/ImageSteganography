package kriptodx;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Formatter;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 *
 * @author DX
 */
public class CryptoTools  {
    
    public static String hashPassword(String password){
        StringBuilder sb = new StringBuilder();
        try {            
            password+="DXSalting It";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            for(int i=0; i< hash.length ;i++) sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }
    
    public static String hashUser(String password){
        StringBuilder sb = new StringBuilder();
        try {            
            password+="DXSalting It";
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            for(int i=0; i< hash.length ;i++) sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }
    
    public static X509Certificate genCRT(String path) throws IOException, CertificateException {
        FileInputStream fin = new FileInputStream(path);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(fin);
        fin.close();
        return certificate;
    }
    // SIMETRIC
    public static SecretKey AESKeyGenerator() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
      //  keygen.init(256);
        SecretKey symmetricKey = keygen.generateKey();
        return symmetricKey;
    }
    
    public static byte[] AESEncrypt(byte[] input, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte output[] = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        output = cipher.doFinal(input);
        return output;
    }
    
    public static byte[] AESDecrypt(byte[] input, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte output[] = null;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        output = cipher.doFinal(input);
        return output;
    }
    // PKI
    public static byte[] readFileBytes(String filename) throws IOException {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    public static PublicKey readPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicSpec);
    }

    public static PrivateKey readPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);  
    }
    public static byte[] encrypt(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }

    public static byte[] decrypt(PrivateKey key, byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(ciphertext);
    }
    //HMAC
    public static String calculateRFC2104HMAC(String data, String key) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }
    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String retVal = formatter.toString();
        formatter.close();
        return retVal;
    }
    
    
}
