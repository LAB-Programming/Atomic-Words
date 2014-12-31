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

/**
 * This class is responsible for loading element data from an XML file and
 * storing and giving access to the element data
 * @author louishyde
 */
public class ElementData {
	
	public static final String SCHEMA_FILE = "/xml" + File.separator + "elementDataSchema.xsd";
	public static final InputSource DEFAULT_DATA_FILE;
	
	private HashMap<String, Element> elements;
	
	// Statically initialize DEFAULT_DATA_FILE
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
		// Create a ValidatorHandler for my schema
		// A ValidatorHandler is a ContentHandler that checks that the XML file is valid
		// It serves as a wrapper for the normal ContentHandler
		ValidatorHandler val = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
								.newSchema(this.getClass().getResource(SCHEMA_FILE))
								.newValidatorHandler();
		val.setContentHandler(new ElementXMLHandler());
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		XMLReader reader = spf.newSAXParser().getXMLReader();
		reader.setContentHandler(val);
		reader.setErrorHandler(new ElementXMLErrorHandler());
		reader.parse(dataFile);
	}
	
	/**
	 * Finds the element with atomic symbol matching the argument.
	 * Note that this is not case sensitive.
	 * @param symbol the atomic symbol of the element that is being searched for
	 * @return the element with atomic symbol of symbol
	 */
	public Element getElementBySymbol(String symbol) {
		if(symbol.length() > 2) return null;
		String properSymbol = symbol.substring(0, 1).toUpperCase() + symbol.substring(1).toLowerCase();
		return elements.get(properSymbol);
	}
	
	/**
	 * This class serves to extract the data from the XML file that it gets
	 * from the SAX parser
	 * @author louishyde
	 */
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
			// if we are in an element that contains data
			if(!localName.equals("element") && !localName.equals("elements")) {
				acc = new StringBuilder();
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// the use of the StringBuilder is to deal with the fact that the
			// SAXParser might not give all of the data in one call to this method
			acc.append(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			AtomicWords.logger.entering(this.getClass().getSimpleName(), "endElement", new Object[] {uri, localName, qName});
			// save the accumulated data to the correct variable if we are finishing a data containing element
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
				// create new element if we are done with an element element
				AtomicWords.logger.finer("XML Parser: End of element tag recording new Element(" + tempSymbol +"," + tempName + "," + tempNumber + "," + tempMass + ")");
				try {
					elements.put(tempSymbol, new Element(tempSymbol, tempName, tempNumber, tempMass));
				} catch(NullPointerException e) {
					// the Validator should throw an exception if any of the data containing elements are missing
					// but just to make sure
					SAXException saxe = new SAXException("One or more data entries for an element were not found", e);
					AtomicWords.logger.throwing(this.getClass().getSimpleName(), "endElement", saxe);
					throw saxe;
				}
				tempSymbol = null;
				tempName = null;
				tempNumber = null;
				tempMass = null;
			} else if(localName.equals("elements")); // we are done so we do not need to do anything
			// the Validator should catch this but just to make sure
			else {
				SAXException e = new SAXException("Bad XML element name!");
				AtomicWords.logger.throwing(this.getClass().getSimpleName(), "endElement", e);
				throw e;
			}
		}
	}
	
	/**
	 * This class is for handling error logging for errors when parsing XML files
	 * @author louishyde
	 */
	private class ElementXMLErrorHandler implements ErrorHandler {
		
		/**
		 * This method formats the exception in a easily readable way
		 * @param e
		 * @return a String with the most important information in the exception
		 */
		private String getParseExceptionInfo(SAXParseException e) {
	        String systemId = e.getSystemId();
	        if (systemId == null) {
	            systemId = "null";
	        }
	        String info = "URI=" + systemId + " Line=" + e.getLineNumber() + ": " + e.getMessage();
	        return info;
		}
		
		// The exceptions are logged at the level FINER because this shows that these
		// methods are called. Since the exceptions are rethrown they will be logged
		// elsewhere as well.
		
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
