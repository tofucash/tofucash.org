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
		try {
			Setting.init();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			return;
		}
		makeTrustedServerFile();
	}
	static void makeTrustedServerFile() {
		String fileName, ip, nodeName, dirName;
		Node node;
//		dirName = "frontendServer";
		dirName = "backendServer";
		ip = "133.18.56.150";
//		ip = "36.55.231.15";
		fileName = ip + ".conf";
		nodeName = "vps5";
		node = new Node(ip, Constant.Server.SERVER_PORT, nodeName, Setting.getAddress(), Setting.getKeyPair());

		try {
			IO.fileWrite(System.getProperty("user.dir") + File.separator + "data" + File.separator + dirName
					+ File.separator, fileName, ByteUtil.getByteObject(node));
			Log.log(ByteUtil.convertByteToObject(
					IO.readFileToByte(System.getProperty("user.dir") + File.separator + "data" + File.separator + dirName + File.separator + fileName)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
