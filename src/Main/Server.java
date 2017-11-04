package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server extends Thread {
	void init() {
		Log.log("Server init done.");
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(Constant.Server.SERVER_PORT);
			Log.log("Server ready.");

			while (true) {
				try {
					Socket sc = ss.accept();
					new Client(sc).start();
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
		private Socket sc;

		public Client(Socket s) {
			sc = s;
		}

		public void run() {
			String json;
			Transaction tx = null;
			Block block = null;
			InputStream is;
			InputStreamReader isr = null;
			NetworkObject no = null;
			BufferedReader br = null;
			PrintWriter pw = null;
			ByteArrayOutputStream baos = null;
			ByteBuffer bbuf = ByteBuffer.allocate(Constant.Server.SERVER_BUF);
			
			try {
				br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (true) {
				try {
					Thread.sleep(1000);
					is = sc.getInputStream();
					isr = new InputStreamReader(is);
					baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[8192];
					int readBytes = -1;
					byte[] data;
					System.out.println("is.available(): " + is.available());
					if(is.available() <= 0) {
						break;
					}
					if ((readBytes = is.read(buffer)) > 1) {
						baos.write(buffer, 0, readBytes);
						data = baos.toByteArray();
						bbuf.put(data);
						pw.println("recept!");
						pw.flush();
					}
				} catch (Exception e) {
					try {
						pw.close();
						br.close();
						pw.close();
						sc.close();
						e.printStackTrace();
						Log.log("[Exception]: Server", Constant.Log.IMPORTANT);
						
						
						break;
					} catch (Exception ex) {
						ex.printStackTrace();
						break;
					}

				}
			}
			try {
				br.close();
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.log("[Exception]: Server", Constant.Log.EXCEPTION);
			}


			try {
				ByteArrayInputStream b = new ByteArrayInputStream(bbuf.array());
				ObjectInputStream o = new ObjectInputStream(b);
				no = (NetworkObject) o.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// test
			System.out.println(no);
			Library.fileWrite(Setting.blockchainBinDir+(100/Constant.Blockchain.SAVE_FILE_PER_DIR) + "/" + "last.txt", Library.getByteObject(no));

			
			if (no.getType() == Constant.NetworkObject.TX) {
				System.out.println("???");
				Blockchain.addTransaction(no.getTx());
				return;
			} else if (no.getType() == Constant.NetworkObject.BLOCK) {
				Blockchain.addBlock(no.getBlock());
				return;
			}
			Log.log("Recept invalid data.", Constant.Log.EXCEPTION);
			Log.log(no.toString(), Constant.Log.EXCEPTION);
		}

		private class RamdomStrings {
			private final String stringchar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			private Random rnd = new Random();
			private StringBuffer sbf = new StringBuffer(15);

			public String GetRandomString(int cnt) {
				for (int i = 0; i < cnt; i++) {
					int val = rnd.nextInt(stringchar.length());
					sbf.append(stringchar.charAt(val));
				}

				return sbf.toString();
			}
		}
	}

}
