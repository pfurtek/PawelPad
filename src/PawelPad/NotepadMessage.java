package furtek_CSCI201L_Assignment5;

import java.io.Serializable;

public class NotepadMessage implements Serializable {
	private static final long serialVersionUID = -7398386139787044172L;
	private String type;
	private String first;
	private int second;
	private String third;
	
	public NotepadMessage(String type, String first, int second) {
		this(type, first, second, "");
	}
	
	public NotepadMessage(String type, String first, int second, String third) {
		this.type = type;
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}
	
	public String getThird() {
		return third;
	}
	
	public void setThird(String third) {
		this.third = third;
	}

}
