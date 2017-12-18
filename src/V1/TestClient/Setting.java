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

import V1.Library.Base58;
import V1.Library.Constant;
import V1.Library.Log;
import V1.Library.TofuError.SettingError;
import V1.Library.TofuException.AddressFormatException;

public class Setting {
	static final String BLOCKCHAIN_BIN_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator + "data"+File.separator + "blockchain"+File.separator;
	static final String TRUSTED_FRONTEND_DIR = System.getProperty("user.dir") +File.separator+".." + File.separator + "data"+File.separator+"frontendServer"+File.separator;
	static final String TRUSTED_BACKEND_DIR = System.getProperty("user.dir") +File.separator+".." + File.separator + "data"+File.separator+"backendServer"+File.separator;

	private static final String address = "Be4qVLKM2PtucWukmUUc6s2CrcbQNH7PRnMbcssMwG6S";
	private static final String privateKey = "B7977D40B2A3B73D12E23609DC29F8900C27EAD559D8457E019BBE0EE2F4948E";
	private static final String publicKey = "229A26A100180C73A8571AC266047200591A08713C1CE73D97A53E837CE336EF40FDDF59442F74574A5148EDD2FFEC1F50C3E52454A0A6A397B968D0FC5B5825";

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
