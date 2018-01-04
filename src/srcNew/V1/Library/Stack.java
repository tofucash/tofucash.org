package V1.Library;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import V1.Library.TofuException.StackException;

public class Stack {
	private int sp;
	private List<byte[]> stack = new ArrayList<byte[]>();
	public Stack() {
		sp = 0;
	}
	public int getSp() {
		return sp;
	}
	public void push(byte[] data) throws StackException {
		if(sp < Constant.Stack.LENGTH_MAX_STACK) {
			stack.add(data);
			sp++;
			return ;
		} else {
			throw new TofuException.StackException("Stack Pointer is out of range");
		}
	}
	public byte[] pop() throws StackException {
		if(sp > 0) {
			return stack.remove(--sp);
		} else {
			throw new TofuException.StackException("Stack Pointer is out of range");
		}
	}
	public void duplicate() throws StackException {
		if(sp > 0 && sp < Constant.Stack.LENGTH_MAX_STACK) {
			stack.add(stack.get(-1 + sp++));
		} else {
			throw new TofuException.StackException("Stack Pointer is out of range");
		}		
	}
	public String toString() {
		String str = "[[SP: " + sp + "]";
		for(int i = 0; i < sp; i++) {
			str += "["+ i + ": " + DatatypeConverter.printHexBinary(stack.get(i))+"]"; 
		}
		return str+"]";
	}
}
