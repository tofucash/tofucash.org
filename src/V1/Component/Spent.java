package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;

public class Spent implements Externalizable {
	private Map<byte[], byte[]> table;
	public Spent() {
		table = null;
	}
	public Spent(String[] addrFrom, String[] outHash) {
		table = new HashMap<byte[], byte[]>();
		for(int i = 0; i < outHash.length && i < addrFrom.length; i++) {
			table.put(DatatypeConverter.parseHexBinary(addrFrom[i]), DatatypeConverter.parseHexBinary(outHash[i]));
		}
	}
	public Map<byte[], byte[]> getSpentTable() {
		return table;
	}
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int tableSize = oi.readInt();
		table = new HashMap<byte[], byte[]>();
		for(int i = 0; i < tableSize; i++) {
			int keyLength = oi.readInt();
			if(keyLength > Constant.Address.BYTE_ADDRESS) {
				return;
			}
			byte[] key = new byte[keyLength];
			oi.read(key);
			
			int valueLength = oi.readInt();
			if(valueLength > Constant.Transaction.BYTE_OUT_HASH) {
				return;
			}
			byte[] value = new byte[valueLength];
			oi.read(value);
			table.put(key, value);
		}
	}
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(table.size());
		for(Entry<byte[], byte[]> entry: table.entrySet()) {
			byte[] key = entry.getKey();
			oo.writeInt(key.length);
			oo.write(key);
			oo.writeInt(entry.getValue().length);
			oo.write(entry.getValue());
		}
	}
}
