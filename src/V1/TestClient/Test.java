package V1.TestClient;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Block;
import V1.Component.BlockHeader;
import V1.Component.Node;
import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Request;
import V1.Component.Spent;
import V1.Component.UTXO;
import V1.Library.Base58;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.TofuException.AddressFormatException;
import net.arnx.jsonic.web.WebServiceServlet.JSON;

public class Test {
	public static void main(String[] args) {
		 try {
		     // Encode a String into bytes
		     String inputString = "blaafsd;lkjfa;sldkfja;lskdfja;lskfja;lskdjf;alksdjf;laksjdf;laksdjf;lak3j2;ofjia;sldkcm/a.x,calkesj;fl2;oiefjalksdmf/ca.s,dmfh2oerji;lawueafjlkcsdjx;hblahblah0000";
		     byte[] input = inputString.getBytes("UTF-8");

		     // Compress the bytes
		     byte[] output = new byte[1000];
		     Deflater compresser = new Deflater();
		     compresser.setInput(input);
		     compresser.finish();
		     int compressedDataLength = compresser.deflate(output);
		     compresser.end();
		     
		     Log.log("datalength: " + inputString.getBytes().length);
		     Log.log("length: " + compressedDataLength);
		     Log.log("length: " + DatatypeConverter.printHexBinary(output));

		     // Decompress the bytes
		     Inflater decompresser = new Inflater();
		     decompresser.setInput(output, 0, compressedDataLength);
		     byte[] result = new byte[input.length];
		     int resultLength = decompresser.inflate(result);
		     decompresser.end();

		     // Decode the bytes into a String
		     String outputString = new String(result, 0, resultLength, "UTF-8");
		     Log.log("output: " + outputString);
		 } catch(java.io.UnsupportedEncodingException ex) {
		     // handle
		 } catch (java.util.zip.DataFormatException ex) {
		     // handle
		 }
	}
	public static void addressCheck() {
		byte[] receiver1;
		try {
			receiver1 = Base58.decode("66UHD9Ac8AWc4yUddFhMRkPtHcWU2Q4dp1s1PZVrm2QWkaUPK8GFWY5u7rt9FR3t8tHP2dwgbqgwgXXG9ym8Y13E");
			Log.log("receiver1: " + DatatypeConverter.printHexBinary(receiver1));
			byte[] addrHex = Crypto.hashTwice(DatatypeConverter.parseHexBinary(
					"0413433316B2BD3B861B509DAB0C99F6867391E72ADB29693ED6168ECC2873F21E15944C8FC970F0E9582382CA0DC991460C01E4297CC921382A5FF21D4E933413"));
			Log.log("addrHex: " + DatatypeConverter.printHexBinary(addrHex));
			Log.log("Base58.encode: " + Base58.encode(addrHex));
		} catch (AddressFormatException e) {
			e.printStackTrace();
		}
	}
	public static void adjustTargetTest2() {
		byte[] defaultTarget = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
		int shift = 1;
		Log.log("[Blockchain.targetAdjust()] shift: " + shift, Constant.Log.TEMPORARY);
		BigInteger targetNum = new BigInteger(defaultTarget);			
		byte[] targetTmp;
		if (shift > 0) {
			targetTmp = targetNum.shiftRight(shift).toByteArray();
		} else {
			targetTmp = targetNum.shiftLeft(shift).toByteArray();
		}
		byte[] newTarget = new byte[Constant.Block.BYTE_TARGET];
		System.arraycopy(targetTmp, 0, newTarget, Constant.Block.BYTE_TARGET-targetTmp.length, targetTmp.length);
		Log.log("d  Target: " + DatatypeConverter.printHexBinary(defaultTarget));
		Log.log("newTarget: " + DatatypeConverter.printHexBinary(newTarget));
	}
	public static void adjustTargetTest() {
		List<Long> blockTimeList = new ArrayList<Long>();
		byte[] newTarget = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
		;
		Log.log("newTarget: " + DatatypeConverter.printHexBinary(newTarget));

		long blockTimeSum = 0, shift;
		int i = 0;
		for (int j = 0; j < Constant.Blockchain.LENGTH_MAX_BLOCK_TIME_LIST - blockTimeList.size(); j++) {
			blockTimeSum += Constant.Blockchain.AVERAGE_BLOCK_TIME;
		}
		blockTimeSum -= System.currentTimeMillis() / 1000
				- Constant.Blockchain.AVERAGE_BLOCK_TIME * (Constant.Blockchain.CONFIRMATION);
		blockTimeSum += System.currentTimeMillis() / 1000 - 9*2;
		Log.log("blockTimeSum: " + blockTimeSum + "\taverageBlockTime: " + Constant.Blockchain.AVERAGE_BLOCK_TIME*(Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK-1));
		shift = ((Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK-1)*Constant.Blockchain.AVERAGE_BLOCK_TIME - blockTimeSum)/(Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK-1)/Constant.Blockchain.TARGET_SHIFT_PER_TIME;
//		shift = blockTimeSum / Constant.Blockchain.AVERAGE_BLOCK_TIME / (Constant.Blockchain.DIFFICULTY_ADJUST_BLOCK - 1) / Constant.Blockchain.TARGET_SHIFT_PER_TIME;
		Log.log("[Blockchain.targetAdjust()] shift: " + shift, Constant.Log.TEMPORARY);
		if (shift > 0) {
//			for(i = 0; i < 8; i++) {
				int newTargetInt = 0;
				byte[] intarr = new byte[4];
				System.arraycopy(newTarget, i*4, intarr, 0, 4);
				newTargetInt = ByteBuffer.wrap(intarr).getInt() >>> shift;
				System.arraycopy(ByteBuffer.allocate(4).putInt(newTargetInt).array(), 0, newTarget, i*4, 4);
//			}
		} else {
//			for(i = 0; i < 8; i++) {
				int newTargetInt = 0;
				byte[] intarr = new byte[4];
				System.arraycopy(newTarget, i*4, intarr, 0, 4);
				newTargetInt = ByteBuffer.wrap(intarr).getInt() << -shift;
				System.arraycopy(ByteBuffer.allocate(4).putInt(newTargetInt).array(), 0, newTarget, i*4, 4);
//			}
		}
		Log.log("newTarget: " + DatatypeConverter.printHexBinary(newTarget));
	}
	public static void old() {
		Log.log("0x17: " + DatatypeConverter.printHexBinary(new byte[] { 0x17 }));
		try {
			Log.log(DatatypeConverter.printHexBinary(Base58.decode(
					"67aNghDqbJpTQrzpeZfrC1haxGFiJLW1xn174j7YT3uHxeJGDTCRE5MFQWFbaXbwCeSc8ga1CBb7nWVHD1KNFxdo")));
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List list = (List) JSON.decode(
				"[255, 193, 96, 102, 209, 167, 134, 223, 217, 212, 113, 24, 233, 58, 20, 255, 201, 123, 14, 89, 77, 244, 74, 133, 166, 153, 158, 38, 242, 46, 15, 172, 69, 246, 73, 36, 47, 84, 174, 188, 233, 107, 229, 21, 206, 22, 207, 171, 203, 248, 50, 74, 89, 212, 217, 94, 115, 131, 65, 156, 223, 201, 34, 210]");
		byte[] byteList = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			byteList[i] = ((BigDecimal) list.get(i)).byteValue();
		}
		Log.log(DatatypeConverter.printHexBinary(byteList));
	}

