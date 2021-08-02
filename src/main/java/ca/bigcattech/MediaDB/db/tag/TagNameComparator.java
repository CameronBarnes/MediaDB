/*
 *     TagNameComparator
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

package ca.bigcattech.MediaDB.db.tag;

import ca.bigcattech.MediaDB.db.DBHandler;

import java.util.Comparator;
import java.util.HashMap;

public class TagNameComparator implements Comparator<String> {
	
	private final DBHandler mDBHandler;
	
	private final HashMap<String, Integer> mTagMap;
	
	public TagNameComparator(DBHandler dbHandler, int size) {
		
		mDBHandler = dbHandler;
		mTagMap = new HashMap<>(size);
	}
	
	
	@Override
	public int compare(String o1, String o2) {
		
		if (o1.equals(o2)) return 0;
		else if (o1.startsWith(o2)) return -1;
		else if (o2.startsWith(o1)) return 1;
		mTagMap.putIfAbsent(o1, mDBHandler.getTagFromName(o1).getNumUses());
		mTagMap.putIfAbsent(o2, mDBHandler.getTagFromName(o2).getNumUses());
		int out = Integer.compare(mTagMap.get(o1), mTagMap.get(o2));
		return out == 0 ? o1.compareTo(o2) : out;
	}
	
}
