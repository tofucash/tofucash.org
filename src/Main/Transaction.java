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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

public class Transaction implements Externalizable {
	private static final long serialVersionUID = 19960331104L;
	
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
	
		
	private static int getSumOut(List<Output> list) {
		int sum = 0;
		for(Iterator<Output> it = list.iterator(); it.hasNext(); ) {
			sum += it.next().amount;
		}
		return sum;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        byte[] inByte = null;
        byte[] outByte = null;
        int inSize = 0, outSize = 0;
		System.out.println("tx read");
		version = oi.readInt();
        System.out.println("version: " + version);
        
        inSize = oi.readInt();
        System.out.println("inSize: " + inSize);
        inByte = new byte[inSize];
        oi.read(inByte, 0, inSize);
        in = (Input[]) getObjectArray(inByte);
        System.out.println("ina:" + Arrays.toString(in));

        outSize = oi.readInt();
        System.out.println("outSize: " + outSize);
        outByte = new byte[outSize];
        oi.read(outByte, 0, outSize);
        out = (Output[]) getObjectArray(outByte);
        System.out.println("outa:" + Arrays.toString(out));
        
        lockTime = oi.readInt();
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		System.out.println("tx write");
        oo.writeInt(Constant.Transaction.VERSION);
        byte[] inByte = Library.getByteObject(in);
        byte[] outByte = Library.getByteObject(out);
        oo.writeInt(inByte.length);
        oo.write(inByte);
        oo.writeInt(outByte.length);
        oo.write(outByte);
        oo.writeInt(lockTime);
	}
		
	private static Object[] getObjectArray(byte[] objByte)
	{
		Object[] obj = new Object[Constant.Transaction.MAX_INPUT_OUTPUT];
		try {
	      ByteArrayInputStream byteis = new ByteArrayInputStream(objByte);
	      ObjectInputStream objis = new ObjectInputStream(byteis);
	      obj = (Object[])objis.readObject();
	      byteis.close();
	      objis.close();
	  } catch (Exception e) {
	      e.printStackTrace();
	  }
	  return obj;
	}

	public String toString() {
		return "[version: "+version + ", lockTime: " + lockTime + ", Input[]: " + in + ", Output[]: " + out + "]";
	}
	
	void setTestData() {
		in[0] = new Input();
		out[0] = new Output();
	}
}
class Input implements Externalizable{
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
		outBlockHeight = (int)oi.read();
		outTxHash = (byte[])oi.readObject();
		outIndex = (int)oi.read();
		answerSize = (int)oi.read();
		answer = (Answer)oi.readObject();
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
		return "[outBlockHeight: "+outBlockHeight+", outTxHash: "+new String(outTxHash).substring(0, 7)+"..., outIndex: "+outIndex+", answerSize: "+answerSize+", answer: "+answer.toString()+"]";
	}
}
class Output implements Externalizable{
	int amount;
	int questionSize;
	Question question;
	public Output() {
		amount = 0;
		questionSize = 0;
		question = new Question();
	}
	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		amount = (int)oi.read();
		questionSize = (int)oi.read();
		question = (Question)oi.readObject();
	}
	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.write(amount);
		oo.write(questionSize);
		oo.writeObject(question);
	}
	
	public String toString() {
		return "[amount: "+amount+", questionSize: "+questionSize+", question: "+question.toString()+"]";
	}
}


//static Transaction checkTransaction(String json) {
//Transaction tx = new Transaction();
//try {
//	Map map = (Map) JSON.decode(json);
//	if (map.containsKey("type") && map.get("type").equals("transaction")) {
//	} else {
//		return null;
//	}
//	if (map.containsKey("in") && ((List) map.get("in")).size() > 0) {
//		tx.in = (List) map.get("in");
//	} else {
//		return null;
//	}
//	if (map.containsKey("out") && ((List)map.get("out")).size() > 0) {
//		tx.out = (List) map.get("out");
//	} else {
//		return null;
//	}
//} catch (ClassCastException e) {
//	// JSON.decode(json) --X--> Map
//	// map.get("") --X--> List
//	return null;
//}
//
//return tx;
//}
//
//static int checkInList(Transaction tx) {
//int sum = 0;
//Input input;
//Output output;
//Transaction outTx;
//for(Iterator<Input> it = tx.in.iterator(); it.hasNext(); ) {
//	input = it.next();
//	outTx = Blockchain.getOutTransaction(input.outBlockHeight, input.outTxHash);
//	output = outTx.out.get(input.outIndex);
//	if(Script.resolve(output.question, input.answer)) {
//		sum += output.amount; 
//	} else {
//		it.remove();
//	}
//	if(sum >= getSumOut(tx.out)) {
//		return sum;
//	}
//}
//return Constant.Transaction.OUTPUT_IS_NOT_ENOUGH; 
//}
//static int checkOutList(Transaction tx) {
//int sum = 0;
//
//return sum; 
//}