	public static void old2() {
		try {
			String pubKey = "3056301006072A8648CE3D020106052B8104000A03420004e0c6429df283f0559d5be2f43c01ec3d88c4b9d77a0b3a36f2615a5a4e2288328d4aebcf3653d531f85052b8eb0b28fa26014b0eacefc911d5da56abda4967f1";
			String outHash = "1284BFDD32F1C82840950EC1DD8FB73F6D8DB83213B9FE2A200FFE2122D58D920D175B4BB6D29325B749A6E5D16A3FB604F8FEB2439D8B4768A194972F81CA9D";
			String sign = "304502206f7c70603bd6d409d55c5d2bc43fea775fcc0ee5d0e28b05d5708559c402d2b00221009b1c0b3af85ea9651b9332f07e2c104b90fba7d7996fd6a2388fc062ad6e3287";
			Log.log("verify: " + Crypto.verify(DatatypeConverter.parseHexBinary(pubKey),
					DatatypeConverter.parseHexBinary(outHash), DatatypeConverter.parseHexBinary(sign)));

			byte[] receiver1 = Base58
					.decode("3zqBQ4ETSn2zM6nb8QqSB2MiiJehdGij5PnVcdMmTLBY9yfwtVoNtQxCGARaKbSVVFRAw7URwhMB67pGcBoxfsby");
			// byte[] receiver2 =
			// Base58.decode("6YoW15JLnxPkya9bBefnXPFcXqwaFP1MfSucx6skcsC6CiFLbqVdMRo6surDgHtndVKxN1VaJUa5scc4qYeqoGK5dkari8Rpptdw2cDsZUNTnYB6nazXLXG46JgVPLtEZvKvhVvDWtnSerKYgDQpA8yUKXy23ChNn7SbjbUhwNdYfrD");
			Log.log("receiver1: " + DatatypeConverter.printHexBinary(receiver1));
			// Log.log("receiver2: " +
			// DatatypeConverter.printHexBinary(receiver2));
			byte[] addrHex = Crypto.hashTwice(DatatypeConverter.parseHexBinary(
					"04a1c9c73ef6a24b745ca7639cd3120f8ceba5757906919a0e6728c91256de735e032f3c42eec8a72616efd370473a315cccae58d22ddd439f397e58faf545eff7"));
			Log.log("addrHex: " + DatatypeConverter.printHexBinary(addrHex));
			Log.log("Base58.encode: " + Base58.encode(addrHex));
			Log.log("decode: " + DatatypeConverter.printHexBinary(Base58.decode(Base58.encode(addrHex))));
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.log("sha512: " + DatatypeConverter.printHexBinary(Crypto.hash512(DatatypeConverter.parseHexBinary(
				"8fd57d8696146456c2277031d4ad869d4b7df903d33b5c5ba7218ca725b293584fbd0dbd06f60b780fd2383ca3060d688ff42aab0296db07ff0b38c88bb65da2"))));
		TestObject ta = JSON.decode(
				"{\"aa\": \"EBB68D8210EA6C9A5342BCE97997EB0B585AC4616224B5B5C67D7BE23ED84F7B87C5E2460D309113930541615B6976F009E49CF206B50816FB9D65470F528D171768\"}",
				TestObject.class);
		Log.log("ta: " + ta);
	}
}
