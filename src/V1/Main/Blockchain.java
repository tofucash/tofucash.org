package V1.Main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Answer;
import V1.Component.Block;
import V1.Component.Input;
import V1.Component.NetworkObject;
import V1.Component.Node;
import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Report;
import V1.Component.Request;
import V1.Component.Spent;
import V1.Component.Transaction;
import V1.Component.UTXO;
import V1.Library.Address;
import V1.Library.Base58;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Constant.Script.OPCode;
import V1.Library.Constant.Script.Result;
//import V1.Library.Constant.Script.Result;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.Script;
import V1.Library.Time;
import V1.Library.TofuError;
import V1.Library.TofuException;
import V1.Library.TofuException.AddressFormatException;
import net.arnx.jsonic.JSON;

public class Blockchain {

	private static Block block;
	private static Block nextBlock;
	private static int blockHeight;

	private static int currentTxFee;
	private static int blockReward;
	private static int blockSubReward;
	private static int blockFrontendReward;

	private static Map<Integer, Map<ByteBuffer, Integer>> blockComfirmationTable;
	// 現在のブロックまでのUTXO
	private static UTXO utxoTable;
	// blockのutxoの追加差分
	private static UTXO utxoTableAppend;
	// blockのutxoの消費差分
	private static UTXO utxoTableRemove;
	// nextBlockのutxoの追加差分
	private static UTXO utxoTableAppendNext;
	// nextBlockのutxoの消費差分
	private static UTXO utxoTableRemoveNext;
	// Map<publicKey , Map<signature , Map<tx , invalidInputList>>>>
	// invalidTxTable;
	private static Map<ByteBuffer, Map<ByteBuffer, Map<Transaction, List<Input>>>> invalidTxTable;
	// Map<blockHash, block> blockTable
	private static Map<ByteBuffer, Block> blockTable;
	private static Map<ByteBuffer, ByteBuffer> blockHashOnceTable;
	// 確定したブロック生成時刻のリスト 長さは最大で LENGTH_MAX_BLOCK_TIME_LIST
	private static List<Long> blockTimeList;
	private static Map<Integer, Block> blockchainTable;

