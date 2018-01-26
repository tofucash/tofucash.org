package V1.Frontend;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Block;
import V1.Component.NetworkObject;
import V1.Component.Node;
import V1.Component.UTXO;
import V1.Component.Work;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;

public class FrontendServer extends Thread {
	private static List<byte[]> receptDataHashList;
	private static Map<String, Node> backendTable;
	private static Map<String, Node> frontendTable;
	private static Map<String, Node> backendTableTrusted;
	private static Map<String, Node> frontendTableTrusted;
	private static List<String> blacklist;

	static void init() throws Exception {
		receptDataHashList = new ArrayList<byte[]>();
		backendTable = new HashMap<String, Node>();
		frontendTable = new HashMap<String, Node>();
		backendTableTrusted = new HashMap<String, Node>();
		frontendTableTrusted = new HashMap<String, Node>();
		blacklist = new ArrayList<String>();
		Node tmp;
		for (File file : new File(Setting.TRUSTED_BACKEND_DIR).listFiles()) {
			tmp = (Node) ByteUtil.convertByteToObject(IO.readFileToByte(Setting.TRUSTED_BACKEND_DIR + file.getName()));
			backendTable.put(tmp.getIp(), tmp);
			backendTableTrusted.put(tmp.getIp(), tmp);
		}
		for (File file : new File(Setting.TRUSTED_FRONTEND_DIR).listFiles()) {
			tmp = (Node) ByteUtil.convertByteToObject(IO.readFileToByte(Setting.TRUSTED_FRONTEND_DIR + file.getName()));
			frontendTable.put(tmp.getIp(), tmp);
			frontendTableTrusted.put(tmp.getIp(), tmp);
		}
		Log.log("Server init done.");
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(Constant.Server.SERVER_PORT);
			Log.log("FrontendServer.init()] ready: port[ " + Constant.Server.SERVER_PORT + " ]");
			Log.log("Initial UTXO/Block request");
			inquiryBackend(new NetworkObject(Constant.NetworkObject.TYPE_UTXO_CHECK, Constant.NetworkObject.VALUE_ALL));
			inquiryBackend(new NetworkObject(Constant.NetworkObject.TYPE_WORK_CHECK, MiningManager.getWork()));
			Log.log("Inquiry: TYPE_UTXO_CHECK");
//			inquiryBackend(
//					new NetworkObject(Constant.NetworkObject.TYPE_BLOCK_CHECK, Constant.NetworkObject.VALUE_ALL));

			while (true) {
				try {
					Socket soc = ss.accept();
					String remoteIp = soc.getRemoteSocketAddress().toString().replaceAll("/(.*):.*", "$1");
					if (blacklist.contains(remoteIp)) {
						Log.log("[Blacklist node] access denied not trusted ip [" + remoteIp + "]");
						soc.close();
					} else if (backendTableTrusted.containsKey(remoteIp)) {
						if (!backendTable.containsKey(remoteIp)) {
							backendTable.put(remoteIp, backendTableTrusted.get(remoteIp));
						}
						new Client(soc, remoteIp).start();
					} else if (frontendTableTrusted.containsKey(remoteIp)) {
						if (!frontendTable.containsKey(remoteIp)) {
							frontendTable.put(remoteIp, frontendTableTrusted.get(remoteIp));
						}
						new Client(soc, remoteIp).start();
					} else {
						Log.log("[Unknown node] access denied not trusted ip [" + remoteIp + "]");
						soc.close();
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
			NetworkObject no = null;
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
							Log.log("[FrontendServer.Client.run()]: FrontendServer", Constant.Log.EXCEPTION);
						} catch (Exception ex) {
							ex.printStackTrace();
							Log.log("[FrontendServer.Client.run()]: FrontendServer", Constant.Log.EXCEPTION);
						}
						break;

					}
				}
				receptNetworkObject(bbuf, remoteIp, pw);
				pw.write("[Frontendserver.Client.run()] Accepted");
				pw.flush();
				pw.close();
				br.close();
				isr.close();
				is.close();
				soc.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.log("[FrontendServer.Client.run()]: FrontendServer", Constant.Log.EXCEPTION);
			}
		}
	}

	static void receptNetworkObject(ByteBuffer bbuf, String remoteIp, PrintWriter pw) {
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

		Log.log("[BackendServer.receptNetworkObject()] ip: " + remoteIp, Constant.Log.TEMPORARY);
		// try {
		// byte[] hash = Crypto.hash256(ByteUtil.getByteObject(no));
		// if (ByteUtil.contains(receptDataHashList, hash)) {
		// Log.log("[FrontendServer.receptNetworkObject()] Already recept: "
		// + DatatypeConverter.printHexBinary(hash), Constant.Log.TEMPORARY);
		// return;
		// } else {
		// receptDataHashList.add(hash);
		// if (receptDataHashList.size() >
		// Constant.Server.MAX_RECEPT_DATA_HASH_LIST) {
		// receptDataHashList.remove(0);
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// Log.log("[FrontendServer.receptNetworkObject()] Invalid
		// NetworkObject: ", Constant.Log.EXCEPTION);
		// return;
		// }

		if (no.getType() == Constant.NetworkObject.TYPE_TX
				|| no.getType() == Constant.NetworkObject.TYPE_TX_BROADCAST) {
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_BLOCK
				|| no.getType() == Constant.NetworkObject.TYPE_BLOCK_BROADCAST) {
			DataManager.addBlock(no.getBlock());
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_NODE
				|| no.getType() == Constant.NetworkObject.TYPE_NODE_BROADCAST) {
			if (remoteIp.equals(no.getNode().getIp())) {
				frontendTable.put(remoteIp, no.getNode());
				Log.log("[FrontendServer.receptNetworkObject()] Update frontendTable: " + frontendTable.toString());
				return;
			}
		} else if (no.getType() == Constant.NetworkObject.TYPE_WORK
				|| no.getType() == Constant.NetworkObject.TYPE_WORK_BROADCAST) {
			// workが変わっていればアップデートし、utxoとブロックデータをアップデートする
			if (no.getWork() != MiningManager.getWork()) {
				MiningManager.receptWork(no);
//				FrontendServer.inquiryBackend(
//						new NetworkObject(Constant.NetworkObject.TYPE_UTXO_CHECK, Constant.NetworkObject.VALUE_DIFF));
//				FrontendServer.inquiryBackend(
//						new NetworkObject(Constant.NetworkObject.TYPE_BLOCK_CHECK, Constant.NetworkObject.VALUE_ALL));
			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_UTXO) {
			Log.log("[FrontendServer.receptNetworkObject()] WHOLE UTXO RECEPT!", Constant.Log.STRONG);
			DataManager.overwriteUTXO(no.getUTXO());
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_UTXO_SPENT_HASH) {
			Log.log("[FrontendServer.receptNetworkObject()] UTXO_SPENT_HASH RECEPT!", Constant.Log.STRONG);
			DataManager.updateUTXO(no.getUTXO(), no.getSpent());
//			if (!DataManager.checkUTXOHash(no.getHash())) {
//				FrontendServer.inquiryBackend(
//						new NetworkObject(Constant.NetworkObject.TYPE_UTXO_CHECK, Constant.NetworkObject.VALUE_ALL));
//			}
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_UTXO_BYTE) {
			try {
				Inflater decompresser = new Inflater();
				decompresser.setInput(no.getHash(), 0, no.getHash().length);
				byte[] result = new byte[no.getBlockHeight()];
				int resultLength = decompresser.inflate(result);
				Log.log("resultLength: " + resultLength);
				Log.log("data: " + DatatypeConverter.printHexBinary(result).substring(0, 20));
				decompresser.end();
				DataManager.overwriteUTXO((UTXO) ByteUtil.convertByteToObject(result));
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Decode the bytes into a String

			// } else if (no.getType() == Constant.NetworkObject.TYPE_SPENT) {
			// DataManager.addUTXORemove(no.getSpent());
			// return;
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_ROUTINE
				|| no.getType() == Constant.NetworkObject.TYPE_ROUTINE_REVOKE) {
			DataManager.receptRoutine(no);
			return;
		}
		Log.log("[FrontendTable.receptNetworkObject()] Recept invalid data from [" + remoteIp + "]",
				Constant.Log.EXCEPTION);
		Log.log("[FrontendTable.receptNetworkObject()] Invalid no: " + no.toString(), Constant.Log.EXCEPTION);
	}

	static void inquiryBackend(NetworkObject no) {
		Random rand = new Random();
		Node node;
		int nodeNum = rand.nextInt(backendTable.size());
		Iterator<Entry<String, Node>> it = backendTable.entrySet().iterator();
		for (int i = 0; i < nodeNum && it.hasNext(); i++) {
			it.next();
		}
		node = it.next().getValue();
		char[] ret = access(no, node);
		if (ret == null) {
//			it.remove();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			inquiryBackend(no);
		}
	}

	static void shareBackend(NetworkObject no) {
		if (!Setting.BROADCAST_BACKEND) {
			Log.log("BROADCAST_BACKEND false");
			return;
		}
//		Log.log("[FrontendServer.shareBackend()] no: " + no, Constant.Log.TEMPORARY);
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
//		Log.log("[FrontendServer.shareFrontend()] no: " + no, Constant.Log.TEMPORARY);
		for (Iterator<Node> it = frontendTable.values().iterator(); it.hasNext();) {
			if (access(no, it.next()) == null) {
				Log.log("[share--end] 3count after remove");
				// it.remove();
			}
		}
	}

	private static char[] access(NetworkObject no, Node node) {
		Socket socket = new Socket();
		Log.log("[FrontendServer.broadcast()] to: " + node.getIp() + ":" + node.getPort());

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
//			Log.log("[FrontendServer.broadcast()] recept: " + new String(cline), Constant.Log.TEMPORARY);
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
			Log.log("[FrontendServer.broadcast()] Cannot connection and detach: " + node.getIp() + ":" + node.getPort(),
					Constant.Log.IMPORTANT);
			return null;
		}
	}
}
