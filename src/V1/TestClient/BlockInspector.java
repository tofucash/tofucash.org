package V1.TestClient;

import java.io.File;

import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.IO;
import V1.Library.Log;

public class BlockInspector {
	public static void main(String[] args) {
		int blockHeight = 1;
		String blockFilePath = Setting.BLOCKCHAIN_BIN_DIR + +(blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR)
				+ File.separator+blockHeight;
		try {
			Log.log(ByteUtil.convertByteToObject(IO.readFileToByte(blockFilePath)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
