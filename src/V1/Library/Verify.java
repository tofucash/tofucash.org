package V1.Library;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import V1.Component.NetworkObject;
import V1.Component.Report;
import V1.Component.Request;
import V1.Component.Work;
import V1.Library.Constant.Verify.Result;
import net.arnx.jsonic.JSON;

public class Verify {
	public static Result verifyMining(Work work, Report report) {
		if(work == null || report == null) {
			return Result.FAIL;
		}
		
		String hash = report.getHash();
		if(!hash.equals(DatatypeConverter.printHexBinary(work.getHash()))) {
			Log.log("report.hash: " + report.getHash());
			Log.log("  work.hash: " + DatatypeConverter.printHexBinary(work.getHash()));
			Log.log("[Mining.verifyMining()] Unnecessary Hash", Constant.Log.IMPORTANT); 
			return Result.FAIL;
		}
		String nonce = report.getNonce();
		String result = report.getResult();
		String dataStr = DatatypeConverter.printHexBinary(work.getHash()) + nonce + report.getFAddress() + report.getCAddress();
		byte[] checkHash = Crypto.hash512(DatatypeConverter.parseHexBinary(dataStr));
		
		BigInteger resultNum = new BigInteger(DatatypeConverter.parseHexBinary(result));
		BigInteger targetNum = new BigInteger(work.getTarget());
		BigInteger subTargetNum = new BigInteger(work.getSubTarget());
		

		if (result.equals(DatatypeConverter.printHexBinary(checkHash))) {
			Log.log("[Mining.verifyMining()] Hash Valid!");
			Log.log("target: " + DatatypeConverter.printHexBinary(targetNum.toByteArray()), Constant.Log.TEMPORARY);
			Log.log("result: " + DatatypeConverter.printHexBinary(resultNum.toByteArray()), Constant.Log.TEMPORARY);
			if (resultNum.compareTo(targetNum) < 0) {
				Log.log("[Mining.verifyMining()] Mining Success! (Target)");
				return Result.TARGET;
			} else if (resultNum.compareTo(subTargetNum) < 0) {
				Log.log("[Mining.verifyMining()] Mining Success! (SubTarget)");
				return Result.SUB_TARGET;
			} else {
				Log.log("[Mining.verifyMining()] Mining Failed!", Constant.Log.INVALID);
			}
		} else {
			Log.log("[Mining.verifyMining()] Hash Invalid!", Constant.Log.INVALID);
		}
		return Result.FAIL;
	}
	public static boolean verifyRequest(Request request) {
		String signature = request.getSignature();
		request.setSignature(request.getPublicKey());
		String txStr = JSON.encode(request);
		request.setSignature(signature);
		return Crypto.verify(Address.getPublicKeyFromByte(DatatypeConverter.parseHexBinary(request.getPublicKey()))
				, txStr.getBytes(), DatatypeConverter.parseHexBinary(signature));
	}
	public static boolean verifyReport(Report report) {
		String signature = report.getSignature();
		report.setSignature(report.getPublicKey());
		String txStr = JSON.encode(report);
		report.setSignature(signature);
		return Crypto.verify(Address.getPublicKeyFromByte(DatatypeConverter.parseHexBinary(report.getPublicKey()))
				, txStr.getBytes(), DatatypeConverter.parseHexBinary(signature));		
	}
}
