package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Constant.Transaction;

public class Input implements Externalizable {
	private static final long serialVersionUID = 199603312040000L;
	private byte[] outHash;
	private byte[] receiver;
	private Answer answer;

	public Input() {
		outHash = null;
		receiver = null;
		answer = null;
	}

	public Input(byte[] outTxHash, byte[] receiver, Answer answer) {
		this.outHash = outTxHash;
		this.receiver = receiver;
		this.answer = answer;
	}

	public byte[] getOutHash() {
		return outHash;
	}
	public byte[] getReceiver() {
		return receiver;
	}
	public Answer getAnswer() {
		return answer;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int outHashLength = oi.readInt();
		if(outHashLength > Constant.Transaction.BYTE_OUT_HASH) {
			return;
		}
		outHash = new byte[outHashLength];
		oi.read(outHash);
		int receiverLength = oi.readInt();
		if(receiverLength > Constant.Address.BYTE_ADDRESS) {
			return;
		}
		receiver = new byte[receiverLength];
		oi.read(receiver);
		answer = (Answer) oi.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(outHash.length);
		oo.write(outHash);
		oo.writeInt(receiver.length);
		oo.write(receiver);
		oo.writeObject(answer);
	}

	public String toString() {
		return "[outTxHash: " + DatatypeConverter.printHexBinary(outHash) + ", answer: " + answer.toString() + "]";
	}
}
