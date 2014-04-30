package net.clonecomputers.lab;

public class Element {
	
	private final String symbol;
	private final String name;
	private final String number;
	private final String mass;

	public Element(String atomicSymbol, String elementName, String atomicNumber, String atomicMass) throws NullPointerException {
		if(atomicSymbol == null) throw new NullPointerException("atomicSymbol");
		if(elementName == null) throw new NullPointerException("elementName");
		if(atomicNumber == null) throw new NullPointerException("atomicNumber");
		if(atomicMass == null) throw new NullPointerException("atomicMass");
		symbol = atomicSymbol;
		name = elementName;
		number = atomicNumber;
		mass = atomicMass;
	}
	
	public String getAtomicSymbol() {
		return symbol;
	}
	
	public String getElementName() {
		return name;
	}
	
	public String getAtomicNumber() {
		return number;
	}
	
	public String getAtomicMass() {
		return mass;
	}
}
