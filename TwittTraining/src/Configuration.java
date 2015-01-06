/**
 * The Configuration class stores parameters for the different Entities
 * @author MPH
 *
 */
public class Configuration {

	// HttpGet
	public static String oauthstream = "https://stream.twitter.com/1.1/statuses/sample.json";
	// To complete with account before connection
	public static String oauthstreamAccount = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=";
	// To complete with account before connection
	public static String oauthstreamFollowers = "https://api.twitter.com/1.1/followers/list.json?screen_name=";
	
	// Mongo server
	public static String mongo_location = "localhost";
	
	// Mongo port
	public static int mongo_port = 27017;
	
	// Mongo database
	public static String mongo_database = "darkmatter";
	
	// Mongo collection
	public static String mongo_collection = "friday";
		
}
