package V1.Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Block;
import V1.Component.NetworkObject;
import V1.Component.Node;
import V1.Component.Request;
import V1.Component.Spent;
import V1.Component.Transaction;
import V1.Component.UTXO;
import V1.Component.Work;
import V1.Library.Address;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Constant.Verify.Result;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.Verify;
import V1.Library.TofuError;
import net.arnx.jsonic.JSON;

public class BackendServer extends Thread {
	private static List<byte[]> receptDataHashList;
	private static Map<String, Node> backendTable;
	private static Map<String, Node> frontendTable;
	private static Map<String, Integer> unreachableTable;
	private static Map<ByteBuffer, Set<String>> pbftTable;
	private static Map<ByteBuffer, Set<String>> pbftHashTable;
	private static Map<ByteBuffer, Block> blockTable;
	private static List<ByteBuffer> requestListHashList;


	static void init() throws Exception {
		receptDataHashList = new ArrayList<byte[]>();

		backendTable = new HashMap<String, Node>();
		frontendTable = new HashMap<String, Node>();
		Node tmp;
		for (File file : new File(Setting.TRUSTED_BACKEND_DIR).listFiles()) {
			tmp = (Node) ByteUtil.convertByteToObject(IO.readFileToByte(Setting.TRUSTED_BACKEND_DIR + file.getName()));
			backendTable.put(tmp.getIp(), tmp);
		}
		for (File file : new File(Setting.TRUSTED_FRONTEND_DIR).listFiles()) {
			tmp = (Node) ByteUtil.convertByteToObject(IO.readFileToByte(Setting.TRUSTED_FRONTEND_DIR + file.getName()));
			frontendTable.put(tmp.getIp(), tmp);
		}
		pbftTable = new HashMap<ByteBuffer, Set<String>>();
		pbftHashTable = new HashMap<ByteBuffer, Set<String>>();
		unreachableTable = new HashMap<String, Integer>();
		blockTable = new HashMap<ByteBuffer, Block>();
		requestListHashList = new ArrayList<ByteBuffer>();
		Log.log("Server init done.");
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(Constant.Server.SERVER_PORT);
			Log.log("BackendServer.init()] ready: port[ " + Constant.Server.SERVER_PORT + " ]");

			while (true) {
				try {
					Socket soc = ss.accept();
					String remoteIp = soc.getRemoteSocketAddress().toString().replaceAll("/(.*):.*", "$1");
					if (backendTable.containsKey(remoteIp) || frontendTable.containsKey(remoteIp)) {
						new Client(soc, remoteIp).start();
					} else {
						Log.log("Access denied not trusted ip [" + remoteIp + "]");
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class Client extends Thread {
		private Socket soc;
		private String remoteIp;

		public Client(Socket soc, String remoteIp) {
			this.soc = soc;
			this.remoteIp = remoteIp;
		}

		public void run() {
			InputStream is = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			PrintWriter pw = null;
			ByteArrayOutputStream baos = null;
			ByteBuffer bbuf = ByteBuffer.allocate(Constant.Server.SERVER_BUF);

			try {
				br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())));
				while (true) {
					try {
						Thread.sleep(Constant.Server.SERVER_READ_SLEEP);
						is = soc.getInputStream();
						isr = new InputStreamReader(is);
						baos = new ByteArrayOutputStream();
						byte[] buffer = new byte[Constant.Server.SERVER_BUF];
						int readBytes = -1;
						byte[] data;
						int available = is.available();
						if (available > 0 && (readBytes = is.read(buffer)) > 1) {
							baos.write(buffer, 0, readBytes);
							data = baos.toByteArray();
							bbuf.put(data);
							pw.println("[BackendServer] Message: Your data is accepted.");
							pw.flush();
						} else {
							break;
						}
					} catch (Exception e) {
						try {
							pw.close();
							br.close();
							pw.close();
							soc.close();
							e.printStackTrace();
							Log.log("[BackendServer.Client.run()]: BackendServer", Constant.Log.EXCEPTION);
						} catch (Exception ex) {
							ex.printStackTrace();
							Log.log("[BackendServer.Client.run()]: BackendServer", Constant.Log.EXCEPTION);
						}
						break;

					}
				}
				receptNetworkObject(bbuf, remoteIp);
				br.close();
				pw.close();
				isr.close();
				is.close();
				soc.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.log("[BackendServer.Client.run()]: BackendServer", Constant.Log.EXCEPTION);
			}
		}
	}

	static void receptNetworkObject(ByteBuffer bbuf, String remoteIp) {
		NetworkObject no = null;
		try {
			ByteArrayInputStream b = new ByteArrayInputStream(bbuf.array());
			ObjectInputStream o = new ObjectInputStream(b);
			no = (NetworkObject) o.readObject();
			o.close();
			b.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// Log.log("[BackendServer.receptNetworkObject()] ip: "+remoteIp+", no:
		// " + no, Constant.Log.TEMPORARY);
		// Log.log("[BackendServer.receptNetworkObject()]
		// AlreadyReceptRejectMode off", Constant.Log.TEMPORARY);
		// try {
		// byte[] hash = Crypto.hash256(ByteUtil.getByteObject(no));
		// if (ByteUtil.contains(receptDataHashList, hash)) {
		// Log.log("[BackendServer.receptNetworkObject()] Already recept: "
		// + DatatypeConverter.printHexBinary(hash), Constant.Log.IMPORTANT);
		// return;
		// } else {
		// receptDataHashList.add(hash);
		// if (receptDataHashList.size() >
		// Constant.Server.MAX_RECEPT_DATA_HASH_LIST) {
		// receptDataHashList.remove(0);
		// }
		// }
		// } catch (Exception e) {
		// Log.log("[BackendServer.receptNetworkObject()] Invalid
		// NetworkObject", Constant.Log.EXCEPTION);
		// e.printStackTrace();
		// return;
		// }

		if (no.getType() == Constant.NetworkObject.TYPE_TX
				|| no.getType() == Constant.NetworkObject.TYPE_TX_BROADCAST) {
			// 新しいトランザクションが送られてきた
			Blockchain.addTransaction(no.getTx());
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_BLOCK) {
			// マイニング成功した新しいブロックが送られてきた
			try {
				byte[] blockHash = Crypto.hash512(ByteUtil.getByteObject(no));
				ByteBuffer buf = ByteBuffer.wrap(blockHash);
				// まだ受信したことがないブロック
				if (!pbftTable.containsKey(buf)) {
//					Log.log("A まだ受信したことがないブロック");
					if (Blockchain.verifyBlock(no.getBlock(), null)) {
//						Log.log("B ブロックの検証完了 pbftTableに追加してブロードキャスト");
						pbftTable.put(buf, new HashSet<String>());
						blockTable.put(buf, no.getBlock());
						BackendServer.shareBackend(new NetworkObject(Constant.NetworkObject.TYPE_BLOCK_BROADCAST, no.getBlock()));
					}
				} else {
//					Log.log("C 既に受信したことがあるブロック/ブロックハッシュ");
					Log.log("[BackendServer.receptNetworkObject()] Already recept block from other node");
				}
			} catch (Exception e) {
				Log.log("[BackendServer.receptNetworkObject()] Invalid NetworkObject", Constant.Log.EXCEPTION);
				e.printStackTrace();
			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_BLOCK_BROADCAST) {
			// マイニング成功を認めた他ノードが再度ブロードキャストしてきた
			try {
				byte[] blockHash = Crypto.hash512(ByteUtil.getByteObject(no));
				ByteBuffer buf = ByteBuffer.wrap(blockHash);
//				Log.log("D ブロードキャストを受信");
				if (pbftTable.containsKey(buf)) {
//					Log.log("E 受信したことがあるブロックなのでPBFT承認数を確認");
					Set<String> set = pbftTable.get(buf);
					set.add(remoteIp);
					if (!set.contains("TYPE_BLOCK_HASH") && set.size() >= backendTable.size() * Constant.Blockchain.NODE_PBFT_RATE) {
//						Log.log("F 承認数が2/3を超えたのでブロックとして認める そしてハッシュを送信する");
						set.add("TYPE_BLOCK_HASH");
						BackendServer
								.shareBackend(new NetworkObject(Constant.NetworkObject.TYPE_BLOCK_HASH, blockHash));
					}
				} else {
					// TYPE_BLOCKを受信したときと同じ挙動
					if (Blockchain.verifyBlock(no.getBlock(), null)) {
//						Log.log("I ブロックの検証完了 pbftTableに追加してブロードキャスト");
						pbftTable.put(buf, new HashSet<String>());
						blockTable.put(buf, no.getBlock());
						BackendServer.shareBackend(new NetworkObject(Constant.NetworkObject.TYPE_BLOCK_BROADCAST, no.getBlock()));
					}
				}
			} catch (Exception e) {
				Log.log("[BackendServer.receptNetworkObject()] Invalid NetworkObject", Constant.Log.EXCEPTION);
				e.printStackTrace();
			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_BLOCK_HASH) {
			// マイニングを認めた他ノードがブロードキャストし合い、一定以上のコンセンサスを得たことの通知を受信
			ByteBuffer buf = ByteBuffer.wrap(no.getHash());
//			Log.log("G ブロックとして認めたいという連絡がきた");
			if (pbftHashTable.containsKey(buf)) {
				Set<String> set = pbftHashTable.get(buf);
				set.add(remoteIp);
				// +1はTYPE_BLOCK_HASHを追加した分
				if (!set.contains("GO_TO_NEXT_BLOCK") && set.size() > backendTable.size() * Constant.Blockchain.NODE_PBFT_RATE + 1) {
//					Log.log("H ブロックとして認めることにする");
//					Log.log("このブロックを許可: " + DatatypeConverter.printHexBinary(buf.array()));
					set.add("GO_TO_NEXT_BLOCK");
//					Log.log("blockTable: ");
//					for(Entry<ByteBuffer, Block> entry: blockTable.entrySet()) {
//						Log.log("[hash: "+DatatypeConverter.printHexBinary(entry.getKey().array()) + ", blockHeader: "+entry.getValue().getBlockHeader()+"]");
//					}
					Blockchain.goToNextBlock(blockTable.get(buf));
				}
			} else {
				Set<String> set = new HashSet<String>();
				set.add(remoteIp);
				pbftHashTable.put(buf, set);
			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_BLOCK_CHECK) {
			Block block = Blockchain.getBlock(no.getBlockHeight());
			if (block != null) {
				access(new NetworkObject(Constant.NetworkObject.TYPE_BLOCK, block), frontendTable.get(remoteIp));
			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_WORK_CHECK) {
			Work currentWork = DataManager.getWork();
			if (currentWork != no.getWork()) {
				access(new NetworkObject(Constant.NetworkObject.TYPE_WORK, currentWork), frontendTable.get(remoteIp));
			} else {
//				access(new NetworkObject(Constant.NetworkObject.TYPE_WORK, no.getWork()), frontendTable.get(remoteIp));
			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_NODE
				|| no.getType() == Constant.NetworkObject.TYPE_NODE_BROADCAST) {
			// 新しいノードの接続要求
			Blockchain.addNode(no);
			if (remoteIp.equals(no.getNode().getIp())) {
				backendTable.put(remoteIp, no.getNode());
				Log.log(backendTable.toString());
				return;
			}
		} else if (no.getType() == Constant.NetworkObject.TYPE_REPORT) {
			// マイニング成功の報告が来た（F層から）
			Result result = DataManager.verifyMining(no.getReport());
			if (result == Result.TARGET || result == Result.SUB_TARGET) {
				Blockchain.addRewardTransaction(no.getReport(), result);
			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_REQUEST
				|| no.getType() == Constant.NetworkObject.TYPE_REQUEST_BROADCAST) {
			// 様々なリクエストが来た（F層から）
			// verify request signature
			Log.log("REQUEST UPDATE!", Constant.Log.STRONG);
			List<Request> requestList = new ArrayList<Request>(Arrays.asList(no.getRequest()));
			ByteBuffer buf;
			try {
				buf = ByteBuffer.wrap(Crypto.hash512(ByteUtil.getByteObject(no.getRequest())));
			} catch (Exception e) {
				e.printStackTrace();
				Log.log("Invalid Request", Constant.Log.INVALID);
				return ;
			}
			if(requestListHashList.contains(buf)) {
				Log.log("[BackendServer.receptNetworkObject()] Request already recept", Constant.Log.STRONG);
				return ;
			}
			requestListHashList.add(buf);
			for (Iterator<Request> it = requestList.iterator(); it.hasNext();) {
				Request request = it.next();
				if (DataManager.verifyRequest(request)) {
					if (Blockchain.addTransaction(DataManager.makeTx(request))) {
					} else {
						it.remove();
						Log.log("[BackendServer.receptNetworkObject()] Request invalid: " + no.getType(), Constant.Log.INVALID);
					}
				} else {
					it.remove();
					Log.log("[BackendServer.receptNetworkObject()] Request signature invalid", Constant.Log.INVALID);
				}
			}
			if (requestList.size() > 0 && no.getType() == Constant.NetworkObject.TYPE_REQUEST) {
				BackendServer.shareBackend(new NetworkObject(Constant.NetworkObject.TYPE_REQUEST_BROADCAST, no.getRequest()));
//				BackendServer.shareBackend(new NetworkObject(Constant.NetworkObject.TYPE_REQUEST_BROADCAST,
//						requestList.toArray(new Request[requestList.size()])));
			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_UTXO_CHECK) {
			if (no.getBlockHeight() == Constant.NetworkObject.VALUE_ALL) {
				access(new NetworkObject(Constant.NetworkObject.TYPE_UTXO, Blockchain.getUTXO()),
						frontendTable.get(remoteIp));
				// byte[] utxo;
				// try {
				// utxo = ByteUtil.getByteObject(Blockchain.getUTXO());
				// byte[] output = new byte[1024 * 1024];
				// Deflater compresser = new Deflater();
				// compresser.setInput(utxo);
				// compresser.finish();
				// int compressedDataLength = compresser.deflate(output);
				// compresser.end();
				// byte[] data = new byte[compressedDataLength];
				// System.arraycopy(output, 0, data, 0, compressedDataLength);
				// NetworkObject retNo = new
				// NetworkObject(Constant.NetworkObject.TYPE_UTXO_BYTE, null);
				// retNo.setUtxoByte(output, utxo.length);
				// Log.log("data: " +
				// DatatypeConverter.printHexBinary(output).substring(0, 20));
				// access(retNo, frontendTable.get(remoteIp));
				// return;
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				return;
			} else if (no.getBlockHeight() == Constant.NetworkObject.VALUE_DIFF) {
				NetworkObject retNo = new NetworkObject(Constant.NetworkObject.TYPE_UTXO_SPENT_HASH, null);
				List<UTXO> appendList = Blockchain.getUTXOLastList();
				UTXO utxo = new UTXO();
				for (UTXO tmp : appendList) {
					utxo.addAll(tmp.getAll());
				}
				List<Spent> removeList = Blockchain.getSpentLast();
				Spent spent = new Spent();
				for (Spent tmp : removeList) {
					spent.addAll(tmp.getAll());
				}
				retNo.setUtxoSpentHash(utxo, spent, Blockchain.getUTXOTableHash());
				access(retNo, frontendTable.get(remoteIp));
				return;
			}
		}
		Log.log("[BackendServer.receptNetworkObject()] Recept invalid data from [" + remoteIp + "]: " + no,
				Constant.Log.EXCEPTION);
	}

	static void shareBackend(NetworkObject no) {
		if (!Setting.BROADCAST_BACKEND) {
			Log.log("BROADCAST_BACKEND false");
			return;
		}
		// Log.log("[FrontendServer.shareBackend()] no: " + no,
		// Constant.Log.TEMPORARY);
		for (Iterator<Node> it = backendTable.values().iterator(); it.hasNext();) {
			if (access(no, it.next()) == null) {
				Log.log("[share--end] 3count after remove");
				// it.remove();
			}
		}
	}

	static void shareFrontend(NetworkObject no) {
		// nonce range share?
		if (!Setting.BROADCAST_FRONTEND) {
			Log.log("BROADCAST_FRONTEND false");
			return;
		}
		// Log.log("[FrontendServer.shareFrontend()] no: " + no,
		// Constant.Log.TEMPORARY);
		for (Iterator<Node> it = frontendTable.values().iterator(); it.hasNext();) {
			if (access(no, it.next()) == null) {
				Log.log("[share--end] 3count after remove");
				// it.remove();
			}
		}
	}

	static char[] access(NetworkObject no, Node node) {
		Socket socket = new Socket();
		Log.log("[BackendServer.access()] to: " + node.getIp() + ":" + node.getPort());

		try {
			InetSocketAddress socketAddress = new InetSocketAddress(node.getIp(), node.getPort());
			socket.connect(socketAddress, Constant.Server.TIMEOUT);
			socket.setSoTimeout(Constant.Server.TIMEOUT);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			byte[] data = null;
			OutputStream os = null;

			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(no);
				data = baos.toByteArray();

				os = socket.getOutputStream();
				os.write(data);
				os.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

			InputStream is1 = socket.getInputStream();
			InputStreamReader ir1 = new InputStreamReader(is1, "UTF-8");
			BufferedReader br1 = new BufferedReader(ir1);

//			while (is1.available() == 0)
//				;
//
//			char[] cline = new char[is1.available()];
//			br1.read(cline);
//			Log.log("[BackendServer.broadcast()] recept: " + new String(cline), Constant.Log.TEMPORARY);
//			Log.log("[FrontendServer.broadcast()] connect: " + node, Constant.Log.TEMPORARY);

			baos.close();
			oos.close();
			os.close();
			ir1.close();
			br1.close();
			is1.close();

			socket.close();
			return new char[1];
		} catch (Exception e) {
//			e.printStackTrace();
			Log.log("[BackendServer.broadcast()] Cannot connection and detach: " + node.getIp() + ":" + node.getPort(),
					Constant.Log.IMPORTANT);
			return null;
		}
	}
}
