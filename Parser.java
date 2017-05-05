package simjava;

import simjava.AST.*;
import simjava.Scanner.Kind;
import simjava.Scanner.Token;

import static simjava.Scanner.Kind.*;

import java.util.ArrayList;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program program = program();
		matchEOF();
		return program;
	}

	public Expression expression() throws SyntaxException {
		Expression e0=null,e1=null;
		Token firstToken = t;
		e0 = term();
		while(t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) || t.isKind(EQUAL) || t.isKind(NOTEQUAL)){
			Token op = t;
			consume();
			e1 = term();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}		
		return e0;
	}

	public Expression term() throws SyntaxException {
		Expression e0=null,e1=null;
		Token firstToken = t;
		e0 = elem();
		while(t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR)){
			Token op = t;
			consume();
			e1 = elem();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}	
		return e0;
	}

	public Expression elem() throws SyntaxException {
		Expression e0=null,e1=null;
		Token firstToken = t;
		e0 = factor();
		while(t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)){
			Token op = t;
			consume();
			e1 = factor();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		return e0;
	}

	public Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Token firstToken = t;
		Expression e = null;
		switch (kind) {
		case IDENT: {
			e = new IdentExpression(firstToken);
			consume();
		}
			break;
		case INT_LIT: {
			e = new IntLitExpression(firstToken);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e = new BooleanLitExpression(firstToken);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e = new ConstantExpression(firstToken);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		default:
			throw new SyntaxException("Error parsing factor: Received " + t.kind + " Expected valid factor");
		}
		return e;
	}

	public Block block() throws SyntaxException {
		Token firstToken = t;
		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		match(LBRACE);
		while(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME) || t.isKind(OP_SLEEP)|| t.isKind(KW_WHILE) || 
				t.isKind(KW_IF) || t.isKind(ASSIGN)|| t.isKind(IDENT) || t.isKind(OP_BLUR) 
				|| t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) || t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) ||
			 t.isKind(KW_XLOC) || t.isKind(KW_YLOC) || t.isKind(KW_SCALE) || t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT)){
			if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME)){
				Dec newdec = dec();
				decs.add(newdec);
			}
			else{
				Statement newstmt = statement();
				statements.add(newstmt);
			}
		}
		match(RBRACE);
		Block block = new Block(firstToken, decs, statements);
		return block;
	}

	public Program program() throws SyntaxException {
		Token firstToken = t;
		ArrayList<ParamDec> paramList = new ArrayList<ParamDec>();
		ParamDec paramdec = null;
		Block b = null;
		if(t.isKind(IDENT) && scanner.peek().kind.equals(LBRACE)){
			match(IDENT);
			b = block();
		}
		else if(t.isKind(IDENT)){
			match(IDENT);
			paramdec = paramDec();
			paramList.add(paramdec);
			while(t.isKind(COMMA)){
				consume();
				paramdec = paramDec();
				paramList.add(paramdec);
			}
			
			b = block();
			
		}
		else{
			throw new SyntaxException("Error in program: Received " + t.kind + " Expected valid program structure");
		}
		
		return new Program(firstToken, paramList, b);
	}

	public ParamDec paramDec() throws SyntaxException {
		Token firstToken = t;
		ParamDec pd = null;
		if(t.isKind(KW_URL) || t.isKind(KW_FILE) || t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN)){
			consume();
			pd = new ParamDec(firstToken, t);
			match(IDENT);
			
			
		}
		else{
			throw new SyntaxException("Error in parameter declaration: Received " + t.kind + " Expected valid param declaration");
		}
		return pd;
	}

	public Dec dec() throws SyntaxException {
		Token firstToken = t;
		consume();
		Dec dec = new Dec(firstToken,t);
		match(IDENT);
		return dec;
	}

	public Statement statement() throws SyntaxException {
		Token firstToken = t;
		Kind kind = t.kind;
		Statement stmt = null;
		switch(kind){
		case OP_SLEEP:{
			consume();
			Expression e = expression();
			match(SEMI);
			stmt = new SleepStatement(firstToken,e);
		}break;
		case KW_WHILE:{
			stmt = whileStatement();
		}break;
		case KW_IF:{
			stmt = ifStatement();
		}break;
		case IDENT: {
			if(scanner.peek().kind.equals(ASSIGN)){
				stmt = assign();
			}else{
				stmt = chain();
			}
			match(SEMI);
			
			
		}break;
		case OP_BLUR:
		case OP_GRAY:
		case OP_CONVOLVE:
		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC:
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE:
		{
				stmt = chain();
				match(SEMI);
		}
			break;
		default:{
			throw new SyntaxException("Error in declaration: Received " + t.kind + " Expected proper statement declaration");
		}
		}
		return stmt;
	}
	
	public AssignmentStatement assign() throws SyntaxException{
		Token firstToken = t;
		IdentLValue var = new IdentLValue(firstToken);
		consume();
		match(ASSIGN);
		Expression e = expression();
		AssignmentStatement stmt = new AssignmentStatement(firstToken, var, e);
		return stmt;
	}
	
	public IfStatement ifStatement() throws SyntaxException{
		Token firstToken = t;
		consume();
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		IfStatement stmt = new IfStatement(firstToken, e, b);
		return stmt;
	}
	
	public WhileStatement whileStatement() throws SyntaxException{
		Token firstToken = t;
		consume();
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		WhileStatement stmt = new WhileStatement(firstToken, e, b);
		return stmt;
	}
	

	public Chain chain() throws SyntaxException {
		Token firstToken = t;
		Token arrow = null;
		Chain e0 = null;
		
		
		e0 = chainElem();
		if(t.isKind(ARROW)|| t.isKind(BARARROW)){
			arrow = consume();
		}
		else{
			throw new SyntaxException("Error parsing block: Received " + t.kind + " Expected valid arrow/bararrow");
		}
		ChainElem e1 = chainElem();
		e0 = new BinaryChain(firstToken, e0, arrow, e1);
		//System.out.println(scanner.peek().kind);
		while(t.isKind(ARROW) || t.isKind(BARARROW)){
			arrow = consume();
			e1 = chainElem();
			e0  = new BinaryChain(firstToken, e0, arrow, e1);
		}
		
		return e0;
	}

	public ChainElem chainElem() throws SyntaxException {
		Token firstToken = t;
		ChainElem chainelem = null;
		Kind kind = t.kind;
		switch(kind){
		case IDENT: {
			chainelem = new IdentChain(firstToken);
			consume();
		}break;
		case OP_BLUR:
		case OP_GRAY:
		case OP_CONVOLVE:
		{
			consume();
			Tuple arg = arg();
			chainelem = new FilterOpChain(firstToken, arg);
			
		}break;
		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC:
		{
			consume();
			Tuple arg = arg();
			chainelem = new FrameOpChain(firstToken, arg);
			
		}break;
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE:
		{
			consume();
			Tuple arg = arg();
			chainelem = new ImageOpChain(firstToken, arg);
				
		}break;

		default:{
			throw new SyntaxException("Error parsing chain: Received " + t.kind + " Expected valid chain element");
		}
			
		}
		return chainelem;
		
		
	}

	public Tuple arg() throws SyntaxException {
		Token firstToken = t;
		ArrayList<Expression> argList = new ArrayList<Expression>();
		Expression e = null;
		if(t.isKind(LPAREN)){
			consume();
			e = expression();
			argList.add(e);
			while(t.isKind(COMMA)){
				consume();
				e = expression();
				argList.add(e);
			}
			match(RPAREN);
		}		
		return new Tuple(firstToken, argList);
	}
	
	

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("Error parsing EOF: Received " + t.kind + " Expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("Received token:  " + t.kind + " Expected: " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
