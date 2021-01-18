package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.symbolTable.DataType;
import miniplc0java.symbolTable.FuncSymbol;
import miniplc0java.tokenizer.Token;
import miniplc0java.util.Pos;

import java.util.List;

public class TypeChecker {

    /**
     * 用来检查表达式左端和右端的类型匹配
     * @param left
     * @param right
     * @return
     * @throws AnalyzeError
     */
    public static DataType typeCheck(DataType left, DataType right, Pos pos) throws AnalyzeError {

        // 表达式的左部和右部不能是void
        if (left == DataType.VOID || right == DataType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidOpVoid, pos);
        }

        // 表达式右边和左边的类型不一致
        if (left != right ){
            throw new AnalyzeError(ErrorCode.NotMatchedType, pos);
        }

        return left;
    }

    /**
     * 用来检查函数调用时的参数类型是否符合
     * @param funcSymbol
     * @param argsList
     * @return
     */
    public static DataType callArgTypeCheck(FuncSymbol funcSymbol, List<DataType> argsList, Token funcToken) throws  AnalyzeError{

        Integer argsLen = funcSymbol.getArgsList().size();
        if(argsLen != argsList.size()) {
            throw new AnalyzeError(ErrorCode.callArgNotMatched, funcToken.getStartPos());
        }

        for(int i=0; i<argsLen; i++){
            if(funcSymbol.getArgsList().get(i).getDatatype() != argsList.get(i)) {
                throw new AnalyzeError(ErrorCode.callArgNotMatched, funcToken.getStartPos());
            }
        }

        return funcSymbol.getDatatype();
    }
}
