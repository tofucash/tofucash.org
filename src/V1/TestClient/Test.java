package V1.TestClient;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Request;
import V1.Component.UTXO;
import V1.Library.Base58;
import V1.Library.Crypto;
import V1.Library.Log;
import V1.Library.TofuException.AddressFormatException;
import net.arnx.jsonic.web.WebServiceServlet.JSON;

public class Test {
	public static void main(String[] args) {
		try {
			String pubKey = "3056301006072A8648CE3D020106052B8104000A03420004e0c6429df283f0559d5be2f43c01ec3d88c4b9d77a0b3a36f2615a5a4e2288328d4aebcf3653d531f85052b8eb0b28fa26014b0eacefc911d5da56abda4967f1";
			String outHash = "1284BFDD32F1C82840950EC1DD8FB73F6D8DB83213B9FE2A200FFE2122D58D920D175B4BB6D29325B749A6E5D16A3FB604F8FEB2439D8B4768A194972F81CA9D";
			String sign = "304502206f7c70603bd6d409d55c5d2bc43fea775fcc0ee5d0e28b05d5708559c402d2b00221009b1c0b3af85ea9651b9332f07e2c104b90fba7d7996fd6a2388fc062ad6e3287";
			Log.log("verify: " + Crypto.verify(DatatypeConverter.parseHexBinary(pubKey), DatatypeConverter.parseHexBinary(outHash), DatatypeConverter.parseHexBinary(sign)));
			
			byte[] receiver1 = Base58.decode("3zqBQ4ETSn2zM6nb8QqSB2MiiJehdGij5PnVcdMmTLBY9yfwtVoNtQxCGARaKbSVVFRAw7URwhMB67pGcBoxfsby");
//			byte[] receiver2 = Base58.decode("6YoW15JLnxPkya9bBefnXPFcXqwaFP1MfSucx6skcsC6CiFLbqVdMRo6surDgHtndVKxN1VaJUa5scc4qYeqoGK5dkari8Rpptdw2cDsZUNTnYB6nazXLXG46JgVPLtEZvKvhVvDWtnSerKYgDQpA8yUKXy23ChNn7SbjbUhwNdYfrD");
			Log.log("receiver1: " + DatatypeConverter.printHexBinary(receiver1));
//			Log.log("receiver2: " + DatatypeConverter.printHexBinary(receiver2));
			byte[] addrHex = Crypto.hashTwice(DatatypeConverter.parseHexBinary("04a1c9c73ef6a24b745ca7639cd3120f8ceba5757906919a0e6728c91256de735e032f3c42eec8a72616efd370473a315cccae58d22ddd439f397e58faf545eff7"));
			Log.log("addrHex: " + DatatypeConverter.printHexBinary(addrHex));
			Log.log("Base58.encode: " + Base58.encode(addrHex));
			Log.log("decode: " + DatatypeConverter.printHexBinary(Base58.decode(Base58.encode(addrHex))));
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.log("sha512: " + DatatypeConverter.printHexBinary(Crypto.hash512(DatatypeConverter.parseHexBinary("8fd57d8696146456c2277031d4ad869d4b7df903d33b5c5ba7218ca725b293584fbd0dbd06f60b780fd2383ca3060d688ff42aab0296db07ff0b38c88bb65da2"))));
		TestObject ta = JSON.decode("{\"aa\": \"EBB68D8210EA6C9A5342BCE97997EB0B585AC4616224B5B5C67D7BE23ED84F7B87C5E2460D309113930541615B6976F009E49CF206B50816FB9D65470F528D171768\"}", TestObject.class);
		Log.log("ta: " + ta);
	}
	public static void old() {
		Log.log("0x17: "+DatatypeConverter.printHexBinary(new byte[]{0x17}));
		try {
			Log.log(DatatypeConverter.printHexBinary(Base58.decode("67aNghDqbJpTQrzpeZfrC1haxGFiJLW1xn174j7YT3uHxeJGDTCRE5MFQWFbaXbwCeSc8ga1CBb7nWVHD1KNFxdo")));
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List list = (List) JSON.decode("[255, 193, 96, 102, 209, 167, 134, 223, 217, 212, 113, 24, 233, 58, 20, 255, 201, 123, 14, 89, 77, 244, 74, 133, 166, 153, 158, 38, 242, 46, 15, 172, 69, 246, 73, 36, 47, 84, 174, 188, 233, 107, 229, 21, 206, 22, 207, 171, 203, 248, 50, 74, 89, 212, 217, 94, 115, 131, 65, 156, 223, 201, 34, 210]");
		byte[]byteList = new byte[list.size()];
		for(int i = 0; i < list.size(); i++) {
			byteList[i] = ((BigDecimal)list.get(i)).byteValue();
		}
		Log.log(DatatypeConverter.printHexBinary(byteList));
	}
}

