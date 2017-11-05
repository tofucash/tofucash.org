package Main;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

public class Blockchain {

	private final static int FORKABLE_BLOCK_HEIGHT = 3;

	private static Block block;
	static int blockHeight;
	private static List<byte[]> prevBlockHashList;
	private static Map<ByteBuffer, Output> utxoList;

	static void init() {
		block = new Block();
		blockHeight = 1;
		prevBlockHashList = new ArrayList<byte[]>();
		utxoList = new HashMap<ByteBuffer, Output>();
		Log.log("Blockchain init done.");
	}

	static int getForkableBlockHeight() {
		return blockHeight - FORKABLE_BLOCK_HEIGHT;
	}

	static boolean addTransaction(Transaction tx) {
		// check transaction input is based on utxo
		int availableSum = 0;
		int i = 0, tmp = 0;
		List<byte[]> unavailableOutputList = new ArrayList<byte[]>();
		List<Input> txList = new ArrayList<Input>(Arrays.asList(tx.getIn()));
		for (Iterator<Input> it = txList.iterator(); it.hasNext(); i++) {
			if ((tmp = checkTx(it, tx, i, unavailableOutputList)) == 0) {
				break;
			}
			availableSum += tmp;
		}
		// message
		// unavailable outputlist size() > 0
		System.out.println("[addTransaction()] availableSum: " + availableSum);

		tx.updateIn(txList.toArray(new Input[txList.size()]));
		System.out.println("block: " + block);
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
				if(checkTx(it, tx, i, unavailableOutputList) == 0) {
					return false;
				}
			}
		}
		if(unavailableOutputList.size() != 0) {
			return false;
		}
		byte[] prevBlockHash = newBlock.getPrevBlockHash();
		if (!prevBlockHashList.contains(prevBlockHash)) {
			prevBlockHashList.add(prevBlockHash);
		}
		if (prevBlockHashList.size() >= Constant.Blockchain.MAX_PREV_BLOCK_HASH_LIST) {
			prevBlockHashList.remove(0);
		}
		Library.fileWrite(Setting.blockchainBinDir + (blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR)
				+ Constant.Environment.SEPARATOR + blockHeight, Library.getByteObject(newBlock));
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
		if(in == null) {
			return 0;
		}
		// in.outBlockHeight ? unnecessary for now
		// in.outTxHash
		Output out = utxoList.get(ByteBuffer.wrap(in.getOutTxHash()));
//		System.out.println("in.getOutTxHash(): " + DatatypeConverter.printHexBinary(in.getOutTxHash()));
//		System.out.println("out: " + out);

		Script script = new Script();
		// out.question, in.answer
		Constant.Script.Result result = script.resolve(out.getQuestion(), in.getAnswer(), tx, index);
		// このstateをどうするか ethereumを参考に

		System.out.println("result: " + result);
		if (result == Constant.Script.Result.SOLVED) {
			return out.getAmount();
		} else if (result == Constant.Script.Result.FAILED) {
			unavailableOutputList.add(in.getOutTxHash());
			it.remove();
		}
		return 0;
	}

	static Block getBlock() {
		return block;
	}

	static void setTestData() {
		System.out.println(
				"new byte[] {0x01, 0x02, 0x03}: " + DatatypeConverter.printHexBinary(new byte[] { 0x01, 0x02, 0x03 }));
		utxoList.put(ByteBuffer.wrap(new byte[] { 0x01, 0x02, 0x03 }), new Output(1, 1,
				new Question(new byte[] { Constant.Script.OPCode.POP32_0, Constant.Script.OPCode.TRUE })));
		System.out.println("utxoList: " + utxoList);
	}

}
