package furtek_CSCI201L_Assignment5;

public class TextAreaItem {
	private String mystring;
	private int begin;
	private int end;
	
	public TextAreaItem(String str, int b, int e) {
		mystring = str;
		begin = b;
		end = e;
	}
	public void setWord(String str) {
		mystring = str;
	}
	public String getWord() {
		return mystring;
	}
	public void setBeginning(int b) {
		begin = b;
	}
	public int getBeginning() {
		return begin;
	}
	public void setEnd(int e) {
		end = e;
	}
	public int getEnd() {
		return end;
	}
}
