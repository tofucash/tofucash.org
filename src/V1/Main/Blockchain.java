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
	// nextBlockのutxoの追加差分
	private static UTXO utxoTableAppend;
	// nextBlockのutxoの消費差分
	private static UTXO utxoTableRemove;
	// Map<publicKey , Map<signature , Map<tx , invalidInputList>>>>
	// invalidTxTable;
	private static Map<ByteBuffer, Map<ByteBuffer, Map<Transaction, List<Input>>>> invalidTxTable;
	// Map<blockHash, block> blockTable
	private static Map<ByteBuffer, Block> blockTable;
	// 確定したブロック生成時刻のリスト 長さは最大で LENGTH_MAX_BLOCK_TIME_LIST
	private static List<Long> blockTimeList;

	static void init() {
		blockHeight = 1;
		block = new Block(blockHeight);
		block.updateHeader(new byte[Constant.Block.BYTE_BLOCK_HASH],
				DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET),
				DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_SUB_TARGET));
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
		Block genesisBlock = new Block(0);
		genesisBlock.updateHeader(new byte[Constant.Block.BYTE_BLOCK_HASH], DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET),DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_SUB_TARGET));
		blockTable.put(ByteBuffer.wrap(new byte[Constant.Block.BYTE_BLOCK_HASH]), genesisBlock);
		blockTimeList = new ArrayList<Long>();
		blockTimeList.add(block.getTimestamp());
		blockReward = Constant.Block.BLOCK_REWARD;
		blockSubReward = blockReward / Constant.Block.BLOCK_SUB_REWARD_RATE;
		blockFrontendReward = blockReward / Constant.Block.BLOCK_FRONTEND_REWARD_RATE;

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
		Log.log("[Blockchain.addTransaction()] Unavailable Input List: " + unavailableInputList,
				Constant.Log.TEMPORARY);

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

	static void goToNextBlock(ByteBuffer blockHash) {
		goToNextBlock(blockTable.get(blockHash), false);
	}

	static boolean goToNextBlock(Block newBlock, boolean broadcast) {
		Log.log("[Blockchain.goToNextBlock()] block: " + newBlock);

		try {
			byte[] blockHash = newBlock.getBlockHash();
			int newBlockHeight = newBlock.getBlockHeight();
			ByteBuffer prevBlockHashBuf = ByteBuffer.wrap(newBlock.getPrevBlockHash());
			ByteBuffer blockHashBuf = ByteBuffer.wrap(blockHash);

			for (int i = 1; i < Constant.Blockchain.CONFIRMATION; i++) {
				if (newBlockHeight - i >= 0 && blockComfirmationTable.containsKey(newBlockHeight - i)) {
					Map<ByteBuffer, Integer> tmp2 = blockComfirmationTable.get(newBlockHeight - i);
					for (ByteBuffer buf : tmp2.keySet()) {
						Log.log("buf.array(): " + DatatypeConverter.printHexBinary(buf.array()));
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

			// ここまで来たら完全なブロックとみなす
			if(blockComfirmationTable.containsKey(newBlockHeight - Constant.Blockchain.CONFIRMATION)) {
				for (ByteBuffer buf : blockComfirmationTable.get(newBlockHeight - Constant.Blockchain.CONFIRMATION).keySet()) {
					if(prevBlockHashBuf.equals(buf)) {
						appendBlockchain(buf);
					} else {
						IO.deleteFile(Setting.BLOCKCHAIN_BIN_DIR + Constant.Blockchain.BLOCKCHAIN_TMP_DIR+blockTable.get(blockHashBuf).getBlockHeight()+"_"+blockHashBuf.array());
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
			try {
				Log.log("[Blockchain.goToNextBlock()] Save block: " + newBlock);
				IO.fileWrite(
						Setting.BLOCKCHAIN_BIN_DIR + Constant.Blockchain.BLOCKCHAIN_TMP_DIR,
						newBlock.getBlockHeight()+"_"+DatatypeConverter.printHexBinary(blockHashBuf.array()), ByteUtil.getByteObject(newBlock));
			} catch (Exception e) {
				e.printStackTrace();
				Log.log("[Blockchain.appendBlockchain()] Invalid block data", Constant.Log.EXCEPTION);
				return false;
			}

			block = nextBlock;
			blockHeight++;
			byte[][] targetList = targetAdjust(blockHashBuf);
			block.updateHeader(blockHash, targetList[0], targetList[1]);
			nextBlock = new Block(blockHeight + 1);

		} catch (Exception e) {
			Log.log("[Blockchain.goToNextBlock()] Invalid newBlock", Constant.Log.EXCEPTION);
			e.printStackTrace();
			return false;
		}
		if (broadcast) {
			BackendServer.shareBackend(new NetworkObject(Constant.Blockchain.BLOCK_BROADCAST_DATA, newBlock));
		}
		blockReward = Constant.Block.BLOCK_REWARD - block.getBlockHeight() / Constant.Block.BLOCK_REWARD_HALVING
				* Constant.Block.BLOCK_REWARD_HALVING_SIZE;
		blockSubReward = blockReward / Constant.Block.BLOCK_SUB_REWARD_RATE;
		blockFrontendReward = blockReward / Constant.Block.BLOCK_FRONTEND_REWARD_RATE;
		DataManager.updateMining(block);
		return true;
	}

	static boolean addRewardTransaction(Report report, Constant.Verify.Result result) {
		Input[] in = new Input[1];
		Output[] out = new Output[2];
		byte[] answerScript = new byte[138];
		byte[] questionScript = new byte[4];
		Transaction tx;
		int cReward = blockSubReward;
		int fReward = blockFrontendReward;
		if (result == Constant.Verify.Result.TARGET) {
			cReward = blockReward;
		}
		answerScript[0] = OPCode.PUSH_MAX_512;
		System.arraycopy(ByteBuffer.allocate(4).putInt(512).array(), 0, answerScript, 1, 4);
		Log.log("addRewardTransaction: " + DatatypeConverter.parseHexBinary(report.getNonce()).length,
				Constant.Log.TEMPORARY);
		System.arraycopy(DatatypeConverter.parseHexBinary(report.getNonce()), 0, answerScript, 5,
				Constant.Block.BYTE_NONCE);
		answerScript[68] = OPCode.PUSH_MAX_512;
		System.arraycopy(ByteBuffer.allocate(4).putInt(512).array(), 0, answerScript, 69, 4);
		System.arraycopy(block.getTarget(), 0, answerScript, 73, Constant.Block.BYTE_NONCE);
		answerScript[137] = OPCode.CHECK_SUB_REWARD;
		questionScript[0] = OPCode.PUBK_DUP;
		questionScript[1] = OPCode.HASH_TWICE;
		questionScript[2] = OPCode.CHECK_ADDR;
		questionScript[3] = OPCode.CHECK_SIG;
		in[0] = new Input(new byte[1], new Answer(answerScript, DatatypeConverter.parseHexBinary(report.getHash())),
				cReward + fReward);
		out[0] = new Output(cReward,
				new Question(questionScript, DatatypeConverter.parseHexBinary(report.getCAddress())));
		out[1] = new Output(fReward,
				new Question(questionScript, DatatypeConverter.parseHexBinary(report.getFAddress())));
		tx = new Transaction(in, out, Constant.Transaction.VERSION, Constant.Blockchain.CONFIRMATION, DatatypeConverter.parseHexBinary(report.getSignature()),
				DatatypeConverter.parseHexBinary(report.getPublicKey()));
		block.addTransaction(tx);
		if (result == Constant.Verify.Result.TARGET) {
			block.nonceFound(DatatypeConverter.parseHexBinary(report.getNonce()),
					DatatypeConverter.parseHexBinary(report.getCAddress()),
					DatatypeConverter.parseHexBinary(report.getResult()));
			return goToNextBlock(block, true);
		}
		return true;
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

		Constant.Script.Result result = Script.resolve(out.getQuestion(), in.getAnswer(), in.getOutHash());

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
		Log.log("newBlock: " + newBlock);
		blockTimeList.add(newBlock.getTimestamp());
		while (blockTimeList.size() > Constant.Blockchain.LENGTH_MAX_BLOCK_TIME_LIST) {
			blockTimeList.remove(0);
		}


		IO.moveFile(Setting.BLOCKCHAIN_BIN_DIR + Constant.Blockchain.BLOCKCHAIN_TMP_DIR+newBlock.getBlockHeight()+"_"+blockHashBuf.array(), Setting.BLOCKCHAIN_BIN_DIR + (blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR) + File.separator+newBlock.getBlockHeight());
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
		Log.log("blockTimeSum: " + blockTimeSum);
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
			targetTmp = targetNum.shiftLeft(shift).toByteArray();
		}
		byte[][] targetList = new byte[2][Constant.Block.BYTE_TARGET];
		System.arraycopy(targetTmp, 0, targetList[0], Constant.Block.BYTE_TARGET - targetTmp.length, targetTmp.length);
		Log.log("default  Target: " + DatatypeConverter.printHexBinary(defaultTarget));
		Log.log("new      Target: " + DatatypeConverter.printHexBinary(targetList[0]));
		
		targetTmp = targetNum.shiftLeft(Constant.Block.SUB_TARGET_SHIFT).toByteArray();
		System.arraycopy(targetTmp, 0, targetList[1], Constant.Block.BYTE_TARGET - targetTmp.length, targetTmp.length);
		return targetList;
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
