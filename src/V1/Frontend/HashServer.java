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
import V1.Component.Work;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;

public class HashServer extends Thread {
	private static List<String> blacklist;
	private static Map<String, Integer> accessTable;

	static void init() {
		blacklist = new ArrayList<String>();
		accessTable = new HashMap<String, Integer>();
	}

	static Map<String, Integer> getAccesstable() {
		return accessTable;
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
			String json = "{\"difficulty\": \"" + DatatypeConverter.printHexBinary(work.getDifficulty())
					+ "\", \"hash\": \"" + DatatypeConverter.printHexBinary(work.getHash()) + "\", \"start\": \""
					+ DatatypeConverter.printHexBinary(calcStart(work.getHash(), remoteIp)) + "\", \"cnt\": \""
					+ Constant.Server.NONCE_CNT + "\", \"algo\": \"" + Constant.Server.HASH_ALGO + "\"}";
			int readBytes = 0;

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
						Log.log("[Client.run()] is.available(): " + available, Constant.Log.TEMPORARY);
						if (available > 0 && (readBytes += is.read(buffer)) > 1) {
							baos.write(buffer, 0, readBytes);
							data = baos.toByteArray();
							bbuf.put(data);
							Log.log("data: " + new String(data));
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
							Log.log("[Exception]: Server", Constant.Log.IMPORTANT);

							break;
						} catch (Exception ex) {
							ex.printStackTrace();
							break;
						}

					}
				}
				byte[] recept = new byte[readBytes];
				Log.log("readBytes: " + readBytes, Constant.Log.TEMPORARY);
				System.arraycopy(bbuf.array(), 0, recept, 0, readBytes);
				String receptBody = new String(recept).replaceAll(".*\r\n", "");
				System.out.println("recept: " + receptBody);

				if (!receptBody.equals("")) {
					MiningManager.receptNonce(receptBody);
				}

				pw.write("HTTP/1.1 200 OK\r\nAccess-Control-Allow-Origin: *\r\n\r\n" + json);
				pw.flush();
				br.close();
				pw.close();
				isr.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.log("[Exception]: Server", Constant.Log.EXCEPTION);
			}
			Log.log("[Client.run()] end");
		}
	}

	static byte[] calcStart(byte[] hash, String remoteIp) {
		ByteBuffer bbuf = ByteBuffer.allocate(Constant.NetworkObject.BYTE_MAX_NONCE + 15);
		// 255.255.255.255 <- ip address max is 15byte ... IPv6??
		bbuf.put(hash);
		bbuf.put(remoteIp.getBytes());
		return Crypto.hash512(bbuf.array());
	}
}
