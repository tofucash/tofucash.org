package V1.Library;

public class TofuError {
	public static class SettingError extends Error {
		private static final long serialVersionUID = 19960331080200L;
		public SettingError(String content) {
			super(content);
			Log.log(content, Constant.Log.EXCEPTION);			
		}
	}
	
	public static class UnimplementedError extends Error {
		private static final long serialVersionUID = 19960331080300L;

		public UnimplementedError(String content) {
			super(content);
			Log.log(content, Constant.Log.EXCEPTION);			
		}
	}
}
