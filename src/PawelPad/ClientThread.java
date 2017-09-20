package furtek_CSCI201L_Assignment5;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
	
	private Notepad notepad;
	private Socket s;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	//private String username;
	
	public ClientThread(Notepad notepad, Socket s) {
		this.notepad = notepad;
		System.out.println("here1");
		this.s = s;
		System.out.println("here2");
		//this.username = username;

		try {
			this.oos = new ObjectOutputStream(s.getOutputStream());
			this.oos.flush();
			System.out.println("here3");
			this.ois = new ObjectInputStream(s.getInputStream());
			System.out.println("here4");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("here5");
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
				if (message.getType().equals("getText")) {
					//username = message.getFirst();
					NotepadMessage answer = notepad.getText(message.getFirst());
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("setText")) {
					//username = message.getFirst();
					boolean answer = notepad.setText(message.getFirst(), message.getThird());
					oos.writeObject(answer);
					oos.flush();
				} else if (message.getType().equals("removedFrom")) {
					//username = message.getFirst();
					notepad.removeFrom(message.getFirst());
				}
			}
		} catch (IOException ioe) {
			//System.out.println("ioe here: " + ioe.getMessage());
			//ns.writeMessage("User " + username + " disconnected.");
			//TODO offline
		} catch (ClassNotFoundException cnfe) {
			//ns.writeMessage("User " + username + " disconnected.");
			//System.out.println("cnfe: " + cnfe.getMessage());
			//TODO offline
		} finally {
			//notepad.removeServerThread(this);
			//TODO offline
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
