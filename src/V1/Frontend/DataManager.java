package V1.Frontend;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Answer;
import V1.Component.Block;
import V1.Component.Input;
import V1.Component.NetworkObject;
import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Request;
import V1.Component.Routine;
import V1.Component.Spent;
import V1.Component.Transaction;
import V1.Component.UTXO;
import V1.Library.Base58;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;
import V1.Library.TofuException.AddressFormatException;
import V1.Library.Verify;
import net.arnx.jsonic.JSON;

public class DataManager extends Thread {
	private static UTXO utxoTable;
	// nextBlockのutxoの消費差分
	private static Spent utxoTableUsed;
	private static Map<ByteBuffer, Map<ByteBuffer, String>> txInfoTable;

	private static Map<String, Map<String, String>> myRoutineTable;
	private static Map<String, Map<String, String>> routineTable;
	private static List<Request> requestTable;
	private static Map<Integer, Block> blockTable;

	static void init() {
		utxoTable = new UTXO();
		utxoTableUsed = new Spent();
		txInfoTable = new HashMap<ByteBuffer, Map<ByteBuffer, String>>();
		myRoutineTable = new HashMap<String, Map<String, String>>();
		routineTable = new HashMap<String, Map<String, String>>();
		requestTable = new ArrayList<Request>();
	}

