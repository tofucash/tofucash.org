package Main;

import java.security.KeyPair;

import javax.xml.bind.DatatypeConverter;

import Main.TofuException.*;
public class Setting {
	static final String address = "1abcdefabcdefabcdefabcdefabcdef123"; 
	static final String blockchainBinDir = "./blockchain/";
	
	private static byte[] byteAddress;
	

	static void init() {
		byteAddress = address.getBytes();
		System.out.println("encode: "+Base58.encode(DatatypeConverter.parseHexBinary("006cecd092b7867838d611f8084ad0983c93eebbf49364e6f8")));
		try {
			System.out.println("decode: "+DatatypeConverter.printHexBinary(Base58.decode(address)));
		} catch (AddressFormatException e) {
			throw new SettingError("Wrong Address Format.");
		}
		
		if(!Library.isDirectory(blockchainBinDir)) {
			throw new SettingError("Blockchain Bin Dir is not directory or doesn't exists.");
		}
	}
	static byte[] getMyAddr() {
		return byteAddress;
	}
}
