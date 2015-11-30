package uk.ac.cam.ln287.fjava.tick5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import uk.ac.cam.cl.fjava.messages.RelayMessage;

public class Database {
	private static Connection connection;

	public Database(String databasePath) throws SQLException { 
		try {
			Class.forName("org.hsqldb.jdbcDriver");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		connection = DriverManager.getConnection("jdbc:hsqldb:file:"
				+ databasePath,"SA","");
		connection.setAutoCommit(false);
		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
					"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		} catch (SQLException e) {
			System.out.println("Warning: Database table \"messages\" already exists.");
		} finally {
			sqlStmt.close();
		}
		try {
			sqlStmt.execute("CREATE TABLE statistics(key VARCHAR(255),value INT)\n" + 
					"INSERT INTO statistics(key,value) VALUES ('Total messages',0)\n" +
					"INSERT INTO statistics(key,value) VALUES ('Total logins',0)");
		} catch (SQLException e) {
			System.out.println("Warning: Database table \"statistics\" already exists.");
		} finally {
			sqlStmt.close();
		}

	}
	public void close() throws SQLException { 
		connection.close(); 
	}
	public void incrementLogins() throws SQLException { 
		Statement stmt = connection.createStatement();
		try{
			stmt.execute("UPDATE statistics SET value = value+1 WHERE key='Total logins'");
		}catch (SQLException e) {
			System.out.println("Warning: Database table \"statistics\" already exists.");
		} finally {
			stmt.close();
			connection.commit();
		}
	}
	public synchronized void addMessage(RelayMessage m) throws SQLException { 
		String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
		Statement statement = connection.createStatement();
		PreparedStatement insertMessage = connection.prepareStatement(stmt);
		try {

			insertMessage.setString(1, m.getFrom()); 
			insertMessage.setString(2, m.getMessage());
			insertMessage.setLong(3, System.currentTimeMillis());
			insertMessage.executeUpdate();
			statement.execute("UPDATE statistics SET value = value+1 WHERE key='Total messages'");
		} finally { 
			insertMessage.close();
			connection.commit();
		}

	}
	public List<RelayMessage> getRecent() throws SQLException { 
		List<RelayMessage> list = new LinkedList<RelayMessage>();
		String stmt = "SELECT nick,message,timeposted FROM messages "+
				"ORDER BY timeposted DESC LIMIT 10";
		PreparedStatement recentMessages = connection.prepareStatement(stmt);
		try {
			ResultSet rs = recentMessages.executeQuery();
			try {
				while (rs.next())
					list.add(new RelayMessage(rs.getString(1),rs.getString(2),new Date(rs.getLong(3))));
			} finally {
				rs.close();
			}
		} finally {
			recentMessages.close();
			return list;
		}
	}

	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("Usage: java uk.ac.cam.ln287.fjava.tick5.Database <database name>");
			return;
		}
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			connection = DriverManager.getConnection("jdbc:hsqldb:file:"
					+args[0],"SA","");
			Statement delayStmt = connection.createStatement();
			try {delayStmt.execute("SET WRITE_DELAY FALSE");}  //Always update data on disk
			finally {delayStmt.close();}

			Statement sqlStmt = connection.createStatement();
			try {
				sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
						"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
			} catch (SQLException e) {
				System.out.println("Warning: Database table \"messages\" already exists.");
			} finally {
				sqlStmt.close();
			}

			connection.setAutoCommit(false);
			String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
			PreparedStatement insertMessage = connection.prepareStatement(stmt);
			try {
				insertMessage.setString(1, "Alastair"); //set value of first "?" to "Alastair"
				insertMessage.setString(2, "Hello, Andy");
				insertMessage.setLong(3, System.currentTimeMillis());
				insertMessage.executeUpdate();
			} finally { //Notice use of finally clause here to finish statement
				insertMessage.close();
			}
			connection.commit();
			stmt = "SELECT nick,message,timeposted FROM messages "+
					"ORDER BY timeposted DESC LIMIT 10";
			PreparedStatement recentMessages = connection.prepareStatement(stmt);
			try {
				ResultSet rs = recentMessages.executeQuery();
				try {
					while (rs.next())
						System.out.println(rs.getString(1)+": "+rs.getString(2)+
								" ["+rs.getLong(3)+"]");
				} finally {
					rs.close();
				}
			} finally {
				recentMessages.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
