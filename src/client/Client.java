package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import game.Const;

/**
 * Client mode of the program
 * 
 * @author NoAuthor
 */
public class Client {

	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;

	/**
	 * Request user's nickname and create player to server connection
	 */
	public Client() {
		
		Scanner scan = new Scanner(System.in);

		System.out.println("Input IP to connect the server");
		System.out.println("Format: xxx.xxx.xxx.xxx");

		String ip = scan.nextLine();

		try {
			System.out.println("Client connection. try");
			// Connect to server and recieve the streams (in and out) to handle messages
			socket = new Socket(ip, Const.Port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			System.out.println("Input your nickname");
			out.println(scan.nextLine());

			// Start output of all income messages to console
			Resender resend = new Resender();
			resend.start();

			// While user do not input "exit" resend everything from console
			// to server
			String str = "";
			while (!str.equals("exit")) {
				str = scan.nextLine();
				out.println(str);
			}
			resend.setStop();
		} catch (Exception e) {
			System.out.println("Client connection. catch");
			e.printStackTrace();
		} finally {
			close();
			scan.close();
		}
	}

	/**
	 * Close input and output streams and socket
	 */
	private void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			System.err.println("Client Streams hadn't closed yet");
		}
	}

	/**
	 * Класс в отдельной нити пересылает все сообщения от сервера в консоль.
	 * Работает пока не будет вызван метод setStop().
	 * 
	 * @author NoAuthor
	 */
	private class Resender extends Thread {

		private boolean stoped;
		
		/**
		 * Прекращает пересылку сообщений
		 */
		public void setStop() {
			stoped = true;
		}

		/**
		 * Считывает все сообщения от сервера и печатает их в консоль.
		 * Останавливается вызовом метода setStop()
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				while (!stoped) {
					String str = in.readLine();
					System.out.println(str);
				}
			} catch (IOException e) {
				System.err.println("Error in message recieving");
				e.printStackTrace();
			}
		}
	}

}
