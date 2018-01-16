package V1.Frontend;

import java.io.File;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Base58;
import V1.Library.IO;
import V1.Library.TofuError;
import V1.Library.TofuError.*;
import V1.Library.TofuException.AddressFormatException;

public class Setting {
	static final String BLOCKCHAIN_BIN_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator
			+ "data" + File.separator + "blockchain" + File.separator;
	static final String TRUSTED_FRONTEND_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator
			+ "data" + File.separator + "frontendServer" + File.separator;
	static final String TRUSTED_BACKEND_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator
			+ "data" + File.separator + "backendServer" + File.separator;

	static final boolean BROADCAST_FRONTEND = false;
	static final boolean BROADCAST_BACKEND = true;

	private static final String address = "66UHD9Ac8AWc4yUddFhMRkPtHcWU2Q4dp1s1PZVrm2QWkaUPK8GFWY5u7rt9FR3t8tHP2dwgbqgwgXXG9ym8Y13E";
	// private static final String privateKey =
	// "63CF6591947F69D40E7E3ABCE6FCE7466B0339A6B03C248346A07C489938A906";
	// private static final String publicKey =
	// "0413433316B2BD3B861B509DAB0C99F6867391E72ADB29693ED6168ECC2873F21E15944C8FC970F0E9582382CA0DC991460C01E4297CC921382A5FF21D4E933413";
	private static byte[] byteAddress;

	static void init() {
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
		try {
			byteAddress = Base58.decode(address);
		} catch (AddressFormatException e) {
			e.printStackTrace();
			throw new TofuError.SettingError("Address invalid");
		}
	}

	static byte[] getByteAddress() {
		return byteAddress;
	}
	static String getAddress() {
		return address;
	}
}
