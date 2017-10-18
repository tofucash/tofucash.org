package Main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
}
