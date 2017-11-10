package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Output implements Externalizable {
	private int amount;
	private int questionSize;
	private Question question;

	public Output() {
		amount = 0;
		questionSize = 0;
		question = new Question();
	}

	public Output(int amount, int questionSize, Question question) {
		this.amount = amount;
		this.questionSize = questionSize;
		this.question = question;
	}

	public Question getQuestion() {
		return question;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		amount = oi.readInt();
		questionSize = oi.readInt();
		question = (Question) oi.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(amount);
		oo.writeInt(questionSize);
		oo.writeObject(question);
	}

	public String toString() {
		return "[amount: " + amount + ", questionSize: " + questionSize + ", question: " + question.toString() + "]";
	}
}
