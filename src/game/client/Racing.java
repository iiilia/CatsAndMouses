package game.client;

import java.io.IOException;
import java.net.Socket;

public class Racing {
	public static String host = "127.0.0.1";
	public static int port = 1993;
	public boolean running;
	public Connection connection;
	
	public Racing() {
		running = false;
	}

	public static void main(String[] args) {
		
		// debug
		
		String name = "Sergey";
		String car = "Yellow";
		
		//debug end
		
		Client client = new Client(name, car);
		Socket socket;
		try {
			socket = new Socket(host, port);
		} catch (IOException e) {
			return;
		}
		Connection connection;
		try {
			connection = new Connection(socket, client);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		new Thread(connection).start();
	}
}
