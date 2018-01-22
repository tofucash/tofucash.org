package V1.Frontend;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import V1.Component.NetworkObject;
import V1.Component.Report;
import V1.Component.Work;
import V1.Library.Base58;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;
import V1.Library.Verify;
import V1.Library.Constant.Verify.Result;
import V1.Library.TofuError;
import net.arnx.jsonic.web.WebServiceServlet.JSON;

public class MiningManager {
	private static Work work;
	private static Map<ByteBuffer, List<Report>> reportMap;
	private static List<byte[]> nonceList;
	private static Map<String, byte[]> ipNonceMap;
	static void init() {
		byte[] decoy = new byte[Constant.Block.BYTE_BLOCK_HASH];
		byte[] target = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
		byte[] subTarget = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_SUB_TARGET);
		// Arrays.fill(decoy, (byte) 0xff);
		work = new Work(decoy, target, subTarget, Setting.getByteAddress());
		reportMap = new HashMap<ByteBuffer, List<Report>>();
		nonceList = new ArrayList<byte[]>();
		nonceList.add(new byte[Constant.Block.BYTE_NONCE]);
		ipNonceMap = new HashMap<String, byte[]>();
		Log.log("MiningManager init done.");
	}

	static void receptWork(final NetworkObject no) {
		work = no.getWork();
		work.setFAddress(Setting.getByteAddress());
		nonceList.clear();
		ipNonceMap.clear();
		// nonceListを埋める
		for(int i = 0; i < Constant.Mining.MAX_CLIENT_NODE; i++) {
			nonceList.add(Crypto.hash512(DatatypeConverter.parseHexBinary(DatatypeConverter.printHexBinary(work.getHash()) + String.format("%04d", i) + DatatypeConverter.printHexBinary(Setting.getByteAddress()))));
		}
		Log.log("[MiningManager.receptWork()] work update: " + work);
	}

	static Work getWork() {
		return work;
	}
	static byte[] getNextNonce(String ipAddress) {
		if(!nonceList.isEmpty()) {
			byte[] nonce = nonceList.remove(0);
			ipNonceMap.put(ipAddress, nonce);
			Log.log("ip: " + ipAddress + "\t nonce: " + DatatypeConverter.printHexBinary(nonce));
			return nonce;
		} else {
			return new byte[Constant.Block.BYTE_NONCE];
		}
	}

	static Report verifyMining(String json, String ipAddress) {
		// Reportオブジェクトか判定
		Report report = null;
		try {
			report =  (Report) JSON.decode(json, Report.class);
			Log.log("[MiningManager.verifyMining()] report: " + report, Constant.Log.TEMPORARY);
		} catch (Exception e) {
			Log.log("[MiningManager.verifyMining()] Not Answer JSON");
			e.printStackTrace();
			return null;
		}
		if(report == null || report.getHash() == null) {
			return null;
		}
		// 自分のF層アドレスが含まれているか確認
		if(report.getFAddress() != null &&!ByteBuffer.wrap(DatatypeConverter.parseHexBinary(report.getFAddress())).equals(ByteBuffer.wrap(Setting.getByteAddress()))) {
			return null;
		}
		// nonceが与えた範囲に収まっているか確認
		if(!ipNonceMap.containsKey(ipAddress)) {
			Log.log("[MiningManager.verifyMining()] Invalid Nonce", Constant.Log.INVALID);
			return null;
		}
		BigInteger tmp = new BigInteger(ipNonceMap.get(ipAddress));
		BigInteger nonceNum = new BigInteger(DatatypeConverter.parseHexBinary(report.getNonce()));
		if(nonceNum.compareTo(tmp) > 0) {
			tmp = tmp.add(BigInteger.valueOf(Constant.Server.NONCE_CNT));
			if(nonceNum.compareTo(tmp) > 0) {
				Log.log("[MiningManager.verifyMining()] Invalid Nonce", Constant.Log.INVALID);
				return null;
			}
		} else {
			Log.log("[MiningManager.verifyMining()] Invalid Nonce", Constant.Log.INVALID);
			return null;
		}
		// Reportが正しいか確認
		Result result;
		if((result = Verify.verifyMining(work, report)) == Result.FAIL) {
			return null;
		} else {
			return report;
		}
		
		// 以下 他ノードにハッシュをチェックしてもらう処理
//		// すでに見つかったnonceか確認
//		// そうでなければworkをこのrequestのnonceに変更してResult.CHECKをreturn
//		ByteBuffer buf = ByteBuffer.wrap(work.getHash());
//		if(reportMap.containsKey(buf)) {
//			List<Report> list = reportMap.get(buf);
//			for(Report tmp: list) {
//				if(tmp.getNonce() == report.getNonce() && tmp.getHash() == report.getHash() && tmp.getCAddress() != report.getCAddress()) {
//					return Result.TARGET;
//				}
//			}
//		} else {
//			List<Report> list = new ArrayList<Report>();;
//			list.add(report);
//			reportMap.put(buf, list);
//		}
//		for(int i = 0; i < Constant.Mining.NONCE_CHECK_NODE; i++) {
//			nonceList.add(DatatypeConverter.parseHexBinary(report.getNonce()));
//		}		
	}
}
