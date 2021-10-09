package edu.tamu.csce434.baseline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.tamu.csce434.vm.DLX;

public class Compiler {
	/**
	 * The layout of the memory ------------------ High | | -------->Data Pointer
	 * (DP) = Memory Size -1 . Global Vars . | | | ------------------ | Grow from
	 * high address to low address | | | . Parameters . \|/ | | ------------------ |
	 * | -------->Frame Pointer (FP) = DP - Global Vars Size . Local Vars . | |
	 * -------->Stack Pointer (SP) = FP - FP Size ------------------ Low
	 */

	/**
	 * The scanner reads token from the source code.
	 */
	private Scanner scanner;

	Map<String, Integer> tokensList = new HashMap<String, Integer>();

	/**
	 * Global Var Map, var name -> var related information
	 */
	private Map<String, Variable> globalVarMap = new HashMap<String, Variable>();
	private List<String> funcList = new Vector<String>();
	private List<Integer> funcPos = new Vector<Integer>();
	/**
	 * 1 -> return 0 -> no return
	 */
	private List<Integer> okToReturn = new Vector<Integer>();

	int buf[] = new int[2400];
	boolean regs[] = new boolean[32];
	int PC;
	boolean canReturn;
	/**
	 * Return Result
	 * Return Result
	 */
	static final int RR = 27;
	/**
	 * Frame Pointer, i.e., base pointer
	 */
	static final int FP = 28;
	/**
	 * Stack Pointer, i.e., the top of the stack
	 */
	static final int SP = 29;
	/**
	 * Data Pointer, i.e., the bottom of the memory for data (global data and stack)
	 */
	static final int DP = 30;
	/**
	 * Branch Address, i.e., the return address
	 */
	static final int BA = 31; // branch address

	// Use this function to print errors
	private void printError(int i) {
		System.out.println("ERROR: " + i);
		System.exit(-10);
	}

	public void BuildTokensList() {
        tokensList.put("let",77);
		tokensList.put("then", 41);
		tokensList.put("do", 42);
		tokensList.put("od", 81);
		tokensList.put("fi", 82);
		tokensList.put("else", 90);
		tokensList.put("call", 100);
		tokensList.put("if", 101);
		tokensList.put("while", 102);
		tokensList.put("return", 103);
		tokensList.put("var", 110);
		tokensList.put("array", 111);
		tokensList.put("function", 112);
		tokensList.put("procedure", 113);
		tokensList.put("main", 200);
		tokensList.put("identifier", 61);
		tokensList.put("number", 60);
		tokensList.put("eof", 255);
		tokensList.put("error", 0);
		tokensList.put("*", 1);
		tokensList.put("/", 2);
		tokensList.put("+", 11);
		tokensList.put("-", 12);
		tokensList.put("==", 20);
		tokensList.put("!=", 21);
		tokensList.put("<", 22);
		tokensList.put(">=", 23);
		tokensList.put("<=", 24);
		tokensList.put(">", 25);
		tokensList.put(".", 30);
		tokensList.put(",", 31);
		tokensList.put("[", 32);
		tokensList.put("]", 34);
		tokensList.put(")", 35);
		tokensList.put("<-", 40);
		tokensList.put("(", 50);
		tokensList.put(";", 70);
		tokensList.put("}", 80);
		tokensList.put("{", 150);
	}

	// Constructor
	public Compiler(String fileName) {
		scanner = new Scanner(fileName);

		funcList.add("inputnum");
		funcPos.add(-1);
		funcList.add("outputnewline");
		funcPos.add(-2);
		funcList.add("outputnum");
		funcPos.add(-3);
		okToReturn.add(0);
		okToReturn.add(0);
		okToReturn.add(0);
		BuildTokensList();
		for (int i = 0; i <= 31; i++)
			regs[i] = false;
		regs[0] = true; // R0 is always zero
		regs[BA] = true; // Return address of branch instruction
		regs[DP] = true; // Data pointer
		regs[SP] = true; // Stack pointer
		regs[FP] = true; // Frame pointer
		regs[RR] = true; // Return result
		PC = 0;
	}

