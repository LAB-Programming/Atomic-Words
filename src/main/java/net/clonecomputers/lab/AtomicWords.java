package net.clonecomputers.lab;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AtomicWords {
	
	private static final String HELP = "\nEnter a word to find out" +
									   "\n  how it can be represented as" +
									   "\n  atomic symbols if it can." +
									   "\nLines started with a non-word" +
									   "\n  character will be parsed as commands" +
									   "\nCommands are:" +
									   "\n  \"help\" to show this dialog" +
									   "\n  \"quit\", \"exit\", or \"stop\" to exit\n";
	
	private static ElementData data;
	
	static final Logger logger = Logger.getLogger("net.clonecomputers.lab.atomicwords");
	
	static {
		logger.setUseParentHandlers(false);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.SEVERE);
		logger.addHandler(consoleHandler);
		try {
			FileHandler fileHandler = new FileHandler("atomic-words%u.log");
			fileHandler.setLevel(Level.INFO);
			fileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(fileHandler);
		} catch(IOException e) {
			logger.log(Level.SEVERE, "Can not open log file, file logging disabled!", e);
		}
		logger.fine("Finished setting up logger");
	}
	
	public static void main(String[] args) {
		try {
			logger.fine("Loading XML data file");
			if(args.length < 1) {
				logger.finer("Loading from default data file");
				data = new ElementData(ElementData.DEFAULT_DATA_FILE);
			} else {
				logger.finer("Loading from user specified data file at " + args[0]);
				data = new ElementData(new InputSource(new FileInputStream(args[0])));
			}
		} catch(SAXException e) {
			logger.log(Level.SEVERE, "Error most likely from parsing XML data file", e);
			if(args.length > 0) {
				logger.info("Falling back on default data file");
				System.out.println("Using default data file instead");
				main(new String[]{});
			} else {
				logger.severe("Error parsing default XML data file, JAR may be corrupt");
			}
			System.exit(3);
		} catch(ParserConfigurationException e) {
			logger.log(Level.SEVERE, "Error creating XML parser", e);
			System.exit(2);
		} catch(IOException e) {
			logger.log(Level.SEVERE, "Error reading XML data file", e);
			if(args.length > 0) {
				logger.info("Falling back on default data file");
				System.out.println("Using default data file instead");
				main(new String[]{});
			} else {
				logger.severe("Error reading from default XML data file, JAR may be corrupt");
			}
			System.exit(1);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		boolean isCommand = false;
		logger.info("Atomic Words started");
		System.out.println("Atomic Words started");
		System.out.println(HELP);
		while(!(isCommand && (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("stop")))) {
			System.out.print(">");
			try {
				input = in.readLine();
			} catch(IOException e) {
				logger.log(Level.SEVERE, "Error reading input!", e);
				continue;
			}
			if(input.isEmpty()) continue;
			isCommand = !Character.isLetter(input.charAt(0));
			if(isCommand) input = input.substring(1);
			input = input.trim();
			if(isCommand) {
				logger.fine("User command: " + input);
				if(input.equalsIgnoreCase("help")) {
					System.out.println(HELP);
				} else {
					logger.info("Unrecognized command: " + input);
					System.out.println("Unrecognized command: " + input);
				}
			} else if(input.matches(".*[^A-Za-z].*")) {
				logger.info("Bad symbol detected in " + input);
				System.out.println("This program does not work with non-word characters");
				continue;
			} else {
				logger.info("Parsing word: " + input);
				System.out.println("Atomicizing word!");
				Set<String> output = parse(input);
				if(output != null && output.size() > 0) {
					logger.info("Found " + output.size() + " ways of spelling " + input);
					logger.fine(output.toString());
					System.out.println(output);
				} else {
					logger.info("Found no ways of spelling " + input);
					System.out.println("There is no way to spell " + input + " using atomic symbols");
				}
			}
		}
		logger.info("Program stopping");
		System.out.println("Exiting Atomic Words");
	}
	
	private static Set<String> parse(String input) {
		logger.entering("AtomicWords", "parse", input);
		Set<String> spellings = new HashSet<String>();
		if(input.length() > 1 && data.getElementBySymbol(input.substring(0, 2)) != null) {
			String atomicSymbol = data.getElementBySymbol(input.substring(0, 2)).getAtomicSymbol();
			logger.finer("Start of " + input + " matches " + atomicSymbol);
			if(input.length() > 2) {
				logger.finest("Recursing with remainder of word");
				Set<String> theRest = parse(input.substring(2));
				spellings.addAll(prependStringToStringsInCollection(atomicSymbol, theRest));
			} else {
				logger.finest("None of word left, not recursing");
				spellings.add(atomicSymbol);
			}
		}
		if(input.length() > 0 && data.getElementBySymbol(input.substring(0, 1)) != null) {
			String atomicSymbol = data.getElementBySymbol(input.substring(0, 1)).getAtomicSymbol();
			logger.finer("Start of " + input + " matches " + atomicSymbol);
			if(input.length() > 1) {
				logger.finest("Recursing with remainder of word");
				Set<String> theRest = parse(input.substring(1));
				spellings.addAll(prependStringToStringsInCollection(atomicSymbol, theRest));
			} else {
				logger.finest("None of word left, not recursing");
				spellings.add(atomicSymbol);
			}
		}
		logger.exiting("AtomicWords", "parse", spellings);
		return spellings;
	}
	
	@SuppressWarnings("unchecked")
	static <T extends Collection<String>> T prependStringToStringsInCollection(String s, final T col) throws UnsupportedOperationException {
		logger.entering("AtomicWords", "prependStringToStringsInCollection", new Object[]{s, col});
		if(col == null) return null;
		T newCol = null;
		try {
			newCol = (T) col.getClass().newInstance(); //complains about being unchecked
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		for(String sOld : col) {
			newCol.add(s + sOld);
		}
		logger.exiting("AtomicWords", "prependStringToStringsInCollection", newCol);
		return newCol;
	}
}
