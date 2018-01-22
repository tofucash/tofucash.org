package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Log;
import V1.Library.TofuError;

public class NetworkObject implements Externalizable {
	private static final long serialVersionUID = 199603311030000L;
	private int type;
	private Block block;
	private Transaction tx;
	private Node node;
	private Work work;
	private Report report;
	private Request[] requestList;
	private UTXO utxo;
	private Spent[] spentList;
	private byte[] hash;
	private Routine routine;
	private int blockHeight;

	public NetworkObject() {
		block = null;
		tx = null;
		node = null;
		work = null;
		report = null;
		requestList = null;
		utxo = null;
		spentList = null;
		hash = null;
		routine = null;
		blockHeight = -1;
	}

	public NetworkObject(int type, Object data) {
		this.type = type;
		block = null;
		tx = null;
		node = null;
		work = null;
		report = null;
		requestList = null;
		utxo = null;
		spentList = null;
		hash = null;
		routine = null;
		blockHeight = -1;
		if (type - Constant.NetworkObject.TYPE_BLOCK < 100) {
			if(type == Constant.NetworkObject.TYPE_BLOCK_CHECK) {
				blockHeight = (int) data;
				return ;
			}
			block = (Block) data;
			block.removeNull();
			Iterator<Transaction> it;
			int i;
			Transaction[] txList = block.getTxList();
			for (i = 0; i < txList.length; i++) {
				txList[i].removeNull();
			}
		} else if (type - Constant.NetworkObject.TYPE_TX < 100) {
			tx = (Transaction) data;
			tx.removeNull();
		} else if (type - Constant.NetworkObject.TYPE_NODE < 100) {
			node = (Node) data;
		} else if (type - Constant.NetworkObject.TYPE_WORK < 100) {
			work = (Work) data;
		} else if (type - Constant.NetworkObject.TYPE_REPORT < 100) {
			report = (Report) data;
		} else if (type - Constant.NetworkObject.TYPE_REQUEST < 100) {
			requestList = (Request[]) data;
		} else if (type - Constant.NetworkObject.TYPE_UTXO < 100) {
			if(type == Constant.NetworkObject.TYPE_UTXO_CHECK) {
				blockHeight = (int) data;
				return ;
			}
			utxo = (UTXO) data;
		} else if (type - Constant.NetworkObject.TYPE_SPENT < 100) {
			spentList = (Spent[]) data;
		} else if (type - Constant.NetworkObject.TYPE_BLOCK_HASH < 100) {
			hash = (byte[]) data;
		} else if (type - Constant.NetworkObject.TYPE_ROUTINE < 100) {
			routine = (Routine) data;
		} else {
			throw new TofuError.UnimplementedError("Invalid NetworkObject Type");
		}
	}

	public int getType() {
		return type;
	}

	public Transaction getTx() {
		return tx;
	}

