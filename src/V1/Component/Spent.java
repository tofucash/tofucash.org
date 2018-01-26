package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Log;

public class Spent implements Externalizable {
	private Map<ByteBuffer, Set<ByteBuffer>> table;

	public Spent() {
		table = new HashMap<ByteBuffer, Set<ByteBuffer>>();
	}

	public Spent(String[] addrFrom, String[] outHash) {
		table = new HashMap<ByteBuffer, Set<ByteBuffer>>();
		for (int i = 0; i < outHash.length && i < addrFrom.length; i++) {
			add(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(addrFrom[i])), ByteBuffer.wrap(DatatypeConverter.parseHexBinary(outHash[i])));
		}
	}
	public void addAll(Map<ByteBuffer, Set<ByteBuffer>> newComponent) {
		for(Entry<ByteBuffer, Set<ByteBuffer>> entry: newComponent.entrySet()) {
			Set<ByteBuffer> set;
			if(table.containsKey(entry.getKey())) {
				set = table.get(entry.getKey());
			} else {
				set = new HashSet<ByteBuffer>();
				table.put(entry.getKey(), set);
			}
			for(ByteBuffer buf: entry.getValue()) {
				set.add(buf);				
			}
			
		}
	}

	public void add(ByteBuffer addrFromBuf, ByteBuffer outHashBuf) {
		Set<ByteBuffer> set;
		if (table.containsKey(addrFromBuf)) {
			set = table.get(addrFromBuf);
		} else {
			set = new HashSet<ByteBuffer>();
			table.put(addrFromBuf, set);
		}
		set.add(outHashBuf);
	}

	public Map<ByteBuffer, Set<ByteBuffer>> getAll() {
		return table;
	}
	public Set<ByteBuffer> get(ByteBuffer addrFrom) {
		return table.get(addrFrom);
	}
	public void clear() {
		table = new HashMap<ByteBuffer, Set<ByteBuffer>>();
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int tableSize = oi.readInt();
		table = new HashMap<ByteBuffer, Set<ByteBuffer>>();
		for (int i = 0; i < tableSize; i++) {
			int keyLength = oi.readInt();
			if (keyLength > Constant.Address.BYTE_ADDRESS) {
				return;
			}
			if(keyLength < 0) {
				continue;
			}
			byte[] key = new byte[keyLength];
			oi.read(key);

			int setLength = oi.readInt();
			if (setLength > Constant.Block.MAX_TX) {
				return;
			}
			Set<ByteBuffer> set = new HashSet<ByteBuffer>();
			for (int j = 0; j < setLength; j++) {
				int valueLength = oi.readInt();
				if (valueLength > Constant.Transaction.BYTE_OUT_HASH) {
					return;
				}
				if(valueLength < 0) {
					continue;
				}
				byte[] value = new byte[valueLength];
				oi.read(value);
				set.add(ByteBuffer.wrap(value));
			}
			table.put(ByteBuffer.wrap(key), set);
		}
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(table.size());
		for (Entry<ByteBuffer, Set<ByteBuffer>> entry : table.entrySet()) {
			byte[] key = entry.getKey().array();
			oo.writeInt(key.length);
			if(key.length < 0) {
				continue;
			}
			oo.write(key);
			oo.writeInt(entry.getValue().size());
			for (ByteBuffer buf : entry.getValue()) {
				oo.writeInt(buf.array().length);
				if(buf.array().length < 0) {
					continue;
				}
				oo.write(buf.array());
			}
		}
	}
	public String toString() {
		String str = "[table: ";
		for(Entry<ByteBuffer, Set<ByteBuffer>> entry: table.entrySet()) {
			str += "[addrFrom: " + DatatypeConverter.printHexBinary(entry.getKey().array())+ ", outHash: [";
			for(ByteBuffer outHash: entry.getValue()) {
				str += DatatypeConverter.printHexBinary(outHash.array()) + " ";
			}
			str += "]] ";
		}
		str += "]";
		return str;
	}
	public String toExplainString() {
		String str = "[table: ";
		for(Entry<ByteBuffer, Set<ByteBuffer>> entry: table.entrySet()) {
			str += "[addrFrom: " + DatatypeConverter.printHexBinary(entry.getKey().array()).substring(0, 5)+ ", outHash: [";
			for(ByteBuffer outHash: entry.getValue()) {
				str += DatatypeConverter.printHexBinary(outHash.array()).substring(0, 5) + " ";
			}
			str += "]] ";
		}
		str += "]";
		return str;
	}
}
