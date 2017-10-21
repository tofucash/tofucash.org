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
	// p117
	private static final long serialVersionUID = 19960331104L;
	
	private List<Input> in;
	private List<Output> out;
	private Input[] ina;
	private Output[] outa;
	private int version = 0;
	private int locktime = 0;
	static void init() {
		Log.log("Transaction init done.");
	}

	public Transaction() {
		in = new ArrayList<Input>();
		out = new ArrayList<Output>();
		ina = new Input[Constant.Transaction.MAX_INPUT_OUTPUT];
		outa = new Output[Constant.Transaction.MAX_INPUT_OUTPUT];
	}
	

//	static Transaction checkTransaction(String json) {
//		Transaction tx = new Transaction();
//		try {
//			Map map = (Map) JSON.decode(json);
//			if (map.containsKey("type") && map.get("type").equals("transaction")) {
//			} else {
//				return null;
//			}
//			if (map.containsKey("in") && ((List) map.get("in")).size() > 0) {
//				tx.in = (List) map.get("in");
//			} else {
//				return null;
//			}
//			if (map.containsKey("out") && ((List)map.get("out")).size() > 0) {
//				tx.out = (List) map.get("out");
//			} else {
//				return null;
//			}
//		} catch (ClassCastException e) {
//			// JSON.decode(json) --X--> Map
//			// map.get("") --X--> List
//			return null;
//		}
//
//		return tx;
//	}
//	
	static int checkInList(Transaction tx) {
		int sum = 0;
		Input input;
		Output output;
		Transaction outTx;
		for(Iterator<Input> it = tx.in.iterator(); it.hasNext(); ) {
			input = it.next();
			outTx = Blockchain.getOutTransaction(input.outBlockHeight, input.outTxHash);
			output = outTx.out.get(input.outIndex);
			if(Question.isCorrect(output.question, input.answer)) {
				sum += output.amount; 
			} else {
				it.remove();
			}
			if(sum >= getSumOut(tx.out)) {
				return sum;
			}
		}
		return Constant.Transaction.OUTPUT_IS_NOT_ENOUGH; 
	}
	static int checkOutList(Transaction tx) {
		int sum = 0;
		
		return sum; 
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
        ina = (Input[]) getObjectArray(inByte);
        System.out.println("ina:" + Arrays.toString(ina));

        outSize = oi.readInt();
        System.out.println("outSize: " + outSize);
        outByte = new byte[outSize];
        oi.read(outByte, 0, outSize);
        outa = (Output[]) getObjectArray(outByte);
        System.out.println("outa:" + Arrays.toString(outa));
        
        locktime = oi.readInt();
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		System.out.println("tx write");
        oo.writeInt(Constant.Transaction.VERSION);
        byte[] inByte = Library.getByteObject(ina);
        byte[] outByte = Library.getByteObject(outa);
        oo.writeInt(inByte.length);
        oo.write(inByte);
        oo.writeInt(outByte.length);
        oo.write(outByte);
        oo.writeInt(locktime);
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
		return ""+version;
	}
	
	void test() {
		in.add(new Input());
		out.add(new Output());
		ina[0] = new Input();
		outa[0] = new Output();
	}
}
class Input implements Externalizable{
	int outBlockHeight;
	byte[] outTxHash;
	int outIndex;
	int answerSize;
	Answer answer;
	public Input() {
		outBlockHeight = 0;
		outTxHash = new byte[Constant.Transaction.BYTE_TX_HASH];
		outIndex = 0;
		answerSize = 1;
		answer = new Answer();
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

