package V1.Library;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import V1.Component.Answer;
import V1.Component.Question;
import V1.Component.Report;
import V1.Component.Work;
import V1.Library.Constant.Script.Result;
import V1.Library.Constant.Script.State;
import V1.Library.TofuException.StackException;

public class Script {
	public static Result resolve(final Question q, final Answer a, byte[] outHash) {
		// answer -- nonce fAddress cAddress
		// question -- blockHash target subTarget checkReward

		Stack stack = new Stack();
		try {
//			Log.log("answer script: " + DatatypeConverter.printHexBinary(a.getScript()));
//			Log.log("question script: " + DatatypeConverter.printHexBinary(q.getScript()));
			runAnswer(stack, a);
			runQuestion(stack, q, outHash);
			if (stack.getSp() == 1) {
				byte[] result = stack.pop();
				// Log.log("result: " +
				// DatatypeConverter.printHexBinary(result));
				if (result.length == 1 && result[0] == Constant.Script.OPCode.TRUE) {
					return Constant.Script.Result.SOLVED;
				}
			}
		} catch (AssertionError | StackException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Constant.Script.Result.FAILED;

	}

	private static void runQuestion(Stack stack, Question q, byte[] outHash)
			throws AssertionError, StackException, Exception {
		int i;
		int pushByte = 0;
		int popByte = 0;
		int registerIndex = 0;
		byte[][] register = new byte[Constant.Script.LENGTH_REGISTER][];
		Constant.Script.State state = State.OP;
		final byte[] questionScript = q.getScript();
		byte[] buf = null;
//		 Log.log("--------------------- runQuestion -------------------------");
		for (i = 0; i < questionScript.length && i < Constant.Script.BYTE_MAX_QUESTION; i++) {
//			 Log.log("state: " + state + ", op: " +Integer.toHexString(questionScript[i]) + ", stack: " + stack);
			switch (state) {
			case OP:
				if (questionScript[i] == Constant.Script.OPCode.PUBK_DUP) {
					buf = stack.pop();
					byte[] publicKey = Address.getPublicKeyFromByte(buf);
					stack.push(publicKey);
					stack.push(buf);
				} else if (questionScript[i] == Constant.Script.OPCode.PUSH_MAX_512) {
					state = State.PUSH_OPTION;
				} else if (questionScript[i] == Constant.Script.OPCode.HASH_TWICE) {
					buf = stack.pop();
					buf = Crypto.hashTwice(buf);
					stack.push(buf);
					// } else if (questionScript[i] ==
					// Constant.Script.OPCode.PUSH256) {
					// state = State.PUSH;
					// pushByte = 256 / 8;
					// } else if (questionScript[i] ==
					// Constant.Script.OPCode.EQUAL) {
					// buf = stack.pop();
					// if (ByteBuffer.wrap(buf) != ByteBuffer.wrap(stack.pop()))
					// {
					// stack.push(new byte[] { Constant.Script.OPCode.TRUE });
					// }
				} else if (questionScript[i] == Constant.Script.OPCode.CHECK_ADDR) {
					buf = stack.pop();
					if (!(ByteBuffer.wrap((buf)).equals(ByteBuffer.wrap(q.getReceiver())))) {
						throw new StackException("Address check false");
					}
				} else if (questionScript[i] == Constant.Script.OPCode.CHECK_REWARD) {
					byte[] subTarget = stack.pop();
					byte[] target = stack.pop();
					byte[] blockHash = stack.pop();
					byte[] nonce = stack.pop();
					byte[] fAddress = stack.pop();
					byte[] cAddress = stack.pop();
					byte[] resultHash = stack.pop();
					// public Work(byte[] hash, byte[] target, byte[] subTarget,
					// byte[] fAddress) {
					// public Report(String hash, String nonce, String result,
					// String cAddress, String fAddress, String signature,
					// String publicKey) {
					V1.Library.Constant.Verify.Result result = Verify.verifyMining(new Work(blockHash, target, subTarget, fAddress),
							new Report(DatatypeConverter.printHexBinary(blockHash),
									DatatypeConverter.printHexBinary(nonce), DatatypeConverter.printHexBinary(resultHash),
									DatatypeConverter.printHexBinary(cAddress),
									DatatypeConverter.printHexBinary(fAddress), "", ""));
					if(result == V1.Library.Constant.Verify.Result.SUB_TARGET || result == V1.Library.Constant.Verify.Result.TARGET) {
						stack.push(new byte[] { Constant.Script.OPCode.TRUE });						
					} else {
						stack.push(new byte[] { Constant.Script.OPCode.FALSE });
					}
					return ;
				} else if (questionScript[i] == Constant.Script.OPCode.EQUAL_VERIFY) {
					buf = stack.pop();
					// Log.log(DatatypeConverter.printHexBinary(buf));
					if (!(ByteBuffer.wrap((buf)).equals(ByteBuffer.wrap(stack.pop())))) {
						throw new StackException("Equal verify false");
					}
				} else if (questionScript[i] == Constant.Script.OPCode.CHECK_SIG) {
					buf = stack.pop();
					// Log.log("buf : " +
					// DatatypeConverter.printHexBinary(buf));
					// Log.log("outHash: " +
					// DatatypeConverter.printHexBinary(outHash));
					byte[] tmp = stack.pop();
					// Log.log("stack.pop() (sign): " +
					// DatatypeConverter.printHexBinary(tmp));
					boolean result = Crypto.verify(buf, DatatypeConverter.printHexBinary(outHash).getBytes(), tmp);
					// Log.log("result: " + result, Constant.Log.TEMPORARY);
					if (result) {
						stack.push(new byte[] { Constant.Script.OPCode.TRUE });
					} else {
						stack.push(new byte[] { Constant.Script.OPCode.FALSE });
					}
				} else if (questionScript[i] == Constant.Script.OPCode.POP0) {
					register[0] = stack.pop();
				} else if (questionScript[i] == Constant.Script.OPCode.TRUE) {
					stack.push(new byte[] { Constant.Script.OPCode.TRUE });
				} else {
					// Log.log("Invalid OPCode: " +
					// Integer.toHexString(questionScript[i]),
					// Constant.Log.EXCEPTION);
					return;
				}
				break;
			case PUSH:
				state = stackPush(stack, questionScript, i, pushByte);
				i += pushByte - 1;
				break;
			// case POP:
			// state = stackPop(stack, popByte, register[registerIndex]);
			// i--;
			// break;
			case PUSH_OPTION:
				pushByte = 4;
				state = stackPush(stack, questionScript, i, pushByte);
				state = State.PUSH;
				pushByte = ByteBuffer.wrap(stack.pop()).getInt();
				i += 3;
				break;
			case END:
				break;
			}
		}
		// Log.log("state: " + state + ", stack: " + stack);
	}

	private static void runAnswer(Stack stack, Answer a) throws AssertionError, StackException {
		int i, j;
		int pushByte = 0;
		Constant.Script.State state = State.OP;
		final byte[] answerScript = a.getScript();
//		 Log.log("--------------------- runAnswer -------------------------");
		for (i = 0; i < answerScript.length && i < Constant.Script.BYTE_MAX_ANSWER; i++) {
//			 Log.log("state: " + state + ", op: " +
//			 Integer.toHexString(answerScript[i]) + ", stack: " + stack);
			switch (state) {
			case OP:
				if (answerScript[i] == Constant.Script.OPCode.FALSE) {
					throw new StackException("OP FALSE");
				} else if (answerScript[i] == Constant.Script.OPCode.PUSH512) {
					state = State.PUSH;
					pushByte = 512 / 8;
				} else if (answerScript[i] == Constant.Script.OPCode.PUSH_MAX_512) {
					state = State.PUSH_OPTION;
				} else {
					// Log.log("Invalid OPCode: " +
					// Integer.toHexString(answerScript[i]),
					// Constant.Log.EXCEPTION);
					return;
				}
				break;
			case PUSH:
				state = stackPush(stack, answerScript, i, pushByte);
				i += pushByte - 1;
				break;
			case PUSH_OPTION:
				pushByte = 4;
				state = stackPush(stack, answerScript, i, pushByte);
				state = State.PUSH;
				pushByte = ByteBuffer.wrap(stack.pop()).getInt();
				i += 3;
				break;
			}
		}
		// Log.log("state: " + state + ", stack: " + stack);
	}

	private static State stackPush(Stack stack, byte[] script, int index, int pushByte) throws StackException {
		ByteBuffer bb = ByteBuffer.allocate(pushByte);
		bb.put(script, index, pushByte);
		stack.push(bb.array());
		return State.OP;
	}
	// State stackPop(Stack stack, int length, byte[] data) throws
	// StackException {
	// data = stack.pop();
	// if(length == -1 && data.length == length) {
	// return State.OP;
	// } else {
	// throw new StackException("POP data length is wrong");
	// }
	// }
}
