package Main;

import Main.TofuException.StackException;

import java.nio.ByteBuffer;
import java.util.Arrays;

import Main.Constant.Script.State;

public class Script {
	// https://en.bitcoin.it/wiki/Script
	Constant.Script.Result resolve(final Question q, final Answer a, final Transaction tx, int index) {
		Stack stack;

		stack = new Stack();
		try {
			runAnswer(stack, a);
			runQuestion(stack, q, tx, index);
			if(stack.getSp() == 1) {
				byte[] result = stack.pop();
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

	void runQuestion(Stack stack, Question q, Transaction tx, int index) throws AssertionError, StackException {
		int i;
		int pushByte = 0;
		Constant.Script.State state = State.OP;
		final byte[] questionScript = q.getScript();
		byte[] buf;
		for (i = 0; i < questionScript.length && i < Constant.Script.BYTE_MAX_QUESTION; i++) {
			switch(state) {
			case OP:
				if(questionScript[i] == Constant.Script.OPCode.DUP) {
					stack.duplicate();
				} else if(questionScript[i] == Constant.Script.OPCode.HASH_TWICE) {
					buf = stack.pop();
					buf = Crypto.hashTwice(buf);
					stack.push(buf);
				} else if(questionScript[i] == Constant.Script.OPCode.PUSH256) {
					state = State.PUSH;
					pushByte = 256;
				} else if(questionScript[i] == Constant.Script.OPCode.EQUAL_VERIFY) {
					buf = stack.pop();
					if( ! Arrays.equals(buf, stack.pop())) {
						throw new StackException("Equal verify false");
					}
				} else if(questionScript[i] == Constant.Script.OPCode.CHECK_SIG) {
					buf = stack.pop();
					Input[] in = tx.getIn();
					in[index].updateAnswer(new Answer(buf));
					Transaction signData = new Transaction(in, tx.getOut(), tx.getVersion(), tx.getLockTime());
					Crypto.verify(buf, Library.getByteObject(signData), stack.pop());
				}
				break;
			case PUSH:
				state =  stackPush(stack, questionScript, i, pushByte);
				break;
			}
		}
	}

	void runAnswer(Stack stack, Answer a) throws AssertionError, StackException {
		int i, j;
		int pushByte = 0;
		Constant.Script.State state = State.OP;
		final byte[] answerScript = a.getScript();
		for (i = 0; i < answerScript.length && i < Constant.Script.BYTE_MAX_ANSWER; i++) {
			switch (state) {
			case OP:
				if (answerScript[i] == Constant.Script.OPCode.FALSE) {
					throw new StackException("OP FALSE");
				} else if (answerScript[i] == Constant.Script.OPCode.PUSH512) {
					state = State.PUSH;
					pushByte = 512;
				} else if (answerScript[i] == Constant.Script.OPCode.PUSH256) {
					state = State.PUSH;
					pushByte = 256;
//				} else if (answerScript[i] == Constant.Script.OPCode.PUSH160) {
//					state = State.PUSH;
//					pushByte = 160;
//				} else if (answerScript[i] == Constant.Script.OPCode.PUSH128) {
//					state = State.PUSH;
//					pushByte = 128;
				} else {

				}
				break;
			case PUSH:
				state =  stackPush(stack, answerScript, i, pushByte);
				break;
			}
		}
	}
	State stackPush(Stack stack, byte[] script, int index, int pushByte) throws StackException {
		ByteBuffer bb = ByteBuffer.allocate(pushByte);
		bb.put(script, index, pushByte);
		stack.push(bb.array());
		return State.OP;
	}
}
