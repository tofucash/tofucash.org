package V1.Library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.omg.CORBA_2_3.portable.InputStream;

public class IO {
	public static boolean isDirectory(String dirPath) {
		return (new File(dirPath)).isDirectory();
	}

	public static boolean isFile(String path) {
		return (new File(path)).isFile();
	}

	// public static boolean createFile(String path) {
	// File file = new File(path);
	// if(!file.isFile()) {
	// return false;
	// }
	// if(!file.exists()) {
	// try {
	// file.createNewFile();
	// } catch (IOException e) {
	// Log.log("cannot create file", Constant.Log.IMPORTANT);
	// e.printStackTrace();
	// return false;
	// }
	// }
	// return true;
	// }

	// public static boolean fileWrite(String path, String content) {
	// try {
	// File file = new File(path);
	// if (!file.exists()) {
	// file.createNewFile();
	// }
	// if (file.isFile() && file.canWrite()) {
	// PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file,
	// false)));
	// pw.print(content);
	// pw.close();
	// } else {
	// Log.log("cannot write file", Constant.Log.IMPORTANT);
	// }
	// } catch (Exception e) {
	// Log.log("cannot write file", Constant.Log.IMPORTANT);
	// e.printStackTrace();
	// }
	// }

	public static void fileWrite(String path, byte[] data) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(data);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.log("cannot write file", Constant.Log.IMPORTANT);
		}
	}

	public static byte[] readFileToByte(String path) throws Exception {
		byte[] b = new byte[Constant.IO.BYTE_BUF];
		FileInputStream fis = new FileInputStream(path);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (fis.read(b) > 0) {
			baos.write(b);
		}
		baos.close();
		fis.close();
		b = baos.toByteArray();

		return b;
	}

	public static String fileReadAll(String path) {
		StringBuilder builder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String string = reader.readLine();
			while (string != null) {
				builder.append(string + System.getProperty("line.separator"));
				string = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new TofuError.FatalError("cannnot read file [" + path + "]");
		}
		return builder.toString();
	}

	public static Object createSendMessageObject(byte[] objByte) {
		Object obj = null;
		try {
			ByteArrayInputStream byteis = new ByteArrayInputStream(objByte);
			ObjectInputStream objis = new ObjectInputStream(byteis);
			obj = objis.readObject();
			byteis.close();
			objis.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}

}
