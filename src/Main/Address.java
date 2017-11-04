package Main;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;

public class Address {
	
	static boolean createAddress() {
		return false;
	}

//	static byte[] createPrivateKey() {
//		SecureRandom random;
//		try {
//			random = SecureRandom.getInstance("SHA256withECDSA");
//			byte token[] = new byte[Constant.Address.BYTE_PRIVATE_KEY];
//			random.nextBytes(token);
//			return token;
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//			throw new TofuException.SettingError("SecureRandom algorithm not found.");
//		}
//	}
	
	static KeyPair createKeyPair() {
		    KeyPairGenerator keyGen = null;
			try {
				keyGen = KeyPairGenerator.getInstance("EC");
			    ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
			    keyGen.initialize(ecSpec, new SecureRandom());
			    return keyGen.generateKeyPair();
			} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException  e) {
				e.printStackTrace();
				throw new TofuException.SettingError("SecureRandom algorithm not found.");
			}
	}
}
