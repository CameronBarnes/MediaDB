/*
 *     Session
 *     Last Modified: 2023-09-16, 3:13 p.m.
 *     Copyright (C) 2023-09-16, 3:13 p.m.  CameronBarnes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.bigcattech.MediaDB.core;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.content.ContentComparator;
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.db.pool.Pool;
import ca.bigcattech.MediaDB.db.pool.PoolBuilder;
import ca.bigcattech.MediaDB.db.tag.Tag;
import ca.bigcattech.MediaDB.db.tag.TagNameComparator;
import ca.bigcattech.MediaDB.gui.forms.DisplayContentForm;
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
	private volatile boolean mSorting = false;
	private SessionState mSessionState;
	private Content[] mContentResults;
	private Content mContent;
	private Pool[] mPoolResults;
	private Pool mPool;
	private boolean mContentFromPool = false;
	private String[] mSearchTags;
	private String[] mSearchTagsBlacklist;
	private int mIngestTempNum = 0;
	private int mScrollBarTempNum = 0;
	private int mNumResultPages = 0;
	private int mResultPage = 0;
	
	private final Options mOptions;
	
	private int mVolume = 100;
	
	private IKeyListener mKeyListener = null;
	private boolean mControl = false;
	
	private DisplayContentForm mTempContentForm = null;
	private final Timer mSlideshowTimer;
	private boolean mIsSlideshow = false;
	
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
		
		mSlideshowTimer = new Timer(options.getSlideshowTimer() * 1000, e -> {
			if (mTempContentForm != null && mContent.getType() != ContentType.VIDEO) {
				boolean cont = mTempContentForm.next(true);
				if (!cont) stopSlideShow();
			}
		});
		mSlideshowTimer.setRepeats(true);
		
		mSessionState = SessionState.HOME;
		
		log.info("Done!");
		
	}
	
	public void startSlideShow() {
		
		if (mSessionState == SessionState.CONTENT_DISPLAY && mSlideshowTimer != null) {
			mIsSlideshow = true;
			mSlideshowTimer.start();
		}
	}
	
	public void stopSlideShow() {
		
		mIsSlideshow = false;
		if (mSlideshowTimer != null) {
			mSlideshowTimer.stop();
		}
	}
	
	public void resetSlideShowTimer() {
		
		stopSlideShow();
		startSlideShow();
	}
	
	public void updateSlideShowTimer() {
		
		mSlideshowTimer.setDelay(mOptions.getSlideshowTimer() * 1000);
	}
	
	public boolean isSlideshow() {
		
		return mIsSlideshow;
	}
	
	public Options getOptions() {
		
		return mOptions;
	}
	
	public void updateDictionary() {
		
		synchronized (mDictionary) {
			mDBHandler.getAllTags().stream().map(Tag::getName).filter(tag -> !mDictionary.contains(tag)).forEach(mDictionary::add);
		}
		
		sortDictionary();
		
	}
	
	public void displaySession() {
		
		if (mSessionState != SessionState.CONTENT_DISPLAY) {
			mTempContentForm = null;
			if (mSlideshowTimer != null) mSlideshowTimer.stop();
		}
		
		switch (mSessionState) {
			case HOME -> home();
			case SEARCH_RESULTS -> results();
			case CONTENT_DISPLAY -> content();
			case INGEST -> ingest();
			case TAGS -> tags();
		}
		
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public void searchPools(String string) {
		
		String[] tmp = string.toLowerCase(Locale.ROOT).split("(\\s*\\|.*\\|\\s*)");
		if (tmp.length == 1)
			searchPools(tmp[0].split(" "), new String[]{});
		else
			searchPools(tmp[0].split(" "), tmp[1].split(" "));
	}
	
	public void searchPools(String[] tags, String[] bannedTags) {
		
		mNumResultPages = 0;
		mResultPage = 0;
		
		log.info("Search pools");
		
		if (tags.length == 1 && tags[0].equals("")) tags = new String[0];
		
		mSearchTags = tags;
		mSearchTagsBlacklist = bannedTags;
		
		ConcurrentLinkedQueue<Pool> results = new ConcurrentLinkedQueue<>();
		
		long start = System.currentTimeMillis();
		Pool[] unfiltered = mDBHandler.searchForPoolsByTags(mOptions.getSearchOptions().isRestricted(), tags);
		log.info((System.currentTimeMillis() - start) + "ms to get Pools from the database");
		
		start = System.currentTimeMillis();
		Arrays.stream(unfiltered).parallel().forEach(pool -> {
			if (checkPoolWithBlackList(pool)) results.add(pool);
		});
		log.info((System.currentTimeMillis() - start) + "ms to remove blacklisted pools");
		
		mPoolResults = results.toArray(new Pool[]{});
		
		//TODO sort the results
		
		if (mPoolResults.length > 0) {
			mSessionState = SessionState.SEARCH_RESULTS;
			displaySession();
		}
		
	}
	
	public void searchContent(String string) {
		
		String[] tmp = string.toLowerCase(Locale.ROOT).split("(\\s*\\|.*\\|\\s*)");
		if (tmp.length == 1)
			searchContent(tmp[0].split(" "), new String[]{});
		else
			searchContent(tmp[0].split(" "), tmp[1].split(" "));
	}
	
	public void searchContent(String[] tags, String[] bannedTags) {
		
		mNumResultPages = 0;
		mResultPage = 0;
		
		log.info("Search content");
		
		if (tags.length == 1 && tags[0].equals("")) tags = new String[0];
		
		mSearchTags = tags;
		mSearchTagsBlacklist = bannedTags;
		
		ConcurrentLinkedQueue<Content> results = new ConcurrentLinkedQueue<>();
		
		long start = System.currentTimeMillis();
		Content[] unfiltered = mDBHandler.searchForContentByTags(mOptions.getSearchOptions(), mSearchTags);
		log.info((System.currentTimeMillis() - start) + "ms to get content from the database");
		
		start = System.currentTimeMillis();
		Arrays.stream(unfiltered).parallel().forEach(content -> {
			if (checkContentWithBlacklist(content)) results.add(content);
		});
		log.info((System.currentTimeMillis() - start) + "ms to remove blacklisted content");
		
		mContentResults = results.toArray(new Content[]{});
		
		//Hash only sort
		/*start = System.currentTimeMillis();
		Arrays.sort(mResults, Comparator.comparing(Content::getHash));
		log.info((System.currentTimeMillis() - start) + "ms to sort content");*/
		
		//Dynamic search options
		start = System.currentTimeMillis();
		ContentComparator comparator = new ContentComparator(mOptions.getSearchOptions());
		Arrays.sort(mContentResults, comparator);
		log.info((System.currentTimeMillis() - start) + "ms to sort content");
		
		if (mContentResults.length > 0) {
			
			mSessionState = SessionState.SEARCH_RESULTS;
			displaySession();
			
		}
		
	}
	
	private boolean checkContentWithBlacklist(Content content) {
		return Utils.stringArrNotContainsStrFromArray(content.getTags(), mSearchTagsBlacklist);
	}
	
	private boolean checkPoolWithBlackList(Pool pool) {
		return Utils.stringArrNotContainsStrFromArray(pool.getAllTags(), mSearchTagsBlacklist);
	}
	
	public void home() {
		
		mIngestTempNum = 0;
		mScrollBarTempNum = 0;
		mNumResultPages = 0;
		mResultPage = 0;
		mContentFromPool = false;
		
		mKeyListener = null;
		
		log.info("Home");
		
		mSessionState = SessionState.HOME;
		mContentResults = new Content[]{};
		mSearchTags = new String[]{};
		mSearchTagsBlacklist = new String[]{};
		
		mMainFrame.displayHome();
		mMainFrame.setBackgroundColor(Color.GRAY);
		
		System.gc();
		
	}
	
	public void results() {
		
		log.info("Search results");
		
		mKeyListener = null;
		mContentFromPool = false;
		
		int resultsLength = mContentResults.length > 0 ? mContentResults.length : mPoolResults.length;
		
		if (mResultPage == 0 && mOptions.getResultsPerPage() != 0) {
			mNumResultPages = resultsLength / mOptions.getResultsPerPage();
			if (resultsLength % mOptions.getResultsPerPage() > 0) mNumResultPages++;
		}
		long start = System.currentTimeMillis();
		
		mKeyListener = mMainFrame.displaySearchResults();
		
		mSessionState = SessionState.SEARCH_RESULTS;
		mMainFrame.setBackgroundColor(Color.GRAY);
		log.info((System.currentTimeMillis() - start) + "ms to to display results");
		
	}
	
	public void content() {

		//log.info("Display Content"); We dont need this right now, leave for debugging but it's just spam most of the time.
		
		mKeyListener = null;
		
		mTempContentForm = mMainFrame.displayContent(mContent);
		mKeyListener = mTempContentForm;
		mSessionState = SessionState.CONTENT_DISPLAY;
		mMainFrame.setBackgroundColor(Color.GRAY);
		
	}
	
	public void content(Content content) {
		
		//log.info("Display Content");
		
		mContent = content;
		content();
		
	}
	
	public void pool(Pool pool) {
		
		mPool = pool;
		pool();
		
	}
	
	public void pool() {
		
		mKeyListener = null;
		
		//TODO do stuff here
		
	}
	
	public void newPoolWithContent(String poolName, Content content) {
		
		Pool pool = PoolBuilder.newPool(mDBHandler);
		pool.setTitle(poolName);
		pool.setContentHashes(new String[]{content.getHash()});
		mDBHandler.exportPool(pool);
		
	}
	
	public void setKeyListener(IKeyListener listener) {
		
		mKeyListener = listener;
	}
	
	public void ingest() {
		
		mScrollBarTempNum = 0;
		mNumResultPages = 0;
		mResultPage = 0;
		mContentFromPool = false;
		
		log.info("Ingest");
		
		mKeyListener = null;
		
		boolean update = mSessionState == SessionState.INGEST || mSessionState == SessionState.INGEST_TASK;
		mSessionState = SessionState.INGEST;
		mContentResults = new Content[]{};
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
		mContentResults = new Content[]{};
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
		mContentFromPool = false;
		
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
	
	public void setContentFromPool(boolean contentFromPool) {
		
		mContentFromPool = contentFromPool;
	}
	
	public boolean isContentFromPool() {
		
		return mContentFromPool;
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
	
	private synchronized void sortDictionary() {
		
		if (mSorting) return;
		
		new Thread(() -> {
			try {
				mSorting = true;
				mDictionary.sort(new TagNameComparator(mDBHandler, mDictionary.size()).reversed());
				mSorting = false;
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}).start();
		
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
	
	public Content[] getContentResults() {
		
		return mContentResults;
	}
	
	public Pool[] getPoolResults() {
		
		return mPoolResults;
	}
	
	public Pool getPool() {
		
		return mPool;
	}
	
	public Content getContent() {
		
		return mContent;
	}
	
	public DBHandler getDBHandler() {
		
		return mDBHandler;
	}

	public void exit() {
		if (mKeyListener != null)
			mKeyListener.exit();
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
