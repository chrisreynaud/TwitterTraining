import java.util.Date;

/**
 * TwitterTweets class: this class allow to serialize BasicDBObject object for tweets
 * 
 * @author creynaud
 *
 */
public class TwitterTweets {

	// Fields to extract from tweet
	public String screen_name;
	public String text;
	public String entities;
	public String created_at;
	public Date created_date;
	
	// Constructor
	public TwitterTweets(){
		
	}
	
	// Getter and Setter
	// -----------------
	public String getScreen_name() {
		return screen_name;
	}

	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getEntities() {
		return entities;
	}

	public void setEntities(String entities) {
		this.entities = entities;
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
