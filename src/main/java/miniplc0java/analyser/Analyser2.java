package miniplc0java.analyser;

import java.util.*;

import miniplc0java.analyser.returnChecker.BranchStack;
import miniplc0java.error.AnalyzeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.CompileError;
import miniplc0java.navm.*;
import miniplc0java.navm.instruction.*;
import miniplc0java.symbolTable.*;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.util.Pos;

public class Analyser2 {
	SymbolIter it; // 这是封装的词法分析器
	SymbolTable symbolTable; // 符号表s
	Map<TokenType, DataType> typeMap;
	Stack<Integer> brStack; // 这个还不知道gai
	BranchStack branchStack; // 返回分支
	OoFile outfile;

	public Analyser2(SymbolIter symbolIter) {
		this.it = symbolIter;
		this.symbolTable = new SymbolTable();
		this.typeMap = new HashMap<>();
		this.outfile = new OoFile();
		this.branchStack = new BranchStack();
		this.brStack = new Stack();
	}

	public void init() {
		this.typeMap.put(TokenType.INT, DataType.INT);
		this.typeMap.put(TokenType.VOID, DataType.VOID);
		this.typeMap.put(TokenType.DOUBLE, DataType.DOUBLE);
		this.typeMap.put(TokenType.STRING, DataType.STRING);
		this.typeMap.put(TokenType.CHAR, DataType.CHAR);
	}

	/* 分析开始 */
	public OoFile analyse() throws CompileError {
		init();
		symbolTable.initSybolTable();
		analyseProgram();// 一遍读完
		windup();// 收尾工作
		return this.outfile;
	}

	private boolean windup() throws AnalyzeError {
		// TODO Auto-generated method stub
		assert this.symbolTable.getLevel() == 0;
		Symbol symbol = symbolTable.findAllBlockSymbol("main");
		if (symbol == null || symbol.getSymboltype() != SymbolType.FUNCTION) {
			throw new AnalyzeError(ErrorCode.NoMain, new Pos(0, 0));
		}
		assert symbol instanceof FuncSymbol;
		FuncSymbol funcSymbol = (FuncSymbol) symbol;

		// 为_start函数添加调用main的指令
		this.outfile.addStartInstruction(new InstructionU32(InstructionType.StackAlloc, (int) 1));
		this.outfile.addStartInstruction(new InstructionU32(InstructionType.Call, (int) funcSymbol.getOffset()));
		this.outfile.addStartInstruction(new InstructionU32(InstructionType.PopN, (int) 1));

		return true;
	}

	/*
	 * 程序结构 program -> decl_stmt* function* 程序->声明语句 或 函数
	 * 
	 * @throws CompileError
	 */
	private void analyseProgram() throws CompileError {
		// TODO Auto-generated method stub
		while (it.check(TokenType.LET_KW) || it.check(TokenType.CONST_KW) || it.check(TokenType.FN_KW)) {
			/* 声明语句分别是常量声明与变量声明，函数是以fn开头 */
			if (it.check(TokenType.LET_KW)) {
				analyseLetDecl();
			} else if (it.check(TokenType.CONST_KW)) {
				analyseConstDecl();
			} else if (it.check(TokenType.FN_KW)) {
				analyseFuncDecl();
			}
		}

		Token eof = it.next();
		if (eof.getTokenType() != TokenType.EOF) {
			throw new AnalyzeError(ErrorCode.EOF, eof.getStartPos());
		}
	}

