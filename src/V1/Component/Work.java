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
	private byte[] subTarget;
	private byte[] fAddress;

	public Work() {
		hash = new byte[] {0x0};
		target = new byte[] {0x0};
	}
	public Work(byte[] hash, byte[] target, byte[] subTarget, byte[] fAddress) {
		this.hash = hash;
		this.target = target;
		this.subTarget = subTarget;
		this.fAddress = fAddress;
	}

	public byte[] getHash() {
		return hash;
	}
	public byte[] getTarget() {
		return target;
	}
	public byte[] getSubTarget() {
		return subTarget;
	}
	public byte[] getFAddress() {
		return fAddress;
	}
	public void setFAddress(byte[] fAddress) {
		this.fAddress = fAddress;
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

		int subTargetLength = oi.readInt();
		if (subTargetLength > Constant.Work.BYTE_MAX_HASH) {
			return;
		}
		subTarget = new byte[subTargetLength];
		oi.read(subTarget);

		int fAddressLength = oi.readInt();
		if (fAddressLength > Constant.Address.BYTE_ADDRESS) {
			return;
		}
		fAddress = new byte[fAddressLength];
		oi.read(fAddress);
}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(hash.length);
		oo.write(hash);
		oo.writeInt(target.length);
		oo.write(target);
		oo.writeInt(subTarget.length);
		oo.write(subTarget);
		oo.writeInt(fAddress.length);
		oo.write(fAddress);
	}
	
	public String toString() {
		return "[hash: " + DatatypeConverter.printHexBinary(hash) + ", target: " + DatatypeConverter.printHexBinary(target) + ", subTarget: " + DatatypeConverter.printHexBinary(subTarget) + ", fAddress: " + DatatypeConverter.printHexBinary(fAddress)+"]";
	}

}
