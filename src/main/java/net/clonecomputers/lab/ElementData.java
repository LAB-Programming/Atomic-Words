package net.clonecomputers.lab;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ElementData {
	
	public static final String SCHEMA_FILE = "/xml" + File.separator + "elementDataSchema.xsd";
	public static final InputSource DEFAULT_DATA_FILE = new InputSource(ElementData.class.getResourceAsStream("/xml" + File.separator + "defaultElementData.xml"));
	
	private HashMap<String, Element> elements;
	
	public ElementData(InputSource dataFile) throws SAXException, ParserConfigurationException, IOException {
		ValidatorHandler val = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(this.getClass().getResource(SCHEMA_FILE)).newValidatorHandler();
		ElementXMLHandler handler = new ElementXMLHandler();
		val.setContentHandler(handler);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		XMLReader reader = spf.newSAXParser().getXMLReader();
		reader.setContentHandler(val);
		reader.setErrorHandler(handler);
		reader.parse(dataFile);
	}
	
	public Element getElementBySymbol(String symbol) {
		if(symbol.length() > 2) return null;
		String properSymbol = symbol.substring(0, 1).toUpperCase() + symbol.substring(1).toLowerCase();
		return elements.get(properSymbol);
	}
	
	private class ElementXMLHandler extends DefaultHandler {
		
		private String tempSymbol;
		private String tempName;
		private String tempNumber;
		private String tempMass;
		
		private StringBuilder acc;
		
		@Override
		public void startDocument() throws SAXException {
			elements = new HashMap<String, Element>();
		}
		
		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
			if(!localName.equals("element") && !localName.equals("elements")) {
				acc = new StringBuilder();
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			acc.append(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(localName.equals("mass")) {
				tempMass = acc.toString();
			} else if(localName.equals("number")) {
				tempNumber = acc.toString();
			} else if(localName.equals("name")) {
				tempName = acc.toString();
			} else if(localName.equals("symbol")) {
				tempSymbol = acc.toString();
			} else if(localName.equals("element")) {
				try {
					elements.put(tempSymbol, new Element(tempSymbol, tempName, tempNumber, tempMass));
				} catch(NullPointerException e) {
					throw new SAXException(e);
				}
				tempSymbol = null;
				tempName = null;
				tempNumber = null;
				tempMass = null;
			} else if(localName.equals("elements"));
			else {
				throw new SAXException("Bad XML element name!");
			}
		}
	}
}
