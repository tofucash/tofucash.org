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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import V1.Component.NetworkObject;
import V1.Component.Node;
import V1.Component.Report;
import V1.Component.Request;
import V1.Component.Spent;
import V1.Component.Transaction;
import V1.Component.UTXO;
import V1.Component.Work;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Constant.Verify.Result;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.TofuError;

public class HashServer extends Thread {
	private static List<String> blacklist;
	private static Map<String, Integer> accessTable;

	static void init() {
		blacklist = new ArrayList<String>();
		accessTable = new HashMap<String, Integer>();
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(Constant.Server.HASH_SERVER_PORT);
			Log.log("Server ready.");

			while (true) {
				try {
					Socket soc = ss.accept();
					String remoteIp = soc.getRemoteSocketAddress().toString().replaceAll("/(.*):.*", "$1");
					Log.log("[access] " + remoteIp);
					if (blacklist.contains(remoteIp)) {
						Log.log("[Blacklist node] access denied not trusted ip [" + remoteIp + "]");
						soc.close();
					} else {
						if (accessTable.containsKey(remoteIp)) {
							int cnt = accessTable.get(remoteIp);
							accessTable.put(remoteIp, cnt++);
							if (cnt > Constant.Server.MAX_ACCESS_PER_DAY) {
								blacklist.add(remoteIp);
							}
						} else {
							accessTable.put(remoteIp, 1);
						}
						new Client(soc, remoteIp).start();
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
			Work work = MiningManager.getWork();
			String json = "{\"target\": \"" + DatatypeConverter.printHexBinary(work.getTarget()) + "\", \"hash\": \""
					+ DatatypeConverter.printHexBinary(work.getHash()) + "\", \"subTarget\": \""
									+ DatatypeConverter.printHexBinary(work.getSubTarget()) + "\", \"fAddress\": \""
											+ DatatypeConverter.printHexBinary(work.getFAddress()) + "\", \"start\": \""
					+ DatatypeConverter.printHexBinary(MiningManager.getNextNonce(remoteIp)) + "\", \"cnt\": \""
					+ Constant.Server.NONCE_CNT + "\", \"algo\": \"" + Constant.Server.HASH_ALGO + "\"}";
			int readBytes = 0;
			String receptBody = "";
			Result result = null;
			Report report = null;
			Request request = null;

			try {
				br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())));
				while (true) {
					try {
						Thread.sleep(Constant.Server.SERVER_READ_SLEEP);
						is = soc.getInputStream();
						isr = new InputStreamReader(is);
						baos = new ByteArrayOutputStream();
						byte[] buffer = new byte[4096];
						byte[] data;
						int available = is.available();
						if (available > 0 && (readBytes += is.read(buffer)) > 1) {
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
							Log.log("[HashServer.Client.run()]: HashServer", Constant.Log.EXCEPTION);
						} catch (Exception ex) {
							Log.log("[HashServer.Client.run()]: HashServer", Constant.Log.EXCEPTION);
							ex.printStackTrace();
						}
						break;
					}
				}
				byte[] recept = new byte[readBytes];
				System.arraycopy(bbuf.array(), 0, recept, 0, readBytes);
				receptBody = new String(recept).replaceAll(".*\r\n", "");
				Log.log("[HashServer.Client.run()] receptBody: " + receptBody);
				if (!receptBody.equals("")) {
					if ((report = MiningManager.verifyMining(receptBody, remoteIp)) != null) {
						Log.log("[HashServer.Client.run()] report: " + report, Constant.Log.IMPORTANT);
					} else if ((request = DataManager.verifyRequest(receptBody)) != null) {
						Log.log("[HashServer.Client.run()] request: " + request, Constant.Log.TEMPORARY);
						if (request.getType() == Constant.Request.TYPE_SEND_TOFU) {
						} else if (request.getType() == Constant.Request.TYPE_CHECK_BALANCE) {
							json = DataManager.getBalance(request);
						} else if(request.getType() == Constant.Request.TYPE_CHECK_TX) {
							json = DataManager.getTransactionInfo(request);
						} else if(request.getType() - Constant.Request.TYPE_ROUTINE < 1000  ) {
							json = DataManager.getRoutineInfo(request);
						} else {
							throw new TofuError.UnimplementedError("[HashServer.Client.run()]  Unknown Request Type");
						}
					}
				}
				Log.loghr("[HashServer.Client.run()] recept -------------------------\n" + receptBody,
						Constant.Log.TEMPORARY);

				pw.write("HTTP/1.1 200 OK\r\nAccess-Control-Allow-Origin: *\r\n\r\n" + json);
				Log.loghr(
						"[HashServer.Client.run()] send ---------------------------\n"
								+ "HTTP/1.1 200 OK\r\nAccess-Control-Allow-Origin: *\r\n\r\n" + json,
						Constant.Log.TEMPORARY);
				pw.flush();
				br.close();
				pw.close();
				isr.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.log("[HashServer.Client.run()]: ", Constant.Log.EXCEPTION);
				try {
					pw.flush();
					br.close();
					pw.close();
					isr.close();
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					Log.log("[HashServer.Client.run()]: ", Constant.Log.EXCEPTION);
				}
			}
			if (report != null) {			// マイニング関連のとき
				FrontendServer.shareBackend(new NetworkObject(Constant.NetworkObject.TYPE_REPORT, report));
			} else if (request != null) {			// リクエストのとき
				if (request.getType() == Constant.Request.TYPE_SEND_TOFU) {
					if(DataManager.balanceEnough(request)) {
						DataManager.addRequestPool(request);
					}
				} else if(request.getType() == Constant.Request.TYPE_CHECK_TX) {
				} else if(request.getType() == Constant.Request.TYPE_CHECK_BALANCE) {
				} else if(request.getType() - Constant.Request.TYPE_ROUTINE < 1000  ) {
					if(DataManager.balanceEnough(request)) {
						DataManager.registerRoutine(request);
					}
				} else {
					throw new TofuError.UnimplementedError("Unknown Request Type");
				}
			}
		}
	}

	static byte[] calcNonceStart(byte[] hash, String remoteIp) {
		ByteBuffer bbuf = ByteBuffer.allocate(Constant.NetworkObject.BYTE_MAX_NONCE + 15);
		// 255.255.255.255 <- ip address max is 15byte ... IPv6??
		bbuf.put(hash);
		bbuf.put(remoteIp.getBytes());
		return Crypto.hash512(bbuf.array());
	}
}
