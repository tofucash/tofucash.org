package V1.TestClient;

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

import V1.Library.Address;
import V1.Library.Base58;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;
import V1.Library.TofuError.SettingError;
import V1.Library.TofuException.AddressFormatException;

public class Setting {
	static final String BLOCKCHAIN_BIN_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator + "data"+File.separator + "blockchain"+File.separator;
	static final String TRUSTED_FRONTEND_DIR = System.getProperty("user.dir") +File.separator+".." + File.separator + "data"+File.separator+"frontendServer"+File.separator;
	static final String TRUSTED_BACKEND_DIR = System.getProperty("user.dir") +File.separator+".." + File.separator + "data"+File.separator+"backendServer"+File.separator;

	private static final String address = "66UHD9Ac8AWc4yUddFhMRkPtHcWU2Q4dp1s1PZVrm2QWkaUPK8GFWY5u7rt9FR3t8tHP2dwgbqgwgXXG9ym8Y13E";
	private static final String privateKey = "63CF6591947F69D40E7E3ABCE6FCE7466B0339A6B03C248346A07C489938A906";
	private static final String publicKey = "0413433316B2BD3B861B509DAB0C99F6867391E72ADB29693ED6168ECC2873F21E15944C8FC970F0E9582382CA0DC991460C01E4297CC921382A5FF21D4E933413";

	private static byte[] byteAddress;
	private static byte[] bytePublicKey;
	private static KeyPair keyPair;
	
	static void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
//		Log.log("length "+DatatypeConverter.parseHexBinary("303E020100301006072A8648CE3D020106052B8104000A042730250201010420").length);
//		Log.log("length "+DatatypeConverter.parseHexBinary("3056301006072A8648CE3D020106052B8104000A03420004").length);
		try {
			byteAddress = Base58.decode(address);
		} catch (AddressFormatException e) {
			throw new SettingError("Wrong Address Format.");
		}

		bytePublicKey = DatatypeConverter.parseHexBinary(publicKey);
		
		KeyFactory kf = KeyFactory.getInstance("EC");
		PrivateKey prvk = kf.generatePrivate(new PKCS8EncodedKeySpec(DatatypeConverter.parseHexBinary(Constant.Address.PRIVATE_KEY_PREFIX + privateKey)));
		PublicKey pubk = kf.generatePublic(new X509EncodedKeySpec(DatatypeConverter.parseHexBinary(Constant.Address.PUBLIC_KEY_PREFIX + publicKey)));
		keyPair = new KeyPair(pubk, prvk);
	}
	static void testSetKey() {
		keyPair = Address.createKeyPair();
		bytePublicKey = keyPair.getPublic().getEncoded();
		byteAddress = Address.getAddress(keyPair.getPublic());
	}
	static byte[] getAddress() {
		return byteAddress;
	}
	static byte[] getPublicKey() {
		return bytePublicKey;
	}
	static KeyPair getKeyPair() {
		return keyPair;
	}
}
