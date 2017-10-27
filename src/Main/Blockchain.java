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
		block = null;
		blockHeight = 0;
		prevBlockHashList = new ArrayList<byte[]>();
		Log.log("Blockchain init done.");
	}

	static int getForkableBlockHeight() {
		return blockHeight - FORKABLE_BLOCK_HEIGHT;
	}

	static void addTransaction(Transaction tx) {
		block.addTransaction(tx);
	}

	static void addBlock(Block newBlock) {
		// check block height (fork)
		// add to latest block pool
		// testBlockchain.add(newBlock);
		byte[] prevBlockHash = newBlock.getPrevBlockHash();
		if(!prevBlockHashList.contains(prevBlockHash)) {
			prevBlockHashList.add(prevBlockHash);
		}
		if(prevBlockHashList.size() >= Constant.Blockchain.MAX_PREV_BLOCK_HASH_LIST) {
			prevBlockHashList.remove(0);
		}
		Library.fileWrite(Setting.blockchainBinDir + blockHeight / Constant.Blockchain.SAVE_FILE_PER_DIR + blockHeight,
				Library.getByteObject(newBlock));
	}

	static Transaction getOutTransaction(int blockHeight, byte[] address) {
		Transaction tx = null;

		// to do
		// using markle tree

		return tx;
	}

}
