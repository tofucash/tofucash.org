package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyPair;

import javax.xml.bind.DatatypeConverter;

public class Tofucoin {
	public static void main(String[] args) {
		// 文字色を変える
		for (int i = 0; i < 8; i++)
			System.out.println("\u001b[00;3" + i + "m esc[00;3" + i + " \u001b[00m");
		// 背景色を変える
		for (int i = 0; i < 8; i++)
			System.out.println("\u001b[00;4" + i + "m esc[00;4" + i + " \u001b[00m");
		Log.log("IMPORTANT", Constant.Log.IMPORTANT);
		Log.log("EXCEPTION", Constant.Log.EXCEPTION);
		Log.log("TEMPORARY", Constant.Log.TEMPORARY);
		Server server = null;

		init();
		byte[] privateKey = Address.createPrivateKey();
		System.out.println("privateKey: " + DatatypeConverter.printHexBinary(privateKey));
		
		KeyPair kp = Address.createPublicKey();
		System.out.println("privateKey: " + DatatypeConverter.printHexBinary(kp.getPrivate().getEncoded()));
		System.out.println("publicKey: " + DatatypeConverter.printHexBinary(kp.getPublic().getEncoded()));

		System.exit(0);
		
		server = new Server();
		server.start();

		Log.log("Access...", Constant.Log.TEMPORARY);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		access();
	}

	static void access() {
		Socket socket = new Socket();

		try {
			InetSocketAddress socketAddress = new InetSocketAddress("0.0.0.0", 8081);
			socket.connect(socketAddress, 30000);
			System.out.println("buffersize: " + socket.getSendBufferSize());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			byte[] data = null;
			OutputStream os = null;
			Transaction tx = new Transaction();
			tx.test();
			Block block = new Block();
			NetworkObject no = new NetworkObject(Constant.NetworkObject.BLOCK, block);
			try {
				// オブジェクトをバイト配列化
				oos = new ObjectOutputStream(baos);
				oos.writeObject(no);
				data = baos.toByteArray();

				// ソケットを作成してバイト配列をサーバに送信する。
				os = socket.getOutputStream();
				os.write(data);
				os.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

			InputStream is1 = socket.getInputStream();
			InputStreamReader ir1 = new InputStreamReader(is1, "UTF-8");
			BufferedReader br1 = new BufferedReader(ir1);

			// よみこめるまでまってる
			while (is1.available() == 0)
				;

			// はじめに読み込んだものを書き出す
			char[] cline = new char[is1.available()];
			br1.read(cline);
			System.out.println(cline);

			// クローズ
			baos.close();
			oos.close();
			br1.close();
			ir1.close();

			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static void init() {
		Log.init();
		Setting.init();
		Blockchain.init();

		Log.loghr("Tofucoin init completed.");
	}
}