	public void Load(Result x) {
		if (x.kind == Result.VAR) {
			x.regno = AllocateReg();
			buf[PC++] = DLX.assemble(DLX.LDW, x.regno, DP, x.address);
			x.kind = Result.REG;
		} else if ((x.kind == Result.PARAM) || (x.kind == Result.LOCAL)) {
			x.regno = AllocateReg();
			buf[PC++] = DLX.assemble(DLX.LDW, x.regno, FP, x.address);
			x.kind = Result.REG;
		} else if (x.kind == Result.CONST) {
			if (x.value == 0)
				x.regno = 0;
			else { // constant not 0
				x.regno = AllocateReg();
				buf[PC++] = DLX.assemble(DLX.ADDI, x.regno, 0, x.value);
			}
			x.kind = Result.REG;
		}
	}

	private int AllocateReg() {
		for (int i = 1; i <= 26; i++)
			if (regs[i] == false) {
				regs[i] = true;
				return i;
			}
		return 0;
	}

	private void DeallocateReg(int regno) {
		// never deallocate special regs
		if (regno <= 26 && regno >= 1) {
			regs[regno] = false;
		}
	}

	/**
	 * Check if the token kind
	 * 
	 * @param s
	 * @return
	 */
	private boolean peek(String s) {
		int temp = scanner.sym;
        //System.out.println(s+" "+temp);
		if (scanner.sym == tokensList.get(s)) {
			return true;
		}
		return false;
	}

	/**
	 * This is the function in Compiler, and it reads next token.
	 */
	private void advance() {
		scanner.Next();
	}

	/**
	 * Use this function to accept a Token and and to get the next Token from the
	 * Scanner
	 * 
	 * @param s
	 * @return
	 */
	private boolean accept(String s) {
		if (peek(s)) {
			advance();
			return true;
		}
		return false;
	}

	/**
	 * Use this function whenever your program needs to expect a specific token
	 * 
	 * @param s
	 */
	private void expect(String s) {
		if (accept(s))
			return;

		printError(tokensList.get(s));

	}

	public Result factor(List<String> paramsList, List<String> localVars) {
		Result x = new Result();
		if (peek("identifier")) {
			String name = scanner.Id2String(scanner.id);
			if (localVars != null && localVars.contains(name)) {
				x.kind = Result.LOCAL;
				x.address = (localVars.indexOf(name) + 1) * -4;
				scanner.removeIdent(scanner.id);
			} else if (paramsList != null && paramsList.contains(name)) {
				x.kind = Result.PARAM;
				x.address = 4 * (paramsList.size() - paramsList.indexOf(name) + 1);
				scanner.removeIdent(scanner.id);
			} else {
				if (scanner.duplicate == true) {
					x.kind = Result.VAR;
					x.address = (scanner.id + 1) * -4;
				} else
					printError(-100);
			}
			advance();
		} else if (peek("number")) {
			x.kind = Result.CONST;
			x.value = scanner.val;
			advance();
		} else if (peek("(")) {
			advance();
			x = expression(paramsList, localVars);
			expect(")");
		} else if (peek("call")) {
			x = funcCall(paramsList, localVars);
		}
		return x;
	}

	public Result term(List<String> paramsList, List<String> localVars) {
		Result x, y = new Result();
		int op;
		x = factor(paramsList, localVars);
		while (peek("*") || peek("/")) {
			if (peek("*")) {
				op = DLX.MUL;
			} else {
				op = DLX.DIV;
			}
			advance();
			y = factor(paramsList, localVars);
			Compute(op, x, y);
		}
		return x;
	}

