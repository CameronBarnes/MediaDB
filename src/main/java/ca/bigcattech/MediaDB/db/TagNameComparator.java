package ca.bigcattech.MediaDB.db;

import java.util.Comparator;

public class TagNameComparator implements Comparator<String> {
	
	private final DBHandler mDBHandler;
	
	public TagNameComparator(DBHandler dbHandler) {
		mDBHandler = dbHandler;
	}
	
	//NOTE: Lower number is closer to the top of the list, which means it'll come up first in the dictionary search
	@Override
	public int compare(String a, String b) {
		if (a.equals(b)) return 0;
		if ((a.isEmpty() || b.isEmpty()) || a.charAt(0) != b.charAt(0)) return a.compareTo(b);
		else if (a.startsWith(b)) return 1;
		else if (b.startsWith(a)) return -1;
		return Long.compare(mDBHandler.countContentWithTag(b), mDBHandler.countContentWithTag(a));
	}
	
}
