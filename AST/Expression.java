package simjava.AST;

import simjava.AST.Type.TypeName;
import simjava.Scanner.Token;

public abstract class Expression extends ASTNode {
	
	TypeName typename;
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

	public TypeName getTypename() {
		return typename;
	}

	public boolean setTypename(TypeName type) {
		try {
			if (type != null)
				this.typename = type;
			else
				this.typename = Type.getTypeName(firstToken);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