	static String getBalance(Request request) {
		String json = "";
		String[] addr = request.getAddrFrom();
		List<List<Map<String, String>>> hashBalance = new ArrayList<List<Map<String, String>>>();
		int i = 0;
		for (i = 0; i < addr.length; i++) {
			Map<ByteBuffer, Output> utxoMap;
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			try {
				utxoMap = utxoTable.get(ByteBuffer.wrap(Base58.decode(addr[i])));
			} catch (AddressFormatException e) {
				e.printStackTrace();
				Log.log("[DataManager.getBalance()] Invalid address: " + new String(addr[i]));
				hashBalance.add(list);
				continue;
			}
			if (utxoMap != null) {
				int j = 0;
				for (Entry<ByteBuffer, Output> entry : utxoMap.entrySet()) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("outHash", DatatypeConverter.printHexBinary(entry.getKey().array()));
					map.put("amount", "" + entry.getValue().getAmount());
					list.add(map);
					j++;
				}
			} else {
				Log.log("[DataManager.getBalance()]UTXO not Exists: " + addr[i], Constant.Log.TEMPORARY);
			}
			hashBalance.add(list);
		}
		return JSON.encode(hashBalance);
	}

	static void overwriteUTXO(UTXO utxoNew) {
//		Log.log("古いUTXO: " + utxoTable.toExplainString());
//		Log.log("新しいUTXO: " + utxoNew.toExplainString());
		utxoTable = utxoNew;
//		utxoTable.addAll(utxoNew.getAll());
		utxoTableUsed.clear();
	}
	static void updateUTXO(UTXO utxoNew, Spent spentNew) {
		utxoTable.removeAll(spentNew.getAll());
		utxoTable.addAll(utxoNew.getAll());
		
//		utxoTable.checkAndRemoveAll(spentNew.getAll());
//		utxoTable.checkAndAddAll(utxoNew.getAll());
		utxoTableUsed.clear();
	}
	static boolean checkUTXOHash(byte[] utxoHashNew) {
		ByteBuffer utxoHashBuf;
		try {
			utxoHashBuf = ByteBuffer.wrap(Crypto.hash512(ByteUtil.getByteObject(utxoTable)));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		ByteBuffer utxoHashNewBuf = ByteBuffer.wrap(utxoHashNew);
		Log.log("utxoHashOldBuf: " + DatatypeConverter.printHexBinary(utxoHashBuf.array()));
		Log.log("utxoHashNewBuf: " + DatatypeConverter.printHexBinary(utxoHashNew));
		return utxoHashBuf.equals(utxoHashNewBuf);
	}

	static boolean balanceEnough(Request request) {
		String[][] outHashList = request.getOutHash();
		try {
			for (int i = 0; i < outHashList.length; i++) {
				byte[] addressFrom = DatatypeConverter.parseHexBinary(request.addrFrom[i]);
				ByteBuffer adderssBuf = ByteBuffer.wrap(addressFrom);
				Set<ByteBuffer> utxoMapUsed = utxoTableUsed.get(adderssBuf);
				Map<ByteBuffer, Output> utxoMap = utxoTable.get(adderssBuf);
				for (String outHash : outHashList[i]) {
					ByteBuffer outHashBuf = ByteBuffer.wrap(DatatypeConverter.parseHexBinary(outHash));
					if (utxoMapUsed != null && utxoMapUsed.contains(outHashBuf)) {
						Log.log("[DataManager.balanceEnough()] UTXO already used");
						return false;
					}
					if (utxoMap != null && utxoMap.containsKey(outHashBuf)) {
						utxoTableUsed.add(ByteBuffer.wrap(addressFrom), ByteBuffer.wrap(DatatypeConverter.parseHexBinary(outHash)));
					} else {
						Log.log("[DataManager.balanceEnough()] UTXO does not exists");
						return false;
					}
				}
			}
		} catch (Exception e) {
			Log.log("[DataManager.balanceEnough()] UTXO add Exception", Constant.Log.EXCEPTION);
			e.printStackTrace();
			return false;
		}

		// txの状況問い合わせ用に用意しておく
		ByteBuffer buf = ByteBuffer.wrap(DatatypeConverter.parseHexBinary(request.getPublicKey()));
		Map<ByteBuffer, String> map;
		if (txInfoTable.containsKey(buf)) {
			map = txInfoTable.get(buf);
		} else {
			map = new HashMap<ByteBuffer, String>();
			txInfoTable.put(buf, map);
		}
		map.put(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(request.getSignature())), "{}");
		return true;
	}

	static Request verifyRequest(String json) {
		try {
			Request request = (Request) JSON.decode(json, Request.class);
			if (request.getType() > 0) {
				return request;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[RequestManager.verifyRequest()] Not Request JSON");
		}
		return null;
	}

	static String getTransactionInfo(Request request) {
		ByteBuffer pubkBuf = ByteBuffer.wrap(DatatypeConverter.parseHexBinary(request.getPublicKey()));
		if (txInfoTable.containsKey(pubkBuf)) {
			Map<ByteBuffer, String> map = txInfoTable.get(pubkBuf);
			ByteBuffer sigBuf = ByteBuffer.wrap(DatatypeConverter.parseHexBinary(request.getSignature()));
			if (map.containsKey(sigBuf)) {
				return "{\"message\": " + map.get(sigBuf) + "}";
			} else {
				map = new HashMap<ByteBuffer, String>();
				map.put(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(request.getSignature())), "{}");
				txInfoTable.put(pubkBuf, map);
			}
		} else {
			Map<ByteBuffer, String> map = new HashMap<ByteBuffer, String>();
			map.put(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(request.getSignature())), "{}");
			txInfoTable.put(pubkBuf, map);
		}
		return "{}";
	}

	static String getRoutineInfo(Request request) {
		String publicKey = request.getPublicKey();
		if (request.getType() == Constant.Request.TYPE_ROUTINE) {
			if (myRoutineTable.containsKey(publicKey)) {
				Map<String, String> map = myRoutineTable.get(publicKey);
				String signature = request.getSignature();
				if (map.containsKey(signature)) {
					return "{\"message\": " + map.get(signature) + "}";
				} else {
					return "{\"messageId\": " + Constant.Request.MESSAGE_NOTFOUND + "}";
				}
			} else if (routineTable.containsKey(publicKey)) {
				Map<String, String> map = routineTable.get(publicKey);
				String signature = request.getSignature();
				if (map.containsKey(signature)) {
					return "{\"message\": " + map.get(signature) + "}";
				} else {
					return "{\"messageId\": " + Constant.Request.MESSAGE_NOTFOUND + "}";
				}
			} else {
				return "{\"messageId\": " + Constant.Request.MESSAGE_NOTFOUND + "}";
			}
		} else if (request.getType() == Constant.Request.TYPE_ROUTINE_REGISTER) {
			Map<String, String> routineMap;
			routineMap = new HashMap<String, String>();
			myRoutineTable.put(publicKey, routineMap);
			return "{\"messageId\": " + Constant.Request.MESSAGE_REGISTERED + "}";
		} else if (request.getType() == Constant.Request.TYPE_ROUTINE_REVOKE) {
			if (myRoutineTable.containsKey(publicKey)) {
				Map<String, String> map = myRoutineTable.get(publicKey);
				String signature = request.getSignature();
				if (map.containsKey(signature)) {
					return "{\"message\": " + map.get(signature) + "}";
				} else {
					return "{\"messageId\": " + Constant.Request.MESSAGE_NOTFOUND + "}";
				}
			} else {
				return "{\"messageId\": " + Constant.Request.MESSAGE_NOTFOUND + "}";
			}
		} else {
			return "{\"messageId\": " + Constant.Request.MESSAGE_UNKNOWN + "}";
		}
	}

	static void registerRoutine(Request request) {
		if (request.getType() == Constant.Request.TYPE_ROUTINE_REGISTER) {
			NetworkObject no = new NetworkObject(Constant.NetworkObject.TYPE_ROUTINE,
					new Routine(request.getLockTime(), request.getAddrTo(), request.getAmountTo(), Setting.getAddress(),
							request.getPublicKey(), request.getSignature()));
			FrontendServer.shareBackend(no);
			FrontendServer.shareFrontend(no);
		} else if (request.getType() == Constant.Request.TYPE_ROUTINE_REVOKE) {
			NetworkObject no = new NetworkObject(Constant.NetworkObject.TYPE_ROUTINE_REVOKE,
					new Routine(request.getLockTime(), request.getAddrTo(), request.getAmountTo(), Setting.getAddress(),
							request.getPublicKey(), request.getSignature()));
			FrontendServer.shareBackend(no);
			FrontendServer.shareFrontend(no);
		}

	}

	static void receptRoutine(NetworkObject no) {
		if (no.getType() == Constant.Request.TYPE_ROUTINE_REGISTER) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(no.getRoutine().getSignature(), no.getRoutine().toString());
			routineTable.put(no.getRoutine().getPublicKey(), map);
		} else if (no.getType() == Constant.Request.TYPE_ROUTINE_REVOKE) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(no.getRoutine().getSignature(), no.getRoutine().toString());
			routineTable.put(no.getRoutine().getPublicKey(), map);
		}
	}
	static void addBlock(Block block) {
		blockTable.put(block.getBlockHeight(), block);
		for (int i = 1; i < Constant.Frontend.MAX_BLOCKCHAIN_TABLE - 1; i++) {
			// 一つ前のblockHeightのブロックを取得し損ねていたら問い合わせる
			if (block.getBlockHeight() - i >= 0 && !blockTable.containsKey(block.getBlockHeight() - i)) {
				FrontendServer.inquiryBackend(new NetworkObject(
						Constant.NetworkObject.TYPE_BLOCK_CHECK, block.getBlockHeight() - i));
			}
		}
	}

	synchronized static void addRequestPool(Request request) {
		requestTable.add(request);
	}

	public void run() {
		new RequestNotifyer().start();
	}

	private final static Object REQUEST_TABLE_LOCK = new Object();
	class RequestNotifyer extends Thread {
		public void run() {
			byte[] last = new byte[1];
			byte[] current;
			while(true) {
				try {
					Thread.sleep(Constant.Manager.REQUEST_NOTIFIER_INTERVAL);
					if(requestTable.size() == 0) {
						continue;
					}
					current = ByteUtil.getByteObject(requestTable);
					if(!Arrays.equals(current, last)) {
						notifyRequest();
						last = current;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void notifyRequest() {
			synchronized (REQUEST_TABLE_LOCK) {
//				FrontendServer.shareBackend(new NetworkObject(Constant.NetworkObject.TYPE_REQUEST,
//						requestTable.toArray(new Request[requestTable.size()])));
				FrontendServer.inquiryBackend(new NetworkObject(Constant.NetworkObject.TYPE_REQUEST, 
						requestTable.toArray(new Request[requestTable.size()])));
//				Spent[] spentList = new Spent[requestTable.size()];
//				int i = 0;
//				for (Request request : requestTable) {
//					spentList[i++] = new Spent(request.getAddrFrom(), request.getAddrFrom());
//				}
//				FrontendServer.shareFrontend(new NetworkObject(Constant.NetworkObject.TYPE_SPENT, spentList));
				requestTable.clear();
			}
		}
	}
}
