package Main;

public class Constant {
	// 他のクラスから参照する定数はConstantへ、それ以外はそれぞれのクラスにprivateとして設定したほうがよいか
	// それだと、どこにどんな定数があったかが分かりづらくなる
	// 他のクラスから参照してほしくない変数は別に無いと思うのでこれでいいか
	// final宣言してあるから改変されることは無い
	
	private Constant() {
	}

	static final int TEST_NETWORK = 1;
	static final int PRODUCT_NETWORK = 2;

	static final int BYTE_TIMESTAMP = 4;

	static class Address {
		private Address() {
		}

		static final int BYTE_ADDRESS = 20;
		static final int BYTE_PRIVATE_KEY = 512;
	}

	static class Blockchain {
		private Blockchain() {
		}
		
		static final int SAVE_FILE_PER_DIR = 1000;
		static final int MAX_PREV_BLOCK_HASH_LIST = 6;
	}

	static class BlockHeader {
		private BlockHeader() {
		}

		static final int VERSION = 1;
	}

	static class Block {
		private Block() {
		}

		static final int VERSION = 1;
		static final int MAX_TX = 100;
		static final int BYTE_BLOCK_HASH = 64;
		static final int BYTE_NONCE = 64;
	}

	static class NetworkObject {
		private NetworkObject() {
		}

		static final int BLOCK = 1;
		static final int TX = 2;
	}

	static class Transaction {
		private Transaction() {
		}

		static final int OUTPUT_IS_NOT_ENOUGH = -1;

		static final int VERSION = 1;
		static final int BYTE_TX_HASH = 64;

		static final int MAX_INPUT_OUTPUT = 31;
	}


	static class Server {
		private Server() {
		}

		static final int SERVER_PORT = 8081;
		static final int SERVER_BUF = 1024 * 1024; // 1MB
	}
	static class Stack {
		private Stack() {}
		
		static final int LENGTH_MAX_STACK = 100;
		
		static final int NOTHING_IN_STACK = Script.OPCode.FALSE;
	}

	static class Log {
		private Log() {
		}

		static final String IMPORTANT = "\u001b[00;36m";
		static final String TEMPORARY = "\u001b[00;45m \u001b[00;37m";
		static final String EXCEPTION = "\u001b[00;41m \u001b[00;31m";
	}

	// class file does not exists
	static class Environment {
		private Environment() {
		}

		static final String SEPARATOR = System.getProperty("file.separator");
	}

	static class Script {
		private Script() {
		}
		static final int BYTE_MAX_ANSWER = 100;
		static final int BYTE_MAX_QUESTION = 100;
		
		static final int LENGTH_REGISTER = 10;
		
		static class OPCode {
			static byte FALSE = 0x00;
			static byte TRUE = 0x01;
			static byte END = 0x02;

			static byte PUSH512 = 0x10;	// push to stack
			static byte PUSH256 = 0x11;	// push to stack
			static byte PUSH160 = 0x12;	// push to stack
			static byte PUSH128 = 0x13;	// push to stack
			static byte PUSH64 = 0x14;	// push to stack
			static byte PUSH32 = 0x15;	// push to stack
			
			static byte POP1_0 = 0x20;	// pop 1 byte to register1 from stack
			static byte POP1_1 = 0x21;
			static byte POP1_2 = 0x23;
			static byte POP1_3 = 0x24;
			static byte POP1_4 = 0x25;
			static byte POP1_5 = 0x26;
			static byte POP1_6 = 0x27;
			static byte POP1_7 = 0x28;
			static byte POP1_8 = 0x29;
			static byte POP1_9 = 0x2a;	
			static byte POP32_0 = 0x2b;	// pop 32byte to register 0 from stack
			
			static byte EQUAL_VERIFY = 0x30;
			static byte IF = 0x31;
			
			static byte HASH_TWICE = 0x40;	// double hash (sha256)
//			static byte HASH160 = 0x41;	// hash (sha256) and hash (ripemd160)
			
			static byte CHECK_SIG = 0x50;	// sig
			
			static byte DUP = 0x60;	// duplicate stack top data
			static byte DUP_PUSH = 0x61;	// duplicate stack top data and push
		}
		enum Result{
			FAILED, SOLVED
		}
		enum State {
			OP, END, PUSH, POP
		}

	}

}
