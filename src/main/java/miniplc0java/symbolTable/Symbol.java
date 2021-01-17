package miniplc0java.symbolTable;

import miniplc0java.util.Pos;

public class Symbol {
	private String name;			//name
	private SymbolType symboltype;	//符号类型
	private DataType datatype;		//对应的数据类型
	private Integer offset;
	private Pos pos;
	
	public Symbol(String name, SymbolType symboltype, DataType datatype, Integer offset, Pos pos) {
		this.name=name;
		this.symboltype=symboltype;
		this.datatype=datatype;
		this.offset=offset;
		this.pos=pos;
	}
	/* 以下是自动生成 */
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public SymbolType getSymboltype() {
		return symboltype;
	}
	public void setSymboltype(SymbolType symboltype) {
		this.symboltype = symboltype;
	}
	public DataType getDatatype() {
		return datatype;
	}
	public void setDatatype(DataType datatype) {
		this.datatype = datatype;
	}
	public Integer getOffset() {
		return offset;
	}
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	public Pos getPos() {
		return pos;
	}
	public void setPos(Pos pos) {
		this.pos = pos;
	}
	@Override
	public String toString() {
		return "Symbol [name=" + name + ", symboltype=" + symboltype + ", datatype=" + datatype + ", offset=" + offset
				+ ", pos=" + pos + "]";
	}
	
	
}
