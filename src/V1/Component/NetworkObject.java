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

import V1.Library.Constant;

public class NetworkObject implements Externalizable {
	private static final long serialVersionUID = 199603311030000L;
	private int type;
	private Transaction tx;
	private Block block;

	public NetworkObject() {

	}

	public NetworkObject(int type, Object data) {
		this.type = type;
		if (type == Constant.NetworkObject.TX) {
			tx = (Transaction) data;
			tx.removeNull();
			block = null;
		} else if (type == Constant.NetworkObject.BLOCK) {
			block = (Block) data;
			tx = null;
			block.removeNull();
			Iterator<Transaction> it;
			int i;
			Transaction[] txList = block.getTxList(); 
			for(i = 0; i < txList.length; i++) {
				txList[i].removeNull();
			}
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

	public String toString() {
		if (type == Constant.NetworkObject.TX) {
			return "[type: " + type + ", tx:" + tx.toString() + "]";
		} else if (type == Constant.NetworkObject.BLOCK) {
			return "[type: " + type + ", block: " + block.toString() + "]";
		} else {
			return "[type: unknown, tx: "+tx.toString()+", block: "+block.toString()+"]";
		}
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		type = (int) oi.readObject();
		if (type == Constant.NetworkObject.TX) {
			tx = (Transaction) oi.readObject();
		} else if (type == Constant.NetworkObject.BLOCK) {
			block = (Block) oi.readObject();
		}
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(type);
		if (type == Constant.NetworkObject.TX) {
			oo.writeObject(tx);
		} else if (type == Constant.NetworkObject.BLOCK) {
			oo.writeObject(block);
		}
	}
}
