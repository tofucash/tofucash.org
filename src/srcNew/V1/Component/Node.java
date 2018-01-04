package V1.Component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Crypto;

public class Node implements Externalizable {
	private static final long serialVersionUID = 199603312030000L;
	private String ip;
	private int port;
	private String name;
	private byte[] address;
	private byte[] publicKey;
	private byte[] signature;

	public Node(String ip, int port, String name, byte[] address, KeyPair keyPair) {
		this.ip = ip;
		this.port = port;
		this.name = name;
		this.address = address;
		this.publicKey = keyPair.getPublic().getEncoded();
		this.signature = Crypto.sign(keyPair.getPrivate(), keyPair.getPublic(),
				(ip + port + name + DatatypeConverter.printHexBinary(address)
						+ DatatypeConverter.printHexBinary(publicKey)).getBytes(StandardCharsets.UTF_8));
	}

	public Node() {
		ip = "";
		port = Constant.Node.DEFAULT_PORT;
		name = "";
		address = null;
		publicKey = null;
		signature = null;
	}

	public byte[] getAddress() {
		return address;
	}

	public String getIp() {
		return ip;
	}
	public int getPort() {
		return port;
	}
	public boolean checkSig() {
		return Crypto
				.verify(publicKey,
						(ip + port + name + DatatypeConverter.printHexBinary(address)
								+ DatatypeConverter.printHexBinary(publicKey)).getBytes(StandardCharsets.UTF_8),
						signature);
	}

	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		ip = (String) oi.readObject();
		port = oi.readInt();
		name = (String) oi.readObject();
		int byteAddress = oi.readInt();
		if (byteAddress > Constant.Node.BYTE_ADDRESS) {
			return;
		}
		address = new byte[byteAddress];
		oi.read(address);

		int bytePublicKey = oi.readInt();
		if (bytePublicKey > Constant.Node.BYTE_PUBLIC_KEY_PREFIX + Constant.Node.BYTE_PUBLIC_KEY) {
			return;
		}
		publicKey = new byte[bytePublicKey];
		oi.read(publicKey);

		int byteSignature = oi.readInt();
		if (byteSignature > Constant.Node.BYTE_MAX_SIGNATURE) {
			return;
		}
		signature = new byte[byteSignature];
		oi.read(signature);

	}

	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(ip);
		oo.writeInt(port);
		oo.writeObject(name);
		oo.writeInt(address.length);
		oo.write(address);
		oo.writeInt(publicKey.length);
		oo.write(publicKey);
		oo.writeInt(signature.length);
		oo.write(signature);
	}

	public String toString() {
		return "[IP: " + ip + ", port: " + port + ", name: " + name + ", address: "
				+ DatatypeConverter.printHexBinary(address) + ", publicKey: "
				+ DatatypeConverter.printHexBinary(publicKey) + ", signature: "
				+ DatatypeConverter.printHexBinary(signature) + "]";
	}
}
