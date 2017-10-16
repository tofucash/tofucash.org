package Main;

public class Constant {
	private Constant() {}
	static final int TEST_NETWORK = 1;
	static final int PRODUCT_NETWORK = 2;
	
	class Transaction {
		static final int OUTPUT_IS_NOT_ENOUGH = -1;
		
		static final int VERSION = 1;
	}
	class Server {
	}
	class Log {
		static final String IMPORTANT = "\u001b[00;36m";
		static final String TEMPORARY = "\u001b[00;45m \u001b[00;37m";
		static final String INVALID = "\u001b[00;41m \u001b[00;31m";
	}
	class NetworkObject {
		static final int BLOCK = 1;
		static final int TX = 2;
	}
}
