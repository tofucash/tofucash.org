package V1.Main;

import java.io.File;
import java.io.IOException;
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
import V1.Component.NetworkObject;
import V1.Component.Node;
import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Script;
import V1.Component.Transaction;
import V1.Library.Base58;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Constant.Script.OPCode;
import V1.Library.Constant.Script.Result;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.TofuError;
import V1.Library.TofuException;
import V1.Library.TofuException.AddressFormatException;
import net.arnx.jsonic.JSON;

public class Blockchain {

	private static Block block;
	private static int blockHeight;

	private static byte[] difficulty;
	private static int currentTxFee;

	private static Map<Integer, List<byte[]>> prevBlockHashTable;
	private static Map<byte[], Map<Integer, Output>> utxoTable;	

	static void init() {
		blockHeight = 1;

		difficulty = new byte[Constant.Blockchain.BYTE_BLOCK_HASH];
		block = new Block(difficulty);
		currentTxFee = 0;

		prevBlockHashTable = new HashMap<Integer, List<byte[]>>();
		utxoTable = new HashMap<byte[], Map<Integer, Output>>();
		
		Log.log("Blockchain init done.");
	}

	static boolean addTransaction(NetworkObject no) {
		// TODO currently invalid tx should be removed?
		//      should be put into tx pool?
		
		Transaction tx = no.getTx();
		// check transaction input is built based on utxo
		int availableInputSum = 0, outputSum = 0;
		int i = 0, tmp = 0;
		List<byte[]> unavailableOutputList = new ArrayList<byte[]>();
		List<Input> txInputList = new ArrayList<Input>(Arrays.asList(tx.getIn()));
		List<Output> txOutputList = new ArrayList<Output>(Arrays.asList(tx.getOut()));
		for (Iterator<Input> it = txInputList.iterator(); it.hasNext(); i++) {
			if ((tmp = checkTxInput(it, tx, i, unavailableOutputList)) == 0) {
				it.remove();
				break;
			}
			availableInputSum += tmp;
		}
		for (Iterator<Output> it = txOutputList.iterator(); it.hasNext(); i++) {
			Output out = it.next();
			if (out == null) {
				break;
			}
			outputSum += out.getAmount();
		}
		// message
		// unavailable outputlist size() > 0
		Log.log("[addTransaction()] availableSum: " + availableInputSum, Constant.Log.TEMPORARY);

		if (availableInputSum < outputSum - Constant.Blockchain.TX_FEE) {
			return false;
		}

		tx.updateIn(txInputList.toArray(new Input[txInputList.size()]));
		Log.log("block: " + block, Constant.Log.TEMPORARY);

		if(!block.addTransaction(tx)) {
			return false;
		}
		if(no.getType() == Constant.Blockchain.TX) {
			Log.log("Broadcast tx: " + tx, Constant.Log.TEMPORARY);
			BackendServer.shareBackend(new NetworkObject(Constant.Blockchain.TX_BROADCAST, no.getTx()));
		}
		
		MiningManager.updateMining(block);
		
		return true;
	}

