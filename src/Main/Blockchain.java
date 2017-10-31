package Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Blockchain {

	private final static int FORKABLE_BLOCK_HEIGHT = 3;

	private static Block block;
	static int blockHeight;
	private static List<byte[]> prevBlockHashList;
	static void init() {
		block = new Block();
		blockHeight = 1;
		prevBlockHashList = new ArrayList<byte[]>();
		Log.log("Blockchain init done.");
	}

	static int getForkableBlockHeight() {
		return blockHeight - FORKABLE_BLOCK_HEIGHT;
	}

	static boolean addTransaction(Transaction tx) {
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