	/*
	 * 分析变量声明 let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
	 * 
	 * @throws CompileError
	 */
	private void analyseLetDecl() throws CompileError {
		// TODO Auto-generated method stub
		boolean isInit = false;// 用来判断变量是否初始化
		it.expect(TokenType.LET_KW);
		Token variable = it.expect(TokenType.IDENT);
		it.expect(TokenType.COLON);

		Token ty = it.next();
		if (ty.getTokenType() != TokenType.INT && ty.getTokenType() != TokenType.DOUBLE) {
			throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
		}
		// 变量的类型不能是void
		if (ty.getTokenType() == TokenType.VOID) {
			throw new AnalyzeError(ErrorCode.InvalidVoid, ty.getStartPos());
		}

		VarSymbol varSymbol = new VarSymbol(variable.getValueString(), SymbolType.BIANLIANG,
				this.typeMap.get(ty.getTokenType()), 0, variable.getStartPos());

		// 写入oOfile
		int offset = 0;
		if (this.symbolTable.getLevel() == 0) {
			varSymbol.setGlobal(true);
			offset = this.outfile.addGlobVar(varSymbol);
		} else {
			offset = this.outfile.addLocalVar(varSymbol);
		}
		varSymbol.setOffset(offset);

		if (it.nextIf(TokenType.ASSIGN) != null) {
			isInit = true;
			if (this.symbolTable.getLevel() == 0) {
				this.outfile.addInstruction(new InstructionU32(InstructionType.GlobA, (int) varSymbol.getOffset()));
			} else {
				this.outfile.addInstruction(new InstructionU32(InstructionType.LocA, (int) varSymbol.getOffset()));
			}
			DataType exprDType = analyseExpr();
			TypeChecker.typeCheck(this.typeMap.get(ty.getTokenType()), exprDType, variable.getStartPos());
			this.outfile.addInstruction(new InstructionNone(InstructionType.Store64));

		}

		it.expect(TokenType.SEMICOLON);

		// 在符号表中注册一个新的变量符号
		varSymbol.setInitialized(isInit);
		// 如果是全局变量
		this.symbolTable.insertSymbol(varSymbol);
		return;
	}

	/* add */
	/*
	 * 分析常量声明 const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
	 * 
	 * @throws CompileError
	 */
	private void analyseConstDecl() throws CompileError {
		// TODO Auto-generated method stub
		it.expect(TokenType.CONST_KW);
		Token variable = it.expect(TokenType.IDENT);
		it.expect(TokenType.COLON);
		Token ty = it.next();
		if (ty.getTokenType() != TokenType.INT && ty.getTokenType() != TokenType.DOUBLE
				&& ty.getTokenType() != TokenType.VOID) {
			throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
		}
		if (ty.getTokenType() == TokenType.VOID) {
			throw new AnalyzeError(ErrorCode.InvalidVoid, ty.getStartPos());
		}

		if (it.check(TokenType.ASSIGN) == false) {
			throw new AnalyzeError(ErrorCode.ConstantNeedValue, it.peek().getStartPos());
		}

		DataType dType = this.typeMap.get(ty.getTokenType());
		VarSymbol varSymbol = new VarSymbol(variable.getValueString(), SymbolType.CONST, dType, 0,
				variable.getStartPos());

		// 写入outfile
		int offset = 0;
		if (this.symbolTable.getLevel() == 0) {
			varSymbol.setGlobal(true);
			offset = this.outfile.addGlobVar(varSymbol);
		} else {
			offset = this.outfile.addLocalVar(varSymbol);
		}
		varSymbol.setOffset(offset);

		it.expect(TokenType.ASSIGN);
		// 将变量的地址取出
		if (this.symbolTable.getLevel() == 0) {
			this.outfile.addInstruction(new InstructionU32(InstructionType.GlobA, (int) varSymbol.getOffset()));
		} else {
			this.outfile.addInstruction(new InstructionU32(InstructionType.LocA, (int) varSymbol.getOffset()));
		}
		DataType exprDType = analyseExpr();
		this.outfile.addInstruction(new InstructionNone(InstructionType.Store64));

		it.expect(TokenType.SEMICOLON);

		// 获取对应的符号数据类型
		varSymbol.setInitialized(true);
		TypeChecker.typeCheck(this.typeMap.get(ty.getTokenType()), exprDType, variable.getStartPos());
		this.symbolTable.insertSymbol(varSymbol);

		return;

	}

	/*
	 * 分析函数声明
	 * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
	 * 
	 * @throws CompileError
	 */
	private void analyseFuncDecl() throws CompileError {
		// TODO Auto-generated method stub
		it.expect(TokenType.FN_KW);
		Token fnIdent = it.expect(TokenType.IDENT);

		// 生成新的函数符号
		FuncSymbol funcSymbol = new FuncSymbol(fnIdent.getValueString(), SymbolType.FUNCTION, DataType.INT, 0,
				fnIdent.getStartPos());

		it.expect(TokenType.L_PAREN);

		// function_param_list的可以说是FIRST集是const或者是ident
		if (it.peek().getTokenType() == TokenType.IDENT || it.peek().getTokenType() == TokenType.CONST_KW) {
			analyseFuncParamList(funcSymbol);
		}

		it.expect(TokenType.R_PAREN);
		it.expect(TokenType.ARROW);
		Token ty = it.next();
		if (ty.getTokenType() != TokenType.INT && ty.getTokenType() != TokenType.DOUBLE
				&& ty.getTokenType() != TokenType.VOID) {
			throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
		}

		// 设置返回参数
		DataType funcReturnType = this.typeMap.get(ty.getTokenType());
		funcSymbol.setDatatype(funcReturnType);

		// 放入符号表中
		this.symbolTable.insertSymbol(funcSymbol);

		// 将这个函数写入oOfile中
		this.outfile.addFunction(funcSymbol);

		this.branchStack.addFnBranch(funcReturnType);
		// 开始分析块语句
		analyseBlockStmt(funcSymbol);
		boolean needRet = this.branchStack.quitFunc(fnIdent.getStartPos());

		if (needRet) {
			this.outfile.addInstruction(new InstructionNone(InstructionType.Ret));
		}
	}

