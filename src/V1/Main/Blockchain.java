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
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.Script;
import V1.Library.TofuError;
import V1.Library.TofuException;
import V1.Library.TofuException.AddressFormatException;
import net.arnx.jsonic.JSON;

public class Blockchain {

	private static Block block;
	private static Block nextBlock;
	private static int blockHeight;

	private static int currentTxFee;

	private static Map<Integer, Map<ByteBuffer, Integer>> blockComfirmationTable;
	// 現在のブロックまでのUTXO
	private static UTXO utxoTable;
	// nextBlockのutxoの追加差分
	private static UTXO utxoTableAppend;
	// nextBlockのutxoの消費差分
	private static UTXO utxoTableRemove;
	// Map<publicKey , Map<signature , Map<tx , invalidInputList>>>>
	// invalidTxTable;
	private static Map<ByteBuffer, Map<ByteBuffer, Map<Transaction, List<Input>>>> invalidTxTable;
	// Map<blockHash, block> blockTable
	private static Map<ByteBuffer, Block> blockTable;
	private static List<Long> blockTimeList;

	static void init() {
		blockHeight = 1;
		block = new Block(blockHeight);
		nextBlock = new Block(blockHeight + 1);
		currentTxFee = 0;

		blockComfirmationTable = new HashMap<Integer, Map<ByteBuffer, Integer>>();
		utxoTable = new UTXO();
		utxoTableAppend = new UTXO();
		utxoTableRemove = new UTXO();
		invalidTxTable = new HashMap<ByteBuffer, Map<ByteBuffer, Map<Transaction, List<Input>>>>();
		blockTable = new HashMap<ByteBuffer, Block>();

		Map<ByteBuffer, Integer> tmp = new HashMap<ByteBuffer, Integer>();
		tmp.put(ByteBuffer.wrap(new byte[Constant.Block.BYTE_BLOCK_HASH]), 0);
		blockComfirmationTable.put(0, tmp);
		blockTable.put(ByteBuffer.wrap(new byte[Constant.Block.BYTE_BLOCK_HASH]), new Block());
		blockTimeList = new ArrayList<Long>();

		Log.log("Blockchain init done.");
	}

