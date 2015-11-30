package uk.ac.cam.ln287.fjava.tick4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
	public ClientHandler(Socket s, MultiQueue<Message> q) {
		socket = s;
		multiQueue = q;
		clientMessages = new SafeMessageQueue<Message>();
		multiQueue.register(clientMessages);
		nickname = "Anonymous" +r.nextInt(100000);
		StatusMessage statusMessage = 
				new StatusMessage(nickname +" connected from " + s.getInetAddress().getHostName());
		q.put(statusMessage);
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