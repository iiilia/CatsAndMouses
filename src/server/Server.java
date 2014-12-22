package server;

import game.Const;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Server mode of the game
 */
public class Server {

	/**
	 * List provides access from different Threads
	 */
	private List<Connection> connections = 
			Collections.synchronizedList(new ArrayList<Connection>());
	private ServerSocket server;

	/**
	 * Create Server.
	 * Wait for connection.
	 * Create Connection for new player and add it to list of connections
	 */
	public Server() {
		try {
			server = new ServerSocket(Const.Port);
			System.out.println("Server connection. try");

			// Wait for connections from all Players
			// Count of players could be changed in resourse.Const.java
			while (connections.size()<Const.CountOfPlayers) {
				Socket socket = server.accept();
				System.out.println("New Client connected");
				// Create Connection for new player and add it to list of connections
				Connection con = new Connection(socket);
				connections.add(con);

				// Initialize thread and call run(),
				con.start();

			}
		} catch (IOException e) {
			System.out.println("Server connection. catch");
			e.printStackTrace();
		} finally {
			closeAll();
		}
	}

	/**
	 * Close all connection threads
	 * Close ServerSocket
	 */
	private void closeAll() {
		try {
			server.close();
			
			// Close each connection from connections list
			synchronized(connections) {
				Iterator<Connection> iter = connections.iterator();
				while(iter.hasNext()) {
					((Connection) iter.next()).close();
				}
			}
		} catch (Exception e) {
			System.err.println("Server Streams hadn't closed yet");
		}
	}

	/**
	 * Data of the connection:
	 * <ul>
	 * <li>Player name</li>
	 * <li>Socket</li>
	 * <li>input stream BufferedReader</li>
	 * <li>output stream PrintWriter</li>
	 * </ul>
	 * Extends Thread 
	 * receives information from Client
	 * 
	 * @author NoAuthor
	 */
	private class Connection extends Thread {
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
	
		private String name = "";
	
		/**
		 * Initializes object's fields
		 * Receives Client's name
		 * 
		 * @param socket
		 *            socket, received from server.accept()
		 */
		public Connection(Socket socket) {
			this.socket = socket;
	
			try {
				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
	
			} catch (IOException e) {
				e.printStackTrace();
				close();
			}
		}
	
		/**
		 * Waits for Client's choice
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				name = in.readLine(); //TODO keyEvent instead of readLine()
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close();
			}
		}
	
		/**
		 * Close input and output streams and socket
		 */
		public void close() {
			try {
				in.close();
				out.close();
				socket.close();
	
				// If there is no connections
				// Close all
				connections.remove(this);
				if (connections.size() == 0) {
					Server.this.closeAll();
					System.exit(0);
				}
			} catch (Exception e) {
				System.err.println("Streams hadn't closed yet");
			}
		}
	}
}