	static void init() throws Exception {
		blockComfirmationTable = new HashMap<Integer, Map<ByteBuffer, Integer>>();
		utxoTable = new UTXO();
		utxoTableAppend = new UTXO();
		utxoTableRemove = new UTXO();
		utxoTableAppendNext = new UTXO();
		utxoTableRemoveNext = new UTXO();
		invalidTxTable = new HashMap<ByteBuffer, Map<ByteBuffer, Map<Transaction, List<Input>>>>();
		blockTable = new HashMap<ByteBuffer, Block>();
		blockchainTable = new HashMap<Integer, Block>();
		blockHashOnceTable = new HashMap<ByteBuffer, ByteBuffer>();

		Block genesisBlock = new Block(0);
		genesisBlock.updateHeader(new byte[Constant.Block.BYTE_BLOCK_HASH],
				DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET),
				DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_SUB_TARGET));
		genesisBlock.removeNull();
		byte[] genesisBlockHashOnce = Crypto.hash512(ByteUtil.getByteObject(genesisBlock.getBlockHeader()));
		String nonce = DatatypeConverter.printHexBinary(new byte[Constant.Block.BYTE_NONCE]);
		String fAddress = DatatypeConverter.printHexBinary(new byte[Constant.Address.BYTE_ADDRESS]);
		String cAddress = DatatypeConverter.printHexBinary(new byte[Constant.Address.BYTE_ADDRESS]);
		String dataStr = DatatypeConverter.printHexBinary(genesisBlockHashOnce) + nonce + fAddress + cAddress;
		byte[] genesisBlockHashTwice = Crypto.hash512(DatatypeConverter.parseHexBinary(dataStr));

		genesisBlock.nonceFound(new byte[Constant.Block.BYTE_NONCE], new byte[Constant.Address.BYTE_ADDRESS],
				genesisBlockHashTwice);
		blockTable.put(ByteBuffer.wrap(genesisBlockHashTwice), genesisBlock);
		blockchainTable.put(0, genesisBlock);
		blockHashOnceTable.put(ByteBuffer.wrap(genesisBlockHashOnce), ByteBuffer.wrap(genesisBlockHashTwice));

		blockHeight = 1;
		block = new Block(blockHeight);
		block.updateHeader(genesisBlockHashTwice, DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET),
				DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_SUB_TARGET));
		block.removeNull();
		nextBlock = new Block(blockHeight + 1);
		currentTxFee = 0;

		Map<ByteBuffer, Integer> tmp = new HashMap<ByteBuffer, Integer>();
		tmp.put(ByteBuffer.wrap(genesisBlockHashTwice), 0);
		blockComfirmationTable.put(0, tmp);
		blockTimeList = new ArrayList<Long>();
		blockTimeList.add(block.getTimestamp());
		blockReward = Constant.Block.BLOCK_REWARD;
		blockSubReward = blockReward / Constant.Block.BLOCK_SUB_REWARD_RATE;
		blockFrontendReward = blockReward / Constant.Block.BLOCK_FRONTEND_REWARD_RATE;

		Log.log("Blockchain init done.");
	}

	synchronized static boolean addTransaction(Transaction tx) {
		int availableInputSum = 0, outputSum = 0;
		List<Input> unavailableInputList = new ArrayList<Input>();
		List<Input> txInputList = new ArrayList<Input>(Arrays.asList(tx.getIn()));
		List<Output> txOutputList = new ArrayList<Output>(Arrays.asList(tx.getOut()));
		UTXO utxoSpent = new UTXO();

		Output outTmp;
		try {
			for (Iterator<Input> inputIt = txInputList.iterator(); inputIt.hasNext();) {
				Input in = inputIt.next();
				if (in == null) {
					break;
				}
				outTmp = checkTxInput(in, tx, unavailableInputList, utxoTableRemove, utxoTableRemoveNext,
						nextBlock.getPrevBlockHash());
				if (outTmp == null) {
					inputIt.remove();
				} else {
					utxoSpent.add(outTmp.getReceiver(), outTmp);
					availableInputSum += outTmp.getAmount();
				}
			}
		} catch (Exception e) {
			Log.log("[Blockchain.addTransaction()] Invalid tx data", Constant.Log.EXCEPTION);
			e.printStackTrace();
			return false;
		}
//		Log.log("[Blockchain.addTransaction()] Available Input Sum: " + availableInputSum, Constant.Log.TEMPORARY);
//		Log.log("[Blockchain.addTransaction()] Unavailable Input List: " + unavailableInputList,
//				Constant.Log.TEMPORARY);

		tx.updateIn(txInputList.toArray(new Input[txInputList.size()]));
		tx.removeNull();
		for (Iterator<Output> outputIt = txOutputList.iterator(); outputIt.hasNext();) {
			Output out = outputIt.next();
			outputSum += out.getAmount();
		}

		if (availableInputSum < outputSum - Constant.Blockchain.TX_FEE) {
			// for debug?
			addInvalidInputPool(tx, unavailableInputList);
			return false;
		}
		if (!nextBlock.addTransaction(tx)) {
			return false;
		}

		UTXO utxoTableAppendNew = new UTXO();
		Iterator<Output> outputIt;
		try {
			for (outputIt = Arrays.asList(tx.getOut()).iterator(); outputIt.hasNext();) {
				Output out = outputIt.next();
				if(!utxoTableAppendNew.add(out.getReceiver(), out)) {
					return false;
				}
			}
		} catch (Exception e) {
			Log.log("[Blockchain.addTransaction()] UTXO add Exception", Constant.Log.EXCEPTION);
			e.printStackTrace();
			return false;
		}
		// ここまで来たらTXは完全とみなす
		utxoTableRemoveNext.addAll(utxoSpent.getAll());
		utxoTableAppendNew.addAll(utxoTableAppendNew.getAll());

		// Log.log("[Blockchain.addTransaction()] Next Block Update: " +
		// nextBlock, Constant.Log.TEMPORARY);
		// if (no.getType() == Constant.Blockchain.TX) {
		// Log.log("[Blockchain.addTransaction()] Broadcast tpsotx: " + tx,
		// Constant.Log.TEMPORARY);
		// BackendServer.shareBackend(new
		// NetworkObject(Constant.Blockchain.TX_BROADCAST_DATA, no.getTx()));
		// }
		return true;
	}

	static boolean verifyBlock(Block newBlock, UTXO utxoTableRemoveNew) {
		Iterator<Input> inputIt;
		Transaction[] txList = newBlock.getTxList();

		Transaction tx;
		for (int i = 0; i < txList.length; i++) {
			tx = txList[i];
			for (inputIt = Arrays.asList(tx.getIn()).iterator(); inputIt.hasNext();) {
				Input in = inputIt.next();
				if (in == null) {
					break;
				}
				if (checkTxInput(in, tx, null, null, utxoTableRemoveNew, newBlock.getPrevBlockHash()) == null) {
					Log.log("[Blockchain.addBlock()] Recept invalid block", Constant.Log.INVALID);
					return false;
				}
			}
		}

		// headerが正しいかどうか検証

		return true;
	}

	synchronized static boolean goToNextBlock(Block newBlock) {
		Iterator<Output> outputIt;
		UTXO utxoTableRemoveNew = new UTXO();
		if (!verifyBlock(newBlock, utxoTableRemoveNew)) {
			return false;
		}
		byte[] blockHash = newBlock.getBlockHash();
		int newBlockHeight = newBlock.getBlockHeight();
		ByteBuffer prevBlockHashBuf = ByteBuffer.wrap(newBlock.getPrevBlockHash());
		ByteBuffer blockHashBuf = ByteBuffer.wrap(blockHash);

		for (int i = 1; i < Constant.Blockchain.CONFIRMATION; i++) {
			if (newBlockHeight - i >= 0 && blockComfirmationTable.containsKey(newBlockHeight - i)) {
				Map<ByteBuffer, Integer> tmp2 = blockComfirmationTable.get(newBlockHeight - i);
				for (Entry<ByteBuffer, Integer> tmp3 : tmp2.entrySet()) {
					Log.log("[Blockchain.goToNextBlock()] comfirmation("+tmp3.getValue()+"): " + DatatypeConverter.printHexBinary(tmp3.getKey().array()), Constant.Log.TEMPORARY);
				}
				Log.log("prevBlockHashBuf.array(): " + DatatypeConverter.printHexBinary(prevBlockHashBuf.array()));
				if (tmp2.containsKey(prevBlockHashBuf)) {
					tmp2.put(prevBlockHashBuf, i);
					if (newBlockHeight - i == 0) {
						break;
					}
					Block prevBlock = blockTable.get(prevBlockHashBuf);
					if (prevBlock == null) {
						Log.log("[Blockchain.addBlock()] Invalid prevBlockHash", Constant.Log.INVALID);
						Log.log("[Blockchain.addBlock()] This should not happen", Constant.Log.INVALID);
						return false;
					}
					prevBlockHashBuf = ByteBuffer.wrap(prevBlock.getPrevBlockHash());
				} else {
					Log.log("[Blockchain.addBlock()] Invalid prevBlockHash", Constant.Log.INVALID);
					return false;
				}
			} else {
				return false;
			}
		}

		if (!utxoTable.checkAndRemoveAll(utxoTableRemoveNew.getAll())) {
			return false;
		}
		UTXO utxoTableAppendNew = new UTXO();
		Output out = null;
		try {
			for (Transaction tx : newBlock.getTxList()) {
				for (outputIt = Arrays.asList(tx.getOut()).iterator(); outputIt.hasNext();) {
					out = outputIt.next();
					utxoTableAppendNew.add(out.getReceiver(), out);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TofuError.FatalError("Invalid output: " + out);
		}
		Log.log("古いUTXO: " + utxoTable.toExplainString());
		if(!utxoTable.addAll(utxoTableAppendNew.getAll())) {
			return false;
		}

		// ここまで来たら完全なブロックとみなす
		utxoTableRemove = utxoTableRemoveNext;
		utxoTableAppend = utxoTableAppendNext;
		utxoTableRemoveNext = new UTXO();
		utxoTableAppendNext = new UTXO();
		Log.log("今回削除されるUTXO: " + utxoTableRemoveNew.toExplainString());
		Log.log("今回追加されるUTXO: " + utxoTableAppendNew.toExplainString());
		Log.log("次使えなくなるUTXO: " + utxoTableRemove.toExplainString());
		Log.log("次追加されるUTXO: " + utxoTableAppend.toExplainString());
		Log.log("新しいUTXO: " + utxoTable.toExplainString());
		
		if (blockComfirmationTable.containsKey(newBlockHeight - Constant.Blockchain.CONFIRMATION)) {
			for (ByteBuffer buf : blockComfirmationTable.get(newBlockHeight - Constant.Blockchain.CONFIRMATION)
					.keySet()) {
				if (prevBlockHashBuf.equals(buf)) {
					appendBlockchain(buf);
				} else {
					IO.deleteFile(Setting.BLOCKCHAIN_BIN_DIR + Constant.Blockchain.BLOCKCHAIN_TMP_DIR
							+ blockTable.get(blockHashBuf).getBlockHeight() + "_" + blockHashBuf.array());
				}
				blockTable.remove(buf);
			}
			blockComfirmationTable.remove(newBlockHeight - Constant.Blockchain.CONFIRMATION);
		}
		if (blockComfirmationTable.containsKey(newBlockHeight)) {
			Map<ByteBuffer, Integer> tmp = blockComfirmationTable.get(newBlockHeight);
			tmp.put(ByteBuffer.wrap(blockHash), 0);
		} else {
			Map<ByteBuffer, Integer> tmp = new HashMap<ByteBuffer, Integer>();
			tmp.put(ByteBuffer.wrap(blockHash), 0);
			blockComfirmationTable.put(newBlockHeight, tmp);
		}

		blockTable.put(blockHashBuf, newBlock);
		blockchainTable.put(newBlock.getBlockHeight(), newBlock);
		byte[] miner = newBlock.getMiner();
		byte[] nonce = newBlock.getNonce();
		newBlock.resetNonce();
		byte[] newBlockHashOnce;
		try {
			newBlockHashOnce = Crypto.hash512(ByteUtil.getByteObject(newBlock.getBlockHeader()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new TofuError.FatalError("Invalid block header: " + newBlock.getBlockHeight());
		}
		newBlock.nonceFound(nonce, miner, blockHash);

		blockHashOnceTable.put(ByteBuffer.wrap(newBlockHashOnce), ByteBuffer.wrap(newBlock.getBlockHash()));
		int i = 0;
		while (blockchainTable.size() > Constant.Blockchain.MAX_BLOCKCHAIN_TABLE) {
			if (blockchainTable.containsKey(newBlock.getBlockHeight() - Constant.Blockchain.MAX_BLOCKCHAIN_TABLE - i)) {
				blockchainTable.remove(newBlock.getBlockHeight() - Constant.Blockchain.MAX_BLOCKCHAIN_TABLE - i);
				i++;
			}
		}
		try {
			Log.log("[Blockchain.goToNextBlock()] Save block: " + newBlock);
			IO.fileWrite(Setting.BLOCKCHAIN_BIN_DIR + Constant.Blockchain.BLOCKCHAIN_TMP_DIR,
					newBlock.getBlockHeight() + "_" + DatatypeConverter.printHexBinary(blockHashBuf.array()),
					ByteUtil.getByteObject(newBlock));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[Blockchain.appendBlockchain()] Invalid block data", Constant.Log.EXCEPTION);
			throw new TofuError.FatalError("Cannot write file: " + Setting.BLOCKCHAIN_BIN_DIR
					+ Constant.Blockchain.BLOCKCHAIN_TMP_DIR + newBlock.getBlockHeight() + "_"
					+ DatatypeConverter.printHexBinary(blockHashBuf.array()) + "\tnewBlock: " + newBlock);
		}

		nextBlock.removeNull();
		block = nextBlock;
		blockHeight++;
		byte[][] targetList = targetAdjust(blockHashBuf);
		block.updateHeader(blockHash, targetList[0], targetList[1]);
		nextBlock = new Block(blockHeight + 1);

		// if (broadcast) {
		// BackendServer.shareBackend(new
		// NetworkObject(Constant.Blockchain.BLOCK_BROADCAST_DATA, newBlock));
		// }
		blockReward = Constant.Block.BLOCK_REWARD - block.getBlockHeight() / Constant.Block.BLOCK_REWARD_HALVING
				* Constant.Block.BLOCK_REWARD_HALVING_SIZE;
		blockSubReward = blockReward / Constant.Block.BLOCK_SUB_REWARD_RATE;
		blockFrontendReward = blockReward / Constant.Block.BLOCK_FRONTEND_REWARD_RATE;
		DataManager.updateMining(block);
		return true;
	}

	synchronized static void addRewardTransaction(Report report, Constant.Verify.Result result) {
		Input[] in = new Input[1];
		Output[] out = new Output[2];
		byte[] questionScript = new byte[14];
		byte[] answerScript = new byte[276];
		Transaction tx;
		int cReward = blockSubReward;
		int fReward = blockFrontendReward;
		if (result == Constant.Verify.Result.TARGET) {
			cReward = blockReward;
		}

		answerScript[0] = OPCode.PUSH_MAX_512;
		System.arraycopy(ByteBuffer.allocate(4).putInt(64).array(), 0, answerScript, 1, 4);
		System.arraycopy(DatatypeConverter.parseHexBinary(report.getResult()), 0, answerScript, 5,
				Constant.Block.BYTE_BLOCK_HASH);

		answerScript[69] = OPCode.PUSH_MAX_512;
		System.arraycopy(ByteBuffer.allocate(4).putInt(64).array(), 0, answerScript, 70, 4);
		System.arraycopy(DatatypeConverter.parseHexBinary(report.getCAddress()), 0, answerScript, 74,
				Constant.Address.BYTE_ADDRESS);

		answerScript[138] = OPCode.PUSH_MAX_512;
		System.arraycopy(ByteBuffer.allocate(4).putInt(64).array(), 0, answerScript, 139, 4);
		System.arraycopy(DatatypeConverter.parseHexBinary(report.getFAddress()), 0, answerScript, 143,
				Constant.Address.BYTE_ADDRESS);

		answerScript[207] = OPCode.PUSH_MAX_512;
		System.arraycopy(ByteBuffer.allocate(4).putInt(64).array(), 0, answerScript, 208, 4);
		System.arraycopy(DatatypeConverter.parseHexBinary(report.getNonce()), 0, answerScript, 212,
				Constant.Block.BYTE_NONCE);

		questionScript[0] = OPCode.PUSH_MAX_512;
		System.arraycopy(ByteBuffer.allocate(4).putInt(4).array(), 0, questionScript, 1, 4);
		System.arraycopy(ByteBuffer.allocate(4).putInt(block.getBlockHeight()).array(), 0, questionScript, 5, 4);
		questionScript[9] = OPCode.POP0; 
		questionScript[10] = OPCode.PUBK_DUP;
		questionScript[11] = OPCode.HASH_TWICE;
		questionScript[12] = OPCode.CHECK_ADDR;
		questionScript[13] = OPCode.CHECK_SIG;

		in[0] = new Input(new byte[1], new Answer(answerScript, DatatypeConverter.parseHexBinary(report.getHash())),
				cReward + fReward);
		out[0] = new Output(cReward,
				new Question(questionScript, DatatypeConverter.parseHexBinary(report.getCAddress())));
		out[1] = new Output(fReward,
				new Question(questionScript, DatatypeConverter.parseHexBinary(report.getFAddress())));
		tx = new Transaction(in, out, Constant.Transaction.VERSION, Constant.Blockchain.CONFIRMATION,
				DatatypeConverter.parseHexBinary(report.getSignature()),
				DatatypeConverter.parseHexBinary(report.getPublicKey()));
		addTransaction(tx);
		if (result == Constant.Verify.Result.TARGET) {
			block.nonceFound(DatatypeConverter.parseHexBinary(report.getNonce()),
					DatatypeConverter.parseHexBinary(report.getCAddress()),
					DatatypeConverter.parseHexBinary(report.getResult()));
			BackendServer.shareBackend(new NetworkObject(Constant.NetworkObject.TYPE_BLOCK, block));
			Log.log("[Blockchain.addRewardTransaction()] Temporary PBFT off", Constant.Log.TEMPORARY);
			Blockchain.goToNextBlock(block);
		}
	}

	static boolean addNode(NetworkObject no) {
		Node node = no.getNode();
		if (!node.checkSig()) {
			Log.log("Access denied invalid node", Constant.Log.INVALID);
			return false;
		}
		if (no.getType() == Constant.Blockchain.NODE) {
			BackendServer.shareBackend(no);
		}
		return true;
	}

	static Output checkTxInput(Input in, Transaction tx, List<Input> unavailableInputList, UTXO utxoTableRemove1,
			UTXO utxoTableRemove2, byte[] prevBlockHash) {
		Map<ByteBuffer, Output> utxoMapUpdate;
		if (utxoTableRemove1 != null) {
			utxoMapUpdate = utxoTableRemove1.get(ByteBuffer.wrap(in.getReceiver()));
			if (utxoMapUpdate != null) {
				// このアドレスはブロックですでに消費したUTXOが一つ以上ある
				if (utxoMapUpdate.containsKey(ByteBuffer.wrap(in.getOutHash()))) {
					// このUTXOは使用済み
					Log.log("[Blockchain.checkTxInput()] UTXO already used at current: " + in, Constant.Log.INVALID);
					return null;
				}
			}
		}
		if (utxoTableRemove2 != null) {
			utxoMapUpdate = utxoTableRemove2.get(ByteBuffer.wrap(in.getReceiver()));
			if (utxoMapUpdate != null) {
				// このアドレスは次のブロックですでに消費したUTXOが一つ以上ある
				if (utxoMapUpdate.containsKey(ByteBuffer.wrap(in.getOutHash()))) {
					// このUTXOは使用済み
					Log.log("[Blockchain.checkTxInput()] UTXO already used at next: " + in, Constant.Log.INVALID);
					return null;
				}
			}
		}
		byte[] outHash = in.getOutHash();

		if (Arrays.equals(outHash, new byte[1])) {
			ByteBuffer buf = ByteBuffer.wrap(in.getReceiver());
			if (!blockHashOnceTable.containsKey(buf)) {
				Log.log("[Blockchain.checkTxInput()] Mined block does not exist", Constant.Log.INVALID);
				return null;
			}
			Block minedBlock = blockTable.get(blockHashOnceTable.get(buf));
			byte[] questionScript = new byte[208];
			questionScript[0] = OPCode.PUSH_MAX_512;
			System.arraycopy(ByteBuffer.allocate(4).putInt(64).array(), 0, questionScript, 1, 4);
			System.arraycopy(in.getReceiver(), 0, questionScript, 5, Constant.Block.BYTE_BLOCK_HASH);

			questionScript[69] = OPCode.PUSH_MAX_512;
			System.arraycopy(ByteBuffer.allocate(4).putInt(64).array(), 0, questionScript, 70, 4);
			System.arraycopy(minedBlock.getTarget(), 0, questionScript, 74, Constant.Block.BYTE_TARGET);

			questionScript[138] = OPCode.PUSH_MAX_512;
			System.arraycopy(ByteBuffer.allocate(4).putInt(64).array(), 0, questionScript, 139, 4);
			System.arraycopy(minedBlock.getSubTarget(), 0, questionScript, 143, Constant.Block.BYTE_TARGET);

			questionScript[207] = OPCode.CHECK_REWARD;
			// answer -- result cAddress fAddress nonce
			// question -- blockHash target subTarget checkReward
			// TODO: blockHeightを使ってblockRewardが正しいか確認
			Result result = Script.resolve(new Question(questionScript, minedBlock.getMiner()), in.getAnswer(), null);
			if (result != Result.SOLVED) {
				Log.log("[Blockchain.checkTxInput()] Invalid coinbase tx", Constant.Log.INVALID);
				return null;
			}
			return new Output(0, new Question(new byte[1], new byte[1]));
		}
		Map<ByteBuffer, Output> utxoMap = utxoTable.get(ByteBuffer.wrap(in.getReceiver()));
		if (utxoMap == null) {
			Log.log("[Blockchain.checkTxInput()] Receiver not exists: " + in, Constant.Log.INVALID);
			return null;
		}

		Output out = utxoMap.get(ByteBuffer.wrap(outHash));
		if (out == null) {
			Log.log("[Blockchain.checkTxInput()] outHash not exists: " + in, Constant.Log.INVALID);
			return null;
		}

		Constant.Script.Result result = Script.resolve(out.getQuestion(), in.getAnswer(), in.getOutHash());

		if (result == Constant.Script.Result.SOLVED) {
			try {
				utxoTableRemove2.add(in.getReceiver(), out);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return out;
		} else if (result == Constant.Script.Result.FAILED) {
			if (unavailableInputList != null) {
				unavailableInputList.add(in);
			}
		}
		return null;
	}

	static UTXO getUTXO() {
		return utxoTable;
	}

	static boolean appendBlockchain(ByteBuffer blockHashBuf) {
		if (!blockTable.containsKey(blockHashBuf)) {
			return false;
		}
		Block newBlock = blockTable.get(blockHashBuf);
		blockTimeList.add(newBlock.getTimestamp());
		while (blockTimeList.size() > Constant.Blockchain.LENGTH_MAX_BLOCK_TIME_LIST) {
			blockTimeList.remove(0);
		}

		IO.moveFile(
				Setting.BLOCKCHAIN_BIN_DIR + Constant.Blockchain.BLOCKCHAIN_TMP_DIR + newBlock.getBlockHeight() + "_"
						+ blockHashBuf.array(),
				Setting.BLOCKCHAIN_BIN_DIR + (blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR) + File.separator
						+ newBlock.getBlockHeight());
		return true;
	}

	static byte[][] targetAdjust(ByteBuffer blockHashBuf) {
		byte[] defaultTarget = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
		long blockTimeSum = 0, shiftTmp;
		int shift;
		if (blockTimeList.size() == 0) {
			blockTimeSum -= blockTable.get(ByteBuffer.wrap(new byte[Constant.Block.BYTE_BLOCK_HASH])).getTimestamp();
		} else {
			blockTimeSum -= blockTimeList.get(0);
		}
		blockTimeSum += blockTable.get(blockHashBuf).getTimestamp();
		blockTimeSum += Constant.Blockchain.AVERAGE_BLOCK_TIME
				* (Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK - blockTimeList.size() - blockTable.size() - 1);
		shiftTmp = ((Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK - 1) * Constant.Blockchain.AVERAGE_BLOCK_TIME
				- blockTimeSum) / Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK
				/ Constant.Blockchain.TARGET_SHIFT_PER_TIME;
		if (shiftTmp > Constant.Block.MAX_DIFFICULTY) {
			shift = Constant.Block.MAX_DIFFICULTY;
		} else {
			shift = (int) shiftTmp;
		}
		Log.log("[Blockchain.targetAdjust()] shift: " + shift, Constant.Log.TEMPORARY);
		BigInteger targetNum = new BigInteger(defaultTarget);
		byte[] targetTmp;
		if (shift > 0) {
			targetTmp = targetNum.shiftRight(shift).toByteArray();
		} else {
			if(-shift > Constant.Block.MAX_TARGET_SHIFT_LEFT) {
				shift = -Constant.Block.MAX_TARGET_SHIFT_LEFT;
			}
			targetTmp = targetNum.shiftLeft(-shift).toByteArray();
		}
		byte[][] targetList = new byte[2][Constant.Block.BYTE_TARGET];
		System.arraycopy(targetTmp, 0, targetList[0], Constant.Block.BYTE_TARGET - targetTmp.length, targetTmp.length);
//		Log.log("default  Target: " + DatatypeConverter.printHexBinary(defaultTarget));
//		Log.log("new      Target: " + DatatypeConverter.printHexBinary(targetList[0]));

		targetTmp = targetNum.shiftLeft(Constant.Block.SUB_TARGET_SHIFT).toByteArray();
		System.arraycopy(targetTmp, 0, targetList[1], Constant.Block.BYTE_TARGET - targetTmp.length, targetTmp.length);
		return targetList;
	}

	static Block getBlock() {
		return block;
	}

	static Block getBlock(int blockHeight) {
		return blockchainTable.get(blockHeight);
	}

	static void addInvalidInputPool(Transaction tx, List<Input> inList) {
		Map<ByteBuffer, Map<Transaction, List<Input>>> tmp;
		Map<Transaction, List<Input>> tmp2 = new HashMap<Transaction, List<Input>>();
		tmp2.put(tx, inList);
		ByteBuffer buf = ByteBuffer.wrap(tx.getPublicKey());
		if (invalidTxTable.containsKey(buf)) {
			tmp = invalidTxTable.get(buf);
			tmp.put(ByteBuffer.wrap(tx.getSignature()), tmp2);
		} else {
			tmp = new HashMap<ByteBuffer, Map<Transaction, List<Input>>>();
			tmp.put(ByteBuffer.wrap(tx.getSignature()), tmp2);
			invalidTxTable.put(buf, tmp);
		}
	}

	static void setTestData() throws AddressFormatException, Exception {
		byte[] script = new byte[4];
		script[0] = OPCode.PUBK_DUP;
		script[1] = OPCode.HASH_TWICE;
		script[2] = OPCode.CHECK_ADDR;
		script[3] = OPCode.CHECK_SIG;
		
		byte[] receiver = Base58
				.decode("3ArnSRhjcNzkYsDuw7j5fdKnGqVvFXBHfsKdbqEuN6cYATYpqari9vKFRbsQnf5BXxtNnAbFwaL86XjzQDhu4Vg3");
		utxoTable.add(receiver, new Output(1000, new Question(script, receiver)));
		receiver = Base58
				.decode("5wfKfv2mt5sAkAACWgv9AHaBFrFHRbxUg2rqjdTg9N4Wvqqm9uHi9bZoAvioMuKfCksNhU7tfucZvqYxT2gmHw5a");
		utxoTable.add(receiver, new Output(1000, new Question(script, receiver)));
		receiver = Base58.decode("4WRVctLfVXpw5aJrqPJbbsdXxz8qGbQXxmxKMCW8kWMxRHtnWREd3e9dGfhFYiFGnvsw36XNXbYgFnKNR2ZHnaFh");
		utxoTable.add(receiver, new Output(1000, new Question(script, receiver)));
		utxoTable.add(receiver, new Output(10000, new Question(script, receiver)));
		
		BackendServer.shareFrontend(new NetworkObject(Constant.NetworkObject.TYPE_UTXO, utxoTable));
		Log.log("[Blockchain.setTestData] utxoList: " + utxoTable, Constant.Log.TEMPORARY);
	}

	// static Transaction verifyRequest(Request request) {
	// Transaction tx = null;
	// if(request.getType() == Constant.Request.TYPE_SEND_TOFU) {
	// Map<byte[], Output> utxoMap =
	// utxoTable.get(ByteBuffer.wrap(request.getAddrFrom()));
	// if(utxoMap.containsKey(request.getTxHash())) {
	// Input[] inList = new Input[Constant.Transaction.MAX_INPUT_OUTPUT];
	// Output[] outList = new Output[Constant.Transaction.MAX_INPUT_OUTPUT];
	// tx = new Transaction(inList, outList, Constant.Transaction.VERSION,
	// Constant.Transaction.DEFAULT_LOCKTIME, request.getTxHash());
	// }
	// }
	// return tx;
	// }
	//
}
