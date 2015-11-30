package uk.ac.cam.ln287.fjava.tick2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

@FurtherJavaPreamble(
		author = "Laura Nechita",
		date = "19th October 2015",
		crsid = "ln287",
		summary = "Chat Client",
		ticker = FurtherJavaPreamble.Ticker.A)
public class ChatClient {

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

	public static void main(String[] args) {
		String server = null;
		int port = 0;
		if (args.length < 2) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		try {
			port = Integer.parseInt(args[1]);
			server = args[0];
			// The socket s is declared final not only to make it accessible from the thread out
			// but also to to prevent changing the value in another thread/part of the program. 
			final Socket s;
			try{
				s =  new Socket(server, port); 
			} catch (IOException e) {
				System.err.println("Cannot connect to " + server + " on port " + port);
				return;
			}
			System.out.println(simpleDateFormat.format(new Date())+ " [Client] Connected to " + server +" on port "+port+ ".");
			Thread output = new Thread() {
				DynamicObjectInputStream dynamicObjectInputStream;
				@Override
				public void run() {
					try {
						dynamicObjectInputStream = new DynamicObjectInputStream(s.getInputStream());
						Message receivedMessage = null;
						while(!s.isClosed()) {	
							receivedMessage = (Message) dynamicObjectInputStream.readObject();
							if (receivedMessage instanceof StatusMessage) {
								String from = " [Server] ";
								System.out.println(
										simpleDateFormat.format(receivedMessage.getCreationTime()) 
										+ from
										+ ((StatusMessage) receivedMessage).getMessage());
							}else if(receivedMessage instanceof RelayMessage) {
								String from = " [" + ((RelayMessage) receivedMessage).getFrom()+ "] ";
								System.out.println(simpleDateFormat.format(receivedMessage.getCreationTime()) 
										+ from 
										+ ((RelayMessage) receivedMessage).getMessage());
							}else if(receivedMessage instanceof NewMessageType){
								dynamicObjectInputStream.addClass(((NewMessageType) receivedMessage).getName(),((NewMessageType) receivedMessage).getClassData());
								System.out.println(simpleDateFormat.format(receivedMessage.getCreationTime()) +" [Client] New class " + ((NewMessageType) receivedMessage).getName() + " loaded.");
							} else {
								Class<?> c = receivedMessage.getClass();
								Field[] fields = c.getDeclaredFields();
								Method[] methods = c.getDeclaredMethods();
								System.out.printf("%s [Client] %s:",simpleDateFormat.format(receivedMessage.getCreationTime()),c.getSimpleName());
								for(Field f: fields) {

									try{
										f.setAccessible(true);
										System.out.printf(" %s(%s)",f.getName(),f.get(receivedMessage));
									} catch (IllegalArgumentException e) {
										System.err.println(e);
									} catch (IllegalAccessException e) {
										System.err.println(e);
									}
								}
								System.out.println();
								for (Method m : methods) 
									if (m.getAnnotation(Execute.class) != null && m.getParameterTypes().length == 0 ) {
										m.setAccessible(true);
										try {
											m.invoke(receivedMessage);
										} catch (IllegalAccessException e) {
											System.err.println(e);
										} catch (IllegalArgumentException e) {
											System.err.println(e);
										} catch (InvocationTargetException e) {
											System.err.println(e);
										}
									}
							}
						}
						dynamicObjectInputStream.close();
					} catch (IOException e) {
						System.err.println("Cannot read text from the input stream");
					} catch (ClassNotFoundException e) {
						System.err.println("Class not found");
					}
				}

			};
			// When the JVM stops all daemon threads are exited. This means the thread is 
			// independent of the user.
			output.setDaemon(true);
			output.start();
			Message message = new Message();
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			while(true ) {
				String textMessage = r.readLine();
				if (textMessage.startsWith("\\nick")) {
					message = new ChangeNickMessage(textMessage.substring(6));
				} else if (textMessage.startsWith("\\quit")) {
					s.close();
					break;
				} else if (textMessage.startsWith("\\")){
					message= null;
					System.out.println("[Client] Unknown Command \"" + textMessage.substring(1)+"\"" );
				} else {
					message = new ChatMessage(textMessage);
				}
				if (message != null) {
					out.writeObject(message);
				}
			}
		} catch (UnknownHostException e) {
			System.err.println("Cannot connect to" + server +" on port " + port);
		} catch (IOException e) {	
			System.err.println("Cannot read text from the input stream");
		}catch (NumberFormatException e) {
			System.err.println("This application requires two arguments: <machine> <port>");
		}
	}
}
