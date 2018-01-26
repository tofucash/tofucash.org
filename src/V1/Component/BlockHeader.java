package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Log;

public class BlockHeader implements Externalizable {
	private static final long serialVersionUID = 199603312020000L;
	private int version;
	private int blockHeight;
	private int txCnt;
	private byte[] prevBlockHash;
	private long timestamp;
	private byte[] miner;
	private byte[] target;
	private byte[] subTarget;
	private byte[] nonce;
	private byte[] merkleRoot;
	private byte[] blockHash;

	public BlockHeader() {
		version = -1;
		blockHeight = -1;
		txCnt = 0;
		prevBlockHash = new byte[1];
		timestamp = -1;
		miner = null;
		target = null;
		subTarget = null;
		nonce = null;
		merkleRoot = null;
		blockHash = null;
	}

	public BlockHeader(int version, int blockHeight, byte[] prevBlockHash, long timestamp, byte[] miner, byte[] target, byte[] subTarget) {
		this.version = version;
		this.blockHeight = blockHeight;
		this.txCnt = 0;
		this.prevBlockHash = prevBlockHash;
		this.timestamp = timestamp;
		this.miner = miner;
		this.target = target;
		this.subTarget = subTarget;
		this.nonce = new byte[Constant.Block.BYTE_NONCE];
		this.merkleRoot = new byte[Constant.MerkleTree.BYTE_MERKLE_ROOT];
		this.blockHash = new byte[Constant.Block.BYTE_BLOCK_HASH];
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
	void setTxCnt(int txCnt) {
		this.txCnt = txCnt;
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
	byte[] getSubTarget() {
		return subTarget;
	}
	long getTimestamp() {
		return timestamp;
	}
	
	void nonceFound(byte[] nonce, byte[] miner, byte[] blockHash) {
		this.nonce = nonce;
		this.miner = miner;
		this.blockHash = blockHash;
	}
	void updateParam(long timestamp, byte[] prevBlockHash, byte[] target, byte[] subTarget) {
		this.timestamp = timestamp;
		this.prevBlockHash = prevBlockHash;
		this.target = target;
		this.subTarget = subTarget;
	}
	byte[] getBlockHash() {
		return blockHash;
	}
	byte[] getNonce() {
		return nonce;
	}
	byte[] getMiner() {
		return miner;
	}
	void resetNonce() {
		this.miner = new byte[Constant.Address.BYTE_ADDRESS];
		this.nonce = new byte[Constant.Block.BYTE_NONCE];
		this.blockHash = new byte[Constant.Block.BYTE_BLOCK_HASH];
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		version = oi.readInt();
		blockHeight = oi.readInt();
		int prevBlockHashLength = oi.readInt();
		if(prevBlockHashLength > Constant.Block.BYTE_BLOCK_HASH) {
			return;
		}
		prevBlockHash = new byte[prevBlockHashLength];
		oi.read(prevBlockHash);
		timestamp = oi.readLong();

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

		int subTargetLength = oi.readInt();
		if(subTargetLength > Constant.Block.BYTE_BLOCK_HASH) {
			return;
		}
		subTarget = new byte[subTargetLength];		
		oi.read(subTarget);

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

		int blockHashLength = oi.readInt();
		if(blockHashLength > Constant.Block.BYTE_BLOCK_HASH) {
			return;
		}
		blockHash = new byte[blockHashLength];		
		oi.read(blockHash);	
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(version);
		oo.writeInt(blockHeight);
		oo.writeInt(prevBlockHash.length);
		oo.write(prevBlockHash);
		oo.writeLong(timestamp);
		oo.writeInt(miner.length);
		oo.write(miner);
		oo.writeInt(target.length);
		oo.write(target);
		oo.writeInt(subTarget.length);
		oo.write(subTarget);
		oo.writeInt(nonce.length);
		oo.write(nonce);
		oo.writeInt(merkleRoot.length);
		oo.write(merkleRoot);
		oo.writeInt(blockHash.length);
		oo.write(blockHash);
	}

	public String toString() {
		return "[version: " + version + ", blockHeight: "+blockHeight + ", txCnt: "+txCnt+", prevBlockHash: " + DatatypeConverter.printHexBinary(prevBlockHash)
		 + ", target: " + DatatypeConverter.printHexBinary(target) + ", subTarget: " + DatatypeConverter.printHexBinary(subTarget)
				+ ", timestamp: " + timestamp + ", miner: "
				+ DatatypeConverter.printHexBinary(miner) + ", nonce: " + DatatypeConverter.printHexBinary(nonce)
				+ ", MerkleTree: " + DatatypeConverter.printHexBinary(merkleRoot) + "]";
	}

}
