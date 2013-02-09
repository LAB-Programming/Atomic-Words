package net.clonecomputers.lab;

import java.util.HashMap;

public enum ElementData {

	HYDROGEN("Hydrogen", "H", 1, 1.00794);
	
	private final String name;
	private final String symbol;
	private final int number;
	private final double mass;
	private static final HashMap<String,ElementData> elementsBySymbol = new HashMap<String,ElementData>();
	private static int i;
	
	private ElementData(String elementName, String atomicSymbol, int atomicNumber, double atomicMass) {
		name = elementName;
		symbol = atomicSymbol;
		number = atomicNumber;
		mass = atomicMass;
		addToElementsBySymbol(symbol, this);
	}
	
	private static void addToElementsBySymbol(String atomicSymbol, ElementData element) {
		elementsBySymbol.put(atomicSymbol, element);
	}
	
	public static ElementData getElementBySymbol(String atomicSymbol) {
		return elementsBySymbol.get(atomicSymbol);
	}
}
