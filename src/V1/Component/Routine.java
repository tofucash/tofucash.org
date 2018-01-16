package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import V1.Library.Constant;

public class Routine implements Externalizable {
	private int lockTime;
	private String[] addrTo;
	private int[] amountTo;
	private String fAddress;
	private String publicKey;
	private String signature;

	public Routine() {
		lockTime = -1;
		addrTo = null;
		amountTo = null;
		fAddress = null;
		publicKey = null;
		signature = null;
	}

	public Routine(int lockTime, String[] addrTo, int[] amountTo, String fAddress, String publicKey, String signature) {
		this.lockTime = lockTime;
		this.addrTo = addrTo;
		this.amountTo = amountTo;
		this.fAddress = fAddress;
		this.publicKey = publicKey;
		this.signature = signature;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public String getSignature() {
		return signature;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		lockTime = oi.readInt();
		int addrToListLength = oi.readInt();
		if (addrToListLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		addrTo = new String[addrToListLength];
		for (int i = 0; i < addrToListLength; i++) {
			int addrToLength = oi.readInt();
			if (addrToLength > Constant.Address.BYTE_ADDRESS * 2) {
				return;
			}
			addrTo[i] = oi.readLine();
		}

		int amountToLength = oi.readInt();
		if (amountToLength > Constant.Transaction.MAX_INPUT_OUTPUT) {
			return;
		}
		amountTo = new int[amountToLength];
		for (int i = 0; i < amountToLength; i++) {
			amountTo[i] = oi.readInt();
		}
		
		int fAddressLength = oi.readInt();
		if(fAddressLength > Constant.Address.BYTE_ADDRESS*2) {
			return;
		}
		fAddress = oi.readLine();

		int publicKeyLength = oi.readInt();
		if(publicKeyLength > Constant.Address.BYTE_PUBLIC_KEY*2) {
			return;
		}
		publicKey = oi.readLine();

		int signatureLength = oi.readInt();
		if(signatureLength > Constant.Transaction.BYTE_MAX_SIGNATURE*2) {
			return;
		}
		signature = oi.readLine();
}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(lockTime);
		oo.writeInt(addrTo.length);
		for (int i = 0; i < addrTo.length; i++) {
			oo.writeInt(addrTo[i].length());
			oo.writeChars(addrTo[i] + "\n");
		}
		oo.writeInt(amountTo.length);
		for (int i = 0; i < amountTo.length; i++) {
			oo.writeInt(amountTo[i]);
		}
		oo.writeInt(fAddress.length());
		oo.writeChars(fAddress+"\n");
		oo.writeInt(publicKey.length());
		oo.writeChars(publicKey+"\n");
		oo.writeInt(signature.length());
		oo.writeChars(signature+"\n");
	}

	public String toString() {
		return "[lockTime: " + lockTime + ", addrTo: " + Arrays.toString(addrTo) + ", amountTo: "
				+ Arrays.toString(amountTo) + ", fAddress: "+fAddress+", publicKey: "+publicKey+", signature: "+signature+"]";
	}
}
