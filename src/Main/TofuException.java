package Main;

public class TofuException {
	public static class AddressFormatException extends Exception {
		AddressFormatException(String content) {
			Log.log(content, Constant.Log.EXCEPTION);			
		}
	}
	
	public static class SettingError extends Error {
		SettingError(String content) {
		}
	}
}

