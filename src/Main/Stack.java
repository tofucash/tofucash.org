package Main;

import java.util.ArrayList;
import java.util.List;

import Main.TofuException.StackException;

public class Stack {
	private int sp;
	private List<byte[]> stack = new ArrayList<byte[]>();
	Stack() {
		sp = 0;
	}
	int getSp() {
		return sp;
	}
	void push(byte[] data) throws StackException {
		if(sp < Constant.Stack.LENGTH_MAX_STACK) {
			stack.add(data);
			return ;
		} else {
			throw new TofuException.StackException("Stack Pointer is out of range");
		}
	}
	byte[] pop() throws StackException {
		if(sp > 0) {
			return stack.get(--sp);
		} else {
			throw new TofuException.StackException("Stack Pointer is out of range");
		}
	}
	void duplicate() throws StackException {
		if(sp > 0 && sp < Constant.Stack.LENGTH_MAX_STACK) {
			stack.add(stack.get(-1 + sp++));
		} else {
			throw new TofuException.StackException("Stack Pointer is out of range");
		}		
	}
	
}
