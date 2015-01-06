import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.BSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * @author creynaud
 *
 * Tool to get tweets from twitter account.
 * Add some tools to work on it.
 * 
 * Procedure to start the application
 *  - go to TwittTraining directory
 *  - start Mongo DB : ./startMongoDB.sh
 *  - ask for Tweets application help : ./startTweets.sh
 * 
 * To retreive tweets:
 *  - ./startTweets.sh retreive
 * To read all tweets:
 *  - ./startTweets.sh read
 * To search tweets:
 *  - ./startTweets.sh search
 * To start application in socket mode
 *  - ./startTweets.sh socket
 * 
 */
public class Tweets {

	private HttpGet request = null;
	List<TwitterFollowers> listFollowers = new ArrayList<TwitterFollowers>();
	List<TwitterTweets> listTweets = new ArrayList<TwitterTweets>();
	private String myObject_Global = null;

	// MongoDB object
	TwitterDBMongo connectDBMongo = new TwitterDBMongo();
	
	// Constructor
	public Tweets(String operation, String account) {
		// Get connection info from XML file
		System.out.println("Start getConnectionData\n");
		Authentication appAuth = getConnectionData("app.xml");
		
		// Open database connection
		System.out.println("Start DB connect\n");
		connectDB();

		if (operation.equalsIgnoreCase("retreive")){
			// Get connection to Twitter for sample
			System.out.println("Start connect(app)\n");
			request = connect(appAuth, Configuration.oauthstream);
			
			// Retrieve data from Twitter
			System.out.println("\nStart retreive\n");
			retrieve(request);
			
		} else if (operation.equalsIgnoreCase("account")){
			// Get connection to Twitter for account
			System.out.println("Start connectAccount(app) for " + account + "\n");
			String accountType = Configuration.oauthstreamAccount + account + "&count=200";
			request = connect(appAuth, accountType);
				
			// Retrieve data from Twitter
			System.out.println("\nStart account\n");
			account(request);
			
		} else if (operation.equalsIgnoreCase("followers")){
			// Get connection to Twitter for account
			System.out.println("Start connectAccount(app) for " + account + "\n");
			String accountType = Configuration.oauthstreamFollowers + account + "&count=200";
			request = connect(appAuth, accountType);
				
			// Retrieve data from Twitter
			System.out.println("\nStart followers\n");
			followers(request);
			
		} else if (operation.equalsIgnoreCase("read")) {
			// Read Tweets from mongodb
			System.out.println("Start read\n");
			readTweet();
			
		} else if (operation.equalsIgnoreCase("search")) {
			// Searh text in Tweets from mongodb
			System.out.println("Start search\n");
			searchTweet();
			
		} else if (operation.equalsIgnoreCase("socket")) {
			// Start socket listen
			System.out.println("Start socket\n");
			TwitterSocket chaussetteServer = new TwitterSocket();
			chaussetteServer.listenSocket();
			
		} else {
			System.out.println("Bad parameters !!\n");
			System.exit(0);
		}
	}
	
	/**
	 * The main function
	 * 
	 * @param args retreive/read/search/socket
	 * 
	 */
	public static void main(String[] args) {
		String operation;
		System.out.println("Start main ...\n");
		if (args.length==1){
			operation = args[0];
			System.out.println("Operation : " + operation + "\n");
			new Tweets(operation, null);
		} else {
			System.out.println("Usage: java Tweets retrieve/read/search/socket");
			System.out.println("	retrieve : to retreive tweets from Twitter (10 by default)");
			System.out.println("	read : to read all tweets from database");
			System.out.println("	search : to search tweets into database by text, date or period");
			System.out.println("	sockect : to start Tweets application in socket mode, awaiting client request\n");
			System.exit(0);
		}
	}

    /**
     * The connection method connects to the MongoDB database via TwitterDBMongo class
     * 
     * @throws MongoException
     * 
     */
	private void connectDB() {
		
		try {
			//mongoServer = new MongoClient( Configuration.mongo_location , Configuration.mongo_port );
			//db = mongoServer.getDB(Configuration.mongo_database);
			connectDBMongo.connectDB();
			
		} //try
		catch (MongoException e) {
			
			e.printStackTrace();
			
		} //catch		
		
	} // End connectDB()

