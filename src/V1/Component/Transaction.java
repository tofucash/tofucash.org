package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;

public class Transaction implements Externalizable {
	private static final long serialVersionUID = 199603311040000L;

	private int version = 0;
	private int lockTime = 0;
	private Input[] in;
	private Output[] out;
	private byte[] signature; 

	public static void init() {
		Log.log("Transaction init done.");
	}

	public Transaction() {
		in = new Input[Constant.Transaction.MAX_INPUT_OUTPUT];
		out = new Output[Constant.Transaction.MAX_INPUT_OUTPUT];
	}

	public Transaction(Input[] in, Output[] out, int version, int lockTime, byte[] signature) {
		this.in = in;
		this.out = out;
		this.version = version;
		this.lockTime = lockTime;
		this.signature = signature;
	}
	public Transaction(Input[] in, Output[] out, int version, int lockTime, KeyPair keyPair) throws Exception {
		this.in = in;
		this.out = out;
		this.version = version;
		this.lockTime = lockTime;
		this.signature = keyPair.getPublic().getEncoded();
		this.signature = Crypto.sign(keyPair.getPrivate(), keyPair.getPublic(), ByteUtil.getByteObject(this));
	}

	public void removeNull() {
		List<Input> inListAsList = new ArrayList<Input>(Arrays.asList(in));
		inListAsList.removeAll(Collections.singleton(null));
		in = inListAsList.toArray(new Input[inListAsList.size()]);
		List<Output> outListAsList = new ArrayList<Output>(Arrays.asList(out));
		outListAsList.removeAll(Collections.singleton(null));
		out = outListAsList.toArray(new Output[outListAsList.size()]);
	}

	public int getVersion() {
		return version;
	}

	public int getLockTime() {
		return lockTime;
	}
	public Input[] getIn() {
		return in;
	}

	public void updateIn(Input[] in) {
		this.in = in;
	}

	public Output[] getOut() {
		return out;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int inCnt = 0, outCnt = 0;

		version = oi.readInt();
		lockTime = oi.readInt();
		int sigLength = oi.readInt();
		if(sigLength > Constant.Transaction.BYTE_MAX_SIGNATURE) {
			return;
		}
		signature = new byte[sigLength];
		oi.read(signature);
		inCnt = oi.readInt();
		if (inCnt >= Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		in = (Input[]) oi.readObject();
		outCnt = oi.readInt();
		if (outCnt >= Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		out = (Output[]) oi.readObject();
		removeNull();
		Log.log("in list :" + Arrays.toString(in), Constant.Log.TEMPORARY);
		Log.log("out list:" + Arrays.toString(out), Constant.Log.TEMPORARY);
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(version);
		oo.writeInt(lockTime);
		oo.writeInt(signature.length);
		oo.write(signature);
		oo.writeInt(in.length);
		oo.writeObject(in);
		oo.writeInt(out.length);
		oo.writeObject(out);
	}

	public String toString() {
		return "[version: " + version + ", lockTime: " + lockTime + ", signature: "+DatatypeConverter.printHexBinary(signature)+", Input[]: " + Arrays.asList(in) + ", Output[]: "
				+ Arrays.asList(out) + "]";
	}

}