	private void Compute(int op, Result x, Result y) {
		if ((x.kind == Result.CONST) && (y.kind == Result.CONST)) {
			if (op == DLX.ADD)
				x.value += y.value;
			else if (op == DLX.SUB)
				x.value -= y.value;
			else if (op == DLX.MUL)
				x.value *= y.value;
			else if (op == DLX.DIV)
				x.value /= y.value;
		} else {
			Load(x);
			if (x.regno == 0) {
				x.regno = AllocateReg();
				buf[PC++] = DLX.assemble(DLX.ADD, x.regno, 0, 0);

			}
			if (y.kind == Result.CONST)
				buf[PC++] = DLX.assemble(op + 16, x.regno, x.regno, y.value); // immediate
																				// form
			else {
				Load(y);
				buf[PC++] = DLX.assemble(op, x.regno, x.regno, y.regno);
				DeallocateReg(y.regno);
			}
		}
	}

	public Result expression(List<String> paramsList, List<String> localVars) {
		Result x, y = new Result();
		int op;
		x = term(paramsList, localVars);
		while (peek("+") || peek("-")) {
			if (peek("+")) {
				op = DLX.ADD;
			} else {
				op = DLX.SUB;
			}
			advance();
			y = term(paramsList, localVars);
			Compute(op, x, y);
		}
		return x;
	}

	public void assignment(List<String> paramsList, List<String> localVars) {

		Result y = new Result();

		int offset = 0;
		int type = 0;
		String name = scanner.Id2String(scanner.id);
		if ((!paramsList.isEmpty()) && (paramsList.contains(name))) {
			type = 1; // parameter
			offset = 4 * (paramsList.size() - paramsList.indexOf(name) + 1);
			scanner.removeIdent(scanner.id);
		} else if ((!localVars.isEmpty()) && (localVars.contains(name))) {
			type = 2; // local variable
			offset = (localVars.indexOf(name) + 1) * -4;
			scanner.removeIdent(scanner.id);
		} else {
			if (scanner.duplicate == true) {
				type = 3; // global var
				offset = (scanner.id + 1) * -4;
			} else
				printError(-100);
		}

		advance();
		expect("<-");
		y = expression(paramsList, localVars);
		Load(y);
		if (type == 3)
			buf[PC++] = DLX.assemble(DLX.STW, y.regno, DP, offset);
		else
			buf[PC++] = DLX.assemble(DLX.STW, y.regno, FP, offset);
		DeallocateReg(y.regno);
	}

	public List<Result> params(List<String> paramsList, List<String> localVars) {
		List<Result> paramList = new Vector<Result>();
		Result x = new Result();
		expect("(");
		if (!peek(")")) {
			paramList.add(expression(paramsList, localVars));
			while (accept(",")) {
				x = expression(paramsList, localVars);
				paramList.add(x);
			}
		}
		expect(")");
		return paramList;
	}

	public List<String> formalParams() {
		List<String> paramList = new Vector<String>();
		expect("(");
		if (!peek(")")) {
			paramList.add(scanner.Id2String(scanner.id));
			scanner.removeIdent(scanner.id);
			advance();
			while (accept(",")) {
				paramList.add(scanner.Id2String(scanner.id));
				scanner.removeIdent(scanner.id);
				advance();
			}
		}
		expect(")");

		return paramList;
	}

