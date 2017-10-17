package Main;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

public class Transaction implements Externalizable {
	// p117
	private static final long serialVersionUID = 19960331104L;
	
	private List<Input> in;
	private List<Output> out;
	private int version = 0;
	private int locktime = 0;
	static void init() {
		Log.log("Transaction init done.");
	}

	public Transaction() {
		in = new ArrayList<Input>();
		out = new ArrayList<Output>();
	}
	

	static Transaction checkTransaction(String json) {
		Transaction tx = new Transaction();
		try {
			Map map = (Map) JSON.decode(json);
			if (map.containsKey("type") && map.get("type").equals("transaction")) {
			} else {
				return null;
			}
			if (map.containsKey("in") && ((List) map.get("in")).size() > 0) {
				tx.in = (List) map.get("in");
			} else {
				return null;
			}
			if (map.containsKey("out") && ((List)map.get("out")).size() > 0) {
				tx.out = (List) map.get("out");
			} else {
				return null;
			}
		} catch (ClassCastException e) {
			// JSON.decode(json) --X--> Map
			// map.get("") --X--> List
			return null;
		}

		return tx;
	}
	
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
	
	static int getSumOut(List<Output> list) {
		int sum = 0;
		for(Iterator<Output> it = list.iterator(); it.hasNext(); ) {
			sum += it.next().amount;
		}
		return sum;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		try {
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
        in = Library.getListByByte(inByte);
        System.out.println("in:" + in);

        outSize = oi.readInt();
        System.out.println("outSize: " + outSize);
        outByte = new byte[outSize];
        oi.read(outByte, 0, outSize);
        out = Library.getListByByte(outByte);
        System.out.println("out:" + out);
        
        
//        System.out.println("out");
//        oi.read(outByte);
//        out = Library.getListByByte(outByte);
//        locktime = oi.readInt();
		}catch (Exception e) {
			e.printStackTrace();
		}
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
        oo.writeInt(locktime);
	}
	public String toString() {
		return ""+version;
	}
	
	void test() {
		in.add(new Input());
		out.add(new Output());
	}
}
class Input {
	int outBlockHeight;
	byte[] outTxHash;
	int outIndex;
	int answerSize;
	Answer answer;
	public Input() {
		outBlockHeight = 0;
		outTxHash = new byte[Constant.Transaction.TRANSACTION_HASH_LENGTH];
		outIndex = 0;
		answerSize = 1;
		answer = new Answer();
	}
}
class Output {
	int amount;
	int questionSize;
	Question question;
	public Output() {
		amount = 0;
		questionSize = 0;
		question = new Question();
	}
}

