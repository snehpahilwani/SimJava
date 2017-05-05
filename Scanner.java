package simjava;


import java.util.ArrayList;
import java.util.Arrays;

public class Scanner {
	
	ArrayList<Integer> line_arr = new ArrayList<Integer>();
	
	public static enum State{
		START, IN_DIGIT, IN_IDENT, AFTER_EQ, AFTER_GT, AFTER_LT, AFTER_PIPE, AFTER_EXCLAIM, AFTER_MINUS, AFTER_SLASH  
	}
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			if(kind==Kind.EOF) return Kind.EOF.text;
			String token_text = chars.substring(pos, pos+length);
			return token_text;
		}
		
		public boolean isKind(Kind kind){
			if(this.kind==kind){
				return true;
			}
			else{
				return false;
			}
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			
			int line_number = Arrays.binarySearch(line_arr.toArray(), pos);
			int corrected_line_number = 0;
			if(line_number<0){
				corrected_line_number = -1 * (line_number+1) -1;
				return new LinePos(corrected_line_number,pos-line_arr.get(corrected_line_number));
			}
			return new LinePos(line_number,pos-line_arr.get(line_number));
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			
			
			if(this.kind==Kind.INT_LIT){
				return Integer.parseInt(this.getText());
			}
			else{
				return -1;
			}
		}
		
		 @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		
		
	}

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();


	}
	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0;
	    int length = chars.length();
	    State state = State.START;
	    int startPos = 0;
	    line_arr.add(0);
	    int ch;
	    while (pos <= length) {
	        ch = pos < length ? chars.charAt(pos) : -1;
	        switch (state) {
	            case START: {
	            	pos = skipWhiteSpace(pos);
	            	ch = pos < length ? chars.charAt(pos) : -1;
	            	startPos = pos;
	                switch (ch) {
	                case -1: {tokens.add(new Token(Kind.EOF, pos, 0)); pos++;}  break;
	                case '+': {tokens.add(new Token(Kind.PLUS, startPos, 1));pos++;} break;
	                case '*': {tokens.add(new Token(Kind.TIMES, startPos, 1));pos++;} break;
	                case '&': {tokens.add(new Token(Kind.AND, startPos, 1));pos++;}break;
	                case '%': {tokens.add(new Token(Kind.MOD, startPos, 1));pos++;} break;
	                case '0': {tokens.add(new Token(Kind.INT_LIT,startPos, 1));pos++;}break;
	                case '(': {tokens.add(new Token(Kind.LPAREN,startPos,1));pos++;}break;
	                case ')': {tokens.add(new Token(Kind.RPAREN,startPos,1));pos++;}break;
	                case '{': {tokens.add(new Token(Kind.LBRACE,startPos,1));pos++;}break;
	                case ',': {tokens.add(new Token(Kind.COMMA,startPos,1));pos++;}break;
	                case ';': {tokens.add(new Token(Kind.SEMI,startPos,1));pos++;}break;
	                case '}': {tokens.add(new Token(Kind.RBRACE,startPos,1));pos++;}break;
	                case '>': {state = State.AFTER_GT;pos++;}break;
	                case '<': {state = State.AFTER_LT;pos++;}break;
	                case '=': {state = State.AFTER_EQ;pos++;}break;
	                case '!': {state = State.AFTER_EXCLAIM; pos++;}break;
	                case '-': {state = State.AFTER_MINUS; pos++;}break;
	                case '|': {state = State.AFTER_PIPE;pos++;}break;
	                case '/': {state = State.AFTER_SLASH;pos++;}break;
	                default: {
	                    if (Character.isDigit(ch)) {state = State.IN_DIGIT;pos++;} 
	                    else if (Character.isJavaIdentifierStart(ch)) {
	                         state = State.IN_IDENT;pos++;
	                     } 
	                     else {throw new IllegalCharException(
	                                "illegal char " +(char)ch+" at pos "+pos);
	                     }
	                  }
	            } 
	            }  break;
	            case IN_DIGIT: {
	            	StringBuilder num_lit = new StringBuilder();
	            	num_lit.append(chars.charAt(pos-1));
	            	while(pos<length && Character.isDigit(chars.charAt(pos))){
	            		num_lit.append(chars.charAt(pos));
	            		pos++;
	            	}
	            	
	            	try{
	            		Integer.parseInt(num_lit.toString());
	            		tokens.add(new Token(Kind.INT_LIT,startPos,num_lit.length()));
	            		//pos++;
	            		state = State.START;
	            		
	            	}
	            	catch(Exception e){
	            		throw new IllegalNumberException("Int Overflow");
	            	}
	            	
	            	
	            }  break;
	            case IN_IDENT: {
	            	StringBuilder ident = new StringBuilder();
	            	ident.append(chars.charAt(pos-1));
	            	while(pos<length && Character.isJavaIdentifierPart(chars.charAt(pos))){
	            		ident.append(chars.charAt(pos));
	            		pos++;
	            	}
	            	switch(ident.toString()){
	            	case "integer": {tokens.add(new Token(Kind.KW_INTEGER,startPos,ident.length()));break;}
	            	case "boolean": {tokens.add(new Token(Kind.KW_BOOLEAN,startPos,ident.length()));break;}
	            	case "image": {tokens.add(new Token(Kind.KW_IMAGE,startPos,ident.length()));break;}
	            	case "url": {tokens.add(new Token(Kind.KW_URL,startPos,ident.length()));break;}
	            	case "file": {tokens.add(new Token(Kind.KW_FILE,startPos,ident.length()));break;}
	            	case "frame": {tokens.add(new Token(Kind.KW_FRAME,startPos,ident.length()));break;}
	            	case "while": {tokens.add(new Token(Kind.KW_WHILE,startPos,ident.length()));break;}
	            	case "if": {tokens.add(new Token(Kind.KW_IF,startPos,ident.length()));break;}
	            	case "sleep": {tokens.add(new Token(Kind.OP_SLEEP,startPos,ident.length()));break;}
	            	case "screenheight": {tokens.add(new Token(Kind.KW_SCREENHEIGHT,startPos,ident.length()));break;}
	            	case "screenwidth": {tokens.add(new Token(Kind.KW_SCREENWIDTH,startPos,ident.length()));break;}
	            	case "gray": {tokens.add(new Token(Kind.OP_GRAY,startPos,ident.length()));break;}
	            	case "convolve": {tokens.add(new Token(Kind.OP_CONVOLVE,startPos,ident.length()));break;}
	            	case "blur": {tokens.add(new Token(Kind.OP_BLUR,startPos,ident.length()));break;}
	            	case "scale": {tokens.add(new Token(Kind.KW_SCALE,startPos,ident.length()));break;}
	            	case "width": {tokens.add(new Token(Kind.OP_WIDTH,startPos,ident.length()));break;}
	            	case "height": {tokens.add(new Token(Kind.OP_HEIGHT,startPos,ident.length()));break;}
	            	case "xloc": {tokens.add(new Token(Kind.KW_XLOC,startPos,ident.length()));break;}
	            	case "yloc": {tokens.add(new Token(Kind.KW_YLOC,startPos,ident.length()));break;}
	            	case "hide": {tokens.add(new Token(Kind.KW_HIDE,startPos,ident.length()));break;}
	            	case "show": {tokens.add(new Token(Kind.KW_SHOW,startPos,ident.length()));break;}
	            	case "move": {tokens.add(new Token(Kind.KW_MOVE,startPos,ident.length()));break;}
	            	case "true": {tokens.add(new Token(Kind.KW_TRUE,startPos,ident.length()));break;}
	            	case "false": {tokens.add(new Token(Kind.KW_FALSE,startPos,ident.length()));break;}
	            	default: {tokens.add(new Token(Kind.IDENT,startPos,ident.length()));break;}
	            	}
	            	state = State.START;
	            }  break;
	            case AFTER_EQ: {
	            	//pos++;
	            	if(pos<length && chars.charAt(pos)=='='){
	            		tokens.add(new Token(Kind.EQUAL,startPos,2));pos++;
	            		state = State.START;
	            	}
	            	else{
	            		{throw new IllegalCharException(
                                "illegal char " +(char)ch+" at pos "+pos);
                     }
	            	}
	            }  break;
	            case AFTER_GT: {
	            	//pos++;
	            	if(pos<length && chars.charAt(pos)=='='){
	            		tokens.add(new Token(Kind.GE, startPos,2));
	            		pos++;
	            		
	            	}
	            	else{
	            		tokens.add(new Token(Kind.GT,startPos,1));
	            	}
	            	state = State.START;
	            }break;
	            case AFTER_LT:{
	            	//pos++;
	            	if(pos<length && chars.charAt(pos)=='='){
	            		tokens.add(new Token(Kind.LE, startPos,2));
	            		pos++;
	            		
	            	}
	            	else if(chars.charAt(pos)=='-'){
	            		tokens.add(new Token(Kind.ASSIGN,startPos,2));
	            		pos++;
	            	}
	            	else{
	            		tokens.add(new Token(Kind.LT,startPos,1));
	            	}
	            	state = State.START;
	            
	            
	            }break;
	            case AFTER_PIPE:{
	            	//pos++;
	            	if(pos<length && chars.charAt(pos)=='-'){
	            		pos++;
	            		if(pos<length && chars.charAt(pos)=='>'){
	            			 tokens.add(new Token(Kind.BARARROW,startPos,3));
	            			 pos++;
	            		}
	            		else{
	            			tokens.add(new Token(Kind.OR,startPos,1));
	            			tokens.add(new Token(Kind.MINUS,pos-1,1));
	            		}
	            	}else{
	            		tokens.add(new Token(Kind.OR,startPos,1));
	            		
	            	}
	            	
	            	state = State.START;
	            }break;
	            case AFTER_EXCLAIM:{
	            	if(pos<length && chars.charAt(pos)=='='){
	            		tokens.add(new Token(Kind.NOTEQUAL,startPos,2));
	            		pos++;
	            	}
	            	else{
	            		tokens.add(new Token(Kind.NOT,startPos,1));
	            	}
	            	state = State.START;
	            }break;
	            case AFTER_MINUS:{
	            	if(pos<length && chars.charAt(pos)=='>'){
	            		tokens.add(new Token(Kind.ARROW,startPos,2));
	            		pos++;
	            	}
	            	else{
	            		tokens.add(new Token(Kind.MINUS,startPos,1));
	            	}
	            	state = State.START;
	            }break;
	            case AFTER_SLASH: {
					if (pos < length && chars.charAt(pos) == '*') {
						pos++;
						while (pos < length) {
							
							pos = skipWhiteSpace(pos);
							if (chars.charAt(pos) == '*') {
								if (pos + 1 < length && chars.charAt(pos + 1) == '/') {
									pos = pos + 2;
									state = State.START;
									break;
								}
								
							}
							pos++;

						}

						if (pos >= length - 1) {
							if (pos == length - 1) {
								pos++;
							}
							state = State.START;
						}
					} else {
						tokens.add(new Token(Kind.DIV, startPos, 1));
					}

					state = State.START;
				}break;
	            
	            default:  assert false;
	        }// switch(state)
	    } // while
	  

		//tokens.add(new Token(Kind.EOF,pos,0));
		return this;  
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		
		return t.getLinePos();
	}
	
	public int skipWhiteSpace(int pos) {
		
		//int[] line = null;
		//int index = 0;
		while( pos < chars.length()){
			if (Character.isWhitespace(chars.charAt(pos))){
				
				if(chars.charAt(pos)=='\n'){
					
					line_arr.add(pos+1);
				}
				pos++;
			}
				
			else
				break;
			}
		return pos;
	}


}
