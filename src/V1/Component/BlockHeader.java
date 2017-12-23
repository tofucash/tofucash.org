package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;

public class BlockHeader implements Externalizable {
	private static final long serialVersionUID = 199603312020000L;
	private int version;
	private int blockHeight;
	private int txCnt;
	private byte[] prevBlockHash;
	private byte[] timestamp;
	private byte[] miner;
	private byte[] target;
	private byte[] nonce;
	private byte[] merkleRoot;

	public BlockHeader() {
		version = -1;
		blockHeight = -1;
		txCnt = 0;
		prevBlockHash = null;
		timestamp = null;
		miner = null;
		target = null;
		nonce = null;
		merkleRoot = null;
	}

	public BlockHeader(int version, int blockHeight, byte[] prevBlockHash, byte[] timestamp, byte[] miner, byte[] target) {
		this.version = version;
		this.blockHeight = blockHeight;
		this.txCnt = 0;
		this.prevBlockHash = prevBlockHash;
		this.timestamp = timestamp;
		this.miner = miner;
		this.target = target;
		this.nonce = new byte[Constant.Block.BYTE_NONCE];
		this.merkleRoot = new byte[Constant.MerkleTree.BYTE_MERKLE_ROOT];
	}

	byte[] getPrevBlockHash() {
		return prevBlockHash;
	}

	int getBlockHeight() {
		return blockHeight;
	}

	int getTxCnt() {
		return txCnt;
	}

	void incrementTx() {
		txCnt++;
	}
	void updateMerkleRoot(byte[] merkleRoot) {
		this.merkleRoot = merkleRoot;
	}
	
	byte[] getTarget() {
		return target;
	}
	void nonceFound(byte[] nonce, byte[] miner) {
		this.nonce = nonce;
		this.miner = miner;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		version = oi.readInt();
		blockHeight = oi.readInt();
		txCnt = oi.readInt();
		int prevBlockHashLength = oi.readInt();
		if(prevBlockHashLength > Constant.Block.BYTE_BLOCK_HASH) {
			return;
		}
		prevBlockHash = new byte[prevBlockHashLength];
		oi.read(prevBlockHash);
		
		int timestampLength = oi.readInt();
		if(timestampLength > Constant.Time.BYTE_TIMESTAMP) {
			return;
		}
		timestamp = new byte[timestampLength];
		oi.read(timestamp);

		int minerLength = oi.readInt();
		if(minerLength > Constant.NetworkObject.BYTE_MAX_MINER) {
			return;
		}
		miner = new byte[minerLength];		
		oi.read(miner);

		int targetLength = oi.readInt();
		if(targetLength > Constant.Block.BYTE_BLOCK_HASH) {
			return;
		}
		target = new byte[targetLength];		
		oi.read(target);
		
		int nonceLength = oi.readInt();
		if(nonceLength > Constant.Block.BYTE_NONCE) {
			return;
		}
		nonce = new byte[nonceLength];		
		oi.read(nonce);
		
		int merkleRootLength = oi.readInt();
		if(merkleRootLength > Constant.MerkleTree.BYTE_MERKLE_ROOT) {
			return;
		}
		merkleRoot = new byte[merkleRootLength];		
		oi.read(merkleRoot);
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(version);
		oo.writeInt(blockHeight);
		oo.writeInt(txCnt);
		oo.writeInt(prevBlockHash.length);
		oo.write(prevBlockHash);
		oo.writeInt(timestamp.length);
		oo.write(timestamp);
		oo.writeInt(miner.length);
		oo.write(miner);
		oo.writeInt(target.length);
		oo.write(target);
		oo.writeInt(nonce.length);
		oo.write(nonce);
		oo.writeInt(merkleRoot.length);
		oo.write(merkleRoot);
	}

	public String toString() {
		return "[version: " + version + ", blockHeight: "+blockHeight + ", prevBlockHash: " + DatatypeConverter.printHexBinary(prevBlockHash)
				+ ", timestamp: " + DatatypeConverter.printHexBinary(timestamp) + ", miner: "
				+ DatatypeConverter.printHexBinary(miner) + ", nonce: " + DatatypeConverter.printHexBinary(nonce)
				+ ", MerkleTree: " + DatatypeConverter.printHexBinary(merkleRoot) + "]";
	}

}
