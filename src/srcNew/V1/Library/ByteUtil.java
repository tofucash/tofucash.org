package V1.Library;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

public class ByteUtil {
	public static boolean contains(List<byte[]> arrays, byte[] other) {
	    for (byte[] b : arrays)
	        if (Arrays.equals(b, other)) return true;
	    return false;
	}
	public static byte[] getByteObject(Object data) throws Exception {
		byte[] retObject = null;
		ByteArrayOutputStream byteos = new ByteArrayOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(byteos);
		objos.writeObject(data);
		objos.close();
		byteos.close();
		retObject = byteos.toByteArray();
		return retObject;
	}

	public static Object convertByteToObject(byte[] objByte) throws Exception {
		Object obj = null;
		ByteArrayInputStream byteis = new ByteArrayInputStream(objByte);
		ObjectInputStream objis = new ObjectInputStream(byteis);
		obj = objis.readObject();
		byteis.close();
		objis.close();
		return obj;
	}

}
