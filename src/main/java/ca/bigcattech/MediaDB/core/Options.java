/*
 *     Options
 *     Last Modified: 2021-07-16, 9:57 p.m.
 *     Copyright (C) 2021-07-16, 9:57 p.m.  CameronBarnes
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

import ca.bigcattech.MediaDB.db.content.ContentType;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.ArrayList;

public class Options {
	
	private final SearchOptions mSearchOptions;
	private int mColumns = 5;
	private int mResultsPerPage = 250;
	private int mSlideshowTimer = 5;
	private boolean mIngestAutoTagField = false;
	
	public Options() {
		mSearchOptions = new SearchOptions();
	}
	
	public int getSlideshowTimer() {
		return mSlideshowTimer;
	}
	
	public void setSlideshowTimer(int slideshowTimer) {
		mSlideshowTimer = slideshowTimer;
	}
	
	public int getResultsPerPage() {
		return mResultsPerPage;
	}
	
	public void setResultsPerPage(int perPage) {
		mResultsPerPage = perPage;
	}
	
	public int getColumns() {
		return mColumns;
	}
	
	public void setColumns(int columns) {
		mColumns = columns;
	}
	
	public boolean isIngestAutoTagField() {
		return mIngestAutoTagField;
	}
	
	public void setIngestAutoTagField(boolean ingestAutoTagField) {
		mIngestAutoTagField = ingestAutoTagField;
	}
	
	public SearchOptions getSearchOptions() {
		return mSearchOptions;
	}
	
	public static class SearchOptions {
		
		private static final String KEY_CONTENT_TYPE = "content_type";
		
		private boolean mRestricted = false;
		
		private boolean mFavoritesFirst = false;
		private boolean mReverseOrder = false;
		private SortType mSortType = SortType.HASH;
		
		private boolean mImages = true;
		private boolean mVideos = true;
		private boolean mGIFs = true;
		private boolean mOther = true;
		
		public boolean allContentAllowed() {
			
			return mImages && mVideos && mGIFs && mOther;
		}
		
		public Bson[] getContentTypeFilters() {
			
			ArrayList<Bson> types = new ArrayList<>();
			
			if (mImages) types.add(Filters.eq(KEY_CONTENT_TYPE, ContentType.IMAGE.name()));
			if (mVideos) types.add(Filters.eq(KEY_CONTENT_TYPE, ContentType.VIDEO.name()));
			if (mGIFs) types.add(Filters.eq(KEY_CONTENT_TYPE, ContentType.GIF.name()));
			if (mOther) types.add(Filters.eq(KEY_CONTENT_TYPE, ContentType.CONTENT.name()));
			
			return types.toArray(new Bson[]{});
			
		}
		
		public boolean isRestricted() {
			
			return mRestricted;
		}
		
		public void setRestricted(boolean restricted) {
			
			mRestricted = restricted;
		}
		
		public SortType getSortType() {
			
			return mSortType;
		}
		
		public void setSortType(SortType sortType) {
			
			mSortType = sortType;
		}
		
		public boolean isImages() {
			
			return mImages;
		}
		
		public void setImages(boolean images) {
			
			mImages = images;
		}
		
		public boolean isVideos() {
			
			return mVideos;
		}
		
		public void setVideos(boolean videos) {
			
			mVideos = videos;
		}
		
		public boolean isGIFs() {
			
			return mGIFs;
		}
		
		public void setGIFs(boolean GIFs) {
			
			mGIFs = GIFs;
		}
		
		public boolean isOther() {
			
			return mOther;
		}
		
		public void setOther(boolean other) {
			
			mOther = other;
		}
		
		public void setFavoritesFirst(boolean favoritesFirst) {
			
			mFavoritesFirst = favoritesFirst;
		}
		
		public boolean isFavoritesFirst() {
			
			return mFavoritesFirst;
		}
		
		public void setReverseOrder(boolean reverseOrder) {
			
			mReverseOrder = reverseOrder;
		}
		
		public boolean isReverseOrder() {
			
			return mReverseOrder;
		}
		
		public void setSearchType(SortType sortType) {
			
			mSortType = sortType;
		}
		
		public SortType getSearchType() {
			
			return mSortType;
		}
		
		public enum SortType {
			HASH,
			TITLE,
			VIEWS,
			CONTENT_TYPE,
			VIEW_TIME
		}
		
	}
	
}
