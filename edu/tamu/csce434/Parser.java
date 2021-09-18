package edu.tamu.csce434;


public class Parser 
{
	private Scanner scanner;
	private int inputNumber;	// stores result when calling inputnum()
	private int token;
	
	// Use this function to print errors, i is symbol/token value
	private void printError(int i) 
	{

	}
	
	
	// Constructor of your Parser
	public Parser(String args[])
	{
		if (args.length != 2)
		{
			System.out.println("Usage: java Parser testFileToScan dataFileToRead");
			System.exit(-1);
		}

		scanner = new Scanner(args[0]);
		
		// Continue the setup 
		
	}
	
	
	// Use this function to accept a Token and and to get the next Token from the Scanner
	private boolean accept(String s) 
	{
		
	}

	// Use this function whenever your program needs to expect a specific token
	private void expect(String s) 
	{
		if (accept(s)) 
			return;
		
		printError();
		
	}
	
	// Implement this function to start parsing your input file
	public void computation() 
	{
		
	}

	public static void main(String[] args) 
	{
		Parser p = new Parser(args);
		p.computation();
	}
}