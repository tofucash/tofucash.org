package V1.Frontend;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import V1.Component.NetworkObject;
import V1.Component.Report;
import V1.Component.Work;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;
import V1.Library.Mining;
import V1.Library.TofuError;
import net.arnx.jsonic.web.WebServiceServlet.JSON;

public class MiningManager {
	private static Work work;

	static void init() {
		byte[] decoy = new byte[Constant.Work.BYTE_MAX_HASH];
		byte[] target = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
//		Arrays.fill(decoy, (byte) 0xff);
		work = new Work(decoy, target);
		Log.log("MiningManager init done.");
	}

	static void receptWork(final NetworkObject no, PrintWriter pw) {
		work = no.getWork();
		Log.log("[MiningManager.receptWork()] work update: " + work);
	}

	static Work getWork() {
		return work;
	}

	static Report verifyMining(String json) {
		Map map = (Map) JSON.decode(json);
		byte[] hash = DatatypeConverter.parseHexBinary((String) map.get("hash"));
		byte[] nonce = DatatypeConverter.parseHexBinary((String) map.get("nonce"));
		byte[] result = DatatypeConverter.parseHexBinary((String) map.get("result"));
		byte[] miner = DatatypeConverter.parseHexBinary((String) map.get("miner"));
		Report report = new Report(hash, nonce, result, miner);
		if(Mining.verifyMining(work, report)) {
			return report;
		} else {
			return null;
		}
	}
}
