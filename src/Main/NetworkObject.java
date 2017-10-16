package Main;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class NetworkObject implements Externalizable{
	private int type;
	private Object data;
	
	public NetworkObject(int type, Object object) {
		
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		type = oi.readInt();
		if (type == Constant.NetworkObject.BLOCK) {
			System.out.println("block read");
			int blockHeight = (int) oi.readObject();
			System.out.println("--blockHeight: " + blockHeight);
		} else if (type == Constant.NetworkObject.TX) {
	        System.out.println("version: " + oi.readObject());
	        System.out.println((String)oi.readObject());
		}

	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(type);
		oo.writeObject(data);
	}
}
