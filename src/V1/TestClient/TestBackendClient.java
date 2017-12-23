package V1.TestClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Answer;
import V1.Component.Block;
import V1.Component.Input;
import V1.Component.NetworkObject;
import V1.Component.Node;
import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Transaction;
import V1.Component.Work;
import V1.Library.Address;
import V1.Library.Base58;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Constant.Script.OPCode;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.TofuError;

public class TestBackendClient {
	static KeyPair keyPair = null;
	static byte[] address = null;

	public static void main(String[] args) {
		init();
		test1();
//		 makeTrustedServerFile();
		// accessTest();
	}

	static void makeTrustedServerFile() {
		String fileName, ip, nodeName, dirName;
		Node node;
		dirName = "backendServer";
		ip = "212.24.106.144";
		fileName = ip + ".conf";
		nodeName = "euro";
		node = new Node(ip, Constant.Server.SERVER_PORT, nodeName, Setting.getAddress(), Setting.getKeyPair());

		try {
			IO.fileWrite(System.getProperty("user.dir") + File.separator + "data" + File.separator + dirName
					+ File.separator, fileName, ByteUtil.getByteObject(node));
			Log.log(ByteUtil.convertByteToObject(
					IO.readFileToByte(System.getProperty("user.dir") + "\\data\\" + dirName + "\\" + fileName)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void test1() {
		keyPair = Address.createKeyPair();
		System.out.println("private: " + DatatypeConverter.printHexBinary(keyPair.getPrivate().getEncoded()));
		System.out.println("public: " + DatatypeConverter.printHexBinary(keyPair.getPublic().getEncoded()));
		String address = Base58.encode(Address.getAddress(keyPair.getPublic()));
		System.out.println("address: " + address);
		System.out.println("private length: " + keyPair.getPrivate().getEncoded().length);
		System.out.println("public length: " + keyPair.getPublic().getEncoded().length);
		System.out.println("address length: " + address.length());
		String text = "11月には全体が動くようにしたい...";
		byte[] sign = Crypto.sign(keyPair.getPrivate(), keyPair.getPublic(), text.getBytes());
		System.out.println("sign: " + DatatypeConverter.printHexBinary(sign));
		System.out.println("sign length: " + sign.length);
		System.out.println("result: " + Crypto.verify(keyPair.getPublic().getEncoded(), text.getBytes(), sign));
		System.exit(0);
	}

	static void init() {
		try {
			Setting.init();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new TofuError.SettingError("No such algorithm.");
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			throw new TofuError.SettingError("Key is invalid.");
		}

		// keyPair = Address.createKeyPair();
		keyPair = Setting.getKeyPair();
		address = Setting.getAddress();
		Log.log("address hex: " + DatatypeConverter.printHexBinary(Address.getAddress(keyPair.getPublic())),
				Constant.Log.TEMPORARY);
		Log.log("address base58: " + Base58.encode(address), Constant.Log.TEMPORARY);
		Log.log("privateKey: " + DatatypeConverter.printHexBinary(keyPair.getPrivate().getEncoded()),
				Constant.Log.TEMPORARY);
		Log.log("publicKey: " + DatatypeConverter.printHexBinary(keyPair.getPublic().getEncoded()),
				Constant.Log.TEMPORARY);

	}

	static void accessTest() {
		Socket socket = new Socket();

		try {
			InetSocketAddress socketAddress = new InetSocketAddress("0.0.0.0", Constant.Server.SERVER_PORT);
			socket.connect(socketAddress, 30000);
			Log.log("buffersize: " + socket.getSendBufferSize(), Constant.Log.TEMPORARY);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			byte[] data = null;
			OutputStream os = null;
			Transaction tx;
			Block block;
			Node node;

			node = new Node("192.168.56.1", 60303, "test node", Setting.getAddress(), Setting.getKeyPair());

			tx = getTestTransaction();
			block = new Block(1, new byte[] { 0x01, 0x4a, 0x02 }, new byte[] { 0x01, 0x4a, 0x02 });
			block.addTransaction(tx);

			// NetworkObject no = new NetworkObject(Constant.NetworkObject.TX,
			// tx);
			// NetworkObject no = new
			// NetworkObject(Constant.NetworkObject.BLOCK, block);
			// NetworkObject no = new NetworkObject(Constant.NetworkObject.NODE,
			// node);
			NetworkObject no = new NetworkObject(Constant.NetworkObject.TYPE_WORK,
					new Work(new byte[] { 0x01, 0x4a, 0x02 }, new byte[] { 0x01, 0x4a, 0x02 }));

			Log.log("no: " + no, Constant.Log.TEMPORARY);
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
			Log.log(new String(cline), Constant.Log.TEMPORARY);

			baos.close();
			oos.close();
			os.close();
			ir1.close();
			br1.close();
			is1.close();

			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static Transaction getTestTransaction() {
		Input[] in = new Input[1];
		Output[] out = new Output[1];
		int version;
		int lockTime;
		byte[] script = new byte[1 + Constant.Address.BYTE_PUBLIC_KEY];
		script[0] = OPCode.PUSH512;
		System.arraycopy(Setting.getPublicKey(), 0, script, 1, Constant.Address.BYTE_PUBLIC_KEY);
		in[0] = new Input(new byte[] { 0x01, 0x02, 0x03 }, new byte[] { 0x01, 0x02, 0x03 }, new Answer(script));
		out[0] = new Output(1,  new Question(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 5, 6, 7, 8, 9, 10, 11, 12, 13,
				14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 }, Setting.getAddress()));
		version = 0xffff;
		lockTime = 100;
		try {
			return new Transaction(in, out, version, lockTime, keyPair);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TofuError.FatalError("cannot create transaction");
		}
	}
}

