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
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Base58;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;

public class UTXO implements Externalizable {
	/**
	 * Map(receiver, Map<utxoHash, Output>
	 */
	private HashMap<ByteBuffer, Map<ByteBuffer, Output>> table;

	// private Map<ByteBuffer, Map<ByteBuffer, Output>> tableInvalid;
	public UTXO() {
		table = new HashMap<ByteBuffer, Map<ByteBuffer, Output>>();
	}

	// answerScriptHashはoutHashが重複するのを防ぐため
	public boolean add(byte[] key, Output out, byte[] answerScriptHash) throws Exception {
		out.setAnswerScriptHash(answerScriptHash);
		return add(key, out, table);
	}

	// public void addInvalid(byte[] key, Output out) throws Exception {
	// add(key, out, tableInvalid);
	// }
	public boolean add(byte[] addr, Output out, Map<ByteBuffer, Map<ByteBuffer, Output>> addTable) throws Exception {
		Map<ByteBuffer, Output> tmp;
		ByteBuffer buf = ByteBuffer.wrap(addr);
		if (addTable.containsKey(buf)) {
			tmp = addTable.get(buf);
			// すでに存在する場合は重複して消えてしまうため拒否
			if (tmp.containsKey(ByteBuffer.wrap(addr))) {
				return false;
			}
		} else {
			tmp = new HashMap<ByteBuffer, Output>();
			addTable.put(ByteBuffer.wrap(addr), tmp);
		}
		tmp.put(ByteBuffer.wrap(Crypto.hashTwice(ByteUtil.getByteObject(out))), out);
		return true;
	}

	public void remove(byte[] addr, byte[] outHash) {
		ByteBuffer buf = ByteBuffer.wrap(addr);
		if (table.containsKey(buf)) {
			Map<ByteBuffer, Output> tmp = table.get(buf);
			ByteBuffer buf2 = ByteBuffer.wrap(outHash);
			if (tmp.containsKey(buf2)) {
				tmp.remove(buf2);
			}
		}
	}

	public Map<ByteBuffer, Output> get(ByteBuffer byteBuffer) {
		if (table.containsKey(byteBuffer)) {
			return table.get(byteBuffer);
		} else {
			return null;
		}
	}

	public Map<ByteBuffer, Map<ByteBuffer, Output>> getAll() {
		return table;
	}

