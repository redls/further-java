package uk.ac.cam.ln287.fjava.tick2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class TestMessageReadWrite {
	static boolean writeMessage(String message, String filename) {
		TestMessage testMessage = new TestMessage();
		testMessage.setMessage("message");
		try {
			FileOutputStream fileOutputStream =
					new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
			out.writeObject(testMessage);
			out.close();
		}catch(IOException e) {
			return false;
		}
		return true;
	}

	static String readMessage(String location) {	
		ObjectInputStream in;
		try{
			if (location.startsWith("http://")) {
				URL url = new URL(location);
				URLConnection urlConnection = url.openConnection();
				in = new ObjectInputStream(urlConnection.getInputStream());

			} else {
				in = new ObjectInputStream(new FileInputStream(location));
			}
			TestMessage t =  (TestMessage) in.readObject();
			in.close();
			return t.getMessage();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("IOException");
		} catch (ClassNotFoundException e) {
			System.out.println("TestMessage class not found");
		}
		return null;
	}

	public static void main(String args[]) {
		writeMessage("Hello world!","testMessage.jobj");
		String message = readMessage("testMessage.jobj");
		System.out.println("Message created by me: " + message+ "\n");
		for(int i = 0; i < args.length; i++) {
			message = readMessage(args[i]);
			System.out.println("Reading message " + i + ":" + message);
		}
	}

}
