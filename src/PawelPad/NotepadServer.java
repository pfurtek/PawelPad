package furtek_CSCI201L_Assignment5;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class NotepadServer extends JFrame implements Runnable {
	private static final long serialVersionUID = -379144007976280602L;
	private Vector<ServerThread> serverThreads;
	private JTextArea log;
	private JButton button;
	private int port;
	private ServerSocket ss;
	private boolean isOn;
	private NotepadServer thisThing;
	private Thread thread;
	
	private Connection conn;
	private Statement st;
	
	private int interval;
	
	private HashMap<String, ObjectInputStream> oisMap;
	private HashMap<String, ObjectOutputStream> oosMap;
	private HashMap<String, ServerUpdater> updatersMap;
	private Vector<ServerUpdater> updatersVector;
	//private NotepadServer thisServer;
	public NotepadServer() {
		super("PawelPad Server");
		thisThing = this;
		isOn = false;
		oisMap = new HashMap<String, ObjectInputStream>();
		oosMap = new HashMap<String, ObjectOutputStream>();
		updatersMap = new HashMap<String, ServerUpdater>();
		updatersVector = new Vector<ServerUpdater>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/NotepadUsers", "root", "Fi112358"); // Change password
			if (conn==null) {
				System.out.println("conn is null");
			}
		} catch (ClassNotFoundException cnfe) {
			System.out.println("class not found");
		} catch (SQLException sqle) {
			System.out.println("sql not connected");
			System.out.println(sqle.getMessage());
		}
		FileReader fr = null;
		try {
			fr = new FileReader("server_config.txt");
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				if (line.substring(0, line.indexOf(':')).equals("port")) {
					port = Integer.valueOf(line.substring(line.lastIndexOf(':')+1));
				} else if (line.substring(0, line.indexOf(':')).equals("interval")) {
					interval = Integer.valueOf(line.substring(line.lastIndexOf(':')+1));
				}
			}
			br.close();
		} catch (FileNotFoundException fnfe) {
		} catch (IOException ioe) {
		} finally {
			if (fr!=null) {
				try {
					fr.close();
				} catch (IOException ioe) {
					
				}
			}
		}
		//port = 6789; //change this to not being hardcoded
		ss = null;
		serverThreads = new Vector<ServerThread>();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		log = new JTextArea();
		log.setEditable(false);
		button = new JButton("Start");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (button.getText().equals("Start")) {
					try {
						ss = new ServerSocket(port);
						isOn = true;
						thread = new Thread(thisThing);
						thread.start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					try {
						isOn = false;
						ss.close();
						thread.interrupt();
						for (ServerThread st : serverThreads) {
							st.kill();
						}
						button.setText("Start");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		panel.add(log);
		JPanel butpanel = new JPanel(new GridLayout(1,1));
		butpanel.add(button);
		panel.add(butpanel);
		add(panel);
		setSize(500,500);
		setVisible(true);
	}
	
	public void removeUpdater(String filename, ServerUpdater updater) {
		synchronized (updatersMap) {
			updatersMap.remove(filename);
		}
		synchronized (updatersVector) {
			updatersVector.remove(updater);
		}
	}
	
	public void removeUserFromUpdaters(String user) {
		synchronized (updatersMap) {
			for (ServerUpdater updater : updatersVector) {
				updater.removeUser(user);
			}
		}
	}
	
	public void run() {
		writeMessage("Server started on Port: " + String.valueOf(port));
		button.setText("Stop");
		try {
			while (true) {
				if (isOn) {
					//System.out.println("waiting for connections...");
					Socket s = ss.accept();
					ObjectInputStream oiss = new ObjectInputStream(s.getInputStream());
					ObjectOutputStream ooss = new ObjectOutputStream(s.getOutputStream());
					try {
						//NotepadMessage message = (NotepadMessage) oiss.readObject();
						NotepadMessage message = (NotepadMessage) oiss.readObject();
						//if (message.getType().equals("Clientport")) {
						Socket temps = new Socket(s.getInetAddress(), message.getSecond());
						writeMessage("Socket created for " + s.getInetAddress() + ", port " + message.getSecond());
						ServerThread st = new ServerThread(s, oiss, ooss, this);
						serverThreads.add(st);
						boolean result = true;
						System.out.println("here");
						ooss.writeObject(result);
						ooss.flush();
						System.out.println("here");
						//st.start();
						//}
						ObjectOutputStream oostemp = new ObjectOutputStream(temps.getOutputStream());
						oostemp.flush();
						ObjectInputStream oistemp = new ObjectInputStream(temps.getInputStream());
						oisMap.put(message.getFirst(), oistemp);
						oosMap.put(message.getFirst(), oostemp);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					//System.out.println("connection from " + s.getInetAddress());
				}
			}
		} catch (IOException ioe) {
			writeMessage("Server stopped.");
			button.setText("Start");
			isOn = false;
		} finally {
			try {
				if (ss != null) {
					ss.close();
				}
			} catch (IOException ioe) {
				System.out.println("ioe closing ss: " + ioe.getMessage());
			}
		}
	}
	
	public void writeMessage(String message) {
		//for (ServerThread st : serverThreads) {
			
			//st.sendMessage(message);
			//System.out.print("Sent message");
		//}
		log.setText(log.getText() + message + "\n");
	}
	
	public boolean checkUsername(String username, int password) {
		// if taken, return true. else, return true
		//Random rand = new Random();
		//boolean result = false;
		//result = rand.nextBoolean();
		try {
			if (conn == null) {
				System.out.println("conn is null");
				return true;
			}
			st = conn.createStatement();
			//st.executeQuery("SELECT login FROM Usernames WHERE login='" + username + "';");
			try {
				ResultSet rs = st.executeQuery("SELECT * FROM NotepadUsers.Usernames WHERE login='" + username + "';");
				int count = 0;
				while (rs.next()) {
					count++;
					rs.getString("login");
				}
				if (count == 0) {
					File newdir = new File(username);
					try{
				        newdir.mkdir();
				    } 
				    catch(SecurityException se){
				        return true;
				    } 
					st.execute("INSERT INTO Usernames(login) VALUES ('" + username + "');");
					st.execute("INSERT INTO Passwords(usernameID, password) VALUES((SELECT usernameID FROM Usernames WHERE login='" + username + "'), " + String.valueOf(password) + ");");
					
					writeMessage("Sign Up success, Username: " + username);
					return false;
				}
				writeMessage("Sign Up failed, Username: " + username);
				return true;
			} catch (SQLException sqle) {
				return true;
			}
		} catch (SQLException sqle2) {
			System.out.println(sqle2.getMessage());
			return true;
		}
	}
	
	public Vector<String> getFileList(String username) {
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM NotepadUsers.documents WHERE usernameID=(SELECT usernameID FROM NotepadUsers.Usernames WHERE login='" + username + "');");
			Vector<String> answer = new Vector<String>();
			while (rs.next()) {
				answer.addElement(rs.getString("filename"));
			}
			return answer;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean checkPassword(String username, int password) {
		try {
			//System.out.println("here");
			if (conn == null) {
				System.out.println("conn is null");
				return false;
			}
			st = conn.createStatement();
			//st.executeQuery("SELECT login FROM Usernames WHERE login='" + username + "';");
			//System.out.println("here");
			try {
				ResultSet rs = st.executeQuery("SELECT * FROM NotepadUsers.Usernames WHERE login='" + username + "';");
				//System.out.println("here");
				int count = 0;
				while (rs.next()) {
					count++;
					rs.getString("login");
				}
				if (count == 0) {
					writeMessage("Sign In failed, Username: " + username);
					return false;
				}
				rs = st.executeQuery("SELECT * FROM NotepadUsers.Usernames u, NotepadUsers.Passwords p WHERE u.usernameID=p.usernameID AND u.login='" + username + "';");
				int result = 0;
				while (rs.next()) {
					result = rs.getInt("password");
				}
				if (result == password) {
					writeMessage("Sign In success, Username: " + username);
					return true;
				} else {
					writeMessage("Sign In failed, Username: " + username);
					return false;
				}
			} catch (SQLException sqle) {
				System.out.println("sqle: " + sqle.getMessage());
				return false;
			}
		} catch (SQLException sqle2) {
			System.out.println("sqle2: " + sqle2.getMessage());
			return false;
		}
	}
	
	public String getFile(String address, String username) {
		FileReader fr = null;
		try {
			String answer = "";
			fr = new FileReader(address);
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				answer = answer.concat(line);
				if (br.ready()) {
					answer = answer.concat("\n");
				}
			}
			br.close();
			if (username.equals("")) {
			} else if (updatersMap.containsKey(address)) {
				new Thread() {
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						updatersMap.get(address).addUser(username);
					}
				}.start();
				writeMessage("File opened: Username: " + address.substring(0, address.indexOf('/')) + ", filename: " + address.substring(address.lastIndexOf('/')+1));
			} else {
				ServerUpdater su = new ServerUpdater(this, address, username, interval);
				updatersMap.put(address, su);
				updatersVector.addElement(su);
				writeMessage("File opened: Username: " + address.substring(0, address.indexOf('/')) + ", filename: " + address.substring(address.lastIndexOf('/')+1));
			}
			return answer;
		} catch (FileNotFoundException fnfe) {
			
		} catch (IOException ioe) {
			
		} finally {
			if (fr!=null) {
				try {
					fr.close();
				} catch (IOException ioe) {
					
				}
			}
		}
		return null;
	}
	
	public boolean saveFile(String address, String text, String username) {
		if (!isOn) return false;
		PrintStream out = null;
		try { //TODO: remove user from previous updater before you add him to his own one
			try {
				st = conn.createStatement();
				//String username = address.substring(0, address.indexOf('/'));
				if (username.equals("")) {
					out = new PrintStream(new FileOutputStream(address));
					out.print(text);
				} else if (updatersMap.containsKey(address)) {
					ResultSet rs = st.executeQuery("SELECT * FROM documents WHERE filename='" + address.substring(address.lastIndexOf('/')+1) + "' AND usernameID=(SELECT usernameID FROM Usernames WHERE login='" + username + "');");
					if (!rs.next())
						st.executeUpdate("INSERT INTO documents(usernameID, filename) VALUES ((SELECT usernameID FROM Usernames WHERE login='" + username + "'), '" + address.substring(address.lastIndexOf('/')+1) + "');");
					out = new PrintStream(new FileOutputStream(username + "/" + address.substring(address.lastIndexOf('/')+1)));
					out.print(text);
					new Thread() {
						public void run() {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							updatersMap.get(address).addUser(username);
						}
					}.start();
					writeMessage("Saved file: Username: " + username + ", filename: " + address.substring(address.lastIndexOf('/')+1));
				} else {
					ResultSet rs = st.executeQuery("SELECT * FROM documents WHERE filename='" + address.substring(address.lastIndexOf('/')+1) + "' AND usernameID=(SELECT usernameID FROM Usernames WHERE login='" + username + "');");
					if (!rs.next())
						st.executeUpdate("INSERT INTO documents(usernameID, filename) VALUES ((SELECT usernameID FROM Usernames WHERE login='" + username + "'), '" + address.substring(address.lastIndexOf('/')+1) + "');");
					out = new PrintStream(new FileOutputStream(username + "/" + address.substring(address.lastIndexOf('/')+1)));
					out.print(text);
					ServerUpdater su = new ServerUpdater(this, address, username, interval);
					updatersMap.put(address, su);
					updatersVector.addElement(su);
					writeMessage("Saved file: Username: " + username + ", filename: " + address.substring(address.lastIndexOf('/')+1));
				}
			} catch (SQLException e) {
				System.out.println("sqle");
				return false;
			}
			return true;
		} catch (FileNotFoundException fnfe2) {
			return false;
		} finally {
			if (out!=null) {
				out.close();
			}
		}
	}
	
	public Vector<String> getSharedDocList(String username, String docname) {
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM NotepadUsers.CanEdit WHERE filenameID=(SELECT filenameID FROM NotepadUsers.documents WHERE filename='" + docname.substring(docname.lastIndexOf('/')+1) + "');");
			System.out.println(docname.substring(docname.lastIndexOf('/')));
			Vector<String> answer = new Vector<String>();
			while (rs.next()) {
				answer.addElement(rs.getString("username"));
			}
			return answer;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Vector<String> getSharesList(String username) {
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM NotepadUsers.Shares WHERE usernameID=(SELECT usernameID FROM NotepadUsers.Usernames WHERE login='" + username + "');");
			Vector<String> answer = new Vector<String>();
			while (rs.next()) {
				answer.addElement(rs.getString("username"));
			}
			return answer;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Vector<String> getSharedWithMe(String myusername, String theirusername) {
		try {
			st = conn.createStatement();
			
			ResultSet rs3 = st.executeQuery("SELECT * FROM NotepadUsers.documents WHERE usernameID=(SELECT usernameID FROM NotepadUsers.Usernames WHERE login='" + theirusername + "');");
			Vector<Integer> ints = new Vector<Integer>();
			Vector<String> filenames = new Vector<String>();
			while (rs3.next()) {
				ints.add(rs3.getInt("filenameID"));
				filenames.add(rs3.getString("filename"));
			}
			Vector<String> answer = new Vector<String>();
			for (int i=0; i<ints.size(); i++) {
				int id = ints.get(i);
				ResultSet rs2 = st.executeQuery("SELECT * FROM NotepadUsers.CanEdit WHERE filenameID=" + id + " AND username='" + myusername + "';");
				if (rs2.next()) {
					answer.addElement(filenames.get(i));
				}
			}
			return answer;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean addSharedUser(String myusername, String docname, String theirusername) {
		try {
			st = conn.createStatement();
			//st.executeQuery("SELECT login FROM Usernames WHERE login='" + username + "';");
			try {
				ResultSet rs = st.executeQuery("SELECT * FROM NotepadUsers.Usernames WHERE login='" + theirusername + "';");
				int count = 0;
				while (rs.next()) {
					count++;
					rs.getString("login");
				}
				if (count == 0) {
					System.out.println("User does not exist " + theirusername);
					return false;
				}
				Vector<String> userlist = getSharedDocList(myusername, docname);
				if (userlist.contains(theirusername)) {
					System.out.println("user is already shared");
					return false;
				}
				st.execute("INSERT INTO NotepadUsers.CanEdit VALUES ((SELECT filenameID FROM NotepadUsers.documents WHERE filename='" + docname.substring(docname.lastIndexOf('/')+1) + "'), '" + theirusername + "');");
				//st.execute("INSERT INTO Passwords(usernameID, password) VALUES((SELECT usernameID FROM Usernames WHERE login='" + username + "'), " + String.valueOf(password) + ");");
				/*writeMessage("Sign Up success, Username: " + username);
				return false;
				writeMessage("Sign Up failed, Username: " + username);
				return true;*/
				
				rs = st.executeQuery("SELECT * FROM NotepadUsers.Shares WHERE usernameID=(SELECT usernameID FROM NotepadUsers.Usernames WHERE login='" + theirusername + "') AND username='" + myusername + "';");
				if (!rs.next()) {
					st.execute("INSERT INTO NotepadUsers.Shares VALUES ((SELECT usernameID FROM NotepadUsers.Usernames WHERE login='" + theirusername + "'), '" + myusername + "');");
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
				return false;
			}
		} catch (SQLException sqle2) {
			System.out.println(sqle2.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean removeSharedUser(String myusername, String docname, String theirusername) {
		try {
			st = conn.createStatement();
			//st.executeQuery("SELECT login FROM Usernames WHERE login='" + username + "';");
			try {
				ResultSet rs = st.executeQuery("SELECT * FROM NotepadUsers.Usernames WHERE login='" + theirusername + "';");
				int count = 0;
				while (rs.next()) {
					count++;
					rs.getString("login");
				}
				if (count == 0) {
					System.out.println("User does not exist " + theirusername);
					return false;
				}
				Vector<String> userlist = getSharedDocList(myusername, docname);
				if (!userlist.contains(theirusername)) {
					System.out.println("user isn't already shared");
					return false;
				}
				st.execute("DELETE FROM NotepadUsers.CanEdit WHERE filenameID=(SELECT filenameID FROM NotepadUsers.documents WHERE filename='" + docname.substring(docname.lastIndexOf('/')+1) + "') AND username='" + theirusername + "';");
				//rs = st.executeQuery("SELECT * FROM NotepadUsers.CanEdit WHERE filenameID=(SELECT filenameID FROM NotepadUsers.documents WHERE username='" + theirusername + "');");
				/*writeMessage("Sign Up success, Username: " + username);
				return false;
				writeMessage("Sign Up failed, Username: " + username);
				return true;*/
				System.out.println("I am here");
				
				ResultSet rs3 = st.executeQuery("SELECT * FROM NotepadUsers.documents WHERE usernameID=(SELECT usernameID FROM NotepadUsers.Usernames WHERE login='" + myusername + "') AND filename='" + docname.substring(docname.lastIndexOf('/')+1) + "';");
				if (rs3.next()) {
					int id = rs3.getInt("filenameID");
					System.out.println("id: " + id);
					System.out.println("user: " + theirusername);
						System.out.println("after delete");
						new Thread() {
							public void run() {
								NotepadServer.this.sendRemoveInfo(theirusername, docname);
							}
						}.start();
						System.out.println("after send info");
						updatersMap.get(docname).removeUser(theirusername);
				}
				
				
				ResultSet rs4 = st.executeQuery("SELECT * FROM NotepadUsers.documents WHERE usernameID=(SELECT usernameID FROM NotepadUsers.Usernames WHERE login='" + myusername + "');");
				Vector<Integer> ints = new Vector<Integer>();
				while (rs4.next()) {
					ints.add(rs4.getInt("filenameID"));
				}
				for (Integer id : ints) {
					System.out.println("id");
					ResultSet rs2 = st.executeQuery("SELECT * FROM NotepadUsers.CanEdit WHERE filenameID=" + id + " AND username='" + theirusername + "';");
					if (rs2.next()) {
						return true;
					}
				}
				st.execute("DELETE FROM NotepadUsers.Shares WHERE usernameID=(SELECT usernameID FROM NotepadUsers.Usernames WHERE login='" + theirusername + "') AND username='" + myusername + "';");
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
				return false;
			}
		} catch (SQLException sqle2) {
			System.out.println(sqle2.getMessage());
			return false;
		}
		return true;
	}
	
	public void sendRemoveInfo(String username, String filename) {
		try {
			System.out.println("removing: " + username + " from " + filename);
			if (oosMap.get(username)!=null) {
				synchronized (oosMap.get(username)) {
					oosMap.get(username).writeObject(new NotepadMessage("removedFrom", filename, 0));
				}
			}
		} catch (IOException e) {
			System.out.println("ioe :(");
			e.printStackTrace();
		}
	}
	
	public void removeServerThread(ServerThread st) {
		serverThreads.remove(st);
	}
	
	public NotepadMessage getText(String username, String filename) {
		try {
			//System.out.println("username" + username);
			if (oosMap.get(username)!=null) {
				synchronized (oosMap.get(username)) {
					oosMap.get(username).writeObject(new NotepadMessage("getText", filename, 0));
					NotepadMessage message = (NotepadMessage) oisMap.get(username).readObject();
					return message;
				}
			}
		} catch (EOFException eofe) {
			System.out.println("eofe: " + eofe.getMessage());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		System.out.println(":((");
		return new NotepadMessage("", "", 0);
	}

	public boolean setText(String username, String filename, String content) {
		try {
			if (oosMap.get(username)!=null) {
				synchronized (oosMap.get(username)) {
					oosMap.get(username).writeObject(new NotepadMessage("setText", filename, 0, content));
					boolean message = (boolean) oisMap.get(username).readObject();
					return message;
				}
			}
		} catch (EOFException eofe) {
			System.out.println("eofe: " + eofe.getMessage());	
		} catch (IOException e) {
			//Client disconnected
			e.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			//Client disconnected
		}
		return false;
	}
	
	public static void main(String [] args) {
		new NotepadServer();
	}
}