	public Block getBlock() {
		return block;
	}
	public Node getNode() {
		return node;
	}
	public Work getWork() {
		return work;
	}
	public Report getReport() {
		return report;
	}
	public Request[] getRequest() {
		return requestList;
	}
	public UTXO getUTXO() {
		return utxo;
	}
	public Spent[] getSpent() {
		return spentList;
	}
	public byte[] getHash() {
		return hash;
	}
	public Routine getRoutine() {
		return routine;
	}
	public int getBlockHeight() {
		return blockHeight;
	}
	public String toString() {
		if (type - Constant.NetworkObject.TYPE_BLOCK < 100) {
			if(type == Constant.NetworkObject.TYPE_BLOCK_CHECK) {
				return "[type: " + type + ", blockHeight: " + blockHeight + "]";
			}
			return "[type: " + type + ", block: " + block.toString() + "]";
		} else if (type - Constant.NetworkObject.TYPE_TX < 100) {
			return "[type: " + type + ", tx:" + tx.toString() + "]";
		} else if (type - Constant.NetworkObject.TYPE_NODE < 100) {
			return "[type: " + type + ", node: " + node.toString() + "]";
		} else if (type - Constant.NetworkObject.TYPE_WORK < 100) {
			return "[type: " + type + ", work: " + work + "]";
		} else if (type - Constant.NetworkObject.TYPE_REPORT < 100) {
			return "[type: " + type + ", report: " + report + "]";
		} else if (type - Constant.NetworkObject.TYPE_REQUEST < 100) {
			return "[type: " + type + ", request: " + Arrays.toString(requestList) + "]";
		} else if (type - Constant.NetworkObject.TYPE_UTXO < 100) {
			if(type == Constant.NetworkObject.TYPE_UTXO_CHECK) {
				return "[type: " + type + ", blockHeight: " + blockHeight + "]";
			}
			return "[type: " + type + ", utxo: " + utxo + "]";
		} else if (type - Constant.NetworkObject.TYPE_SPENT < 100) {
			return "[type: " + type + ", spent: " + Arrays.toString(spentList) + "]";
		} else if (type - Constant.NetworkObject.TYPE_BLOCK_HASH < 100) {
			return "[type: " + type + ", hash: " + DatatypeConverter.printHexBinary(hash) + "]";
		} else if (type - Constant.NetworkObject.TYPE_ROUTINE < 100) {
			return "[type: " + type + ", routine: " + routine + "]";
		} else {
			throw new TofuError.UnimplementedError("unknown type");
		}
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		type = (int) oi.readObject();
		if (type - Constant.NetworkObject.TYPE_BLOCK < 100) {
			if(type == Constant.NetworkObject.TYPE_BLOCK_CHECK) {
				blockHeight = oi.readInt();
			} else {
				block = (Block) oi.readObject();
			}
		} else if (type - Constant.NetworkObject.TYPE_TX < 100) {
			tx = (Transaction) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_NODE < 100) {
			node = (Node) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_WORK < 100) {
			work = (Work) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_REPORT < 100) {
			report = (Report) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_REQUEST < 100) {
			int requestListLength = oi.readInt();
			requestList = new Request[requestListLength];
			for(int i = 0; i < requestListLength; i++) {
				requestList[i] = (Request) oi.readObject();
			}
		} else if (type - Constant.NetworkObject.TYPE_UTXO < 100) {
			if(type == Constant.NetworkObject.TYPE_UTXO_CHECK) {
				blockHeight = oi.readInt();
			} else {
				utxo = (UTXO) oi.readObject();
			}
		} else if (type - Constant.NetworkObject.TYPE_SPENT < 100) {
			int spentListLength = oi.readInt();
			spentList = new Spent[spentListLength];
			for(int i = 0; i < spentListLength; i++) {
				spentList[i] = (Spent) oi.readObject();
			}
		} else if (type - Constant.NetworkObject.TYPE_BLOCK_HASH < 100) {
			int blockHashLength = oi.readInt();
			if(blockHashLength > Constant.Block.BYTE_BLOCK_HASH) {
				return;
			}
			hash = new byte[blockHashLength];
			oi.read(hash);
		} else if (type - Constant.NetworkObject.TYPE_ROUTINE < 100) {
			routine = (Routine) oi.readObject();
		}
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(type);
		if (type - Constant.NetworkObject.TYPE_BLOCK < 100) {
			if(type == Constant.NetworkObject.TYPE_BLOCK_CHECK) {
				oo.writeInt(blockHeight);
			} else {
				oo.writeObject(block);				
			}
		} else if (type - Constant.NetworkObject.TYPE_TX < 100) {
			oo.writeObject(tx);
		} else if (type - Constant.NetworkObject.TYPE_NODE < 100) {
			oo.writeObject(node);
		} else if (type - Constant.NetworkObject.TYPE_WORK < 100) {
			oo.writeObject(work);
		} else if (type - Constant.NetworkObject.TYPE_REPORT < 100) {
			oo.writeObject(report);
		} else if (type - Constant.NetworkObject.TYPE_REQUEST < 100) {
			oo.writeInt(requestList.length);
			for(int i = 0; i < requestList.length; i++) {
				oo.writeObject(requestList[i]);
			}
		} else if (type - Constant.NetworkObject.TYPE_UTXO < 100) {
			if(type == Constant.NetworkObject.TYPE_UTXO_CHECK) {
				oo.writeInt(blockHeight);
			} else {
				oo.writeObject(utxo);
			}
		} else if (type - Constant.NetworkObject.TYPE_SPENT < 100) {
			oo.writeInt(spentList.length);
			for(int i = 0; i < spentList.length; i++) {
				oo.writeObject(spentList[i]);
			}
		} else if (type - Constant.NetworkObject.TYPE_BLOCK_HASH < 100) {
			oo.writeInt(hash.length);
			oo.write(hash);
		} else if (type - Constant.NetworkObject.TYPE_ROUTINE < 100) {
			oo.writeObject(routine);
		}
	}
}