	static boolean addBlock(NetworkObject no) {
		// TODO check fork (utxo)
		// TODO check Merkle tree
		// TODO update difficulty
		Block newBlock = no.getBlock();
		int i;
		Iterator<Input> inputIt;
		Iterator<Output> outputIt;
		List<byte[]> unavailableOutputList = new ArrayList<byte[]>();
		for (Transaction tx : newBlock.getTxList()) {
			i = 0;
			for (inputIt = Arrays.asList(tx.getIn()).iterator(); inputIt.hasNext();) {
				if (checkTxInput(inputIt, tx, i, unavailableOutputList) == 0) {
					Log.log("Recept data rejected.", Constant.Log.INVALID);
					return false;
				}
			}
		}
		if (unavailableOutputList.size() != 0) {
			return false;
		}

		try {
			for (Transaction tx : newBlock.getTxList()) {
				for (outputIt = Arrays.asList(tx.getOut()).iterator(); outputIt.hasNext();) {
					Output out = outputIt.next();
					byte[] outputHash = Crypto.hashTwice(ByteUtil.getByteObject(out));
					if (!utxoTable.containsKey(outputHash)) {
						Map<Integer, Output> tmp = new HashMap<Integer, Output>();
						tmp.put(blockHeight, out);
						utxoTable.put(outputHash, tmp);
					} else {
						Map<Integer, Output> tmp = utxoTable.get(outputHash);
						tmp.put(blockHeight, out);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("invalid block data", Constant.Log.EXCEPTION);
			return false;
		}
		
		return goToNextBlock(newBlock, no.getType() == Constant.Blockchain.BLOCK);
	}
	static boolean goToNextBlock(Block newBlock, boolean broadcast) {
		try {
			IO.fileWrite(Setting.BLOCKCHAIN_BIN_DIR + (blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR)
					+ File.separator + blockHeight, ByteUtil.getByteObject(newBlock));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("invalid block data", Constant.Log.EXCEPTION);
			return false;
		}
		// TODO: manage orphan block...
		// TODO: manage utxoTable Version...
		int newBlockHeight = newBlock.getBlockHeight();
		byte[] prevBlockHash = newBlock.getPrevBlockHash();
		if (!prevBlockHashTable.containsKey(newBlockHeight)) {
			List<byte[]> tmp = new ArrayList<byte[]>();
			tmp.add(prevBlockHash);
			prevBlockHashTable.put(newBlockHeight, tmp);
		} else {
			List<byte[]> tmp = prevBlockHashTable.get(newBlockHeight);
			tmp.add(prevBlockHash);
		}
		if (prevBlockHashTable.size() >= Constant.Blockchain.MAX_PREV_BLOCK_HASH_LIST) {
			prevBlockHashTable.remove(blockHeight - Constant.Blockchain.MAX_PREV_BLOCK_HASH_LIST);
		}

		if(broadcast) {
			BackendServer.shareBackend(new NetworkObject(Constant.Blockchain.BLOCK_BROADCAST, newBlock));
		}

		block = new Block(difficulty);
		blockHeight++;

		return true;

	}
	static boolean nonceFound(byte[] nonce, byte[] miner) {
		block.nonceFound(nonce, miner);
		NetworkObject no = new NetworkObject(Constant.Blockchain.BLOCK, block);
				
		return goToNextBlock(block, true);
	}

	static boolean addNode(NetworkObject no) {
		Node node = no.getNode();
		if (!node.checkSig()) {
			Log.log("Access denied invalid node", Constant.Log.INVALID);
			return false;
		}
		if(no.getType() == Constant.Blockchain.NODE) {
			BackendServer.shareBackend(no);
		}
		return true;
	}

	static Transaction getOutTransaction(int blockHeight, byte[] address) {
		Transaction tx = null;

		// to do
		// using Merkle tree

		return tx;
	}

	static int checkTxInput(Iterator<Input> it, Transaction tx, int index, List<byte[]> unavailableOutputList) {
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
		Log.log("new byte[] {0x01, 0x02, 0x03}: " + DatatypeConverter.printHexBinary(new byte[] { 0x01, 0x02, 0x03 }),
				Constant.Log.TEMPORARY);
		Map<Integer, Output> tmp = new HashMap<Integer, Output>();
		byte[] script = new byte[5 + Constant.Address.BYTE_ADDRESS];
		script[0] = OPCode.PUBK_DUP;
		script[1] = OPCode.HASH_TWICE;
		script[2] = OPCode.PUSH256;
		try {
			System.arraycopy(Base58.decode("Be4qVLKM2PtucWukmUUc6s2CrcbQNH7PRnMbcssMwG6S"), 0, script, 3,
					Constant.Address.BYTE_ADDRESS);
		} catch (AddressFormatException e) {
			e.printStackTrace();
			throw new TofuError.SettingError("Addreess is wrong.");
		}
		script[35] = OPCode.EQUAL_VERIFY;
		script[36] = OPCode.CHECK_SIG;
		tmp.put(1, new Output(1, 1, new Question(script)));
		utxoTable.put(ByteBuffer.wrap(new byte[] { 0x01, 0x02, 0x03 }).array(), tmp);
		// tmp.put(1, new Output(1, 1, new Question(new byte[] {
		// Constant.Script.OPCode.POP32_0, Constant.Script.OPCode.TRUE })));
		// utxoTable.put(ByteBuffer.wrap(new byte[] { 0x01, 0x02, 0x03 }), tmp);
		Log.log("utxoList: " + utxoTable, Constant.Log.TEMPORARY);
	}

}
