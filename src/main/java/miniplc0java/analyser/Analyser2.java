package miniplc0java.analyser;


import java.util.*;
import miniplc0java.error.AnalyzeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.CompileError;
import miniplc0java.navm.*;
import miniplc0java.symbolTable.*;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.util.Pos;


class Analyser2 {
	SymbolIter it;						//这是封装的词法分析器
	SymbolTable symbolTable;			//符号表s
	Map<TokenType, DataType> typeMap;
//	Stack<Integer> brStack;				//这个还不知道gai
//	BranchStack branchStack;			//返回分支
	OoFile outfile;
	
	
	public Analyser2(SymbolIter symbolIter) {
		this.it=symbolIter;
		this.symbolTable=new SymbolTable();
		this.typeMap=new HashMap<>();
		this.outfile=new OoFile();
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
	public OoFile analyse() throws CompileError{
		init();
		symbolTable.initSybolTable();
		analyseProgram();//一遍读完
		windup();//收尾工作
		return this.outfile;
	}
	private void windup() {
		// TODO Auto-generated method stub
		
	}
	private void analyseProgram() {
		// TODO Auto-generated method stub
		
	}
	
	
}