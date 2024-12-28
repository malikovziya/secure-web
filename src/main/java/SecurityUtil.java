import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import java.util.Base64;

public class SecurityUtil {

    private static final String SECRET_KEY = System.getenv("SECRET_KEY");
    private static final String HMAC_KEY = System.getenv("HMAC_KEY");

    // Encrypt data using AES
    public static String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // Decrypt data using AES
    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData);
    }

    // Sign the data using HMAC
    public static String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(HMAC_KEY.getBytes(), "HmacSHA256");
        mac.init(secretKey);
        byte[] signature = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(signature);
    }

    // Verify HMAC signature
    public static boolean verifySignature(String data, String signature) throws Exception {
        String calculatedSignature = sign(data);
        return calculatedSignature.equals(signature);
    }
}
