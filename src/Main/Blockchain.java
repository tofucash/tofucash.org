package Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Blockchain {

	private final static int FORKABLE_BLOCK_HEIGHT = 3;

	private static Block block;
	static int blockHeight;
	private static List<byte[]> prevBlockHashList;
	private static Map<byte[], Output> utxoList;
	static void init() {
		block = new Block();
		blockHeight = 1;
		prevBlockHashList = new ArrayList<byte[]>();
		utxoList = new HashMap<byte[], Output>();
		Log.log("Blockchain init done.");
	}

	static int getForkableBlockHeight() {
		return blockHeight - FORKABLE_BLOCK_HEIGHT;
	}

	static boolean addTransaction(Transaction tx) {
		// check transaction input is based on utxo
		int availableSum = 0;
		int i = 0;
		List<byte[]> unavailableOutputList = new ArrayList<byte[]>();
		List<Input> txList = new ArrayList<Input>(Arrays.asList(tx.getIn()));
		for(Iterator<Input> it = txList.iterator(); it.hasNext(); i++) {
			Input in = it.next();
			// in.outBlockHeight ??　不要かも
			// in.outTxHash
			Output out = utxoList.get(in.getOutTxHash());
			
			Script script = new Script();
			// out.question, in.answer
			Constant.Script.Result result = script.resolve(out.question, in.getAnswer(), tx, i);
			// このstateをどうするか ethereumを参考に
			
			if(result == Constant.Script.Result.SOLVED) {
				availableSum += out.amount;
			}else if(result == Constant.Script.Result.FAILED) {
				unavailableOutputList.add(in.getOutTxHash());
				it.remove();
			} else {
				throw new TofuException.UnimplementedError("Undefined State.");
			}
		}
		// message
		// unavailable outputlist size() > 0
		
		tx.updateIn((Input[])txList.toArray());
		return block.addTransaction(tx);
	}

	static void addBlock(Block newBlock) {
		// check block height (fork)
		// add to latest block pool
		// testBlockchain.add(newBlock);
		System.out.println("addBlock");
		byte[] prevBlockHash = newBlock.getPrevBlockHash();
		if(!prevBlockHashList.contains(prevBlockHash)) {
			prevBlockHashList.add(prevBlockHash);
		}
		if(prevBlockHashList.size() >= Constant.Blockchain.MAX_PREV_BLOCK_HASH_LIST) {
			prevBlockHashList.remove(0);
		}
		Library.fileWrite(Setting.blockchainBinDir + (blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR) + Constant.Environment.SEPARATOR + blockHeight,
				Library.getByteObject(newBlock));
	}

	static Transaction getOutTransaction(int blockHeight, byte[] address) {
		Transaction tx = null;

		// to do
		// using markle tree

		return tx;
	}
	
	static Block getBlock() {
		return block;
	}

}
