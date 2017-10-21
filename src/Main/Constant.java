package Main;

public class Constant {
	private Constant() {}
	static final int TEST_NETWORK = 1;
	static final int PRODUCT_NETWORK = 2;
	
	static final int BYTE_TIMESTAMP = 4;
	
	class Address {
		private Address() {}
		static final int BYTE_ADDRESS = 20;
		static final int BYTE_PRIVATE_KEY = 512;
	}
	class BlockHeader {
		private BlockHeader() {}
		static final int VERSION = 1;
	}
	class Block {
		private Block() {}
		static final int VERSION = 1;
		static final int MAX_TX = 10000;
		static final int BYTE_BLOCK_HASH = 64;
		static final int BYTE_NONCE = 64;
	}
	class Transaction {
		private Transaction() {}
		static final int OUTPUT_IS_NOT_ENOUGH = -1;
		
		static final int VERSION = 1;
		static final int BYTE_TX_HASH = 64;
		
		static final int MAX_INPUT_OUTPUT = 31;
	}
	class Server {
		private Server() {}
	}
	class Log {
		private Log() {}
		static final String IMPORTANT = "\u001b[00;36m";
		static final String TEMPORARY = "\u001b[00;45m \u001b[00;37m";
		static final String EXCEPTION = "\u001b[00;41m \u001b[00;31m";
	}
	class NetworkObject {
		private NetworkObject() {}
		static final int BLOCK = 1;
		static final int TX = 2;
	}
}
