package V1.Library;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*
 * http://tech-gym.com/2012/11/android/1055.html
 */

public class Hash {
	public static final String ALGO = "hmacSHA512";
//	public static String get(String data, String key) {
//        try {
//                SecretKeySpec sk = new SecretKeySpec(key.getBytes(), ALGO);
//                Mac mac = Mac.getInstance(ALGO);
//                mac.init(sk);
//
//                byte[] mac_bytes = mac.doFinal(data.getBytes());
//
//                StringBuilder sb = new StringBuilder(2 * mac_bytes.length);
//                for(byte b: mac_bytes) {
//                        sb.append(String.format("%02x", b&0xff) );
//                }
//                return String.valueOf(sb);
//		} catch (Exception e) {}
//		return "";
//	}
	public static byte[] getByte(byte[] data, String key) {
        SecretKeySpec sk = new SecretKeySpec(key.getBytes(), ALGO);
        byte[] hash = null;
        Mac mac;
		try {
			mac = Mac.getInstance(ALGO);
	        mac.init(sk);
	        hash = mac.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return hash;
	}
}