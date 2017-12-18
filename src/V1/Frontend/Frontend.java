package V1.Frontend;

import V1.Library.TofuError;

public class Frontend {
	public static void main(String[] args) {
		init();
		FrontendServer server = new FrontendServer();
		server.start();
		HashServer hashServer = new HashServer();
		hashServer.start();
	}
	static void init() {
		Setting.init();
		MiningManager.init();
		HashServer.init();
		try {
			FrontendServer.init();
		} catch (Exception e) {
			e.printStackTrace();
			throw new TofuError.SettingError("Server init failed.");
		}
	}
}
