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
//	private Map<ByteBuffer, Map<ByteBuffer, Output>> tableInvalid;
	public UTXO() {
		table = new HashMap<ByteBuffer, Map<ByteBuffer, Output>>();
	}
	public void add(byte[] key, Output out) throws Exception {
		add(key, out, table);
	}
//	public void addInvalid(byte[] key, Output out) throws Exception {
//		add(key, out, tableInvalid);
//	}
	public void add(byte[] addr, Output out, Map<ByteBuffer, Map<ByteBuffer, Output>> addTable) throws Exception {
		Map<ByteBuffer, Output> tmp;
		ByteBuffer buf = ByteBuffer.wrap(addr);
		if(addTable.containsKey(buf)) {
			tmp = addTable.get(buf);
		} else {
			tmp = new HashMap<ByteBuffer, Output>();
			addTable.put(ByteBuffer.wrap(addr), tmp);
		}
		tmp.put(ByteBuffer.wrap(Crypto.hashTwice(ByteUtil.getByteObject(out))), out);
	}
	
	public void remove(byte[] addr, byte[] outHash) {
		ByteBuffer buf = ByteBuffer.wrap(addr);
		if(table.containsKey(buf)) {
			Map<ByteBuffer, Output> tmp = table.get(buf);
			ByteBuffer buf2 = ByteBuffer.wrap(outHash);
			if(tmp.containsKey(buf2)) {
				tmp.remove(buf2);
			}
		}
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
