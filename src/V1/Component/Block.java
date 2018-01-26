package V1.Component;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.MerkleTree;
import V1.Library.Time;
import V1.TestClient.Setting;
import net.arnx.jsonic.JSON;

public class Block implements Externalizable {
	private static final long serialVersionUID = 199603311050000L;

	private BlockHeader header;
	private ArrayList<Transaction> txList;

	private List<byte[]> merkleTree;
	private List<byte[]> txHashList;
	private Object txListLock;

	public Block() {
		header = null;
		txList = null;
		merkleTree = null;
		txHashList = null;
		txListLock = new Object();
	}

	public Block(int blockHeight) {
		txList = new ArrayList<Transaction>();
		merkleTree = new ArrayList<byte[]>();
		txHashList = new ArrayList<byte[]>();
		header = new BlockHeader(Constant.BlockHeader.VERSION, blockHeight, new byte[1], 0,
				new byte[Constant.Address.BYTE_ADDRESS], new byte[1], new byte[1]);
		txListLock = new Object();
	}

	// genesisblockのため
	public Block(int blockHeight, byte[] prevBlockHash, byte[] target, byte[] subTarget) {
		txList = new ArrayList<Transaction>();
		merkleTree = new ArrayList<byte[]>();
		txHashList = new ArrayList<byte[]>();
		header = new BlockHeader(Constant.BlockHeader.VERSION, blockHeight, prevBlockHash, 0,
				new byte[Constant.Address.BYTE_ADDRESS], target, subTarget);
		txListLock = new Object();
	}

	public boolean addTransaction(Transaction tx) {
		synchronized (txListLock) {
			try {
				txHashList.add(Crypto.hash256(ByteUtil.getByteObject(tx)));
			} catch (Exception e) {
				e.printStackTrace();
				Log.log("[Block.addTransaction()] Invalid data", Constant.Log.EXCEPTION);
				return false;
			}
			if (!MerkleTree.updateMerkleTree(merkleTree, txHashList)) {
				merkleTree.remove(merkleTree.size() - 1);
				return false;
			}
			header.updateMerkleRoot(merkleTree.get(0));
			txList.add(tx);
			return true;
		}
	}

	private boolean addTransactionNotLock(Transaction tx) {
		try {
			txHashList.add(Crypto.hash256(ByteUtil.getByteObject(tx)));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[Block.addTransaction()] Invalid data", Constant.Log.EXCEPTION);
			return false;
		}
		if (MerkleTree.updateMerkleTree(merkleTree, txHashList)) {
			try {
				merkleTree.remove(merkleTree.size() - 1);
			} catch(Exception e){
				Log.log("[Block.addTransactionNotLock()] Merkle Tree Invalid", Constant.Log.INVALID);
			}
			return false;
		}
		header.updateMerkleRoot(merkleTree.get(0));
		txList.add(tx);
		return true;
	}

	public synchronized void removeInvalidTx(int index) {
		synchronized (txListLock) {
			ArrayList<Transaction> oldTxList = (ArrayList<Transaction>) txList.clone();
			merkleTree = new ArrayList<byte[]>();
			txHashList = new ArrayList<byte[]>();
			for (int i = 0; i < oldTxList.size(); i++) {
				if (i == index) {
					continue;
				}
				addTransactionNotLock(oldTxList.get(i));
			}
		}
	}

	public BlockHeader getBlockHeader() {
		return header;
	}

	public byte[] getPrevBlockHash() {
		return header.getPrevBlockHash();
	}

	public int getBlockHeight() {
		return header.getBlockHeight();
	}
	public int getTxCnt() {
		return header.getTxCnt();
	}

	public Transaction[] getTxList() {
		return txList.toArray(new Transaction[txList.size()]);
	}

	public byte[] getTarget() {
		return header.getTarget();
	}

	public byte[] getSubTarget() {
		return header.getSubTarget();
	}

	public long getTimestamp() {
		return header.getTimestamp();
	}

	public void nonceFound(byte[] nonce, byte[] miner, byte[] blockHash) {
		header.nonceFound(nonce, miner, blockHash);
	}

	public byte[] getBlockHash() {
		return header.getBlockHash();
	}

	public byte[] getNonce() {
		return header.getNonce();
	}

	public byte[] getMiner() {
		return header.getMiner();
	}

	public void resetNonce() {
		header.resetNonce();
	}

	public void updateHeader(byte[] prevBlockHash, byte[] target, byte[] subTarget) {
		long timestamp = Time.getTimestamp();
		header.updateParam(timestamp, prevBlockHash, target, subTarget);
	}

	public void removeNull() {
		txList.removeAll(Collections.singleton(null));
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		header = (BlockHeader) oi.readObject();
		int txCnt = oi.readInt();
		header.setTxCnt(txCnt);
		txList = new ArrayList<Transaction>();
		for (int i = 0; i < txCnt; i++) {
			txList.add((Transaction) oi.readObject());
		}
		// for(int i = 0; i < txCnt; i++) {
		// byte[] data = new byte[oi.readInt()];
		// oi.read(data, 0, data.length);
		// txList[i] = (Transaction)convertByteToTx(data);
		// System.out.println(txList[i]);
		// }
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(header);
		// for (int i = 0; i < txList.length; i++) {
		// byte[] data = Library.getByteObject(txList[i]);
		// oo.writeInt(data.length);
		// oo.write(data);
		// }
		int txCnt = txList.size();
		oo.writeInt(txCnt);
		for (int i = 0; i < txCnt; i++) {
			oo.writeObject(txList.get(i));
		}
	}

	Transaction convertByteToTx(byte[] data) {
		Transaction tx = null;
		try {
			ByteArrayInputStream byteis = new ByteArrayInputStream(data);
			ObjectInputStream objis = new ObjectInputStream(byteis);
			tx = (Transaction) objis.readObject();
			byteis.close();
			objis.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return tx;
	}

	public String toString() {
		return "[header: " + header.toString() + ", txList: " + txList + "]";
	}
}
