package simjava;

import static simjava.Scanner.Kind.EQUAL;
import static simjava.Scanner.Kind.GE;
import static simjava.Scanner.Kind.GT;
import static simjava.Scanner.Kind.KW_SCREENHEIGHT;
import static simjava.Scanner.Kind.KW_SCREENWIDTH;
import static simjava.Scanner.Kind.LE;
import static simjava.Scanner.Kind.LT;
import static simjava.Scanner.Kind.NOTEQUAL;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import simjava.AST.ASTVisitor;
import simjava.AST.AssignmentStatement;
import simjava.AST.BinaryChain;
import simjava.AST.BinaryExpression;
import simjava.AST.Block;
import simjava.AST.BooleanLitExpression;
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
import simjava.Scanner.Token;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	int slotNumber = 1;
	int cmdArg = 0;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, 1);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		// TODO visit the local variables
		for (Dec dec : program.getB().getDecs()) {
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, startRun, endRun,
					dec.getSlotNumber());
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		cw.visitEnd();// end of class
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypename());
		IdentLValue assignVar = assignStatement.getVar();
		assignVar.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().setLeftSide(true);
		binaryChain.getE0().visit(this, null);

		TypeName binaryChainTypeLeft = binaryChain.getE0().getTypename();
		switch (binaryChainTypeLeft) {
		case URL:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",
					PLPRuntimeImageIO.readFromURLSig, false);
			break;
		case FILE:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",
					PLPRuntimeImageIO.readFromFileDesc, false);
			break;
		default:
			break;
		}

		Dec binDec = (Dec) binaryChain.getE1().visit(this, binaryChain.getArrow());
		if (arg != null && (int) arg == 0) {
			mv.visitInsn(POP);
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		e0.visit(this, arg);
		e1.visit(this, arg);

		Label binExpStart = new Label();
		Label binExpEnd = new Label();
		Kind opKind = binaryExpression.getOp().kind;
		switch (opKind) {
		case PLUS:
			if (e0.getTypename() == TypeName.INTEGER && e1.getTypename() == TypeName.INTEGER) {
				mv.visitInsn(IADD);
			} else if (e0.getTypename() == TypeName.IMAGE && e1.getTypename() == TypeName.IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			}
			break;
		case MINUS:
			if (e0.getTypename() == TypeName.INTEGER && e1.getTypename() == TypeName.INTEGER) {
				mv.visitInsn(ISUB);
			} else if (e0.getTypename() == TypeName.IMAGE && e1.getTypename() == TypeName.IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}
			break;
		case DIV:
			if (e0.getTypename() == TypeName.INTEGER && e1.getTypename() == TypeName.INTEGER) {
				mv.visitInsn(IDIV);
			} else if (e0.getTypename() == TypeName.IMAGE && e1.getTypename() == TypeName.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
			}
			break;
		case TIMES:
			if (e0.getTypename() == TypeName.INTEGER && e1.getTypename() == TypeName.INTEGER) {
				mv.visitInsn(IMUL);
			} else if (e0.getTypename() == TypeName.INTEGER && e1.getTypename() == TypeName.IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			} else if (e0.getTypename() == TypeName.IMAGE && e1.getTypename() == TypeName.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}
			break;
		case MOD:
			if (e0.getTypename() == TypeName.INTEGER && e1.getTypename() == TypeName.INTEGER) {
				mv.visitInsn(IREM);
			} else if (e0.getTypename() == TypeName.INTEGER && e1.getTypename() == TypeName.IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			} else if (e0.getTypename() == TypeName.IMAGE && e1.getTypename() == TypeName.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			}
			break;
		case AND:
			if (e0.getTypename() == TypeName.BOOLEAN && e1.getTypename() == TypeName.BOOLEAN) {
				mv.visitInsn(IAND);
			}
			break;
		case OR:
			if (e0.getTypename() == TypeName.BOOLEAN && e1.getTypename() == TypeName.BOOLEAN) {
				mv.visitInsn(IOR);
			}
			break;
		case LT:
			mv.visitJumpInsn(IF_ICMPGE, binExpStart);
			mv.visitLdcInsn(true);
			break;
		case LE:
			mv.visitJumpInsn(IF_ICMPGT, binExpStart);
			mv.visitLdcInsn(true);
			break;
		case GT:
			mv.visitJumpInsn(IF_ICMPLE, binExpStart);
			mv.visitLdcInsn(true);
			break;
		case GE:
			mv.visitJumpInsn(IF_ICMPLT, binExpStart);
			mv.visitLdcInsn(true);
			break;
		case EQUAL:
			if (binaryExpression.getE0().getTypename().equals(TypeName.INTEGER)
					|| binaryExpression.getE0().getTypename().equals(TypeName.BOOLEAN))
				mv.visitJumpInsn(IF_ICMPNE, binExpStart);
			else
				mv.visitJumpInsn(IF_ACMPNE, binExpStart);
			mv.visitLdcInsn(true);
			break;
		case NOTEQUAL:
			if (binaryExpression.getE0().getTypename().equals(TypeName.INTEGER)
					|| binaryExpression.getE0().getTypename().equals(TypeName.BOOLEAN))
				mv.visitJumpInsn(IF_ICMPEQ, binExpStart);
			else
				mv.visitJumpInsn(IF_ACMPEQ, binExpStart);
			mv.visitLdcInsn(true);
			break;
		default:
			break;
		}
		if (opKind == LT || opKind == LE || opKind == GT || opKind == GE || opKind == EQUAL || opKind == NOTEQUAL) {
			mv.visitJumpInsn(GOTO, binExpEnd);
			mv.visitLabel(binExpStart);
			mv.visitLdcInsn(false);
			mv.visitLabel(binExpEnd);
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Implement this
		Label blockStart = new Label();
		mv.visitLabel(blockStart);
		for (Dec dec : block.getDecs()) {
			dec.visit(this, arg);
		}
		for (Statement stmt : block.getStatements()) {
			stmt.visit(this, 0);
		}
		Label blockEnd = new Label();
		mv.visitLabel(blockEnd);
		if (arg == null || (int) arg == 0) {
			for (Dec dec : block.getDecs()) {
				mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, blockStart,
						blockEnd, dec.getSlotNumber());
			}
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		Kind kind = constantExpression.firstToken.kind;
		if (kind == KW_SCREENWIDTH) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
					PLPRuntimeFrame.getScreenWidthSig, false);
		} else if (kind == KW_SCREENHEIGHT) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
					PLPRuntimeFrame.getScreenHeightSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {

		declaration.setSlotNumber(slotNumber++);
		if (declaration.getTypeName() == TypeName.FRAME) {
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlotNumber());
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		Kind filterOpKind = filterOpChain.firstToken.kind;
		switch (filterOpKind) {
		case OP_BLUR:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
			break;
		case OP_CONVOLVE:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig,
					false);
			break;
		case OP_GRAY:
			if (((Token) arg).kind == Kind.BARARROW)
				mv.visitInsn(DUP);
			else
				mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		Kind frameOpKind = frameOpChain.firstToken.kind;
		switch (frameOpKind) {
		case KW_SHOW:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc,
					false);
			break;
		case KW_HIDE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc,
					false);
			break;
		case KW_MOVE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,
					false);
			break;
		case KW_XLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc,
					false);
			break;
		case KW_YLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc,
					false);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		TypeName identChainType = identChain.getTypename();
		if (identChain.getDec() instanceof ParamDec) {
			if (identChain.isLeftSide()) {
				mv.visitFieldInsn(GETSTATIC, className, identChain.getDec().getIdent().getText(),
						identChain.getDec().getTypeName().getJVMTypeDesc());
			} else {
				if (identChain.getTypename() == TypeName.FILE) {
					mv.visitFieldInsn(GETSTATIC, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getTypeName().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
				} else {
					mv.visitFieldInsn(PUTSTATIC, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getTypeName().getJVMTypeDesc());
				}
				return identChain.getDec();
			}
		} else {
			if (identChain.isLeftSide()) {
				switch (identChainType) {

				case INTEGER:
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlotNumber());
					break;
				case IMAGE:
				case FRAME:
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());
					break;
				default:
					break;
				}
			} else {
				switch (identChainType) {
				case INTEGER:
					mv.visitVarInsn(ISTORE, identChain.getDec().getSlotNumber());
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlotNumber());
					break;
				case IMAGE:
					mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNumber());
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());
					break;
				case FRAME:
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
							PLPRuntimeFrame.createOrSetFrameSig, false);
					mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNumber());
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());

					break;
				default:
					break;
				}
				return identChain.getDec();
			}
		}

		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec identDec = identExpression.getDec();
		if (identDec instanceof ParamDec) {
			if (identDec.getTypeName() == TypeName.BOOLEAN) {
				mv.visitFieldInsn(GETSTATIC, className, identExpression.getFirstToken().getText(), "Z");
			} else if (identDec.getTypeName() == TypeName.INTEGER) {
				mv.visitFieldInsn(GETSTATIC, className, identExpression.getFirstToken().getText(), "I");
			}
		} else {
			if (identDec.getTypeName() == TypeName.INTEGER || identDec.getTypeName() == TypeName.BOOLEAN) {
				mv.visitVarInsn(ILOAD, identDec.getSlotNumber());
			} else {
				mv.visitVarInsn(ALOAD, identDec.getSlotNumber());
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec identDec = identX.getDec();
		if (identDec instanceof ParamDec) {
			if (identDec.getTypeName() == TypeName.BOOLEAN) {
				mv.visitFieldInsn(PUTSTATIC, className, identX.getText(), "Z");
			} else if (identDec.getTypeName() == TypeName.INTEGER) {
				mv.visitFieldInsn(PUTSTATIC, className, identX.getText(), "I");
			}
		} else {
			if (identDec.getTypeName() == TypeName.INTEGER || identDec.getTypeName() == TypeName.BOOLEAN) {
				mv.visitVarInsn(ISTORE, identDec.getSlotNumber());
			} else {
				if (identDec.getTypeName() == TypeName.IMAGE) {
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
							PLPRuntimeImageOps.copyImageSig, false);
				}
				mv.visitVarInsn(ASTORE, identDec.getSlotNumber());
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.getE().visit(this, arg);
		Label label = new Label();
		mv.visitJumpInsn(IFEQ, label);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(label);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		Kind imageOpKind = imageOpChain.firstToken.kind;
		switch (imageOpKind) {
		case OP_WIDTH:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth",
					PLPRuntimeImageOps.getWidthSig, false);
			break;
		case OP_HEIGHT:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight",
					PLPRuntimeImageOps.getHeightSig, false);
			break;
		case KW_SCALE:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(intLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		TypeName paramDecType = paramDec.getTypeName();
		if (paramDecType == TypeName.INTEGER) {
			FieldVisitor fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), "I", null, null);
			fv.visitEnd();
		} else if (paramDecType == TypeName.BOOLEAN) {
			FieldVisitor fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), "Z", null, null);
			fv.visitEnd();
		} else if (paramDecType == TypeName.URL) {
			FieldVisitor fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), "Ljava/net/URL;", null, null);
			fv.visitEnd();
		} else if (paramDecType == TypeName.FILE) {
			FieldVisitor fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), "Ljava/io/File;", null, null);
			fv.visitEnd();
		}
		mv.visitVarInsn(ALOAD, 0);
		if (paramDecType == TypeName.INTEGER) {
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(cmdArg++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "I");
		} else if (paramDecType == TypeName.BOOLEAN) {
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(cmdArg++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Z");
		} else if (paramDecType == TypeName.FILE) {
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(cmdArg++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		} else if (paramDecType == TypeName.URL) {
			mv.visitVarInsn(ALOAD, 1);// args
			mv.visitLdcInsn(cmdArg++);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}

		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Expression e = sleepStatement.getE();
		e.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression expr : tuple.getExprList()) {
			expr.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label guard = new Label();
		Label body = new Label();
		mv.visitJumpInsn(GOTO, guard);
		mv.visitLabel(body);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(guard);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, body);
		return null;
	}

}
