package edu.tamu.csce434;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class Scanner {
    public int sym; // current token on the input 
    public int val; // value of last number encountered
    public int id;  // index of last identifier encountered

    private PushbackReader reader;
    private FileInputStream fileStream;

    // Side effects of Advance()
    // Current character in the fileInputStream we are looking at
    public char inputChar;
    // True when Advance() has reach EOF.
    public boolean reachedEOF = false;

    // Map identifier lexemes to their unique index
    private ArrayList<String> idMap;

    final int INVALID_CHAR = 0;

    java.util.Map< String, Integer > reservedWords = new java.util.HashMap<>() {{
        put("then",      41);
        put("do",        42);
        put("let",       77);
        put("od",        81);
        put("fi",        82);
        put("else",      90);
        put("call",     100);
        put("if",       101);
        put("while",    102);
        put("return",   103);
        put("var",      110);
        put("array",    111);
        put("function", 112);
        put("procedure",113);
        put("main",     200);
    }};
    java.util.Map< String, Integer > nonAlphaLexemes = new java.util.HashMap<>() {{
        put("*",    1);
        put("/",    2);
        put("+",    11);
        put("-",    12);
        put("==",   20);
        put("!=",   21);
        put("<",    22);
        put(">=",   23);
        put("<=",   24);
        put(">",    25);
        put(".",    30);
        put(",",    31);
        put("[",    32);
        put("]",    34);
        put(")",    35);
        put("<-",   40);
        put("(",    50);
        put(";",    70);
        put("}",    80);
        put("{",    150);
    }};



    public void closefile()
	{
        reachedEOF = true; // just in case
        try {
            fileStream.close();
        } catch (java.io.IOException e) {
            Error("Could not close file!");
        }
	}

	/** 
	 * Advance to the next token 
	 */
    public void Next() {
        do { // Skip whitespace and comments, and catch EOF.
            Advance();
            if (reachedEOF) {
                sym = 255; // EOF
                return;
            }
            if (inputChar == '/' && GetLookAhead() == '/') {
                while (!isNewline(inputChar)) {
                    Advance();
                }
            }
        } while (Character.isWhitespace(inputChar));
        String lexeme = "" + inputChar;

        if (Character.isLetter(inputChar)) {
            while (Character.isLetterOrDigit(GetLookAhead())) {
                Advance();
                lexeme += inputChar;
            }
            if (reservedWords.containsKey(lexeme)) {
                sym = reservedWords.get(lexeme);
            } else {
                sym = 61; // ident
                if (idMap.contains(lexeme)) {
                    id = idMap.indexOf(lexeme);
                } else {
                    id = idMap.size();
                    idMap.add(lexeme);
                }
            }
        } else if (Character.isDigit(inputChar)) {
            while (Character.isDigit(GetLookAhead())) {
                Advance();
                lexeme += inputChar;
            }
            val = Integer.parseInt(lexeme);
            sym = 60; // number
        } else { // must be some non-alphanumeric token
            // First, check to see if it is a length 2 lexeme
            String twoLongSymbolLexeme = lexeme + GetLookAhead();
            if (nonAlphaLexemes.containsKey(twoLongSymbolLexeme)) {
                // Eat the lookahead since we are using it
                Advance();
                sym = nonAlphaLexemes.get(twoLongSymbolLexeme);

            // Otherwise, it must be a length 1 lexeme. Return errorToken=0 if we don't find it
            } else {
                sym = nonAlphaLexemes.getOrDefault(lexeme, 0);
            }
        }
	}

    private boolean isNewline(char c) {
        return c=='\n' || c=='\r';
    }
    /**
     * Move to next char in the input
     */
	public void Advance() {
        if (reachedEOF) return;
        try {
            int r = 0;
            r = reader.read();
            if (r == -1) {
                reachedEOF = true;
                return;
            }
            inputChar = (char) r;
        } catch (IOException e) {
            Error("Reader failed to get next character in Advance()");
            return;
        }
	}

    /**
     * Peek the next char without Advance()'ing
     */
    public char GetLookAhead() {
        if (reachedEOF) return INVALID_CHAR;
        try {
            int r = reader.read();
            char lookAhead = (char) r;
            if (r == -1) {
                return INVALID_CHAR;
            }
            reader.unread(r);
            return  lookAhead;
        } catch (IOException e) {
            Error("Reader failed to get next character in GetLookAhead()");
            return INVALID_CHAR;
        }
    }

    public Scanner(String fileName) {
        try {
            File file = new File(fileName);
            fileStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Error("Could not open file '" + fileName + "'");
            return;
        }
        reader = new PushbackReader(new BufferedReader(new InputStreamReader(fileStream)));
        idMap = new ArrayList<>();

        // Load first token in
        Next();
    }

    /**
     * Converts given id to name; returns null in case of error
     */
    public String Id2String(int id) {
        return idMap.get(id);
    }

    /**
     * Signal an error message
     * 
     */
    public void Error(String errorMsg) {
        System.err.println("ERROR: \"" + errorMsg + "\"");
    }

    /**
     * Converts given name to id; returns -1 in case of error
     */
    public int String2Id(String name) {
        return idMap.indexOf(name);
    }

}

