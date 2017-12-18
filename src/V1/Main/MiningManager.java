package V1.Main;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Block;
import V1.Component.NetworkObject;
import V1.Component.Report;
import V1.Component.Work;
import V1.Frontend.FrontendServer;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;
import V1.Library.Mining;
import net.arnx.jsonic.web.WebServiceServlet.JSON;

public class MiningManager {
	private static Work work;
	static void init() {
		byte[] decoy = new byte[Constant.Work.BYTE_MAX_HASH];
		Arrays.fill(decoy, (byte) 0xff);
		work = new Work(decoy, decoy);
		Log.log("MiningManager init done.");
	}

	static void updateMining(Block block) {
		try {
			work = new Work(Crypto.hash512(ByteUtil.getByteObject(block.getBlockHeader())), Crypto.hash512(ByteUtil.getByteObject(block.getTarget())));
			BackendServer.shareFrontend(work);
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[MiningManager.updateMining()] Invalid block", Constant.Log.EXCEPTION);
		}
	}
	static boolean verifyMining(Report report) {
		return Mining.verifyMining(work, report);
	}
}
