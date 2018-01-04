package V1.Frontend;

import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Request;
import V1.Component.Transaction;
import V1.Library.Log;
import net.arnx.jsonic.web.WebServiceServlet.JSON;

public class RequestManager {
	static Request verifyRequest(String json) {
		try {
//			Map map = (Map) JSON.decode(json);
//			int type = (int) map.get("type");
//			int amount = (int) map.get("amount");
//			List addrFrom = (List) map.get("addrFrom");
//			List addrTo = (List) map.get("addrTo");
//			List txHash = (List) map.get("txHash");
//			List script = (List) map.get("script");
//			for()
//			byte[] addrFrom = DatatypeConverter.parseHexBinary((String) map.get("addrFrom"));
//			byte[] addrTo = DatatypeConverter.parseHexBinary((String) map.get("addrTo"));
//			byte[] txHash = DatatypeConverter.parseHexBinary((String) map.get("txHash"));
//			byte[] script = DatatypeConverter.parseHexBinary((String) map.get("script"));
//			return new Request(type, amount, addrFrom, addrTo, txHash, script);
			return (Request) JSON.decode(json, Request.class);
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[RequestManager.verifyRequest()] Not Request JSON");
		}
		return null;
	}
}
