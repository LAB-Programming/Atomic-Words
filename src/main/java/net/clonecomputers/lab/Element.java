package net.clonecomputers.lab;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * A class for storing the data of a single element including its symbol,
 * name, number, and mass. Soon it will also be able to return an image of the
 * element as it would look on the periodic table
 * @author louishyde
 *
 */
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
	
	
	// Below here everything is work in progress towards being able to create
	// an image for the element that would look like its box on the periodic
	// table of elements
	
	
	public Image getImage(int height) {
		int width = Math.round(height*0.9f);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = null;
		try {
			g = (Graphics2D) image.getGraphics();
			drawImage(g, width, height);
		} finally {
			if (g != null) g.dispose();
		}
		return image;
	}
	
	private void drawImage(Graphics2D g, int width, int height) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, width, height);
		float smallTextHeight = height/10f;
		float largeTextHeight = 3*smallTextHeight;
		int smallFontSize = getFontSize(smallTextHeight, "SansSerif");
		int largeFontSize = getFontSize(largeTextHeight, "SansSerif");
		Font small = new Font("SansSerif", Font.PLAIN, smallFontSize);
		Font smallBold = new Font("SansSerif", Font.BOLD, smallFontSize);
		Font largeBold = new Font("SansSerif", Font.BOLD, largeFontSize);
		g.setFont(small);
		// draw element name and atomic mass
		g.setFont(smallBold);
		// draw atomic number
		g.setFont(largeBold);
		// draw atomic symbol
	}
	
	private int getFontSize(float height, String fontType) {
		// TODO finish!
		return 0;
	}
}
