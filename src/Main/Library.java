package Main;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.List;

public class Library {
	static byte[] getByteObject(Object data) {
		byte[] retObject = null;
		try {
			ByteArrayOutputStream byteos = new ByteArrayOutputStream();
			ObjectOutputStream objos = new ObjectOutputStream(byteos);
			objos.writeObject(data);
			objos.close();
			byteos.close();
			retObject = byteos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retObject;
	}
	
	static boolean isDirectory(String dirPath) {
		return (new File(dirPath)).isDirectory();
	}
	
	static boolean fileWrite(String path, byte[] data) {
		try {
			File file = new File(path);

			if (file.isFile() && file.canWrite()) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
				pw.print(data);
				pw.close();
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;		
	}
}
