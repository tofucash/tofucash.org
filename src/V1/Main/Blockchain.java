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
import V1.Component.Script;
import V1.Component.Transaction;
import V1.Component.UTXO;
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

	private static byte[] target;
	private static int currentTxFee;

	private static Map<Integer, List<byte[]>> prevBlockHashTable;
	private static UTXO utxoTable;

	static void init() {
		blockHeight = 1;

		target = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
		byte[] prevBlockHash = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_PREV_BLOCK_HASH);
		block = new Block(blockHeight, prevBlockHash, target);
		currentTxFee = 0;

		prevBlockHashTable = new HashMap<Integer, List<byte[]>>();
		utxoTable = new UTXO();
		
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
			if ((tmp = checkTxInput(it, tx, unavailableOutputList)) == 0) {
				it.remove();
			}
			availableInputSum += tmp;
		}
		Log.log("[Blockchain.addTransaction()] Unavailable Output: "+unavailableOutputList, Constant.Log.IMPORTANT);
		Log.log("[Blockchain.addTransaction()] Available Input Sum: " + availableInputSum, Constant.Log.TEMPORARY);
		for (Iterator<Output> it = txOutputList.iterator(); it.hasNext(); i++) {
			Output out = it.next();
			if (out == null) {
				break;
			}
			outputSum += out.getAmount();
		}

		if (availableInputSum < outputSum - Constant.Blockchain.TX_FEE) {
			return false;
		}