	/**
	 * The followers method is responsible to read a block of users (followers)
	 * for one account and to provide to consumers
	 * 
	 * @param request the HttpGet request to access to the stream
	 * @throws Exception
	 *            
	 */
	private void followers(HttpGet request) {

		String in;
		int myCount = 0;
		Scanner sc = new Scanner(System.in);
		
		try {

			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// Clear global variable
				myObject_Global = "";

				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				
				// Read one block which contains all users
				// We create a list of users and for which one of them we extract information
				// No insert in database
				if ((in = reader.readLine()) != null) {
					// String -> JSON
					BasicDBObject objectFollowers = (BasicDBObject)JSON.parse(in);
					BasicDBObject myObject_final = new BasicDBObject();
					TwitterFollowers follower = new TwitterFollowers();
					// Clear before use
					listFollowers.clear();
					
					BasicDBList objectUserList = (BasicDBList)objectFollowers.get("users");
					for (int i=0; i < objectUserList.size(); i++)
					{
						// Get user block
						BasicDBObject oneObject = (BasicDBObject)objectUserList.get(i);
						follower.setScreen_name(oneObject.get("screen_name").toString());
						follower.setLocation(oneObject.get("location").toString());
						follower.setFriends_count(oneObject.get("friends_count").toString());
						follower.setCreated_at(oneObject.get("created_at").toString());
						follower.setCreated_date(convertTwitterDateToDate(oneObject.get("created_at").toString()));
						
						// Add date after conversion from Twitter Date to Date
						// For debug -- System.out.println("created_date : " + convertTwitterDateToDate(myObject.get("created_at").toString()) + "\n");
						myObject_final.put("created_date", convertTwitterDateToDate(oneObject.get("created_at").toString()));
						
						// Insert into global variable for socket client
						myObject_Global += myObject_final + "\n";
						listFollowers.add(follower);
			
						// Count
						myCount = myCount + 1;
					} // for
				} // if
				
				System.out.println("End followers nb: " + myCount + "\n");
				sc.close();

			} // End if

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End followers()
	
	
	/**
	 * The account method is responsible to read a list of tweets
	 * for one account and to provide to consumers
	 * 
	 * @param request the HttpGet request to access to the stream
	 * @throws Exception
	 *            
	 */
	private void account(HttpGet request) {

		String in;
		int myCount = 0;
		Scanner sc = new Scanner(System.in);
		Hashtable<String, Integer> hashtagsList; 
		
		try {

			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// Clear global variable
				myObject_Global = "";

				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

				// Here we receive a list of stream to get in a BasicDBList object
				// We read each element of this list to extract information
				if ((in = reader.readLine()) != null) {

					// String -> JSON
					BasicDBList objectList = (BasicDBList)JSON.parse(in);
					BasicDBObject myObject_final = new BasicDBObject();
					TwitterTweets tweet = new TwitterTweets();
					// Clear before use
					listTweets.clear();
					
					for (int i=0; i < objectList.size(); i++)
					{
						BasicDBObject oneObject = (BasicDBObject)objectList.get(i);
						// Get user block
						DBObject myObject_user = (DBObject)oneObject.get("user");
						tweet.setScreen_name(myObject_user.get("screen_name").toString());
						tweet.setText(oneObject.get("text").toString());
						tweet.setEntities(oneObject.get("entities").toString());
						tweet.setCreated_at(oneObject.get("created_at").toString());
						tweet.setCreated_date(convertTwitterDateToDate(oneObject.get("created_at").toString()));

						// Insert into global variable for socket client
						myObject_Global += myObject_final + "\n";
						listTweets.add(tweet);
						
						// Insert into mongodb
						// For debug -- System.out.println("Account: " + myObject_final + "\n");
						connectDBMongo.insertDB(myObject_final);
						
						// Count
						myCount = myCount + 1;
						
					} // End for
					
				} // End if
				
				System.out.println("End account nb: " + myCount + "\n");
				sc.close();

			} // End if

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End account()
	
	
	/**
	 * The retrieve method is responsible to read the content from the stream
	 * and to provide to consumers
	 * 
	 * @param request the HttpGet request to access to the stream
	 * @throws Exception
	 *            
	 */
	private void retrieve(HttpGet request) {

		String in;
		int myCount = 0;
		int totalTweets = 0;
		boolean goOut = false;
		Scanner sc = new Scanner(System.in);
		Hashtable<String, Integer> hashtagsList; 
		
		try {
			// Initialize hashtable
			hashtagsList = new Hashtable<String, Integer>(); 
			//System.out.println("Number of tweets to retreive?\n");
			//totalTweets = sc.nextInt();
			totalTweets = 10;
			
			if (totalTweets < 1 || totalTweets > 10000){
				System.out.println("Incorrect number, must be between 1 and 10000");
				System.exit(0);
			}

			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// Clear global variable
				myObject_Global = "";

				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

				// since this is a continuous stream, there is no end with the
				// readLine, we just check whether live boolean variable is set
				// to false
				while ((in = reader.readLine()) != null && !goOut) {
					
					if (!in.startsWith("{\"delete"))
					{
						// String -> JSON
						DBObject myObject = (DBObject)JSON.parse(in);
						
						// Get user block
						DBObject myObject_user = (DBObject)myObject.get("user");
						BasicDBObject myObject_final = new BasicDBObject();
						myObject_final.put("screen_name", myObject_user.get("screen_name"));
						myObject_final.put("text", myObject.get("text"));
						myObject_final.put("created_at", myObject.get("created_at"));
						myObject_final.put("entities", myObject.get("entities"));
						
						// Add date after conversion from Twitter Date to Date
						// For debug -- System.out.println("created_date : " + convertTwitterDateToDate(myObject.get("created_at").toString()) + "\n");
						myObject_final.put("created_date", convertTwitterDateToDate(myObject.get("created_at").toString()));

						// Get hastags
						getHastags((DBObject)myObject.get("entities"), hashtagsList);
						
						// Insert into global variable for socket client
						myObject_Global += myObject_final + "\n";
						
						// Insert into mongodb
						connectDBMongo.insertDB(myObject_final);

						// Count
						myCount = myCount + 1;
					}
					// Exit
					if(myCount == totalTweets)
						goOut = true;

				} // while
				
				// Sort Hashtable
				if ( hashtagsList.size() > 0 ){
					sortHashtagsAndDisplay(hashtagsList);
					//sortHashtagsAndDisplayByArray(hashtagsList);
				}
				
				System.out.println("End retreive with hashtaglist : " + hashtagsList.size() + "\n");
				sc.close();

			} // End if

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End retrieve()
	
	/**
	 * The getHashtags method is responsible to extract all Hashtags from a tweet
	 * 
	 * @param DBObject_entities where data are extracted
	 * @param hashtagsList to record hashtags
	 * @throws Exception
	 *
	 */
	private void getHastags(DBObject DBObject_entities, Hashtable<String, Integer> hashtagsList) {
		
		String textHT;
		
		try {

			BasicDBList listHT = (BasicDBList)DBObject_entities.get("hashtags");
			for (int i=0; i < listHT.size(); i++)
			{
				BasicDBObject myHashtag = (BasicDBObject)listHT.get(i);
				textHT = (String)myHashtag.get("text");
				
				Integer valueHT = 0;
				if (hashtagsList.containsKey(textHT))
				{
					// Hashtag already present
					valueHT = hashtagsList.get(textHT);
				} 
				valueHT++;
				// For debug -- System.out.println("Put : " + textHT + " / value : " + value);
				// Add or replace
				hashtagsList.put(textHT, valueHT);	
			}

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End getHashtags()

	/**
	 * The sortHashtagsAndDisplay method is responsible to sort Hashtable
	 * 
	 * @param hashtagsList the Hashtable<String, Integer> containing data to sort
	 * @throws Exception
	 * 
	 */
	private void sortHashtagsAndDisplay(Hashtable<String, Integer> hashtagsList) {

		try {
			// Read HashTable -- for test
			/*Set keySet =hashtagsList.keySet();
			Iterator it= keySet.iterator();
			while (it.hasNext()){
				Object key =it.next();
				System.out.println("clé : "+(String)key+
									" - valeur : "+ hashtagsList.get(key));
			}*/
			
			ValueComparator myComp =  new ValueComparator(hashtagsList);
	        TreeMap<String, Integer> sortedHashtags = new TreeMap<String, Integer>(myComp);
	        sortedHashtags.putAll(hashtagsList);
	        System.out.println(sortedHashtags);
	        
	        Set ensemble = sortedHashtags.keySet();
	        Collection valHash = sortedHashtags.values();
			Iterator it2 = ensemble.iterator();
			Iterator it3 = valHash.iterator();  // mandatory due to get(key) methode error
			Integer i = 0;
			while(it2.hasNext() && i < 10){
				Object keyTree = it2.next();
				Object keyValue = it3.next();
			    //System.out.println(keyTree + " : " + sortedHashtags.get(keyTree));
				System.out.println(keyTree + " : " + keyValue);
				i++;
			}
			
		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End sortHashtagsAndDisplay()

	/**
	 * The sortHashtagsAndDisplayByArray method is responsible to sort Hashtable via an array
	 * Due to issue on treemap (impossible to execute get(key) methode after sorts)
	 * 
	 * @param hashtagsList the Hashtable<String, Integer> containing data to sort
	 * @throws Exception
	 * 
	 */
	private void sortHashtagsAndDisplayByArray(Hashtable<String, Integer> hashtagsList) {
		// New array
		HashtagType arrayHT [] = new HashtagType[hashtagsList.size()];
		// Hashtags structure
		HashtagType structureHT =  new HashtagType();
		HashtagType structureTemp =  new HashtagType();
		// Variables
		int i = 0;
		int j = 0;

		try {
			// Read HashTable to insert it in array
			Set keySet =hashtagsList.keySet();
			Iterator it= keySet.iterator();
			i = 0;
			while (it.hasNext()){
				Object key =it.next();
				structureHT.name = (String)key;
				structureHT.value = hashtagsList.get(key);
				arrayHT[i] = structureHT;
				i++;
			}
			
		    for (i = 0; i < hashtagsList.size(); i++) { 
		    	//System.out.println("Index i : " + i);
		        for (j = 0; j < (hashtagsList.size()-i-1); j++) {  
		        	//System.out.println("Index j : " + j);
		            if (arrayHT[j].value < arrayHT[j+1].value){
		            	structureTemp = arrayHT[j+1];
		            	arrayHT[j+1] = arrayHT[j];
		            	arrayHT[j] = structureTemp;
		            }
		        }
		    }
		    
		    for (i = 0; i < 10; i++) { 
		    	System.out.println("Hashtag : " + arrayHT[i].name + " , value : " + arrayHT[i].value);
		    }

			
		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End sortHashtagsAndDisplayByArray()

	
	/**
	 * The getConnectionData method retrieves the data for the authentication
	 * 
	 * @param stFile the file containing the data for the OAuth authentication
	 * @return an instance of Authentication containing the data
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws SAXException
	 * @throws IOException
	 * 
	 */
	private Authentication getConnectionData(String stFile) {
	
		Authentication sr = null;
		
		try {
			
			// Open the file and parse it to retrieve the four required information
			File file = new File(stFile);
			InputStream inputStream;
			inputStream = new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream, "ISO-8859-1");
	
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			
			XMLReader saxReader = XMLReaderFactory.createXMLReader();
			sr = new Authentication();
			saxReader.setContentHandler(sr);
			saxReader.parse(is);
			
		} //try
		catch (FileNotFoundException e) {

			e.printStackTrace();

		} // catch
		
		catch (UnsupportedEncodingException e) {

			e.printStackTrace();

		} // catch

		catch (SAXException e) {

			e.printStackTrace();

		} // catch
		
		catch (IOException e) {

			e.printStackTrace();

		} // catch

		return sr;
		
	} // End getConnectionData()
	
	/**
	 * The connect method connects to the stream via OAuth
	 * 
	 * @param appAuth the data for connection
	 * @return HttpGet object, the request
	 * @throws OAuthMessageSignerException
	 * @throws OAuthExpectationFailedException
	 * @throws OAuthCommunicationException
	 * 
	 */
	private HttpGet connect(Authentication appAuth, String authType) {

		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				appAuth.getConsumerKey(), appAuth.getConsumerSecret());

		consumer.setTokenWithSecret(appAuth.getAccessToken(), appAuth.getAccessSecret());
		//HttpGet request = new HttpGet("https://stream.twitter.com/1.1/statuses/sample.json");
		HttpGet request = new HttpGet(authType);
		System.out.println("-ConsumerKey:" + appAuth.getConsumerKey());
		System.out.println("-ConsumerSecre:" + appAuth.getConsumerSecret());
		System.out.println("-AccessToken:" + appAuth.getAccessToken());
		System.out.println("-AccessSecret:" + appAuth.getAccessSecret());
		try {

			consumer.sign(request);

		} // try
		catch (OAuthMessageSignerException e) {

			e.printStackTrace();

		} // catch
		catch (OAuthExpectationFailedException e) {

			e.printStackTrace();

		} // catch
		catch (OAuthCommunicationException e) {

			e.printStackTrace();

		} // catch

		return request;

	} // End connect()
	
	/**
	 * The read method is responsible to read the content from MongoDB
	 * and to display the result directly in the console
	 * 
	 * @throws Exception
	 *
	 */
	private void readTweet() {
		
		int nbTweets = 0;

		try {

			DBCursor myCursor = connectDBMongo.readDB();
			
			while (myCursor.hasNext()){
				nbTweets = nbTweets + 1;
				System.out.println(myCursor.next());
			}
			
			System.out.println("Total number of tweets: " + nbTweets + "\n");

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End readTweet()

	/**
	 * The search method is responsible to ask criteria
	 * and to launch the appropriate method
	 * 
	 * @throws Exception
	 * 
	 */
	private void searchTweet() {
		
		int searchType = 0;
		Scanner sc = new Scanner(System.in);

		try {
			
			System.out.println("By text(1), by date(2) or for a period(3)?: \n");
			searchType = sc.nextInt();
			
			switch (searchType) {
	            case 1:  searchTweetByText();
	                     break;
	            case 2:  searchTweetByDate();
	                     break;
	            case 3:  searchTweetByPeriod();
                		 break;
	            default: 
	                     break;
			}
			// Close scanner
			sc.close();
        
		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End searchTweet()

	/**
	 * The search method by text is responsible to search a specific content from MongoDB
	 * and to display the result directly in the console
	 * 
	 * @throws Exception
	 *
	 */
	private void searchTweetByText() {
		
		int nbTweets = 0;
		Scanner sc = new Scanner(System.in);
		BasicDBObject query = new BasicDBObject(); 
		String textToFind;

		try {
			
			System.out.println("Text to find: \n");
			textToFind = sc.next();
			
			// Build String to find
			Pattern regex = Pattern.compile(textToFind);
			query.put("text", regex);

			DBCursor myCursor = connectDBMongo.findDB(query);
			
			while (myCursor.hasNext()){
				nbTweets = nbTweets + 1;
				System.out.println(myCursor.next());
			}
			
			System.out.println("\nTotal number of tweets: " + nbTweets + "\n");
			sc.close();

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End searchTweetByText()
	
	/**
	 * The search method by date is responsible to search a specific date from MongoDB
	 * and to display the result directly in the console
	 * 
	 * @throws Exception
	 *
	 */
	private void searchTweetByDate() {
		
		int nbTweets = 0;
		Scanner sc = new Scanner(System.in);
		BasicDBObject query = new BasicDBObject(); 
		String dateToFind = null;
		String myDate;

		try {
			
			// Get date
			System.out.println("Date to find (DD/MM/YYYY): \n");
			myDate = sc.next();

			// Build regex
			dateToFind = convertDateToRegex(myDate);
			
			// Build String to find
			Pattern regex = Pattern.compile(dateToFind);
			query.put("created_at", regex);

			DBCursor myCursor = connectDBMongo.findDB(query);
			
			while (myCursor.hasNext()){
				nbTweets = nbTweets + 1;
				System.out.println(myCursor.next());
			}
			
			System.out.println("\nTotal number of tweets: " + nbTweets + "\n");
			sc.close();

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End searchTweetByDate()
	
	/**
	 * The convert method extract day, month and year from a date given by user
	 * A regex string is build with these information
	 * 
	 * @param aDate the date to convert in regex
	 * @return String for regex
	 * @throws ParseException 
	 *
	 */
	private String convertDateToRegex(String aDate) throws ParseException {	
		int requestDay = 0;
		int requestYear = 0;
		String requestMonthString = null;
		String dateToFind = null;
		Locale locale = Locale.ENGLISH;
		
		try{
			// Format date
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			java.util.Date date = formatter.parse(aDate);
			
			// Extract day, month and year
			Calendar dateCalendar = Calendar.getInstance();
			dateCalendar.setTime(date);
			requestDay = dateCalendar.get(Calendar.DAY_OF_MONTH);
			System.out.println("Day apres: " + requestDay + "\n");
			requestMonthString = dateCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
			System.out.println("Month apres 2: " + requestMonthString + "\n");
			requestYear = dateCalendar.get(Calendar.YEAR);
			System.out.println("Year apres: " + requestYear + "\n");
			
			//Date to find, example: Jan 02.*2015
			dateToFind = requestMonthString 
						+ " " + String.format("%02d", requestDay)
						+ ".*" + String.valueOf(requestYear);
			System.out.println("Date to find: " + dateToFind + "\n");		
		}  //try
		catch (ParseException pe) {
			
			pe.printStackTrace();
			
		} //catch
		catch (Exception e) {
			
			e.printStackTrace();
			
		}

		return dateToFind;
	}
	
	/**
	 * The search method for a period is responsible to search a specific date in an interval
	 * from MongoDB and to display the result directly in the console
	 * 
	 * @throws Exception
	 * 
	 */
	private void searchTweetByPeriod() {
		
		int nbTweets = 0;
		Scanner sc = new Scanner(System.in);
		BasicDBObject query = new BasicDBObject();
		Locale locale = Locale.FRENCH;
		String minDate1;
		String maxDate1;

		try {
			
			// Get dates min and max
			System.out.println("From date (DD/MM/YYYY-HH:mm): \n");
			minDate1 = sc.next();
			System.out.println("To date (DD/MM/YYYY-HH:mm): \n");
			maxDate1 = sc.next();
			
			// Format date
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-HH:mm", locale);
			java.util.Date fromDateTmp = formatter.parse(minDate1);
			java.util.Date toDateTmp = formatter.parse(maxDate1);
			
			BasicDBObject dateRange = new BasicDBObject ("$gte", fromDateTmp);
			dateRange.put("$lt", toDateTmp);
			
			query.put("created_date", dateRange);
			System.out.println("Range:" + dateRange + "\n");

			DBCursor myCursor = connectDBMongo.findDB(query);

			while (myCursor.hasNext()){
				nbTweets = nbTweets + 1;
				System.out.println(myCursor.next());
			}
			System.out.println("Date:" + fromDateTmp + "\n");
			System.out.println("Date:" + toDateTmp + "\n");
			System.out.println("Range:" + dateRange + "\n");
			System.out.println("\nTotal number of tweets: " + nbTweets + "\n");
			sc.close();

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch

	} // End searchTweetByPeriod()

	/**
	 * The convert method extract Twitter date in order to convert it in Java format Date
	 * Twitter Date format : EEE MMM dd HH:mm:ss ZZZZZ yyyy
	 * 
	 * @param aDate - Twitter format date
	 * @return Date - java.util.Date format date
	 * @throws ParseException 
	 *
	 */
	private java.util.Date convertTwitterDateToDate(String aDate) throws ParseException {	
		Locale locale = Locale.ENGLISH;
		java.util.Date dateConvert = null;
		
		try{
			// Format date
			final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
			SimpleDateFormat dateFormatterTwitter = new SimpleDateFormat(TWITTER, locale);
			dateFormatterTwitter.setLenient(false);
			dateConvert = dateFormatterTwitter.parse(aDate);
		
		}  //try
		catch (Exception e) {
			
			e.printStackTrace();
			
		}

		return dateConvert;
	} // End convertTwitterDateToDate
	
	/**
	 * The getTweets method returns tweets retreive from last execution and stored in global variable
	 * This method is called by ClientWorker to return info to client
	 * 
	 * @return List<TwitterTweets> the list of TwitterTweets objects
	 * @throws ParseException 
	 *
	 */
	public List<TwitterTweets> getTweets() {	
		return listTweets;
	} // End getTweets

	/**
	 * The getFollowers method returns followers list recorded during last call
	 * This method is called by ClientWorker to return info to client via socket
	 * 
	 * @return List<TwitterFollowers> the list of TwitterFollowers objects
	 * @throws ParseException 
	 *
	 */
	public List<TwitterFollowers> getFollowers() {	
		return listFollowers;
	} // End getFollowers
}

