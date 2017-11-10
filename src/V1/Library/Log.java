package V1.Library;

public class Log {
	public static void init() {
		log("Log init done.");
	}
	public static void log(Object content) {
		log(content, Constant.Log.NORMAL);
	}
	public static void log(Object content, String level) {
		System.out.println(level + content + "\u001b[00m");
	}
	
	public static void loghr(String content) {
		log(content);
		log("-----------------------------------------------");
	}
}
