package simjava.AST;

import simjava.AST.Type.TypeName;
import simjava.Scanner.Token;


public abstract class Chain extends Statement {
	
	TypeName typename;
	public boolean isLeftSide;
	
	
	public boolean isLeftSide() {
		return isLeftSide;
	}

	public void setLeftSide(boolean isLeftSide) {
		this.isLeftSide = isLeftSide;
	}

	public Chain(Token firstToken) {
		super(firstToken);
	}

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
