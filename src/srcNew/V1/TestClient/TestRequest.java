package V1.TestClient;

import java.util.Arrays;

import V1.Component.Block;

public class TestRequest {
	public int type;
	public byte[][] addrFrom;
	public Block aaa;
	public String toString() {
		return "type: " + type+", addrFrom: " + Arrays.asList(addrFrom) + ", aaa: " + aaa;
	}
}
