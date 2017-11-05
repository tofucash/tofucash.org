package Main;

public class TofuException {
	public static class AddressFormatException extends Exception {
		AddressFormatException(String content) {
			super(content);
			Log.log(content, Constant.Log.EXCEPTION);			
		}
	}
	public static class StackException extends Exception {
		StackException(String content) {
			super(content);
			Log.log(content, Constant.Log.EXCEPTION);			
		}
	}
	
	public static class SettingError extends Error {
		SettingError(String content) {
			super(content);
			Log.log(content, Constant.Log.EXCEPTION);			
		}
	}
	
	public static class UnimplementedError extends Error {
		UnimplementedError(String content) {
			super(content);
			Log.log(content, Constant.Log.EXCEPTION);			
		}
	}
//	public static class OutOfIndexException extends Exception {
//		OutOfIndexException(String content) {
//			super(content);
//			Log.log(content, Constant.Log.EXCEPTION);			
//		}
//		
//	}
}

