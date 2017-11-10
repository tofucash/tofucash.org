package V1.Main;

import V1.Library.IO;
import V1.Library.TofuError.*;
public class Setting {	
	static final String blockchainBinDir = System.getProperty("user.dir")+"\\data\\blockchain\\";	// windows
//	static final String blockchainBinDir = "./data/blockchain/";	// others
	
	static void init() {
		if(!IO.isDirectory(blockchainBinDir)) {
			throw new SettingError("Blockchain Bin Dir is not directory or doesn't exists.");
		}
	}
}
