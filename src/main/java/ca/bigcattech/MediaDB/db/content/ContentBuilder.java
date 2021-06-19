package ca.bigcattech.MediaDB.db.content;

import ca.bigcattech.MediaDB.image.SimilarityFinder;

import java.io.File;
import java.util.Arrays;

public class ContentBuilder {
	
	private final Content mContent;
	
	public ContentBuilder(ContentType type) {
		
		mContent = new Content(type);
	}
	
	public ContentBuilder hash(String hash) {
		
		mContent.setHash(hash);
		return this;
	}
	
	public ContentBuilder signature(SimilarityFinder.ImageSignature signature) {
		
		mContent.setSignature(signature);
		return this;
	}
	
	public ContentBuilder file(File file) {
		
		mContent.setFile(file);
		return this;
	}
	
	public ContentBuilder tags(String[] tags) {
		
		mContent.setTags(tags);
		return this;
	}
	
	public ContentBuilder addTag(String tag) {
		
		String[] temp = Arrays.copyOf(mContent.getTags(), mContent.getTags().length + 1);
		temp[temp.length - 1] = tag;
		mContent.setTags(temp);
		return this;
	}
	
	public ContentBuilder restricted() {
		
		mContent.setRestricted(true);
		return this;
	}
	
	public ContentBuilder restricted(boolean bool) {
		
		mContent.setRestricted(bool);
		return this;
	}
	
	public ContentBuilder setPrivate() {
		
		mContent.setPrivate(true);
		return this;
	}
	
	public ContentBuilder setPrivate(boolean isPrivate) {
		
		mContent.setPrivate(isPrivate);
		return this;
	}
	
	public ContentBuilder setFavorite() {
		mContent.setFavorite(true);
		return this;
	}
	
	public ContentBuilder setFavorite(boolean favorite) {
		mContent.setFavorite(favorite);
		return this;
	}
	
	public ContentBuilder title(String title) {
		
		mContent.setTitle(title);
		return this;
	}
	
	public ContentBuilder description(String description) {
		
		mContent.setDescription(description);
		return this;
	}
	
	public ContentBuilder views(int views) {
		
		mContent.setViews(views);
		return this;
	}
	
	public ContentBuilder videoLength(long length) {
		mContent.setVideoLength(length);
		return this;
	}
	
	public ContentBuilder timeSpent(long length) {
		mContent.setTimeSpent(length);
		return this;
	}
	
	public Content build() throws Content.ContentValidationException {
		
		if (mContent.getTags() == null) mContent.setTags(new String[]{});
		mContent.validate();
		return mContent;
	}
	
}
