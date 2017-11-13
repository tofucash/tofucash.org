package V1.Main;

import V1.Library.IO;
import V1.Library.TofuError.*;

public class Setting {
	static final String BLOCKCHAIN_BIN_DIR = System.getProperty("user.dir") + "\\data\\blockchain\\"; // windows
	// static final String blockchainBinDir =
	// System.getProperty("user.dir")+"/data/blockchain/"; // linux
	static final String TRUSTED_SERVER_DIR = System.getProperty("user.dir") + "\\data\\trustedServer\\"; // windows
	// static final String trustedServerListFile =
	// System.getProperty("user.dir")+"/data/trustedServer/"; // linux
	
	static final boolean BROADCAST_FRONTEND = false;
	static final boolean BROADCAST_BACKEND = false;

	static void init() {
		if (!IO.isDirectory(BLOCKCHAIN_BIN_DIR)) {
			throw new SettingError("BlockchainBinDir [" + BLOCKCHAIN_BIN_DIR + "] is not directory or doesn't exists.");
		}
		if (!IO.isDirectory(TRUSTED_SERVER_DIR)) {
			throw new SettingError(
					"TrustedServerDir [" + TRUSTED_SERVER_DIR + "] is not directory or doesn't exists.");
		}
	}
}
