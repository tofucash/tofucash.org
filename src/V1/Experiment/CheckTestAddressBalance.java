package V1.Experiment;

import java.util.Random;

import V1.Library.Constant;

public class CheckTestAddressBalance {
	public static void main(String[] args) {
		PseudoClient.init();
		Random rnd = new Random(123);
		byte[][] one = new byte[1][];
		one[0] = new byte[Constant.Address.BYTE_ADDRESS];
		PseudoClient.checkBalance(one, rnd);
	}
}
