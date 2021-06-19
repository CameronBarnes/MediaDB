package ca.bigcattech.MediaDB.core;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.db.TagNameComparator;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.content.ContentComparator;
import ca.bigcattech.MediaDB.gui.frames.MainFrame;
import ca.bigcattech.MediaDB.gui.interfaces.IKeyListener;
import ca.bigcattech.MediaDB.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Session {
	
	private static final Logger log = LoggerFactory.getLogger(Session.class);
	private final MainFrame mMainFrame;
	private final DBHandler mDBHandler;
	private final Ingest mIngest;
	private final ArrayList<String> mDictionary = new ArrayList<>();
	private SessionState mSessionState;
	private Content[] mResults;
	private Content mContent;
	private String[] mSearchTags;
	private String[] mSearchTagsBlacklist;
	private boolean mAllowRestricted = false;
	private int mIngestTempNum = 0;
	private int mScrollBarTempNum = 0;
	private int mNumResultPages = 0;
	private int mResultPage = 0;
	
	private final Options mOptions;
	
	private int mVolume = 100;
	
	private IKeyListener mKeyListener = null;
	private volatile boolean mControl = false;
	
	public Session(MainFrame frame, DBHandler dbHandler, Ingest ingest, Options options) {
		
		log.info("Setting up session");
		
		//Allow for read from disk
		mOptions = options;
		
		mMainFrame = frame;
		mMainFrame.setSession(this);
		
		mDBHandler = dbHandler;
		
		mIngest = ingest;
		mIngest.setSession(this);
		
		updateDictionary();
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (mKeyListener != null) {
				switch (e.getID()) {
					case KeyEvent.KEY_TYPED -> mKeyListener.keyTyped(e, mControl);
					case KeyEvent.KEY_PRESSED -> {
						if (e.getKeyCode() == KeyEvent.VK_CONTROL) mControl = true;
						mKeyListener.keyPressed(e, mControl);
					}
					case KeyEvent.KEY_RELEASED -> {
						if (e.getKeyCode() == KeyEvent.VK_CONTROL) mControl = false;
						mKeyListener.keyReleased(e, mControl);
					}
				}
			}
			return false;
		});
		
		mSessionState = SessionState.HOME;
		
		log.info("Done!");
		
	}
	
	public Options getOptions() {
		return mOptions;
	}
	
	public void updateDictionary() {
		
		synchronized (mDictionary) {
			mDBHandler.getAllTags().forEach(tag -> mDictionary.add(tag.getName()));
		}
		
		sortDictionary();
		
	}
	
	public void displaySession() {
		
		switch (mSessionState) {
			case HOME -> home();
			case SEARCH_RESULTS -> results();
			case CONTENT_DISPLAY -> content();
			case INGEST -> ingest();
			case TAGS -> tags();
		}
		
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public void search(String string) {
		String[] tmp = string.toLowerCase(Locale.ROOT).split("(\\s*\\|.*\\|\\s*)");
		if (tmp.length == 1)
			search(mAllowRestricted, tmp[0].split(" "), new String[]{});
		else
			search(mAllowRestricted, tmp[0].split(" "), tmp[1].split(" "));
	}
	
	@Deprecated
	public void search(String[] tags) {
		search(mAllowRestricted, tags, new String[]{});
	}
	
	public void search(String[] tags, String[] bannedTags) {
		search(mAllowRestricted, tags, bannedTags);
	}
	
	public void search(boolean allowRestricted, String[] tags) {
		search(allowRestricted, tags, new String[]{});
	}
	
	public void search(boolean allowRestricted, String[] tags, String[] bannedTags) {
		
		mNumResultPages = 0;
		mResultPage = 0;
		
		log.info("Search");
		
		if (tags.length == 1 && tags[0].equals("")) tags = new String[0];
		
		mAllowRestricted = allowRestricted;
		
		mSearchTags = tags;
		mSearchTagsBlacklist = bannedTags;
		
		ConcurrentLinkedQueue<Content> results = new ConcurrentLinkedQueue<>();
		
		long start = System.currentTimeMillis();
		Content[] unfiltered = mDBHandler.searchForContentByTags(mAllowRestricted, mSearchTags);
		log.info((System.currentTimeMillis() - start) + "ms to get content from the database");
		
		start = System.currentTimeMillis();
		Arrays.stream(unfiltered).parallel().forEach(content -> {
			if (checkContentWithBlacklist(content)) results.add(content);
		});
		log.info((System.currentTimeMillis() - start) + "ms to remove blacklisted content");
		
		mResults = results.toArray(new Content[]{});
		
		//Hash only sort
		/*start = System.currentTimeMillis();
		Arrays.sort(mResults, Comparator.comparing(Content::getHash));
		log.info((System.currentTimeMillis() - start) + "ms to sort content");*/
		
		//Dynamic search options
		start = System.currentTimeMillis();
		ContentComparator comparator = new ContentComparator(mOptions.getSearchOptions());
		Arrays.sort(mResults, comparator);
		log.info((System.currentTimeMillis() - start) + "ms to sort content");
		
		if (mResults.length > 0) {
			
			mSessionState = SessionState.SEARCH_RESULTS;
			displaySession();
			
		}
		
	}
	
	private boolean checkContentWithBlacklist(Content content) {
		return !Utils.stringArrContainsStrFromArray(content.getTags(), mSearchTagsBlacklist);
	}
	
	public void home() {
		
		mIngestTempNum = 0;
		mScrollBarTempNum = 0;
		mAllowRestricted = false;
		mNumResultPages = 0;
		mResultPage = 0;
		
		mKeyListener = null;
		
		log.info("Home");
		
		mSessionState = SessionState.HOME;
		mResults = new Content[]{};
		mSearchTags = new String[]{};
		mSearchTagsBlacklist = new String[]{};
		
		mMainFrame.displayHome();
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public void results() {
		
		log.info("Search results");
		
		mKeyListener = null;
		
		if (mResultPage == 0 && mOptions.getResultsPerPage() != 0) {
			mNumResultPages = mResults.length / mOptions.getResultsPerPage();
			if (mResults.length % mOptions.getResultsPerPage() > 0) mNumResultPages++;
		}
		long start = System.currentTimeMillis();
		
		mKeyListener = mMainFrame.displayContentResults();
		
		mSessionState = SessionState.SEARCH_RESULTS;
		mMainFrame.setBackgroundColor(Color.GRAY);
		log.info((System.currentTimeMillis() - start) + "ms to to display results");
		
	}
	
	public void content() {
		
		log.info("Display Content");
		
		mKeyListener = null;
		
		mKeyListener = mMainFrame.displayContent(mContent);
		mSessionState = SessionState.CONTENT_DISPLAY;
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public void content(Content content) {
		
		//log.info("Display Content");
		
		mContent = content;
		content();
		
	}
	
	public void setKeyListener(IKeyListener listener) {
		mKeyListener = listener;
	}
	
	public void ingest() {
		
		mScrollBarTempNum = 0;
		mNumResultPages = 0;
		mResultPage = 0;
		
		log.info("Ingest");
		
		mKeyListener = null;
		
		boolean update = mSessionState == SessionState.INGEST || mSessionState == SessionState.INGEST_TASK;
		mSessionState = SessionState.INGEST;
		mResults = new Content[]{};
		mSearchTags = new String[]{};
		mSearchTagsBlacklist = new String[]{};
		if (!update) {
			
			mIngest.ingestContent();
			mIngestTempNum = mIngest.getNumIngestTasks();
			
		}
		mMainFrame.displayIngest(update);
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public void ingest(File directory) {
		
		mScrollBarTempNum = 0;
		
		log.info("Ingest");
		
		mKeyListener = null;
		
		if (directory.equals(FileSystemHandler.CONTENT_DIR)) {
			JOptionPane.showMessageDialog(mMainFrame, "You cant import from the content folder, defaulting to the ingest folder.", "Error", JOptionPane.ERROR_MESSAGE);
			directory = FileSystemHandler.INGEST_DIR;
		}
		
		boolean update = mSessionState == SessionState.INGEST || mSessionState == SessionState.INGEST_TASK;
		mSessionState = SessionState.INGEST;
		mResults = new Content[]{};
		mSearchTags = new String[]{};
		mSearchTagsBlacklist = new String[]{};
		if (!update) {
			
			mIngest.ingestContent(directory);
			mIngestTempNum = mIngest.getNumIngestTasks();
			
		}
		log.info("Number of new ingest tasks: " + mIngestTempNum);
		mMainFrame.displayIngest(update);
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public void displayIngestTask(Ingest.IngestTask task) {
		
		log.info("Display ingest task");
		
		mKeyListener = null;
		
		mSessionState = SessionState.INGEST_TASK;
		mMainFrame.displayIngestTask(task);
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public void ingestCancel() {
		mIngest.stop();
	}
	
	public void tags() {
		
		mKeyListener = null;
		
		mIngestTempNum = 0;
		mScrollBarTempNum = 0;
		mNumResultPages = 0;
		mResultPage = 0;
		
		log.info("Tags");
		
		mMainFrame.displayTagManagement();
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public int getIngestTempNum() {
		return mIngestTempNum;
	}
	
	public int getScrollBarTempNum() {
		return mScrollBarTempNum;
	}
	
	public void setScrollBarTempNum(int num) {
		mScrollBarTempNum = num;
	}
	
	public int getNumResultPages() {
		return mNumResultPages;
	}
	
	public int getResultPage() {
		return mResultPage;
	}
	
	public void setResultPage(int page) {
		if (page < 0) mResultPage = 0;
		else if (page > mNumResultPages - 1) mResultPage = mNumResultPages - 1;
		else mResultPage = page;
	}
	
	public Ingest getIngest() {
		return mIngest;
	}
	
	public void back() {
		
		log.info("Back");
		
		mKeyListener = null;
		
		switch (mSessionState) {
			
			case HOME, INGEST_TASK, TAGS -> {
				//Nothing, this wont ever happen
			}
			case SEARCH_RESULTS, INGEST -> {
				mSessionState = SessionState.HOME;
			}
			case CONTENT_DISPLAY -> {
				mSessionState = SessionState.SEARCH_RESULTS;
			}
			
		}
		
		displaySession();
		
	}
	
	public void revalidateAndRepaintFrame() {
		mMainFrame.revalidate();
		mMainFrame.repaint();
	}
	
	public ArrayList<String> getDictionary() {
		synchronized (mDictionary) {
			return new ArrayList<>(mDictionary);
		}
	}
	
	private void sortDictionary() {
		synchronized (mDictionary) {
			new Thread(() -> mDictionary.sort(new TagNameComparator(mDBHandler))).start();
		}
	}
	
	public void addTagToDictionary(String tag) {
		addTagToDictionary(new String[]{tag});
	}
	
	public void addTagToDictionary(String[] tags) {
		
		boolean updated = false;
		
		synchronized (mDictionary) {
			
			for (String tag : tags) {
				if (!mDictionary.contains(tag)) {
					updated = true;
					mDictionary.add(tag);
				}
			}
			
		}
		
		if (updated) sortDictionary();
		
	}
	
	public void setVolume(int volume) {
		if (volume > 100) mVolume = 100;
		else mVolume = Math.max(volume, 0);
	}
	
	public int getVolume() {
		return mVolume;
	}
	
	public void removeTagFromDictionary(String tag) {
		synchronized (mDictionary) {
			mDictionary.remove(tag);
		}
	}
	
	public String[] getSearchTags() {
		return mSearchTags;
	}
	
	public String[] getSearchTagsBlacklist() {
		return mSearchTagsBlacklist;
	}
	
	public SessionState getSessionState() {
		return mSessionState;
	}
	
	public Content[] getResults() {
		return mResults;
	}
	
	public Content getContent() {
		return mContent;
	}
	
	public DBHandler getDBHandler() {
		return mDBHandler;
	}
	
	public enum SessionState {
		HOME,
		SEARCH_RESULTS,
		CONTENT_DISPLAY,
		INGEST,
		INGEST_TASK,
		TAGS
	}
	
}
