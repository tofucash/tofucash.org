package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

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
		table = (Map) oi.readObject();
	}
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(table);
	}
}
