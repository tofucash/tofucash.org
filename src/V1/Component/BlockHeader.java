package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

public class BlockHeader implements Externalizable {
	private static final long serialVersionUID = 199603312020000L;
	private int version;
	private int blockHeight;
	private int txCnt;
	private byte[] prevBlockHash;
	private byte[] timestamp;
	private byte[] miner;
	private byte[] difficulty;
	private byte[] nonce;
	private byte[] merkleRoot;

	public BlockHeader() {
		version = -1;
		blockHeight = -1;
		txCnt = 0;
		prevBlockHash = null;
		timestamp = null;
		miner = null;
		difficulty = null;
		nonce = null;
		merkleRoot = null;
	}

	public BlockHeader(int version, int blockHeight, byte[] prevBlockHash, byte[] timestamp, byte[] miner, byte[] difficulty) {
		this.version = version;
		this.blockHeight = blockHeight;
		this.txCnt = 0;
		this.prevBlockHash = prevBlockHash;
		this.timestamp = timestamp;
		this.miner = miner;
		this.difficulty = difficulty;
		this.nonce = null;
		this.merkleRoot = null;
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
	void updateMerkleRoot(byte[] MerkleRoot) {
		this.merkleRoot = MerkleRoot;
	}
	
	byte[] getDifficulty() {
		return difficulty;
	}
	void updateNonce(byte[] nonce) {
		this.nonce = nonce;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		version = oi.readInt();
		blockHeight = oi.readInt();
		txCnt = oi.readInt();
		oi.read(prevBlockHash);
		oi.read(timestamp);
		oi.read(miner);
		oi.read(difficulty);
		oi.read(nonce);
		oi.read(merkleRoot);
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(version);
		oo.writeInt(blockHeight);
		oo.writeInt(txCnt);
		oo.write(prevBlockHash);
		oo.write(timestamp);
		oo.write(miner);
		oo.write(difficulty);
		oo.write(nonce);
		oo.write(merkleRoot);
	}

	public String toString() {
		return "[version: " + version + ", prevBlockHash: " + DatatypeConverter.printHexBinary(prevBlockHash)
				+ ", timestamp: " + DatatypeConverter.printHexBinary(timestamp) + ", miner: "
				+ DatatypeConverter.printHexBinary(miner) + ", nonce: " + DatatypeConverter.printHexBinary(nonce)
				+ ", MerkleTree: " + DatatypeConverter.printHexBinary(merkleRoot) + "]";
	}

}
