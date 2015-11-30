package uk.ac.cam.ln287.fjava.tick5;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class ClientHandler {
	private Socket socket;
	private MultiQueue<Message> multiQueue;
	private String nickname;
	private MessageQueue<Message> clientMessages;
	private Random r = new Random();
	private Database database;
	public ClientHandler(Socket s, MultiQueue<Message> q, Database database1) {
		socket = s;
		multiQueue = q;
		database = database1;
		clientMessages = new SafeMessageQueue<Message>();
		
		List<RelayMessage> list = null;
		try {
			list = database.getRecent();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(list != null) {
            for(int j = list.size()-1;j >= 0; j--) {
                clientMessages.put(list.get(j));
            }
        }
		multiQueue.register(clientMessages);
		nickname = "Anonymous" +r.nextInt(100000);
		StatusMessage statusMessage = 
				new StatusMessage(nickname +" connected from " + s.getInetAddress().getHostName());
		q.put(statusMessage);
		try {
			this.database.incrementLogins();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Thread input = new Thread() {
			ObjectInputStream objectInputStream;
			@Override
			public void run() {
				try {
					objectInputStream = new ObjectInputStream(socket.getInputStream());
					Message receivedMessage = null;
					while(!socket.isClosed()) {

						receivedMessage = (Message) objectInputStream.readObject();
						if (receivedMessage instanceof ChangeNickMessage) {
							String name = ((ChangeNickMessage)receivedMessage).name;
							StatusMessage statusMessage = 
									new StatusMessage(nickname +" is now known as " + name + ".");
							nickname = ((ChangeNickMessage)receivedMessage).name;
							multiQueue.put(statusMessage);
						} else if(receivedMessage instanceof ChatMessage) {
							RelayMessage relayMessage = 
									new RelayMessage(nickname,((ChatMessage)receivedMessage));
							multiQueue.put(relayMessage);
							try {
								database.addMessage(relayMessage);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {						
					e.printStackTrace();
				}catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					socket.close();
				} catch(IOException e) {}

			}
		};
		input.setDaemon(true);
		input.start();
		Thread output = new Thread() {	 
			ObjectOutputStream out;
			@Override
			public void run() {
				try {
					out = new ObjectOutputStream(socket.getOutputStream());
					while(true) {
						out.writeObject(clientMessages.take());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					socket.close();
				} catch(IOException e) {}
			}
		};
		output.setDaemon(true);
		output.start();

	}
}