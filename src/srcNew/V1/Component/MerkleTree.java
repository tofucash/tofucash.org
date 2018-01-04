package V1.Component;

import java.nio.ByteBuffer;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.Log;

public class MerkleTree {
	synchronized static public boolean updateMerkleTree(List<byte[]> merkleTree, final List<byte[]> txHashList) {
		ByteBuffer buf;
		if (txHashList == null) {
			return false;
		}

		if (txHashList.size() <= 2) {
			buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
			buf.put(txHashList.get(0));
			if (txHashList.size() == 2) {
				buf.put(txHashList.get(1));
				merkleTree.set(0, Crypto.hash512(buf.array()));
			} else {
				merkleTree.add(Crypto.hash512(buf.array()));
			}
			return true;
		}

		int size = txHashList.size();
		int i;
		// if (size % 2 == 0) {
		// i = (int) Math.floor(Math.sqrt(size - 1));
		// } else {
		// i = (int) Math.floor(Math.sqrt(size));
		// }
		for (i = 0; i < Constant.Block.MAX_TX_POWER - 1; i++) {
			if (size > Math.pow(2, i) && size <= Math.pow(2, i + 1)) {
				Log.log("i: " + i, Constant.Log.TEMPORARY);
				if (size == ((int) (Math.pow(2, i) + 1))) {
					merkleTree.add(0, null);
					Log.log("add 0 node", Constant.Log.TEMPORARY);
					int insertIndex = 2;
					int insertTimes = 1;
					for (int j = 0; j < i - 1; j++) {
						Log.log("insert times: " + insertTimes);
						for (int k = 0; k < insertTimes; k++) {
							merkleTree.add(insertIndex, new byte[Constant.Block.BYTE_BLOCK_HASH]);
						}
						insertIndex = insertIndex * 2 + 1;
						insertTimes *= 2;
					}
				}
				if (size % 2 == 1) {
					merkleTree.add(null);
				}

				if (i > Constant.Block.MAX_TX_POWER) {
					return false;
				}
				int merkleTreeIndex = merkleTree.size() - 1;
				int diff = (int) (Math.pow(2, i + 1) - 2 - merkleTreeIndex);
				Log.log("merkleTreeIndex: " + merkleTreeIndex, Constant.Log.TEMPORARY);
				Log.log("diff: " + diff, Constant.Log.TEMPORARY);
				buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
				buf.put(txHashList.get(merkleTreeIndex - diff));
				if (merkleTreeIndex - diff + 1 < txHashList.size()) {
					buf.put(txHashList.get(merkleTreeIndex - diff + 1));
				}
				merkleTree.set(merkleTreeIndex, Crypto.hash512(buf.array()));
				while (merkleTreeIndex > 0) {
					buf = ByteBuffer.allocate(Constant.Block.BYTE_BLOCK_HASH * 2);
					if (merkleTreeIndex % 2 == 0) {
						buf.put(merkleTree.get(merkleTreeIndex - 1));
						buf.put(merkleTree.get(merkleTreeIndex));
					} else {
						buf.put(merkleTree.get(merkleTreeIndex));
						if (merkleTreeIndex + 1 < merkleTree.size()) {
							buf.put(merkleTree.get(merkleTreeIndex + 1));
						}
					}
					merkleTreeIndex = (int) Math.floor((merkleTreeIndex - 1) / 2);
					merkleTree.set(merkleTreeIndex, Crypto.hash512(buf.array()));
				}
				return true;

			}
		}
		return false;
	}
}
