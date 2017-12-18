package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;

public class Report implements Externalizable {
	private byte[] hash;
	private byte[] nonce;
	private byte[] result;
	private byte[] miner;

	public Report() {
		hash = null;
		nonce = null;
		result = null;
		miner = null;
	}

	public Report(byte[] hash, byte[] nonce, byte[] result, byte[] miner) {
		this.hash = hash;
		this.nonce = nonce;
		this.result = result;
		this.miner = miner;
	}
	public byte[] getHash() {
		return hash;
	}
	public byte[] getNonce() {
		return nonce;
	}
	public byte[] getResult() {
		return result;
	}
	public byte[] getMiner() {
		return miner;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int hashLength = oi.readInt();
		if (hashLength > Constant.Report.BYTE_MAX_HASH) {
			return;
		}
		hash = new byte[hashLength];
		oi.read(hash);
		
		int nonceLength = oi.readInt();
		if (nonceLength > Constant.Report.BYTE_MAX_NONCE) {
			return;
		}
		nonce = new byte[nonceLength];
		oi.read(nonce);
		
		int resultLength = oi.readInt();
		if (resultLength > Constant.Report.BYTE_MAX_HASH) {
			return;
		}
		result = new byte[resultLength];
		oi.read(result);

		int minerLength = oi.readInt();
		if (minerLength > Constant.Report.BYTE_MAX_MINER) {
			return;
		}
		miner = new byte[minerLength];
		oi.read(miner);

	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(hash.length);
		oo.write(hash);
		oo.writeInt(nonce.length);
		oo.write(nonce);
		oo.writeInt(result.length);
		oo.write(result);
		oo.writeInt(miner.length);
		oo.write(miner);
	}

	public String toString() {
		return "[hash: " + DatatypeConverter.printHexBinary(hash) + ", nonce: "
				+ DatatypeConverter.printHexBinary(nonce) + ", result: " + DatatypeConverter.printHexBinary(result) + ", miner: " + DatatypeConverter.printHexBinary(miner)
				+ "]";
	}
}
