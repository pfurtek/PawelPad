package furtek_CSCI201L_Assignment5;

import java.util.ArrayList;

import javax.swing.JTextArea;

public class TextAreaParser {
	private ArrayList<TextAreaItem> words;
	private int currIndex = -1;
	
	public TextAreaParser(JTextArea ta) {
		words = new ArrayList<TextAreaItem>();
		String fulltext = ta.getText().toLowerCase();
		String currWord = "";
		int start = -1;
		int finish = 0;
		for (int i=0; i<fulltext.length(); i++) {
			if (fulltext.charAt(i)>='a' && fulltext.charAt(i)<='z') {
				currWord = currWord + fulltext.charAt(i);
				if (start==-1) {
					start=i;
				}
				finish = i+1;
			} else if (fulltext.charAt(i)==' ' || fulltext.charAt(i)==9 || fulltext.charAt(i)=='\n' || fulltext.charAt(i)=='\0') {
				if (!currWord.equals("")) {
					words.add(new TextAreaItem(currWord, start, finish));
					start = -1;
					currWord = "";
				}
			}
		}
		if (!currWord.equals("")) {
			words.add(new TextAreaItem(currWord, start, fulltext.length()));
		}
	}
	public TextAreaItem getCurrent() {
		if (currIndex==-1) {
			currIndex = 0;
		}
		return words.get(currIndex);
	}
	public TextAreaItem getNext() {
		currIndex++;
		return words.get(currIndex);
	}
	public boolean ready() {
		if (words.size()>currIndex+1) {
			return true;
		}
		return false;
	}
}
