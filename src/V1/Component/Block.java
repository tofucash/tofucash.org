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
import V1.TestClient.Setting;
import net.arnx.jsonic.JSON;

public class Block implements Externalizable {
	private static final long serialVersionUID = 199603311050000L;

	private BlockHeader header;
	private Transaction[] txList;

	private List<byte[]> merkleTree;
	private List<byte[]> txHashList;

	static void init() {
		Log.log("Block init done.");
	}

	public Block() {
		header = null;
		txList = null;
		merkleTree = null;
		txHashList = null;
	}
	public Block(byte[] target) {
		header = new BlockHeader(Constant.BlockHeader.VERSION, -1, new byte[Constant.Block.BYTE_BLOCK_HASH],
				new byte[Constant.Time.BYTE_TIMESTAMP], new byte[Constant.Address.BYTE_ADDRESS], target);
		txList = new Transaction[Constant.Block.MAX_TX];
		merkleTree = new ArrayList<byte[]>();
		txHashList = new ArrayList<byte[]>();
	}

	public synchronized boolean addTransaction(Transaction tx) {
		try {
			txHashList.add(Crypto.hash256(ByteUtil.getByteObject(tx)));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("invalid data", Constant.Log.EXCEPTION);
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
	public void nonceFound(byte[] nonce, byte[] miner) {
		header.nonceFound(nonce, miner);
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
		txList = (Transaction[]) oi.readObject();
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
		oo.writeObject(txList);
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
