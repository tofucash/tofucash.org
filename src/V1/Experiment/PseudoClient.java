package V1.Experiment;

import java.io.File;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import com.sun.javafx.collections.MappingChange.Map;

import V1.Library.Address;
import V1.Library.Base58;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.KeyAddressSet;
import V1.Library.Log;
import net.arnx.jsonic.JSON;

public class PseudoClient extends Thread {
	final private static int MAX_TRIAL = 60;
	final private static int EACH_SEND_AMOUNT = 100;
	final private static int INTERVAL1 = 2000;
	final private static int INTERVAL_PLUS_MAX = 2000;
	final private static int INTERVAL2 = 2000;
	final private static int CLIENT_NUM = 10;
	final private static int UTXO_SPLID = 3;
	// final private static int CLIENT_NUM = Constant.Test.CLIENT_NUM;

	// 0から9までノード/プロセスごとに異なる
	private static int NODE_ID = 0;

	private static List<String> ipList;

	public static void main(String[] args) {
		// n.jspnの数
		// int pseudoClientNum = 1;
		if (args.length > 0) {
			NODE_ID = Integer.parseInt(args[0]);
		}
		String keyAddressSetFileDir = System.getProperty("user.dir") + File.separator + ".." + File.separator + "data"
				+ File.separator + Constant.Test.EXP_DIR + File.separator + NODE_ID + File.separator;
		// String keyAddressSetFileDir = System.getProperty("user.dir") +
		// File.separator + "data" + File.separator
		// + Constant.Test.EXP_DIR + File.separator + NODE_ID + File.separator;
		init();
		Log.log("MAX_TRIAL: " + MAX_TRIAL);
		Log.log("NODE_ID: " + NODE_ID);
		Log.log("ipList: " + ipList);
		Random rnd = new Random(123);

		try {
			for (int i = 0; i < CLIENT_NUM; i++) {
				KeyPair[] keyPairList = new KeyPair[Constant.Test.ACCOUNT_NUM];
				byte[][] addressList = new byte[Constant.Test.ACCOUNT_NUM][512];
				KeyAddressSet keyAddressSet = JSON.decode(IO.fileReadAll(keyAddressSetFileDir + i + ".json"),
						KeyAddressSet.class);
				for (int j = 0; j < Constant.Test.ACCOUNT_NUM; j++) {
					keyPairList[j] = Address.createKeyPair();
					KeyFactory kf = KeyFactory.getInstance("EC");
					PrivateKey prvk = kf.generatePrivate(new PKCS8EncodedKeySpec(DatatypeConverter
							.parseHexBinary(Constant.Address.PRIVATE_KEY_PREFIX + keyAddressSet.privateKey[j])));
					PublicKey pubk;
					pubk = kf.generatePublic(new X509EncodedKeySpec(DatatypeConverter
							.parseHexBinary(Constant.Address.PUBLIC_KEY_PREFIX + keyAddressSet.publicKey[j])));
					keyPairList[j] = new KeyPair(pubk, prvk);
					addressList[j] = DatatypeConverter.parseHexBinary(keyAddressSet.address[j]);
				}
				new PseudoClient(keyPairList, addressList, i).start();
				// checkBalance(addressList, rnd);
				// System.exit(0);
			}
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		}
		byte[][] one = new byte[1][];
		one[0] = new byte[Constant.Address.BYTE_ADDRESS];
		checkBalance(one, rnd);

	}

	static void init() {
		String ipListFilePath = System.getProperty("user.dir") + File.separator + ".." + File.separator + "data"
				+ File.separator + Constant.Test.EXP_DIR + File.separator + "ipList.json";
		// String ipListFilePath = System.getProperty("user.dir") +
		// File.separator + "data" + File.separator
		// + Constant.Test.EXP_DIR + File.separator + "ipList.json";
		ipList = (List<String>) JSON.decode(IO.fileReadAll(ipListFilePath));
	}

