package V1.Frontend;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Answer;
import V1.Component.Input;
import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Request;
import V1.Component.Transaction;
import V1.Component.UTXO;
import V1.Library.Base58;
import V1.Library.Constant;
import V1.Library.Log;
import V1.Library.TofuException.AddressFormatException;
import net.arnx.jsonic.JSON;

public class DataManager {
	private static UTXO utxoTable;

	static void init() {
		utxoTable = new UTXO();
	}


	static String getBalance(Request request) {
		String json = "";
		String[] addr = request.getAddrFrom();
		List<List<Map<String, String>>> hashBalance = new ArrayList<List<Map<String, String>>>();
		int i = 0;
		for (i = 0; i < addr.length; i++) {
			Map<ByteBuffer, Output> utxo;
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			try {
				utxo = utxoTable.get(ByteBuffer.wrap(Base58.decode(addr[i])));
			} catch (AddressFormatException e) {
				e.printStackTrace();
				Log.log("[DataManager.getBalance()] Invalid address: " + new String(addr[i]));
				hashBalance.add(list);
				continue;
			}
			if (utxo != null) {
				int j = 0;
				Log.log("[DataManager.getBalance()] utxo.size(): " + utxo.size(), Constant.Log.TEMPORARY);
				for (Entry<ByteBuffer, Output> entry : utxo.entrySet()) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("outHash", DatatypeConverter.printHexBinary(entry.getKey().array()));
					map.put("amount", "" + entry.getValue().getAmount());
					list.add(map);
					j++;
				}
			} else {
				Log.log("[DataManager.getBalance()]UTXO not Exists: " + addr[i], Constant.Log.TEMPORARY);
			}
			hashBalance.add(list);
		}
		return JSON.encode(hashBalance);
	}

	static void addUTXO(UTXO utxo) {
		utxoTable.addAll(utxo);
		Log.log("[UTXO.addUTXO()] Update utxoTable: " + utxoTable, Constant.Log.TEMPORARY);
	}
}
