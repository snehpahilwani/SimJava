package simjava.AST;

import simjava.Scanner.Token;

public class IdentChain extends ChainElem {
	
	public Dec dec;
	
	

	public Dec getDec() {
		return dec;
	}


	public void setDec(Dec dec) {
		this.dec = dec;
	}


	public IdentChain(Token firstToken) {
		super(firstToken);
	}


	@Override
	public String toString() {
		return "IdentChain [firstToken=" + firstToken + "]";
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentChain(this, arg);
	}

}