	static int checkBalance(byte[][] addr, Random rnd) {
		String outHashRequestJsonStr = "POST / HTTP/1.1\r\n\r\n{\"type\":10100,\"addrFrom\":[";
		for (int i = 0; i < addr.length - 1; i++) {
			outHashRequestJsonStr += "\"" + Base58.encode(addr[i]) + "\",";
		}
		outHashRequestJsonStr += "\"" + Base58.encode(addr[addr.length - 1]) + "\"]}";
		String outHashListJsonStr = "";
		try {
			while (outHashListJsonStr.equals("")) {
				int remote = rnd.nextInt(ipList.size());
				outHashListJsonStr = HttpManager.access(ipList.get(remote), outHashRequestJsonStr);
				if (outHashListJsonStr == null) {
					Log.log("Remove Node: " + ipList.get(remote));
					ipList.remove(ipList.get(remote));
					outHashListJsonStr = "";
				}
				int interval = (int) (INTERVAL2 + Math.random() * INTERVAL_PLUS_MAX);
				Log.log("SLEEP(" + interval + ")");
				Thread.sleep(interval);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.log("outHashListJsonStr: " + outHashListJsonStr);
		List<List<LinkedHashMap<String, String>>> outHashList = (List<List<LinkedHashMap<String, String>>>) JSON
				.decode(outHashListJsonStr);
		int total = 0;
		for (int i = 0; i < addr.length; i++) {
			int sum = 0;
			for (Iterator<LinkedHashMap<String, String>> it = outHashList.get(i).iterator(); it.hasNext();) {
				LinkedHashMap<String, String> map = it.next();
				sum += Integer.parseInt(map.get("amount"));
			}
			Log.log("Sum: " + sum + "\tAddr: " + DatatypeConverter.printHexBinary(addr[i]));
			total += sum;
		}
		return total;
	}

	private KeyPair[] keyPairList;
	private byte[][] addressList;
	private int clientId;

	PseudoClient(KeyPair[] keyPairList, byte[][] addressList, int clientId) {
		this.keyPairList = keyPairList;
		this.addressList = addressList;
		this.clientId = clientId;
	}

	public void run() {
		Random rnd = new Random(clientId);
		List<List<String>> utxoLastUsedList = new ArrayList<List<String>>();
		for (int i = 0; i < Constant.Test.ACCOUNT_NUM; i++) {
			utxoLastUsedList.add(new ArrayList<String>());
		}
		String outHashRequestJsonStr = "POST / HTTP/1.1\r\n\r\n{\"type\":10100,\"addrFrom\":[";
		for (int i = 0; i < Constant.Test.ACCOUNT_NUM - 1; i++) {
			outHashRequestJsonStr += "\"" + Base58.encode(addressList[i]) + "\",";
		}
		outHashRequestJsonStr += "\"" + Base58.encode(addressList[Constant.Test.ACCOUNT_NUM - 1]) + "\"]}";
		List<List<LinkedHashMap<String, String>>> outHashList = null;

		// UTXOをあらかじめ分けて置く場合はここでoutHashListを取ってくるだけでいい
		outHashList = getOutHashList(outHashRequestJsonStr, rnd);
		Log.log("outHashList: " + outHashList);
		for (int n = 0; n < MAX_TRIAL; n++) {
			int end = 0;
			String result;
			for (int i = 0; i < Constant.Test.ACCOUNT_NUM; i++) {
				// if(i % (Constant.Test.ACCOUNT_NUM / 2) == 0) {
				// outHashList = getOutHashList(outHashRequestJsonStr, rnd);
				// }
				if (outHashList.get(i).size() == 0) {
					Log.log("Thead[" + clientId + "] end.");
					checkBalance(addressList, rnd);
					byte[][] one = new byte[1][];
					one[0] = new byte[Constant.Address.BYTE_ADDRESS];
					checkBalance(one, rnd);
					return;
				}
				List<String> list = utxoLastUsedList.get(i);
				// UTXOをあらかじめ分けて置く場合はUTXOを一度使ったらもう送信しない
				// if(list.size() > UTXO_SPLID -1) {
				// list.remove(list.get(0));
				// }
				result = "";
				for (LinkedHashMap<String, String> outHashMap : outHashList.get(i)) {
					if (list.contains(outHashMap.get("outHash"))) {
						continue;
					}
					int amount = Integer.parseInt(outHashMap.get("amount"));
					if (amount >= EACH_SEND_AMOUNT + 1) {
						list.add(outHashMap.get("outHash"));
						String address = DatatypeConverter.printHexBinary(addressList[i]);
						String publicKey = DatatypeConverter.printHexBinary(keyPairList[i].getPublic().getEncoded())
								.substring(Constant.Address.BYTE_PUBLIC_KEY_PREFIX * 2);
						String outHashSign = DatatypeConverter.printHexBinary(Crypto.sign(keyPairList[i].getPrivate(),
								keyPairList[i].getPublic(), outHashMap.get("outHash").getBytes()));
						String answerScript = "17000000" + Integer.toHexString((outHashSign.length() / 2)) + outHashSign
								+ "17000000" + Integer.toHexString(publicKey.length() / 2) + publicKey;
						String request = "{\"addrFrom\":[\"" + address
								+ "\"],\"addrTo\":[\"00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\""
								+ address + "\"],\"amountFrom\":[[" + amount + "]],\"amountTo\":[" + EACH_SEND_AMOUNT
								+ "," + (amount - EACH_SEND_AMOUNT - 1) + "],\"answerScript\":[[\"" + answerScript
								+ "\"]],\"lockTime\":0,\"outHash\":[[\"" + outHashMap.get("outHash")
								+ "\"]],\"publicKey\":\"" + publicKey
								+ "\",\"questionScript\":[[\"61407050\"],[\"61407050\"]],\"signature\":\"" + publicKey
								+ "\",\"type\":10000,\"version\":1}";
						String requestSign = DatatypeConverter.printHexBinary(Crypto.sign(keyPairList[i].getPrivate(),
								keyPairList[i].getPublic(), request.getBytes()));
						request = "{\"addrFrom\":[\"" + address
								+ "\"],\"addrTo\":[\"00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\""
								+ address + "\"],\"amountFrom\":[[" + amount + "]],\"amountTo\":[" + EACH_SEND_AMOUNT
								+ "," + (amount - EACH_SEND_AMOUNT - 1) + "],\"answerScript\":[[\"" + answerScript
								+ "\"]],\"lockTime\":0,\"outHash\":[[\"" + outHashMap.get("outHash")
								+ "\"]],\"publicKey\":\"" + publicKey
								+ "\",\"questionScript\":[[\"61407050\"],[\"61407050\"]],\"signature\":\"" + requestSign
								+ "\",\"type\":10000,\"version\":1}";
						while (result.equals("")) {
							int remote = rnd.nextInt(ipList.size());
							result = HttpManager.access(ipList.get(remote), request);
							if (result == null) {
								Log.log("Remove Node: " + ipList.get(remote));
								ipList.remove(ipList.get(remote));
								result = "";
							}
							try {
								int interval = (int) (INTERVAL2 + Math.random() * INTERVAL_PLUS_MAX);
								Log.log("SLEEP(" + interval + ")");
								Thread.sleep(interval);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						Log.log("Result[" + n + "][" + i + "]: " + result);
						try {
							Thread.sleep(INTERVAL1);
							Log.log("SLEEP(" + INTERVAL1 + ")");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						break;
					}
				}
				if (result.equals("")) {
					Log.log("end++");
					end++;
				}
			}
			if (end >= Constant.Test.ACCOUNT_NUM) {
				// if (checkBalance(addressList, rnd) ==
				// Constant.Test.NODE_NUM*Constant.Test.CLIENT_NUM*Constant.Test.ACCOUNT_NUM*EACH_SEND_AMOUNT)
				// {
				checkBalance(addressList, rnd);
				Log.log("Thead[" + clientId + "] end.");
				byte[][] one = new byte[1][];
				one[0] = new byte[Constant.Address.BYTE_ADDRESS];
				checkBalance(one, rnd);
				return;
				// }
			}
		}
		Log.log("MAX_TRIAL END: " + MAX_TRIAL);
		Log.log("Thead[" + clientId + "] end.");
		checkBalance(addressList, rnd);
		byte[][] one = new byte[1][];
		one[0] = new byte[Constant.Address.BYTE_ADDRESS];
		checkBalance(one, rnd);
	}

	static List<List<LinkedHashMap<String, String>>> getOutHashList(String outHashRequestJsonStr, Random rnd) {
		String outHashListJsonStr = "";
		String result = "";
		while (outHashListJsonStr.equals("")) {
			int remote = rnd.nextInt(ipList.size());
			try {
				outHashListJsonStr = HttpManager.access(ipList.get(remote), outHashRequestJsonStr);
			} catch (Exception e) {

			} finally {
				if (outHashListJsonStr == null) {
					try {
						Log.log("Remove Node: " + ipList.get(remote));
						ipList.remove(ipList.get(remote));
					} catch (Exception e) {
					}
					outHashListJsonStr = "";
				}
				try {
					Log.log("SLEEP(" + INTERVAL1 + ")");
					Thread.sleep(INTERVAL1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		outHashListJsonStr = outHashListJsonStr.replaceAll(".*\r\n", "");
		List<List<LinkedHashMap<String, String>>> outHashList = (List<List<LinkedHashMap<String, String>>>) JSON
				.decode(outHashListJsonStr);
		return outHashList;
	}
}
