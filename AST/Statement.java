package simjava.AST;

import simjava.Scanner.Token;

public abstract class Statement extends ASTNode {

	public Statement(Token firstToken) {
		super(firstToken);
	}

	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
