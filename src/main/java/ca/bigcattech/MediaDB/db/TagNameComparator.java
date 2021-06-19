/*
 *     TagNameComparator
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
