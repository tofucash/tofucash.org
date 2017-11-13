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
import V1.Library.TofuError;

public class NetworkObject implements Externalizable {
	private static final long serialVersionUID = 199603311030000L;
	private int type;
	private Transaction tx;
	private Block block;
	private Node node;
	private byte[] hash;

	public NetworkObject() {
		tx = null;
		block = null;
		node = null;
		hash = null;
	}

	public NetworkObject(int type, Object data) {
		this.type = type;
		if (type == Constant.NetworkObject.TX) {
			tx = (Transaction) data;
			tx.removeNull();
			block = null;
			node = null;
		} else if (type == Constant.NetworkObject.BLOCK) {
			block = (Block) data;
			tx = null;
			node = null;
			block.removeNull();
			Iterator<Transaction> it;
			int i;
			Transaction[] txList = block.getTxList();
			for (i = 0; i < txList.length; i++) {
				txList[i].removeNull();
			}
		} else if (type == Constant.NetworkObject.NODE) {
			node = (Node) data;
			block = null;
			tx = null;
		} else if (type == Constant.NetworkObject.HASH) {
			hash = (byte[]) data;
			node = null;
			block = null;
			tx = null;
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
	public byte[] getHash() {
		return hash;
	}

	public String toString() {
		if (type == Constant.NetworkObject.TX) {
			return "[type: " + type + ", tx:" + tx.toString() + "]";
		} else if (type == Constant.NetworkObject.BLOCK) {
			return "[type: " + type + ", block: " + block.toString() + "]";
		} else if (type == Constant.NetworkObject.NODE) {
			return "[type: " + type + ", node: " + node.toString() + "]";
		} else if (type == Constant.NetworkObject.HASH) {
			return "[type: " + type + ", hash: " + DatatypeConverter.printHexBinary(hash) + "]";
		} else {
			throw new TofuError.UnimplementedError("unknown type");
		}
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		type = (int) oi.readObject();
		if (type == Constant.NetworkObject.TX) {
			tx = (Transaction) oi.readObject();
		} else if (type == Constant.NetworkObject.BLOCK) {
			block = (Block) oi.readObject();
		} else if (type == Constant.NetworkObject.NODE) {
			node = (Node) oi.readObject();
		} else if (type == Constant.NetworkObject.HASH) {
			int byteHash = oi.readInt();
			if(byteHash > Constant.NetworkObject.BYTE_MAX_HASH) {
				return;
			}
			hash = new byte[byteHash];
			oi.read(hash);
		}
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(type);
		if (type == Constant.NetworkObject.TX) {
			oo.writeObject(tx);
		} else if (type == Constant.NetworkObject.BLOCK) {
			oo.writeObject(block);
		} else if (type == Constant.NetworkObject.NODE) {
			oo.writeObject(node);
		} else if (type == Constant.NetworkObject.HASH) {
			oo.writeInt(hash.length);
			oo.write(hash);
		}
	}
}
