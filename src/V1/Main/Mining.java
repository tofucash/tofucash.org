package V1.Main;

import V1.Component.Block;
import V1.Component.Work;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;

public class Mining {
	static void updateMining(Block block) {
		Work work;
		try {
			work = new Work(Crypto.hash512(ByteUtil.getByteObject(block.getBlockHeader())), Crypto.hash512(ByteUtil.getByteObject(block.getDifficulty())));
			BackendServer.shareFrontend(work);
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("invalid data", Constant.Log.EXCEPTION);
		}
	}
}
