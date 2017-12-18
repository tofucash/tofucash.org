package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;

public class Work implements Externalizable {
	private static final long serialVersionUID = 199603310801000L;
	private byte[] hash;
	private byte[] target;

	public Work() {
		hash = new byte[] {0x0};
		target = new byte[] {0x0};
	}
	public Work(byte[] hash, byte[] target) {
		this.hash = hash;
		this.target = target;
	}

	public byte[] getHash() {
		return hash;
	}
	public byte[] getTarget() {
		return target;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int hashLength = oi.readInt();
		if (hashLength > Constant.Work.BYTE_MAX_HASH) {
			return;
		}
		hash = new byte[hashLength];
		oi.read(hash);

		int targetLength = oi.readInt();
		if (targetLength > Constant.Work.BYTE_MAX_HASH) {
			return;
		}
		target = new byte[targetLength];
		oi.read(target);
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(hash.length);
		oo.write(hash);
		oo.writeInt(target.length);
		oo.write(target);
	}
	
	public String toString() {
		return "[hash: " + DatatypeConverter.printHexBinary(hash) + ", target: " + DatatypeConverter.printHexBinary(target)+"]";
	}

}
