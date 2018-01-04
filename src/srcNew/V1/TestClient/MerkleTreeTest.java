package V1.TestClient;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;
import V1.Library.MerkleTree;

public class MerkleTreeTest {
	public static void main(String[] args) {
		test1();
		test2();
	}

	private static void test1() {
		ByteBuffer buf;
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(new byte[] {0x01, 0x02, 0x03});
		Log.log("1: "+ DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));
		byte[] hash1 = Crypto.hash512(buf.array());

		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(new byte[] {0x01, 0x02, 0x03});
		buf.put(new byte[] {0x01, 0x02, 0x03});
		Log.log("2: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));
		byte[] hash2 = Crypto.hash512(buf.array());
		
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash2);
		buf.put(hash1);
		Log.log("3: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));
		
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash2);
		buf.put(hash2);
		Log.log("4: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));
		byte[] hash4 = Crypto.hash512(buf.array());
		
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash1);
		byte[] hash5_tmp = Crypto.hash512(buf.array());
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash4);
		buf.put(hash5_tmp);
		Log.log("5: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));

		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash2);
		byte[] hash6_tmp = Crypto.hash512(buf.array());
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash4);
		buf.put(hash6_tmp);
		Log.log("6: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));

		
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash2);
		buf.put(hash1);
		byte[] hash7_tmp = Crypto.hash512(buf.array());
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash4);
		buf.put(hash7_tmp);
		Log.log("7: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));

		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash4);
		buf.put(hash4);
		Log.log("8: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));
		byte[] hash8 = Crypto.hash512(buf.array());
		
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash5_tmp);
		byte[] hash9_tmp = Crypto.hash512(buf.array());
		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
		buf.put(hash8);
		buf.put(hash9_tmp);
		Log.log("9: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));		

//		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
//		buf.put(hash6_tmp);
//		byte[] hash10_tmp = Crypto.hash512(buf.array());
//		buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
//		buf.put(hash4);
//		buf.put(hash10_tmp);
//		Log.log("10: "+DatatypeConverter.printHexBinary(Crypto.hash512(buf.array())));		

		Log.loghr("test1 end");
	}
	private static void test2() {
		List<byte[]> merkleTree = new ArrayList<byte[]>();
		List<byte[]> txHashList = new ArrayList<byte[]>();
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		txHashList.add(new byte[] {0x01, 0x02, 0x03});
		MerkleTree.updateMerkleTree(merkleTree, txHashList);
		Log.log(DatatypeConverter.printHexBinary(merkleTree.get(0)));
		for(int i = 0; i < merkleTree.size(); i++) {
			Log.log("" + i + " :" + DatatypeConverter.printHexBinary(merkleTree.get(i)));
		}
	}
}
