package V1.Library;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Report;
import V1.Component.Work;

public class Mining {
	public static boolean verifyMining(Work work, Report report) {
		byte[] hash = report.getHash();
		if(!Arrays.equals(hash, work.getHash())) {
			Log.log("[MiningManager.verifyMining()] Unnecessary Hash", Constant.Log.IMPORTANT); 
			return false;
		}

		byte[] nonce = report.getNonce();
		byte[] result = report.getResult();
		byte[] miner = report.getMiner();
		String dataStr = DatatypeConverter.printHexBinary(work.getHash()) + DatatypeConverter.printHexBinary(nonce);
		
		byte[] checkHash = Crypto.hash512(DatatypeConverter.parseHexBinary(dataStr));
//		Log.log("checkHash: " + DatatypeConverter.printHexBinary(checkHash));
//		Log.log("result: " + DatatypeConverter.printHexBinary(result));
		
		// TODO: compare each byte 
		byte[] resultHead = new byte[8];
		byte[] targetHead = new byte[8];
		System.arraycopy(result, 0, resultHead, 0, 8);
		System.arraycopy(work.getTarget(), 0, targetHead, 0, 8);
		

		if (Arrays.equals(result, checkHash)) {
			Log.log("[MiningManger.verifyMining()] Hash Valid!");
			Log.log("ByteBuffer.wrap(hashHead).getLong(): " + ByteBuffer.wrap(resultHead).getLong(), Constant.Log.TEMPORARY);
			Log.log("ByteBuffer.wrap(targetHead).getInt(): " + ByteBuffer.wrap(targetHead).getLong(), Constant.Log.TEMPORARY);
			if (ByteBuffer.wrap(resultHead).getLong() < ByteBuffer.wrap(targetHead).getLong()) {
				Log.log("[MiningManger.verifyMining()] Mining Success!");
				return true;
			} else {
				Log.log("[MiningManger.verifyMining()] Mining Failed!", Constant.Log.INVALID);
			}
		} else {
			Log.log("[MiningManger.verifyMining()] Hash Invalid!", Constant.Log.INVALID);
		}
		return false;
	}
}
