package Main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import net.arnx.jsonic.JSON;

public class Transaction implements Externalizable {
	private static final long serialVersionUID = 199603311040000L;

	private Input[] in;
	private Output[] out;
	private int version = 0;
	private int lockTime = 0;

	static void init() {
		Log.log("Transaction init done.");
	}

	public Transaction() {
		in = new Input[Constant.Transaction.MAX_INPUT_OUTPUT];
		out = new Output[Constant.Transaction.MAX_INPUT_OUTPUT];
	}

	public Transaction(Input[] in, Output[] out, int version, int lockTime) {
		this.in = in;
		this.out = out;
		this.version = version;
		this.lockTime = lockTime;
	}

	void removeNull() {
		List<Input> inListAsList = new ArrayList<Input>(Arrays.asList(in));
		inListAsList.removeAll(Collections.singleton(null));
		in = inListAsList.toArray(new Input[inListAsList.size()]);
		List<Output> outListAsList = new ArrayList<Output>(Arrays.asList(out));
		outListAsList.removeAll(Collections.singleton(null));
		out = outListAsList.toArray(new Output[outListAsList.size()]);
	}

	Input[] getIn() {
		return in;
	}

	void updateIn(Input[] in) {
		this.in = in;
	}

	Output[] getOut() {
		return out;
	}

	int getVersion() {
		return version;
	}

	int getLockTime() {
		return lockTime;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		byte[] inByte = null;
		byte[] outByte = null;
		int inCnt = 0, outCnt = 0, inSize = 0, outSize = 0;
		int i;

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
		System.out.println("in list :" + Arrays.toString(in));
		System.out.println("out list:" + Arrays.toString(out));

		lockTime = oi.readInt();
		version = oi.readInt();
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		int i;
		oo.writeInt(in.length);
		oo.writeObject(in);
		oo.writeInt(out.length);
		oo.writeObject(out);
		oo.writeInt(lockTime);
		oo.writeInt(version);
	}

	private static Object convertByteToObject(byte[] objByte, int cnt) {
		Object obj = null;
		try {
			ByteArrayInputStream byteis = new ByteArrayInputStream(objByte);
			ObjectInputStream objis = new ObjectInputStream(byteis);
			obj = objis.readObject();
			byteis.close();
			objis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		;
		return obj;
	}

	public String toString() {
		return "[version: " + version + ", lockTime: " + lockTime + ", Input[]: " + Arrays.asList(in) + ", Output[]: "
				+ Arrays.asList(out) + "]";
	}

	void setTestData() {
		in[0] = new Input(1, new byte[] { 0x01, 0x02, 0x03 }, 0, 1,
				new Answer(new byte[] { Constant.Script.OPCode.PUSH32, 100, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
						15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 }));
		out[0] = new Output(1, 9, new Question(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 5, 6, 7, 8, 9, 10, 11, 12, 13,
				14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 }));
		version = 0xffff;
		lockTime = 100;
	}
}

class Input implements Externalizable {
	private int outBlockHeight;
	private byte[] outTxHash;
	private int outIndex;
	private int answerSize;
	private Answer answer;

	public Input() {
		outBlockHeight = 0;
		outTxHash = new byte[Constant.Transaction.BYTE_TX_HASH];
		outIndex = 0;
		answerSize = 1;
		answer = new Answer();
	}

	Input(int outBlockHeight, byte[] outTxHash, int outIndex, int answerSize, Answer answer) {
		this.outBlockHeight = outBlockHeight;
		this.outTxHash = outTxHash;
		this.outIndex = outIndex;
		this.answerSize = answerSize;
		this.answer = answer;
	}

	int getOutBlockHeight() {
		return outBlockHeight;
	}

	byte[] getOutTxHash() {
		return outTxHash;
	}

	int getOutIndex() {
		return outIndex;
	}

	int getAnswerSize() {
		return answerSize;
	}

	Answer getAnswer() {
		return answer;
	}

	void updateAnswer(Answer answer) {
		this.answer = answer;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		outBlockHeight = (int) oi.read();
		outTxHash = (byte[]) oi.readObject();
		outIndex = (int) oi.read();
		answerSize = (int) oi.read();
		answer = (Answer) oi.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.write(outBlockHeight);
		oo.writeObject(outTxHash);
		oo.write(outIndex);
		oo.write(answerSize);
		oo.writeObject(answer);
	}

	public String toString() {
		return "[outBlockHeight: " + outBlockHeight + ", outTxHash: " + DatatypeConverter.printHexBinary(outTxHash)
				+ ", outIndex: " + outIndex + ", answerSize: " + answerSize + ", answer: " + answer.toString() + "]";
	}
}

class Output implements Externalizable {
	private int amount;
	private int questionSize;
	private Question question;

	public Output() {
		amount = 0;
		questionSize = 0;
		question = new Question();
	}

	Output(int amount, int questionSize, Question question) {
		this.amount = amount;
		this.questionSize = questionSize;
		this.question = question;
	}

	Question getQuestion() {
		return question;
	}

	int getAmount() {
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