	public Result funcCall(List<String> paramsList, List<String> localVars) {

		List<Integer> registers = new Vector<Integer>();
		Result x = new Result();
		int i = 0;
		int returnIndex;
		List<Result> parameterList = new Vector<Result>();
		expect("call");
		if ((peek("identifier")) && (funcList.contains(scanner.Id2String(scanner.id)))) {

			i = funcList.indexOf(scanner.Id2String(scanner.id));
			returnIndex = okToReturn.get(i);
			i = funcPos.get(i);
			scanner.removeIdent(scanner.id);
			advance();
			if (peek("("))
				parameterList = params(paramsList, localVars);

			if (i < 0) { // predefined function
				switch (i) {
					case -1: // predefined InputNum()
						x.regno = AllocateReg();
						buf[PC++] = DLX.assemble(DLX.RDI, x.regno);
						break;
					case -2: // predefined OutputNewLine()
						buf[PC++] = DLX.assemble(DLX.WRL);
						break;
					case -3: // predefined OutputNum()
						Result y = parameterList.get(0);
						Load(y);
						buf[PC++] = DLX.assemble(DLX.WRD, y.regno);
						DeallocateReg(y.regno);
						break;
				}
			} else { // other user-defined function - callee saved style
				// push registers
				for (int k = 1; k <= 26; k++)
					if (regs[k]) {
						registers.add(k);
						buf[PC++] = DLX.assemble(DLX.PSH, k, SP, -4);
						regs[k] = false;
					}

				// push parameter
				for (Result elem : parameterList) {
					Load(elem);
					buf[PC++] = DLX.assemble(DLX.PSH, elem.regno, SP, -4);
					DeallocateReg(elem.regno);
				}

				// Jump to target function
				buf[PC++] = DLX.assemble(DLX.JSR, i * 4);

				// unwind the registers!

				for (int j = registers.size(); j > 0; j--) {
					buf[PC++] = DLX.assemble(DLX.POP, registers.get(j - 1), SP, 4);
					regs[registers.get(j - 1)] = true;
				}

			}

			// TODO If we can use the reg[RR] directly?
			if (returnIndex == 1) {
				x.regno = AllocateReg();
				buf[PC++] = DLX.assemble(DLX.ADDI, x.regno, RR, 0);
				// buf[PC++] = DLX.assemble(DLX.LDW, x.regno, DP, -4);

			}
		}
		return x;
	}

	public Result relation(List<String> paramsList, List<String> localVars) {
		Result e1, e2;
		int op = 0;

		e1 = expression(paramsList, localVars);
		op = scanner.sym;
		if ((op >= 20) && (op <= 25)) {
			advance();
			e2 = expression(paramsList, localVars);
			if ((e1.kind == Result.CONST) && (e2.kind == Result.CONST))
				e1.value -= e2.value;
			else
				Compute(DLX.CMP, e1, e2);
			e1.kind = Result.CONDITION;
			e1.cond = op;
			e1.fixuplocation = 0;
		} else
			printError(20);
		return e1;
	}

	private int NegatedBranchOp(int tokenValue) {
		int i = tokenValue % 10;
		if (i % 2 == 0)
			return 41 + i;
		else
			return 39 + i;
	}

	private void CondNegBraFwd(Result x) {
		x.fixuplocation = PC;
		buf[PC++] = DLX.assemble(NegatedBranchOp(x.cond), x.regno, 0);
	}

	private void UnCondBraFwd(Result x) {
		buf[PC++] = DLX.assemble(DLX.BEQ, 0, x.fixuplocation);
		x.fixuplocation = PC - 1;
	}

	private void Fixup(int loc) {
		buf[loc] = buf[loc] & 0xffff0000;
		buf[loc] = buf[loc] + ((PC - loc) & 0xffff);
	}

	private void FixAll(int loc) {
		int next;
		while (loc != 0) {
			next = buf[loc] & 0x0000ffff;
			Fixup(loc);
			loc = next;
		}
	}

