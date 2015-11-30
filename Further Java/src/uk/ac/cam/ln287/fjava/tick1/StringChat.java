package uk.ac.cam.ln287.fjava.tick1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class StringChat{

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
			Thread output = new Thread() {
				@Override
				public void run() {
					byte[] buffer = new byte[1024];
					try {
						InputStream inputStream = s.getInputStream();
						while(true) {

							int nbBytes = inputStream.read(buffer);
							String result = new String(buffer, 0, nbBytes);
							System.out.println(result);
						}
					} catch (IOException e) {
						System.err.println("Cannot read text from the input stream");
					}
				}
			};
			// When the JVM stops all daemon threads are exited. This means the thread is 
			// independent of the user.
			output.setDaemon(true);
			output.start();
			while(true ) {
				BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
				OutputStream out =s.getOutputStream();
				out.write(r.readLine().getBytes());
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