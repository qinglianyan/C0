package symbolTable;

import java.util.ArrayList;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.ErrorCode;

public class BlockSympleTable {
	/* 这是一个块符号表，需要划分层次 */
	private Integer level;							//层
	private ArrayList<Symbol> symbols;				//这一层的符号表
	private BlockSympleTable prevBlockSympleTable;	//上一层符号表
	
	public BlockSympleTable(Integer level, BlockSympleTable prevBlockSympleTable) {
		this.level=level;
		this.symbols=new ArrayList<>();
		this.prevBlockSympleTable=prevBlockSympleTable;
	}
	
	/* 从所有表中找符号 */
	/* 返回null就是没找到 */
	public Symbol findAllSymbolTable(String name) {
		int temp=this.symbols.size();
		for(int i=0;i<temp;i++) {
			if(this.symbols.get(i).getName().contentEquals(name)) {
				return this.symbols.get(i);
			}
		}
		if(this.level ==0) {//gai
			return null;
		}
		else {
			return this.prevBlockSympleTable.findAllSymbolTable(name);
		}
	}
	
	/* 从当前块中找符号 */
	/* 返回null就是没找到 */
	public Symbol findThisSymbolTable(String name) {
		int temp=this.symbols.size();
		for(int i=0;i<temp;i++) {
			if(this.symbols.get(i).getName().contentEquals(name)) {
				return this.symbols.get(i);
			}
		}
		return null;
	}
	
	public boolean addSymbol(Symbol symbol) throws AnalyzeError{
		if(findThisSymbolTable(symbol.getName())!=null) {
			throw new AnalyzeError(ErrorCode.DuplicateDeclaration,symbol.getPos());
		}
		this.symbols.add(symbol);
		return true;
	}
}
