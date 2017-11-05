package Main;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

public class Question extends Script implements Externalizable{
	private static final long serialVersionUID = 199603311070000L;
	private byte[] script;
	
	public Question() {
		script = null;
	}
	public Question(byte[] script) {
		this.script = script;
	}	
	byte[] getScript() {
		return this.script;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int readByte = oi.readInt();
		if(readByte > Constant.Script.BYTE_MAX_QUESTION) {
			return;
		}
		script = new byte[readByte];
		oi.read(script);
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(script.length);
		oo.write(script);
	}
	public String toString() {
		return DatatypeConverter.printHexBinary(script);
	}

}