	/*
	 * 分析函数参数列表
	 * function_param_list -> function_param (',' function_param)*
	 * 
	 * @throws CompileError
	 */
	public void analyseFuncParamList(FuncSymbol funcSymbol) throws CompileError {
		VarSymbol varSymbol = analyseFuncParam();
		funcSymbol.addArgs(varSymbol);
		while (it.nextIf(TokenType.COMMA) != null) {
			VarSymbol varSymbolMany = analyseFuncParam();
			funcSymbol.addArgs(varSymbolMany);
		}
	}

	/*
	 * 分析函数参数
	 * function_param -> 'const'? IDENT ':' ty
	 * 
	 * @return Symbol 返回解析的函数参数符号
	 * @throws CompileError
	 */
	public VarSymbol analyseFuncParam() throws CompileError {

		boolean isConst = false;
		if (it.check(TokenType.CONST_KW)) {
			it.expect(TokenType.CONST_KW);
			isConst = true;
		}

		Token fnParam = it.expect(TokenType.IDENT);
		it.expect(TokenType.COLON);

		// 解析类型
		Token ty = it.next();
		if (ty.getTokenType() != TokenType.INT && ty.getTokenType() != TokenType.DOUBLE
				&& ty.getTokenType() != TokenType.VOID) {
			throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
		}
		if (ty.getTokenType() == TokenType.VOID) {
			throw new AnalyzeError(ErrorCode.InvalidVoid, ty.getStartPos());
		}

		VarSymbol varSymbol;
		varSymbol = new VarSymbol(fnParam.getValueString(), isConst ? SymbolType.CONST : SymbolType.BIANLIANG,
				this.typeMap.get(ty.getTokenType()), 0, fnParam.getStartPos());

		return varSymbol;
	}

	/**
	 * 解析各种语句，主要是分发的功能
	 * stmt -> expr_stmt | decl_stmt | if_stmt | while_stmt |
	 * return_stmt | block_stmt | empty_stmt 
	 * first集为
	 * first(expr_stmt) = {'-',IDNET,UINT_VALUE, DOUBLE_VALUE, STRING_VALUE, '('}
	 * first(decal_stmt) = {LET_KW,CONST_KW}
	 * first(if_stmt) = {IF_KW}
	 * first(while_stmt) = {WHILE_KW}
	 * first(return_stmt) = {RETURN_KW}
	 * first(block_stmt) = {L_BRACE}
	 * first(empty_stmt) = {SEMICOLON}
	 * 
	 * @throws CompileError
	 */
	public void analyseStmt() throws CompileError {

		Token token = it.peek();
		if (token.getTokenType() == TokenType.SEMICOLON) {
			// 空语句
			analyseEmptyStmt();
		} else if (token.getTokenType() == TokenType.L_BRACE) {
			analyseBlockStmt(null);
		} else if (token.getTokenType() == TokenType.RETURN_KW) {
			analyseReturnStmt();
		} else if (token.getTokenType() == TokenType.WHILE_KW) {
			analyseWhileStmt();
		} else if (token.getTokenType() == TokenType.IF_KW) {
			analyseIfStmt();
		} else if (token.getTokenType() == TokenType.LET_KW) {
			analyseLetDecl();
		} else if (token.getTokenType() == TokenType.CONST_KW) {
			analyseConstDecl();
		} else if (token.getTokenType() == TokenType.MINUS || token.getTokenType() == TokenType.IDENT
				|| token.getTokenType() == TokenType.UINT_LITERAL || token.getTokenType() == TokenType.DOUBLE_LITERAL
				|| token.getTokenType() == TokenType.STRING_LITERAL || token.getTokenType() == TokenType.L_PAREN) {
			analyseExprStmt();
		} else {
			throw new AnalyzeError(ErrorCode.UnExpectToken, it.peek().getStartPos());
		}

	}

