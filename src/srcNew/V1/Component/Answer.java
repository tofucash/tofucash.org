package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Script;
import V1.Library.Constant.Script.OPCode;

public class Answer extends Script implements Externalizable {
	private static final long serialVersionUID = 199603311060000L;
	private byte[] script;
	private byte[] receiver;

	public Answer() {
		script = null;
		receiver = null;
	}

	public Answer(byte[] script, byte[] receiver) {
		this.script = script;
		this.receiver = receiver;
	}

	public byte[] getScript() {
		return script;
	}
	public byte[] getReceiver() {
		return receiver;
	}


	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int readByte = oi.readInt();
		if(readByte > Constant.Script.BYTE_MAX_ANSWER) {
			return;
		}
		script = new byte[readByte];
		oi.read(script);
		int receiverLength = oi.readInt();
		if(receiverLength > Constant.Address.BYTE_ADDRESS) {
			return;
		}
		receiver = new byte[receiverLength];
		oi.read(receiver);

	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(script.length);
		oo.write(script);
		oo.writeInt(receiver.length);
		oo.write(receiver);
	}

	public String toString() {
		return "[answerScript: " + DatatypeConverter.printHexBinary(script) + ", receiver: " + DatatypeConverter.printHexBinary(receiver) + "]";
	}
}
