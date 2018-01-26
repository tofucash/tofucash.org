package V1.TestClient;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import V1.Component.Node;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.IO;
import V1.Library.Log;

public class CreateTrustedServerFile {
	public static void main(String[] args) {
		if (args.length != 3) {
			Log.log("usage: java CreateTrustedServerFile \"f\"/\"b\" \"XX.XX.XX.XX\" \"nodeName\"");
			return;
		}
		try {
			Setting.init();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			return;
		}
		String fileName, ip, nodeName, dirName;
		Node node;
		if (args[0].equals("b")) {
			dirName = "backendServer";
		} else if (args[0].equals("f")) {
			dirName = "frontendServer";
		} else {
			Log.log("usage: java CreateTrustedServerFile \"f\"/\"b\" \"XX.XX.XX.XX\" \"nodeName\"");
			return;
		}
		ip = args[1];
		nodeName = args[2];
		fileName = ip + ".conf";
		node = new Node(ip, Constant.Server.SERVER_PORT, nodeName, Setting.getAddress(), Setting.getKeyPair());

		try {
			IO.fileWrite(System.getProperty("user.dir") + File.separator + ".." + File.separator + "data" + File.separator + dirName
					+ File.separator, fileName, ByteUtil.getByteObject(node));
			Log.log(ByteUtil.convertByteToObject(IO.readFileToByte(System.getProperty("user.dir") + File.separator
					+ ".." + File.separator + "data" + File.separator + dirName + File.separator + fileName)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