	public boolean checkAndRemoveAll(Map<ByteBuffer, Set<ByteBuffer>> removeComponent) {
		// 削除するUTXOがないなら不正
		for (Entry<ByteBuffer, Set<ByteBuffer>> entry : removeComponent.entrySet()) {
			if (table.containsKey(entry.getKey())) {
				Map<ByteBuffer, Output> map = table.get(entry.getKey());
				for (ByteBuffer outHash : entry.getValue()) {
					if (map.containsKey(outHash)) {
						// まずはあるかどうか確認
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		for (Entry<ByteBuffer, Set<ByteBuffer>> entry : removeComponent.entrySet()) {
			if (table.containsKey(entry.getKey())) {
				Map<ByteBuffer, Output> map = table.get(entry.getKey());
				for (ByteBuffer outHash : entry.getValue()) {
					map.remove(outHash);
				}
			}
		}
		return true;
	}

	public void removeAll(Map<ByteBuffer, Set<ByteBuffer>> removeComponent) {
		for (Entry<ByteBuffer, Set<ByteBuffer>> entry : removeComponent.entrySet()) {
			if (table.containsKey(entry.getKey())) {
				Map<ByteBuffer, Output> map = table.get(entry.getKey());
				for (ByteBuffer outHash : entry.getValue()) {
					map.remove(outHash);
				}
			}
		}
	}

	synchronized public void addAll(Map<ByteBuffer, Map<ByteBuffer, Output>> newComponent) {
		for (Entry<ByteBuffer, Map<ByteBuffer, Output>> entry : newComponent.entrySet()) {
			Map<ByteBuffer, Output> map;
			if (table.containsKey(entry.getKey())) {
				map = table.get(entry.getKey());
			} else {
				map = new HashMap<ByteBuffer, Output>();
				table.put(entry.getKey(), map);
			}
			for (Entry<ByteBuffer, Output> newMap : entry.getValue().entrySet()) {
				map.put(newMap.getKey(), newMap.getValue());
			}

		}
	}

	synchronized public boolean checkAndAddAll(Map<ByteBuffer, Map<ByteBuffer, Output>> newComponent) {
		// まずは同じoutHashがないか確認
		for (Entry<ByteBuffer, Map<ByteBuffer, Output>> entry : newComponent.entrySet()) {
			if (table.containsKey(entry.getKey())) {
				Map<ByteBuffer, Output> map = table.get(entry.getKey());
				for (Entry<ByteBuffer, Output> newMap : entry.getValue().entrySet()) {
					if (map.containsKey(newMap.getKey())) {
						// 既に存在する場合は追加すると消えてしまう
//						Log.log("[UTXO.addAll()] outHash already exists: "
//								+ DatatypeConverter.printHexBinary(newMap.getKey().array()) + "\toutput: "
//								+ newMap.getValue(), Constant.Log.IMPORTANT);
						return false;
					}
				}
			}
		}
		for (Entry<ByteBuffer, Map<ByteBuffer, Output>> entry : newComponent.entrySet()) {
			Map<ByteBuffer, Output> map;
			if (table.containsKey(entry.getKey())) {
				map = table.get(entry.getKey());
			} else {
				map = new HashMap<ByteBuffer, Output>();
				table.put(entry.getKey(), map);
			}
			for (Entry<ByteBuffer, Output> newMap : entry.getValue().entrySet()) {
				map.put(newMap.getKey(), newMap.getValue());
			}

		}
		return true;
	}

	public void clear() {
		table.clear();
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		Map<byte[], Map<byte[], Output>> map;
		map = (Map<byte[], Map<byte[], Output>>) oi.readObject();
		for (Entry<byte[], Map<byte[], Output>> entry : map.entrySet()) {
			HashMap<ByteBuffer, Output> txHashMap = new HashMap<ByteBuffer, Output>();
			for (Entry<byte[], Output> tmp : entry.getValue().entrySet()) {
				txHashMap.put(ByteBuffer.wrap(tmp.getKey()), tmp.getValue());
			}
			table.put(ByteBuffer.wrap(entry.getKey()), txHashMap);
		}
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		Map<byte[], Map<byte[], Output>> map = new HashMap<byte[], Map<byte[], Output>>();
		for (Entry<ByteBuffer, Map<ByteBuffer, Output>> entry : table.entrySet()) {
			Map<byte[], Output> txHashMap = new HashMap<byte[], Output>();
			for (Entry<ByteBuffer, Output> tmp : entry.getValue().entrySet()) {
				txHashMap.put(tmp.getKey().array(), tmp.getValue());
			}
			map.put(entry.getKey().array(), txHashMap);
		}
		oo.writeObject(map);
	}

	public String toExplainString() {
		String tableStr = "";
		for (Entry<ByteBuffer, Map<ByteBuffer, Output>> entry : table.entrySet()) {
			tableStr += "[addr: " + Base58.encode(entry.getKey().array()).substring(0, 5) + ", outputList: [";
			for (Entry<ByteBuffer, Output> outMap : entry.getValue().entrySet()) {
				tableStr += "[outHash: " + DatatypeConverter.printHexBinary(outMap.getKey().array()).substring(0, 5)
						+ ", amount: " + outMap.getValue().getAmount() + "]";
			}
			tableStr += "]] ";
		}
		return "[table: " + tableStr + "]";
	}

	public String toString() {
		String tableStr = "";
		for (Entry<ByteBuffer, Map<ByteBuffer, Output>> entry : table.entrySet()) {
			tableStr += "[addr: " + DatatypeConverter.printHexBinary(entry.getKey().array()) + ", output: "
					+ entry.getValue() + "]";
		}
		return "[table: " + tableStr + "]";
	}
}