	/*
	 * 分析代码块
	 * block_stmt -> '{' stmt* '}'
	 * 
	 * @param funcSymbol 函数的符号，用来获取函数的参数
	 * @throws CompileError
	 */
	public void analyseBlockStmt(FuncSymbol funcSymbol) throws CompileError {
		it.expect(TokenType.L_BRACE);

		// 生成新的符号表块
		this.symbolTable.addBlockTable();

		// 将函数的参数表插入符号表中
		if (funcSymbol != null) {
			int agrsSize = funcSymbol.getArgsList().size();
			ArrayList<VarSymbol> funcParams = funcSymbol.getArgsList();
			int argOffset = 0;
			if (funcSymbol.getDatatype() != DataType.VOID) {
				argOffset = 1;
			}
			for (int i = 0; i < agrsSize; i++) {
				// 是一个参数
				VarSymbol funcParam = funcParams.get(i);
				funcParam.setParam(true);
				funcParam.setOffset(i + argOffset);
				this.symbolTable.insertSymbol(funcParam);
			}
		}

		while (it.check(TokenType.R_BRACE) == false) {
			analyseStmt();
		}
		it.expect(TokenType.R_BRACE);

		// 删除这个符号表块
		this.symbolTable.removeBlockTable();
	}

	/*
	 * 空语句
	 * empty_stmt -> ';'
	 * 
	 * @throws CompileError
	 */
	public void analyseEmptyStmt() throws CompileError {
		it.expect(TokenType.SEMICOLON);
		return;
	}

	/*
	 * 分析while语句
	 * while_stmt -> 'while' expr block_stmt
	 * 
	 * @throws CompileError
	 */
	public void analyseWhileStmt() throws CompileError {
		Token whileToken = it.expect(TokenType.WHILE_KW);
		int startOffset = this.outfile.addInstruction(new InstructionU32(InstructionType.Br, (int) 0));
		analyseExpr();
		this.outfile.addInstruction(new InstructionU32(InstructionType.BrTrue, (int) 1));
		int falseOffset = this.outfile.addInstruction(new InstructionU32(InstructionType.Br, (int) 0));
		this.branchStack.addWhileBranch();
		analyseBlockStmt(null);
		int loopOffset = this.outfile.addInstruction(new InstructionU32(InstructionType.Br, (int) 0));
		this.outfile.modInstructionU32(loopOffset, startOffset - loopOffset);
		this.outfile.modInstructionU32(falseOffset, loopOffset - falseOffset);

		this.branchStack.quitBranch(whileToken.getStartPos());
	}

	/*
	 * 分析If语句:
	 * if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
	 * 
	 * @throws CompileError
	 */
	public void analyseIfStmt() throws CompileError {

		Token ifToken = it.expect(TokenType.IF_KW);
		analyseExpr();

		this.outfile.addInstruction(new InstructionU32(InstructionType.BrTrue, (int) 1));
		int falseBrOffset = this.outfile.addInstruction(new InstructionU32(InstructionType.Br, (int) 0)); // 注意此参数将来回填
		this.brStack.push(falseBrOffset);

		// 向分支表中插入新的分支
		this.branchStack.addIfBranch();
		analyseBlockStmt(null);
		this.branchStack.quitBranch(ifToken.getStartPos());
		int trueBrOffset = this.outfile.addInstruction(new InstructionU32(InstructionType.Br, (int) 0));

		if (it.check(TokenType.ELSE_KW)) {
			Token elseToken = it.expect(TokenType.ELSE_KW);
			// 回填falseBrOffset, 并标记为已经修改
			this.outfile.modInstructionU32(falseBrOffset, this.outfile.getCurOffset() - falseBrOffset);
			falseBrOffset = -1;
			// 接下来对应两个分支，分别是block_stmt以及if_stmt;
			if (it.check(TokenType.L_BRACE)) {
				// 这是block_stmt分支
				this.branchStack.addElseBranch();
				analyseBlockStmt(null);
				this.branchStack.quitBranch(elseToken.getStartPos());
				// 回填br的参数
			} else if (it.peek().getTokenType() == TokenType.IF_KW) {
				analyseIfStmt();
			} else {
				throw new AnalyzeError(ErrorCode.UnExpectToken, it.peek().getStartPos());
			}
			// 分析else完毕，产生一个br
			int elseBrOffset = this.outfile.addInstruction(new InstructionU32(InstructionType.Br, (int) 0));
		}

		// 回填falseBrOffset
		if (falseBrOffset != -1) {
			this.outfile.modInstructionU32(falseBrOffset, this.outfile.getCurOffset() - falseBrOffset);
			falseBrOffset = -1;
		}
		// 回填trueBrOffset
		this.outfile.modInstructionU32(trueBrOffset, this.outfile.getCurOffset() - trueBrOffset);
		return;
	}

