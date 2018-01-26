package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Log;

public class Output implements Externalizable {
	private static final long serialVersionUID = 199603312010000L;
	private int amount;
	private Question question;
	private byte[] answerScriptHash;

	public Output() {
		amount = 0;
		question = null;
		answerScriptHash = null;
	}

	public Output(int amount, Question question) {
		this.amount = amount;
		this.question = question;
		this.answerScriptHash = new byte[1];
	}

	public int getAmount() {
		return amount;
	}
	public byte[] getReceiver() {
		return question.getReceiver();
	}
	public Question getQuestion() {
		return question;
	}
	public void setAnswerScriptHash(byte[] answerScriptHash) {
		this.answerScriptHash = answerScriptHash;
	}


	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		amount = oi.readInt();
		question = (Question) oi.readObject();
		int answerScriptHashLength = oi.readInt();
		if(answerScriptHashLength > Constant.Output.BYTE_ANSWER_SCRIPT_HASH) {
			return;
		}
		answerScriptHash = new byte[answerScriptHashLength];
		oi.read(answerScriptHash);
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(amount);
		oo.writeObject(question);
		oo.writeInt(answerScriptHash.length);
		oo.write(answerScriptHash);
	}

	public String toString() {
		return "[amount: " + amount + ", question: " + question.toString() + ", answerScriptHash: "+DatatypeConverter.printHexBinary(answerScriptHash)+"]";
	}
}