	static boolean addTransaction(NetworkObject no) {
		Transaction tx = no.getTx();
		int availableInputSum = 0, outputSum = 0;
		int i = 0;
		List<Input> unavailableInputList = new ArrayList<Input>();
		List<Input> txInputList = new ArrayList<Input>(Arrays.asList(tx.getIn()));
		List<Output> txOutputList = new ArrayList<Output>(Arrays.asList(tx.getOut()));
		UTXO utxoSpent = new UTXO();
		Output outTmp;
		try {
			for (Iterator<Input> it = txInputList.iterator(); it.hasNext(); i++) {
				outTmp = checkTxInput(it, tx, unavailableInputList);
				if (outTmp == null) {
					it.remove();
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
		Log.log("[Blockchain.addTransaction()] Available Input Sum: " + availableInputSum, Constant.Log.TEMPORARY);
		Log.log("[Blockchain.addTransaction()] Unavailable Input List: " + unavailableInputList, Constant.Log.TEMPORARY);

		tx.updateIn(txInputList.toArray(new Input[txInputList.size()]));
		tx.removeNull();
		for (Iterator<Output> it = txOutputList.iterator(); it.hasNext(); i++) {
			Output out = it.next();
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

		// ここまで来たらTXは完全とみなす
		utxoTableRemove.addAll(utxoSpent);
		Iterator<Output> outputIt;
		try {
			for (outputIt = Arrays.asList(tx.getOut()).iterator(); outputIt.hasNext();) {
				Output out = outputIt.next();
				utxoTableAppend.add(out.getReceiver(), out);
			}
		} catch (Exception e) {
			Log.log("[Blockchain.addTransaction()] UTXO add Exception", Constant.Log.EXCEPTION);
			e.printStackTrace();
		}

		Log.log("[Blockchain.addTransaction()] Next Block Update: " + nextBlock, Constant.Log.TEMPORARY);
		if (no.getType() == Constant.Blockchain.TX) {
			Log.log("[Blockchain.addTransaction()] Broadcast tx: " + tx, Constant.Log.TEMPORARY);
			BackendServer.shareBackend(new NetworkObject(Constant.Blockchain.TX_BROADCAST_DATA, no.getTx()));
		}
		return true;
	}

	static boolean addBlock(NetworkObject no) {
		// TODO update target
		Block newBlock = no.getBlock();
		Iterator<Input> inputIt;
		Iterator<Output> outputIt;
		UTXO utxoNewComponent = new UTXO();
		for (Transaction tx : newBlock.getTxList()) {
			int i = 0;
			for (inputIt = Arrays.asList(tx.getIn()).iterator(); inputIt.hasNext();) {
				if (checkTxInput(inputIt, tx, null) == null) {
					Log.log("[Blockchain.addBlock()] Recept invalid block", Constant.Log.INVALID);
					return false;
				}
			}
		}

		try {
			for (Transaction tx : newBlock.getTxList()) {
				for (outputIt = Arrays.asList(tx.getOut()).iterator(); outputIt.hasNext();) {
					Output out = outputIt.next();
					utxoTable.add(out.getReceiver(), out);
					utxoNewComponent.add(out.getReceiver(), out);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[Blockchain.addBlock()] Invalid block data", Constant.Log.EXCEPTION);
			return false;
		}

		// unnecessary ? for insurance ?
		// BackendServer.shareFrontend(new
		// NetworkObject(Constant.NetworkObject.TYPE_UTXO, utxoNewComponent));

		return goToNextBlock(newBlock, no.getType() == Constant.Blockchain.BLOCK);
	}

	static boolean goToNextBlock(Block newBlock, boolean broadcast) {
		if (broadcast) {
			BackendServer.shareBackend(new NetworkObject(Constant.Blockchain.BLOCK_BROADCAST_DATA, newBlock));
		}

		try {
			byte[] blockHash = Crypto.hashTwice(ByteUtil.getByteObject(newBlock));
			int newBlockHeight = newBlock.getBlockHeight();
			byte[] prevBlockHash = newBlock.getPrevBlockHash();
			ByteBuffer prevBlockHashBuf = ByteBuffer.wrap(newBlock.getPrevBlockHash());
			ByteBuffer blockHashBuf = ByteBuffer.wrap(blockHash);

			for (int i = 1; i < Constant.Blockchain.CONFIRMATION; i++) {
				if (newBlockHeight - i >= 0 && blockComfirmationTable.containsKey(newBlockHeight - i)) {
					Map<ByteBuffer, Integer> tmp2 = blockComfirmationTable.get(newBlockHeight - i);
					if (tmp2.containsKey(prevBlockHashBuf)) {
						if (i * 2 == Math.pow(2, Constant.Blockchain.CONFIRMATION)) {
							for (ByteBuffer buf : blockComfirmationTable.get(newBlockHeight - i).keySet()) {
								appendBlockchain(buf);
								blockTable.remove(buf);
							}
							blockComfirmationTable.remove(newBlockHeight - i);
							break;
						}
						tmp2.put(prevBlockHashBuf, i * 2);
						prevBlockHashBuf = ByteBuffer.wrap(blockTable.get(prevBlockHashBuf).getPrevBlockHash());
					} else {
						Log.log("[Blockchain.addBlock()] Invalid prevBlockHash", Constant.Log.INVALID);
						return false;
					}
				} else {
					return false;
				}
			}
			if (blockComfirmationTable.containsKey(newBlockHeight)) {
				Map<ByteBuffer, Integer> tmp = blockComfirmationTable.get(newBlockHeight);
				tmp.put(ByteBuffer.wrap(prevBlockHash), 0);
			} else {
				Map<ByteBuffer, Integer> tmp = new HashMap<ByteBuffer, Integer>();
				tmp.put(ByteBuffer.wrap(prevBlockHash), 0);
				blockComfirmationTable.put(newBlockHeight, tmp);
			}

			blockTable.put(blockHashBuf, newBlock);
			block = nextBlock;
			blockHeight++;
			block.updateHeader(blockHash, targetAdjust(blockHashBuf));
			nextBlock = new Block(blockHeight + 1);

		} catch (Exception e) {
			Log.log("[Blockchain.goToNextBlock()] Invalid newBlock", Constant.Log.EXCEPTION);
			e.printStackTrace();
		}

		DataManager.updateMining(block);
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
		if (no.getType() == Constant.Blockchain.NODE) {
			BackendServer.shareBackend(no);
		}
		return true;
	}

	static Output checkTxInput(Iterator<Input> it, Transaction tx, List<Input> unavailableInputList) {
		Input in = it.next();
		if (in == null) {
			return null;
		}
		Map<ByteBuffer, Output> utxoMapUpdate = utxoTableRemove.get(ByteBuffer.wrap(in.getReceiver()));
		if (utxoMapUpdate != null) {
			// このアドレスは次のブロックですでに消費したUTXOが一つ以上ある
			if (utxoMapUpdate.containsKey(ByteBuffer.wrap(in.getOutHash()))) {
				// このUTXOは使用済み
				Log.log("[Blockchain.checkTxInput()] UTXO already used: " + in, Constant.Log.IMPORTANT);
				return null;
			}
		}

		Map<ByteBuffer, Output> utxoMap = utxoTable.get(ByteBuffer.wrap(in.getReceiver()));
		if (utxoMap == null) {
			Log.log("[Blockchain.checkTxInput()] receiver not exists: " + in, Constant.Log.IMPORTANT);
			return null;
		}
		Output out = utxoMap.get(ByteBuffer.wrap(in.getOutHash()));
		if (out == null) {
			Log.log("[Blockchain.checkTxInput()] outHash not exists: " + in, Constant.Log.IMPORTANT);
			return null;
		}

		Result result = Script.resolve(out.getQuestion(), in.getAnswer(), in.getOutHash());

		Log.log("checkTx() script.resolve() result: " + result, Constant.Log.TEMPORARY);

		if (result == Constant.Script.Result.SOLVED) {
			return out;
		} else if (result == Constant.Script.Result.FAILED) {
			if (unavailableInputList != null) {
				unavailableInputList.add(in);
			}
		}
		return null;
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
		try {
			Log.log("[Blockchain.goToNextBlock()] Save block: " + newBlock);
			IO.fileWrite(
					Setting.BLOCKCHAIN_BIN_DIR + (blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR) + File.separator,
					"" + blockHeight, ByteUtil.getByteObject(newBlock));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[Blockchain.appendBlockchain()] Invalid block data", Constant.Log.EXCEPTION);
			return false;
		}
		return true;
	}

	static byte[] targetAdjust(ByteBuffer blockHashBuf) {
		byte[] newTarget = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
		long blockTimeSum = 0, shift;
		int i = 0;
		for (int j = 0; j < Constant.Blockchain.LENGTH_MAX_BLOCK_TIME_LIST - blockTimeList.size(); j++) {
			blockTimeSum += Constant.Blockchain.AVERAGE_BLOCK_TIME;
		}
		blockTimeSum -= blockTimeList.get(0);
		blockTimeSum += blockTable.get(blockHashBuf).getTimestamp();
		Log.log("blockTimeSum: " + blockTimeSum);
		shift = ((Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK - 1) * Constant.Blockchain.AVERAGE_BLOCK_TIME
				- blockTimeSum) / (Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK - 1)
				/ Constant.Blockchain.TARGET_SHIFT_PER_TIME;
		Log.log("[Blockchain.targetAdjust()] shift: " + shift, Constant.Log.TEMPORARY);
		if (shift > 0) {
			// TODO: manage overflow each integer
			// for(i = 0; i < 8; i++) {
			int newTargetInt = 0;
			byte[] intarr = new byte[4];
			System.arraycopy(newTarget, i * 4, intarr, 0, 4);
			newTargetInt = ByteBuffer.wrap(intarr).getInt() >>> shift;
			System.arraycopy(ByteBuffer.allocate(4).putInt(newTargetInt).array(), 0, newTarget, i * 4, 4);
			// }
		} else {
			// for(i = 0; i < 8; i++) {
			int newTargetInt = 0;
			byte[] intarr = new byte[4];
			System.arraycopy(newTarget, i * 4, intarr, 0, 4);
			newTargetInt = ByteBuffer.wrap(intarr).getInt() << -shift;
			System.arraycopy(ByteBuffer.allocate(4).putInt(newTargetInt).array(), 0, newTarget, i * 4, 4);
			// }
		}
		Log.log("newTarget: " + DatatypeConverter.printHexBinary(newTarget));

		return newTarget;
	}

	static Block getBlock() {
		return block;
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
		byte[] receiver1 = Base58
				.decode("3zqBQ4ETSn2zM6nb8QqSB2MiiJehdGij5PnVcdMmTLBY9yfwtVoNtQxCGARaKbSVVFRAw7URwhMB67pGcBoxfsby");
		byte[] receiver2 = Base58
				.decode("3iU5kRFGE3qGZiYNYaWVEB2aZyq3V5aQSXgSLMzna4QDMGGo76BwGqGiZhSZ4QNc4VHGahfy7xHQhET6SgfXyFGu");
		utxoTable.add(receiver1, new Output(10, new Question(script, receiver1)));
		utxoTable.add(receiver2, new Output(10, new Question(script, receiver2)));
		BackendServer.shareFrontend(new NetworkObject(Constant.NetworkObject.TYPE_UTXO, utxoTable));
		Log.log("utxoList: " + utxoTable, Constant.Log.TEMPORARY);
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
