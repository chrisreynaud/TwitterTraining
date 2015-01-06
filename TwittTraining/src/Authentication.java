import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Utility class: Authentication that reads the XML file for connecting to the stream
 * then retrieves the consumer key and secret, and the access token and secret
 * @author MPH
 *
 */
public class Authentication implements ContentHandler {

	//tags in the XML file
	private boolean bAccessToken = false;
	private boolean bAccessSecret = false;
	private boolean bConsumerKey = false;
	private boolean bConsumerSecret = false;
	
	//content of the tags in the XML file
	private String accessToken = null;
	private String accessSecret = null;
	private String consumerKey = null;
	private String consumerSecret = null;
	
	/**
	 * Constructor
	 */
	public Authentication() {

	} //constructor
	
	/**
	 * setDocumentLocator allows to retrieve on which line and column the caret is 
	 * when reading the file
	 * 
	 * @param locator
	 * 
	 */
	public void setDocumentLocator(Locator locator) {

		//Note: left blank intentionally
		
	} //setDocumentLocator()

	/**
	 * startDocument is called when starting reading the document
	 */
	public void startDocument() throws SAXException {
		
		//Note: left blank intentionally
		
	} //startDocument()

	/**
	 * endDocument is called when finishing reading the document
	 */
	public void endDocument() throws SAXException {
		
		//Note: left blank intentionally
		
	} //endDocument()

	/**
	 * startPrefixMapping is called when starting a new namespace
	 */
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		
		//Note: left blank intentionally
		
	} //startPrefixMapping()

	/**
	 * endPrefixMapping is called when finishing a namespace
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
		
		//Note: left blank intentionally
		
	} //endPrefixMapping()

	/**
	 * startElement is called when an opening tag is on the stream
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		
		//we check according the different tags in the XML file
		if (localName.equals("accessToken"))
			bAccessToken = true;
		
		if (localName.equals("accessSecret"))
			bAccessSecret = true;

		if (localName.equals("consumerKey"))
			bConsumerKey = true;

		if (localName.equals("consumerSecret"))
			bConsumerSecret = true;

	} //startElement()

	/**
	 * endElement is called on closing tag on the stream
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (localName.equals("accessToken"))
			bAccessToken = false;
		
		if (localName.equals("accessSecret"))
			bAccessSecret = false;

		if (localName.equals("consumerKey"))
			bConsumerKey = false;

		if (localName.equals("consumerSecret"))
			bConsumerSecret = false;
		
		
	} //endElement()

	/**
	 * characters is called when reading the content between a pair of tags
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		//based on the opening tag, we set the correct variable
		if (bAccessToken)
			accessToken = new String(ch, start, length);
		
		if (bAccessSecret) 
			accessSecret = new String(ch, start, length);
		
		if (bConsumerKey) 
			consumerKey = new String(ch, start, length);

		if (bConsumerSecret) 
			consumerSecret = new String(ch, start, length);

	} //characters()

	/**
	 * ignorableWhitespace is called every time a whitespace can be ignored
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		
		//Note: left blank intentionally
		
	} //ignorableWhitespace()

	public void processingInstruction(String target, String data)
			throws SAXException {
		
		//Note: left blank intentionally
		
	} //processingInstruction()

	public void skippedEntity(String name) throws SAXException {
		
		//Note: left blank intentionally
		
	} //skippedEntity()
	
	/**
	 * getAccessToken returns the access token coming from the XML file
	 * @return the access token
	 */
	public String getAccessToken() {
		
		return accessToken;
		
	} //getAccessToken()
	
	/**
	 * getAccessSecret returns the access secret coming from the XML file
	 * @return the access secret
	 */
	public String getAccessSecret() {
		
		return accessSecret;
		
	} //getAccessSecret()

	/**
	 * getConsumerKey returns the consumer key coming from the XML file
	 * @return the consumer key
	 */
	public String getConsumerKey() {
		
		return consumerKey;
		
	} //getConsumerKey()

	/**
	 * getConsumerSecret returns the consumer secret coming from the XML file
	 * @return the consumer secret
	 */
	public String getConsumerSecret() {
		
		return consumerSecret;
		
	} //getConsumerSecret()
	
} //class Authentication
