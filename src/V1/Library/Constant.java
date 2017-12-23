package V1.Library;

import java.nio.ByteBuffer;

public class Constant {
	// 他のクラスから参照する定数はConstantへ、それ以外はそれぞれのクラスにprivateとして設定したほうがよいか
	// それだと、どこにどんな定数があったかが分かりづらくなる
	// 他のクラスから参照してほしくない変数は別に無いと思うのでこれでいいか
	// final宣言してあるから改変されることは無い
	
	private Constant() {
	}

	public static final int TEST_NETWORK = 1;
	public static final int PRODUCT_NETWORK = 2;

	public static class Address {
		private Address() {
		}

		public static final int BYTE_ADDRESS = 128;
		public static final int BYTE_PRIVATE_KEY = 64;
		public static final int BYTE_PUBLIC_KEY = 65;
		public static final int BYTE_PRIVATE_KEY_PREFIX = 32;
		public static final int BYTE_PUBLIC_KEY_PREFIX = 23;
		public static final String PRIVATE_KEY_PREFIX = "303E020100301006072A8648CE3D020106052B8104000A042730250201010420";
		public static final String PUBLIC_KEY_PREFIX = "3056301006072A8648CE3D020106052B8104000A034200";
	}

	public static class Block {
		private Block() {
		}

		public static final int VERSION = 1;
		public static final int MAX_TX_POWER = 8;
		public static final int MAX_TX = (int) Math.pow(2, MAX_TX_POWER);
		public static final int BYTE_BLOCK_HASH = 64;
		public static final int BYTE_NONCE = 64;
		public static final String DEFAULT_TARGET = "0fffff0000000000000000000000000000000000000000000000000000000000";
		public static final String DEFAULT_PREV_BLOCK_HASH = "0000000000000000000000000000000000000000000000000000000000000000";
	}
	public static class Blockchain {
		private Blockchain() {
		}
		
		public static final int SAVE_FILE_PER_DIR = 1000;
		public static final int MAX_PREV_BLOCK_HASH_LIST = 6;
		public static final int BYTE_BLOCK_HASH = Block.BYTE_BLOCK_HASH;
		public static final int TX_FEE = 1;

		public static final int BLOCK = NetworkObject.TYPE_BLOCK;
		public static final int TX = NetworkObject.TYPE_TX;
		public static final int NODE = NetworkObject.TYPE_NODE;
		public static final int BLOCK_BROADCAST = NetworkObject.TYPE_BLOCK_BROADCAST;
		public static final int TX_BROADCAST = NetworkObject.TYPE_TX_BROADCAST;
		public static final int NODE_BROADCAST = NetworkObject.TYPE_NODE_BROADCAST;
		public static final int ADJUST_TARGET = 2016;

	}

	public static class BlockHeader {
		private BlockHeader() {
		}

		public static final int VERSION = 1;
	}


	public static class Crypto {
		private Crypto() {
		}
		public static final String SIGN_ALGO = "SHA256withECDSA";
	}
	public static class IO {
		private IO() {
		}
		public static final int BYTE_BUF = 1024;
	}
	public static class MerkleTree {
		private MerkleTree() {
		}
		public static final int BYTE_MERKLE_ROOT = 64;

	}

	public static class NetworkObject {
		private NetworkObject() {
		}

		public static final int TYPE_BLOCK = 100;
		public static final int TYPE_TX = 200;
		public static final int TYPE_NODE = 300;

		public static final int TYPE_BLOCK_BROADCAST = 110;
		public static final int TYPE_TX_BROADCAST = 210;
		public static final int TYPE_NODE_BROADCAST = 310;

		public static final int TYPE_WORK = 600;

		public static final int TYPE_REPORT = 700;

		public static final int TYPE_REQUEST = 800;
		
