import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * TwitterDBMongo class: this class implement TwitterDB interface for specific Mongo DB access
 * 
 * @author creynaud
 *
 */
public class TwitterDBMongo implements TwitterDB {
	//MongoDB connection
	private DB db = null;

	//MongoDB server
	private MongoClient mongoServer = null;
	
	// Constructor
	public TwitterDBMongo(){
		
	}
		
	/**
	 * The connection method connects to the MongoDB database
	 * 
	 * @throws UnknownHostException
	 * @throws MongoException
	 * 
	 */
	public void connectDB() {

		try {
			mongoServer = new MongoClient(Configuration.mongo_location, Configuration.mongo_port);
			db = mongoServer.getDB(Configuration.mongo_database);

		} // try
		catch (UnknownHostException | MongoException e) {

			e.printStackTrace();

		} // catch

	} // End connectDB()
	
	/**
	 * The connection method connects to the MongoDB database
	 * 
	 * @param objectDB BasicDBObject object to insert into database
	 * @throws Exception
	 * 
	 */
	public void insertDB(BasicDBObject objectDB){
		try{
			// Get mongo collection
			DBCollection myCollection = db.getCollection(Configuration.mongo_collection);
			
			// Insert into mongodb
			myCollection.insert(objectDB);
		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch
	}
	
	/**
	 * The readDB() method connects to the MongoDB database to read all row
	 * 
	 * @return DBCursor a cursor on all records
	 * @throws Exception
	 * 
	 */
	public DBCursor readDB(){

		DBCursor myCursor = null;
		
		try {

			// Get mongo collection
			DBCollection myCollection = db.getCollection(Configuration.mongo_collection);
			
			// Find all data Mongo
			myCursor = myCollection.find();

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch
		
		return myCursor;
	}
	
	/**
	 * The findDB(BasicDBObject) method connects to the MongoDB database to read data according the query
	 * 
	 * @param query - the query to execute on database
	 * @return DBCursor a cursor on all records according to the query
	 * @throws Exception
	 * 
	 */
	public DBCursor findDB(BasicDBObject query){
		
		DBCursor myCursor = null;

		try {

			// Get mongo collection
			DBCollection myCollection = db.getCollection(Configuration.mongo_collection);
			
			// Find data into Mongo
			myCursor = myCollection.find(query);
			

		} // try
		catch (Exception e) {

			e.printStackTrace();

		} // catch
		
		return myCursor;
	}
}
