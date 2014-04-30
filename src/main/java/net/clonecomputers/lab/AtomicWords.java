package net.clonecomputers.lab;

import java.io.FileInputStream;
import java.io.IOException;
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
	}
	
	public static void main(String[] args) {
		try {
			if(args.length < 1) {
				data = new ElementData(ElementData.DEFAULT_DATA_FILE);
			} else {
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
				System.out.println("This program does not work with non-word characters");
				continue;
			} else if(isCommand) {
				if(input.equalsIgnoreCase("help")) {
					System.out.println(HELP);
				}
			} else {
				System.out.println("Atomicizing word!");
				Set<String> output = parse(input);
				if(output != null && output.size() > 0) {
					System.out.println(output);
				} else {
					System.out.println("There is no way to spell " + input + " using atomic symbols");
				}
			}
		}
		System.out.println("Exiting Atomic Words");
	}
	
	private static Set<String> parse(String input) {
		Set<String> spellings = new HashSet<String>();
		if(input.length() > 1 && data.getElementBySymbol(input.substring(0, 2)) != null) {
			String atomicSymbol = data.getElementBySymbol(input.substring(0, 2)).getAtomicSymbol();
			if(input.length() > 2) {
				Set<String> theRest = parse(input.substring(2));
				spellings.addAll(prependStringToStringsInCollection(atomicSymbol, theRest));
			} else {
				spellings.add(atomicSymbol);
			}
		}
		if(input.length() > 0 && data.getElementBySymbol(input.substring(0, 1)) != null) {
			String atomicSymbol = data.getElementBySymbol(input.substring(0, 1)).getAtomicSymbol();
			if(input.length() > 1) {
				Set<String> theRest = parse(input.substring(1));
				spellings.addAll(prependStringToStringsInCollection(atomicSymbol, theRest));
			} else {
				spellings.add(atomicSymbol);
			}
		}
		return spellings;
	}
	
	@SuppressWarnings("unchecked")
	static <T extends Collection<String>> T prependStringToStringsInCollection(String s, final T col) throws UnsupportedOperationException {
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
		return newCol;
	}
}
