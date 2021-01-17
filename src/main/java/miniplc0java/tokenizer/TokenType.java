package miniplc0java.tokenizer;

public enum TokenType {
    /** 空 */
    None,
    /** 关键字（补） */
    INT,			//整型
    STRING,			//字符串
    VOID,			//void
    DOUBLE,			//浮点数
    CHAR,			//字符
    FN_KW,			//'fn'
    LET_KW,     	//'let'
    CONST_KW,	    //'const'
    AS_KW,      	//'as'
    WHILE_KW,   	//'while'
    IF_KW,      	//'if'
    ELSE_KW,    	//'else'
    RETURN_KW,  	//'return'

    // 这两个是扩展 c0 的
    BREAK_KW,   	//'break'
    CONTINUE_KW,	//'continue'
    
    /** 字面量 *///改
    digit,			//数字
    UINT_LITERAL,	//无符号整数
    STRING_LITERAL,	//字符串常量
    DOUBLE_LITERAL,	//double浮点数
    CHAR_LITERAL,	//char字面量
    
    
    /** 标识符 */
    IDENT,      	//[_a-zA-Z] [_a-zA-Z0-9]*
    				//由下划线或字母开头，后面可以接零或多个下划线、字母或数字。
    				//标识符不能和关键字重复。
    /** 运算符 */
    PLUS,			// +
    MINUS,			// -
    MUL,			// *
    DIV,			// /
    ASSIGN,			// =
    EQ,				// ==
    NEQ,			// !=
    LT,				// <
    GT,				// >
    LE,				// <=
    GE,				// >=
    L_PAREN,		// (
    R_PAREN,		// )
    L_BRACE,		// {
    R_BRACE,		// }
    ARROW,			// ->
    COMMA,			// ,
    COLON,			// :
    SEMICOLON,		// ;
    
    /** 注释 */
    COMMENT,		// //
    /** 文件尾 */
    EOF;

    
    
}
