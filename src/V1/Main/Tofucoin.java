package V1.Main;



import java.io.IOException;
import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;


import V1.Library.Constant;
import V1.Library.Log;
import V1.Library.TofuError;

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
		Log.log("INVALID", Constant.Log.INVALID);
		byte[] bytes = ByteBuffer.allocate(4).putInt(12345).array();
		Log.log("bytes: "+DatatypeConverter.printHexBinary(bytes), Constant.Log.TEMPORARY);
		Log.log("bytes int: "+ByteBuffer.wrap(new byte[] {0x22, 0x21, 0x15, 0x64}).getInt(), Constant.Log.TEMPORARY);

		BackendServer server = null;

		init();

		testInit();

//		byte[] privateKey = Address.createPrivateKey();
//		System.out.println("privateKey: " + DatatypeConverter.printHexBinary(privateKey));
//		
//		KeyPair kp = Address.createPublicKey();
//		System.out.println("privateKey: " + DatatypeConverter.printHexBinary(kp.getPrivate().getEncoded()));
//		System.out.println("publicKey: " + DatatypeConverter.printHexBinary(kp.getPublic().getEncoded()));

//		System.exit(0);
		
		server = new BackendServer();
		server.start();

		Log.log("Server is running.");
	}
	
	private static void testInit() {
		try {
			Blockchain.setTestData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void init() {
		Log.init();
		Blockchain.init();
		DataManager.init();
		try {
			Setting.init();
			BackendServer.init();
		} catch (Exception e) {
			e.printStackTrace();
			throw new TofuError.SettingError("Server init failed.");
		}

		Log.loghr("Tofucoin init completed.");
	}
}