	public void returnStatement(List<String> paramsList, List<String> localVars) {
		expect("return");
		Result x = new Result();
		if (canReturn == false) {
			buf[PC++] = DLX.assemble(DLX.ADD, SP, FP, 0);
			buf[PC++] = DLX.assemble(DLX.POP, FP, SP, 4);
			buf[PC++] = DLX.assemble(DLX.POP, BA, SP, 4 * (paramsList.size()));
			buf[PC++] = DLX.assemble(DLX.RET, BA);
		} else {
			x = expression(paramsList, localVars);
			Load(x);
			buf[PC++] = DLX.assemble(DLX.ADD, SP, FP, 0);
			buf[PC++] = DLX.assemble(DLX.POP, FP, SP, 4);
			buf[PC++] = DLX.assemble(DLX.POP, BA, SP, 4 * (paramsList.size() + 1));
			// TODO we should use the RR register to store the return result.
			// buf[PC++] = DLX.assemble(DLX.STW, x.regno, DP, -4);
			buf[PC++] = DLX.assemble(DLX.ADDI, RR, x.regno, 0);
			buf[PC++] = DLX.assemble(DLX.RET, BA);
			DeallocateReg(x.regno);
		}
	}

	public void statement(List<String> paramsList, List<String> localVars) {
        if(peek("let")) {
            expect("let");
            if(peek("identifier")) {
                    assignment(paramsList, localVars);
            } else {
                printError(-1);
            }
        } else if (peek("identifier")) {
			assignment(paramsList, localVars);
		} else if (peek("call")) {
			funcCall(paramsList, localVars);
		} else if (peek("if")) {
			ifStatement(paramsList, localVars);
		} else if (peek("while")) {
			whileStatement(paramsList, localVars);
		} else if (peek("return")) {
			returnStatement(paramsList, localVars);
		} else {
			printError(-1);
		}
	}

	public void statementSequence(List<String> paramsList, List<String> localVars) {
		statement(paramsList, localVars);
		while (accept(";")) {
			if ( scanner.sym == 80 || scanner.sym == 81 || scanner.sym == 82 || scanner.sym == 90)
				break;

            //System.out.println("Statement"+scanner.sym);
			statement(paramsList, localVars);
		}
	}

	public void whileStatement(List<String> paramsList, List<String> localVars) {

		expect("while");
		int looplocation = PC;
		Result x = new Result();
		x = relation(paramsList, localVars);
		CondNegBraFwd(x);
		expect("do");
		statementSequence(paramsList, localVars);
		buf[PC] = DLX.assemble(DLX.BEQ, 0, looplocation - PC);
		PC++;
		Fixup(x.fixuplocation);
		expect("od");
	}

	public void ifStatement(List<String> paramsList, List<String> localVars) {

		expect("if");
		Result x = new Result();
		Result follow = new Result();
		follow.fixuplocation = 0;
		x = relation(paramsList, localVars);
		CondNegBraFwd(x);
		expect("then");
		statementSequence(paramsList, localVars);
		if (peek("else")) {
			advance();
			UnCondBraFwd(follow);
			Fixup(x.fixuplocation);
			statementSequence(paramsList, localVars);
		} else
			Fixup(x.fixuplocation);

		expect("fi");
		FixAll(follow.fixuplocation);
	}

	public void declarations(Map<String, Variable> varMap) {
		expect("var");
		expect("identifier");

		while (accept(","))
			expect("identifier");
		expect(";");
	}

	public List<String> localVarDec() {
		List<String> localVarList = new Vector<String>();
		expect("var");
		localVarList.add(scanner.Id2String(scanner.id));
		scanner.removeIdent(scanner.id);
		advance();
		while (accept(",")) {
			localVarList.add(scanner.Id2String(scanner.id));
			scanner.removeIdent(scanner.id);
			advance();
		}
		expect(";");
		return localVarList;
	}

