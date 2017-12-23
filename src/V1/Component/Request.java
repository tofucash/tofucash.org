package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.TofuError;

public class Request implements Externalizable {
	public int type;
	public int version;
	public int lockTime;
	public int[] amountFrom;
	public int[] amountTo;
	public String[] addrFrom;
	public String[] addrTo;
	public String[] outHash;
	public String[] answerScript;
	public String[] questionScript;
	public String signature;
	

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
	}

	public Request(int type, int version, int lockTime, int[] amountFrom, int[] amountTo, String[] addrFrom, String[] addrTo, String[] outHash,
			String[] answerScript, String[] questionScript, String signature) {
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
	}

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

	public String[] getOutHash() {
		return outHash;
	}

	public String[] getAnswerScript() {
		return answerScript;
	}

	public String[] getQuestionScript() {
		return questionScript;
	}

	public String getSignature() {
		return signature;
	}

	public String toString() {
		if(type == Constant.Request.TYPE_SEND_TOFU) {
			return "[type: " + type + ", amount: " + Arrays.asList(amountFrom) + ", amountTo: " + Arrays.asList(amountTo)
			+ ", addrFrom: " + Arrays.asList(addrFrom) + ", addrTo: "
			+ Arrays.asList(addrTo) + ", outHash: "
			+ Arrays.asList(outHash) + ", answerScript: "
			+ Arrays.asList(answerScript) + ", questionScript: "
			+ Arrays.asList(questionScript) + ", signature: "
			+ Arrays.asList(signature) + "]";
		} else if(type == Constant.Request.TYPE_CHECK_BALANCE) {
			return "[type: "+ type+ ", addrFrom: "+ Arrays.asList(addrFrom)+"]";
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
		for (int i = 0; i < amountToLength; i++) {
			amountTo[i] = oi.readInt();
		}

		int addrFromListLength = oi.readInt();
		if (addrFromListLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		for (int i = 0; i < addrFromListLength; i++) {
			int addrFromLength = oi.readInt();
			if (addrFromLength > Constant.Address.BYTE_ADDRESS*2) {
				return;
			}
			addrFrom[i] = (String) oi.readObject();
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
			addrTo[i] = (String) oi.readObject();
		}

		int outHashListLength = oi.readInt();
		if (outHashListLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		for (int i = 0; i < outHashListLength; i++) {
			int outHashLength = oi.readInt();
			if (outHashLength > Constant.Transaction.BYTE_OUT_HASH*2) {
				return;
			}
			outHash[i] = (String) oi.readObject();
		}

		int answerScriptListLength = oi.readInt();
		if (answerScriptListLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		for (int i = 0; i < answerScriptListLength; i++) {
			int answerScriptLength = oi.readInt();
			if (answerScriptLength > Constant.Address.BYTE_ADDRESS*2) {
				return;
			}
			answerScript[i] = (String) oi.readObject();
		}

		int questionScriptListLength = oi.readInt();
		if (questionScriptListLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		for (int i = 0; i < questionScriptListLength; i++) {
			int questionScriptLength = oi.readInt();
			if (questionScriptLength > Constant.Address.BYTE_ADDRESS*2) {
				return;
			}
			questionScript[i] = (String) oi.readObject();
		}

		
		
		int signatureLength = oi.readInt();
		if (signatureLength > Constant.Transaction.BYTE_MAX_SIGNATURE*2) {
			return;
		}
		signature = (String) oi.readObject();
		
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
			oo.writeChars(addrFrom[i]);
		}
		oo.writeInt(addrTo.length);
		for(int i = 0; i < addrTo.length; i++) {
			oo.writeInt(addrTo[i].length());
			oo.writeChars(addrTo[i]);
		}
		oo.writeInt(outHash.length);
		for(int i = 0; i < outHash.length; i++) {
			oo.writeInt(outHash[i].length());
			oo.writeChars(outHash[i]);
		}
		oo.writeInt(answerScript.length);
		for(int i = 0; i < answerScript.length; i++) {
			oo.writeInt(answerScript[i].length());
			oo.writeChars(answerScript[i]);
		}
		oo.writeInt(questionScript.length);
		for(int i = 0; i < questionScript.length; i++) {
			oo.writeInt(questionScript[i].length());
			oo.writeChars(questionScript[i]);
		}
		oo.writeInt(signature.length());
		oo.writeChars(signature);
	}
}
