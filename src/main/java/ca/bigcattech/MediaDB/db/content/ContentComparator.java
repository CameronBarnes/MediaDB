package ca.bigcattech.MediaDB.db.content;

import ca.bigcattech.MediaDB.core.Options;

import java.util.Comparator;

public class ContentComparator implements Comparator<Content> {
	
	private final Options.SearchOptions mSearchOptions;
	
	public ContentComparator(Options.SearchOptions options) {
		mSearchOptions = options;
	}
	
	@Override
	public int compare(Content o1, Content o2) {
		if (mSearchOptions.isFavoritesFirst()) {
			if (!o1.isFavorite() && o2.isFavorite()) return 1;
			else if (o1.isFavorite() && !o2.isFavorite()) return -1;
		}
		
		int ranking = o1.compareTo(mSearchOptions, o2);
		if (mSearchOptions.isReverseOrder()) ranking *= -1;
		return ranking;
		
	}
	
}
