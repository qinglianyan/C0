package miniplc0java.symbolTable;

import miniplc0java.util.Pos;

public class VarSymbol extends  Symbol{
	/* 变量声明的符号，是符号的子类 */
    //是否已经初始化
    private boolean initialized;
    //是否是全局变量
    private boolean isGlobal;
    //是否是函数参数
    private boolean isParam;

    public VarSymbol(String name, SymbolType symbolType, DataType dType, Integer offset, Pos pos) {
        super(name, symbolType, dType, offset, pos);
        this.initialized = false;
        this.isGlobal = false;
        this.isParam = false;
    }

    public boolean setInitialized(boolean initialized) {
        this.initialized = initialized;
        return this.initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public VarSymbol setGlobal(boolean global) {
        isGlobal = global;
        return  this;
    }

    public boolean isParam() {
        return isParam;
    }

    public VarSymbol setParam(boolean param) {
        isParam = param;
        return  this;
    }
}
