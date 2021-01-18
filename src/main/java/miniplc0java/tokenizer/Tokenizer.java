package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.util.Pos;
import miniplc0java.error.ErrorCode;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUInt();
        }
        else if (Character.isAlphabetic(peek)||peek=='_') {
            return lexIdentOrKeyword();
        }
        else if(peek=='\"') {
        	return lexString();
        }
//        else if() {
//        	return lexChar();
//        }
//        else if() {
//        	return lexComment();
//        }
        else {
            return lexOperatorOrUnknown();
        }
    }

    /* 无符号整数 */
    /* 如果加double就在这里改 */
    private Token lexUInt() throws TokenizeError {
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        // Token 的 Value 应填写数字的值

    	String tem="";
    	Pos beginp=it.currentPos();
    	while(!it.isEOF()&&Character.isDigit(it.peekChar())) {
    		
    		tem=tem+it.nextChar();
    	}
    	try{
    		int a=Integer.parseInt(tem);
    		return new Token(TokenType.UINT_LITERAL, a, beginp, it.currentPos());
    	}
    	//忘了咋抛异常了，后面再改
    	catch(Error e) {
    		throw new Error("Not implemented");
    	}
    }

    /* 标识符或者是关键字 */
    private Token lexIdentOrKeyword() throws TokenizeError {
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
    	String tem="";
    	Pos beginp=it.currentPos();
    	while(!it.isEOF()&&Character.isLetterOrDigit(it.peekChar())||it.peekChar()=='_') {
    		tem=tem+it.nextChar();
    	}
    	try {
    		if(tem.contentEquals("fn")) {
    			return new Token(TokenType.FN_KW, "fn", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("let")) {
    			return new Token(TokenType.LET_KW, "let", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("const")) {
    			return new Token(TokenType.CONST_KW, "const", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("as")) {
    			return new Token(TokenType.AS_KW, "as", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("while")) {
    			return new Token(TokenType.WHILE_KW, "while", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("if")) {
    			return new Token(TokenType.IF_KW, "if", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("else")) {
    			return new Token(TokenType.ELSE_KW, "else", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("return")) {
    			return new Token(TokenType.RETURN_KW, "return", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("break")) {
    			return new Token(TokenType.BREAK_KW, "break", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("continue")) {
    			return new Token(TokenType.CONTINUE_KW, "continue", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("int")) {
    			return new Token(TokenType.INT, "int", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("string")) {
    			return new Token(TokenType.STRING, "string", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("void")) {
    			return new Token(TokenType.VOID, "void", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("double")) {
    			return new Token(TokenType.DOUBLE, "double", beginp,it.currentPos());
    		}
    		else if(tem.contentEquals("char")) {
    			return new Token(TokenType.CHAR, "char", beginp,it.currentPos());
    		}
    		else {
    			return new Token(TokenType.IDENT, tem, beginp,it.currentPos());
    		}
    	}
    	//忘记怎么抛出异常了，以后改
    	catch(Error e) {
    		throw new Error("Not implemented");
    	}
    }
    
    /* 字符串类型 */
    private Token lexString() throws TokenizeError{
    	String tem="";
    	Pos beginp=it.currentPos();
    	
    	char temp=it.nextChar();
    	if(temp!='\"') {
    		throw new TokenizeError(ErrorCode.InvalidString, it.currentPos());
    	}
    	while(((temp=it.nextChar())!='\"')) {
    		if(it.isEOF()) {
    			throw new TokenizeError(ErrorCode.InvalidString,beginp);
    		}
    		if(temp=='\\') {
    			temp=it.nextChar();
    			if(it.isEOF()) {
    				throw new TokenizeError(ErrorCode.InvalidString,beginp);
    			}
    			switch(temp) {
    			case '\\': temp = '\\'; break;
                case '\'': temp = '\''; break;
                case '\"': temp = '\"'; break;
                case 'n': temp = '\n'; break;
                case 't': temp = '\t'; break;
                case 'r': temp = '\r'; break;
                default:throw new TokenizeError(ErrorCode.InvalidString,beginp);
    			}
    		}
    		tem+=temp;
    		if(it.isEOF()) {
    			throw new TokenizeError(ErrorCode.InvalidString, beginp);
    		}
    	}
    	Pos endp=it.currentPos();
    	return new Token(TokenType.STRING_LITERAL,tem,beginp,endp);
    }
    
    
    /* 运算符或者是不能识别 */
    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':{
            	if(it.peekChar()=='>') {
            		it.nextChar();
            		return new Token(TokenType.ARROW, "->",it.previousPos(),it.currentPos());
            	}
            }
            	return new Token(TokenType.MINUS, '-',it.previousPos(), it.currentPos());

            case '*':
            	return new Token(TokenType.MUL, '*',it.previousPos(), it.currentPos());
            
            case '/':{
            	if(it.peekChar()=='/') {
            		it.nextChar();
            		return new Token(TokenType.COMMENT, "//", it.previousPos(), it.currentPos());
            	}
            	else {
            		return new Token(TokenType.DIV, '/',it.previousPos(), it.currentPos());
            	}
            }
            case '=':{
            	if(it.peekChar()=='=') {
            		it.nextChar();
            		return new Token(TokenType.EQ, "==",it.previousPos(), it.currentPos());
            	}
            	else {
            		return new Token(TokenType.ASSIGN, '=',it.previousPos(), it.currentPos());
            	}
            }
            case '!':{
            	if(it.peekChar()=='=') {
            		it.nextChar();
            		return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
            	}
            	else {
            		throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            	}
            }
            
            case '<':
            	return new Token(TokenType.LT, '<',it.previousPos(), it.currentPos());
            
            case '>':
            	return new Token(TokenType.GT, '>',it.previousPos(), it.currentPos());
            
            case '(':
            	return new Token(TokenType.L_PAREN, '(',it.previousPos(), it.currentPos());
            
            case ')':
            	return new Token(TokenType.R_PAREN, ')',it.previousPos(), it.currentPos());
            
            case '{':
            	return new Token(TokenType.L_BRACE, '{',it.previousPos(), it.currentPos());
            	
            case '}':
            	return new Token(TokenType.R_BRACE, '}',it.previousPos(), it.currentPos());
            	
            case ',':
            	return new Token(TokenType.COMMA, ',',it.previousPos(), it.currentPos());
            
            case ':':
            	return new Token(TokenType.COLON, ':',it.previousPos(), it.currentPos());
            
            case ';':
            	return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());
            
            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}