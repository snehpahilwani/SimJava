package simjava.AST;

import simjava.AST.Type.TypeName;
import simjava.Scanner.Token;

public class Dec extends ASTNode {
	
	final Token ident;
	TypeName typeName;
	int slotNumber;
	
	

	public Dec(Token firstToken, Token ident) {
		super(firstToken);

		this.ident = ident;
	}

	
	

	public void setSlotNumber(int slotNumber) {
		this.slotNumber = slotNumber;
	}




	public int getSlotNumber() {
		return slotNumber;
	}


	public Token getIdent() {
		return ident;
	}

	@Override
	public String toString() {
		return "Dec [ident=" + ident + ", firstToken=" + firstToken + "]";
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ident == null) ? 0 : ident.hashCode());
		return result;
	}
	
	

	public TypeName getTypeName() {
		return typeName;
	}

	public boolean setTypename(TypeName type) {
		try {
			if (type != null)
				this.typeName = type;
			else
				this.typeName = Type.getTypeName(firstToken);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Dec)) {
			return false;
		}
		Dec other = (Dec) obj;
		if (ident == null) {
			if (other.ident != null) {
				return false;
			}
		} else if (!ident.equals(other.ident)) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitDec(this,arg);
	}

}
