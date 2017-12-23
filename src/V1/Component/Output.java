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

	public Output() {
		amount = 0;
		question = null;
	}

	public Output(int amount, Question question) {
		this.amount = amount;
		this.question = question;
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


	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		amount = oi.readInt();
		question = (Question) oi.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(amount);
		oo.writeObject(question);
	}

	public String toString() {
		return "[amount: " + amount + ", question: " + question.toString() + "]";
	}
}
