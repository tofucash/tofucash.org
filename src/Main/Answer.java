package Main;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

public class Answer extends Script implements Externalizable{
	private byte[] script;
	public Answer() {
		script = null;
	}
	public Answer(byte[] script) {
		this.script = script;
	}
	
	byte[] getScript() {
		return script;
	}
	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		oi.read(script);
	}
	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.write(script);
	}
	public String toString() {
		return DatatypeConverter.printHexBinary(script);
	}
}