	/*
	 * 分析return语句
	 * return_stmt -> 'return' expr? ';'
	 * 
	 * @throws CompileError
	 */
	public void analyseReturnStmt() throws CompileError {
		Token re = it.expect(TokenType.RETURN_KW);
		Token token = it.peek();
		DataType dType = DataType.VOID;

		if (token.getTokenType() == TokenType.MINUS || token.getTokenType() == TokenType.IDENT
				|| token.getTokenType() == TokenType.UINT_LITERAL || token.getTokenType() == TokenType.STRING_LITERAL
				|| token.getTokenType() == TokenType.DOUBLE_LITERAL || token.getTokenType() == TokenType.L_PAREN) {

			// 有返回值,将返回值的地址放在Args[0]处
			this.outfile.addInstruction(new InstructionU32(InstructionType.ArgA, (int) 0));
			dType = analyseExpr();
			this.outfile.addInstruction(new InstructionNone(InstructionType.Store64));
		}
		this.branchStack.returnOnce(dType, re.getStartPos());

		it.expect(TokenType.SEMICOLON);
		this.outfile.addInstruction(new InstructionNone(InstructionType.Ret));
	}

	/*
	 * 分析: empty_stmt -> ';'
	 * 
	 * @throws CompileError
	 */
	public void analyseExprStmt() throws CompileError {
		DataType dType = analyseExpr();
		System.out.println(dType);
		if (dType != DataType.VOID) {
			this.outfile.addInstruction(new InstructionU32(InstructionType.PopN, (int) 1));
		}
		it.expect(TokenType.SEMICOLON);
	}

	/*
	 * 分析expr,将文法改写如下
	 * 这里相当于是改成了算符优先文法，先出现的优先级低
	 * Expr -> Cond ( (== | != | < | > | <= | >=) Cond )?
	 * Cond ->Term {(+ | -) Term}
	 * Term -> Factor { (* | /) Factor}
	 * Factor -> Atom { as (INT | DOUBLE )}?
	 * Atom -> '-'? Item
	 * Item -> '(' Expr ')' |IDENT | UINT_VALUE |DOUBLE_VALUE | func_call | IDENT '=' Expr
	 * 每一个表达式都会返回其类型用来做判断
	 * 
	 * @return 返回一个SymbolType用来做类型判断
	 * @throws CompileError
	 */
	public DataType analyseExpr() throws CompileError {

		DataType leftType = analyseCond();
		Token token = it.peek();

		// 可选的比较
		if (token.getTokenType() == TokenType.EQ || token.getTokenType() == TokenType.NEQ
				|| token.getTokenType() == TokenType.LT || token.getTokenType() == TokenType.GT
				|| token.getTokenType() == TokenType.LE || token.getTokenType() == TokenType.GE) {

			Token compare = it.next();
			DataType rightType = analyseCond();
			TypeChecker.typeCheck(leftType, rightType, compare.getStartPos());

			if (leftType == DataType.INT) {
				this.outfile.addInstruction(new InstructionNone(InstructionType.CmpI));
			} else if (leftType == DataType.DOUBLE) {
				this.outfile.addInstruction(new InstructionNone(InstructionType.CmpF));
			}

			switch (token.getTokenType()) {
			case EQ: {
				this.outfile.addInstruction(new InstructionNone(InstructionType.Not));
				break;
			}
			case NEQ: {
				break;
			}
			case LT: {
				this.outfile.addInstruction(new InstructionNone(InstructionType.SetLt));
				break;
			}
			case GT: {
				this.outfile.addInstruction(new InstructionNone(InstructionType.SetGt));
				break;
			}
			case LE: {
				this.outfile.addInstruction(new InstructionNone(InstructionType.SetGt));
				this.outfile.addInstruction(new InstructionNone(InstructionType.Not));
				break;
			}
			case GE: {
				this.outfile.addInstruction(new InstructionNone(InstructionType.SetLt));
				this.outfile.addInstruction(new InstructionNone(InstructionType.Not));
				break;
			}
			default: {
				break;
			}
			}
			leftType = DataType.VOID;
		}
		return leftType;
	}

