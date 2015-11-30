package uk.ac.cam.ln287.fjava.tick5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer {

	public static void main(String args[]) {
		ServerSocket serverSocket =null;
		int port = 0;
		if (args.length != 2) {
			System.out.println("Usage: java ChatServer <port>");	
			return;
		}
		try {
			port = Integer.parseInt(args[0]);
		}catch (NumberFormatException e) {
			System.out.println("Usage: java ChatServer <port>");	
			return;
		}
		try {
			Database database = new Database(args[1]);
			serverSocket = new ServerSocket(port);
			MultiQueue<Message> multiQueue = new MultiQueue<Message>();
			while(true) {
				Socket s = serverSocket.accept();
				System.out.println(String.format("New client connected from %s", s.getInetAddress().getHostName()));
				ClientHandler clientHandler = new ClientHandler(s, multiQueue, database);

			}
		} catch (IOException e) {
			System.err.println("Cannot use port number " + port);
			return;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				if(serverSocket != null) serverSocket.close();
			} catch(IOException ignored) {}
		}
	}
}
