package simjava.AST;

import simjava.Scanner.Token;

public class ParamDec extends Dec {
	
	public ParamDec(Token firstToken, Token ident) {
		super(firstToken, ident);
	}

	@Override
	public String toString() {
		return "ParamDec [ident=" + ident + ", firstToken=" + firstToken + "]";
	}
	

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitParamDec(this,arg);
	}

}