	/*
	 * 分析Cond表达式
	 * Cond -> Term {(+ | -) Term}
	 * 
	 * @throws CompileError
	 */
	public DataType analyseCond() throws CompileError {

		DataType leftType = analyseTerm();
		while (it.check(TokenType.PLUS) || it.check(TokenType.MINUS)) {
			Token token = it.next();
			DataType rightType = analyseTerm();
			TypeChecker.typeCheck(leftType, rightType, token.getStartPos());
			leftType = rightType;

			// 生成加法或者减法指令
			if (token.getTokenType() == TokenType.PLUS) {
				if (rightType == DataType.INT)
					this.outfile.addInstruction(new InstructionNone(InstructionType.AddI));
				else if (rightType == DataType.DOUBLE)
					this.outfile.addInstruction(new InstructionNone(InstructionType.AddF));
			} else if (token.getTokenType() == TokenType.MINUS) {
				if (rightType == DataType.INT)
					this.outfile.addInstruction(new InstructionNone(InstructionType.SubI));
				else if (rightType == DataType.DOUBLE)
					this.outfile.addInstruction(new InstructionNone(InstructionType.SubF));
			}
		}

		return leftType;
	}

	/*
	 * 分析Term表达式
	 * Term -> Factor { (* | /) Factor}
	 * 
	 * @throws CompileError
	 */
	public DataType analyseTerm() throws CompileError {

		DataType leftType = analyseFactor();
		while (it.check(TokenType.MUL) || it.check(TokenType.DIV)) {
			Token token = it.next();
			DataType rightType = analyseFactor();
			TypeChecker.typeCheck(leftType, rightType, token.getStartPos());

			// 生成乘法或者除法指令
			if (token.getTokenType() == TokenType.MUL) {
				if (rightType == DataType.INT)
					this.outfile.addInstruction(new InstructionNone(InstructionType.MulI));
				else if (rightType == DataType.DOUBLE)
					this.outfile.addInstruction(new InstructionNone(InstructionType.MulF));
			} else if (token.getTokenType() == TokenType.DIV) {
				if (rightType == DataType.INT)
					this.outfile.addInstruction(new InstructionNone(InstructionType.DivI));
				else if (rightType == DataType.DOUBLE)
					this.outfile.addInstruction(new InstructionNone(InstructionType.DivF));
			}

		}

		return leftType;
	}

	/*
	 * 分析Factor表达式
	 * Factor -> Atom { as ( INT | DOUBLE )}
	 * 
	 * @throws CompileError
	 */
	public DataType analyseFactor() throws CompileError {

		DataType dType = analyseAtom();

		while (it.check(TokenType.AS_KW)) {

			// 如果类型的左端是VOID则不可以转换
			if (dType == DataType.VOID) {
				throw new AnalyzeError(ErrorCode.InvalidOpVoid, it.peek().getStartPos());
			}

			it.expect(TokenType.AS_KW); // 吃掉as符号
			Token ty = it.next();
			if (ty.getTokenType() != TokenType.INT && ty.getTokenType() != TokenType.DOUBLE) { // as 只能接INT和DOUBLE
				throw new AnalyzeError(ErrorCode.UnExpectToken, ty.getStartPos());
			}
			if (dType == DataType.INT && ty.getTokenType() == TokenType.DOUBLE) {
				this.outfile.addInstruction(new InstructionNone(InstructionType.ItoF));
			} else if (dType == DataType.DOUBLE && ty.getTokenType() == TokenType.INT) {
				this.outfile.addInstruction(new InstructionNone(InstructionType.FtoI));
			}
			dType = this.typeMap.get(ty.getTokenType());
		}

		return dType;
	}

