package V1.Main;

import java.io.File;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.IO;
import V1.Library.TofuError.*;

public class Setting {
	static final String BLOCKCHAIN_BIN_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator
			+ "data" + File.separator + "blockchain" + File.separator;
	static final String TRUSTED_FRONTEND_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator
			+ "data" + File.separator + "frontendServer" + File.separator;
	static final String TRUSTED_BACKEND_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator
			+ "data" + File.separator + "backendServer" + File.separator;

	static final boolean BROADCAST_FRONTEND = true;
	static final boolean BROADCAST_BACKEND = true;
	private static final String address = "8C4uHzpdVq5ZmCZxBWdbNt3L91pfEqxwS8AHsVwEMEnE552oyTz8YpwSvdZFuWQ2mTjLCjcH15Zjd9yttDMtNxBvE2ToZFcmrejx2TwbRrgyexJEtUX43E6T5dGh2xSk9vD4fG8f2RGMEARxiAq823ZmsKu8j8trDVFYpJmZfwx2ijS";
	private static final String privateKey = "63CF6591947F69D40E7E3ABCE6FCE7466B0339A6B03C248346A07C489938A906";
	private static final String publicKey = "0413433316B2BD3B861B509DAB0C99F6867391E72ADB29693ED6168ECC2873F21E15944C8FC970F0E9582382CA0DC991460C01E4297CC921382A5FF21D4E933413";
	private static byte[] byteAddress;
	private static byte[] bytePublicKey;
	private static KeyPair keyPair;

	static void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (!IO.isDirectory(BLOCKCHAIN_BIN_DIR)) {
			throw new SettingError("BlockchainBinDir [" + BLOCKCHAIN_BIN_DIR + "] is not directory or doesn't exists.");
		}
		if (!IO.isDirectory(TRUSTED_FRONTEND_DIR)) {
			throw new SettingError(
					"FrontendServerDir [" + TRUSTED_FRONTEND_DIR + "] is not directory or doesn't exists.");
		}
		if (!IO.isDirectory(TRUSTED_BACKEND_DIR)) {
			throw new SettingError(
					"BackendServerDir [" + TRUSTED_BACKEND_DIR + "] is not directory or doesn't exists.");
		}
		bytePublicKey = DatatypeConverter.parseHexBinary(publicKey);
		KeyFactory kf = KeyFactory.getInstance("EC");
		PrivateKey prvk = kf.generatePrivate(new PKCS8EncodedKeySpec(DatatypeConverter.parseHexBinary(Constant.Address.PRIVATE_KEY_PREFIX + privateKey)));
		PublicKey pubk = kf.generatePublic(new X509EncodedKeySpec(DatatypeConverter.parseHexBinary(Constant.Address.PUBLIC_KEY_PREFIX + publicKey)));
		keyPair = new KeyPair(pubk, prvk);

	}
	static byte[] getBytePublicKey() {
		return bytePublicKey;
	}
	static KeyPair getKeyPair() {
		return keyPair;
	}
}
