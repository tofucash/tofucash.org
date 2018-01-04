package V1.Frontend;

import java.io.File;

import V1.Library.IO;
import V1.Library.TofuError.*;

public class Setting {
	static final String BLOCKCHAIN_BIN_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator + "data"+File.separator + "blockchain"+File.separator;
	static final String TRUSTED_FRONTEND_DIR = System.getProperty("user.dir") +File.separator+".." + File.separator + "data"+File.separator+"frontendServer"+File.separator;
	static final String TRUSTED_BACKEND_DIR = System.getProperty("user.dir") +File.separator+".." + File.separator + "data"+File.separator+"backendServer"+File.separator;
	
	static final boolean BROADCAST_FRONTEND = false;
	static final boolean BROADCAST_BACKEND = true;

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
	}
}
