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
	private Request request;
	private UTXO utxo;

	public NetworkObject() {
		block = null;
		tx = null;
		node = null;
		work = null;
		report = null;
		request = null;
		utxo = null;
	}

	public NetworkObject(int type, Object data) {
		this.type = type;
		block = null;
		tx = null;
		node = null;
		work = null;
		report = null;
		request = null;
		utxo = null;
		if (type - Constant.NetworkObject.TYPE_BLOCK < 100) {
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
			request = (Request) data;
		} else if (type - Constant.NetworkObject.TYPE_UTXO < 100) {
			utxo = (UTXO) data;
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
	public Request getRequest() {
		return request;
	}
	public UTXO getUTXO() {
		return utxo;
	}
	public String toString() {
		if (type - Constant.NetworkObject.TYPE_BLOCK < 100) {
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
			return "[type: " + type + ", request: " + request + "]";
		} else if (type - Constant.NetworkObject.TYPE_UTXO < 100) {
			return "[type: " + type + ", utxo: " + utxo + "]";
		} else {
			throw new TofuError.UnimplementedError("unknown type");
		}
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		type = (int) oi.readObject();
		if (type - Constant.NetworkObject.TYPE_BLOCK < 100) {
			block = (Block) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_TX < 100) {
			tx = (Transaction) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_NODE < 100) {
			node = (Node) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_WORK < 100) {
			work = (Work) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_REPORT < 100) {
			report = (Report) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_REQUEST < 100) {
			request = (Request) oi.readObject();
		} else if (type - Constant.NetworkObject.TYPE_UTXO < 100) {
			utxo = (UTXO) oi.readObject();
		}
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(type);
		if (type - Constant.NetworkObject.TYPE_BLOCK < 100) {
			oo.writeObject(block);
		} else if (type - Constant.NetworkObject.TYPE_TX < 100) {
			oo.writeObject(tx);
		} else if (type - Constant.NetworkObject.TYPE_NODE < 100) {
			oo.writeObject(node);
		} else if (type - Constant.NetworkObject.TYPE_WORK < 100) {
			oo.writeObject(work);
		} else if (type - Constant.NetworkObject.TYPE_REPORT < 100) {
			oo.writeObject(report);
		} else if (type - Constant.NetworkObject.TYPE_REQUEST < 100) {
			oo.writeObject(request);
		} else if (type - Constant.NetworkObject.TYPE_UTXO < 100) {
			oo.writeObject(utxo);
		}
	}
}
