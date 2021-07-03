/*
 *     Tag
 *     Last Modified: 2021-06-28, 11:42 p.m.
 *     Copyright (C) 2021-07-03, 2:22 a.m.  CameronBarnes
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
