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
	private Transaction[] txList;

	private List<byte[]> merkleTree;
	private List<byte[]> txHashList;

	public Block() {
		header = null;
		txList = null;
		merkleTree = null;
		txHashList = null;
	}
	public Block(int blockHeight) {
		txList = new Transaction[Constant.Block.MAX_TX];
		merkleTree = new ArrayList<byte[]>();
		txHashList = new ArrayList<byte[]>();
		header = new BlockHeader(Constant.BlockHeader.VERSION, blockHeight, new byte[1],
				0, new byte[Constant.Address.BYTE_ADDRESS], new byte[1], new byte[1]);
	}
	
	public synchronized boolean addTransaction(Transaction tx) {
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
		txList[header.getTxCnt()] = tx;
		header.incrementTx();
		return true;
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

	public Transaction[] getTxList() {
		return txList;
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
		List<Transaction> txListAsList = new ArrayList<Transaction>(Arrays.asList(txList));
		txListAsList.removeAll(Collections.singleton(null));
		txList = txListAsList.toArray(new Transaction[txListAsList.size()]);
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		header = (BlockHeader) oi.readObject();
		txList = new Transaction[header.getTxCnt()];
		for(int i = 0; i < header.getTxCnt(); i++) {
			txList[i] = (Transaction) oi.readObject();
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
		for(int i = 0; i < txList.length; i++) {
			oo.writeObject(txList[i]);
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
		return "[header: " + header.toString() + ", txList: " + Arrays.asList(txList).toString() + "]";
	}
}
