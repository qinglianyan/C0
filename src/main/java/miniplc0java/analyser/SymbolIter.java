package miniplc0java.analyser;

import miniplc0java.error.CompileError;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.tokenizer.*;

public class SymbolIter {
	/* 这里是参考了网上的一个做法，将tokenizer封装起来 */
	private Tokenizer tokenizer;
	private Token peekedToken = null;

	public SymbolIter(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	/**
	 * 查看下一个 Token
	 * 
	 * @return
	 * @throws TokenizeError
	 */
	public Token peek() throws TokenizeError {
		if (peekedToken == null) {
			peekedToken = tokenizer.nextToken();
		}
		return peekedToken;
	}

	/**
	 * 获取下一个 Token
	 * 
	 * @return
	 * @throws TokenizeError
	 */
	public Token next() throws TokenizeError {
		if (peekedToken != null) {
			var token = peekedToken;
			peekedToken = null;
			return token;
		} else {
			return tokenizer.nextToken();
		}
	}

	/**
	 * 如果下一个 token 的类型是 tt，则返回 true
	 * 
	 * @param tt
	 * @return
	 * @throws TokenizeError
	 */
	public boolean check(TokenType tt) throws TokenizeError {
		var token = peek();
		return token.getTokenType() == tt;
	}

	/**
	 * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
	 * 
	 * @param tt 类型
	 * @return 如果匹配则返回这个 token，否则返回 null
	 * @throws TokenizeError
	 */
	public Token nextIf(TokenType tt) throws TokenizeError {
		var token = peek();
		if (token.getTokenType() == tt) {
			return next();
		} else {
			return null;
		}
	}

	/**
	 * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
	 * 
	 * @param tt 类型
	 * @return 这个 token
	 * @throws CompileError 如果类型不匹配
	 */
	public Token expect(TokenType tt) throws CompileError {
		var token = peek();
		if (token.getTokenType() == tt) {
			return next();
		} else {
			throw new ExpectedTokenError(tt, token);
		}
	}

}
