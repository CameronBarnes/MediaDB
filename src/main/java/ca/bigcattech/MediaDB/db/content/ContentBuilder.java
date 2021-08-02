/*
 *     ContentBuilder
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
	
	public ContentBuilder pools(int[] pools) {
		
		mContent.setPools(pools);
		return this;
	}
	
	public Content build() throws Content.ContentValidationException {
		
		if (mContent.getTags() == null) mContent.setTags(new String[]{});
		if (mContent.getPools() == null) mContent.setPools(new int[]{});
		mContent.validate();
		return mContent;
	}
	
}
