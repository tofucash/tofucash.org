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

import javax.xml.bind.DatatypeConverter;

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
			InputStream is = null;
			InputStreamReader isr = null;
			NetworkObject no = null;
			BufferedReader br = null;
			PrintWriter pw = null;
			ByteArrayOutputStream baos = null;
			ByteBuffer bbuf = ByteBuffer.allocate(Constant.Server.SERVER_BUF);

			try {
				br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sc.getOutputStream())));
				while (true) {
					try {
						Thread.sleep(1000);
						is = sc.getInputStream();
						isr = new InputStreamReader(is);
						baos = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int readBytes = -1;
						byte[] data;
						System.out.println("is.available(): " + is.available());
						if (is.available() <= 0) {
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
				br.close();
				pw.close();
				isr.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.log("[Exception]: Server", Constant.Log.EXCEPTION);
			}
			try {
				ByteArrayInputStream b = new ByteArrayInputStream(bbuf.array());
				ObjectInputStream o = new ObjectInputStream(b);
				no = (NetworkObject) o.readObject();
				o.close();
				b.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			bbuf = null;
			// test
			System.out.println("[Client.run()] no: " + no);

			if (no.getType() == Constant.NetworkObject.TX) {
				Blockchain.addTransaction(no.getTx());
				return;
			} else if (no.getType() == Constant.NetworkObject.BLOCK) {
				Blockchain.addBlock(no.getBlock());
				return;
			}
			Log.log("Recept invalid data.", Constant.Log.EXCEPTION);
			Log.log(no.toString(), Constant.Log.EXCEPTION);
		}
	}

}
