package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Address;
import V1.Library.Constant;

public class Report implements Externalizable {
	public String hash;
	public String nonce;
	public String result;
	public String cAddress;
	public String fAddress;
	public String publicKey;
	public String signature;

	public Report() {
		hash = null;
		nonce = null;
		result = null;
		cAddress = null;
		fAddress = null;
		publicKey = null;
		signature = null;
	}

	public Report(String hash, String nonce, String result, String cAddress, String fAddress, String signature, String publicKey) {
		this.hash = hash;
		this.nonce = nonce;
		this.result = result;
		this.cAddress = cAddress;
		this.fAddress = fAddress;
		this.publicKey = publicKey;
		this.signature = signature;
	}

	public String getHash() {
		return hash;
	}

	public String getNonce() {
		return nonce;
	}

	public String getResult() {
		return result;
	}

	public String getCAddress() {
		return cAddress;
	}
	public String getFAddress() {
		return fAddress;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int hashLength = oi.readInt();
		if (hashLength > Constant.Report.BYTE_MAX_HASH*2) {
			return;
		}
		hash = oi.readLine();

		int nonceLength = oi.readInt();
		if (nonceLength > Constant.Report.BYTE_MAX_NONCE*2) {
			return;
		}
		nonce = oi.readLine();

		int resultLength = oi.readInt();
		if (resultLength > Constant.Report.BYTE_MAX_HASH*2) {
			return;
		}
		result = oi.readLine();

		int cAddressLength = oi.readInt();
		if (cAddressLength > Constant.Report.BYTE_MAX_MINER*2) {
			return;
		}
		cAddress = oi.readLine();

		int fAddressLength = oi.readInt();
		if (fAddressLength > Constant.Report.BYTE_MAX_MINER*2) {
			return;
		}
		fAddress = oi.readLine();

		int publicKeyLength = oi.readInt();
		if (publicKeyLength > Constant.Address.BYTE_PUBLIC_KEY*2) {
			return;
		}
		publicKey = oi.readLine();

		int signatureLength = oi.readInt();
		if (signatureLength > Constant.Transaction.BYTE_MAX_SIGNATURE*2) {
			return;
		}
		signature = oi.readLine();
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(hash.length());
		oo.writeBytes(hash+"\n");
		oo.writeInt(nonce.length());
		oo.writeBytes(nonce+"\n");
		oo.writeInt(result.length());
		oo.writeBytes(result+"\n");
		oo.writeInt(cAddress.length());
		oo.writeBytes(cAddress+"\n");
		oo.writeInt(fAddress.length());
		oo.writeBytes(fAddress+"\n");
		oo.writeInt(publicKey.length());
		oo.writeBytes(publicKey+"\n");
		oo.writeInt(signature.length());
		oo.writeBytes(signature+"\n");
	}

	public String toString() {
		return "[hash: " + hash + ", nonce: "+ nonce + ", result: " + result
				+ ", cAddress: " + cAddress + ", fAddress: " + fAddress + ", publicKey: " + publicKey + ", signature: " + signature +"]";
	}
}
