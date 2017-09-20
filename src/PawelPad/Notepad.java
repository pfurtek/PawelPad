package furtek_CSCI201L_Assignment5;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.undo.UndoManager;

import furtek_CSCI201_Assignment1.AutoCorrect;
import furtek_CSCI201_Assignment1.MyComparator;
import furtek_CSCI201_Assignment1.Parsers;
import furtek_CSCI201_Assignment1.WordTrie;

public class Notepad extends JFrame {
	private static final long serialVersionUID = -786520193639755794L;
	private JTabbedPane editor;
	private JMenuBar menuBar;
	private Cursor mycursor;
	private Cursor textcursor;
	private Cursor clickcursor;
	static private Color goldColor0;
	static private Color goldColor1;
	static private Color goldColor2;
	static private Color goldColor3;
	static private Color goldColor4;
	static private Color goldColor5;
	static private Color goldColor6;
	static private Color goldColor7;
	private Font plainfont12;
	private Font plainfont14;
	private Font boldfont30;
	private Font boldfont14;
	private Font textfont;
	private JFrame thisFrame;
	
	private JPanel logoPanel;
	private JPanel lowerPanel;
	private JPanel loginPanel;
	private JPanel signInPanel;
	private JPanel signUpPanel;
	private JLabel label;
	private JLabel logo;
	
	private boolean offline;
	private String hostname;
	private int port;
	private Socket s;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private String userName;
	
	private ServerSocket ss;
	private int sport;
	private ObjectOutputStream ooss;
	private ObjectInputStream oiss;
	
	private JMenu usersMenu;
	private HashMap<Component, Integer> isMine;
	//private HashMap<String, JTextArea> areamap;
	private HashMap<Component, JTextArea> ourMap;
	private boolean menuIsThere = false;
	
	public Notepad() {
		super("PawelPad");
		setupServerSocket();
		readConfigFile();
		s = null;
		offline = false;
		isMine = new HashMap<Component, Integer>();
		//areamap = new HashMap<String, JTextArea>();
		createGUI();
		createMenu();
		createTabs();
	}
	
	public NotepadMessage getText(String filename) {
		//System.out.println(filename);
		for (int i=0; i<editor.getComponentCount(); i++) {
			//System.out.println(editor.getTitleAt(i) + " / " + filename);
			if (editor.getTitleAt(i).equals(filename)) {
				return new NotepadMessage("getText", filename, 1, ourMap.get(editor.getComponentAt(i)).getText());
			}
		}
		return new NotepadMessage("getText", filename, 0, "");
	}
	
	public boolean setText(String filename, String doccontent) {
		for (int i=0; i<editor.getComponentCount(); i++) {
			if (editor.getTitleAt(i).equals(filename)) {
				int pos = ourMap.get(editor.getComponentAt(i)).getCaretPosition();
				ourMap.get(editor.getComponentAt(i)).setText(doccontent);
				if (pos > doccontent.length()) {
					ourMap.get(editor.getComponentAt(i)).setCaretPosition(doccontent.length());
				} else {
					ourMap.get(editor.getComponentAt(i)).setCaretPosition(pos);
				}
				return true;
			}
		}
		return false;
	}
	
	public void removeFrom(String filename) {
		System.out.println("in remove from");
		for (int i=0; i<editor.getComponentCount(); i++) {
			System.out.println(editor.getTitleAt(i) + " / " + filename);
			if (editor.getTitleAt(i).equals(filename)) {
				editor.setSelectedIndex(i);
				new Thread() {
					public void run() {
						System.out.println("show");
						JOptionPane.showMessageDialog(editor, "You got removed from file " + filename + "!", "Removed!", JOptionPane.CANCEL_OPTION);
						System.out.println("showed");
					}
				}.start();
				editor.setTitleAt(i, editor.getTitleAt(i).substring(editor.getTitleAt(i).lastIndexOf('/')+1));
				return;
			}
		}
	}
	
	private void setupServerSocket() {
		int x = 6780;
		ss=null;
		while (ss==null) {
			try {
				ServerSocket tempss = new ServerSocket(x);
				ss = tempss;
				sport = x;
			} catch (IOException ioe) {
				// this will get thrown if I can't bind to portNumber
				x++;
			}
		}
	}
	
