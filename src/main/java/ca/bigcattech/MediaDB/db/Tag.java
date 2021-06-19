package ca.bigcattech.MediaDB.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tag {
	
	private final String mName;
	private TagType mTagType = TagType.TAG;
	private String[] mParentTags;
	private boolean mRestricted = false;
	
	public Tag(String name) {
		
		mName = name;
		mParentTags = new String[]{};
	}
	
	public Tag(String name, String[] parentTags) {
		
		mName = name;
		mParentTags = parentTags;
	}
	
	public Tag(String name, String[] parentTags, boolean restricted) {
		
		mName = name;
		mParentTags = parentTags;
		mRestricted = restricted;
	}
	
	public Tag(String name, TagType type, String[] parentTags, boolean restricted) {
		
		mName = name;
		mTagType = type;
		mParentTags = parentTags;
		mRestricted = restricted;
	}
	
	public Tag(String name, boolean restricted) {
		
		mName = name;
		mRestricted = restricted;
	}
	
	public String getName() {
		
		return mName;
	}
	
	public TagType getTagType() {
		return mTagType;
	}
	
	public void setTagType(TagType type) {
		mTagType = type;
	}
	
	public String[] getParentTags() {
		
		return mParentTags;
	}
	
	public void setParentTags(String[] parentTags) {
		
		mParentTags = parentTags;
	}
	
	public void addParentTag(String tag) {
		
		addParentTags(new String[]{tag});
	}
	
	public void addParentTags(String[] tags) {
		
		List<String> list = new ArrayList<>(Arrays.asList(mParentTags));
		for (String tag : tags) {
			
			if (list.contains(tag)) continue;
			list.add(tag);
			
		}
		
		mParentTags = list.toArray(new String[]{});
		
	}
	
	public void removeParentTag(String tag) {
		
		removeParentTags(new String[]{tag});
	}
	
	public void removeParentTags(String[] tags) {
		
		List<String> list = new ArrayList<>(Arrays.asList(mParentTags));
		
		for (String tag : tags) {
			list.remove(tag);
		}
		
		mParentTags = list.toArray(new String[]{});
		
	}
	
	public boolean isRestricted() {
		
		return mRestricted;
	}
	
	public void setRestricted(boolean restricted) {
		
		mRestricted = restricted;
	}
	
	@Override
	public String toString() {
		
		return mName;
	}
	
	public int compareToName(Tag tag) {
		
		return mName.compareToIgnoreCase(tag.getName());
	}
	
	public int compareToName(String tag) {
		
		return mName.compareToIgnoreCase(tag);
	}
	
	public enum TagType {
		TAG,
		PERSON,
		CHARACTER,
		AUTHOR
	}
	
}
