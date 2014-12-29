package game.client;

import game.server.JSONable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;

public class Connection implements Closeable, Runnable {
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private ConcurrentLinkedQueue<String> inputQueue;
	private ConcurrentLinkedQueue<String> outputQueue;
	private boolean running;
	private Client client;

	public Connection(Socket socket, Client client)
			throws IOException {
		this.socket = socket;
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		running = false;
		inputQueue = new ConcurrentLinkedQueue<>();
		outputQueue = new ConcurrentLinkedQueue<>();
		this.client = client;
	}
	
	public void sendHandshake() {																//ѕосылаем инициализирующее сообщение серверу
		JSONObject message = new JSONObject();
		message.put("message", "connect");
		message.put("type", "player");
		message.put("name", client.name);
		message.put("cartype", client.carType.getName());
		message.put("tracktiled", true);
		send(message);
	}
	
	@Override
	public void run() {
		running = true;	
		sendHandshake();
		String line = null;
		try {
            socket.setSoTimeout(10);
        } catch (SocketException e1) {
            close();
        }
		while (running) {
		    while(!outputQueue.isEmpty()) {
		        out.println(outputQueue.poll());
		    }
			try {
				line = in.readLine();
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				close();
				break;
			}
			if (line == null) {
				close();
				break;
			}
			inputQueue.add(line);
		}
	}
		
	@Override
	public void close() {
		stop();
		out.close();
		try {
			in.close();
			socket.close();
		} catch (IOException e) {
			// like I give a fuck
		}
	}
	
	public void stop() {
		running = false;
	}
	
	@Override
	public int hashCode() {
		return socket.hashCode();
	}

	public InetAddress getAddress() {
		return socket.getInetAddress();
	}

	public boolean hasInput() {
		return !inputQueue.isEmpty();
	}

	public String nextInput() {
		return inputQueue.poll();
	}
	
	public void send(String message) {
	    if(outputQueue.size() > 5) {
	        outputQueue.poll();
	    }
	    outputQueue.add(message);
	}
	
	public void send(JSONObject message) {
		send(message.toString());
	}
	
	public void send(JSONable message) {
		send(message.toJSON());
	}

}
