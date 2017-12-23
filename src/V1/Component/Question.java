package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Log;


public class Question extends Script implements Externalizable{
	private static final long serialVersionUID = 199603311070000L;
	private byte[] script;
	private byte[] receiver;
	
	public Question() {
		script = null;
		receiver = null;
	}
	public Question(byte[] script, byte[] receiver) {
		this.script = script;
		this.receiver = receiver;
	}	
	public byte[] getScript() {
		return this.script;
	}
	public byte[] getReceiver() {
		return this.receiver;
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		int readByte = oi.readInt();
		if(readByte > Constant.Script.BYTE_MAX_QUESTION) {
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
		return "[questionScript: "+DatatypeConverter.printHexBinary(script) + ", receiver: "+DatatypeConverter.printHexBinary(receiver);
	}

}
