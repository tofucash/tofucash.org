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
//		byte[] zero = new byte[Constant.Work.BYTE_MAX_HASH];
//		byte[] target = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
//		byte[] subTarget = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_SUB_TARGET);
//		work = new Work(zero, target, subTarget, new byte[1]);
		updateMining(Blockchain.getBlock());
		Log.log("DataManager init done.");
	}

	static void updateMining(Block block) {
		try {
			work = new Work(Crypto.hash512(ByteUtil.getByteObject(block.getBlockHeader())), block.getTarget(), block.getSubTarget(), new byte[1]);
			BackendServer.shareFrontend(new NetworkObject(Constant.NetworkObject.TYPE_WORK, work));
		} catch (Exception e) {
			e.printStackTrace();
			Log.log("[DataManager.updateMining()] Invalid block", Constant.Log.EXCEPTION);
		}
	}
	static Work getWork() {
		return work;
	}
	static Result verifyMining(Report report) {
		Result result = Verify.verifyMining(work, report);
		if(result == Result.TARGET || result == Result.SUB_TARGET) {
			// マイニングに成功したなら、これ以上今のWorkでハッシュを計算してもらう必要がない
			byte[] zero = new byte[Constant.Work.BYTE_MAX_HASH];
			byte[] target = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_TARGET);
			byte[] subTarget = DatatypeConverter.parseHexBinary(Constant.Block.DEFAULT_SUB_TARGET);
			work = new Work(zero, target, subTarget, new byte[1]);
//			BackendServer.shareFrontend(new NetworkObject(Constant.NetworkObject.TYPE_WORK, decoy));
		} else {
			// おかしいハッシュが来たなら正しい素材を渡し直す
			updateMining(Blockchain.getBlock());
		}
		return result;
	}
	static boolean verifyRequest(Request request) {
		return Verify.verifyRequest(request);
	}
	static Transaction makeTx(Request request) {
		int[][] amountFrom = request.getAmountFrom();
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
		byte[][][] answerScript = new byte[answerScriptStr.length][][];
		for (int i = 0; i < answerScriptStr.length; i++) {
			answerScript[i] = new byte[answerScriptStr[i].length][];
			for(int j = 0; j < answerScriptStr[i].length; j++) {
				answerScript[i][j] = DatatypeConverter.parseHexBinary(answerScriptStr[i][j]);
			}
		}
		byte[][] questionScript = new byte[questionScriptStr.length][];
		for (int i = 0; i < questionScriptStr.length; ) {
			for(String str: questionScriptStr[i]) {
				questionScript[i] = DatatypeConverter.parseHexBinary(str);
				i++;
			}
		}
		int inputLength = 0;
		for(int i = 0; i < outHash.length; i++) {
			for(int j = 0; j < outHash[i].length; j++) {
				inputLength++;
			}
		}
		Input[] in = new Input[inputLength];
		Output[] out = new Output[questionScriptStr.length];
		for (int i = 0, n = 0; n < in.length; i++) {
			for(int j = 0; j < outHash[i].length; j++) {
				in[n] = new Input(outHash[i][j],
						new Answer(answerScript[i][j], 
								addrFrom[i]), 
						amountFrom[i][j]);
				n++;
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
