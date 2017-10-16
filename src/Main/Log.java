package Main;

public class Log {
	static void init() {
		log("Log init done.");
	}
	static void logerr(String content) {
		
	}
	static void log(String content) {
		System.out.println(content);
	}
	static void log(String content, String level) {
		log(level + content + "\u001b[00m");
	}
	
	static void loghr(String content) {
		log(content);
		log("-----------------------------------------------");
	}
}
