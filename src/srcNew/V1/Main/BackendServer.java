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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import V1.Component.NetworkObject;
import V1.Component.Node;
import V1.Component.Request;
import V1.Component.Transaction;
import V1.Component.Work;
import V1.Library.Address;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.Mining;
import V1.Library.TofuError;
import net.arnx.jsonic.JSON;

public class BackendServer extends Thread{
	private static List<byte[]> receptDataHashList;
	private static Map<String, Node> backendTable;
	private static Map<String, Node> frontendTable;

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

		Log.log("Server init done.");
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(Constant.Server.SERVER_PORT);
			Log.log("BackendServer.init()] ready: port[ " + Constant.Server.SERVER_PORT+" ]");

			while (true) {
				try {
					Socket soc = ss.accept();
					String remoteIp = soc.getRemoteSocketAddress().toString().replaceAll("/(.*):.*", "$1");
					if(backendTable.containsKey(remoteIp) || frontendTable.containsKey(remoteIp)) {
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
						byte[] buffer = new byte[1024];
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
		Log.log("[BackendServer.receptNetworkObject()] no: " + no, Constant.Log.TEMPORARY);
		try {
			byte[] hash = Crypto.hash256(ByteUtil.getByteObject(no));
			if (ByteUtil.contains(receptDataHashList, hash)) {
				Log.log("[BackendServer.receptNetworkObject()] Already recept: " + DatatypeConverter.printHexBinary(hash), Constant.Log.IMPORTANT);
				return;
			} else {
				receptDataHashList.add(hash);
				if (receptDataHashList.size() > Constant.Server.MAX_RECEPT_DATA_HASH_LIST) {
					receptDataHashList.remove(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[BackendServer.receptNetworkObject()] Invalid NetworkObject", Constant.Log.EXCEPTION);
			return;
		}


		if (no.getType() == Constant.NetworkObject.TYPE_TX || no.getType() == Constant.NetworkObject.TYPE_TX_BROADCAST) {
			Blockchain.addTransaction(no);
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_BLOCK
				|| no.getType() == Constant.NetworkObject.TYPE_BLOCK_BROADCAST) {
			Blockchain.addBlock(no);
			return;
		} else if (no.getType() == Constant.NetworkObject.TYPE_NODE
				|| no.getType() == Constant.NetworkObject.TYPE_NODE_BROADCAST) {
			Blockchain.addNode(no);
			if(remoteIp.equals(no.getNode().getIp())) {
				backendTable.put(remoteIp, no.getNode());
				Log.log(backendTable.toString());
				return;
			}
		} else if (no.getType() == Constant.NetworkObject.TYPE_REPORT) {
			if(DataManager.verifyMining(no.getReport())) {
				Blockchain.nonceFound(no.getReport().getNonce(), no.getReport().getMiner());
			}
			return;
		} else if(no.getType() == Constant.NetworkObject.TYPE_REQUEST) {
			// verify request signature
			String signature = no.getRequest().getSignature();
			no.getRequest().setSignature(no.getRequest().getPublicKey());
			String txStr = JSON.encode(no.getRequest());
			no.getRequest().setSignature(signature);
			if (Crypto.verify(Address.getPublicKeyFromByte(DatatypeConverter.parseHexBinary(no.getRequest().getPublicKey()))
					, txStr.getBytes(), DatatypeConverter.parseHexBinary(signature))) {
				Blockchain.addTransaction(new NetworkObject(Constant.Blockchain.TX, DataManager.makeTx(no.getRequest())));
				return;
			}
			Log.log("[BackendServer.receptNetworkObject()] TX signature invalid", Constant.Log.INVALID);
		}
		Log.log("[BackendServer.receptNetworkObject()] Recept invalid data from [" + remoteIp + "]: " + no, Constant.Log.EXCEPTION);
	}
	static void shareBackend(NetworkObject no) {
		if (!Setting.BROADCAST_BACKEND) {
			return;
		}
		Log.log("[FrontendServer.shareBackend()] no: " + no, Constant.Log.TEMPORARY);
		broadcast(no, backendTable);
	}
	static void shareFrontend(NetworkObject no) {
		if (!Setting.BROADCAST_FRONTEND) {
			return;
		}
		if(no.getType() == Constant.NetworkObject.TYPE_WORK || no.getType() == Constant.NetworkObject.TYPE_UTXO) {
			Log.log("[FrontendServer.shareFrontend()] no: " + no, Constant.Log.TEMPORARY);
			broadcast(no, frontendTable);			
		} else {
			Log.log("[BackendServer.shareFrontend()] Invalid Share Data", Constant.Log.IMPORTANT);
		}
	}
	static void broadcast(NetworkObject no, Map<String, Node> remote) {
		for (Iterator<Node> it = remote.values().iterator(); it.hasNext(); ) {
			Node node = it.next();
			Socket socket = new Socket();
			Log.log("[BackendServer.broadcast()] to: " + node.getIp()+":"+node.getPort());

			try {
				InetSocketAddress socketAddress = new InetSocketAddress(node.getIp(), node.getPort());
				socket.connect(socketAddress, 30000);

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

				while (is1.available() == 0)
					;

				char[] cline = new char[is1.available()];
				br1.read(cline);
				Log.log("[BackendServer.broadcast()] recept: " + new String(cline), Constant.Log.TEMPORARY);

				baos.close();
				oos.close();
				os.close();
				ir1.close();
				br1.close();
				is1.close();

				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.log("[BackendServer.broadcast()] Cannot connection and detach: " + node.getIp()+":"+node.getPort(), Constant.Log.IMPORTANT);
				it.remove();
			}
		}
	}
}
