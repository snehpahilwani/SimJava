package simjava;


import simjava.AST.ASTVisitor;
import simjava.AST.AssignmentStatement;
import simjava.AST.BinaryChain;
import simjava.AST.BinaryExpression;
import simjava.AST.Block;
import simjava.AST.BooleanLitExpression;
import simjava.AST.Chain;
import simjava.AST.ChainElem;
import simjava.AST.ConstantExpression;
import simjava.AST.Dec;
import simjava.AST.Expression;
import simjava.AST.FilterOpChain;
import simjava.AST.FrameOpChain;
import simjava.AST.IdentChain;
import simjava.AST.IdentExpression;
import simjava.AST.IdentLValue;
import simjava.AST.IfStatement;
import simjava.AST.ImageOpChain;
import simjava.AST.IntLitExpression;
import simjava.AST.ParamDec;
import simjava.AST.Program;
import simjava.AST.SleepStatement;
import simjava.AST.Statement;
import simjava.AST.Tuple;
import simjava.AST.WhileStatement;
import simjava.AST.Type.TypeName;
import simjava.Scanner.Kind;
import simjava.Scanner.LinePos;
import simjava.Scanner.Token;

import java.util.ArrayList;

import static simjava.AST.Type.TypeName.*;
import static simjava.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain e0 = binaryChain.getE0();
		Token op = binaryChain.getArrow();
		ChainElem e1 = binaryChain.getE1();
		e0.visit(this, arg);
		e1.visit(this, arg);
		if (e0.getTypename() == TypeName.URL && e1.getTypename() == IMAGE && op.isKind(ARROW)) {
			binaryChain.setTypename(IMAGE);
		} else if (e0.getTypename() == TypeName.FILE && e1.getTypename() == IMAGE && op.isKind(ARROW)) {
			binaryChain.setTypename(IMAGE);
		} else if (e0.getTypename() == TypeName.FRAME && op.isKind(ARROW)) {
			if (e1 instanceof FrameOpChain && (e1.firstToken.isKind(KW_XLOC) || e1.firstToken.isKind(KW_YLOC))) {
				binaryChain.setTypename(INTEGER);
			} else if (e1 instanceof FrameOpChain && (e1.firstToken.isKind(KW_SHOW) || e1.firstToken.isKind(KW_HIDE)
					|| e1.firstToken.isKind(KW_MOVE))) {
				binaryChain.setTypename(FRAME);
			} else {
				throw new TypeCheckException("Binary Chain Expression wrong!");
			}

		} else if (e0.getTypename() == TypeName.IMAGE && op.isKind(ARROW)) {
			if (e1 instanceof ImageOpChain && (e1.firstToken.isKind(OP_WIDTH) || e1.firstToken.isKind(OP_HEIGHT))) {
				binaryChain.setTypename(INTEGER);
			} else if (e1.getTypename() == TypeName.FRAME) {
				binaryChain.setTypename(FRAME);
			} else if (e1.getTypename() == TypeName.FILE) {
				binaryChain.setTypename(NONE);
			} else if (e1 instanceof FilterOpChain && (e1.firstToken.isKind(OP_GRAY) || e1.firstToken.isKind(OP_BLUR)
					|| e1.firstToken.isKind(OP_CONVOLVE))) {
				binaryChain.setTypename(IMAGE);
			} else if (e1 instanceof ImageOpChain && e1.firstToken.isKind(KW_SCALE)) {
				binaryChain.setTypename(IMAGE);

			} else if (e1 instanceof IdentChain && e1.getTypename() == INTEGER) {
				binaryChain.setTypename(IMAGE);
			} else if (e1 instanceof IdentChain && e1.getTypename() == IMAGE) {
				binaryChain.setTypename(IMAGE);
			}

			else {
				throw new TypeCheckException("Binary Chain Expression wrong!");
			}
		} else if (e0.getTypename() == TypeName.IMAGE && (op.isKind(ARROW) || op.isKind(BARARROW))) {
			if (e1 instanceof FilterOpChain && (e1.firstToken.isKind(OP_GRAY) || e1.firstToken.isKind(OP_BLUR)
					|| e1.firstToken.isKind(Kind.OP_CONVOLVE))) {
				binaryChain.setTypename(IMAGE);
			}

			else {
				throw new TypeCheckException("Binary Chain Expression wrong!");
			}
		} else if (e0.getTypename() == TypeName.INTEGER && op.isKind(ARROW)) {
			if (e1 instanceof IdentChain && e1.getTypename() == INTEGER) {
				binaryChain.setTypename(INTEGER);
			}
		} else {
			throw new TypeCheckException("Binary Chain Exception");
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		Token op = binaryExpression.getOp();
		e0.visit(this, arg);
		e1.visit(this, arg);
		if (e0.getTypename() == INTEGER && e1.getTypename() == INTEGER
				&& (op.isKind(PLUS) || op.isKind(MINUS) || op.isKind(TIMES) || op.isKind(DIV) || op.isKind(MOD))) {
			binaryExpression.setTypename(INTEGER);
		} else if (e0.getTypename() == IMAGE && e1.getTypename() == IMAGE && (op.isKind(PLUS) || op.isKind(MINUS) || op.isKind(DIV))) {
			binaryExpression.setTypename(IMAGE);
		} else if (e0.getTypename() == INTEGER && e1.getTypename() == IMAGE && op.isKind(TIMES)) {
			binaryExpression.setTypename(IMAGE);
		} else if (e1.getTypename() == INTEGER && e0.getTypename() == IMAGE
				&& (op.isKind(TIMES) || op.isKind(DIV) || op.isKind(MOD))) {
			binaryExpression.setTypename(IMAGE);
		} else if (e0.getTypename() == INTEGER && e1.getTypename() == INTEGER
				&& (op.isKind(LT) || op.isKind(GT) || op.isKind(LE) || op.isKind(GE))) {
			binaryExpression.setTypename(BOOLEAN);
		} else if (e0.getTypename() == BOOLEAN && e1.getTypename() == BOOLEAN && (op.isKind(LT) || op.isKind(GT)
				|| op.isKind(LE) || op.isKind(GE) || op.isKind(AND) || op.isKind(OR))) {
			binaryExpression.setTypename(BOOLEAN);
		} else if (e0.getTypename() == e1.getTypename() && (op.isKind(EQUAL) || op.isKind(Kind.NOTEQUAL))) {
			binaryExpression.setTypename(BOOLEAN);
		} else {
			throw new TypeCheckException("Binary Expression invalidated.");
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		ArrayList<Dec> declist = block.getDecs();
		ArrayList<Statement> stmtlist = block.getStatements();
		for (Dec dec : declist) {
			dec.visit(this, null);
		}
		for (Statement stmt : stmtlist) {
			stmt.visit(this, null);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setTypename(BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		Tuple tuple = filterOpChain.getArg();
		tuple.visit(this, arg);
		if (tuple.getExprList().size() == 0) {
			filterOpChain.setTypename(IMAGE);
		} else {
			throw new TypeCheckException("FilterOpChain Tuple size should be zero");
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Tuple tuple = frameOpChain.getArg();
		Kind framekind = frameOpChain.firstToken.kind;
		tuple.visit(this, arg);
		if ((framekind == KW_SHOW || framekind == KW_HIDE) && tuple.getExprList().size() == 0) {
			frameOpChain.setTypename(NONE);
		} else if ((framekind == KW_XLOC || framekind == KW_YLOC) && tuple.getExprList().size() == 0) {
			frameOpChain.setTypename(INTEGER);
		} else if (framekind == KW_MOVE && tuple.getExprList().size() == 2) {
			frameOpChain.setTypename(NONE);
		} else {
			throw new TypeCheckException("Bug in Parser :P");
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec dec = symtab.lookup(identChain.getFirstToken().getText());
		if (dec != null) {
			identChain.setTypename(dec.getTypeName());
			identChain.setDec(dec);
		} else {
			throw new TypeCheckException("No declaration found for identifier");
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec dec = symtab.lookup(identExpression.getFirstToken().getText());
		if (dec != null) {
			identExpression.setTypename(dec.getTypeName());
			identExpression.setDec(dec);
		} else {
			throw new TypeCheckException("No declaration found for identifier expression");
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Expression e = ifStatement.getE();
		Block b = ifStatement.getB();
		e.visit(this, arg);

		if (!(e.getTypename() == BOOLEAN)) {

			throw new TypeCheckException("If Expression expects Boolean");
		}
		b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setTypename(INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Expression e = sleepStatement.getE();
		e.visit(this, arg);
		if (!(e.getTypename() == INTEGER)) {
			throw new TypeCheckException("Sleep Expression expects Integer");
		}

		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Expression e = whileStatement.getE();
		Block b = whileStatement.getB();
		e.visit(this, arg);
		if (!(e.getTypename() == BOOLEAN)) {
			throw new TypeCheckException("While Expression expects Boolean");
		}

		b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setTypename(null);
		if (!symtab.insert(declaration.getIdent().getText(), declaration)) {
			throw new TypeCheckException("Variable already declared");
		}

		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		ArrayList<ParamDec> paramlist = program.getParams();
		Block b = program.getB();
		for (ParamDec param : paramlist) {
			param.visit(this, null);
		}
		b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {

		IdentLValue identlval = assignStatement.getVar();
		Expression e = assignStatement.getE();
		identlval.visit(this, arg);
		e.visit(this, arg);
		if (!(identlval.dec.getTypeName() == e.getTypename())) {
			throw new TypeCheckException("Identifier type should be equal to expression type");
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec dec = symtab.lookup(identX.getFirstToken().getText());
		if (dec != null) {
			identX.setDec(dec);
		} else {
			throw new TypeCheckException("No declaration found for identifier Left Value");
		}
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		paramDec.setTypename(null);
		if (!symtab.insert(paramDec.getIdent().getText(), paramDec)) {
			throw new TypeCheckException("Variable already declared");
		}

		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setTypename(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Tuple tuple = imageOpChain.getArg();
		Kind framekind = imageOpChain.firstToken.kind;
		tuple.visit(this, arg);
		if ((framekind == OP_WIDTH || framekind == OP_HEIGHT) && tuple.getExprList().size() == 0) {
			imageOpChain.setTypename(INTEGER);
		} else if (framekind == KW_SCALE && tuple.getExprList().size() == 1) {
			imageOpChain.setTypename(IMAGE);
		} else {
			throw new TypeCheckException("Bug in Parser :P");
		}

		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression e : tuple.getExprList()) {
			e.visit(this, null);
			if (!(e.getTypename() == TypeName.INTEGER)) {
				throw new TypeCheckException("Expression should be INTEGER type");
			}

		}
		return null;
	}

}
