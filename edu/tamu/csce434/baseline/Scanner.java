package edu.tamu.csce434.baseline;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

public class Scanner {
	public int sym = -1;
	public int val;
	public int id;
	/**
	 * if the incoming identifier is already in indentlist, then duplicate is true.
	 * It indicate that the identifier is a global var.
	 */
	public boolean duplicate; // if incoming identifier is already in identlist

	private LineNumberReader lineNumberReader;
	private int lastCharacter;
	/**
	 * identList store the global var.
	 * TODO Maybe we need a global var list. That is more clear.
	 */
	private List<String> identList = new Vector<String>();
	private List<Integer> identVal = new Vector<Integer>();

	Map<String, Integer> reservedWords = new HashMap<String, Integer>();
	Map<String, Integer> otherTokens = new HashMap<String, Integer>();

	/**
	 * put the reserved words into reservedWords map (name -> symbol id)
	 * put the reserved symbols into otherTokens map (name -> symbol id)
	 */
	private void prePopulate() {
        reservedWords.put("let", 77);
		reservedWords.put("then", 41);
		reservedWords.put("do", 42);
		reservedWords.put("od", 81);
		reservedWords.put("fi", 82);
		reservedWords.put("else", 90);
		reservedWords.put("call", 100);
		reservedWords.put("if", 101);
		reservedWords.put("while", 102);
		//reservedWords.put("return", 103);
		reservedWords.put("var", 110);
		reservedWords.put("array", 111);
		reservedWords.put("function", 112);
		//reservedWords.put("procedure", 113);
		reservedWords.put("main", 200);

		otherTokens.put("eof", 255);
		otherTokens.put("error", 0);
		otherTokens.put("*", 1);
		otherTokens.put("/", 2);
		otherTokens.put("+", 11);
		otherTokens.put("-", 12);
		otherTokens.put("==", 20);
		otherTokens.put("!=", 21);
		otherTokens.put("<", 22);
		otherTokens.put(">=", 23);
		otherTokens.put("<=", 24);
		otherTokens.put(">", 25);
		otherTokens.put(".", 30);
		otherTokens.put(",", 31);
		otherTokens.put("[", 32);
		otherTokens.put("]", 34);
		otherTokens.put(")", 35);
		otherTokens.put("<-", 40);
		otherTokens.put("(", 50);
		otherTokens.put(";", 70);
		otherTokens.put("}", 80);
		otherTokens.put("{", 150);
	}

	public char getLastCharacter() {
		return (char) lastCharacter;
	}

	public void closefile() {
		try {
			lineNumberReader.close();
		} catch (IOException e) {
			System.err.println("Error closing file reader");
			System.exit(-1);
		}
	}

	/**
	 * Read the next token. Update the value of sym, identList, and identVal.
	 */
	public void Next() {
		if (sym == 255) {
			System.err.println("Scanner error: called Next() on EOF");
			return;
		}

		while (Character.isWhitespace(getLastCharacter()))
			Advance();

		if (lastCharacter == -1) {
			sym = 255;
			return;
		}

		if (Character.isLetter(getLastCharacter())) {
			String tmptoken = Character.toString(getLastCharacter());
			Advance();

			while (Character.isLetter(getLastCharacter())
					|| Character.isDigit(getLastCharacter())) {
				tmptoken = tmptoken.concat(Character
						.toString(getLastCharacter()));
				Advance();
			}

			if (reservedWords.containsKey(tmptoken)) {
				sym = reservedWords.get(tmptoken);
				return;
			}

			sym = 61;
			if (!identList.contains(tmptoken)) {
				id = identList.size();
				identList.add(tmptoken);
				identVal.add(0);
				duplicate = false;
			} else {
				id = identList.indexOf(tmptoken);
				duplicate = true;
			}

			return;
		}

		else if (Character.isDigit(getLastCharacter())) {
			String tmptoken = Character.toString(getLastCharacter());
			Advance();
			while (Character.isDigit(getLastCharacter())) {
				tmptoken = tmptoken.concat(Character
						.toString(getLastCharacter()));
				Advance();
			}
			sym = 60;
			val = Integer.parseInt(tmptoken);
			return;
		}

		else if (getLastCharacter() == ';' || getLastCharacter() == '.'
				|| getLastCharacter() == '(' || getLastCharacter() == ')'
				|| getLastCharacter() == '{' || getLastCharacter() == '}'
				|| getLastCharacter() == ',' || getLastCharacter() == '='
				|| getLastCharacter() == '<' || getLastCharacter() == '+'
				|| getLastCharacter() == '-' || getLastCharacter() == '*'
				|| getLastCharacter() == '/' || getLastCharacter() == '!'
				|| getLastCharacter() == '>' || getLastCharacter() == ']'
				|| getLastCharacter() == '[') {
			String tmptoken = Character.toString(getLastCharacter());
			Advance();

			if ((tmptoken.equals("=") && getLastCharacter() == '=')
					|| (tmptoken.equals("!") && getLastCharacter() == '=')
					|| (tmptoken.equals(">") && getLastCharacter() == '=')
					|| (tmptoken.equals("<") && getLastCharacter() == '=')
					|| (tmptoken.equals("<") && getLastCharacter() == '-')) {
				tmptoken = tmptoken.concat(Character
						.toString(getLastCharacter()));
				Advance();
			}

			if (otherTokens.containsKey(tmptoken)) {
				sym = otherTokens.get(tmptoken);
				return;
			}
		}

		sym = 0;
		return;

	}

	public void Advance() {
		try {
			lastCharacter = lineNumberReader.read();
		} catch (IOException e) {
			System.err.println("Error reading character");
			System.exit(-1);
		}
	}

	public Scanner(String fileName) {
		try {
			lineNumberReader = new LineNumberReader(new FileReader(fileName));
			lineNumberReader.setLineNumber(1);
		} catch (IOException e) {
			System.err.println("File " + fileName + " could not be read");
			System.exit(-1);
		}

		prePopulate();
		Advance();
		Next();
	}

	/**
	 * Converts given id to name; returns null in case of error
	 */
	public String Id2String(int id) {
		if ((id < 0) || (id > identList.size()))
			return null;
		else
			return identList.get(id);
	}

	public void Error(String errorMsg) {
		System.err.println("Scanner error: " + errorMsg);
		sym = 0;

	}

	/**
	 * Converts given name to id; returns -1 in case of error
	 */
	public int String2Id(String name) {
		return (identList.indexOf(name));
	}

	public void removeIdent(int index) {
		if (duplicate == false) {
			identList.remove(index);
			identVal.remove(index);
		}
	}

	public int getIdentSize() {
		return (identList.size());
	}

	public int getIdentVal(int id) {
		return (identVal.get(id));
	}

	public void setIdentVal(int id, int val) {
		identVal.set(id, val);
	}
}
