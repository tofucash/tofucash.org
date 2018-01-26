package V1.Experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyPair;

import javax.xml.bind.DatatypeConverter;

import V1.Library.Address;
import V1.Library.Constant;
import V1.Library.KeyAddressSet;
import V1.Library.Log;
import net.arnx.jsonic.JSON;

public class CreateKeyAddress {
	private static int CLIENT_ID = -1;
	public static void main(String[] args) {
		// String keyAddressSetFileDir = System.getProperty("user.dir") +
		// File.separator + ".." + File.separator + "data"
		// + File.separator + "experiment20180122"+ File.separator;
		 String keyAddressSetFileDir = System.getProperty("user.dir") +
		 File.separator + "data" + File.separator
		 + "experiment20180122" + File.separator + CLIENT_ID + File.separator;
//		String keyAddressSetFileDir = "/dev/null/";
		new File(keyAddressSetFileDir).mkdir();
		try {
			for (int i = 0; i < Constant.Test.ACCOUNT_NUM; i++) {
				KeyAddressSet keyAddressSet = new KeyAddressSet();
				for (int j = 0; j < Constant.Test.ACCOUNT_NUM; j++) {
					KeyPair keyPair = Address.createKeyPair();
					keyAddressSet.privateKey[j] = DatatypeConverter.printHexBinary(keyPair.getPrivate().getEncoded())
							.substring(Constant.Address.BYTE_PRIVATE_KEY_PREFIX * 2);
					keyAddressSet.publicKey[j] = DatatypeConverter.printHexBinary(keyPair.getPublic().getEncoded())
							.substring(Constant.Address.BYTE_PUBLIC_KEY_PREFIX * 2);
					keyAddressSet.address[j] = DatatypeConverter
							.printHexBinary(Address.getAddress(keyPair.getPublic()));
				}
				File file = new File(keyAddressSetFileDir + i + ".json");
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				pw.print(JSON.encode(keyAddressSet));
				pw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Log.log("Done.");
	}
}
