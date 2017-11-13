package V1.Library;

import java.nio.ByteBuffer;

public class Time {
	public static byte[] getCurrentDate() {
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
		byte[] date = new byte[Constant.Time.BYTE_TIMESTAMP];
		System.arraycopy(buf.putLong(System.currentTimeMillis() / 1000).array(), Long.BYTES-Constant.Time.BYTE_TIMESTAMP, date, 0, Constant.Time.BYTE_TIMESTAMP);
		return date;
	}
}
