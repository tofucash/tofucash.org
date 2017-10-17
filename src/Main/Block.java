package Main;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

public class Block implements Externalizable {
	private final static int NONCE_LENGTH = 512; 
	private final static int WALLET_LENGTH = 512;
	
	private int blockHeight;
	private byte[] nonce;
	private byte[] miner;
	private List tx;

	
	static void init() {
		Log.log("Block init done.");
	}
	public Block() {		
		blockHeight = 0;
		nonce = null;
		miner = null;
		tx = null;
	}
	void setBlockHeight(int blockHeight) {
		this.blockHeight = blockHeight;
	}
	static Block checkBlock(String json) {
		Block block = new Block();
		try {
			Map map = (Map) JSON.decode(json);
			if (map.containsKey("type") && map.get("type").equals("block")) {
			} else {
				return null;
			}
			if (map.containsKey("header") && ((Map)map.get("header")).size() > 0) {
				Map header = (Map) map.get("header");
				if(header.containsKey("blockHeight") && ((int) header.get("blockHeight")) >= Blockchain.getForkableBlockHeight()) {
					block.blockHeight = ((int) header.get("blockHeight"));
				} else {
					return null;
				}
				if(header.containsKey("nonce") && ((byte[]) header.get("nonce")).length == NONCE_LENGTH) {
					block.nonce = (byte[]) header.get("nonce");
				} else {
					return null;
				}
				if(header.containsKey("miner") && ((byte[]) header.get("miner")).length == WALLET_LENGTH) {
					block.miner = ((byte[]) header.get("miner"));
				} else {
					return null;
				}
			} else {
				return null;
			}
			if (map.containsKey("tx") && ((List) map.get("tx")).size() >= 0) {
				block.tx = (List) map.get("tx");
			} else {
				return null;
			}
		} catch (ClassCastException e) {
			// JSON.decode(json) --X--> Map
			// map.get("") --X--> List/Map
			return null;
		}
		return block;
	}
	synchronized boolean addTransaction(Transaction tx) {
		double inSum = Transaction.checkInList(tx);
		double outSum = Transaction.checkOutList(tx);
		return true;
	}
	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		System.out.println("block read");
		blockHeight = (int) oi.readObject();
		System.out.println("--blockHeight: " + blockHeight);
	}
	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		System.out.println("block write");
		oo.writeObject(blockHeight);
	}
	public String toString() {
		return "blockHeight: "+ blockHeight;
	}
}
