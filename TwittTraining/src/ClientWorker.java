import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Utility class: this class is in charge to create a socket to listen on 4321 port
 * and to manage client request
 * @author creynaud
 *
 */
public class ClientWorker implements Runnable {

	// Socket client
	private Socket client;

	// Constructor
	ClientWorker(Socket client) {
		this.client = client;
	}

	/**
	 * The run method is executed when a thread is started
	 * This method reads request from client, call the tweets server in order to execute the request
	 * and send the response to the client
	 * The response, a string, is formetted from tweets/followers object
	 * At the end, a STOP message is sent to the client 
	 * 
	 * @throws IOException
	 *            
	 */
	public void run() {
		String line;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			System.out.println("\nIn run method ...");
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("in or out failed");
			System.exit(-1);
		}

		while (true) {
			String part1 = "";
			String part2 = "";
			String result = "";
			try {
				// Get client request and parameters
				line = in.readLine();
				System.out.println("Client request : " + line);
				if (line.indexOf("|")!=-1){
					String[] parts = line.split("\\|");
					part1 = parts[0];
					part2 = parts[1];
					System.out.println("part1:" + part1);
					System.out.println("part2:" + part2);
				} else {
					part1 = line;
				}
				
				// Launch request
				Tweets tweetClient = new Tweets(part1, part2);

				// Send data back to client
				if (part1.equals("followers")){
					List<TwitterFollowers>listResultF =  tweetClient.getFollowers();
					for (int i=0; i < listResultF.size(); i++)
					{
						// Build response from follower object
						TwitterFollowers objFollower = new TwitterFollowers();
						objFollower = listResultF.get(i);
						result += "screen_name: " + objFollower.getScreen_name() + " - "
									+ "location: " + objFollower.getLocation() + " - "
									+ "friends_count: " + objFollower.getFriends_count() + " - "
									+ "created_at: " + objFollower.getCreated_at() + " - "
									+ "created_date: " + objFollower.getCreated_date()
									+ "\n";
					}
					// Response sent to client
					out.println(result);
				} else {
					List<TwitterTweets>listResultT =  tweetClient.getTweets();
					for (int i=0; i < listResultT.size(); i++)
					{
						// Build response from tweet object
						TwitterTweets objTweet = new TwitterTweets();
						objTweet = listResultT.get(i);
						result += "screen_name: " + objTweet.getScreen_name() + " - "
									+ "text: " + objTweet.getText() + " - "
									+ "entities: " + objTweet.getEntities() + " - "
									+ "created_at: " + objTweet.getCreated_at() + " - "
									+ "created_date: " + objTweet.getCreated_date()
									+ "\n";
					}
					// Response sent to client
					out.println(result);
				}
				
				// STOP socket message sent to client
				out.println("STOP");
				this.client.close();
				break;
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Read failed");
				System.exit(-1);
			}
		}
	}
}
