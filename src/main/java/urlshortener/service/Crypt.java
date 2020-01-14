package urlshortener.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class Crypt {

    // Any key of 16 characters
    private static String key = "IngenieriaWeb150";

    /**
     * Encrypt the text with the AES protocol and key
     * @param strClearText plain text to encryp
     * @return the text ecrypted
     * @throws Exception
     */
    public static String encrypt(String strClearText){
        String strData="";

        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher=Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted=cipher.doFinal(strClearText.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b: encrypted) {
                sb.append((char)b);
            }

            strData = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return strData;
    }

    /**
     * Decrypt the strecrnypted text
     * @param strEncrypted encrypted text to decrypt
     * @return the plain text
     * @throws Exception
     */
    public static String decrypt(String strEncrypted){
        String strData="";

        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher=Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] bb = new byte[strEncrypted.length()];
            for (int i=0; i<strEncrypted.length(); i++) {
                bb[i] = (byte) strEncrypted.charAt(i);
            }
            strData = new String(cipher.doFinal(bb));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return strData;
    }
}