	/*
	 * 分析Atom语句
	 * Atom -> '-'? Item
	 * 
	 * @throws CompileError
	 */
	public DataType analyseAtom() throws CompileError {

		// 如果存在'-'号
		int isNeg = 0;
		Token token = null;
		while (it.check(TokenType.MINUS)) {
			isNeg++;
			token = it.next(); // 吃掉负号
		}
		DataType dType = analyseItem();

		for (; isNeg > 0; isNeg--) {
			if (dType != DataType.INT && dType != DataType.DOUBLE)
				throw new AnalyzeError(ErrorCode.InvalidExpr, token.getStartPos());
			if (dType == DataType.INT)
				this.outfile.addInstruction(new InstructionNone(InstructionType.NegI));
			if (dType == DataType.DOUBLE)
				this.outfile.addInstruction(new InstructionNone(InstructionType.NegF));
		}
		return dType;
	}

	/*
	 * 分析Item语句：
	 * Item -> '(' Expr ')' |IDENT | UINT_VALUE | DOUBLE_VALUE | func_call | IDENT'=' Expr
	 * 其中func_call 的first集也是IDent
	 * 
	 * @throws CompileError
	 */
	public DataType analyseItem() throws CompileError {

		DataType dType = DataType.INT;
		// 对应 '(' Expr ')'
		if (it.check(TokenType.L_PAREN)) {
			it.expect(TokenType.L_PAREN);
			dType = analyseExpr();
			it.expect(TokenType.R_PAREN);
		}
		// 对应 UINT_VALUE, 整型字面量
		else if (it.check(TokenType.UINT_LITERAL)) {
			Token uint = it.expect(TokenType.UINT_LITERAL);
			dType = DataType.INT;
			// 123456
			// 生成指令
			long tmp = ((Integer) uint.getValue()).longValue();
			this.outfile
					.addInstruction(new InstructionU64(InstructionType.Push, ((Integer) uint.getValue()).longValue()));
		}
		// 对应DOUBLE_VALUE，浮点型字面量
		else if (it.check(TokenType.DOUBLE_LITERAL)) {
			Token doubleValue = it.expect(TokenType.DOUBLE_LITERAL);
			dType = DataType.DOUBLE;
			// 生成指令
			this.outfile.addInstruction(new InstructionU64(InstructionType.Push, (long) doubleValue.getValue()));
		}
		// 对应STRING_VALUE, 字符串型字面量
		else if (it.check(TokenType.STRING_LITERAL)) {
			Token stringValue = it.expect(TokenType.STRING_LITERAL);
			dType = DataType.STRING;
			// 装入全局变量中
			int offset = this.outfile.addGlobStr(stringValue.getValueString());
			this.outfile.addInstruction(new InstructionU64(InstructionType.Push, (long) offset));
		}
		// 对应剩下的三个，其前缀相同
		else if (it.check(TokenType.IDENT)) {
			dType = analyseCallOrAssignOrIdent();
		} else {
			/* 这里 */
			System.err.println(it.peek().getTokenType().toString());
			throw new AnalyzeError(ErrorCode.UnExpectToken, it.peek().getStartPos());
		}

		return dType;
	}

