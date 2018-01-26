package V1.Experiment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import V1.Library.Constant;
import V1.Library.Log;

public class HttpManager {
	static String access(String ip, String request) {
		String body = "";
		try {
			Socket socket = new Socket();
			InetSocketAddress socketAddress = new InetSocketAddress(ip, Constant.Server.HASH_SERVER_PORT);
			socket.connect(socketAddress, 30000);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStream os = null;

//			Log.log("request: " + request);
			os = socket.getOutputStream();
			PrintStream ps = new PrintStream(os);
			ps.print(request);
			os.flush();

			InputStream is1 = socket.getInputStream();
			InputStreamReader ir1 = new InputStreamReader(is1, "UTF-8");
			BufferedReader br1 = new BufferedReader(ir1);
			String line;
			while ((line = br1.readLine()) != null) {
				body = line;
//				Log.log(line, Constant.Log.TEMPORARY);
			}

			baos.close();
			os.close();
			ir1.close();
			br1.close();
			is1.close();

			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return body;
	}
}
