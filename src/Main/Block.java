package Main;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

public class Block implements Externalizable {
	private static final long serialVersionUID = 199603311050000L;
	private final static int NONCE_LENGTH = 512;
	private final static int WALLET_LENGTH = 512;

	private static byte[] myAddr;

	private BlockHeader header;
	private int txCnt;
	private Transaction[] txList;

	static void init() {
		Log.log("Block init done.");
	}

	public Block() {
		byte[] timestamp = new byte[Constant.BYTE_TIMESTAMP];
		header = new BlockHeader(Constant.BlockHeader.VERSION, new byte[Constant.Block.BYTE_BLOCK_HASH], timestamp,
				new byte[Constant.Block.BYTE_NONCE], Setting.getMyAddr());
		txCnt = 0;
		txList = new Transaction[Constant.Block.MAX_TX];
	}

	synchronized boolean addTransaction(Transaction tx) {
		txList[txCnt] = tx;
		txCnt++;
		return true;
	}

	byte[] getPrevBlockHash() {
		return header.getPrevBlockHash();
	}

	Transaction[] getTxList() {
		return txList;
	}

	void removeNull() {
		List<Transaction> txListAsList = new ArrayList<Transaction>(Arrays.asList(txList));
		txListAsList.removeAll(Collections.singleton(null));
		txList = txListAsList.toArray(new Transaction[txListAsList.size()]);
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		header = (BlockHeader) oi.readObject();
		txCnt = oi.readInt();
		System.out.println("txCnt: " + txCnt);
		txList = new Transaction[txCnt];
		txList = (Transaction[]) oi.readObject();
//		for(int i = 0; i < txCnt; i++) {
//			byte[] data = new byte[oi.readInt()];
//			oi.read(data, 0, data.length);
//			txList[i] = (Transaction)convertByteToTx(data);
//			System.out.println(txList[i]);
//		}
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(header);
		oo.writeInt(txCnt);
		System.out.println("txCnt: " + txCnt);
//		for (int i = 0; i < txList.length; i++) {
//			byte[] data = Library.getByteObject(txList[i]);
//			oo.writeInt(data.length);
//			oo.write(data);
//		}
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
