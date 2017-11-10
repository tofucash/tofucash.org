package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.DatatypeConverter;

public class BlockHeader implements Externalizable{
	private int version;
	private byte[] prevBlockHash;
	private byte[] timestamp;
	private byte[] nonce;
	private byte[] miner;
	
	public BlockHeader() {
		version = -1;
		prevBlockHash = new byte[1];
		timestamp = new byte[1];
		nonce = new byte[1];
		miner = new byte[1];
	}

	public BlockHeader(int version, byte[] prevBlockHash, byte[] timestamp, byte[] nonce, byte[] miner) {
		this.version = version;
		this.prevBlockHash = prevBlockHash;
		this.timestamp = timestamp;
		this.nonce = nonce;
		this.miner = miner;
	}
	
	byte[] getPrevBlockHash() {
		return prevBlockHash;
	}
	
	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		version = oi.read();
		oi.read(prevBlockHash);
		oi.read(timestamp);
		oi.read(nonce);
		oi.read(miner);
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.write(version);
		oo.write(prevBlockHash);
		oo.write(timestamp);
		oo.write(nonce);
		oo.write(miner);
	}

	public String toString() {
		return "[version: "+version+", prevBlockHash: " + DatatypeConverter.printHexBinary(prevBlockHash) + ", timestamp: " + DatatypeConverter.printHexBinary(timestamp) + ", nonce: "+DatatypeConverter.printHexBinary(nonce)+", miner: "+DatatypeConverter.printHexBinary(miner)+"]";
	}

}
