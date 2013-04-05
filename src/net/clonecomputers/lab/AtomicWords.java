package net.clonecomputers.lab;

import java.util.Set;

public class AtomicWords {
	
	private static final String HELP = "\nEnter a word to find out" +
									   "\n  how it can be represented as" +
									   "\n  atomic symbols if it can." +
									   "\nLines started with a non-word" +
									   "\n  character will be parsed as commands" +
									   "\nCommands are:" +
									   "\n  \"help\" to show this dialog" +
									   "\n  \"quit\", \"exit\", or \"stop\" to exit\n";
									   
	
	public static void main(String[] args) {
		System.out.println("Atomic Words started");
		System.out.println(HELP);
		String input = "";
		boolean isCommand = false;
		while(!(isCommand && (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("stop")))) {
			input = System.console().readLine(">");
			if(input.isEmpty()) continue;
			isCommand = !Character.isLetter(input.charAt(0));
			if(isCommand) input = input.substring(1);
			input = input.trim();
			if(input.matches(".*[^A-Za-z].*")) {
				continue;
			} else if(isCommand) {
				if(input.equalsIgnoreCase("help")) {
					System.out.println(HELP);
				}
			} else {
				System.out.println("Atomicizing word!");
				//System.out.println(parse()); TODO make method parse that returns a set of different ways to spell the word in atomic symbols
			}
		}
		System.out.println("Exiting Atomic Words");
	}
}
