package furtek_CSCI201L_Assignment5;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

public class ServerThread extends Thread {
	
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private NotepadServer ns;
	private Socket s;
	private String username;
	public ServerThread(Socket s, ObjectInputStream ois, ObjectOutputStream oos, NotepadServer cs) {
		
			this.oos = oos;
			this.ois = ois;
			this.ns = cs;
			this.s = s;
			// if we don't have any of these, we shouldn't start the thread (we need both)
			this.start();
		
	}
	/*public void sendMessage(String message) {
		try {
			oos.writeObject(message);
			oos.flush();
		} catch (IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
		}
	}*/
	
	public void kill() {
		try {
			if (s!=null) {
				s.close();
			}
		} catch (IOException ioe) {
			
		}
	}
	
	public void run() {
		try {
			while (true) {
				NotepadMessage message = (NotepadMessage)ois.readObject();
				System.out.println("here");
				if (message.getType().equals("Sign Up")) {
					username = message.getFirst();
					ns.writeMessage(message.getType() + " attempt: Username: " + message.getFirst() + ", Password: " + String.valueOf(message.getSecond()));
					boolean answer = ns.checkUsername(message.getFirst(), message.getSecond());
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("Sign In")) {
					username = message.getFirst();
					ns.writeMessage(message.getType() + " attempt: Username: " + message.getFirst() + ", Password: " + String.valueOf(message.getSecond()));
					boolean answer = ns.checkPassword(message.getFirst(), message.getSecond());
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("Open/Save")) {
					Vector<String> answer = ns.getFileList(message.getFirst());
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("Open")) {
					String answer = ns.getFile(message.getFirst(), username);
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("getSharedDoc")) {
					Vector<String> answer = ns.getSharedDocList(username, message.getFirst());
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("addSharedUser")) {
					boolean answer = ns.addSharedUser(username, message.getFirst(), message.getThird());
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("removeSharedUser")) {
					System.out.println("removing");
					boolean answer = ns.removeSharedUser(username, message.getFirst(), message.getThird());
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("getSharesList")) {
					Vector<String> answer = ns.getSharesList(username);
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("getSharedWithMe")) {
					Vector<String> answer = ns.getSharedWithMe(username, message.getFirst()); //me, thatuser
					oos.writeObject(answer);
					oos.flush();
				} else {
					boolean answer = ns.saveFile(message.getType(), message.getFirst(), username);
					oos.writeObject(answer);
					oos.flush();
				}
			}
		} catch (IOException ioe) {
			//System.out.println("ioe here: " + ioe.getMessage());
			ns.writeMessage("User " + username + " disconnected.");
		} catch (ClassNotFoundException cnfe) {
			ns.writeMessage("User " + username + " disconnected.");
			//System.out.println("cnfe: " + cnfe.getMessage());
		} finally {
			ns.removeServerThread(this);
			ns.removeUserFromUpdaters(username);
			try {
				if (s!=null) {
					s.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