	public void functionDecl() {
		List<String> paramsList = new Vector<String>();
		List<String> localVars = new Vector<String>();
		for (int i = 0; i <= 31; i++)
			regs[i] = false;
		regs[0] = true; // R0 is always zero
		regs[BA] = true; // Return address of branch instruction
		regs[DP] = true; // Data pointer
		regs[SP] = true; // Stack pointer
		regs[FP] = true; // Frame pointer
		regs[RR] = true; // Return result
		if (peek("function") || peek("procedure"))
			advance();
		if (PC == 0)
			PC = 1;
		if (peek("identifier")) {
			funcList.add(scanner.Id2String(scanner.id));
			funcPos.add(PC);
			if (canReturn == true)
				okToReturn.add(1);
			else
				okToReturn.add(0);
			scanner.removeIdent(scanner.id);
			advance();
		}
		if (peek("(")) {
			paramsList = formalParams();
		}
		expect(";");

		if (peek("var")) {
			localVars = localVarDec();
		}
		expect("{");

		// set up FP and SP

		buf[PC++] = DLX.assemble(DLX.PSH, BA, SP, -4);
		buf[PC++] = DLX.assemble(DLX.PSH, FP, SP, -4);
		buf[PC++] = DLX.assemble(DLX.ADD, FP, 0, SP);
		buf[PC++] = DLX.assemble(DLX.SUBI, SP, SP, 4 * (localVars.size() + 1));

		statementSequence(paramsList, localVars);
		if (peek("}")) {
			// epilogue
			if (canReturn == false) {
				buf[PC++] = DLX.assemble(DLX.ADD, SP, FP, 0);
				buf[PC++] = DLX.assemble(DLX.POP, FP, SP, 4);
				buf[PC++] = DLX.assemble(DLX.POP, BA, SP, 4 * (paramsList.size()));
				buf[PC++] = DLX.assemble(DLX.RET, BA);
			} else {
				// the epilogue of function call is finished in returnStatement.
				// So here do nothing.
			}
			advance();
		}

		expect(";");
	}

	/**
	 * The parsing function. 1. Check "main" 2. Parse varDecl. varDecl is the
	 * declaration of global vars. 3. Parse funcDecl. funcDecl is the declaration of
	 * functions. 4. Check "{" 5. Prepare for the main function. 6. Parse
	 * statSequence. statSequence is the main function. 7. Check "}" 8. Check "." 9.
	 * End the execution.
	 * 
	 * @return the machine code which runs on the DLX VM.
	 */
	public int[] getProgram() {
		int totalGlobals = 0;
		List<String> pList = new Vector<String>();
		List<String> vList = new Vector<String>();

		/**
		 * Step 1. Check "main"
		 */
		expect("main");

		/**
		 * Step 2. Parse varDecl.
		 */
		if (peek("var"))
			declarations(globalVarMap);
		totalGlobals = scanner.getIdentSize();
        //System.out.println("Var dec done");
		/**
		 * Step 3. Parse funcDecl.
		 */
		//while (peek("function") || peek("procedure")) {
			//if (peek("function"))
				//canReturn = true;
			//else
				//canReturn = false;
			//functionDecl();
		//}

		/**
		 * Step 4. Check "{".
		 */
		expect("{");

		/**
		 * Step 5. Prepare for the main function.
		 */
		if (PC != 1) { // there is at least one function/procedure
			buf[0] = DLX.assemble(DLX.BEQ, 0, PC);
		}
		buf[PC++] = DLX.assemble(DLX.SUBI, FP, DP, 4 * (totalGlobals + 1));
		buf[PC++] = DLX.assemble(DLX.SUBI, SP, DP, 4 * (totalGlobals + 2));
		for (int i = 0; i <= 31; i++)
			regs[i] = false;
		regs[0] = true; // R0 is always zero
		regs[BA] = true; // Return address of branch instruction
		regs[DP] = true; // Data pointer
		regs[SP] = true; // Stack pointer
		regs[FP] = true; // Frame pointer
		regs[RR] = true; // Return result

		/**
		 * Step 6. Parse statSequence.
		 */
		statementSequence(pList, vList);

		/**
		 * Step 7. Check "}"
		 */
		expect("}");

		/**
		 * Step 8. Check "."
		 */
		expect(".");

		/**
		 * Step 9. End the execution.
		 */
		scanner.closefile();
		buf[PC] = DLX.assemble(DLX.RET, 0);
		return buf;
	}

}
