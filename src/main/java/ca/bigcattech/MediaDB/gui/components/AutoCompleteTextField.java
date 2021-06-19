package ca.bigcattech.MediaDB.gui.components;

import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.db.TagNameComparator;
import ca.bigcattech.MediaDB.utils.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Locale;

public class AutoCompleteTextField extends JTextField implements KeyListener, DocumentListener {
	
	private ArrayList<String> mDictionary;
	private int mCurrentGuess;
	private Color mIncompleteColour;
	private boolean mAreGuessing;
	private boolean mCaseSensitive;
	private boolean mCheckSeparatedWords;
	
	private DBHandler mDBHandler;
	
	public AutoCompleteTextField() {
		
		mDictionary = new ArrayList<>();
		mCurrentGuess = -1;
		mIncompleteColour = Color.BLUE.brighter();
		mAreGuessing = false;
		mCaseSensitive = false;
		mCheckSeparatedWords = true;
		
		this.addKeyListener(this);
		this.getDocument().addDocumentListener(this);
		this.setFocusTraversalKeysEnabled(false);
		
	}
	
	public AutoCompleteTextField(boolean caseSensitive, boolean checkSeparatedWords) {
		
		mDictionary = new ArrayList<>();
		mCurrentGuess = -1;
		mIncompleteColour = Color.BLUE.brighter();
		mAreGuessing = false;
		mCaseSensitive = caseSensitive;
		mCheckSeparatedWords = checkSeparatedWords;
		
		this.addKeyListener(this);
		this.getDocument().addDocumentListener(this);
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		String input = getSeparatedText();
		
		if (mAreGuessing && (input != null || input.equals(""))) {
			
			if (mCheckSeparatedWords) {
				
				Character lastChar = Utils.getLastChar(input);
				if (lastChar == null) return;
				if (lastChar.equals(' ')) {
					mAreGuessing = false;
					mCurrentGuess = -1;
					return;
				}
				
			}
			
			String guess = getCurrentGuess();
			String drawGuess = guess;
			
			if (!mCaseSensitive) {
				guess = guess.toLowerCase(Locale.ROOT);
			}
			
			if (!guess.startsWith(input)) {
				mAreGuessing = false;
				mCurrentGuess = -1;
				return;
			}
			
			String subGuess = drawGuess.substring(input.length());
			
			Rectangle2D subGuessBounds = g.getFontMetrics().getStringBounds(drawGuess, g);
			Rectangle2D wholeInputBounds = g.getFontMetrics().getStringBounds(this.getText(), g);
			
			int centeredY = (getHeight() / 2) + (int) (subGuessBounds.getHeight() / 2);
			
			g.setColor(mIncompleteColour);
			g.drawString(subGuess, (int) wholeInputBounds.getWidth() + 2, centeredY - 2);
			
		}
		
	}
	
	public void setDBHandler(DBHandler dbHandler) {
		//mDBHandler = dbHandler; //TODO using this to disable the alternate guess method
	}
	
	private void findCurrentGuess() {
		
		if (mDictionary.isEmpty()) return;
		String input = getSeparatedText();
		if (!mCaseSensitive) input = input.toLowerCase(Locale.ROOT);
		
		for (int i = 0; i < mDictionary.size(); i++) {
			
			mCurrentGuess = -1;
			String possibility = mDictionary.get(i);
			if (!mCaseSensitive) possibility = possibility.toLowerCase(Locale.ROOT);
			if (!possibility.equals(input) && possibility.startsWith(input)) {
				mCurrentGuess = i;
				break;
			}
			
		}
		
	}
	
	private void alternateGuess() {
		
		if (mDBHandler == null) {
			findCurrentGuess();
			return;
		}
		
		if (mDictionary.isEmpty()) return;
		String input = getSeparatedText();
		if (!mCaseSensitive) input = input.toLowerCase(Locale.ROOT);
		
		ArrayList<String> valid = new ArrayList<>();
		
		for (String possibility: mDictionary) {
			if (!mCaseSensitive) possibility = possibility.toLowerCase(Locale.ROOT);
			if (!possibility.equals(input) && possibility.startsWith(input)) valid.add(possibility);
		}
		
		if (valid.isEmpty()) return;
		valid.sort(new TagNameComparator(mDBHandler));
		
		mDictionary.indexOf(valid.get(0));
		
	}
	
	private String getSeparatedText() {
		String text = mCaseSensitive ? this.getText() : this.getText().toLowerCase();
		if (mCheckSeparatedWords) return Utils.getLastSubstring(text, " ");
		return this.getText();
	}
	
	public void setDictionary(ArrayList<String> dictionary) {
		mDictionary = dictionary;
		//Collections.sort(mDictionary);
	}
	
	public Color getIncompleteColour() {
		
		return mIncompleteColour;
	}
	
	public void setIncompleteColour(Color colour) {
		
		mIncompleteColour = colour;
	}
	
	public boolean isCaseSensitive() {
		return mCaseSensitive;
	}
	
	public void setCaseSensitive(boolean caseSensitive) {
		mCaseSensitive = caseSensitive;
	}
	
	public boolean isCheckSeparatedWords() {
		
		return mCheckSeparatedWords;
	}
	
	public void setCheckSeparatedWords(boolean separateWords) {
		
		mCheckSeparatedWords = separateWords;
	}
	
	public String getCurrentGuess() {
		
		if (mCurrentGuess == -1 || mDictionary.isEmpty()) return this.getText();
		return mDictionary.get(mCurrentGuess);
		
	}
	
	public String getReplacementText() {
		
		if (!mCheckSeparatedWords) return getCurrentGuess();
		String raw = this.getText();
		int index = raw.lastIndexOf(' ');
		if (index == -1) return getCurrentGuess();
		return raw.substring(0, index + 1) + getCurrentGuess();
		
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		
		if (e.getKeyCode() == KeyEvent.VK_TAB) {
			
			if (mAreGuessing) {
				this.setText(getReplacementText());
				mAreGuessing = true;
				alternateGuess();
			}
			e.consume();
			
		}
		
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		
		String temp = getSeparatedText();
		Character lastChar = Utils.getLastChar(temp);
		if (lastChar == null) return;
		if (mCheckSeparatedWords && lastChar.equals(' ')) {
			this.mAreGuessing = false;
			mCurrentGuess = -1;
		}
		
		if (temp.length() == 1) mAreGuessing = true;
		if (mAreGuessing) alternateGuess();
		
	}
	
	@Override
	public void removeUpdate(DocumentEvent e) {
		
		mAreGuessing = true;
		String temp = getSeparatedText();
		Character lastChar = Utils.getLastChar(temp);
		
		if (lastChar == null) {
			mAreGuessing = false;
			return;
		}
		if (mCheckSeparatedWords && lastChar.equals(' ')) {
			this.mAreGuessing = false;
			mCurrentGuess = -1;
		}
		
		if (mAreGuessing) alternateGuess();
		
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
		if (e.getKeyCode() == KeyEvent.VK_TAB) e.consume();
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		
		if (e.getKeyCode() == KeyEvent.VK_TAB) e.consume();
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
	
	}
	
	@Override
	public void setText(String text) {
		
		super.setText(text);
		mCurrentGuess = -1;
		this.mAreGuessing = false;
	}
	
}
