package miniplc0java.analyser;

import java.util.*;
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

class Analyser2 {
	SymbolIter it; // 这是封装的词法分析器
	SymbolTable symbolTable; // 符号表s
	Map<TokenType, DataType> typeMap;
//	Stack<Integer> brStack;				//这个还不知道gai
//	BranchStack branchStack;			//返回分支
	OoFile outfile;

	public Analyser2(SymbolIter symbolIter) {
		this.it = symbolIter;
		this.symbolTable = new SymbolTable();
		this.typeMap = new HashMap<>();
		this.outfile = new OoFile();
//		this.branchStack=new BranchStack();
//		this.brStack=new Stack();
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

	private void windup() {
		// TODO Auto-generated method stub

	}

	/*
	 * 程序结构 program -> decl_stmt* function* 程序->声明语句 或 函数
	 * 
	 * @throws CompileError
	 */
	/**
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

	private void analyseFuncDecl() {
		// TODO Auto-generated method stub

	}

	private void analyseConstDecl() {
		// TODO Auto-generated method stub

	}

	/*
	 * 分析变量声明 let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
	 * 
	 * @throws CompileError
	 */
	private void analyseLetDecl() throws CompileError {
		// TODO Auto-generated method stub
		boolean isInit = false;//用来判断变量是否初始化
		it.expect(TokenType.LET_KW);
		Token variable=it.expect(TokenType.IDENT);
		it.expect(TokenType.COLON);
		
		Token ty=it.next();
		if(ty.getTokenType()!=TokenType.INT&&ty.getTokenType()!=TokenType.DOUBLE) {
			throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
		}
		if(ty.getTokenType()==TokenType.VOID) {
			throw new AnalyzeError(ErrorCode.InvalidVoid, ty.getStartPos());
		}
		
		VarSymbol varSymbol = new VarSymbol(variable.getValueString(), SymbolType.BIANLIANG, this.typeMap.get(ty.getTokenType()), 0, variable.getStartPos());

        //写入oOfile
        int offset = 0;
        if(this.symbolTable.getLevel() ==0 ) {
            varSymbol.setGlobal(true);
            offset = this.outfile.addGlobVar(varSymbol);
        } else {
            offset = this.outfile.addLocalVar(varSymbol);
        }
        varSymbol.setOffset(offset);

        if(it.nextIf(TokenType.ASSIGN)!=null){
            isInit = true;
            if(this.symbolTable.getLevel() == 0 ){
                this.outfile.addInstruction(new InstructionU32(InstructionType.GlobA, (int)varSymbol.getOffset()));
            }
            else {
                this.outfile.addInstruction(new InstructionU32(InstructionType.LocA, (int)varSymbol.getOffset()));
            }
            DType exprDType = analyseExpr();
            TypeChecker.typeCheck(this.typeMap.get(ty.getTokenType()), exprDType, variable.getStartPos());
            this.oO.addInstruction(new InstructionNone(InstructionType.Store64));

        }

        it.expectToken(TokenType.SEMICOLON);

        // 在符号表中注册一个新的变量符号

        varSymbol.setInitialized(isInit);
        // 如果是全局变量
        this.symbolTable.insertSymbol(varSymbol);
        //解析变量声明分析完毕
        return;
	}

}