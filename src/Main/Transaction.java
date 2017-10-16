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
	private static final int TRANSACTION_HASH_LENGTH = 512;

	private static final long serialVersionUID = 8531245739641223373L;
	
	private List<Input> in;
	private List<Output> out;
	private Question q;
	private int version = 0;
	static void init() {
		Log.log("Transaction init done.");
	}

	public Transaction() {
		in = new ArrayList<Input>();
		out = new ArrayList<Output>();
		q = new Question();
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
			if (map.containsKey("question") && ((List)map.get("question")).size() > 0) {
				tx.q = (Question) map.get("question");
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
			if(Question.isCorrect(tx.q, input.answer)) {
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
        System.out.println("version: " + oi.readObject());
        System.out.println((String)oi.readObject());
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
        oo.writeObject(Constant.Transaction.VERSION);
        oo.writeObject("XX hidden XX");
	}
	public String toString() {
		return ""+version;
	}
}
class Input {
	int outBlockHeight;
	byte[] outTxHash;
	int outIndex;
	int answerSize;
	Answer answer;
}
class Output {
	int amount;
	int scriptSize;
	byte[] script;
}
