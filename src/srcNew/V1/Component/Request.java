package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Log;
import V1.Library.TofuError;

public class Request implements Externalizable {
	private static final long serialVersionUID = 199603310103000L;
	public int type;
	public int version;
	public int lockTime;
	public int[] amountFrom;
	public int[] amountTo;
	public String[] addrFrom;
	public String[] addrTo;
	public String[][] outHash;
	public String[][] answerScript;
	public String[][] questionScript;
	public String signature;
	public String publicKey;
	

	public Request() {
		type = -1;
		version = -1;
		lockTime = -1;
		amountFrom = null;
		amountTo = null;
		addrFrom = null;
		addrTo = null;
		outHash = null;
		answerScript = null;
		questionScript = null;
		signature = null;
		publicKey = null;
	}

	public Request(int type, int version, int lockTime, int[] amountFrom, int[] amountTo, String[] addrFrom, String[] addrTo, String[][] outHash,
			String[][] answerScript, String[][] questionScript, String signature, String publicKey) {
		this.type = type;
		this.version = version;
		this.lockTime = lockTime;
		this.amountFrom = amountFrom;
		this.amountTo = amountTo;
		this.addrFrom = addrFrom;
		this.addrTo = addrTo;
		this.outHash = outHash;
		this.answerScript = answerScript;
		this.questionScript = questionScript;
		this.signature = signature;
		this.publicKey = publicKey;
	}
//	public Request(Transaction tx) {
//		this.type = Constant.Request.TYPE_SEND_TOFU;
//		this.version = tx.getVersion();
//		this.lockTime = tx.getLockTime();
//		this.amountFrom = new int[tx.getIn().length];
//		this.amountTo = new int[tx.getOut().length];
//		this.addrFrom = new String[tx.getIn().length];
//		this.addrTo = new String[tx.getOut().length];
//		this.outHash = new String[tx.getIn().length][Constant.Transaction.BYTE_OUT_HASH];
//		this.answerScript = new String[tx.getIn().length];
//		this.questionScript = new String[tx.getOut().length];
//		this.signature = DatatypeConverter.printHexBinary(tx.getSignature());
//		this.publicKey = DatatypeConverter.printHexBinary(tx.getPublicKey());
//		Input[] in = tx.getIn();
//		Output[] out = tx.getOut();
//		for(int i = 0; i < in.length; i++) {
//			amountFrom[i] = in[i].getAmount();
//			addrFrom[i] = DatatypeConverter.printHexBinary(in[i].getReceiver());
//			answerScript[i] = DatatypeConverter.printHexBinary(in[i].getAnswer().getScript());
//		}
//		for(int i = 0; i < out.length; i++) {
//			amountTo[i] = out[i].getAmount();
//			addrTo[i] = DatatypeConverter.printHexBinary(out[i].getReceiver());
//			questionScript[i] = DatatypeConverter.printHexBinary(out[i].getQuestion().getScript());
//		}
//	}

	public int getType() {
		return type;
	}
	public int getVersion() {
		return version;
	}
	public int getLockTime() {
		return lockTime;
	}

	public int[] getAmountFrom() {
		return amountFrom;
	}

	public int[] getAmountTo() {
		return amountTo;
	}

	public String[] getAddrFrom() {
		return addrFrom;
	}

	public String[] getAddrTo() {
		return addrTo;
	}

	public String[][] getOutHash() {
		return outHash;
	}

	public String[][] getAnswerScript() {
		return answerScript;
	}

	public String[][] getQuestionScript() {
		return questionScript;
	}

