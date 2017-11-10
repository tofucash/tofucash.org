package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Constant.Transaction;

public class Input implements Externalizable {
	private byte[] outTxHash;
	private int outIndex;
	private int answerSize;
	private Answer answer;

	public Input() {
		outTxHash = new byte[Constant.Transaction.BYTE_TX_HASH];
		outIndex = 0;
		answerSize = 1;
		answer = new Answer();
	}

	public Input(byte[] outTxHash, int outIndex, int answerSize, Answer answer) {
		this.outTxHash = outTxHash;
		this.outIndex = outIndex;
		this.answerSize = answerSize;
		this.answer = answer;
	}

	public byte[] getOutTxHash() {
		return outTxHash;
	}

	public int getOutIndex() {
		return outIndex;
	}

	public int getAnswerSize() {
		return answerSize;
	}

	public Answer getAnswer() {
		return answer;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		outTxHash = (byte[]) oi.readObject();
		outIndex = (int) oi.read();
		answerSize = (int) oi.read();
		answer = (Answer) oi.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(outTxHash);
		oo.write(outIndex);
		oo.write(answerSize);
		oo.writeObject(answer);
	}

	public String toString() {
		return "[outTxHash: " + DatatypeConverter.printHexBinary(outTxHash)
				+ ", outIndex: " + outIndex + ", answerSize: " + answerSize + ", answer: " + answer.toString() + "]";
	}
}
