package edu.tamu.csce434.baseline;

public class Result {
	int kind;		// const = 1, var = 2, reg = 3, condition = 4  
	int value; 		// value if it is a constant
	int address; 	// address if it is a variable;
	int regno;		// register number if it is a reg or a condition
	int cond, fixuplocation;		// if it is a condition

	final static int CONST = 1;
	final static int VAR = 2;
	final static int REG = 3;
	final static int CONDITION = 4;
	final static int PARAM = 5;
	final static int LOCAL = 6;
}
