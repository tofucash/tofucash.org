package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;

public class Work implements Externalizable {
	private static final long serialVersionUID = 199603310801000L;
	private byte[] hash;
	private byte[] difficulty;

	public Work() {
		hash = new byte[] {0x0};
		difficulty = new byte[] {0x0};
	}
	public Work(byte[] hash, byte[] difficulty) {
		this.hash = hash;
		this.difficulty = difficulty;
	}

	public byte[] getHash() {
		return hash;
	}
	public byte[] getDifficulty() {
		return difficulty;
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int hashLength = oi.readInt();
		if (hashLength > Constant.Work.BYTE_MAX_HASH) {
			return;
		}
		hash = new byte[hashLength];
		oi.read(hash);

		int difficultyLength = oi.readInt();
		if (difficultyLength > Constant.Work.BYTE_MAX_HASH) {
			return;
		}
		difficulty = new byte[difficultyLength];
		oi.read(difficulty);
	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(hash.length);
		oo.write(hash);
		oo.writeInt(difficulty.length);
		oo.write(difficulty);
	}
	
	public String toString() {
		return "[hash: " + DatatypeConverter.printHexBinary(hash) + ", difficulty: " + DatatypeConverter.printHexBinary(difficulty)+"]";
	}

}
