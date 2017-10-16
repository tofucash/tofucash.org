package Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Blockchain {
	
	private final static int FORKABLE_BLOCK_HEIGHT = 3;

	private static Block block;
	protected static int blockHeight;
	
	static void init() {
		block = null;
		blockHeight = 0;
		Log.log("Blockchain init done.");
	}
	
	
	static int getForkableBlockHeight() {
		return blockHeight - FORKABLE_BLOCK_HEIGHT;
	}
	
	static void addTransaction(Transaction tx) {
		block.addTransaction(tx);
	}
	static void addBlock(Block newBlock) {
		// check blockheigh (fork)
		testBlockchain.add(newBlock);
	}
	static Transaction getOutTransaction(int blockHeight, byte[] address) {
		Transaction tx = null;
		
		// to do
		// using markle tree
		
		return tx;
	}

	private static List<Block> testBlockchain = new ArrayList<Block>(); 
}
