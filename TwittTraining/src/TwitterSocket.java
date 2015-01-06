import java.io.IOException;
import java.net.ServerSocket;

/**
 * TwitterSocket class: this class is in charge to manage the socket and thread for the server part
 * 
 * @author creynaud
 *
 */
public class TwitterSocket {
	
	ServerSocket server = null;
	
	// Constructor
	public TwitterSocket(){
		
	}
	
	/**
	 * The listenSocket() method creates a server socket to listen on a specific port, and wait on accept() method
	 * until a client launch a request. 
	 * When a client request arrives, the run() method of ClientWorker class is started, and a thread is also started.
	 * At the end the process returns on accept() method awaiting for new client request.
	 * 
	 * @throws IOException
	 *            
	 */
	public void listenSocket() {
		try {
			server = new ServerSocket(4321);
		} catch (IOException e) {
			System.out.println("Could not listen on port 4321");
			System.exit(-1);
		}
		while (true) {
			ClientWorker w;
			try {
				// server.accept returns a client connection
				w = new ClientWorker(server.accept());
				Thread t = new Thread(w);
				t.start();
			} catch (IOException e) {
				System.out.println("Accept failed: 4321");
				System.exit(-1);
			}
		}
	}
}