//		tx.removeNull();	equal to below code?
		tx.updateIn(txInputList.toArray(new Input[txInputList.size()]));

		if(!block.addTransaction(tx)) {
			return false;
		}
		Log.log("[Blockchain.addTransaction()] Block Update: " + block, Constant.Log.TEMPORARY);
		if(no.getType() == Constant.Blockchain.TX) {
			Log.log("[Blockchain.addTransaction()] Broadcast tx: " + tx, Constant.Log.TEMPORARY);
			BackendServer.shareBackend(new NetworkObject(Constant.Blockchain.TX_BROADCAST, no.getTx()));
		}
		
		MiningManager.updateMining(block);
		
		return true;
	}

	static boolean addBlock(NetworkObject no) {
		// TODO check fork (utxo)
		// TODO check Merkle tree
		// TODO update target
		Block newBlock = no.getBlock();
		int i;
		Iterator<Input> inputIt;
		Iterator<Output> outputIt;
		List<byte[]> unavailableOutputList = new ArrayList<byte[]>();
		UTXO utxoNewComponent = new UTXO();
		for (Transaction tx : newBlock.getTxList()) {
			i = 0;
			for (inputIt = Arrays.asList(tx.getIn()).iterator(); inputIt.hasNext();) {
				if (checkTxInput(inputIt, tx, unavailableOutputList) == 0) {
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
					utxoTable.add(out.getReceiver(), out);
					utxoNewComponent.add(out.getReceiver(), out);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("invalid block data", Constant.Log.EXCEPTION);
			return false;
		}
		BackendServer.shareFrontend(new NetworkObject(Constant.NetworkObject.TYPE_UTXO, utxoNewComponent));
		return goToNextBlock(newBlock, no.getType() == Constant.Blockchain.BLOCK);
	}
	static boolean goToNextBlock(Block newBlock, boolean broadcast) {
		try {
			Log.log("[Blockchain.goToNextBlock()] Save block: " + block);
			IO.fileWrite(Setting.BLOCKCHAIN_BIN_DIR + (blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR)
					+ File.separator, ""+blockHeight, ByteUtil.getByteObject(newBlock));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("invalid block data", Constant.Log.EXCEPTION);
			return false;
		}
		// TODO: manage orphan block...
		// TODO: manage utxoTable Version...
//		int newBlockHeight = newBlock.getBlockHeight();
		byte[] prevBlockHash = newBlock.getPrevBlockHash();
//		if (!prevBlockHashTable.containsKey(newBlockHeight)) {
//			List<byte[]> tmp = new ArrayList<byte[]>();
//			tmp.add(prevBlockHash);
//			prevBlockHashTable.put(newBlockHeight, tmp);
//		} else {
//			List<byte[]> tmp = prevBlockHashTable.get(newBlockHeight);
//			tmp.add(prevBlockHash);
//		}
//		if (prevBlockHashTable.size() >= Constant.Blockchain.MAX_PREV_BLOCK_HASH_LIST) {
//			prevBlockHashTable.remove(blockHeight - Constant.Blockchain.MAX_PREV_BLOCK_HASH_LIST);
//		}

		if(broadcast) {
			BackendServer.shareBackend(new NetworkObject(Constant.Blockchain.BLOCK_BROADCAST, newBlock));
		}

		blockHeight++;
		if(blockHeight % Constant.Blockchain.ADJUST_TARGET == 0) {
			target = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
		}
		block = new Block(blockHeight, prevBlockHash, target);
		
		MiningManager.updateMining(block);
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

	static int checkTxInput(Iterator<Input> it, Transaction tx, List<byte[]> unavailableOutputList) {
		Input in = it.next();
		if (in == null) {
			return 0;
		}
		Map<ByteBuffer, Output> utxoMap = utxoTable.get(ByteBuffer.wrap(in.getReceiver()));
		if (utxoMap == null) {
			Log.log("[Blockchain.addTransaction()] receiver not exists: " + in, Constant.Log.IMPORTANT);
			return 0;
		}
		Output out = utxoMap.get(ByteBuffer.wrap(in.getOutHash()));
		if (out == null) {
			Log.log("[Blockchain.addTransaction()] outHash not exists: " + in, Constant.Log.IMPORTANT);
			return 0;
		}

		Script script = new Script();
		Result result = script.resolve(out.getQuestion(), in.getAnswer(), in.getOutHash());

		Log.log("checkTx() script.resolve() result: " + result, Constant.Log.TEMPORARY);

		if (result == Constant.Script.Result.SOLVED) {
			return out.getAmount();
		} else if (result == Constant.Script.Result.FAILED) {
			unavailableOutputList.add(in.getOutHash());
		}
		return 0;
	}

	static Block getBlock() {
		return block;
	}

	static void setTestData() throws AddressFormatException, Exception {
		byte[] script = new byte[4];
		script[0] = OPCode.PUBK_DUP;
		script[1] = OPCode.HASH_TWICE;
		script[2] = OPCode.CHECK_ADDR;
		script[3] = OPCode.CHECK_SIG;
		byte[] receiver1 = Base58.decode("3zqBQ4ETSn2zM6nb8QqSB2MiiJehdGij5PnVcdMmTLBY9yfwtVoNtQxCGARaKbSVVFRAw7URwhMB67pGcBoxfsby");
		byte[] receiver2 = Base58.decode("3iU5kRFGE3qGZiYNYaWVEB2aZyq3V5aQSXgSLMzna4QDMGGo76BwGqGiZhSZ4QNc4VHGahfy7xHQhET6SgfXyFGu");
		utxoTable.add(receiver1, new Output(10, new Question(script, receiver1)));
		utxoTable.add(receiver2, new Output(10, new Question(script, receiver2)));
		BackendServer.shareFrontend(new NetworkObject(Constant.NetworkObject.TYPE_UTXO, utxoTable));
		Log.log("utxoList: " + utxoTable, Constant.Log.TEMPORARY);
	}
//	static Transaction verifyRequest(Request request) {
//		Transaction tx = null;
//		if(request.getType() == Constant.Request.TYPE_SEND_TOFU) {
//			Map<byte[], Output> utxoMap = utxoTable.get(ByteBuffer.wrap(request.getAddrFrom()));
//			if(utxoMap.containsKey(request.getTxHash())) {
//				Input[] inList = new Input[Constant.Transaction.MAX_INPUT_OUTPUT];
//				Output[] outList = new Output[Constant.Transaction.MAX_INPUT_OUTPUT];
//				tx = new Transaction(inList, outList, Constant.Transaction.VERSION, Constant.Transaction.DEFAULT_LOCKTIME, request.getTxHash());
//			}
//		}
//		return tx;
//	}
//
}
