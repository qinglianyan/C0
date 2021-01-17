package symbolTable;

public enum DataType {
	/* 数据类型 */
	/*
	 * 它是符号的一个属性，符号可以是参数、常量、变量、函数
	 * 如果是函数就表示对应返回值的类型
	 * 注意就算是void也应该有对应的类型
	 */
	VOID,	//空类型
	INT,	//整数
	STRING,	//字符串
	/* 这俩暂时搁置 */
	DOUBLE,	//浮点数
	CHAR	//字符
}
