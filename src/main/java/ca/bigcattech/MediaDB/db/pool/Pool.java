/*
 *     Pool
 *     Last Modified: 2021-08-02, 6:46 a.m.
 *     Copyright (C) 2021-08-02, 6:46 a.m.  CameronBarnes
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

package ca.bigcattech.MediaDB.db.pool;

import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.db.content.Content;

import java.util.ArrayList;
import java.util.Arrays;

public class Pool {
	
	private final int mUID;
	private String mTitle;
	private String mDescription;
	private String[] mContentHashes;
	private String[] mAllTags;
	private boolean mIsPrivate = false;
	private boolean mIsRestricted = false;
	private String mThumbnailFile;
	private boolean mIsFavorite = false;
	
	Pool(int uid) {
		
		mUID = uid;
	}
	
	public void update(DBHandler dbHandler) {
		
		//Handling tags
		ArrayList<String> tags = new ArrayList<>();
		
		Content[] contentArr = dbHandler.getContentFromHash(mContentHashes);
		
		for (Content content : contentArr) {
			for (String tag : content.getTags()) {
				if (!tags.contains(tag)) tags.add(tag);
			}
			
			if (Arrays.stream(content.getPools()).noneMatch(i -> i == mUID)) {
				content.addPool(mUID);
				try {
					dbHandler.exportContent(content);
				}
				catch (Content.ContentValidationException ignored) {
				}
			}
			
		}
		
		mAllTags = tags.toArray(new String[]{});
		
		//Handling content restriction
		//If manually set to private, it's restricted, otherwise, if any of the tags are restricted, it's restricted
		//I check if any content objects are restricted instead of checking if any of mAllTags are restricted because the content objects should already be updated
		//I can change this later to use dbHandler.isTagRestricted(mAllTags) if I'd prefer to do it that way
		mIsRestricted = mIsPrivate || Arrays.stream(contentArr).anyMatch(Content::isRestricted);
		
		//Handling thumbnail
		if (mContentHashes.length > 0) mThumbnailFile = contentArr[0].getThumbnailFile();
		
	}
	
	public static PoolBuilder builder(int uid) {
		
		return new PoolBuilder(uid);
	}
	
	public void setFavorite(boolean favorite) {
		
		mIsFavorite = favorite;
	}
	
	public boolean isFavorite() {
		
		return mIsFavorite;
	}
	
	void setThumbnailFile(String thumbnailFile) {
		
		mThumbnailFile = thumbnailFile;
	}
	
	public String getThumbnailFile() {
		
		return mThumbnailFile;
	}
	
	void setAllTags(String[] allTags) {
		
		mAllTags = allTags;
	}
	
	public String[] getAllTags() {
		
		return mAllTags;
	}
	
	public void setPrivate(boolean isPrivate) {
		
		mIsPrivate = isPrivate;
	}
	
	public boolean isPrivate() {
		
		return mIsPrivate;
	}
	
	public void setRestricted(boolean isRestricted) {
		
		mIsRestricted = isRestricted;
	}
	
	public boolean isRestricted() {
		
		return mIsRestricted;
	}
	
	public void setContentHashes(String[] contentHashes) {
		
		mContentHashes = contentHashes;
	}
	
	public String[] getContentHashes() {
		
		return mContentHashes;
	}
	
	public void addContentHash(String contentHash) {
		
		ArrayList<String> hashes = new ArrayList<>(Arrays.asList(mContentHashes));
		if (!hashes.contains(contentHash)) hashes.add(contentHash);
		mContentHashes = hashes.toArray(new String[0]);
		
	}
	
	public void addContentHash(Content content) {
		
		addContentHash(content.getHash());
		content.addPool(mUID);
		
	}
	
	public void setTitle(String title) {
		
		mTitle = title;
	}
	
	public String getTitle() {
		
		return mTitle.equals("") || mTitle.equals("None") ? String.valueOf(mUID) : mTitle;
	}
	
	public void setDescription(String description) {
		
		mDescription = description;
	}
	
	public String getDescription() {
		
		return mDescription;
	}
	
	public int getUID() {
		
		return mUID;
	}
	
}
