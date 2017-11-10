package V1.Main;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Block;
import V1.Component.Input;
import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Script;
import V1.Component.Transaction;
import V1.Library.Base58;
import V1.Library.Constant;
import V1.Library.Constant.Script.OPCode;
import V1.Library.Constant.Script.Result;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.TofuError;
import V1.Library.TofuException;
import V1.Library.TofuException.AddressFormatException;

public class Blockchain {

	private final static int FORKABLE_BLOCK_HEIGHT = 3;

	private static Block block;
	static int blockHeight;
	private static List<byte[]> prevBlockHashList;
	private static Map<ByteBuffer, Map<Integer, Output>> utxoTable;

	static void init() {
		block = new Block();
		blockHeight = 1;
		prevBlockHashList = new ArrayList<byte[]>();
		utxoTable = new HashMap<ByteBuffer, Map<Integer, Output>>();
		Log.log("Blockchain init done.");
	}

	static int getForkableBlockHeight() {
		return blockHeight - FORKABLE_BLOCK_HEIGHT;
	}

	static boolean addTransaction(Transaction tx) {
		// check transaction input is built based on utxo
		int availableSum = 0;
		int i = 0, tmp = 0;
		List<byte[]> unavailableOutputList = new ArrayList<byte[]>();
		List<Input> txList = new ArrayList<Input>(Arrays.asList(tx.getIn()));
		for (Iterator<Input> it = txList.iterator(); it.hasNext(); i++) {
			if ((tmp = checkTx(it, tx, i, unavailableOutputList)) == 0) {
				it.remove();
				break;
			}
			availableSum += tmp;
		}
		// message
		// unavailable outputlist size() > 0
		Log.log("[addTransaction()] availableSum: " + availableSum, Constant.Log.TEMPORARY);

		tx.updateIn(txList.toArray(new Input[txList.size()]));
		Log.log("block: " + block, Constant.Log.TEMPORARY);
		return block.addTransaction(tx);
	}

	static void newBlock() {
		block = new Block();
		blockHeight++;
	}

	static boolean addBlock(Block newBlock) {
		// check block height (fork)
		// add to latest block pool
		// testBlockchain.add(newBlock);

		// check txList
		int i;
		Iterator<Input> it;
		List<byte[]> unavailableOutputList = new ArrayList<byte[]>();
		for (Transaction tx : newBlock.getTxList()) {
			i = 0;
			for (it = Arrays.asList(tx.getIn()).iterator(); it.hasNext();) {
				if (checkTx(it, tx, i, unavailableOutputList) == 0) {
					Log.log("Recept data rejected.", Constant.Log.INVALID);
					return false;
				}
			}
		}
		if (unavailableOutputList.size() != 0) {
			return false;
		}
		byte[] prevBlockHash = newBlock.getPrevBlockHash();
		if (!prevBlockHashList.contains(prevBlockHash)) {
			prevBlockHashList.add(prevBlockHash);
		}
		if (prevBlockHashList.size() >= Constant.Blockchain.MAX_PREV_BLOCK_HASH_LIST) {
			prevBlockHashList.remove(0);
		}
		IO.fileWrite(Setting.blockchainBinDir + (blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR)
				+ Constant.Environment.SEPARATOR + blockHeight, IO.getByteObject(newBlock));
		return true;
	}

	static Transaction getOutTransaction(int blockHeight, byte[] address) {
		Transaction tx = null;

		// to do
		// using markle tree

		return tx;
	}

	static int checkTx(Iterator<Input> it, Transaction tx, int index, List<byte[]> unavailableOutputList) {
		Input in = it.next();
		if (in == null) {
			return 0;
		}
		Map<Integer, Output> utxoMap = utxoTable.get(ByteBuffer.wrap(in.getOutTxHash()));
		if (utxoMap == null) {
			return 0;
		}
		Output out = utxoMap.get(in.getOutIndex());
		if (out == null) {
			return 0;
		}

		Script script = new Script();
		Result result = script.resolve(out.getQuestion(), in.getAnswer(), tx, index);
		// このstateをどうするか ethereumを参考に

		Log.log("checkTx() script.resolve() result: " + result, Constant.Log.TEMPORARY);
		
		if (result == Constant.Script.Result.SOLVED) {
			return out.getAmount();
		} else if (result == Constant.Script.Result.FAILED) {
			unavailableOutputList.add(in.getOutTxHash());
		}
		return 0;
	}

	static Block getBlock() {
		return block;
	}

	static void setTestData() {
		Log.log(
				"new byte[] {0x01, 0x02, 0x03}: " + DatatypeConverter.printHexBinary(new byte[] { 0x01, 0x02, 0x03 }), Constant.Log.TEMPORARY);
		Map<Integer, Output> tmp = new HashMap<Integer, Output>();
		byte[] script = new byte[5+Constant.Address.BYTE_ADDRESS];
		script[0] = OPCode.PUBK_DUP;
		script[1] = OPCode.HASH_TWICE;
		script[2] = OPCode.PUSH256;
		try {
			System.arraycopy(Base58.decode("Be4qVLKM2PtucWukmUUc6s2CrcbQNH7PRnMbcssMwG6S"), 0, script, 3, Constant.Address.BYTE_ADDRESS);
		} catch (AddressFormatException e) {
			e.printStackTrace();
			throw new TofuError.SettingError("Addreess is wrong.");
		}
		script[35] = OPCode.EQUAL_VERIFY;
		script[36] = OPCode.CHECK_SIG;
		tmp.put(1, new Output(1, 1, new Question(script)));
		utxoTable.put(ByteBuffer.wrap(new byte[] { 0x01, 0x02, 0x03 }), tmp);
//		tmp.put(1, new Output(1, 1, new Question(new byte[] { Constant.Script.OPCode.POP32_0, Constant.Script.OPCode.TRUE })));
//		utxoTable.put(ByteBuffer.wrap(new byte[] { 0x01, 0x02, 0x03 }), tmp);
		Log.log("utxoList: " + utxoTable, Constant.Log.TEMPORARY);
	}

}
