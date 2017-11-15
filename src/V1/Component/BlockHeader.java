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
	private int blockCnt;
	private byte[] prevBlockHash;
	private byte[] timestamp;
	private byte[] miner;
	private byte[] nonce;
	private byte[] MerkleRoot;

	public BlockHeader() {
		version = -1;
		blockHeight = -1;
		blockCnt = 0;
		prevBlockHash = null;
		timestamp = null;
		miner = null;
		nonce = null;
		MerkleRoot = null;
	}

	public BlockHeader(int version, int blockHeight, byte[] prevBlockHash, byte[] timestamp, byte[] miner) {
		this.version = version;
		this.blockHeight = blockHeight;
		this.blockCnt = 0;
		this.prevBlockHash = prevBlockHash;
		this.timestamp = timestamp;
		this.miner = miner;
		this.nonce = null;
		this.MerkleRoot = null;
	}

	byte[] getPrevBlockHash() {
		return prevBlockHash;
	}

	int getBlockHeight() {
		return blockHeight;
	}

	int getBlockCnt() {
		return blockCnt;
	}

	void incrementBlock() {
		blockCnt++;
	}
	void updateMerkleRoot(byte[] MerkleRoot) {
		this.MerkleRoot = MerkleRoot;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		version = oi.readInt();
		blockHeight = oi.readInt();
		blockCnt = oi.readInt();
		oi.read(prevBlockHash);
		oi.read(timestamp);
		oi.read(miner);
		oi.read(nonce);
		oi.read(MerkleRoot);
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(version);
		oo.writeInt(blockHeight);
		oo.writeInt(blockCnt);
		oo.write(prevBlockHash);
		oo.write(timestamp);
		oo.write(miner);
		oo.write(nonce);
		oo.write(MerkleRoot);
	}

	public String toString() {
		return "[version: " + version + ", prevBlockHash: " + DatatypeConverter.printHexBinary(prevBlockHash)
				+ ", timestamp: " + DatatypeConverter.printHexBinary(timestamp) + ", miner: "
				+ DatatypeConverter.printHexBinary(miner) + ", nonce: " + DatatypeConverter.printHexBinary(nonce)
				+ ", MerkleTree: " + DatatypeConverter.printHexBinary(MerkleRoot) + "]";
	}

}
