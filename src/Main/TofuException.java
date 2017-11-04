package Main;

public class TofuException {
	public static class AddressFormatException extends Exception {
		AddressFormatException(String content) {
			Log.log(content, Constant.Log.EXCEPTION);			
		}
	}
	public static class StackException extends Exception {
		StackException(String content) {
			
		}
	}
	
	public static class SettingError extends Error {
		SettingError(String content) {
		}
	}
	
	public static class UnimplementedError extends Error {
		UnimplementedError(String content) {
			
		}
	}
}

