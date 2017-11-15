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
	private final static int NONCE_LENGTH = 512;
	private final static int WALLET_LENGTH = 512;

	private BlockHeader header;
	private Transaction[] txList;

	private List<byte[]> merkleTree;
	private List<byte[]> txHashList;

	static void init() {
		Log.log("Block init done.");
	}

	public Block() {
		header = new BlockHeader(Constant.BlockHeader.VERSION, -1, new byte[Constant.Block.BYTE_BLOCK_HASH],
				new byte[Constant.Time.BYTE_TIMESTAMP], new byte[Constant.Address.BYTE_ADDRESS]);
		txList = new Transaction[Constant.Block.MAX_TX];
		merkleTree = new ArrayList<byte[]>();
		txHashList = new ArrayList<byte[]>();
	}

	public synchronized boolean addTransaction(Transaction tx) {
		// TODO update MerkleTree and root
		try {
			merkleTree.add(Crypto.hash256(ByteUtil.getByteObject(tx)));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("invalid data", Constant.Log.EXCEPTION);
			return false;
		}
		if (!updateMerkleTree()) {
			merkleTree.remove(merkleTree.size() - 1);
			return false;
		}
		header.updateMerkleRoot(merkleTree.get(0));
		txList[header.getBlockCnt()] = tx;
		header.incrementBlock();
		return true;
	}

	synchronized private boolean updateMerkleTree() {
		// 2(+0=2)->3 3(+1=4)->6 5(+3=7)->10
		// size > 2^0 -> +1 size > 2^1 -> +2 size > 2^2 -> +3
		// 2(0) 3(2) 5()
		int powered = 1;
		int size = merkleTree.size();
		ByteBuffer buf;
		// TODO use sqrt() instead of pow() (^i)
		for (int i = 1; i < Constant.Block.MAX_TX_POWER; i++) {
			if (size > powered && size <= (powered = 2 ^ i)) {
				int merkleTreeIndex = merkleTree.size() - 1;
				int diff = 2 ^ (i - 1) - 1;
				if (size == (2 ^ (i - 1) + 1)) {
					merkleTree.add(0, null);
					int insertIndex = 2;
					while (insertIndex < merkleTree.size()) {
						for (int j = 0; j < Math.sqrt(insertIndex); j++) {
							merkleTree.add(insertIndex, new byte[Constant.Block.BYTE_BLOCK_HASH]);
						}
						insertIndex = insertIndex * 2 + 1;
					}
				}
				if(size % 2 ==  1) {
					merkleTree.add(0, null);
				}

				buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
				buf.put(txHashList.get((merkleTreeIndex - diff) * 2));
				buf.put(txHashList.get((merkleTreeIndex - diff) * 2 + 1));
				merkleTree.set(merkleTreeIndex, Crypto.hash512(buf.array()));
				while (merkleTreeIndex > 0) {
					buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
					if (merkleTreeIndex % 2 == 0) {
						buf.put(merkleTree.get(merkleTreeIndex - 1));
						buf.put(merkleTree.get(merkleTreeIndex));
					} else {
						buf.put(merkleTree.get(merkleTreeIndex));
						buf.put(merkleTree.get(merkleTreeIndex + 1));
					}
					merkleTreeIndex = (int) Math.floor(merkleTreeIndex - 1);
					merkleTree.set(merkleTreeIndex, Crypto.hash512(buf.array()));
				}
				return true;
			}

		}
		return false;
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

	public void removeNull() {
		List<Transaction> txListAsList = new ArrayList<Transaction>(Arrays.asList(txList));
		txListAsList.removeAll(Collections.singleton(null));
		txList = txListAsList.toArray(new Transaction[txListAsList.size()]);
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		header = (BlockHeader) oi.readObject();
		txList = new Transaction[header.getBlockCnt()];
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
