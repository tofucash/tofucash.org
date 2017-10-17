package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class Server extends Thread {
	void init() {
		Log.log("Server init done.");
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(8081);
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
			NetworkObject no = null;
			BufferedReader br = null;
			PrintWriter pw = null;
			int available;

			try {
				br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (true) {
				try {
					is = sc.getInputStream();
					do {
						available = is.available();
						Thread.sleep(100);
					} while (available == 0);
					byte[] data = new byte[available];
					is.read(data, 0, is.available());
					// バイト配列をオブジェクトに復元
					System.out.println("server available: " + available);
					ByteArrayInputStream b = new ByteArrayInputStream(data);
					ObjectInputStream o = new ObjectInputStream(b);
					no = (NetworkObject) o.readObject();
					pw.println("recept!");
					pw.flush();
					br.close();
					pw.close();
				} catch (Exception e) {
					try {
						pw.close();
						br.close();
						pw.close();
						sc.close();
						Log.log("[Exception]: Server", Constant.Log.IMPORTANT);
						e.printStackTrace();
						break;
					} catch (Exception ex) {
						ex.printStackTrace();
						break;
					}

				}
			}
			System.out.println("recept: " + tx);
			System.out.println("recept: " + block);
			json = new String("");
			tx = Transaction.checkTransaction(json);
			if (tx != null) {
				Blockchain.addTransaction(tx);
				return;
			}
			block = Block.checkBlock(json);
			if (block != null) {
				Blockchain.addBlock(block);
				return;
			}
			Log.log("Recept invalid data.", Constant.Log.INVALID);
			Log.log(json, Constant.Log.INVALID);
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
