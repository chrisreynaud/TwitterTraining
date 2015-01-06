import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/**
 * TwitterDB interface: this interface declares methods to define for a database access
 * These methods will be directly used in Tweets class to allow a generic access to a database
 * 
 * @author creynaud
 *
 */
public interface TwitterDB {
	
	public void connectDB();
	public void insertDB(BasicDBObject objectDB);
	public DBCursor readDB();
	public DBCursor findDB(BasicDBObject query);

}