	private void readConfigFile() {
		FileReader fr = null;
		try {
			fr = new FileReader("client_config.txt");
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				if (line.substring(0, line.indexOf(':')).equals("port")) {
					port = Integer.valueOf(line.substring(line.lastIndexOf(':')+1));
				} else if (line.substring(0, line.indexOf(':')).equals("host")) {
					hostname = line.substring(line.lastIndexOf(':')+1);
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
	}
	
	private void createGUI() {
		setSize(800, 400);
		setLocation(200, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image cursorImage = tk.getImage("resources/standardCursor.png");
		mycursor = tk.createCustomCursor(cursorImage, new Point(0,0), "Custom Cursor");
		Image cursorImag = tk.getImage("resources/textCursor.png");
		textcursor = tk.createCustomCursor(cursorImag, new Point(0,0), "Text Cursor");
		Image cursorIma = tk.getImage("resources/clickCursor.png");
		clickcursor = tk.createCustomCursor(cursorIma, new Point(0,0), "click Cursor");
		setCursor(mycursor);
		
		goldColor0 = new Color(255, 255, 240);
		goldColor1 = new Color(255, 250, 150);
		goldColor2 = new Color(255, 235, 0);
		goldColor3 = new Color(255, 200, 130);
		goldColor4 = Color.ORANGE;
		goldColor5 = new Color(235, 100, 0);
		goldColor6 = new Color(140, 65, 0);
		goldColor7 = new Color(255, 220, 150);
		
		Font mainfont = null;
		try {
			mainfont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/kenvector_future.ttf"));
		} catch (IOException ioe) {
			mainfont = new Font("Futura", Font.PLAIN, 12);
		} catch (FontFormatException ffe) {
			mainfont = new Font("Futura", Font.PLAIN, 12);
		}
		plainfont12 = mainfont.deriveFont(Font.PLAIN, 12);
		plainfont14 = mainfont.deriveFont(Font.PLAIN, 14);
		boldfont30 = mainfont.deriveFont(Font.BOLD, 30);
		boldfont14 = mainfont.deriveFont(Font.BOLD, 14);
		
		try {
			textfont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/kenvector_future_thin.ttf"));
			textfont = textfont.deriveFont(Font.PLAIN, 12);
		} catch (IOException ioe) {
			textfont = new Font("Futura", Font.PLAIN, 12);
		} catch (FontFormatException ffe) {
			textfont = new Font("Futura", Font.PLAIN, 12);
		}
		textfont = new Font("Futura", Font.PLAIN, 12);
		this.getContentPane().setBackground(goldColor1);
		Image iconImage = tk.getImage("resources/appIcon.png");
		
		if (System.getProperty("os.name").equals("Mac OS X")) {
			Class<?> applicationClass;
			try {
				applicationClass = Class.forName("com.apple.eawt.Application");
				try {
					Method getApplicationMethod = applicationClass.getMethod("getApplication");
					Method setDockIconMethod = applicationClass.getMethod("setDockIconImage", java.awt.Image.class);
					Object macOSXApplication;
					try {
						macOSXApplication = getApplicationMethod.invoke(null);
						setDockIconMethod.invoke(macOSXApplication, iconImage);
					} catch (IllegalAccessException e) {
					} catch (IllegalArgumentException e) {
					} catch (InvocationTargetException e) {}
				} catch (NoSuchMethodException | SecurityException e) {}
			} catch (ClassNotFoundException e) {}
		} else {
			this.setIconImage(iconImage);
		}
		
		logoPanel = new JPanel();
		logo = new JLabel("PawelPad");
		logo.setFont(boldfont30);
		logo.setForeground(goldColor4);
		label = new JLabel("");
		label.setFont(boldfont14);
		label.setForeground(goldColor4);
		JPanel labelPanel = new JPanel();
		labelPanel.add(label);
		logoPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		logoPanel.add(logo, gbc);
		lowerPanel = new JPanel(new CardLayout());
		gbc.gridy = 1;
		logoPanel.add(lowerPanel, gbc);
		
		loginPanel = new JPanel();
		JButton signUpButton = new JButton("Sign Up") {
			private static final long serialVersionUID = 1365411118095749132L;

			public void paintComponent(Graphics g) {
				Dimension size = this.getSize();
				if (this.getModel().isRollover()) {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
					} catch (IOException ioe) {}
				} else {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
					} catch (IOException e) {}
				}
				g.setFont(plainfont14);
                g.setColor(Color.WHITE);
                g.drawString("Sign Up", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Sign Up")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
			}
		};
		JButton signInButton = new JButton("Sign In") {
			private static final long serialVersionUID = 1365411118095749132L;

			public void paintComponent(Graphics g) {
				Dimension size = this.getSize();
				if (this.getModel().isRollover()) {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
					} catch (IOException ioe) {}
				} else {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
					} catch (IOException e) {}
				}
				g.setFont(plainfont14);
                g.setColor(Color.WHITE);
                g.drawString("Sign In", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Sign In")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
			}
		};
		JButton offlineButton = new JButton("Offline") {
			private static final long serialVersionUID = 1365411118095749132L;

			public void paintComponent(Graphics g) {
				//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
				Dimension size = this.getSize();
				if (this.getModel().isRollover()) {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
					} catch (IOException ioe) {}
				} else {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
					} catch (IOException e) {}
				}
				g.setFont(plainfont14);
                g.setColor(Color.WHITE);
                g.drawString("Offline", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Offline")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
			}
		};
		signUpButton.setCursor(clickcursor);
		signUpButton.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, goldColor7));
		signUpButton.setMargin(new Insets(0,0,0,0));
		signUpButton.setBackground(goldColor7);
		signUpButton.setPreferredSize(new Dimension(100, 20));
		signUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout layout = (CardLayout) lowerPanel.getLayout();
				layout.show(lowerPanel, "signup");
				thisFrame.repaint();
			}
		});
		signInButton.setCursor(clickcursor);
		signInButton.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, goldColor7));
		signInButton.setMargin(new Insets(0,0,0,0));
		signInButton.setBackground(goldColor7);
		signInButton.setPreferredSize(new Dimension(100, 20));
		signInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout layout = (CardLayout) lowerPanel.getLayout();
				layout.show(lowerPanel, "signin");
				thisFrame.repaint();
			}
		});
		offlineButton.setCursor(clickcursor);
		offlineButton.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, goldColor7));
		offlineButton.setMargin(new Insets(0,0,0,0));
		offlineButton.setBackground(goldColor7);
		offlineButton.setPreferredSize(new Dimension(100, 20));
		offlineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout layout = (CardLayout) lowerPanel.getLayout();
				layout.show(lowerPanel, "label");
				label.setText("Offline");
				offline = true;
				setJMenuBar(menuBar);
				thisFrame.repaint();
			}
		});
		loginPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		loginPanel.add(signUpButton, gbc);
		gbc.gridx = 1;
		loginPanel.add(signInButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		loginPanel.add(offlineButton, gbc);
		lowerPanel.add(loginPanel, "login");
		add(logoPanel);
		
		lowerPanel.add(labelPanel, "label");
		
		signUpPanel = new JPanel(new GridBagLayout());
		JPanel signUpUpper = new JPanel();
		signUpUpper.setLayout(new GridLayout(3,2));
		GridLayout lay = (GridLayout) signUpUpper.getLayout();
		lay.setVgap(3);
		JLabel username = new JLabel("Username:");
		username.setFont(boldfont14);
		username.setForeground(goldColor4);
		signUpUpper.add(username);
		JTextField userfield = new JTextField();
		userfield.setBackground(goldColor0);
		userfield.setSelectionColor(goldColor3);
		userfield.setSelectedTextColor(goldColor6);
		userfield.setForeground(goldColor5);
		userfield.setCursor(textcursor);
		userfield.setFont(textfont);
		userfield.setBorder(BorderFactory.createEmptyBorder());
		signUpUpper.add(userfield);
		JLabel password = new JLabel("Password:");
		password.setFont(boldfont14);
		password.setForeground(goldColor4);
		signUpUpper.add(password);
		JPasswordField passfield = new JPasswordField();
		passfield.setBackground(goldColor0);
		passfield.setSelectionColor(goldColor3);
		passfield.setSelectedTextColor(goldColor6);
		passfield.setForeground(goldColor5);
		passfield.setCursor(textcursor);
		passfield.setFont(textfont);
		passfield.setBorder(BorderFactory.createEmptyBorder());
		signUpUpper.add(passfield);
		JLabel repeat = new JLabel("Repeat:");
		repeat.setFont(boldfont14);
		repeat.setForeground(goldColor4);
		signUpUpper.add(repeat);
		JPasswordField repfield = new JPasswordField();
		repfield.setBackground(goldColor0);
		repfield.setSelectionColor(goldColor3);
		repfield.setSelectedTextColor(goldColor6);
		repfield.setForeground(goldColor5);
		repfield.setCursor(textcursor);
		repfield.setFont(textfont);
		repfield.setBorder(BorderFactory.createEmptyBorder());
		signUpUpper.add(repfield);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		signUpPanel.add(signUpUpper, gbc);
		JButton signUplower = new JButton("Sign Up") {
			private static final long serialVersionUID = 1365411118095749132L;

			public void paintComponent(Graphics g) {
				//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
				Dimension size = this.getSize();
				if (this.getModel().isRollover()) {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
					} catch (IOException ioe) {}
				} else {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
					} catch (IOException e) {}
				}
				g.setFont(plainfont14);
                g.setColor(Color.WHITE);
                g.drawString("Sign Up", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Sign Up")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
			}
		};
		signUplower.setCursor(clickcursor);
		signUplower.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, goldColor7));
		signUplower.setMargin(new Insets(0,0,0,0));
		signUplower.setBackground(goldColor7);
		signUplower.setPreferredSize(new Dimension(100, 20));
		signUplower.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				char [] pass = passfield.getPassword();
				char [] rep = repfield.getPassword();
				if (pass.length==0 || rep.length==0 || userfield.getText().length()==0) {
					JOptionPane.showMessageDialog(thisFrame, "At least one field is empty!", "Empty Field", JOptionPane.ERROR_MESSAGE);
					return;
				}
				boolean same = true;
				boolean num = false;
				boolean upper = false;
				if (pass.length != rep.length) {
					same = false;
				}
				for (int i=0; i<pass.length; i++) {
					if (i>=rep.length || pass[i]!=rep[i]) {
						same = false;
					}
					if (pass[i]>='0' && pass[i]<='9') {
						num = true;
					} else if (pass[i]>='A' && pass[i]<='Z') {
						upper = true;
					}
				}
				if (!same) {
					JOptionPane.showMessageDialog(thisFrame, "Both passwords have to be the same!", "Invalid Repeat", JOptionPane.ERROR_MESSAGE);
				} else if (!num || !upper) {
					JOptionPane.showMessageDialog(thisFrame, "Password has to have at least 1 digit and 1 upper case letter!", "Invalid Password", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						s = new Socket(hostname, port);
						System.out.println("here");
						oos = new ObjectOutputStream(s.getOutputStream());
						NotepadMessage message = new NotepadMessage("Clientport", userfield.getText(), sport);
						oos.writeObject(message);
						oos.flush();
						System.out.println("here");
						ois = new ObjectInputStream(s.getInputStream());
						try {
							boolean result = (boolean) ois.readObject();
							if (result) {
								Socket temps = ss.accept();
								System.out.println("here");
								//TODO: create ClientThread
								new ClientThread(Notepad.this, temps);
							} else {
								//Notepad.this.turnOffline();
							}
						} catch (ClassNotFoundException e1) {
							System.out.println("cnfe: " + e1.getMessage());
						}
						message = new NotepadMessage("Sign Up", userfield.getText(), String.valueOf(passfield.getPassword()).hashCode());
						//String message = "Username: " + userfield.getText() + ", Password: " + String.valueOf(passfield.getPassword());
						oos.writeObject(message);
						oos.flush();
						System.out.println("here");
						try {
							boolean taken = (boolean) ois.readObject();
							if (!taken) {
								label.setText("Hello, " + userfield.getText() + "!");
								userName = userfield.getText();
								CardLayout layout = (CardLayout) lowerPanel.getLayout();
								layout.show(lowerPanel, "label");
								setJMenuBar(menuBar);
								thisFrame.repaint();
							} else {
								JOptionPane.showMessageDialog(thisFrame, "Username is already taken!", "Invalid Username", JOptionPane.WARNING_MESSAGE);
							}
						} catch (ClassNotFoundException e1) {
							System.out.println("cnfe: " + e1.getMessage());
						}
					} catch (UnknownHostException uhe) {
						//Offline
						label.setText("Offline");
						CardLayout layout = (CardLayout) lowerPanel.getLayout();
						layout.show(lowerPanel, "label");
						setJMenuBar(menuBar);
						thisFrame.repaint();
						offline = true;
						JOptionPane.showMessageDialog(thisFrame, "Unable to connect to the server.\n Program in offline mode.", "Connection Error", JOptionPane.WARNING_MESSAGE);
					} catch (IOException ioe) {
						label.setText("Offline");
						CardLayout layout = (CardLayout) lowerPanel.getLayout();
						layout.show(lowerPanel, "label");
						setJMenuBar(menuBar);
						thisFrame.repaint();
						offline = true;
						JOptionPane.showMessageDialog(thisFrame, "Unable to connect to the server.\n Program in offline mode.", "Connection Error", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		gbc.gridy = 1;
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		signUpPanel.add(signUplower, gbc);
		JButton signUpBacklower = new JButton("Back") {
			private static final long serialVersionUID = 1365411118095749132L;

			public void paintComponent(Graphics g) {
				//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
				Dimension size = this.getSize();
				if (this.getModel().isRollover()) {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
					} catch (IOException ioe) {}
				} else {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
					} catch (IOException e) {}
				}
				g.setFont(plainfont14);
                g.setColor(Color.WHITE);
                g.drawString("Back", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Back")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
			}
		};
		signUpBacklower.setCursor(clickcursor);
		signUpBacklower.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, goldColor7));
		signUpBacklower.setMargin(new Insets(0,0,0,0));
		signUpBacklower.setBackground(goldColor7);
		signUpBacklower.setPreferredSize(new Dimension(100, 20));
		signUpBacklower.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout layout = (CardLayout) lowerPanel.getLayout();
				layout.show(lowerPanel, "login");
			}
		});
		gbc.gridy = 1;
		gbc.gridx = 0;
		signUpPanel.add(signUpBacklower, gbc);
		lowerPanel.add(signUpPanel, "signup");
		
		signInPanel = new JPanel(new GridBagLayout());
		JPanel signInUpper = new JPanel();
		signInUpper.setLayout(new GridLayout(2,2));
		GridLayout lay2 = (GridLayout) signInUpper.getLayout();
		lay2.setVgap(3);
		JLabel username2 = new JLabel("Username:");
		username2.setFont(boldfont14);
		username2.setForeground(goldColor4);
		signInUpper.add(username2);
		JTextField userfield2 = new JTextField();
		userfield2.setBackground(goldColor0);
		userfield2.setSelectionColor(goldColor3);
		userfield2.setSelectedTextColor(goldColor6);
		userfield2.setForeground(goldColor5);
		userfield2.setCursor(textcursor);
		userfield2.setFont(textfont);
		userfield2.setBorder(BorderFactory.createEmptyBorder());
		signInUpper.add(userfield2);
		JLabel password2 = new JLabel("Password:");
		password2.setFont(boldfont14);
		password2.setForeground(goldColor4);
		signInUpper.add(password2);
		JPasswordField passfield2 = new JPasswordField();
		passfield2.setBackground(goldColor0);
		passfield2.setSelectionColor(goldColor3);
		passfield2.setSelectedTextColor(goldColor6);
		passfield2.setForeground(goldColor5);
		passfield2.setCursor(textcursor);
		passfield2.setFont(textfont);
		passfield2.setBorder(BorderFactory.createEmptyBorder());
		signInUpper.add(passfield2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		signInPanel.add(signInUpper, gbc);
		JButton signInlower = new JButton("Sign In") {
			private static final long serialVersionUID = 1365411118095749132L;

			public void paintComponent(Graphics g) {
				Dimension size = this.getSize();
				if (this.getModel().isRollover()) {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
					} catch (IOException ioe) {}
				} else {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
					} catch (IOException e) {}
				}
				g.setFont(plainfont14);
                g.setColor(Color.WHITE);
                g.drawString("Sign In", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Sign In")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
			}
		};
		signInlower.setCursor(clickcursor);
		signInlower.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, goldColor7));
		signInlower.setMargin(new Insets(0,0,0,0));
		signInlower.setBackground(goldColor7);
		signInlower.setPreferredSize(new Dimension(100, 20));
		signInlower.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				char [] pass = passfield2.getPassword();
				if (pass.length==0 || userfield2.getText().length()==0) {
					JOptionPane.showMessageDialog(thisFrame, "At least one field is empty!", "Empty Field", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						s = new Socket(hostname, port);
						System.out.println("here");
						oos = new ObjectOutputStream(s.getOutputStream());
						NotepadMessage message = new NotepadMessage("Clientport", userfield2.getText(), sport);
						oos.writeObject(message);
						oos.flush();
						System.out.println("here");
						ois = new ObjectInputStream(s.getInputStream());
						try {
							boolean result = (boolean) ois.readObject();
							if (result) {
								Socket temps = ss.accept();
								System.out.println("here");
								//TODO: create ClientThread
								new ClientThread(Notepad.this, temps);
								System.out.println("here");
							} else {
								Notepad.this.turnOffline();
							}
						} catch (ClassNotFoundException e1) {
							System.out.println("cnfe: " + e1.getMessage());
						}
						message = new NotepadMessage("Sign In", userfield2.getText(), String.valueOf(passfield2.getPassword()).hashCode());
						System.out.println("hereve");
						oos.writeObject(message);
						oos.flush();
						System.out.println("here");
						try {
							boolean correct = (boolean) ois.readObject();
							if (correct) {
								label.setText("Hello, " + userfield2.getText() + "!");
								userName = userfield2.getText();
								CardLayout layout = (CardLayout) lowerPanel.getLayout();
								layout.show(lowerPanel, "label");
								setJMenuBar(menuBar);
								thisFrame.repaint();
							} else {
								JOptionPane.showMessageDialog(thisFrame, "Username or password invalid!", "Invalid Information!", JOptionPane.WARNING_MESSAGE);
							}
						} catch (ClassNotFoundException e1) {
							System.out.println("cnfe: " + e1.getMessage());
						}
					} catch (UnknownHostException e1) {
						label.setText("Offline");
						CardLayout layout = (CardLayout) lowerPanel.getLayout();
						layout.show(lowerPanel, "label");
						setJMenuBar(menuBar);
						thisFrame.repaint();
						offline = true;
						JOptionPane.showMessageDialog(thisFrame, "Unable to connect to the server.\n Program in offline mode.", "Connection Error", JOptionPane.WARNING_MESSAGE);
					} catch (IOException e1) {
						label.setText("Offline");
						CardLayout layout = (CardLayout) lowerPanel.getLayout();
						layout.show(lowerPanel, "label");
						setJMenuBar(menuBar);
						thisFrame.repaint();
						offline = true;
						JOptionPane.showMessageDialog(thisFrame, "Unable to connect to the server.\n Program in offline mode.", "Connection Error", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		gbc.gridy = 1;
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		signInPanel.add(signInlower, gbc);
		JButton signInBacklower = new JButton("Back") {
			private static final long serialVersionUID = 1365411118095749132L;

			public void paintComponent(Graphics g) {
				//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
				Dimension size = this.getSize();
				if (this.getModel().isRollover()) {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
					} catch (IOException ioe) {}
				} else {
					try {
						g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
					} catch (IOException e) {}
				}
				g.setFont(plainfont14);
                g.setColor(Color.WHITE);
                g.drawString("Back", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Back")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
			}
		};
		signInBacklower.setCursor(clickcursor);
		signInBacklower.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, goldColor7));
		signInBacklower.setMargin(new Insets(0,0,0,0));
		signInBacklower.setBackground(goldColor7);
		signInBacklower.setPreferredSize(new Dimension(100, 20));
		signInBacklower.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout layout = (CardLayout) lowerPanel.getLayout();
				layout.show(lowerPanel, "login");
			}
		});
		gbc.gridy = 1;
		gbc.gridx = 0;
		signInPanel.add(signInBacklower, gbc);
		lowerPanel.add(signInPanel, "signin");
		
		thisFrame = this;
	}
	
	public void turnOffline() {
		label.setText("Offline");
		offline = true;
		JOptionPane.showMessageDialog(thisFrame, "You are now offline!", "Server disconnected!", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void createTabs() {
		editor = new JTabbedPane();
		editor.setUI(new BasicTabbedPaneUI() {
			   @Override
			   protected void installDefaults() {
			       super.installDefaults();
			       highlight = goldColor1;
			       lightHighlight = goldColor1;
			       shadow = goldColor1;
			       darkShadow = goldColor1;
			       focus = Color.yellow;
			   }
			});
		editor.setBorder(null);
		editor.setFont(plainfont12);
		editor.setBackground(goldColor2);
		editor.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("Selected: " + editor.getSelectedIndex());
				System.out.println(menuIsThere);
				if (editor.getTabCount()==0 && menuIsThere) {
					menuBar.remove(usersMenu);
					menuIsThere = false;
					thisFrame.repaint();
				} else if (menuBar.getMenuCount() == 1 || editor.getTabCount()==0) {
					//do nothing, adds menu in open
				} else if (isMine.get(editor.getSelectedComponent())==1 && !menuIsThere) {
					menuBar.add(usersMenu);
					menuIsThere = true;
					thisFrame.repaint();
				} else if (isMine.get(editor.getSelectedComponent())==0 && menuIsThere) {
					menuBar.remove(usersMenu);
					menuIsThere = false;
					thisFrame.repaint();
				}
			}
		});
	}
	
	private void createMenu() {
		usersMenu = new JMenu("Users");
		usersMenu.setCursor(clickcursor);
		usersMenu.setForeground(Color.WHITE);
		usersMenu.setFont(boldfont14);
		usersMenu.add("Add").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// show popup
				//TODO: update so that when a file is being opened I update the map with either 1 or 0
				JFrame fram = new JFrame("Add User");
				JLabel toplabel = new JLabel("Add User");
				toplabel.setFont(boldfont14);
				toplabel.setBorder(BorderFactory.createMatteBorder(10, 10, 0, 10, goldColor1));
				fram.add(toplabel, BorderLayout.NORTH);
				JTextField textfield = new JTextField();
				textfield.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
				textfield.setFont(plainfont12);
				textfield.setBackground(goldColor0);
				textfield.setSelectionColor(goldColor3);
				textfield.setSelectedTextColor(goldColor6);
				textfield.setForeground(goldColor5);
				textfield.setCursor(textcursor);
				fram.add(textfield, BorderLayout.CENTER);
				JPanel butons = new JPanel();
				butons.setLayout(new BoxLayout(butons, BoxLayout.LINE_AXIS));
				butons.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				JButton chose = new JButton("Add") {
					private static final long serialVersionUID = 1365411118095749132L;

					public void paintComponent(Graphics g) {
						//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
						Dimension size = this.getSize();
						if (this.getModel().isRollover()) {
							try {
								g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
							} catch (IOException ioe) {}
						} else {
							try {
								g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
							} catch (IOException e) {}
						}
						g.setFont(plainfont14);
		                g.setColor(Color.WHITE);
		                g.drawString("Add", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Add")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
					}
				};
				chose.setCursor(clickcursor);
				chose.setBackground(goldColor1);
				chose.setBorder(BorderFactory.createEmptyBorder());
				chose.setMargin(new Insets(0,0,0,0));
				chose.setPreferredSize(new Dimension(100, 20));
				chose.setMinimumSize(new Dimension(100,20));
				JButton cancl = new JButton("Cancel") {
					private static final long serialVersionUID = 1365411118095749132L;

					public void paintComponent(Graphics g) {
						//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
						Dimension size = this.getSize();
						if (this.getModel().isRollover()) {
							try {
								g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
							} catch (IOException ioe) {}
						} else {
							try {
								g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
							} catch (IOException e) {}
						}
						g.setFont(plainfont14);
		                g.setColor(Color.WHITE);
		                g.drawString("Cancel", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Cancel")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
					}
				};
				cancl.setCursor(clickcursor);
				cancl.setBackground(goldColor1);
				cancl.setBorder(BorderFactory.createEmptyBorder());
				cancl.setPreferredSize(new Dimension(100, 20));
				JPanel but1 = new JPanel();
				but1.add(chose);
				JPanel but2 = new JPanel();
				but2.add(cancl);
				butons.add(but1);
				butons.add(but2);
				butons.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
				fram.add(butons, BorderLayout.SOUTH);
				fram.setCursor(mycursor);
				fram.setSize(300, 115);
				fram.setLocationRelativeTo(thisFrame);
				fram.setVisible(true);
				WindowFocusListener listen = new WindowFocusListener() {
					@Override
					public void windowGainedFocus(WindowEvent e) {
						fram.toFront();
					}
					@Override
					public void windowLostFocus(WindowEvent e) {}	
				};
				thisFrame.addWindowFocusListener(listen);
				cancl.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						thisFrame.removeWindowFocusListener(listen);
						fram.setVisible(false);
					}
				});
				chose.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String user = textfield.getText();
						boolean result = false;
						if (user.equals("")) {
							JOptionPane.showMessageDialog(thisFrame, "Text Field is empty!", "No success!", JOptionPane.ERROR_MESSAGE);
							return;
						}
						try {
							//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
							NotepadMessage message = new NotepadMessage("addSharedUser", editor.getTitleAt(editor.getSelectedIndex()), 0, user); // get name of the doc
							oos.writeObject(message);
							oos.flush();
							//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
							result = (boolean) ois.readObject();
							if (!result) {
								JOptionPane.showMessageDialog(thisFrame, "Adding user was unsuccessful!", "No success!", JOptionPane.ERROR_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(thisFrame, "Adding user was successful!", "Success!", JOptionPane.INFORMATION_MESSAGE);
							}
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(thisFrame, "Getting shared list unsuccessful! You are now offline!", "No success!", JOptionPane.ERROR_MESSAGE);
							offline = true;
							label.setText("offline");
							return;
						} catch (ClassNotFoundException cnfe) {
							JOptionPane.showMessageDialog(thisFrame, "Getting shared list unsuccessful! You are now offline!", "No success!", JOptionPane.ERROR_MESSAGE);
							offline = true;
							label.setText("offline");
							return;
						}
					}
				});
			}
		});
		usersMenu.add("Remove").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Vector<String> listdata = new Vector<String>();
				try {
					//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					NotepadMessage message = new NotepadMessage("getSharedDoc", editor.getTitleAt(editor.getSelectedIndex()), 0); // get name of the doc
					oos.writeObject(message);
					oos.flush();
					//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
					listdata = (Vector<String>) ois.readObject();
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(thisFrame, "Getting shared list unsuccessful! You are now offline!", "No success!", JOptionPane.ERROR_MESSAGE);
					offline = true;
					label.setText("offline");
					return;
				} catch (ClassNotFoundException cnfe) {
					JOptionPane.showMessageDialog(thisFrame, "Getting shared list unsuccessful! You are now offline!", "No success!", JOptionPane.ERROR_MESSAGE);
					offline = true;
					label.setText("offline");
					return;
				}
				DefaultListModel<String> model = new DefaultListModel<>();
				for (String str : listdata) {
					model.addElement(str);
				}
				
				// Ask for list of people who can view current file
				JFrame fram = new JFrame("Remove User");
				JLabel toplabel = new JLabel("Remove User");
				toplabel.setFont(boldfont14);
				toplabel.setBorder(BorderFactory.createMatteBorder(10, 10, 0, 10, goldColor1));
				fram.add(toplabel, BorderLayout.NORTH);
				JList<String> list = new JList<String>(model);
				JScrollPane scrollp = new JScrollPane(list);
				scrollp.setBorder(BorderFactory.createLineBorder(goldColor1, 10));
				list.setFixedCellWidth(118);
				list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
				list.setVisibleRowCount(-1);
				list.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
				list.setFont(plainfont12);
				list.setBackground(goldColor0);
				list.setSelectionBackground(goldColor3);
				list.setSelectionForeground(goldColor6);
				list.setForeground(goldColor5);
				fram.add(list, BorderLayout.CENTER);
				JPanel butons = new JPanel();
				butons.setLayout(new BoxLayout(butons, BoxLayout.LINE_AXIS));
				butons.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				JButton cancl = new JButton("Cancel") {
					private static final long serialVersionUID = 1365411118095749132L;

					public void paintComponent(Graphics g) {
						//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
						Dimension size = this.getSize();
						if (this.getModel().isRollover()) {
							try {
								g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
							} catch (IOException ioe) {}
						} else {
							try {
								g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
							} catch (IOException e) {}
						}
						g.setFont(plainfont14);
		                g.setColor(Color.WHITE);
		                g.drawString("Cancel", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Cancel")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
					}
				};
				cancl.setCursor(clickcursor);
				cancl.setBackground(goldColor1);
				cancl.setBorder(BorderFactory.createEmptyBorder());
				cancl.setPreferredSize(new Dimension(100, 20));
				JPanel but = new JPanel();
				but.add(cancl);
				butons.add(but);
				butons.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
				fram.add(butons, BorderLayout.SOUTH);
				fram.setCursor(mycursor);
				fram.setSize(300, 300);
				fram.setLocationRelativeTo(thisFrame);
				fram.setVisible(true);
				WindowFocusListener listen = new WindowFocusListener() {
					@Override
					public void windowGainedFocus(WindowEvent e) {
						fram.toFront();
					}
					@Override
					public void windowLostFocus(WindowEvent e) {}	
				};
				thisFrame.addWindowFocusListener(listen);
				cancl.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						thisFrame.removeWindowFocusListener(listen);
						fram.setVisible(false);
					}
				});
				list.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						String toremove = list.getSelectedValue();
						int selected = list.getSelectedIndex();
						if (selected == -1) return;
						try {
							//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
							NotepadMessage message = new NotepadMessage("removeSharedUser", editor.getTitleAt(editor.getSelectedIndex()), 0, toremove); // get name of the doc
							oos.writeObject(message);
							oos.flush();
							//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
							boolean result = (boolean) ois.readObject();
							if (!result) {
								JOptionPane.showMessageDialog(thisFrame, "Removing user was unsuccessful!", "No success!", JOptionPane.ERROR_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(thisFrame, "Removing user was successful!", "Success!", JOptionPane.INFORMATION_MESSAGE);
								((DefaultListModel) list.getModel()).remove(selected);
								fram.repaint();
							}
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(thisFrame, "Getting shared list unsuccessful! You are now offline!", "No success!", JOptionPane.ERROR_MESSAGE);
							offline = true;
							label.setText("offline");
							return;
						} catch (ClassNotFoundException cnfe) {
							JOptionPane.showMessageDialog(thisFrame, "Getting shared list unsuccessful! You are now offline!", "No success!", JOptionPane.ERROR_MESSAGE);
							offline = true;
							label.setText("offline");
							return;
						}
					}
				});
			}
		});
		
		menuBar = new JMenuBar() {
			private static final long serialVersionUID = 5112149155137521824L;
			@Override
            public void paintComponent(Graphics g) {
                Dimension size = this.getSize();
                g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/menubar.png"), 0, 0, size.width, size.height, this);
            }
        };
        //menuBar.add(usersMenu); //TODO delete this line
		JMenu fileMenu = new JMenu("File");
		fileMenu.setCursor(clickcursor);
		fileMenu.setForeground(Color.WHITE);
		fileMenu.setFont(boldfont14);
		menuBar.setBorder(null);
		FileMenuItemClicked fmic = new FileMenuItemClicked();
		fileMenu.add("New").addActionListener(fmic);
		fileMenu.add("Open").addActionListener(fmic);
		fileMenu.add("Save").addActionListener(fmic);
		fileMenu.add("Close").addActionListener(fmic);
		fileMenu.setMnemonic('F');
		fileMenu.getItem(0).setMnemonic('N');
		((JPopupMenu)fileMenu.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
		fileMenu.getItem(0).setIcon(new ImageIcon("resources/newIcon.png"));
		fileMenu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenu.getItem(0).setFont(plainfont12);
		fileMenu.getItem(0).setCursor(clickcursor);
		fileMenu.getItem(1).setMnemonic('O');
		fileMenu.getItem(1).setCursor(clickcursor);
		fileMenu.getItem(1).setIcon(new ImageIcon("resources/openIcon.png"));
		fileMenu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenu.getItem(1).setFont(plainfont12);
		fileMenu.getItem(2).setMnemonic('S');
		fileMenu.getItem(2).setCursor(clickcursor);
		fileMenu.getItem(2).setIcon(new ImageIcon("resources/saveIcon.png"));
		fileMenu.getItem(2).setEnabled(false);
		fileMenu.getItem(2).setFont(plainfont12);
		fileMenu.getItem(2).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenu.getItem(3).setMnemonic('C');
		fileMenu.getItem(3).setCursor(clickcursor);
		fileMenu.getItem(3).setIcon(new ImageIcon("resources/closeIcon.png"));
		fileMenu.getItem(3).setFont(plainfont12);
		fileMenu.getItem(3).setEnabled(false);
		menuBar.add(fileMenu);
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			UIManager.put("TabbedPane.selected", new javax.swing.plaf.ColorUIResource(Color.ORANGE));
			UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
			UIManager.put("OptionPane.background",new javax.swing.plaf.ColorUIResource(new Color(255, 250, 150)));
			UIManager.put("Button.foreground",new Color(140, 65, 0));
			UIManager.put("Button.margin", new Insets(50,10,10,10));
			UIManager.put("OptionPane.messageForeground", new Color(140, 65, 0));
			UIManager.put("Button.background",new Color(255, 255, 240));
			UIManager.put("Panel.background",new Color(255, 250, 150));
			UIManager.put("Button.border", BorderFactory.createBevelBorder(BevelBorder.RAISED));
			UIManager.put("ComboBox.selectionBackground", new Color(255,200,130));
			UIManager.put("ComboBox.selectionForeground", new Color(140,65,0));
			UIManager.put("ComboBox.background", new Color(255,255,240));
			UIManager.put("ComboBox.foreground", new Color(235,100,0));
			UIManager.put("Menu.selectionBackground", new Color(255,200,130));
			UIManager.put("Menu.borderPainted", false);
			UIManager.put("Menu.selectionForeground",  new Color(140,65,0));
			UIManager.put("MenuItem.background", new Color(255,255,250));
			UIManager.put("MenuItem.foreground", new Color(235,100,0));
			UIManager.put("MenuItem.selectionForeground", new Color(140,65,0));
			UIManager.put("MenuItem.selectionBackground", new Color(255,200,130));
			UIManager.put("Separator.foreground", new Color(255,200,130));
			UIManager.put("MenuItem.acceleratorForeground", new Color(240,170,100));
			UIManager.put("MenuItem.acceleratorSelectionForeground", Color.WHITE);
			UIManager.put("MenuItem.borderPainted", false);
			try {
				UIManager.put("MenuItem.acceleratorFont", Font.createFont(Font.TRUETYPE_FONT, new File("resources/kenvector_future.ttf")).deriveFont(12f));
			} catch (IOException ioe) {
				UIManager.put("MenuItem.acceleratorFont", new Font("Futura", Font.PLAIN, 12));
			} catch (FontFormatException ffe) {
				UIManager.put("MenuItem.acceleratorFont", new Font("Futura", Font.PLAIN, 12));
			}
		} catch (Exception e) {
			System.out.println("Warning! Cross-platform L&F not used!");
		}
		Notepad np = new Notepad();
		np.setVisible(true);
	}
	
	private class FileMenuItemClicked implements ActionListener {
		private HashMap<Component, UndoManager> undoMap;
		private EditMenuItemClicked emic;
		private String currDir;
		private HashMap<Component, File> fileMap;
		private HashSet<String> fileset;
		private SpellCheckItemClicked scic;
		public FileMenuItemClicked() {
			ourMap = new HashMap<Component, JTextArea>();
			fileMap = new HashMap<Component, File>();
			undoMap = new HashMap<Component, UndoManager>();
			fileset = new HashSet<String>();
			emic = new EditMenuItemClicked();
			scic = new SpellCheckItemClicked(editor, ourMap);
			currDir = "";
		}
		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			String itemName = e.getActionCommand();
			if (itemName.equals("New")) {
				thisFrame.remove(logoPanel);
				//thisFrame.setLayout(new BorderLayout());
				thisFrame.add(editor);
				JPanel tab = new JPanel();
				isMine.put(tab, 0); //TODO
				JTextArea textarea = new JTextArea();
				textarea.setLineWrap(true);
				textarea.setBackground(goldColor0);
				textarea.setSelectionColor(goldColor3);
				textarea.setSelectedTextColor(goldColor6);
				textarea.setForeground(goldColor5);
				textarea.setCursor(textcursor);
				textarea.setFont(textfont);
				textarea.setMargin(new Insets(5, 5, 5, 5));
				UndoManager undom = new UndoManager();
				textarea.getDocument().addUndoableEditListener(undom);
				JScrollPane scrollPane = new JScrollPane(textarea);
				scrollPane.setBorder(null);
				JScrollBar scrollbar = new JScrollBar();
				scrollbar.setBackground(goldColor4);
				scrollbar.setUI(new BasicScrollBarUI() {
					protected void configureScrollBarColors() {
						super.configureScrollBarColors();
						thumbColor = goldColor1;
						trackColor = goldColor4;
						thumbLightShadowColor = goldColor1;
				        thumbDarkShadowColor = goldColor1;
				        thumbHighlightColor = goldColor1;
				        trackHighlightColor = goldColor2;
					}
					protected void installComponents() {
						super.installComponents();
						scrollbar.remove(incrButton);
						incrButton = new JButton("lol") {
							private static final long serialVersionUID = 4017454050721649349L;

							public void paintComponent(Graphics g) {
								Dimension size = this.getSize();
								g.setColor(goldColor1);
								g.fillRect(0, 0, size.width, size.height);
								try {
									g.drawImage(ImageIO.read(new File("resources/arrowDownBut.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							}
						};
						incrButton.setCursor(clickcursor);
						incrButton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, goldColor1));
						scrollbar.add(incrButton);
						scrollbar.remove(decrButton);
						decrButton = new JButton("lol") {
							private static final long serialVersionUID = 7042704001161111312L;

							public void paintComponent(Graphics g) {
								Dimension size = this.getSize();
								g.setColor(goldColor1);
								g.fillRect(0, 0, size.width, size.height);
								try {
									g.drawImage(ImageIO.read(new File("resources/arrowUpBut.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							}
						};
						decrButton.setCursor(clickcursor);
						decrButton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, goldColor1));
						scrollbar.add(decrButton);
					}
				});
				scrollPane.setVerticalScrollBar(scrollbar);
				tab.add(scrollPane, BorderLayout.CENTER);
				tab.setLayout(new BoxLayout(tab, BoxLayout.X_AXIS));
				tab.setBorder(BorderFactory.createEmptyBorder());
				editor.add("new", tab);
				ourMap.put(tab, textarea);
				undoMap.put(tab, undom);
				editor.setSelectedComponent(tab);
				if (editor.getTabCount()==1) {
					menuBar.getMenu(0).getItem(2).setEnabled(true);
					menuBar.getMenu(0).getItem(3).setEnabled(true);
					JMenu editMenu = new JMenu("Edit");
					editMenu.setForeground(Color.WHITE);
					editMenu.setFont(boldfont14);
					editMenu.add("Undo").addActionListener(emic);
					editMenu.getItem(0).setEnabled(undom.canUndo());
					editMenu.setMnemonic('E');
					editMenu.getItem(0).setMnemonic('U');
					editMenu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					editMenu.add("Redo").addActionListener(emic);
					editMenu.getItem(1).setEnabled(undom.canRedo());
					editMenu.getItem(1).setMnemonic('R');
					editMenu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					editMenu.addSeparator();
					editMenu.add("Cut").addActionListener(emic);
					editMenu.add("Copy").addActionListener(emic);
					editMenu.add("Paste").addActionListener(emic);
					editMenu.getItem(3).setMnemonic('C');
					editMenu.getItem(3).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					editMenu.getItem(4).setMnemonic('C');
					editMenu.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					editMenu.getItem(5).setMnemonic('P');
					editMenu.getItem(5).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					editMenu.addSeparator();
					editMenu.add("Select All").addActionListener(emic);
					editMenu.getItem(7).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					editMenu.getItem(7).setMnemonic('S');
					editMenu.setCursor(clickcursor);
					((JPopupMenu)editMenu.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
					editMenu.getItem(0).setFont(plainfont12);
					editMenu.getItem(1).setFont(plainfont12);
					editMenu.getItem(3).setFont(plainfont12);
					editMenu.getItem(4).setFont(plainfont12);
					editMenu.getItem(5).setFont(plainfont12);
					editMenu.getItem(7).setFont(plainfont12);
					editMenu.getItem(0).setCursor(clickcursor);
					editMenu.getItem(1).setCursor(clickcursor);
					editMenu.getItem(3).setCursor(clickcursor);
					editMenu.getItem(4).setCursor(clickcursor);
					editMenu.getItem(5).setCursor(clickcursor);
					editMenu.getItem(7).setCursor(clickcursor);
					editMenu.getItem(0).setIcon(new ImageIcon("resources/undoIcon.png"));
					editMenu.getItem(1).setIcon(new ImageIcon("resources/redoIcon.png"));
					editMenu.getItem(3).setIcon(new ImageIcon("resources/cutIcon.png"));
					editMenu.getItem(4).setIcon(new ImageIcon("resources/copyIcon.png"));
					editMenu.getItem(5).setIcon(new ImageIcon("resources/pasteIcon.png"));
					editMenu.getItem(7).setIcon(new ImageIcon("resources/selectIcon.png"));
					menuBar.add(editMenu);
					JMenu spellcheck = new JMenu("SpellCheck");
					spellcheck.setForeground(Color.WHITE);
					spellcheck.setMnemonic('S');
					spellcheck.setFont(boldfont14);
					spellcheck.add("Run").addActionListener(scic);
					spellcheck.add("Configure").addActionListener(scic);
					spellcheck.getItem(0).setMnemonic('R');
					((JPopupMenu)spellcheck.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
					spellcheck.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
					spellcheck.getItem(1).setMnemonic('C');
					spellcheck.getItem(0).setFont(plainfont12);
					spellcheck.getItem(1).setFont(plainfont12);
					spellcheck.getItem(0).setCursor(clickcursor);
					spellcheck.getItem(1).setCursor(clickcursor);
					spellcheck.setCursor(clickcursor);
					spellcheck.getItem(0).setIcon(new ImageIcon("resources/runIcon.png"));
					spellcheck.getItem(1).setIcon(new ImageIcon("resources/configIcon.png"));
					menuBar.add(spellcheck);
					thisFrame.revalidate();
					thisFrame.repaint();
				}
    			textarea.addKeyListener(new KeyListener() {
					public void keyPressed(KeyEvent e) {}
					public void keyTyped(KeyEvent e) {
						menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
    					menuBar.getMenu(1).getItem(0).setText(undom.getUndoPresentationName());
    					menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
    					menuBar.getMenu(1).getItem(1).setText(undom.getRedoPresentationName());
					}
					public void keyReleased(KeyEvent e) {}
				});
				textarea.requestFocusInWindow();
			} else if (itemName.equals("Close")) {
				if (editor.getTabCount()>0) {
					Component toremove = editor.getSelectedComponent();
					ourMap.remove(toremove);
					File temp = fileMap.get(toremove);
					if (temp!=null)
						fileset.remove(fileMap.get(toremove).getAbsolutePath());
					fileMap.remove(toremove);
					undoMap.remove(toremove);
					editor.remove(toremove);
					if (editor.getTabCount()==0) {
						thisFrame.remove(editor);
						/*thisFrame.setLayout(new GridBagLayout());
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridx = 0;
						gbc.gridy = 0;
						thisFrame.add(logo, gbc);*/
						thisFrame.add(logoPanel);
						thisFrame.revalidate();
						thisFrame.repaint();
						menuBar.remove(2);
						menuBar.remove(1);
						menuBar.getMenu(0).getItem(2).setEnabled(false);
	    				menuBar.getMenu(0).getItem(3).setEnabled(false);
					}
				}
			} else if (itemName.equals("Open")) {
				int choice = 0;
				if (!offline) {
					String[] options = new String[2];
					options[0] = "Online";
					options[1] = "Offline";
					choice = JOptionPane.showOptionDialog(thisFrame, "Where would you like to open this file?", "Open...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
					System.out.println(choice);
				}
				if (offline || choice == 1) {
					JFileChooser fc = null;
					if (currDir.equals("")) {
						fc = new JFileChooser(System.getProperty("user.dir"));
					} else {
						fc = new JFileChooser(currDir);
					}
					fc.setAcceptAllFileFilterUsed(false);
					fc.setFileFilter(new FileNameExtensionFilter("text files (*.txt)", "txt"));
					fc.setDialogTitle("Open File...");
					int returnValue = fc.showOpenDialog(null);
					currDir = fc.getCurrentDirectory().getPath();
				    if (returnValue == JFileChooser.APPROVE_OPTION) {
				       	if (fc.getSelectedFile()!=null) {
				       		File openfile = fc.getSelectedFile();
				       		if (fileset.contains(openfile.getAbsolutePath())) {
								JOptionPane.showMessageDialog(null, "Cannot open " + openfile.getName() + " - file is already opened in another tab!", "File is already opened!", JOptionPane.ERROR_MESSAGE);
			        			return;
			        		}
			        		if (!openfile.getName().endsWith(".txt") || openfile.getName().length()<5) {
								JOptionPane.showMessageDialog(null, "You input illegal filename!", "Illegal Filename", JOptionPane.ERROR_MESSAGE);
								return;
							}
			        		FileReader fr = null;
			        		try {
			        			fr = new FileReader(openfile);
			        			BufferedReader br = new BufferedReader(fr);
			        			JPanel tab = new JPanel();
			        			isMine.put(tab, 0); //TODO
				        		JTextArea textarea = new JTextArea();
				        		UndoManager undom = new UndoManager();
				    			textarea.getDocument().addUndoableEditListener(undom);
				       			textarea.setLineWrap(true);
				       			textarea.setCursor(textcursor);
				       			textarea.setMargin(new Insets(5, 5, 5, 5));
				       			textarea.setBackground(goldColor0);
				   				textarea.setSelectionColor(goldColor3);
				   				textarea.setSelectedTextColor(goldColor6);
				  				textarea.setForeground(goldColor5);
				  				textarea.setFont(textfont);
				        		JScrollPane scrollPane = new JScrollPane(textarea);
				        		scrollPane.setBorder(null);
				    			JScrollBar scrollbar = new JScrollBar();
				   				scrollbar.setUI(new BasicScrollBarUI() {
				   					protected void configureScrollBarColors() {
				  						super.configureScrollBarColors();
			    						thumbColor = goldColor1;
			    						trackColor = goldColor4;
				    					thumbLightShadowColor = goldColor1;
				    			        thumbDarkShadowColor = goldColor1;
				    			        thumbHighlightColor = goldColor1;
				    			        trackHighlightColor = goldColor2;
				    				}
				    				protected void installComponents() {
				    					super.installComponents();
				    					scrollbar.remove(incrButton);
				    					incrButton = new JButton("lol") {
											private static final long serialVersionUID = -1042540660865588326L;
												public void paintComponent(Graphics g) {
												//super.paintComponent(g);
				    							Dimension size = this.getSize();
				    							g.setColor(goldColor1);
				    							g.fillRect(0, 0, size.width, size.height);
				    							try {
				    								g.drawImage(ImageIO.read(new File("resources/arrowDownBut.png")), 0, 0, size.width, size.height, this);
				    							} catch (IOException ioe) {}
				    						}
				   						};
				   						incrButton.setCursor(clickcursor);
				   						//incrButton.setBackground(goldColor4);
				   						incrButton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, goldColor1));
			    						scrollbar.add(incrButton);
			    						scrollbar.remove(decrButton);
			    						decrButton = new JButton("lol") {
											private static final long serialVersionUID = 6230078406438332260L;	
											public void paintComponent(Graphics g) {
				    							//super.paintComponent(g);
				    							Dimension size = this.getSize();
				    							g.setColor(goldColor1);
				    							g.fillRect(0, 0, size.width, size.height);
				    							try {
				    								g.drawImage(ImageIO.read(new File("resources/arrowUpBut.png")), 0, 0, size.width, size.height, this);
				    							} catch (IOException ioe) {}
				    						}
				    					};
				    					decrButton.setCursor(clickcursor);
				    					//decrButton.setBackground(goldColor4);
				    					decrButton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, goldColor1));
				    					scrollbar.add(decrButton);
				    				}
				   				});
				    			scrollPane.setVerticalScrollBar(scrollbar);
				        		tab.add(scrollPane, BorderLayout.CENTER);
				        		tab.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, goldColor6));
				       			tab.setLayout(new BoxLayout(tab, BoxLayout.X_AXIS));
				       			editor.add(openfile.getName(), tab);
				       			ourMap.put(tab, textarea);
				       			fileMap.put(tab, openfile);
				       			editor.setSelectedComponent(tab);
				       			fileset.add(openfile.getAbsolutePath());
				       			if (editor.getTabCount()==1) {
				       				thisFrame.remove(logoPanel);
			        				//thisFrame.setLayout(new BorderLayout());
			        				thisFrame.add(editor);
			        				menuBar.getMenu(0).getItem(2).setEnabled(true);
			    					menuBar.getMenu(0).getItem(3).setEnabled(true);
			    					JMenu editMenu = new JMenu("Edit");
			    					editMenu.setForeground(Color.WHITE);
			    					editMenu.setFont(boldfont14);
			    					editMenu.add("Undo").addActionListener(emic);
			    					editMenu.getItem(0).setEnabled(undom.canUndo());
			    					editMenu.setMnemonic('E');
			    					editMenu.getItem(0).setMnemonic('U');
			    					editMenu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			    					editMenu.add("Redo").addActionListener(emic);
			    					editMenu.getItem(1).setEnabled(undom.canRedo());
			    					editMenu.getItem(1).setMnemonic('R');
			    					editMenu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			    					editMenu.addSeparator();
			    					editMenu.add("Cut").addActionListener(emic);
			    					editMenu.add("Copy").addActionListener(emic);
			    					editMenu.add("Paste").addActionListener(emic);
			    					editMenu.getItem(3).setMnemonic('C');
			    					editMenu.getItem(3).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			    					editMenu.getItem(4).setMnemonic('C');
			    					editMenu.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			    					editMenu.getItem(5).setMnemonic('P');
			    					editMenu.getItem(5).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			    					editMenu.addSeparator();
			    					editMenu.add("Select All").addActionListener(emic);
			    					editMenu.getItem(7).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			    					editMenu.getItem(7).setMnemonic('S');
			    					((JPopupMenu)editMenu.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
			    					((JPopupMenu)editMenu.getItem(0).getParent()).setForeground(goldColor3);
			    					editMenu.getItem(0).setFont(plainfont12);
			    					editMenu.getItem(1).setFont(plainfont12);
			    					editMenu.getItem(3).setFont(plainfont12);
			    					editMenu.getItem(4).setFont(plainfont12);
			    					editMenu.getItem(5).setFont(plainfont12);
			    					editMenu.getItem(7).setFont(plainfont12);
			    					editMenu.getItem(0).setCursor(clickcursor);
			    					editMenu.getItem(1).setCursor(clickcursor);
			    					editMenu.getItem(3).setCursor(clickcursor);
			    					editMenu.getItem(4).setCursor(clickcursor);
			    					editMenu.getItem(5).setCursor(clickcursor);
			    					editMenu.getItem(7).setCursor(clickcursor);
			    					editMenu.setCursor(clickcursor);
			    					editMenu.getItem(0).setIcon(new ImageIcon("resources/undoIcon.png"));
			    					editMenu.getItem(1).setIcon(new ImageIcon("resources/redoIcon.png"));
			    					editMenu.getItem(3).setIcon(new ImageIcon("resources/cutIcon.png"));
			    					editMenu.getItem(4).setIcon(new ImageIcon("resources/copyIcon.png"));
			    					editMenu.getItem(5).setIcon(new ImageIcon("resources/pasteIcon.png"));
			    					editMenu.getItem(7).setIcon(new ImageIcon("resources/selectIcon.png"));
			    					menuBar.add(editMenu);
			    					JMenu spellcheck = new JMenu("SpellCheck");
			    					spellcheck.setForeground(Color.WHITE);
			    					spellcheck.setMnemonic('S');
			    					spellcheck.setFont(boldfont14);
			    					spellcheck.add("Run").addActionListener(scic);
			    					spellcheck.add("Configure").addActionListener(scic);
			    					spellcheck.getItem(0).setMnemonic('R');
			    					((JPopupMenu)spellcheck.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
			    					spellcheck.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
			    					spellcheck.getItem(1).setMnemonic('C');
			    					spellcheck.setCursor(clickcursor);
			    					spellcheck.getItem(0).setFont(plainfont12);
			    					spellcheck.getItem(1).setFont(plainfont12);
			    					spellcheck.getItem(0).setCursor(clickcursor);
			    					spellcheck.getItem(1).setCursor(clickcursor);
			    					spellcheck.getItem(0).setIcon(new ImageIcon("resources/runIcon.png"));
			    					spellcheck.getItem(1).setIcon(new ImageIcon("resources/configIcon.png"));
			    					menuBar.add(spellcheck);
			    					thisFrame.revalidate();
			    					thisFrame.repaint();
				        		}
				       			while (br.ready()) {
				       				String line = br.readLine();
				       				textarea.insert(line, textarea.getCaretPosition());
				       				if (br.ready()) {
				       					textarea.insert("" + '\n', textarea.getCaretPosition());
				        			}
				        		}
				       			br.close();
				       			undoMap.put(tab, undom);
				       			menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
				       			menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
				       			textarea.addKeyListener(new KeyListener() {
				       				public void keyPressed(KeyEvent e) {}
				       				public void keyTyped(KeyEvent e) {
				       					menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
				       					menuBar.getMenu(1).getItem(0).setText(undom.getUndoPresentationName());
				       					menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
				       					menuBar.getMenu(1).getItem(1).setText(undom.getRedoPresentationName());
				        			}
				       				public void keyReleased(KeyEvent e) {}
				        		});
				        		textarea.requestFocusInWindow();
				        	} catch (FileNotFoundException fnfe) {
					   			JOptionPane.showMessageDialog(null, "Sorry, the file you chose could not be found!", "File Not Found!", JOptionPane.ERROR_MESSAGE);
				       		} catch (IOException ioe) {
				       		} finally {
				       			if (fr!=null) {
				       				try {
				       					fr.close();
				       				} catch (IOException ioe) {}
				       			}
			        		}
			        	}
			        }
				} else if (choice==0) {
					Vector<String> listdata = new Vector<String>();
					try {
						//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
						NotepadMessage message = new NotepadMessage("getSharesList", "", 0); // get name of the doc
						oos.writeObject(message);
						oos.flush();
						//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
						listdata = (Vector<String>) ois.readObject();
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(thisFrame, "Getting shared list unsuccessful! You are now offline!", "No success!", JOptionPane.ERROR_MESSAGE);
						offline = true;
						label.setText("offline");
						return;
					} catch (ClassNotFoundException cnfe) {
						JOptionPane.showMessageDialog(thisFrame, "Getting shared list unsuccessful! You are now offline!", "No success!", JOptionPane.ERROR_MESSAGE);
						offline = true;
						label.setText("offline");
						return;
					}
					
					// Ask for list of people who can view current file
					JFrame fram = new JFrame("Choose User");
					JLabel toplabel = new JLabel("Choose User");
					toplabel.setFont(boldfont14);
					toplabel.setBorder(BorderFactory.createMatteBorder(10, 10, 0, 10, goldColor1));
					fram.add(toplabel, BorderLayout.NORTH);
					JList<String> list = new JList<String>(listdata);
					JScrollPane scrollp = new JScrollPane(list);
					scrollp.setBorder(BorderFactory.createLineBorder(goldColor1, 10));
					list.setFixedCellWidth(118);
					list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
					list.setVisibleRowCount(-1);
					list.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
					list.setFont(plainfont12);
					list.setBackground(goldColor0);
					list.setSelectionBackground(goldColor3);
					list.setSelectionForeground(goldColor6);
					list.setForeground(goldColor5);
					fram.add(list, BorderLayout.CENTER);
					JPanel butons = new JPanel();
					butons.setLayout(new BoxLayout(butons, BoxLayout.LINE_AXIS));
					butons.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
					JButton chose = new JButton("Select") {
						private static final long serialVersionUID = 1365411118095749132L;

						public void paintComponent(Graphics g) {
							//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
								} catch (IOException e) {}
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Select", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Select")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					chose.setCursor(clickcursor);
					chose.setBackground(goldColor1);
					chose.setBorder(BorderFactory.createEmptyBorder());
					chose.setMargin(new Insets(0,0,0,0));
					chose.setPreferredSize(new Dimension(100, 20));
					chose.setMinimumSize(new Dimension(100,20));
					JButton cancl = new JButton("My files") {
						private static final long serialVersionUID = 1365411118095749132L;

						public void paintComponent(Graphics g) {
							//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
								} catch (IOException e) {}
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("My files", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("My files")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					cancl.setCursor(clickcursor);
					cancl.setBackground(goldColor1);
					cancl.setBorder(BorderFactory.createEmptyBorder());
					cancl.setPreferredSize(new Dimension(100, 20));
					JPanel but1 = new JPanel();
					but1.add(cancl);
					JPanel but2 = new JPanel();
					but2.add(chose);
					butons.add(but2);
					butons.add(but1);
					butons.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
					fram.add(butons, BorderLayout.SOUTH);
					fram.setCursor(mycursor);
					fram.setSize(300, 300);
					fram.setLocationRelativeTo(thisFrame);
					fram.setVisible(true);
					WindowFocusListener listen = new WindowFocusListener() {
						@Override
						public void windowGainedFocus(WindowEvent e) {
							fram.toFront();
						}
						@Override
						public void windowLostFocus(WindowEvent e) {}	
					};
					thisFrame.addWindowFocusListener(listen);
					chose.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							thisFrame.removeWindowFocusListener(listen);
							fram.setVisible(false);
							
							String usr = list.getSelectedValue();
							Vector<String> listdata = null;
							
							try {
								//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
								NotepadMessage message = new NotepadMessage("getSharedWithMe", usr, 0);
								oos.writeObject(message);
								oos.flush();
								//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
								listdata = (Vector<String>) ois.readObject();
							} catch (IOException ioe) {
								JOptionPane.showMessageDialog(thisFrame, "Open unsuccessful! You are now offline!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
								offline = true;
								label.setText("offline");
								return;
							} catch (ClassNotFoundException cnfe) {
								JOptionPane.showMessageDialog(thisFrame, "Open unsuccessful! You are now offline!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
								offline = true;
								label.setText("offline");
								return;
							}
							JFrame fram = new JFrame("Choose file to open...");
							JPanel chooser = new JPanel(new BorderLayout());
							//String[] listdata = {"list1", "list2", "list3", "list4"};
							JList<String> list = new JList<String>(listdata);
							JScrollPane scrollp = new JScrollPane(list);
							scrollp.setBorder(BorderFactory.createLineBorder(goldColor1, 10));
							list.setFixedCellWidth(118);
							list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
							list.setVisibleRowCount(-1);
							list.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
							list.setFont(plainfont12);
							list.setBackground(goldColor0);
							list.setSelectionBackground(goldColor3);
							list.setSelectionForeground(goldColor6);
							list.setForeground(goldColor5);
							JLabel toplabel = new JLabel("Select a file:");
							toplabel.setFont(boldfont14);
							toplabel.setBorder(BorderFactory.createMatteBorder(10, 10, 0, 10, goldColor1));
							//chooser.setLayout(new BoxLayout(chooser, BoxLayout.Y_AXIS));
							chooser.add(toplabel, BorderLayout.NORTH);
							chooser.add(scrollp);
							JPanel filePanel = new JPanel();
							JLabel filelabel = new JLabel("File:");
							filelabel.setFont(boldfont14);
							JTextField textfield = new JTextField();
							textfield.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
							textfield.setFont(plainfont12);
							textfield.setBackground(goldColor0);
							textfield.setSelectionColor(goldColor3);
							textfield.setSelectedTextColor(goldColor6);
							textfield.setForeground(goldColor5);
							textfield.setCursor(textcursor);
							textfield.setEditable(false);
							//textfield.setMargin(new Insets(3,3,3,3));
							filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.LINE_AXIS));
							filePanel.add(filelabel);
							filePanel.add(textfield);
							filePanel.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
							chooser.add(filePanel, BorderLayout.SOUTH);
							list.addListSelectionListener(new ListSelectionListener() {
								@Override
								public void valueChanged(ListSelectionEvent e) {
									textfield.setText(list.getSelectedValue());
								}
							});
							JPanel butons = new JPanel();
							butons.setLayout(new BoxLayout(butons, BoxLayout.LINE_AXIS));
							butons.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
							JButton chose = new JButton("Select") {
								private static final long serialVersionUID = 1365411118095749132L;

								public void paintComponent(Graphics g) {
									//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
									Dimension size = this.getSize();
									if (this.getModel().isRollover()) {
										try {
											g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
										} catch (IOException ioe) {}
									} else {
										try {
											g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
										} catch (IOException e) {}
									}
									g.setFont(plainfont14);
					                g.setColor(Color.WHITE);
					                g.drawString("Select", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Select")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
								}
							};
							chose.setCursor(clickcursor);
							chose.setBackground(goldColor1);
							chose.setBorder(BorderFactory.createEmptyBorder());
							chose.setMargin(new Insets(0,0,0,0));
							chose.setPreferredSize(new Dimension(100, 20));
							chose.setMinimumSize(new Dimension(100,20));
							JButton cancl = new JButton("Cancel") {
								private static final long serialVersionUID = 1365411118095749132L;

								public void paintComponent(Graphics g) {
									//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
									Dimension size = this.getSize();
									if (this.getModel().isRollover()) {
										try {
											g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
										} catch (IOException ioe) {}
									} else {
										try {
											g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
										} catch (IOException e) {}
									}
									g.setFont(plainfont14);
					                g.setColor(Color.WHITE);
					                g.drawString("Cancel", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Cancel")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
								}
							};
							cancl.setCursor(clickcursor);
							cancl.setBackground(goldColor1);
							cancl.setBorder(BorderFactory.createEmptyBorder());
							cancl.setPreferredSize(new Dimension(100, 20));
							JPanel but1 = new JPanel();
							but1.add(chose);
							JPanel but2 = new JPanel();
							but2.add(cancl);
							butons.add(but1);
							butons.add(but2);
							butons.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
							fram.add(chooser);
							fram.add(butons, BorderLayout.SOUTH);
							fram.setCursor(mycursor);
							fram.setSize(500, 400);
							fram.setLocationRelativeTo(thisFrame);
							fram.setVisible(true);
							WindowFocusListener listen = new WindowFocusListener() {
								@Override
								public void windowGainedFocus(WindowEvent e) {
									fram.toFront();
								}
								@Override
								public void windowLostFocus(WindowEvent e) {}	
							};
							thisFrame.addWindowFocusListener(listen);
							cancl.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									thisFrame.removeWindowFocusListener(listen);
									fram.setVisible(false);
								}
							});
							chose.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									String filename = textfield.getText();
									String text = null;
									if (filename.equals("")) {
										JOptionPane.showMessageDialog(fram, "No file selected!", "No selection!", JOptionPane.ERROR_MESSAGE);
										return;
									}
									try {
										//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
										NotepadMessage message = new NotepadMessage("Open", usr + "/" + filename, 0);
										oos.writeObject(message);
										oos.flush();
										//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
										text = (String) ois.readObject();
										if (text == null) {
											JOptionPane.showMessageDialog(fram, "Open unsuccessful!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
											thisFrame.removeWindowFocusListener(listen);
											fram.setVisible(false);
										}
										JPanel tab = new JPanel();
										System.out.println("here");
										isMine.put(tab, 0);
						        		JTextArea textarea = new JTextArea();
						        		textarea.setText(text);
						        		UndoManager undom = new UndoManager();
						    			textarea.getDocument().addUndoableEditListener(undom);
						       			textarea.setLineWrap(true);
						       			textarea.setCursor(textcursor);
						       			textarea.setMargin(new Insets(5, 5, 5, 5));
						       			textarea.setBackground(goldColor0);
						   				textarea.setSelectionColor(goldColor3);
						   				textarea.setSelectedTextColor(goldColor6);
						  				textarea.setForeground(goldColor5);
						  				textarea.setFont(textfont);
						        		JScrollPane scrollPane = new JScrollPane(textarea);
						        		scrollPane.setBorder(null);
						    			JScrollBar scrollbar = new JScrollBar();
						   				scrollbar.setUI(new BasicScrollBarUI() {
						   					protected void configureScrollBarColors() {
						  						super.configureScrollBarColors();
					    						thumbColor = goldColor1;
					    						trackColor = goldColor4;
						    					thumbLightShadowColor = goldColor1;
						    			        thumbDarkShadowColor = goldColor1;
						    			        thumbHighlightColor = goldColor1;
						    			        trackHighlightColor = goldColor2;
						    				}
						    				protected void installComponents() {
						    					super.installComponents();
						    					scrollbar.remove(incrButton);
						    					incrButton = new JButton("lol") {
													private static final long serialVersionUID = -1042540660865588326L;
														public void paintComponent(Graphics g) {
														//super.paintComponent(g);
						    							Dimension size = this.getSize();
						    							g.setColor(goldColor1);
						    							g.fillRect(0, 0, size.width, size.height);
						    							try {
						    								g.drawImage(ImageIO.read(new File("resources/arrowDownBut.png")), 0, 0, size.width, size.height, this);
						    							} catch (IOException ioe) {}
						    						}
						   						};
						   						incrButton.setCursor(clickcursor);
						   						//incrButton.setBackground(goldColor4);
						   						incrButton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, goldColor1));
					    						scrollbar.add(incrButton);
					    						scrollbar.remove(decrButton);
					    						decrButton = new JButton("lol") {
													private static final long serialVersionUID = 6230078406438332260L;	
													public void paintComponent(Graphics g) {
						    							//super.paintComponent(g);
						    							Dimension size = this.getSize();
						    							g.setColor(goldColor1);
						    							g.fillRect(0, 0, size.width, size.height);
						    							try {
						    								g.drawImage(ImageIO.read(new File("resources/arrowUpBut.png")), 0, 0, size.width, size.height, this);
						    							} catch (IOException ioe) {}
						    						}
						    					};
						    					decrButton.setCursor(clickcursor);
						    					//decrButton.setBackground(goldColor4);
						    					decrButton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, goldColor1));
						    					scrollbar.add(decrButton);
						    				}
						   				});
						    			scrollPane.setVerticalScrollBar(scrollbar);
						        		tab.add(scrollPane, BorderLayout.CENTER);
						        		tab.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, goldColor6));
						       			tab.setLayout(new BoxLayout(tab, BoxLayout.X_AXIS));
						       			ourMap.put(tab, textarea);
						       			editor.addTab(usr + "/" + filename, tab);
						       			editor.setSelectedComponent(tab);
						       			if (editor.getTabCount()==1) {
						       				thisFrame.remove(logoPanel);
					        				//thisFrame.setLayout(new BorderLayout());
					        				thisFrame.add(editor);
					        				menuBar.getMenu(0).getItem(2).setEnabled(true);
					    					menuBar.getMenu(0).getItem(3).setEnabled(true);
					    					JMenu editMenu = new JMenu("Edit");
					    					editMenu.setForeground(Color.WHITE);
					    					editMenu.setFont(boldfont14);
					    					editMenu.add("Undo").addActionListener(emic);
					    					editMenu.getItem(0).setEnabled(undom.canUndo());
					    					editMenu.setMnemonic('E');
					    					editMenu.getItem(0).setMnemonic('U');
					    					editMenu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.add("Redo").addActionListener(emic);
					    					editMenu.getItem(1).setEnabled(undom.canRedo());
					    					editMenu.getItem(1).setMnemonic('R');
					    					editMenu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.addSeparator();
					    					editMenu.add("Cut").addActionListener(emic);
					    					editMenu.add("Copy").addActionListener(emic);
					    					editMenu.add("Paste").addActionListener(emic);
					    					editMenu.getItem(3).setMnemonic('C');
					    					editMenu.getItem(3).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.getItem(4).setMnemonic('C');
					    					editMenu.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.getItem(5).setMnemonic('P');
					    					editMenu.getItem(5).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.addSeparator();
					    					editMenu.add("Select All").addActionListener(emic);
					    					editMenu.getItem(7).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.getItem(7).setMnemonic('S');
					    					((JPopupMenu)editMenu.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
					    					((JPopupMenu)editMenu.getItem(0).getParent()).setForeground(goldColor3);
					    					editMenu.getItem(0).setFont(plainfont12);
					    					editMenu.getItem(1).setFont(plainfont12);
					    					editMenu.getItem(3).setFont(plainfont12);
					    					editMenu.getItem(4).setFont(plainfont12);
					    					editMenu.getItem(5).setFont(plainfont12);
					    					editMenu.getItem(7).setFont(plainfont12);
					    					editMenu.getItem(0).setCursor(clickcursor);
					    					editMenu.getItem(1).setCursor(clickcursor);
					    					editMenu.getItem(3).setCursor(clickcursor);
					    					editMenu.getItem(4).setCursor(clickcursor);
					    					editMenu.getItem(5).setCursor(clickcursor);
					    					editMenu.getItem(7).setCursor(clickcursor);
					    					editMenu.setCursor(clickcursor);
					    					editMenu.getItem(0).setIcon(new ImageIcon("resources/undoIcon.png"));
					    					editMenu.getItem(1).setIcon(new ImageIcon("resources/redoIcon.png"));
					    					editMenu.getItem(3).setIcon(new ImageIcon("resources/cutIcon.png"));
					    					editMenu.getItem(4).setIcon(new ImageIcon("resources/copyIcon.png"));
					    					editMenu.getItem(5).setIcon(new ImageIcon("resources/pasteIcon.png"));
					    					editMenu.getItem(7).setIcon(new ImageIcon("resources/selectIcon.png"));
					    					menuBar.add(editMenu);
					    					JMenu spellcheck = new JMenu("SpellCheck");
					    					spellcheck.setForeground(Color.WHITE);
					    					spellcheck.setMnemonic('S');
					    					spellcheck.setFont(boldfont14);
					    					spellcheck.add("Run").addActionListener(scic);
					    					spellcheck.add("Configure").addActionListener(scic);
					    					spellcheck.getItem(0).setMnemonic('R');
					    					((JPopupMenu)spellcheck.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
					    					spellcheck.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
					    					spellcheck.getItem(1).setMnemonic('C');
					    					spellcheck.setCursor(clickcursor);
					    					spellcheck.getItem(0).setFont(plainfont12);
					    					spellcheck.getItem(1).setFont(plainfont12);
					    					spellcheck.getItem(0).setCursor(clickcursor);
					    					spellcheck.getItem(1).setCursor(clickcursor);
					    					spellcheck.getItem(0).setIcon(new ImageIcon("resources/runIcon.png"));
					    					spellcheck.getItem(1).setIcon(new ImageIcon("resources/configIcon.png"));
					    					menuBar.add(spellcheck);
					    					menuIsThere = true;
					    					thisFrame.revalidate();
					    					thisFrame.repaint();
						        		}
						       			undoMap.put(tab, undom);
						       			menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
						       			menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
						       			textarea.addKeyListener(new KeyListener() {
						       				public void keyPressed(KeyEvent e) {}
						       				public void keyTyped(KeyEvent e) {
						       					menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
						       					menuBar.getMenu(1).getItem(0).setText(undom.getUndoPresentationName());
						       					menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
						       					menuBar.getMenu(1).getItem(1).setText(undom.getRedoPresentationName());
						        			}
						       				public void keyReleased(KeyEvent e) {}
						        		});
						        		textarea.requestFocusInWindow();
										
									} catch (IOException ioe) {
										JOptionPane.showMessageDialog(fram, "Open unsuccessful! You are now offline!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
										offline = true;
										label.setText("offline");
										thisFrame.removeWindowFocusListener(listen);
										fram.setVisible(false);
										return;
									} catch (ClassNotFoundException cnfe) {
										JOptionPane.showMessageDialog(fram, "Open unsuccessful! You are now offline!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
										offline = true;
										label.setText("offline");
										thisFrame.removeWindowFocusListener(listen);
										fram.setVisible(false);
										return;
									}
									thisFrame.removeWindowFocusListener(listen);
									fram.setVisible(false);
								}
							});
						}
					});
					cancl.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							thisFrame.removeWindowFocusListener(listen);
							fram.setVisible(false);
							
							String usr = list.getSelectedValue();
							Vector<String> listdata = null;
							
							try {
								//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
								NotepadMessage message = new NotepadMessage("Open/Save", userName, 0);
								oos.writeObject(message);
								oos.flush();
								//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
								listdata = (Vector<String>) ois.readObject();
							} catch (IOException ioe) {
								JOptionPane.showMessageDialog(thisFrame, "Open unsuccessful! You are now offline!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
								offline = true;
								label.setText("offline");
								return;
							} catch (ClassNotFoundException cnfe) {
								JOptionPane.showMessageDialog(thisFrame, "Open unsuccessful! You are now offline!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
								offline = true;
								label.setText("offline");
								return;
							}
							JFrame fram = new JFrame("Choose file to open...");
							JPanel chooser = new JPanel(new BorderLayout());
							//String[] listdata = {"list1", "list2", "list3", "list4"};
							JList<String> list = new JList<String>(listdata);
							JScrollPane scrollp = new JScrollPane(list);
							scrollp.setBorder(BorderFactory.createLineBorder(goldColor1, 10));
							list.setFixedCellWidth(118);
							list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
							list.setVisibleRowCount(-1);
							list.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
							list.setFont(plainfont12);
							list.setBackground(goldColor0);
							list.setSelectionBackground(goldColor3);
							list.setSelectionForeground(goldColor6);
							list.setForeground(goldColor5);
							JLabel toplabel = new JLabel("Select a file:");
							toplabel.setFont(boldfont14);
							toplabel.setBorder(BorderFactory.createMatteBorder(10, 10, 0, 10, goldColor1));
							//chooser.setLayout(new BoxLayout(chooser, BoxLayout.Y_AXIS));
							chooser.add(toplabel, BorderLayout.NORTH);
							chooser.add(scrollp);
							JPanel filePanel = new JPanel();
							JLabel filelabel = new JLabel("File:");
							filelabel.setFont(boldfont14);
							JTextField textfield = new JTextField();
							textfield.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
							textfield.setFont(plainfont12);
							textfield.setBackground(goldColor0);
							textfield.setSelectionColor(goldColor3);
							textfield.setSelectedTextColor(goldColor6);
							textfield.setForeground(goldColor5);
							textfield.setCursor(textcursor);
							textfield.setEditable(false);
							//textfield.setMargin(new Insets(3,3,3,3));
							filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.LINE_AXIS));
							filePanel.add(filelabel);
							filePanel.add(textfield);
							filePanel.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
							chooser.add(filePanel, BorderLayout.SOUTH);
							list.addListSelectionListener(new ListSelectionListener() {
								@Override
								public void valueChanged(ListSelectionEvent e) {
									textfield.setText(list.getSelectedValue());
								}
							});
							JPanel butons = new JPanel();
							butons.setLayout(new BoxLayout(butons, BoxLayout.LINE_AXIS));
							butons.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
							JButton chose = new JButton("Select") {
								private static final long serialVersionUID = 1365411118095749132L;

								public void paintComponent(Graphics g) {
									//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
									Dimension size = this.getSize();
									if (this.getModel().isRollover()) {
										try {
											g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
										} catch (IOException ioe) {}
									} else {
										try {
											g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
										} catch (IOException e) {}
									}
									g.setFont(plainfont14);
					                g.setColor(Color.WHITE);
					                g.drawString("Select", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Select")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
								}
							};
							chose.setCursor(clickcursor);
							chose.setBackground(goldColor1);
							chose.setBorder(BorderFactory.createEmptyBorder());
							chose.setMargin(new Insets(0,0,0,0));
							chose.setPreferredSize(new Dimension(100, 20));
							chose.setMinimumSize(new Dimension(100,20));
							JButton cancl = new JButton("Cancel") {
								private static final long serialVersionUID = 1365411118095749132L;

								public void paintComponent(Graphics g) {
									//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
									Dimension size = this.getSize();
									if (this.getModel().isRollover()) {
										try {
											g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
										} catch (IOException ioe) {}
									} else {
										try {
											g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
										} catch (IOException e) {}
									}
									g.setFont(plainfont14);
					                g.setColor(Color.WHITE);
					                g.drawString("Cancel", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Cancel")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
								}
							};
							cancl.setCursor(clickcursor);
							cancl.setBackground(goldColor1);
							cancl.setBorder(BorderFactory.createEmptyBorder());
							cancl.setPreferredSize(new Dimension(100, 20));
							JPanel but1 = new JPanel();
							but1.add(chose);
							JPanel but2 = new JPanel();
							but2.add(cancl);
							butons.add(but1);
							butons.add(but2);
							butons.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
							fram.add(chooser);
							fram.add(butons, BorderLayout.SOUTH);
							fram.setCursor(mycursor);
							fram.setSize(500, 400);
							fram.setLocationRelativeTo(thisFrame);
							fram.setVisible(true);
							WindowFocusListener listen = new WindowFocusListener() {
								@Override
								public void windowGainedFocus(WindowEvent e) {
									fram.toFront();
								}
								@Override
								public void windowLostFocus(WindowEvent e) {}	
							};
							thisFrame.addWindowFocusListener(listen);
							cancl.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									thisFrame.removeWindowFocusListener(listen);
									fram.setVisible(false);
								}
							});
							chose.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									String filename = textfield.getText();
									String text = null;
									if (filename.equals("")) {
										JOptionPane.showMessageDialog(fram, "No file selected!", "No selection!", JOptionPane.ERROR_MESSAGE);
										return;
									}
									try {
										//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
										NotepadMessage message = new NotepadMessage("Open", userName + "/" + filename, 0);
										oos.writeObject(message);
										oos.flush();
										//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
										text = (String) ois.readObject();
										if (text == null) {
											JOptionPane.showMessageDialog(fram, "Open unsuccessful!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
											thisFrame.removeWindowFocusListener(listen);
											fram.setVisible(false);
										}
										JPanel tab = new JPanel();
										isMine.put(tab, 1);
						        		JTextArea textarea = new JTextArea();
						        		textarea.setText(text);
						        		UndoManager undom = new UndoManager();
						    			textarea.getDocument().addUndoableEditListener(undom);
						       			textarea.setLineWrap(true);
						       			textarea.setCursor(textcursor);
						       			textarea.setMargin(new Insets(5, 5, 5, 5));
						       			textarea.setBackground(goldColor0);
						   				textarea.setSelectionColor(goldColor3);
						   				textarea.setSelectedTextColor(goldColor6);
						  				textarea.setForeground(goldColor5);
						  				textarea.setFont(textfont);
						        		JScrollPane scrollPane = new JScrollPane(textarea);
						        		scrollPane.setBorder(null);
						    			JScrollBar scrollbar = new JScrollBar();
						   				scrollbar.setUI(new BasicScrollBarUI() {
						   					protected void configureScrollBarColors() {
						  						super.configureScrollBarColors();
					    						thumbColor = goldColor1;
					    						trackColor = goldColor4;
						    					thumbLightShadowColor = goldColor1;
						    			        thumbDarkShadowColor = goldColor1;
						    			        thumbHighlightColor = goldColor1;
						    			        trackHighlightColor = goldColor2;
						    				}
						    				protected void installComponents() {
						    					super.installComponents();
						    					scrollbar.remove(incrButton);
						    					incrButton = new JButton("lol") {
													private static final long serialVersionUID = -1042540660865588326L;
														public void paintComponent(Graphics g) {
														//super.paintComponent(g);
						    							Dimension size = this.getSize();
						    							g.setColor(goldColor1);
						    							g.fillRect(0, 0, size.width, size.height);
						    							try {
						    								g.drawImage(ImageIO.read(new File("resources/arrowDownBut.png")), 0, 0, size.width, size.height, this);
						    							} catch (IOException ioe) {}
						    						}
						   						};
						   						incrButton.setCursor(clickcursor);
						   						//incrButton.setBackground(goldColor4);
						   						incrButton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, goldColor1));
					    						scrollbar.add(incrButton);
					    						scrollbar.remove(decrButton);
					    						decrButton = new JButton("lol") {
													private static final long serialVersionUID = 6230078406438332260L;	
													public void paintComponent(Graphics g) {
						    							//super.paintComponent(g);
						    							Dimension size = this.getSize();
						    							g.setColor(goldColor1);
						    							g.fillRect(0, 0, size.width, size.height);
						    							try {
						    								g.drawImage(ImageIO.read(new File("resources/arrowUpBut.png")), 0, 0, size.width, size.height, this);
						    							} catch (IOException ioe) {}
						    						}
						    					};
						    					decrButton.setCursor(clickcursor);
						    					//decrButton.setBackground(goldColor4);
						    					decrButton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, goldColor1));
						    					scrollbar.add(decrButton);
						    				}
						   				});
						    			scrollPane.setVerticalScrollBar(scrollbar);
						        		tab.add(scrollPane, BorderLayout.CENTER);
						        		tab.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, goldColor6));
						       			tab.setLayout(new BoxLayout(tab, BoxLayout.X_AXIS));
						       			ourMap.put(tab, textarea);
						       			editor.addTab(userName + "/" + filename, tab);
						       			editor.setSelectedComponent(tab);
						       			if (editor.getTabCount()==1) {
						       				thisFrame.remove(logoPanel);
					        				//thisFrame.setLayout(new BorderLayout());
					        				thisFrame.add(editor);
					        				menuBar.getMenu(0).getItem(2).setEnabled(true);
					    					menuBar.getMenu(0).getItem(3).setEnabled(true);
					    					JMenu editMenu = new JMenu("Edit");
					    					editMenu.setForeground(Color.WHITE);
					    					editMenu.setFont(boldfont14);
					    					editMenu.add("Undo").addActionListener(emic);
					    					editMenu.getItem(0).setEnabled(undom.canUndo());
					    					editMenu.setMnemonic('E');
					    					editMenu.getItem(0).setMnemonic('U');
					    					editMenu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.add("Redo").addActionListener(emic);
					    					editMenu.getItem(1).setEnabled(undom.canRedo());
					    					editMenu.getItem(1).setMnemonic('R');
					    					editMenu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.addSeparator();
					    					editMenu.add("Cut").addActionListener(emic);
					    					editMenu.add("Copy").addActionListener(emic);
					    					editMenu.add("Paste").addActionListener(emic);
					    					editMenu.getItem(3).setMnemonic('C');
					    					editMenu.getItem(3).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.getItem(4).setMnemonic('C');
					    					editMenu.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.getItem(5).setMnemonic('P');
					    					editMenu.getItem(5).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.addSeparator();
					    					editMenu.add("Select All").addActionListener(emic);
					    					editMenu.getItem(7).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					    					editMenu.getItem(7).setMnemonic('S');
					    					((JPopupMenu)editMenu.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
					    					((JPopupMenu)editMenu.getItem(0).getParent()).setForeground(goldColor3);
					    					editMenu.getItem(0).setFont(plainfont12);
					    					editMenu.getItem(1).setFont(plainfont12);
					    					editMenu.getItem(3).setFont(plainfont12);
					    					editMenu.getItem(4).setFont(plainfont12);
					    					editMenu.getItem(5).setFont(plainfont12);
					    					editMenu.getItem(7).setFont(plainfont12);
					    					editMenu.getItem(0).setCursor(clickcursor);
					    					editMenu.getItem(1).setCursor(clickcursor);
					    					editMenu.getItem(3).setCursor(clickcursor);
					    					editMenu.getItem(4).setCursor(clickcursor);
					    					editMenu.getItem(5).setCursor(clickcursor);
					    					editMenu.getItem(7).setCursor(clickcursor);
					    					editMenu.setCursor(clickcursor);
					    					editMenu.getItem(0).setIcon(new ImageIcon("resources/undoIcon.png"));
					    					editMenu.getItem(1).setIcon(new ImageIcon("resources/redoIcon.png"));
					    					editMenu.getItem(3).setIcon(new ImageIcon("resources/cutIcon.png"));
					    					editMenu.getItem(4).setIcon(new ImageIcon("resources/copyIcon.png"));
					    					editMenu.getItem(5).setIcon(new ImageIcon("resources/pasteIcon.png"));
					    					editMenu.getItem(7).setIcon(new ImageIcon("resources/selectIcon.png"));
					    					menuBar.add(editMenu);
					    					JMenu spellcheck = new JMenu("SpellCheck");
					    					spellcheck.setForeground(Color.WHITE);
					    					spellcheck.setMnemonic('S');
					    					spellcheck.setFont(boldfont14);
					    					spellcheck.add("Run").addActionListener(scic);
					    					spellcheck.add("Configure").addActionListener(scic);
					    					spellcheck.getItem(0).setMnemonic('R');
					    					((JPopupMenu)spellcheck.getItem(0).getParent()).setBorder(BorderFactory.createLineBorder(goldColor3, 1));
					    					spellcheck.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
					    					spellcheck.getItem(1).setMnemonic('C');
					    					spellcheck.setCursor(clickcursor);
					    					spellcheck.getItem(0).setFont(plainfont12);
					    					spellcheck.getItem(1).setFont(plainfont12);
					    					spellcheck.getItem(0).setCursor(clickcursor);
					    					spellcheck.getItem(1).setCursor(clickcursor);
					    					spellcheck.getItem(0).setIcon(new ImageIcon("resources/runIcon.png"));
					    					spellcheck.getItem(1).setIcon(new ImageIcon("resources/configIcon.png"));
					    					menuBar.add(spellcheck);
					    					menuBar.add(usersMenu);
					    					menuIsThere = true;
					    					thisFrame.revalidate();
					    					thisFrame.repaint();
						        		}
						       			undoMap.put(tab, undom);
						       			menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
						       			menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
						       			textarea.addKeyListener(new KeyListener() {
						       				public void keyPressed(KeyEvent e) {}
						       				public void keyTyped(KeyEvent e) {
						       					menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
						       					menuBar.getMenu(1).getItem(0).setText(undom.getUndoPresentationName());
						       					menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
						       					menuBar.getMenu(1).getItem(1).setText(undom.getRedoPresentationName());
						        			}
						       				public void keyReleased(KeyEvent e) {}
						        		});
						        		textarea.requestFocusInWindow();
										
									} catch (IOException ioe) {
										JOptionPane.showMessageDialog(fram, "Open unsuccessful! You are now offline!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
										offline = true;
										label.setText("offline");
										thisFrame.removeWindowFocusListener(listen);
										fram.setVisible(false);
										return;
									} catch (ClassNotFoundException cnfe) {
										JOptionPane.showMessageDialog(fram, "Open unsuccessful! You are now offline!", "Not Openned!", JOptionPane.ERROR_MESSAGE);
										offline = true;
										label.setText("offline");
										thisFrame.removeWindowFocusListener(listen);
										fram.setVisible(false);
										return;
									}
									thisFrame.removeWindowFocusListener(listen);
									fram.setVisible(false);
								}
							});
						}
					});
					//TODO: add button
				}
			} else if (itemName.equals("Save")) {
				int choice = 0;
				if (!offline) {
					String[] options = new String[2];
					options[0] = "Online";
					options[1] = "Offline";
					choice = JOptionPane.showOptionDialog(thisFrame, "Where would you like to save this file?", "Save...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
					System.out.println(choice);
				}
				if (offline || choice==1) {
					JFileChooser fc = null;
					if (editor.getTitleAt(editor.getSelectedIndex()).equals("new")) {
						fc = new JFileChooser(System.getProperty("user.dir"));
					} else {
						fc = new JFileChooser(fileMap.get(editor.getSelectedComponent()).getParent());
						fc.setSelectedFile(fileMap.get(editor.getSelectedComponent()));
					}
					fc.setAcceptAllFileFilterUsed(false);
					fc.setFileFilter(new FileNameExtensionFilter("text files (*.txt)", "txt"));
					fc.setDialogTitle("Save As...");
					int returnValue = fc.showSaveDialog(null);
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File savefile = fc.getSelectedFile();
						if (fileset.contains(savefile.getAbsolutePath()) && !fileMap.get(editor.getComponentAt(editor.getSelectedIndex())).getAbsolutePath().equals(savefile.getAbsolutePath())) {
							JOptionPane.showMessageDialog(null, "Cannot save this file as " + savefile.getName() + " - file is already opened in another tab!", "File is already opened!", JOptionPane.ERROR_MESSAGE);
		        			return;
		        		}
						if (!savefile.getName().endsWith(".txt") || savefile.getName().length()<5) {
							JOptionPane.showMessageDialog(null, "You input illegal filename!" + '\n' + "It has to end with .txt!", "Illegal Filename", JOptionPane.ERROR_MESSAGE);
							return;
						}
						FileReader fr = null;
						try {
							fr = new FileReader(savefile);
							int result = 0;
							if (!savefile.getAbsolutePath().equals(fileMap.get(editor.getSelectedComponent()).getAbsolutePath())) {
								result = JOptionPane.showConfirmDialog(null, savefile.getName() + " already exists.\nDo you want to replace it?", "Confirm Save As", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
							}
							if (result==0) {
		        				PrintStream out = null;
		    					try {
		    					    out = new PrintStream(new FileOutputStream(savefile.getName()));
		    						String txt = ourMap.get(editor.getSelectedComponent()).getText();
		    						out.print(txt);
		    						editor.setTitleAt(editor.getSelectedIndex(), savefile.getName());
		    						fileset.remove(fileMap.get(editor.getSelectedComponent()).getAbsolutePath());
		    						fileMap.put(editor.getSelectedComponent(), savefile);
		    						fileset.add(savefile.getAbsolutePath());
		    					} catch (FileNotFoundException fnfe2) {
		    					} finally {
		    						if (out!=null) {
		    							out.close();
		    						}
		    					}
		        			}
						} catch (FileNotFoundException fnfe) {
							PrintStream out = null;
							try {
							    out = new PrintStream(new FileOutputStream(savefile.getName()));
								String txt = ourMap.get(editor.getSelectedComponent()).getText();
								out.print(txt);
								editor.setTitleAt(editor.getSelectedIndex(), savefile.getName());
								fileMap.put(editor.getSelectedComponent(), savefile);
								fileset.add(savefile.getAbsolutePath());
							} catch (FileNotFoundException fnfe2) {
								JOptionPane.showMessageDialog(null, "File could not be created!", "Save Error", JOptionPane.ERROR_MESSAGE);
							} finally {
								if (out!=null) {
									out.close();
								}
							}
						} finally {
							if (fr!=null) {
								try {
									fr.close();
								} catch (IOException ioe) {}
							}
						}
					}
				} else if (choice==0) {
					Vector<String> listdata = null;
					try {
						//ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
						NotepadMessage message = new NotepadMessage("Open/Save", userName, 0);
						oos.writeObject(message);
						oos.flush();
						//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
						listdata = (Vector<String>) ois.readObject();
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(thisFrame, "Save unsuccessful! You are now offline!", "Not Saved!", JOptionPane.ERROR_MESSAGE);
						offline = true;
						label.setText("offline");
						return;
					} catch (ClassNotFoundException cnfe) {
						JOptionPane.showMessageDialog(thisFrame, "Save unsuccessful! You are now offline!", "Not Saved!", JOptionPane.ERROR_MESSAGE);
						offline = true;
						label.setText("offline");
						return;
					}
					JFrame fram = new JFrame("Choose file to save...");
					JPanel chooser = new JPanel(new BorderLayout());
					JList<String> list = new JList<String>(listdata);
					JScrollPane scrollp = new JScrollPane(list);
					scrollp.setBorder(BorderFactory.createLineBorder(goldColor1, 10));
					list.setFixedCellWidth(118);
					list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
					list.setVisibleRowCount(-1);
					list.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
					list.setFont(plainfont12);
					list.setBackground(goldColor0);
					list.setSelectionBackground(goldColor3);
					list.setSelectionForeground(goldColor6);
					list.setForeground(goldColor5);
					JLabel toplabel = new JLabel("Select a file:");
					toplabel.setFont(boldfont14);
					toplabel.setBorder(BorderFactory.createMatteBorder(10, 10, 0, 10, goldColor1));
					//chooser.setLayout(new BoxLayout(chooser, BoxLayout.Y_AXIS));
					chooser.add(toplabel, BorderLayout.NORTH);
					chooser.add(scrollp);
					JPanel filePanel = new JPanel();
					JLabel filelabel = new JLabel("File:");
					filelabel.setFont(boldfont14);
					JTextField textfield = new JTextField();
					textfield.setBorder(BorderFactory.createLineBorder(goldColor6, 1));
					textfield.setFont(plainfont12);
					textfield.setBackground(goldColor0);
					textfield.setSelectionColor(goldColor3);
					textfield.setSelectedTextColor(goldColor6);
					textfield.setForeground(goldColor5);
					textfield.setCursor(textcursor);
					//textfield.setMargin(new Insets(3,3,3,3));
					filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.LINE_AXIS));
					filePanel.add(filelabel);
					filePanel.add(textfield);
					filePanel.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
					chooser.add(filePanel, BorderLayout.SOUTH);
					list.addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							textfield.setText(list.getSelectedValue());
						}
					});
					JPanel butons = new JPanel();
					butons.setLayout(new BoxLayout(butons, BoxLayout.LINE_AXIS));
					butons.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
					JButton chose = new JButton("Select") {
						private static final long serialVersionUID = 1365411118095749132L;
	
						public void paintComponent(Graphics g) {
							//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
								} catch (IOException e) {}
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Select", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Select")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					chose.setCursor(clickcursor);
					chose.setBackground(goldColor1);
					chose.setBorder(BorderFactory.createEmptyBorder());
					chose.setMargin(new Insets(0,0,0,0));
					chose.setPreferredSize(new Dimension(100, 20));
					chose.setMinimumSize(new Dimension(100,20));
					JButton cancl = new JButton("Cancel") {
						private static final long serialVersionUID = 1365411118095749132L;
	
						public void paintComponent(Graphics g) {
							//this.setPreferredSize(new Dimension(g.getFontMetrics(plainfont14).stringWidth("Offline") + 30, 20));
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubar.png")), 0, 0, size.width, size.height, this);
								} catch (IOException e) {}
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Cancel", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Cancel")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					cancl.setCursor(clickcursor);
					cancl.setBackground(goldColor1);
					cancl.setBorder(BorderFactory.createEmptyBorder());
					cancl.setPreferredSize(new Dimension(100, 20));
					JPanel but1 = new JPanel();
					but1.add(chose);
					JPanel but2 = new JPanel();
					but2.add(cancl);
					butons.add(but1);
					butons.add(but2);
					butons.setBorder(BorderFactory.createMatteBorder(0, 10, 10, 10, goldColor1));
					fram.add(chooser);
					fram.add(butons, BorderLayout.SOUTH);
					fram.setCursor(mycursor);
					fram.setSize(500, 400);
					fram.setLocationRelativeTo(thisFrame);
					fram.setVisible(true);
					WindowFocusListener listen = new WindowFocusListener() {
						@Override
						public void windowGainedFocus(WindowEvent e) {
							fram.toFront();
						}
						@Override
						public void windowLostFocus(WindowEvent e) {}	
					};
					thisFrame.addWindowFocusListener(listen);
					cancl.addActionListener(new ActionListener() {
	
						@Override
						public void actionPerformed(ActionEvent e) {
							thisFrame.removeWindowFocusListener(listen);
							fram.setVisible(false);
						}
					});
					chose.addActionListener(new ActionListener() {
	
						@Override
						public void actionPerformed(ActionEvent e) {
							thisFrame.removeWindowFocusListener(listen);
							String filename = textfield.getText();
							//save the file
							boolean result = false;
							try {
								NotepadMessage message = new NotepadMessage(userName + "/" + filename, ourMap.get(editor.getSelectedComponent()).getText(), 0);
								oos.writeObject(message);
								oos.flush();
								//ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
								result = (boolean) ois.readObject();
								if (result) {
									editor.setTitleAt(editor.getSelectedIndex(), filename);
									isMine.put(editor.getSelectedComponent(), 1);
									menuBar.add(usersMenu);
									menuIsThere = true;
									thisFrame.repaint();
								} else {
									JOptionPane.showMessageDialog(fram, "Save unsuccessful!", "Not Saved!", JOptionPane.ERROR_MESSAGE);
									
								}
							} catch (IOException ioe) {
								JOptionPane.showMessageDialog(fram, "Couldn't reach the server! You are now in offline mode!", "Not Saved!", JOptionPane.ERROR_MESSAGE);
								offline = true;
								label.setText("Offline");
							}
							catch (ClassNotFoundException cnfe) {
								JOptionPane.showMessageDialog(fram, "Couldn't reach the server! You are now in offline mode!", "Not Saved!", JOptionPane.ERROR_MESSAGE);
								offline = true;
								label.setText("Offline");
							} finally {
								thisFrame.removeWindowFocusListener(listen);
								fram.setVisible(false);
							}
						}
					});
				}
			}
		}
		private class EditMenuItemClicked implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				String itemName = e.getActionCommand();
				if (itemName.equals("Select All")) {
					ourMap.get(editor.getSelectedComponent()).selectAll();
				} else if (itemName.equals("Copy")) {
					ourMap.get(editor.getSelectedComponent()).copy();
				} else if (itemName.equals("Paste")) {
					ourMap.get(editor.getSelectedComponent()).paste();
				} else if (itemName.equals("Cut")) {
					ourMap.get(editor.getSelectedComponent()).cut();
				} else if (itemName.startsWith("Undo")) {
					UndoManager undom = undoMap.get(editor.getComponentAt(editor.getSelectedIndex()));
					undom.undo();
					menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
					menuBar.getMenu(1).getItem(0).setText(undom.getUndoPresentationName());
					menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
					menuBar.getMenu(1).getItem(1).setText(undom.getRedoPresentationName());
				} else if (itemName.startsWith("Redo")) {
					UndoManager undom = undoMap.get(editor.getComponentAt(editor.getSelectedIndex()));
					undom.redo();
					menuBar.getMenu(1).getItem(0).setEnabled(undom.canUndo());
					menuBar.getMenu(1).getItem(0).setText(undom.getUndoPresentationName());
					menuBar.getMenu(1).getItem(1).setEnabled(undom.canRedo());
					menuBar.getMenu(1).getItem(1).setText(undom.getRedoPresentationName());
				}
			}
		} //end EditMenuItemClicked
		private class SpellCheckItemClicked implements ActionListener {
			private HashMap<Component, Integer> typemap;
			private File wlfile;
			private File kbfile;
			private WordTrie wordlist;
			private HashMap<Character, ArrayList<Character>> keyboard;
			private JFileChooser fc;
			
			private JPanel panel;
			private JPanel spellcheckpanel;
			private JPanel labels;
			private JLabel spelllabel;
			private JPanel buttons;
			private JButton ignorebutton;
			private JButton addbutton;
			private JPanel fixes;
			private JButton changebutton;
			private JComboBox<String> mybox;
			private JButton exit;
			
			public SpellCheckItemClicked(JTabbedPane cont, HashMap<Component, JTextArea> ourMap) {
				typemap = new HashMap<Component, Integer>();
				wlfile = new File("resources/wordlist.wl");
				kbfile = new File("resources/qwerty-us.kb");
				Parsers myparsers = new Parsers();
				FileReader wlfr = null;
				try {
					wlfr = new FileReader(wlfile);
					wordlist = myparsers.wordListParse(wlfr);
				} catch (FileNotFoundException fnfe) {
					JOptionPane.showMessageDialog(null, "Default wordlist file could not be found! Please select an existing .wl file before doing SpellCheck!", "Default WordList not found!", JOptionPane.ERROR_MESSAGE);
				}
				FileReader kbfr = null;
				try {
					kbfr = new FileReader(kbfile);
					keyboard = myparsers.keyboardParse(kbfr);
				} catch (FileNotFoundException fnfe) {
					JOptionPane.showMessageDialog(null, "Default keyboard file could not be found! Please select an existing .kb file before doing SpellCheck!", "Default Keyboard File not found!", JOptionPane.ERROR_MESSAGE);
				}
				fc = new JFileChooser(System.getProperty("user.dir"));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				String itemName = e.getActionCommand();
				if (itemName.equals("Run")) {
					TextAreaParser taparser = new TextAreaParser(ourMap.get(editor.getComponentAt(editor.getSelectedIndex())));
					boolean check = true;
					while (taparser.ready() && check) {
						check = wordlist.find(taparser.getNext().getWord());
					}
					if (!taparser.ready() && check) {
						JOptionPane.showMessageDialog(null, "There is nothing to be corrected!", "Everything correct!", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					panel = new JPanel(new BorderLayout());
					panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, goldColor6));
					panel.setBackground(goldColor7);
					SpellCheckButtonsClicked scbc = new SpellCheckButtonsClicked(panel, taparser);
					
					Component ourComp = editor.getSelectedComponent();
					if (typemap.containsKey(ourComp) && typemap.get(ourComp)!=0) {
						if (typemap.get(ourComp)==2) {
							ourMap.get(ourComp).getParent().getParent().getParent().remove(1);
						} else {
							return;
						}
					}
					typemap.put(ourComp, 1);
					
					panel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
					panel.setPreferredSize(new Dimension(250, Integer.MAX_VALUE));
					spellcheckpanel = new JPanel();
					spellcheckpanel.setBackground(goldColor7);
					
					labels = new JPanel();
					labels.setBackground(goldColor7);
					labels.setLayout(new BoxLayout(labels, BoxLayout.X_AXIS));
					spelllabel = new JLabel("Spelling: " + taparser.getCurrent().getWord(), JLabel.LEFT);
					spelllabel.setFont(boldfont14);
					spelllabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
					spelllabel.setForeground(goldColor6);
					labels.add(spelllabel);
					
					buttons = new JPanel();
					buttons.setLayout(new GridLayout());
					ignorebutton = new JButton("Ignore") {
						private static final long serialVersionUID = 1365411118095749132L;

						public void paintComponent(Graphics g) {
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/menubar.png"), 0, 0, size.width, size.height, this);
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Ignore", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Ignore")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					ignorebutton.setCursor(clickcursor);
					ignorebutton.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, goldColor7));
					ignorebutton.setMargin(new Insets(0,0,0,0));
					ignorebutton.setBackground(goldColor7);
					addbutton = new JButton("Add") {
						private static final long serialVersionUID = -3064485868394599098L;

						public void paintComponent(Graphics g) {
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/menubar.png"), 0, 0, size.width, size.height, this);
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Add", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Add")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					addbutton.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, goldColor7));
					addbutton.setMargin(new Insets(0,0,0,0));
					addbutton.setBackground(goldColor7);
					addbutton.setForeground(Color.WHITE);
					addbutton.setCursor(clickcursor);
					ignorebutton.addActionListener(scbc);
					addbutton.addActionListener(scbc);
					addbutton.setPreferredSize(new Dimension(addbutton.getPreferredSize().width, 20));
					buttons.add(ignorebutton);
					buttons.add(addbutton);
					fixes = new JPanel();
					fixes.setLayout(new GridLayout());
					mybox = new JComboBox<String>();
					mybox.setUI(new BasicComboBoxUI() {
						protected JButton createArrowButton() {
							JButton button = new JButton(new ImageIcon("resources/arrowDownBut.png")) {
								private static final long serialVersionUID = -9217693127521852153L;

								public void paintComponent(Graphics g) {
									Dimension size = this.getSize();
									g.setColor(goldColor7);
									g.fillRect(0, 0, size.width, size.height);
									g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/arrowDownBut.png"), 0, 0, size.width, size.height, this);
								}
							};
							return button;
						}
					});
					((JButton)((Container)mybox).getComponent(0)).setContentAreaFilled(true);
					((JButton)((Container)mybox).getComponent(0)).setBorder(BorderFactory.createEmptyBorder());
					mybox.setBackground(goldColor0);
					mybox.setForeground(goldColor6);
					mybox.setBorder(BorderFactory.createEmptyBorder());
					mybox.setCursor(clickcursor);
					mybox.setFont(plainfont12);
					changebutton = new JButton("Change") {
						private static final long serialVersionUID = 2846529494853396884L;

						public void paintComponent(Graphics g) {
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/menubar.png"), 0, 0, size.width, size.height, this);
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Change", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Change")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					changebutton.setCursor(clickcursor);
					changebutton.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, goldColor7));
					changebutton.setMargin(new Insets(0,0,0,0));
					changebutton.setBackground(goldColor7);
					changebutton.setPreferredSize(new Dimension(changebutton.getPreferredSize().width, 20));
					MyComparator comp = new MyComparator(taparser.getCurrent().getWord());
					TreeSet<String> suggestions = new TreeSet<String>(comp);
					if (wordlist.findPrefix(taparser.getCurrent().getWord())) {
						ArrayList<String> tempvec = wordlist.allFromPrefix(taparser.getCurrent().getWord());
						for (int j=0; j<tempvec.size(); j++) {
							suggestions.add(tempvec.get(j));
						}
					}
					AutoCorrect ac = new AutoCorrect();
					ArrayList<String> mistakes = ac.possibleMistakes(wordlist, keyboard, taparser.getCurrent().getWord());
					for (int j=0; j<mistakes.size(); j++) {
						ArrayList<String> tempvec = wordlist.allFromPrefix(mistakes.get(j));
						for (int k=0; k<tempvec.size(); k++) {
							suggestions.add(tempvec.get(k));
						}
					}
					if (suggestions.isEmpty()) {
						mybox.addItem("no corrections");
						changebutton.setEnabled(false);
					} else {
						Iterator<String> it = suggestions.iterator();
						int x=0;
						boolean flag=true;
						int lastdiff = 0;
						while (it.hasNext() && flag) {
							String str = it.next();
							int thisdiff = ac.difference(taparser.getCurrent().getWord(), str);
							if (x<10 || thisdiff==lastdiff) {
								mybox.addItem(str);
							} else {
								flag = false;
							}
							lastdiff = thisdiff;
							x++;
						}
					}
					changebutton.addActionListener(scbc);
					fixes.add(mybox);
					fixes.add(changebutton);
					
					exit = new JButton("Close") {
						private static final long serialVersionUID = 2846529494853396884L;

						public void paintComponent(Graphics g) {
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/menubar.png"), 0, 0, size.width, size.height, this);
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Close", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Close")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					exit.setCursor(clickcursor);
					exit.addActionListener(scbc);
					exit.setBorder(null);
					exit.setMargin(new Insets(0,0,0,0));
					exit.setBackground(goldColor7);
					exit.setPreferredSize(new Dimension(exit.getPreferredSize().width, 20));
					spellcheckpanel.setLayout(new BoxLayout(spellcheckpanel, BoxLayout.Y_AXIS));
					spellcheckpanel.add(labels);
					spellcheckpanel.add(Box.createRigidArea(new Dimension(0,5)));
					spellcheckpanel.add(buttons);
					spellcheckpanel.add(Box.createRigidArea(new Dimension(0,5)));
					spellcheckpanel.add(fixes);
					spellcheckpanel.add(Box.createRigidArea(new Dimension(0,5)));
					spellcheckpanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(goldColor6), "Spell Check", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, plainfont12, goldColor6));
					panel.add(spellcheckpanel, BorderLayout.NORTH);
					panel.add(exit, BorderLayout.SOUTH);
					if (ourMap.get(ourComp)==null) {
						System.out.println("damn");
					}
					ourMap.get(ourComp).getParent().getParent().getParent().add(panel, BorderLayout.EAST);
					
					panel.requestFocusInWindow();
					ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionStart(taparser.getCurrent().getBeginning());
					ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionEnd(taparser.getCurrent().getEnd());
					ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
					ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setEditable(false);
					thisFrame.revalidate();
					thisFrame.repaint();
				} else if (itemName.equals("Configure")) {
					Component ourComp = editor.getSelectedComponent();
					if (typemap.containsKey(ourComp) && typemap.get(ourComp)!=0) {
						if (typemap.get(ourComp)==1) {
							ourMap.get(ourComp).getParent().getParent().getParent().remove(1);
						} else {
							return;
						}
					}
					typemap.put(ourComp, 2);
					JPanel panel2 = new JPanel(new BorderLayout());
					panel2.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, goldColor6));
					panel2.setBackground(goldColor7);
					JPanel spellcheckpanel = new JPanel(new GridLayout(5,1));
					spellcheckpanel.setBackground(goldColor7);
					JLabel label1 = new JLabel(wlfile.getName());
					label1.setBackground(goldColor7);
					label1.setFont(boldfont14);
					label1.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
					JButton button1 = new JButton("Change WordList...") {
						private static final long serialVersionUID = 2846529494853396884L;

						public void paintComponent(Graphics g) {
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/menubar.png"), 0, 0, size.width, size.height, this);
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Change WordList...", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Change WordList...")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					button1.setCursor(clickcursor);
					button1.setBorder(null);
					button1.setMargin(new Insets(0,0,0,0));
					button1.setBackground(goldColor7);
					button1.setFont(boldfont14);
					button1.setPreferredSize(new Dimension(button1.getPreferredSize().width, 20));
					spellcheckpanel.add(label1);
					JPanel temppan1 = new JPanel(new GridLayout(1,1));
					JPanel temppan3 = new JPanel();
					temppan3.setBackground(goldColor7);
					temppan1.add(button1);
					spellcheckpanel.add(temppan1);
					spellcheckpanel.add(temppan3);
					button1.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							fc.setAcceptAllFileFilterUsed(false);
							fc.setFileFilter(new FileNameExtensionFilter("wordlist files (*.wl)", "wl"));
							fc.setDialogTitle("Select File...");
							int returnValue = fc.showDialog(null, "Select");
							if (returnValue == JFileChooser.APPROVE_OPTION) {
								File file = fc.getSelectedFile();
								if (!file.getName().endsWith(".wl") || file.getName().length()<4) {
									JOptionPane.showMessageDialog(null, "You input illegal filename!", "Illegal Filename", JOptionPane.ERROR_MESSAGE);
									return;
								}
								FileReader fr = null;
								try {
									fr = new FileReader(file);
									wlfile = file;
									label1.setText(file.getName());
								} catch (FileNotFoundException fnfe) {
									JOptionPane.showMessageDialog(null, "File could not be found!", "File not found!", JOptionPane.ERROR_MESSAGE);
									return;
								} finally {
									if (fr!=null) {
										try {
											fr.close();
										} catch (IOException ioe) {}
									}
								}
							}
						}
					});
					JLabel label2 = new JLabel(kbfile.getName());
					label2.setBackground(goldColor7);
					label2.setFont(boldfont14);
					label2.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
					JButton button2 = new JButton("Change Keyboard...") {
						private static final long serialVersionUID = 2846529494853396884L;

						public void paintComponent(Graphics g) {
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/menubar.png"), 0, 0, size.width, size.height, this);
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Change Keyboard...", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Change Keyboard...")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					button2.setCursor(clickcursor);
					button2.setBorder(null);
					button2.setMargin(new Insets(0,0,0,0));
					button2.setBackground(goldColor7);
					button2.setFont(boldfont14);
					button2.setPreferredSize(new Dimension(panel2.getPreferredSize().width, 20));
					spellcheckpanel.add(label2);
					JPanel temppan2 = new JPanel(new GridLayout(1,1));
					temppan2.add(button2);
					spellcheckpanel.add(temppan2);
					button2.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							fc.setAcceptAllFileFilterUsed(false);
							fc.setFileFilter(new FileNameExtensionFilter("keyboard files (*.kb)", "kb"));
							fc.setDialogTitle("Select File...");
							int returnValue = fc.showDialog(null, "Select");
							if (returnValue == JFileChooser.APPROVE_OPTION) {
								File file = fc.getSelectedFile();
								if (!file.getName().endsWith(".kb") || file.getName().length()<4) {
									JOptionPane.showMessageDialog(null, "You input illegal filename!", "Illegal Filename", JOptionPane.ERROR_MESSAGE);
									return;
								}
								FileReader fr = null;
								try {
									fr = new FileReader(file);
									kbfile = file;
									label2.setText(file.getName());
								} catch (FileNotFoundException fnfe) {
									JOptionPane.showMessageDialog(null, "File could not be found!", "File not found!", JOptionPane.ERROR_MESSAGE);
									return;
								} finally {
									if (fr!=null) {
										try {
											fr.close();
										} catch (IOException ioe) {}
									}
								}
							}
						}
					});
					spellcheckpanel.setBorder(new TitledBorder(new TitledBorder(BorderFactory.createLineBorder(goldColor6), "Configure", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, plainfont12, goldColor6)));
					panel2.add(spellcheckpanel, BorderLayout.NORTH);
					ourMap.get(ourComp).getParent().getParent().getParent().add(panel2, BorderLayout.EAST);
					JButton eexit = new JButton("Close") {
						private static final long serialVersionUID = 2846529494853396884L;

						public void paintComponent(Graphics g) {
							Dimension size = this.getSize();
							if (this.getModel().isRollover()) {
								try {
									g.drawImage(ImageIO.read(new File("resources/menubarRollover.png")), 0, 0, size.width, size.height, this);
								} catch (IOException ioe) {}
							} else {
								g.drawImage(Toolkit.getDefaultToolkit().getImage("resources/menubar.png"), 0, 0, size.width, size.height, this);
							}
							g.setFont(plainfont14);
			                g.setColor(Color.WHITE);
			                g.drawString("Close", size.width/2 - g.getFontMetrics(plainfont14).stringWidth("Close")/2, size.height/2 + g.getFontMetrics(plainfont14).getHeight()/3);
						}
					};
					SpellCheckButtonsClicked scbc = new SpellCheckButtonsClicked(panel2, null);
					eexit.addActionListener(scbc);
					eexit.setCursor(clickcursor);
					eexit.setBorder(null);
					eexit.setMargin(new Insets(0,0,0,0));
					eexit.setBackground(goldColor7);
					eexit.setPreferredSize(new Dimension(eexit.getPreferredSize().width, 20));
					panel2.add(eexit, BorderLayout.SOUTH);
					panel2.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));
					panel2.setPreferredSize(new Dimension(250, Integer.MAX_VALUE));
					thisFrame.revalidate();
					thisFrame.repaint();
					panel2.setSize(250, editor.getHeight());
					thisFrame.revalidate();
					thisFrame.repaint();
				}
			}
			private class SpellCheckButtonsClicked implements ActionListener {
				private JPanel panell;
				private TextAreaParser taparser;
				private int delay;
				
				public SpellCheckButtonsClicked(JPanel pan, TextAreaParser tap) {
					panell = pan;
					taparser = tap;
					delay = 0;
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					String itemName = e.getActionCommand();
					if (itemName.equals("Ignore")) {
						if (!taparser.ready()) {
							panell.getParent().remove(panell);
							JOptionPane.showMessageDialog(null, "Spell check is done!", "SpellCheck is done!", JOptionPane.INFORMATION_MESSAGE);
							typemap.remove(editor.getComponentAt(editor.getSelectedIndex()));
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setEditable(true);
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
							return;
						}
						while (taparser.ready() && wordlist.find(taparser.getNext().getWord())) {}
						if (wordlist.find(taparser.getCurrent().getWord())) {
							panell.getParent().remove(panell);
							JOptionPane.showMessageDialog(null, "Spell check is done!", "SpellCheck is done!", JOptionPane.INFORMATION_MESSAGE);
							typemap.remove(editor.getComponentAt(editor.getSelectedIndex()));
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setEditable(true);
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
							return;
						}
						JLabel label = (JLabel)((JPanel)((JPanel)panel.getComponent(0)).getComponent(0)).getComponent(0);
						label.setText("Spelling: " + taparser.getCurrent().getWord());
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionStart(taparser.getCurrent().getBeginning()+delay);
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionEnd(taparser.getCurrent().getEnd()+delay);
						
						JButton button = changebutton;
						mybox.removeAllItems();
						MyComparator comp = new MyComparator(taparser.getCurrent().getWord());
						TreeSet<String> suggestions = new TreeSet<String>(comp);
						if (wordlist.findPrefix(taparser.getCurrent().getWord())) {
							ArrayList<String> tempvec = wordlist.allFromPrefix(taparser.getCurrent().getWord());
							for (int j=0; j<tempvec.size(); j++) {
								suggestions.add(tempvec.get(j));
							}
						}
						AutoCorrect ac = new AutoCorrect();
						ArrayList<String> mistakes = ac.possibleMistakes(wordlist, keyboard, taparser.getCurrent().getWord());
						for (int j=0; j<mistakes.size(); j++) {
							ArrayList<String> tempvec = wordlist.allFromPrefix(mistakes.get(j));
							for (int k=0; k<tempvec.size(); k++) {
								suggestions.add(tempvec.get(k));
							}
						}
						if (suggestions.isEmpty()) {
							mybox.addItem("no corrections");
							button.setEnabled(false);
						} else {
							Iterator<String> it = suggestions.iterator();
							button.setEnabled(true);
							int x=0;
							boolean flag=true;
							int lastdiff = 0;
							while (it.hasNext() && flag) {
								String str = it.next();
								int thisdiff = ac.difference(taparser.getCurrent().getWord(), str);
								if (x<10 || thisdiff==lastdiff) {
									mybox.addItem(str);
								} else {
									flag = false;
								}
								lastdiff = thisdiff;
								x++;
							}
						}
						
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
					} else if (itemName.equals("Add")) {
						wordlist.add(taparser.getCurrent().getWord());
						if (!taparser.ready()) {
							panell.getParent().remove(panell);
							JOptionPane.showMessageDialog(null, "Spell check is done!", "SpellCheck is done!", JOptionPane.INFORMATION_MESSAGE);
							typemap.remove(editor.getComponentAt(editor.getSelectedIndex()));
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setEditable(true);
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
							return;
						}
						while (taparser.ready() && wordlist.find(taparser.getNext().getWord())) {}
						if (wordlist.find(taparser.getCurrent().getWord())) {
							panell.getParent().remove(panell);
							JOptionPane.showMessageDialog(null, "Spell check is done!", "SpellCheck is done!", JOptionPane.INFORMATION_MESSAGE);
							typemap.remove(editor.getComponentAt(editor.getSelectedIndex()));
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setEditable(true);
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
							return;
						}
						JLabel label = (JLabel)((JPanel)((JPanel)panel.getComponent(0)).getComponent(0)).getComponent(0);
						label.setText("Spelling: " + taparser.getCurrent().getWord());
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionStart(taparser.getCurrent().getBeginning()+delay);
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionEnd(taparser.getCurrent().getEnd()+delay);
						
						JButton button = changebutton;
						mybox.removeAllItems();
						MyComparator comp = new MyComparator(taparser.getCurrent().getWord());
						TreeSet<String> suggestions = new TreeSet<String>(comp);
						if (wordlist.findPrefix(taparser.getCurrent().getWord())) {
							ArrayList<String> tempvec = wordlist.allFromPrefix(taparser.getCurrent().getWord());
							for (int j=0; j<tempvec.size(); j++) {
								suggestions.add(tempvec.get(j));
							}
						}
						AutoCorrect ac = new AutoCorrect();
						ArrayList<String> mistakes = ac.possibleMistakes(wordlist, keyboard, taparser.getCurrent().getWord());
						for (int j=0; j<mistakes.size(); j++) {
							ArrayList<String> tempvec = wordlist.allFromPrefix(mistakes.get(j));
							for (int k=0; k<tempvec.size(); k++) {
								suggestions.add(tempvec.get(k));
							}
						}
						if (suggestions.isEmpty()) {
							mybox.addItem("no corrections");
							button.setEnabled(false);
						} else {
							Iterator<String> it = suggestions.iterator();
							button.setEnabled(true);
							int x=0;
							boolean flag=true;
							int lastdiff = 0;
							while (it.hasNext() && flag) {
								String str = it.next();
								int thisdiff = ac.difference(taparser.getCurrent().getWord(), str);
								if (x<10 || thisdiff==lastdiff) {
									mybox.addItem(str);
								} else {
									flag = false;
								}
								lastdiff = thisdiff;
								x++;
							}
						}
						
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
					} else if (itemName.equals("Close")) {
						panell.getParent().remove(panell);
						typemap.remove(editor.getComponentAt(editor.getSelectedIndex()));
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setEditable(true);
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
					} else if (itemName.equals("Change")) {
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionStart(taparser.getCurrent().getBeginning()+delay);
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionEnd(taparser.getCurrent().getEnd()+delay);
						JButton button = changebutton;
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).replaceSelection((String)mybox.getSelectedItem());
						delay+=((String)mybox.getSelectedItem()).length() - (taparser.getCurrent().getEnd()-taparser.getCurrent().getBeginning());
						if (!taparser.ready()) {
							panell.getParent().remove(panell);
							JOptionPane.showMessageDialog(null, "Spell check is done!", "SpellCheck is done!", JOptionPane.INFORMATION_MESSAGE);
							typemap.remove(editor.getComponentAt(editor.getSelectedIndex()));
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setEditable(true);
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
							return;
						}
						while (taparser.ready() && wordlist.find(taparser.getNext().getWord())) {}
						if (wordlist.find(taparser.getCurrent().getWord())) {
							panell.getParent().remove(panell);
							JOptionPane.showMessageDialog(null, "Spell check is done!", "SpellCheck is done!", JOptionPane.INFORMATION_MESSAGE);
							typemap.remove(editor.getComponentAt(editor.getSelectedIndex()));
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
							ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setEditable(true);
							return;
						}
						JLabel label = (JLabel)((JPanel)((JPanel)panel.getComponent(0)).getComponent(0)).getComponent(0);
						label.setText("Spelling: " + taparser.getCurrent().getWord());
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionStart(taparser.getCurrent().getBeginning()+delay);
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).setSelectionEnd(taparser.getCurrent().getEnd()+delay);
						
						mybox.removeAllItems();
						MyComparator comp = new MyComparator(taparser.getCurrent().getWord());
						TreeSet<String> suggestions = new TreeSet<String>(comp);
						if (wordlist.findPrefix(taparser.getCurrent().getWord())) {
							ArrayList<String> tempvec = wordlist.allFromPrefix(taparser.getCurrent().getWord());
							for (int j=0; j<tempvec.size(); j++) {
								suggestions.add(tempvec.get(j));
							}
						}
						AutoCorrect ac = new AutoCorrect();
						ArrayList<String> mistakes = ac.possibleMistakes(wordlist, keyboard, taparser.getCurrent().getWord());
						for (int j=0; j<mistakes.size(); j++) {
							ArrayList<String> tempvec = wordlist.allFromPrefix(mistakes.get(j));
							for (int k=0; k<tempvec.size(); k++) {
								suggestions.add(tempvec.get(k));
							}
						}
						if (suggestions.isEmpty()) {
							mybox.addItem("no corrections");
							button.setEnabled(false);
						} else {
							Iterator<String> it = suggestions.iterator();
							button.setEnabled(true);
							int x=0;
							boolean flag=true;
							int lastdiff = 0;
							while (it.hasNext() && flag) {
								String str = it.next();
								int thisdiff = ac.difference(taparser.getCurrent().getWord(), str);
								if (x<10 || thisdiff==lastdiff) {
									mybox.addItem(str);
								} else {
									flag = false;
								}
								lastdiff = thisdiff;
								x++;
							}
						}			
						ourMap.get(editor.getComponentAt(editor.getSelectedIndex())).requestFocusInWindow();
					}
				}
			} //ends SpellCheckButtonClicked
		} //ends SpellCheckItemClicked
	} //ends FileMenuItemClicked
} //ends Notepad class