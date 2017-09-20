package furtek_CSCI201L_Assignment5;

import java.util.LinkedList;
import java.util.Vector;

import furtek_CSCI201L_Assignment5.diff_match_patch.Patch;

public class ServerUpdater extends Thread {
	
	private NotepadServer server;
	private String filename;
	private Vector<String> users;
	private int interval;
	
	public ServerUpdater(NotepadServer server, String filename, String user, int interval) {
		this.server = server;
		this.filename = filename;
		this.users = new Vector<String>();
		this.users.addElement(user);
		this.interval = interval;
		this.start();
	}
	
	public void run() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (!users.isEmpty()) {
			String original = server.getFile(filename, "");
			String text = original;
			diff_match_patch dmp = new diff_match_patch();
			synchronized (users) {
				for (int i=0; i<users.size(); i++) {
					String user = users.elementAt(i);
					NotepadMessage message = server.getText(user, filename);
					if (message.getSecond()==0) {
						System.out.println(":(");
						this.removeUser(user);
					} else {
						LinkedList<Patch> patch = dmp.patch_make(original, message.getThird());
						text = (String)(dmp.patch_apply(patch, text)[0]);
					}
				}
			}
			//System.out.println("text: " + text);
			synchronized (users) {
				for (int i=0; i<users.size(); i++) {
					String user = users.elementAt(i);
					boolean result = server.setText(user, filename, text);
					if (!result) {
						this.removeUser(user);
					}
				}	
			}
			server.saveFile(filename, text, "");
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				// TODO Change so that its outside of the loop
			}
		}
		server.removeUpdater(filename, this);
	}
	
	public void addUser(String user) {
		synchronized (users) {
			users.addElement(user);
		}
	}
	
	public void removeUser(String user) {
		synchronized (users) {
			users.remove(user);
		}
	}

}