		public static final int TYPE_UTXO = 900;

		
		public static final int BYTE_MAX_HASH = 64;
		public static final int BYTE_MAX_NONCE = 64;
		public static final int BYTE_MAX_MINER = 64;
		
	}
	public static class Node {
		private Node() {
		}
		public static final int DEFAULT_PORT = Server.SERVER_PORT;
		public static final int BYTE_ADDRESS = Address.BYTE_ADDRESS;
		public static final int BYTE_PUBLIC_KEY = Address.BYTE_PUBLIC_KEY;
		public static final int BYTE_MAX_SIGNATURE = Transaction.BYTE_MAX_SIGNATURE;
		public static final int BYTE_PUBLIC_KEY_PREFIX = 24;
	}
	public static class Report {
		private Report() {
		}
		public static final int BYTE_MAX_HASH = NetworkObject.BYTE_MAX_HASH;
		public static final int BYTE_MAX_NONCE = NetworkObject.BYTE_MAX_NONCE;
		public static final int BYTE_MAX_MINER = NetworkObject.BYTE_MAX_MINER;
	}
	public static class Request {
		private Request() {
		}
		public static final int TYPE_SEND_TOFU = 1000;
		public static final int TYPE_CHECK_BALANCE = 2000;
	}

	public static class Server {
		private Server() {
		}

		public static final int SERVER_READ_SLEEP = 1000;
		public static final int SERVER_PORT = 45910;
		public static final int HASH_SERVER_PORT = 50813;
		public static final int SERVER_BUF = 1024 * 1024; // 1MB
		public static final int MAX_RECEPT_DATA_HASH_LIST = 1000;
		
		public static final int MAX_ACCESS_PER_DAY = 10;
		public static final int NONCE_CNT = 100;
		public static final String HASH_ALGO = "sha512";
	}
	public static class Stack {
		private Stack() {}
		
		public static final int LENGTH_MAX_STACK = 100;
		
		public static final int NOTHING_IN_STACK = Script.OPCode.FALSE;
	}
	public static class Transaction {
		private Transaction() {
		}

		public static final int OUTPUT_IS_NOT_ENOUGH = -1;

		public static final int VERSION = 1;
		public static final int BYTE_OUT_HASH = 64;

		public static final int MAX_INPUT_OUTPUT = 31;
		
		public static final int BYTE_MAX_SIGNATURE = 72;
		public static final int DEFAULT_LOCKTIME = 1;
	}
	public static class Time {
		private Time() {}
		public static final int BYTE_TIMESTAMP = 5;
	}

	public static class Log {
		private Log() {
		}

		public static final String NORMAL = "\u001b[00;44m \u001b[00;34m";
		public static final String IMPORTANT = "\u001b[00;46m \u001b[00;36m";
		public static final String TEMPORARY = "\u001b[00;47m \u001b[00;33m";
		public static final String EXCEPTION = "\u001b[00;41m \u001b[00;31m";
		public static final String INVALID = "\u001b[00;45m \u001b[00;35m";
	}
	
	public static class Work {
		public static final int BYTE_MAX_HASH = NetworkObject.BYTE_MAX_HASH;
	}

	public static class Script {
		private Script() {
		}
		public static final int BYTE_MAX_ANSWER = 1000;
		public static final int BYTE_MAX_QUESTION = 1000;
		
		public static final int LENGTH_REGISTER = 10;
		
		public static class OPCode {
			public static byte FALSE = 0x00;
			public static byte TRUE = 0x01;
			public static byte END = 0x02;

			public static byte PUSH512 = 0x10;	// push to stack
			public static byte PUSH256 = 0x11;	// push to stack
			public static byte PUSH160 = 0x12;	// push to stack
			public static byte PUSH128 = 0x13;	// push to stack
			public static byte PUSH72 = 0x14;	// push to stack
			public static byte PUSH64 = 0x15;	// push to stack
			public static byte PUSH32 = 0x16;	// push to stack
			public static byte PUSH_MAX_512 = 0x17;	// PUSH_MAX_512 data.length data
			
			public static byte POP1_0 = 0x20;	// pop 1 byte to register1 from stack
			
			public static byte EQUAL = 0x30;
			public static byte EQUAL_VERIFY = 0x31;
			public static byte IF = 0x31;
			
			public static byte HASH_TWICE = 0x40;	// double hash (sha256)
//			public static byte HASH160 = 0x41;	// hash (sha256) and hash (ripemd160)
			
			public static byte CHECK_SIG = 0x50;	// sig
			
			public static byte DUP = 0x60;	// duplicate stack top data
			public static byte PUBK_DUP = 0x61;	// duplicate stack top data (attach public key prefix)
//			public static byte DUP_PUSH = 0x62;	// duplicate stack top data and push
			
			public static byte CHECK_ADDR = 0x70;
			
			
		}
		public enum Result{
			FAILED, SOLVED
		}
		public enum State {
			OP, END, PUSH, POP, PUSH_OPTION
		}

	}

}
