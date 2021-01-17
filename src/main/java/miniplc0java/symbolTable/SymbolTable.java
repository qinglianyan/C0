package miniplc0java.symbolTable;

import java.util.ArrayList;

import miniplc0java.error.AnalyzeError;

public class SymbolTable {
	private ArrayList<BlockSympleTable> sympletable;
	private Integer level;//当前level
	
	public SymbolTable() {
		this.sympletable=new ArrayList<>();
		this.level=-1;
	}
	
	public void initSybolTable() {
		this.level++;
		BlockSympleTable blockSympleTable=new BlockSympleTable(this.level, null);
		this.sympletable.add(blockSympleTable);
	}
	
	/* 添加一个新的block块，这时候栈顶是当前块 */
	public void addBlockTable() {
		this.level++;
		BlockSympleTable prevBlock=this.sympletable.get(this.sympletable.size()-1);
		BlockSympleTable newBlock=new BlockSympleTable(level, prevBlock);
		this.sympletable.add(newBlock);
	}
	
	/* 移除已经分析完的块 */
	public void removeBlockTable() {
		this.level--;
		this.sympletable.remove(this.sympletable.size()-1);
	}
	
	/* 从当前块开始找符号 */
	public Symbol findAllBlockSymbol(String name) throws AnalyzeError{
		BlockSympleTable thisBlock=this.sympletable.get(this.sympletable.size()-1);
		return thisBlock.findAllSymbolTable(name);
	}
	
	/* 只找当前块中的符号 */
	public Symbol findThisBlockSymbol(String name) throws AnalyzeError{
		BlockSympleTable thisBlock=this.sympletable.get(this.sympletable.size()-1);
		return thisBlock.findThisSymbolTable(name);
	}
	
	/* 添加一个symbol */
	public boolean insertSymbol(Symbol symbol) throws AnalyzeError{
		BlockSympleTable thisBlock=this.sympletable.get(this.sympletable.size()-1);
		return thisBlock.addSymbol(symbol);
	}
	
	/* 自动生成 */
	public int getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}
}
