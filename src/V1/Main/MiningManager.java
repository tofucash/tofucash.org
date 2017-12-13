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
			work = new Work(Crypto.hash512(ByteUtil.getByteObject(block.getBlockHeader())), Crypto.hash512(ByteUtil.getByteObject(block.getDifficulty())));
			BackendServer.shareFrontend(work);
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("invalid data", Constant.Log.EXCEPTION);
		}
	}
	static boolean verfiyMining(Report report) {
		byte[] hash = report.getHash();
		if(!Arrays.equals(hash, work.getHash())) {
			Log.log("Old hash");
			return false;
		}

		byte[] nonce = report.getNonce();
		byte[] result = report.getResult();
		byte[] miner = report.getMiner();
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
				Blockchain.nonceFound(nonce, miner);
			} else {
				Log.log("mining FAIL!", Constant.Log.INVALID);				
			}
		} else {
			Log.log("mining INVALID!", Constant.Log.INVALID);
		}
		return false;
	}
}
