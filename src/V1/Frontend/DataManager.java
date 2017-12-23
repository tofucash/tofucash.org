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

	static Transaction makeTx(Request request) {
		int[] amountFrom = request.getAmountFrom();
		int[] amountTo = request.getAmountTo();
		String[] addrFromStr = request.getAddrFrom();
		String[] addrToStr = request.getAddrTo();
		String[] outHashStr = request.getOutHash();
		String[] answerScriptStr = request.getAnswerScript();
		String[] questionScriptStr = request.getQuestionScript();
		byte[][] addrFrom = new byte[addrFromStr.length][];
		byte[][] addrTo = new byte[addrToStr.length][];
		for (int i = 0; i < addrFromStr.length; i++) {
			addrFrom[i] = DatatypeConverter.parseHexBinary(addrFromStr[i]);
		}
		for (int i = 0; i < addrToStr.length; i++) {
			addrTo[i] = DatatypeConverter.parseHexBinary(addrToStr[i]);
		}
		byte[][] outHash = new byte[outHashStr.length][];
		for (int i = 0; i < outHashStr.length; i++) {
			outHash[i] = DatatypeConverter.parseHexBinary(outHashStr[i]);
		}
		byte[][] answerScript = new byte[answerScriptStr.length][];
		for (int i = 0; i < answerScriptStr.length; i++) {
			answerScript[i] = DatatypeConverter.parseHexBinary(answerScriptStr[i]);
		}
		byte[][] questionScript = new byte[questionScriptStr.length][];
		for (int i = 0; i < questionScriptStr.length; i++) {
			questionScript[i] = DatatypeConverter.parseHexBinary(questionScriptStr[i]);
		}
		Input[] in = new Input[amountFrom.length];
		Output[] out = new Output[amountTo.length];
		for (int i = 0; i < in.length; i++) {
			in[i] = new Input(outHash[i], addrFrom[i], new Answer(answerScript[i]));
		}
		for (int i = 0; i < out.length; i++) {
			out[i] = new Output(amountTo[i], new Question(questionScript[i], addrTo[i]));
		}
		int version = request.getVersion();
		int lockTime = request.getLockTime();
		byte[] signature = DatatypeConverter.parseHexBinary(request.getSignature());
		Transaction tx = new Transaction(in, out, version, lockTime, signature);
		tx.removeNull();
		return tx;
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
