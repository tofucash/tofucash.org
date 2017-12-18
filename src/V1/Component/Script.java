package V1.Component;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import V1.Library.ByteUtil;
import V1.Library.Constant;
import V1.Library.Crypto;
import V1.Library.IO;
import V1.Library.Log;
import V1.Library.Stack;
import V1.Library.TofuException;
import V1.Library.Constant.Script.OPCode;
import V1.Library.Constant.Script.Result;
import V1.Library.Constant.Script.State;
import V1.Library.TofuException.StackException;

public class Script {
	public Result resolve(final Question q, final Answer a, final Transaction tx, int index) {
		Stack stack;

		stack = new Stack();
		try {
			System.out.println("answer script: "+DatatypeConverter.printHexBinary(a.getScript()));
			System.out.println("question script: "+DatatypeConverter.printHexBinary(q.getScript()));
			runAnswer(stack, a);
			runQuestion(stack, q, tx, index);
			if(stack.getSp() == 1) {
				byte[] result = stack.pop();
				System.out.println("result: "+DatatypeConverter.printHexBinary(result));
				if(result.length == 1 && result[0] == Constant.Script.OPCode.TRUE) {
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

	private void runQuestion(Stack stack, Question q, Transaction tx, int index) throws AssertionError, StackException, Exception {
		int i;
		int pushByte = 0;
		int popByte = 0;
		int registerIndex = 0; 
		byte[][] register = new byte[Constant.Script.LENGTH_REGISTER][];
		Constant.Script.State state = State.OP;
		final byte[] questionScript = q.getScript();
		byte[] buf = null;
		System.out.println("--------------------- runQuestion -------------------------");
		for (i = 0; i < questionScript.length && i < Constant.Script.BYTE_MAX_QUESTION; i++) {
			System.out.println("state: "+ state + ", op: " +questionScript[i]+ ", stack: "+stack);
			switch(state) {
			case OP:
				if(questionScript[i] == Constant.Script.OPCode.PUBK_DUP) {
					byte[] publicKey = new byte[Constant.Address.BYTE_PUBLIC_KEY_PREFIX + Constant.Address.BYTE_PUBLIC_KEY];
					System.arraycopy(DatatypeConverter.parseHexBinary(Constant.Address.PUBLIC_KEY_PREFIX), 0, publicKey, 0, Constant.Address.BYTE_PUBLIC_KEY_PREFIX);
					buf = stack.pop();
					System.arraycopy(buf, 0, publicKey, Constant.Address.BYTE_PUBLIC_KEY_PREFIX, Constant.Address.BYTE_PUBLIC_KEY);
					stack.push(publicKey);
					stack.duplicate();
				} else if(questionScript[i] == Constant.Script.OPCode.HASH_TWICE) {
					buf = stack.pop();
					buf = Crypto.hashTwice(buf);
					stack.push(buf);
				} else if(questionScript[i] == Constant.Script.OPCode.PUSH256) {
					state = State.PUSH;
					pushByte = 256 / 8;
				} else if(questionScript[i] == Constant.Script.OPCode.EQUAL) {
					buf = stack.pop();
					if( ByteBuffer.wrap(buf) != ByteBuffer.wrap(stack.pop())) {
						stack.push(new byte[] {Constant.Script.OPCode.TRUE});
					}
				} else if(questionScript[i] == Constant.Script.OPCode.EQUAL_VERIFY) {
					buf = stack.pop();
					System.out.println(DatatypeConverter.printHexBinary(buf));
//					byte[] buf2 = stack.pop();
//					System.out.println(DatatypeConverter.printHexBinary(buf2));
					if( !Arrays.equals(buf, stack.pop())) {
						throw new StackException("Equal verify false");
					}
				} else if(questionScript[i] == Constant.Script.OPCode.CHECK_SIG) {
					buf = stack.pop();
					Transaction signData = new Transaction(tx.getIn(), tx.getOut(), tx.getVersion(), tx.getLockTime(), buf);
					boolean result = Crypto.verify(buf, ByteUtil.getByteObject(signData), tx.getSignature());
					Log.log("result: "+result, Constant.Log.TEMPORARY);
					if(result) {
						stack.push(new byte[] {Constant.Script.OPCode.TRUE});
					} else {
						stack.push(new byte[] {Constant.Script.OPCode.FALSE});						
					}
				} else if(questionScript[i] == Constant.Script.OPCode.POP1_0) {
					register[0] = stack.pop();
				} else if(questionScript[i] == Constant.Script.OPCode.TRUE) {
					stack.push(new byte[] {Constant.Script.OPCode.TRUE});
				}
				break;
			case PUSH:
				state = stackPush(stack, questionScript, i, pushByte);
				i += pushByte - 1;
				break;
//			case POP:
//				state = stackPop(stack, popByte, register[registerIndex]);
//				i--;
//				break;
			case END:
				break;
			}
		}
		System.out.println("state: "+ state + ", stack: "+stack);
	}

	private void runAnswer(Stack stack, Answer a) throws AssertionError, StackException {
		int i, j;
		int pushByte = 0;
		Constant.Script.State state = State.OP;
		final byte[] answerScript = a.getScript();
		System.out.println("--------------------- runAnswer -------------------------");
		for (i = 0; i < answerScript.length && i < Constant.Script.BYTE_MAX_ANSWER; i++) {
			System.out.println("state: "+ state + ", op: " +answerScript[i]+ ", stack: "+stack);
			switch (state) {
			case OP:
				if (answerScript[i] == Constant.Script.OPCode.FALSE) {
					throw new StackException("OP FALSE");
				} else if (answerScript[i] == Constant.Script.OPCode.PUSH512) {
					state = State.PUSH;
					pushByte = 512 / 8;
				} else if (answerScript[i] == Constant.Script.OPCode.PUSH256) {
					state = State.PUSH;
					pushByte = 256 / 8;
//				} else if (answerScript[i] == Constant.Script.OPCode.PUSH160) {
//					state = State.PUSH;
//					pushByte = 160;
//				} else if (answerScript[i] == Constant.Script.OPCode.PUSH128) {
//					state = State.PUSH;
//					pushByte = 128;
//				} else if (answerScript[i] == Constant.Script.OPCode.PUSH64) {
//					state = State.PUSH;
//					pushByte = 64;
				} else if (answerScript[i] == Constant.Script.OPCode.PUSH32) {
					state = State.PUSH;
					pushByte = 32 / 8;
				} else {

				}
				break;
			case PUSH:
				state =  stackPush(stack, answerScript, i, pushByte);
				i += pushByte - 1;
				break;
			}
		}
		System.out.println("state: "+ state + ", stack: "+stack);
	}
	State stackPush(Stack stack, byte[] script, int index, int pushByte) throws StackException {
		ByteBuffer bb = ByteBuffer.allocate(pushByte);
		bb.put(script, index, pushByte);
		stack.push(bb.array());
		return State.OP;
	}
//	State stackPop(Stack stack, int length, byte[] data) throws StackException {
//		data = stack.pop();
//		if(length == -1 && data.length == length) {
//			return State.OP;
//		} else {
//			throw new StackException("POP data length is wrong");
//		}
//	}
}
