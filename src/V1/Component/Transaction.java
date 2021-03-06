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
	private byte[] publicKey;

	public static void init() {
		Log.log("Transaction init done.");
	}

	public Transaction() {
		in = new Input[Constant.Transaction.MAX_INPUT_OUTPUT];
		out = new Output[Constant.Transaction.MAX_INPUT_OUTPUT];
	}

	public Transaction(Input[] in, Output[] out, int version, int lockTime, byte[] signature, byte[] publicKey) {
		this.in = in;
		this.out = out;
		this.version = version;
		this.lockTime = lockTime;
		this.signature = signature;
		this.publicKey = publicKey;
	}
	public Transaction(Input[] in, Output[] out, int version, int lockTime, KeyPair keyPair) throws Exception {
		this.in = in;
		this.out = out;
		this.version = version;
		this.lockTime = lockTime;
		this.signature = keyPair.getPublic().getEncoded();
		this.publicKey = keyPair.getPublic().getEncoded();
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
	public byte[] getPublicKey() {
		return publicKey;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int inCnt = 0, outCnt = 0;

		version = oi.readInt();
		lockTime = oi.readInt();
		inCnt = oi.readInt();
		if (inCnt > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		in = new Input[inCnt];
		for(int i = 0; i < inCnt; i++) {
			in[i] = (Input) oi.readObject();
		}
		outCnt = oi.readInt();
		if (outCnt > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		out = new Output[outCnt];
		for(int i = 0; i < outCnt; i++) {
			out[i] = (Output) oi.readObject();			
		}
		int sigLength = oi.readInt();
		if(sigLength > Constant.Transaction.BYTE_MAX_SIGNATURE) {
			return;
		}
		signature = new byte[sigLength];
		oi.read(signature);
		int publicKeyLength = oi.readInt();
		if(publicKeyLength > Constant.Address.BYTE_PUBLIC_KEY) {
			return;
		}
		publicKey = new byte[publicKeyLength];
		oi.read(publicKey);
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(version);
		oo.writeInt(lockTime);
		oo.writeInt(in.length);
		for(int i = 0; i < in.length; i++) {
			oo.writeObject(in[i]);
		}
		oo.writeInt(out.length);
		for(int i = 0; i < out.length; i++) {
			oo.writeObject(out[i]);
		}
		oo.writeInt(signature.length);
		oo.write(signature);
		oo.writeInt(publicKey.length);
		oo.write(publicKey);
	}

	public String toString() {
		return "[version: " + version + ", lockTime: " + lockTime + ", signature: "+DatatypeConverter.printHexBinary(signature) + ", publicKey: "+DatatypeConverter.printHexBinary(publicKey)+", Input[]: " + Arrays.asList(in) + ", Output[]: "
				+ Arrays.asList(out) + "]";
	}

}
