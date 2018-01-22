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
	private int amount;
	private Answer answer;

	public Input() {
		outHash = null;
		answer = null;
		amount = -1;
	}

	public Input(byte[] outHash, Answer answer, int amount) {
		this.outHash = outHash;
		this.answer = answer;
		this.amount = amount;
	}

	public byte[] getOutHash() {
		return outHash;
	}
	public byte[] getReceiver() {
		return answer.getReceiver();
	}
	public Answer getAnswer() {
		return answer;
	}
	public int getAmount() {
		return amount;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int outHashLength = oi.readInt();
		if(outHashLength > Constant.Transaction.BYTE_OUT_HASH) {
			return;
		}
		outHash = new byte[outHashLength];
		oi.read(outHash);
		answer = (Answer) oi.readObject();
		amount = oi.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(outHash.length);
		oo.write(outHash);
		oo.writeObject(answer);
		oo.writeInt(amount);
	}

	public String toString() {
		return "[outHash: " + DatatypeConverter.printHexBinary(outHash) + ", answer: " + answer.toString() + ", amount: " + amount+ "]";
	}
}
