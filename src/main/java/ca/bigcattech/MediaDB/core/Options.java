/*
 *     Options
 *     Last Modified: 2021-06-18, 7:28 p.m.
 *     Copyright (C) 2021-06-18, 7:28 p.m.  CameronBarnes
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

public class Options {
	
	private final SearchOptions mSearchOptions;
	private int mColumns = 5;
	private int mResultsPerPage = 250;
	private boolean mIngestAutoTagField = false;
	
	public Options() {
		
		mSearchOptions = new SearchOptions();
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
		
		private boolean mFavoritesFirst = false;
		private boolean mReverseOrder = false;
		private SearchType mSearchType = SearchType.HASH;
		
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
		
		public void setSearchType(SearchType searchType) {
			
			mSearchType = searchType;
		}
		
		public SearchType getSearchType() {
			
			return mSearchType;
		}
		
		public enum SearchType {
			HASH,
			TITLE,
			VIEWS,
			CONTENT_TYPE,
			VIEW_TIME
		}
		
	}
	
}
