package V1.Main;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Answer;
import V1.Component.Block;
import V1.Component.Input;
import V1.Component.NetworkObject;
import V1.Component.Output;
import V1.Component.Question;
import V1.Component.Report;
import V1.Component.Request;
import V1.Component.Transaction;
import V1.Component.Work;
import V1.Frontend.FrontendServer;
import V1.Library.Address;
import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Constant.Verify.Result;
import V1.Library.Crypto;
import V1.Library.Log;
import V1.Library.Verify;
import net.arnx.jsonic.web.WebServiceServlet.JSON;

public class DataManager {
	private static Work work;
	static void init() {
		byte[] zero = new byte[Constant.Work.BYTE_MAX_HASH];
		byte[] target = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
		byte[] subTarget = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_SUB_TARGET);
		work = new Work(zero, target, subTarget, new byte[1]);
		Log.log("DataManager init done.");
	}

	static void updateMining(Block block) {
		Log.log("[DataManager.updateMining()] blockHeader: " + block.getBlockHeader());
		try {
			work = new Work(Crypto.hash512(ByteUtil.getByteObject(block.getBlockHeader())), block.getTarget(), block.getSubTarget(), new byte[1]);
			BackendServer.shareFrontend(new NetworkObject(Constant.NetworkObject.TYPE_WORK, work));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[DataManager.updateMining()] Invalid block", Constant.Log.EXCEPTION);
		}
	}
	static Result verifyMining(Report report) {
		return Verify.verifyMining(work, report);
	}
	static boolean verifyRequest(Request request) {
		return Verify.verifyRequest(request);
	}
	static Transaction makeTx(Request request) {
		int[] amountFrom = request.getAmountFrom();
		int[] amountTo = request.getAmountTo();
		String[] addrFromStr = request.getAddrFrom();
		String[] addrToStr = request.getAddrTo();
		String[][] outHashStr = request.getOutHash();
		String[][] answerScriptStr = request.getAnswerScript();
		String[][] questionScriptStr = request.getQuestionScript();
		byte[][] addrFrom = new byte[addrFromStr.length][];
		byte[][] addrTo = new byte[addrToStr.length][];
		for (int i = 0; i < addrFromStr.length; i++) {
			addrFrom[i] = DatatypeConverter.parseHexBinary(addrFromStr[i]);
		}
		for (int i = 0; i < addrToStr.length; i++) {
			addrTo[i] = DatatypeConverter.parseHexBinary(addrToStr[i]);
		}
		byte[][][] outHash = new byte[outHashStr.length][][];
		for (int i = 0; i < outHashStr.length; i++) {
			outHash[i] = new byte[outHashStr[i].length][];
			for(int j = 0; j < outHashStr[i].length; j++) {
				outHash[i][j] = DatatypeConverter.parseHexBinary(outHashStr[i][j]);
			}
		}
		int answerScriptLength = 0;
		for(String[] arr: answerScriptStr) {
			answerScriptLength += arr.length;
		}
		byte[][] answerScript = new byte[answerScriptLength][];
		for (int i = 0; i < answerScriptLength; ) {
			for(String str: answerScriptStr[i]) {
				answerScript[i] = DatatypeConverter.parseHexBinary(str);
				i++;
			}
		}
		int questionScriptLength = 0;
		for(String[] arr: questionScriptStr) {
			questionScriptLength += arr.length;
		}
		byte[][] questionScript = new byte[questionScriptStr.length][];
		for (int i = 0; i < questionScriptStr.length; ) {
			for(String str: questionScriptStr[i]) {
				questionScript[i] = DatatypeConverter.parseHexBinary(str);
				i++;
			}
		}
		Input[] in = new Input[amountFrom.length];
		Output[] out = new Output[amountTo.length];
		int n = 0;
		for (int i = 0; i < in.length; i++) {
			for(int j = 0; j < outHash[i].length; j++) {
				in[i] = new Input(outHash[i][j], new Answer(answerScript[i], addrFrom[i]), amountFrom[n++]);
			}
		}
		for (int i = 0; i < out.length; i++) {
			out[i] = new Output(amountTo[i], new Question(questionScript[i], addrTo[i]));
		}
		int version = request.getVersion();
		int lockTime = request.getLockTime();
		byte[] signature = DatatypeConverter.parseHexBinary(request.getSignature());
		byte[] publicKey = DatatypeConverter.parseHexBinary(request.getPublicKey());
		Transaction tx = new Transaction(in, out, version, lockTime, signature, publicKey);
		tx.removeNull();
		return tx;
	}
}
