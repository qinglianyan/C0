package miniplc0java.navm;

import miniplc0java.error.AnalyzeError;
import miniplc0java.symbolTable.DataType;
import miniplc0java.symbolTable.FuncSymbol;
import miniplc0java.symbolTable.SymbolType;
import miniplc0java.symbolTable.VarSymbol;
import miniplc0java.util.Pos;

import java.util.HashMap;
import java.util.Map;

public class Lib {

    private static Map<String, String> lib;

    static {
        lib = new HashMap<>();
        lib.put("getint","getint");
        lib.put("getdouble","getdouble");
        lib.put("getchar","getchar");
        lib.put("putint","putint");
        lib.put("putdouble","putdouble");
        lib.put("putchar","putchar");
        lib.put("putstr","putstr");
        lib.put("putln","putln");
    }

    /**
     * 生成lib中的函数符号,并且在oO文件中注册这个函数
     * @param funcName  函数名字
     * @param pos       在源文件中的位置
     * @return FuncSymbol 函数符号，如果不在lib中则返回空
     */
    public static FuncSymbol genLibFunc(String funcName, Pos pos, OoFile oO) throws AnalyzeError {


        //如果lib中没有这个函数
        if(lib.get(funcName)==null) {
            return null;
        }
        int offset = oO.addLibFunc(funcName);

        //如果这个函数是getint
        if(funcName.contentEquals("getint")) {
            FuncSymbol getInt = new FuncSymbol(funcName, SymbolType.FUNCTION, DataType.INT, offset, pos);
            return  getInt;
        }
        //如果这个函数是getdouble
        if(funcName.contentEquals("getdouble")) {
            FuncSymbol getDouble = new FuncSymbol(funcName, SymbolType.FUNCTION, DataType.DOUBLE, offset, pos);
            return getDouble;
        }
        //如果这个函数是getchar
        if(funcName.contentEquals("getchar")) {
            FuncSymbol getChar = new FuncSymbol(funcName, SymbolType.FUNCTION, DataType.INT, offset, pos);
            return getChar;
        }
        //如果这个函数是putint
        if(funcName.contentEquals("putint")) {

            FuncSymbol putInt = new FuncSymbol(funcName, SymbolType.FUNCTION, DataType.VOID, offset, pos);
            putInt.addArgs(new VarSymbol("param",SymbolType.BIANLIANG, DataType.INT, offset, pos));
            return  putInt;
        }
        //putdouble
        if(funcName.contentEquals("putdouble")) {
            FuncSymbol putInt = new FuncSymbol(funcName, SymbolType.FUNCTION, DataType.VOID, offset, pos);
            putInt.addArgs(new VarSymbol("param",SymbolType.BIANLIANG, DataType.DOUBLE, offset, pos));
            return  putInt;
        }
        //putchar
        if(funcName.contentEquals("putchar")) {
            FuncSymbol putChar = new FuncSymbol(funcName, SymbolType.FUNCTION, DataType.VOID, offset, pos);
            putChar.addArgs(new VarSymbol("param",SymbolType.BIANLIANG, DataType.INT, offset, pos));
            return  putChar;
        }
        //putstr
        if(funcName.contentEquals("putstr")) {
            FuncSymbol putStr = new FuncSymbol(funcName, SymbolType.FUNCTION, DataType.VOID, offset, pos);
            putStr.addArgs(new VarSymbol("param",SymbolType.BIANLIANG, DataType.STRING, offset, pos));
            return  putStr;
        }
        //putln
        if(funcName.contentEquals("putln")) {
            FuncSymbol putLn = new FuncSymbol(funcName, SymbolType.FUNCTION, DataType.VOID, offset, pos);
            return  putLn;
        }
        //都不匹配
        else {
            return null;
        }
    }
}
