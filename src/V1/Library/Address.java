package V1.Library;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;

public class Address {

	public static byte[] getAddress(PublicKey publicKey) {
		return Crypto.hashTwice(publicKey.getEncoded());
	}

	public static byte[] createPrivateKey() {
		SecureRandom random;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
			byte token[] = new byte[Constant.Address.BYTE_PRIVATE_KEY];
			random.nextBytes(token);
			return token;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new TofuError.SettingError("SecureRandom algorithm not found.");
		}
	}

//	public static KeyPair test() {
//		KeyPair apair = null;
//		try {
//			KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
//			ECGenParameterSpec gps = new ECGenParameterSpec("secp256k1"); // NIST
//																			// P-256
//			kpg.initialize(gps);
//			apair = kpg.generateKeyPair();
//			ECPublicKey apub = (ECPublicKey) apair.getPublic();
//			ECParameterSpec aspec = apub.getParams();
//			// could serialize aspec for later use (in compatible JRE)
//			//
//			// for test only reuse bogus pubkey, for real substitute values
//			ECPoint apoint = apub.getW();
//			BigInteger x = apoint.getAffineX(), y = apoint.getAffineY();
//			// construct point plus params to pubkey
//			ECPoint bpoint = new ECPoint(x, y);
//			ECPublicKeySpec bpubs = new ECPublicKeySpec(bpoint, aspec);
//			KeyFactory kfa = KeyFactory.getInstance("EC");
//			ECPublicKey bpub = (ECPublicKey) kfa.generatePublic(bpubs);
//			//
//			// for test sign with original key, verify with reconstructed key
//			Signature sig = Signature.getInstance("SHA256withECDSA");
//			byte[] data = "test".getBytes();
//			sig.initSign(apair.getPrivate());
//			sig.update(data);
//			byte[] dsig = sig.sign();
//			sig.initVerify(bpub);
//			sig.update(data);
//			System.out.println(sig.verify(dsig));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return apair;
//	}

	public static KeyPair createKeyPair() {
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("EC");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
			keyGen.initialize(ecSpec, new SecureRandom());
			return keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			throw new TofuError.SettingError("SecureRandom algorithm not found.");
		}
	}
}
