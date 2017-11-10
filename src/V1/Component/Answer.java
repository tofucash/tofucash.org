package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Constant.Script.OPCode;

public class Answer extends Script implements Externalizable {
	private static final long serialVersionUID = 199603311060000L;
	private byte[] script;

	public Answer() {
		script = null;
	}

	public Answer(byte[] script) {
		this.script = script;
	}

	public byte[] getScript() {
		return script;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int readByte = oi.readInt();
		if(readByte > Constant.Script.BYTE_MAX_ANSWER) {
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