	public String getSignature() {
		return signature;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setSignature(String str) {
		signature = str;
	}

	public String toString() {
		if(type == Constant.Request.TYPE_SEND_TOFU) {
			return "[type: " + type + ", amount: " + Arrays.toString(amountFrom) + ", amountTo: " + Arrays.toString(amountTo)
			+ ", addrFrom: " + Arrays.toString(addrFrom) + ", addrTo: "
			+ Arrays.toString(addrTo) + ", outHash: "
			+ Arrays.toString(outHash) + ", answerScript: "
			+ Arrays.toString(answerScript) + ", questionScript: "
			+ Arrays.toString(questionScript) + ", signature: "
					+ signature + ", publicKey: "
							+ publicKey + "]";
		} else if(type == Constant.Request.TYPE_CHECK_BALANCE) {
			return "[type: "+ type+ ", addrFrom: "+ Arrays.toString(addrFrom)+"]";
		} else {
			throw new TofuError.UnimplementedError("Unknown Request Type");
		}
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		type = oi.readInt();
		version = oi.readInt();
		lockTime = oi.readInt();
		int amountFromLength = oi.readInt();
		if (amountFromLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		amountFrom = new int[amountFromLength];
		for (int i = 0; i < amountFromLength; i++) {
			amountFrom[i] = oi.readInt();
		}

		int amountToLength = oi.readInt();
		if (amountToLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		amountTo = new int[amountToLength];
		for (int i = 0; i < amountToLength; i++) {
			amountTo[i] = oi.readInt();
		}

		int addrFromListLength = oi.readInt();
		if (addrFromListLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		addrFrom = new String[addrFromListLength];
		for (int i = 0; i < addrFromListLength; i++) {
			int addrFromLength = oi.readInt();
			if (addrFromLength > Constant.Address.BYTE_ADDRESS*2) {
				return;
			}
			addrFrom[i] = oi.readLine();
		}

		int addrToListLength = oi.readInt();
		if (addrToListLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		addrTo = new String[addrToListLength];
		for (int i = 0; i < addrToListLength; i++) {
			int addrToLength = oi.readInt();
			if (addrToLength > Constant.Address.BYTE_ADDRESS*2) {
				return;
			}
			addrTo[i] = oi.readLine();
		}

		int outHashListLength = oi.readInt();
		if (outHashListLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		outHash = new String[outHashListLength][];
		for (int i = 0; i < outHashListLength; i++) {
			int outHashListLength2 = oi.readInt();
			if (outHashListLength2 > Constant.Transaction.MAX_INPUT_OUTPUT) {
				return;
			}
			outHash[i] = new String[outHashListLength2];
			for(int j = 0; j < outHashListLength2; j++) {
				int outHashLength = oi.readInt();
				if (outHashLength > Constant.Transaction.BYTE_OUT_HASH*2) {
					return;
				}
				outHash[i][j] = oi.readLine();
			}
		}

		int answerScriptListLength = oi.readInt();
		if (answerScriptListLength > Constant.Block.MAX_TX) {
			return;
		}
		answerScript = new String[answerScriptListLength][];
		for (int i = 0; i < answerScriptListLength; i++) {
			int answerScriptListLength2 = oi.readInt();
			if (answerScriptListLength2 > Constant.Transaction.MAX_INPUT_OUTPUT) {
				return;
			}
			answerScript[i] = new String[answerScriptListLength2];
			for(int j = 0; j < answerScriptListLength2; j++) {
				int answerScriptLength = oi.readInt();
				if (answerScriptLength > Constant.Script.BYTE_MAX_ANSWER*2) {
					return;
				}
				answerScript[i][j] = oi.readLine();
			}
		}

		int questionScriptListLength = oi.readInt();
		if (questionScriptListLength > Constant.Block.MAX_TX) {
			return;
		}
		questionScript = new String[questionScriptListLength][];
		for (int i = 0; i < questionScriptListLength; i++) {
			int questionScriptListLength2 = oi.readInt();
			if (questionScriptListLength2 > Constant.Transaction.MAX_INPUT_OUTPUT) {
				return;
			}
			questionScript[i] = new String[questionScriptListLength2];
			for(int j = 0; j < questionScriptListLength2; j++) {
				int questionScriptLength = oi.readInt();
				if (questionScriptLength > Constant.Script.BYTE_MAX_QUESTION*2) {
					return;
				}
				questionScript[i][j] = oi.readLine();
			}
		}

		
		
		int signatureLength = oi.readInt();
		if (signatureLength > Constant.Transaction.BYTE_MAX_SIGNATURE*2) {
			return;
		}
		signature = oi.readLine();

		int publicKeyLength = oi.readInt();
		if (publicKeyLength > Constant.Address.BYTE_PUBLIC_KEY*2) {
			return;
		}
		publicKey = oi.readLine();

	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(type);
		oo.writeInt(version);
		oo.writeInt(lockTime);
		oo.writeInt(amountFrom.length);
		for(int i = 0; i < amountFrom.length; i++) {
			oo.writeInt(amountFrom[i]);
		}
		oo.writeInt(amountTo.length);
		for(int i = 0; i < amountTo.length; i++) {
			oo.writeInt(amountTo[i]);
		}
		oo.writeInt(addrFrom.length);
		for(int i = 0; i < addrFrom.length; i++) {
			oo.writeInt(addrFrom[i].length());
			oo.writeBytes(addrFrom[i]+"\n");
		}
		oo.writeInt(addrTo.length);
		for(int i = 0; i < addrTo.length; i++) {
			oo.writeInt(addrTo[i].length());
			oo.writeBytes(addrTo[i]+"\n");
		}
		oo.writeInt(outHash.length);
		for(int i = 0; i < outHash.length; i++) {
			oo.writeInt(outHash[i].length);
			for(int j = 0; j < outHash[i].length; j++) {
				oo.writeInt(outHash[i][j].length());
				oo.writeBytes(outHash[i][j]+"\n");
			}
		}
		oo.writeInt(answerScript.length);
		for(int i = 0; i < answerScript.length; i++) {
			oo.writeInt(answerScript[i].length);
			for(int j = 0; j < answerScript[i].length; j++) {
				oo.writeInt(answerScript[i][j].length());
				oo.writeBytes(answerScript[i][j]+"\n");
			}
		}
		oo.writeInt(questionScript.length);
		for(int i = 0; i < questionScript.length; i++) {
			oo.writeInt(questionScript[i].length);
			for(int j = 0; j < questionScript[i].length; j++) {
				oo.writeInt(questionScript[i][j].length());
				oo.writeBytes(questionScript[i][j]+"\n");
			}
		}
		oo.writeInt(signature.length());
		oo.writeBytes(signature+"\n");
		oo.writeInt(publicKey.length());
		oo.writeBytes(publicKey+"\n");
	}
}
