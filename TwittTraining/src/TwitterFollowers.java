import java.util.Date;

/**
 * TwitterFollowers class: this class allow to serialize BasicDBObject object for followers
 * 
 * @author creynaud
 *
 */
public class TwitterFollowers {

	// Fields to extract from followers
	public String screen_name;
	public String location;
	public String friends_count;
	public String created_at;
	public Date created_date;
	
	// Constructor
	public TwitterFollowers(){
		
	}
	
	// Getter and Setter
	// -----------------
	public String getScreen_name() {
		return screen_name;
	}

	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getFriends_count() {
		return friends_count;
	}

	public void setFriends_count(String friends_count) {
		this.friends_count = friends_count;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public Date getCreated_date() {
		return created_date;
	}

	public void setCreated_date(Date created_date) {
		this.created_date = created_date;
	}

}
