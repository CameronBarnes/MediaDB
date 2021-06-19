package ca.bigcattech.MediaDB.db.content;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.core.Ingest;
import ca.bigcattech.MediaDB.core.Options;
import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.db.Tag;
import ca.bigcattech.MediaDB.image.SimilarityFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Content {
	
	public final ContentType mType;
	//Important properties
	private String mHash = null;
	private File mFile;
	private String[] mTags;
	private SimilarityFinder.ImageSignature mSignature = null;
	
	private long mVideoLength = 0;
	
	private boolean mIsPrivate = false; //manual private flag
	private boolean mIsRestricted = false; //automatic private flag
	
	// Quality of life features
	private String mTitle;
	private String mDescription;
	private int mViews = 0;
	private long mTimeSpent = 0;
	
	private boolean mFavorite = false;
	
	Content(ContentType type) {
		mType = type;
	}
	
	public static ContentBuilder builder(ContentType type) {
		return new ContentBuilder(type);
	}
	
	public File getFile() {
		return mFile;
	}
	
	public void setFile(File file) {
		mFile = file;
	}
	
	public String getHash() {
		return mHash;
	}
	
	public void setHash(String hash) {
		mHash = hash;
	}
	
	public ContentType getType() {
		return mType;
	}
	
	public SimilarityFinder.ImageSignature getSignature() {
		return mSignature;
	}
	
	public void setSignature(SimilarityFinder.ImageSignature signature) {
		mSignature = signature;
	}
	
	public boolean isRestricted() {
		return mIsRestricted;
	}
	
	void setRestricted(boolean restricted) {
		mIsRestricted = restricted;
	}
	
	public boolean isPrivate() {
		return mIsPrivate;
	}
	
	public void setPrivate(boolean isPrivate) {
		mIsPrivate = isPrivate;
		if (mIsPrivate) mIsRestricted = true;
	}
	
	public boolean isFavorite() {
		return mFavorite;
	}
	
	public void setFavorite(boolean favorite) {
		mFavorite = favorite;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String[] getTags() {
		return mTags;
	}
	
	public void setTags(String[] tags) {
		mTags = tags;
	}
	
	public void addTags(String[] tags) {
		
		List<String> list = new ArrayList<>(Arrays.asList(mTags));
		for (String tag : tags) {
			if (tag.equals("") || list.contains(tag)) continue;
			list.add(tag);
		}
		mTags = list.toArray(new String[0]);
		
	}
	
	public void removeTag(String tag) {
		removeTag(new String[]{tag});
	}
	
	public void removeTag(String[] tags) {
		
		List<String> list = new ArrayList<>(Arrays.asList(mTags));
		for (String tag : tags) {
			if (tag.equals("")) continue;
			list.remove(tag);
		}
		mTags = list.toArray(new String[0]);
		
	}
	
	public String getDescription() {
		return mDescription;
	}
	
	public void setDescription(String description) {
		mDescription = description;
	}
	
	public int getViews() {
		return mViews;
	}
	
	void setViews(int views) {
		mViews = views;
	}
	
	public int incrementViews() {
		return ++mViews;
	}
	
	public long getVideoLength() {
		return mVideoLength;
	}
	
	public void setVideoLength(long videoLength) {
		mVideoLength = videoLength;
	}
	
	public long getTimeSpent() {
		return mTimeSpent;
	}
	
	public void setTimeSpent(long timeSpent) {
		mTimeSpent = timeSpent;
	}
	
	public void incrementTimeSpent(long timeSpent) {
		mTimeSpent += timeSpent;
	}
	
	public FormattingError isValid() {
		
		removeInvalidTags();
		
		if (mHash == null || mHash.isEmpty() || mHash.equals("null")) return FormattingError.NULL_HASH;
		if (mFile == null) return FormattingError.NULL_PATH;
		if (!mFile.exists()) return FormattingError.FILE_NOT_EXIST;
		if (!mFile.isFile()) return FormattingError.PATH_NOT_FILE;
		if (FileSystemHandler.getContentTypeOfFile(mFile) != mType) return FormattingError.CONTENT_TYPE_MISMATCH;
		
		return FormattingError.VALID;
		
	}
	
	public void validate() throws ContentValidationException {
		
		FormattingError formattingError = this.isValid();
		if (formattingError != FormattingError.VALID) throw new ContentValidationException(formattingError);
		
	}
	
	public String getThumbnailFile() {
		
		if (mType == ContentType.GIF) return mFile.getPath();
		if (mType == ContentType.IMAGE) return FileSystemHandler.CONTENT_THUMBNAIL_DIR.toString() + '\\' + mHash + Ingest.SUFFIX_THUMBNAIL + '.' + FileSystemHandler.getExtension(mFile);
		
		return "";
		
	}
	
	public void update(DBHandler dbHandler) {
		
		//Update tags
		addParentTag(dbHandler, mTags);
		
		removeInvalidTags();
		
		//If manually set to private, it's restricted
		if (mIsPrivate) {
			mIsRestricted = true;
			return;
		}
		
		if (dbHandler.isTagRestricted(mTags)) {
			mIsRestricted = true;
			//If I add anything after here I need to add a return
		}
		
	}
	
	public synchronized void removeInvalidTags() {
		
		List<String> list = new ArrayList<>(Arrays.asList(mTags));
		list.removeIf(e -> e.equals("") || e.equals(" "));
		mTags = list.toArray(new String[0]);
		
	}
	
	private void addParentTag(DBHandler dbHandler, String[] tags) {
		
		for (String name : tags) {
			
			Tag tag = dbHandler.getTagFromName(name);
			if (tag == null) continue;
			
			addTags(tag.getParentTags());
			addParentTag(dbHandler, tag.getParentTags());
			
		}
		
	}
	
	public int compareTo(Options.SearchOptions searchOptions, Content o) {
		
		int out = 0;
		
		if (searchOptions.getSearchType() == Options.SearchOptions.SearchType.TITLE) {
			out = this.getTitle().compareTo(o.getTitle());
		}
		else if (searchOptions.getSearchType() == Options.SearchOptions.SearchType.VIEWS) {
			out = Integer.compare(o.getViews(), this.getViews());
		}
		else if (searchOptions.getSearchType() == Options.SearchOptions.SearchType.CONTENT_TYPE) {
			if (this.mType == ContentType.VIDEO && o.mType != ContentType.VIDEO) out = -1;
			else if (this.mType != ContentType.VIDEO && o.mType == ContentType.VIDEO) out = 1;
			else if (this.mType == ContentType.IMAGE && o.mType != ContentType.IMAGE) out = -1;
			else if (this.mType != ContentType.IMAGE && o.mType == ContentType.IMAGE) out = 1;
			else if (this.mType != ContentType.GIF && o.mType == ContentType.GIF) out = 1;
			else if (this.mType == ContentType.GIF && o.mType != ContentType.GIF) out = -1;
		}
		else if (searchOptions.getSearchType() == Options.SearchOptions.SearchType.VIEW_TIME) {
			out = Long.compare(o.mTimeSpent, this.mTimeSpent);
		}
		
		return out == 0 ? this.getHash().compareTo(o.getHash()) : out;
		
	}
	
	public enum FormattingError {
		NULL_HASH,
		NULL_PATH,
		FILE_NOT_EXIST,
		PATH_NOT_FILE,
		CONTENT_TYPE_MISMATCH,
		VALID
	}
	
	public static class ContentValidationException extends Exception {
		public ContentValidationException(FormattingError error) {
			super(error.name());
		}
	}
	
}