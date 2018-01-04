package V1.Library;

public class TofuException {
	public static class AddressFormatException extends Exception {
		private static final long serialVersionUID = 19960331090000L;

		public AddressFormatException(String content) {
			super(content);
			Log.log(content, Constant.Log.EXCEPTION);
		}
	}

	public static class StackException extends Exception {
		private static final long serialVersionUID = 19960331090100L;

		public StackException(String content) {
			super(content);
			Log.log(content, Constant.Log.EXCEPTION);
		}
	}

	public static class InvalidDataException extends Exception {
		private static final long serialVersionUID = 19960331090200L;
		
	}
	//	public static class OutOfIndexException extends Exception {
//		OutOfIndexException(String content) {
//			super(content);
//			Log.log(content, Constant.Log.EXCEPTION);			
//		}
//		
//	}
}
