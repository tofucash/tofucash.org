package V1.TestClient;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Log;

public class TestObject {
	public byte[] aa;
	public TestObject() {
	}
	public String toString() {
		return "[aa: "+DatatypeConverter.printHexBinary(aa)+"]";
	}
}