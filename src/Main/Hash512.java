package Main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash512 {
	static byte[] hash(byte[] data) {
		    MessageDigest md = null;
//		    StringBuilder sb = null;
		    byte[] hash = new byte[512]; 
		    try {
		        md = MessageDigest.getInstance("SHA-512");
		    } catch (NoSuchAlgorithmException e) {
		        e.printStackTrace();
		    }
		    md.update(data);
		    hash = md.digest();
//		    sb = new StringBuilder();
//		    for (byte b : md.digest()) {
//		        String hex = String.format("%02x", b);
//		        sb.append(hex);
//		    }
//		    return sb.toString().getBytes();
		    return hash;
	}
	
	static byte[] hashTwice(byte[] data) {
		return hash(hash(data));
	}
}
