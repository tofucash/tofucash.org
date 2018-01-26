package V1.Library;

public class KeyAddressSet {
	public String[] privateKey;
	public String[] publicKey;
	public String[] address;

	public KeyAddressSet() {
		privateKey = new String[Constant.Test.ACCOUNT_NUM];
		publicKey = new String[Constant.Test.ACCOUNT_NUM];
		address = new String[Constant.Test.ACCOUNT_NUM];
	}
}