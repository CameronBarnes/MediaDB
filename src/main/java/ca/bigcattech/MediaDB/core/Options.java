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
