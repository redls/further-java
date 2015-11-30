package uk.ac.cam.ln287.fjava.tick1;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class StringReceive {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		int portNumber = 0;
		String serverName = args[0]; 
		Socket socket;
		byte[] buffer = new byte[1024];
		try {
			portNumber = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		try {
			socket = new Socket(serverName, portNumber);
		}catch (IOException e) {
			System.err.println("Cannot connect to " + serverName + " on port " + portNumber);
			return;
		}
		try {
			InputStream inputStream = socket.getInputStream();
			while(true) {
				int nbBytes = inputStream.read(buffer);
				String result = new String(buffer, 0, nbBytes);
				System.out.print(result);
			}
		} catch (UnknownHostException e) {
			System.err.println("Cannot connect to" + serverName +" on port " + portNumber);
		} catch (IOException e) {
			System.err.println("Cannot read text from the input stream");
		} catch (NumberFormatException e) {
			System.err.println("This application requires two arguments: <machine> <port>");
		}
	}

}
