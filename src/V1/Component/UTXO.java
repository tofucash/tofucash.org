package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import V1.Library.ByteUtil;
import V1.Library.Crypto;

public class UTXO implements Externalizable{
	/**
	 * Map(receiver, Map<utxoHash, Output>*/
	private Map<ByteBuffer, Map<ByteBuffer, Output>> table;
	public UTXO() {
		table = new HashMap<ByteBuffer, Map<ByteBuffer, Output>>();
	}
	public void add(byte[] key, Output out) throws Exception {
		Map<ByteBuffer, Output> tmp;
		ByteBuffer buf = ByteBuffer.wrap(key);
		if(table.containsKey(buf)) {
			tmp = table.get(buf);
		} else {
			tmp = new HashMap<ByteBuffer, Output>();
			table.put(ByteBuffer.wrap(key), tmp);
		}
		tmp.put(ByteBuffer.wrap(Crypto.hashTwice(ByteUtil.getByteObject(out))), out);
	}
	public Map<ByteBuffer, Output> get(ByteBuffer byteBuffer) {
		if(table.containsKey(byteBuffer)) {
			return table.get(byteBuffer);
		} else {
			return null;
		}		
	}
	public Map<ByteBuffer, Map<ByteBuffer, Output>> getAll() {
		return table;
	}
	public void addAll(UTXO newUTXO) {
		table.putAll(newUTXO.getAll());
	}
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		Map<byte[], Map<byte[], Output>> map;
		map = (Map) oi.readObject();
		for(Entry<byte[], Map<byte[], Output>> entry: map.entrySet()) {
			Map<ByteBuffer, Output> txHashMap = new HashMap<ByteBuffer, Output>();
			for(Entry<byte[], Output> tmp: entry.getValue().entrySet()) {
				txHashMap.put(ByteBuffer.wrap(tmp.getKey()), tmp.getValue());
			}
			table.put(ByteBuffer.wrap(entry.getKey()), txHashMap);
		}
	}
	public void writeExternal(ObjectOutput oo) throws IOException {
		Map<byte[], Map<byte[], Output>> map = new HashMap<byte[], Map<byte[], Output>>();
		for(Entry<ByteBuffer, Map<ByteBuffer, Output>> entry: table.entrySet()) {
			Map<byte[], Output> txHashMap = new HashMap<byte[], Output>();
			for(Entry<ByteBuffer, Output> tmp: entry.getValue().entrySet()) {
				txHashMap.put(tmp.getKey().array(), tmp.getValue());
			}
			map.put(entry.getKey().array(), txHashMap);
		}
		oo.writeObject(map);
	}
	public String toString() {
		String tableStr = "";
		for(Entry<ByteBuffer, Map<ByteBuffer, Output>> entry: table.entrySet()) {
			tableStr += "[addr: "+DatatypeConverter.printHexBinary(entry.getKey().array()) + ", output: " + entry.getValue()+"]";
		}
		return "[table: "+tableStr+"]";
	}
}
