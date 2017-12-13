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
import V1.Library.TofuError;
import net.arnx.jsonic.web.WebServiceServlet.JSON;

public class MiningManager {
	private static Work work;

	static void init() {
		byte[] decoy = new byte[Constant.Work.BYTE_MAX_HASH];
		Arrays.fill(decoy, (byte) 0xff);
		work = new Work(decoy, decoy);
		Log.log("MiningManager init done.");
	}

	static void receptWork(final NetworkObject no, PrintWriter pw) {
		if (no.getType() == Constant.NetworkObject.WORK_REQUEST) {
			work = no.getWork();
			Log.log("work update: " + work);
		} else if (no.getType() == Constant.NetworkObject.WORK_UNDERTAKE) {
			throw new TofuError.UnimplementedError("old if branch");
			// pw.println("{hash: " +
			// DatatypeConverter.printHexBinary(work.getHash()) + ", difficulty:
			// " + DatatypeConverter.printHexBinary(work.getDifficulty()) +
			// "}");
			// pw.flush();
			// Log.log("send back: {hash: " +
			// DatatypeConverter.printHexBinary(work.getHash()) + ", difficulty:
			// " + DatatypeConverter.printHexBinary(work.getDifficulty()) +
			// "}");
		}
	}

	static Work getWork() {
		return work;
	}

	static void receptNonce(String json) {
		Map map = (Map) JSON.decode(json);
		byte[] hash = DatatypeConverter.parseHexBinary((String) map.get("hash"));
		byte[] nonce = DatatypeConverter.parseHexBinary((String) map.get("nonce"));
		byte[] result = DatatypeConverter.parseHexBinary((String) map.get("result"));
		byte[] miner = DatatypeConverter.parseHexBinary((String) map.get("miner"));
		String dataStr = DatatypeConverter.printHexBinary(work.getHash()) + DatatypeConverter.printHexBinary(nonce);
		
		byte[] checkHash = Crypto.hash512(DatatypeConverter.parseHexBinary(dataStr));
		Log.log("checkHash: " + DatatypeConverter.printHexBinary(checkHash));
		Log.log("result: " + DatatypeConverter.printHexBinary(result));
		
		byte[] resultHead = new byte[8];
		byte[] difficultyHead = new byte[8];
		System.arraycopy(result, 0, resultHead, 0, 8);
		System.arraycopy(work.getDifficulty(), 0, difficultyHead, 0, 8);
		
		// TODO: compare each byte 

		if (Arrays.equals(result, checkHash)) {
			Log.log("hash OK!");
			Log.log("ByteBuffer.wrap(hashHead).getLong(): " + ByteBuffer.wrap(resultHead).getLong(), Constant.Log.TEMPORARY);
			Log.log("ByteBuffer.wrap(difficultyHead).getInt(): " + ByteBuffer.wrap(difficultyHead).getLong(), Constant.Log.TEMPORARY);
			if (ByteBuffer.wrap(resultHead).getLong() < ByteBuffer.wrap(difficultyHead).getLong()) {
				Log.log("mining OK!");
				NetworkObject no = new NetworkObject(Constant.NetworkObject.REPORT, new Report(work.getHash(), nonce, result, miner));
				FrontendServer.shareBackend(no);
			} else {
				Log.log("mining FAIL!", Constant.Log.INVALID);				
			}
		} else {
			Log.log("mining INVALID!", Constant.Log.INVALID);
		}
	}
}