	/*
	 * 分析赋值语句，Ident表达式以及函数调用表达式 
	 * call_expr -> IDENT '(' call_param_list? ')'
	 * 类型为函数的返回类型
	 * ident_expr -> IDENT
	 * 类型为IDENT的类型
	 * assign_expr -> l_expr '=' expr；
	 * l_expr -> IDENT
	 * 
	 * @throws CompileError
	 */
	public DataType analyseCallOrAssignOrIdent() throws CompileError {

		Token identToken = it.expect(TokenType.IDENT);

		boolean isLib = false;
		Symbol symbol = this.symbolTable.findAllBlockSymbol(identToken.getValueString());
		if (symbol == null) {
			if ((symbol = Lib.genLibFunc(identToken.getValueString(), identToken.getStartPos(),
					this.outfile)) == null) {
				throw new AnalyzeError(ErrorCode.NotDeclared, identToken.getStartPos());
			} else {
				// 函数调用是一个lib函数
				isLib = true;
			}
		}
		DataType leftType = symbol.getDatatype();

		// 赋值语句
		if (it.check(TokenType.ASSIGN)) {
			Token assign = it.expect(TokenType.ASSIGN);
			// 如果被赋值的是一个常量，则抛出异常
			if (symbol.getSymboltype() == SymbolType.CONST) {
				throw new AnalyzeError(ErrorCode.AssignToConstant, assign.getStartPos());
			}
			if (symbol.getSymboltype() != SymbolType.BIANLIANG) {
				throw new AnalyzeError(ErrorCode.AssignNotToVar, assign.getStartPos());
			}
			assert symbol instanceof VarSymbol;
			VarSymbol varSymbol = (VarSymbol) symbol;

			// 生成加载和存储指令
			if (varSymbol.isGlobal()) {
				this.outfile.addInstruction(new InstructionU32(InstructionType.GlobA, varSymbol.getOffset()));
			} else if (varSymbol.isParam()) {
				this.outfile.addInstruction(new InstructionU32(InstructionType.ArgA, varSymbol.getOffset()));
			} else {
				this.outfile.addInstruction(new InstructionU32(InstructionType.LocA, varSymbol.getOffset()));
			}
			DataType rightType = analyseExpr();
			this.outfile.addInstruction(new InstructionNone(InstructionType.Store64));

			TypeChecker.typeCheck(leftType, rightType, assign.getStartPos());
			// 赋值语句的类型是void
			return DataType.VOID;
		}

		// 函数调用语句
		else if (it.check(TokenType.L_PAREN)) {

			// 如果这个函数不是一个标识符号
			if (symbol.getSymboltype() != SymbolType.FUNCTION) {
				throw new AnalyzeError(ErrorCode.NotDeclared, identToken.getStartPos());
			}
			assert symbol instanceof FuncSymbol;
			FuncSymbol funcSymbol = (FuncSymbol) symbol;

			// 压入返回值slot
			if (funcSymbol.getDatatype() != DataType.VOID) {
				this.outfile.addInstruction(new InstructionU32(InstructionType.StackAlloc, (int) 1));
			} else {
				this.outfile.addInstruction(new InstructionU32(InstructionType.StackAlloc, (int) 0));
			}

			it.expect(TokenType.L_PAREN);

			ArrayList argsList = new ArrayList();
			if (it.check(TokenType.L_PAREN) || it.check(TokenType.MINUS) || it.check(TokenType.UINT_LITERAL)
					|| it.check(TokenType.STRING_LITERAL) || it.check(TokenType.DOUBLE_LITERAL)
					|| it.check(TokenType.IDENT)) {

				argsList = analyseCallParam((FuncSymbol) symbol, identToken);
			}
			TypeChecker.callArgTypeCheck(funcSymbol, argsList, identToken);

			// 如果调用的是库函数
			if (isLib) {
				this.outfile.addInstruction(new InstructionU32(InstructionType.CallName, (int) funcSymbol.getOffset()));
			} else {
				this.outfile.addInstruction(new InstructionU32(InstructionType.Call, (int) funcSymbol.getOffset()));
			}

			it.expect(TokenType.R_PAREN);
			return leftType;
		}

		// 只是一个IDENT
		else {
			// 生成指令，将变量的的值放在栈顶，其位置由符号表存储
			assert symbol instanceof VarSymbol;
			VarSymbol varSymbol = (VarSymbol) symbol;
			if (varSymbol.isGlobal()) {
				this.outfile.addInstruction(new InstructionU32(InstructionType.GlobA, (int) varSymbol.getOffset()));
			} else if (varSymbol.isParam()) {
				this.outfile.addInstruction(new InstructionU32(InstructionType.ArgA, (int) varSymbol.getOffset()));
			} else {
				this.outfile.addInstruction(new InstructionU32(InstructionType.LocA, (int) varSymbol.getOffset()));
			}
			this.outfile.addInstruction(new InstructionNone(InstructionType.Load64));
			return leftType;
		}
	}

	/*
	 * 分析函数的参数
	 * call_param_list -> expr (',' expr)*
	 * 然后检查函数调用的参数是否相同。
	 * 
	 * @param funcSymbol 函数的符号，用来获取参数信息
	 * @param funcToken  函数的Token， 用来获取位置报错
	 * @return ArrayList 用来返回调用参数，交给上层检查
	 * @throws CompileError
	 */
	public ArrayList analyseCallParam(FuncSymbol funcSymbol, Token funcToken) throws CompileError {
		// 实际参数的类型表
		ArrayList<DataType> actualArgList = new ArrayList<>();
		DataType dType = analyseExpr();
		actualArgList.add(dType);
		while (it.check(TokenType.COMMA)) {
			it.expect(TokenType.COMMA);
			dType = analyseExpr();
			actualArgList.add(dType);
		}
		return actualArgList;
	}

}