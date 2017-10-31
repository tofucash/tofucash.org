package Main;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class NetworkObject implements Externalizable {
	private int type;
	private Transaction tx;
	private Block block;

	public NetworkObject() {

	}

	public NetworkObject(int type, Object data) {
		this.type = type;
		if (type == Constant.NetworkObject.TX) {
			tx = (Transaction) data;
			block = null;
		} else if (type == Constant.NetworkObject.BLOCK) {
			block = (Block) data;
			tx = null;
		}
	}

	int getType() {
		return type;
	}

	Transaction getTx() {
		return tx;
	}

	Block getBlock() {
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
		System.out.println("networkObject read");
		type = (int) oi.readObject();
		System.out.println("readInt: " + type);
		if (type == Constant.NetworkObject.TX) {
			tx = (Transaction) oi.readObject();
		} else if (type == Constant.NetworkObject.BLOCK) {
			block = (Block) oi.readObject();
		}
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		System.out.println("networkObject write");
		System.out.println("type: " + type);
		oo.writeObject(type);
		if (type == Constant.NetworkObject.TX) {
			oo.writeObject(tx);
		} else if (type == Constant.NetworkObject.BLOCK) {
			oo.writeObject(block);
		}
	}
}
