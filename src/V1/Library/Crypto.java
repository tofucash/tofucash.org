package V1.Library;

import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/*
 * http://tech-gym.com/2012/11/android/1055.html
 */

public class Crypto {
	// public public static final String ALGO = "hmacSHA512";
	// public public static String get(String data, String key) {
	// try {
	// SecretKeySpec sk = new SecretKeySpec(key.getBytes(), ALGO);
	// Mac mac = Mac.getInstance(ALGO);
	// mac.init(sk);
	//
	// byte[] mac_bytes = mac.doFinal(data.getBytes());
	//
	// StringBuilder sb = new StringBuilder(2 * mac_bytes.length);
	// for(byte b: mac_bytes) {
	// sb.append(String.format("%02x", b&0xff) );
	// }
	// return String.valueOf(sb);
	// } catch (Exception e) {}
	// return "";
	// }
	// public public static byte[] getByte(byte[] data, String key) {
	// SecretKeySpec sk = new SecretKeySpec(key.getBytes(), ALGO);
	// byte[] hash = null;
	// Mac mac;
	// try {
	// mac = Mac.getInstance(ALGO);
	// mac.init(sk);
	// hash = mac.doFinal(data);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return hash;
	// }
	public static byte[] hash(byte[] data, String algo) {
		MessageDigest md = null;
		// StringBuilder sb = null;
		byte[] hash = new byte[512];
		try {
			md = MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(data);
		hash = md.digest();
		// sb = new StringBuilder();
		// for (byte b : md.digest()) {
		// String hex = String.format("%02x", b);
		// sb.append(hex);
		// }
		// return sb.toString().getBytes();
		return hash;
	}

	public static byte[] hash256(byte[] data) {
		return hash(data, "SHA-256");
	}

	// public static byte[] hash160(byte[] data) {
	// return hash(data, "RIPEMD160");
	// }

	public static byte[] hashTwice(byte[] data) {
		return hash256(hash256(data));
	}

//	public static byte[] signature(byte[] data, PrivateKey key) {
//		try {
//			// Signatureの初期化
//			Signature signAlg = Signature.getInstance(alg);
//			signAlg.initSign(key);
//			signAlg.update(data);
//			// 署名の生成
//			byte[] sign = signAlg.sign();
//			return sign;
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		byte[] bytes = null;
//		return bytes;
//
//	}

	public static byte[] sign(PrivateKey privateKey, PublicKey publicKey, byte[] data) {
		Signature dsa;
		byte[] signature = null;
		try {
			dsa = Signature.getInstance(Constant.Crypto.SIGN_ALGO);
			dsa.initSign(privateKey);
			dsa.update(data);
			signature = dsa.sign();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}
		return signature;
	}

	public static boolean verify(byte[] publicKeyByte, byte[] data, byte[] signature) {
		Signature dsa;
		boolean result = false;
		try {
			dsa = Signature.getInstance(Constant.Crypto.SIGN_ALGO);
			PublicKey publicKey = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(publicKeyByte));
			dsa.initVerify(publicKey);
			dsa.update(data);
			result = dsa.verify(signature);
		} catch (KeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return result;
	}
}