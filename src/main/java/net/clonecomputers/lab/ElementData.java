package net.clonecomputers.lab;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ElementData {
	
	public static final String SCHEMA_FILE = "/xml" + File.separator + "elementDataSchema.xsd";
	public static final InputSource DEFAULT_DATA_FILE;
	
	private HashMap<String, Element> elements;
	
	static {
		String dataFile = "/xml" + File.separator + "defaultElementData.xml";
		try {
			DEFAULT_DATA_FILE = new InputSource(ElementData.class.getResourceAsStream(dataFile));
		} catch(NullPointerException e) {
			AtomicWords.logger.severe("Cannot find default data file at " + dataFile + "! JAR is most likely corrupt!");
			throw e;
		}
	}
	
	public ElementData(InputSource dataFile) throws SAXException, ParserConfigurationException, IOException {
		AtomicWords.logger.entering(this.getClass().getSimpleName(), "ElementData", dataFile);
		ValidatorHandler val = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(this.getClass().getResource(SCHEMA_FILE)).newValidatorHandler();
		val.setContentHandler(new ElementXMLHandler());
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		XMLReader reader = spf.newSAXParser().getXMLReader();
		reader.setContentHandler(val);
		reader.setErrorHandler(new ElementXMLErrorHandler());
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
			AtomicWords.logger.finest("XML Parser: Starting XML document");
		}
		
		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
			AtomicWords.logger.finest("XML Parser: Starting element (" + namespaceURI + "," + localName + "," + qName + ")");
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
			AtomicWords.logger.entering(this.getClass().getSimpleName(), "endElement", new Object[] {uri, localName, qName});
			if(localName.equals("mass")) {
				AtomicWords.logger.finest("XML Parser: Mass recorded");
				tempMass = acc.toString();
			} else if(localName.equals("number")) {
				AtomicWords.logger.finest("XML Parser: Number recorded");
				tempNumber = acc.toString();
			} else if(localName.equals("name")) {
				AtomicWords.logger.finest("XML Parser: Name recorded");
				tempName = acc.toString();
			} else if(localName.equals("symbol")) {
				AtomicWords.logger.finest("XML Parser: Symbol recorded");
				tempSymbol = acc.toString();
			} else if(localName.equals("element")) {
				AtomicWords.logger.finer("XML Parser: End of element tag recording new Element(" + tempSymbol +"," + tempName + "," + tempNumber + "," + tempMass + ")");
				try {
					elements.put(tempSymbol, new Element(tempSymbol, tempName, tempNumber, tempMass));
				} catch(NullPointerException e) {
					SAXException saxe = new SAXException("One or more data entries for an element were not found", e);
					AtomicWords.logger.throwing(this.getClass().getSimpleName(), "endElement", saxe);
					throw saxe;
				}
				tempSymbol = null;
				tempName = null;
				tempNumber = null;
				tempMass = null;
			} else if(localName.equals("elements")); // we are done so we do not need to do anything
			else {
				SAXException e = new SAXException("Bad XML element name!");
				AtomicWords.logger.throwing(this.getClass().getSimpleName(), "endElement", e);
				throw e;
			}
		}
	}
	
	private class ElementXMLErrorHandler implements ErrorHandler {
		
		private String getParseExceptionInfo(SAXParseException e) {
	        String systemId = e.getSystemId();
	        if (systemId == null) {
	            systemId = "null";
	        }
	        String info = "URI=" + systemId + " Line=" + e.getLineNumber() + ": " + e.getMessage();
	        return info;
		}
		
		@Override
		public void error(SAXParseException exception) throws SAXException {
			AtomicWords.logger.log(Level.FINER, "SAX Parser Error", exception);
			throw new SAXException("Error: " + getParseExceptionInfo(exception));
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			AtomicWords.logger.log(Level.FINER, "SAX Parser FATAL Error", exception);
			throw new SAXException("Fatal: " + getParseExceptionInfo(exception));
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			AtomicWords.logger.warning("SAX Parser Warning: " + getParseExceptionInfo(exception));
			AtomicWords.logger.log(Level.FINER, "SAX Parser Warning", exception);
		}
		
	}
}
