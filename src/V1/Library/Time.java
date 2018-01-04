package V1.Library;

import java.nio.ByteBuffer;

public class Time {
//	public static byte[] getCurrentDate() {
//		ByteBuffer buf = ByteBuffer.allocate(64);
//		byte[] date = new byte[Constant.Time.BYTE_TIMESTAMP];
//		System.arraycopy(buf.putLong(System.currentTimeMillis() / 1000).array(), 64-Constant.Time.BYTE_TIMESTAMP, date, 0, Constant.Time.BYTE_TIMESTAMP);
//		return date;
//	}
	public static long getTimestamp() {
		return System.currentTimeMillis()/1000;
	}
}
