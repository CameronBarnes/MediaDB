/*
 *     Content
 *     Last Modified: 9/20/23, 5:19 PM
 *     Copyright (C) 9/20/23, 5:19 PM  CameronBarnes
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

package ca.bigcattech.MediaDB.core.data.content;

import ca.bigcattech.MediaDB.core.data.signature.Signature;

import java.io.File;
import java.util.Optional;

public class Content {
	
	// Core Properties
	public final ContentType mType;
	
	private final int mID;
	private String mHash;
	private File mFile;
	private short[] mTags;
	private Optional<Signature> mSignatureOptional;
	
	private int[] mPools;
	
	// Private content features
	private final boolean mIsPrivate = false; // Manual Private Flag
	private final boolean mIsRestricted = false; // Automatic Private Flag
	
	// Quality of life features
	private Optional<String> mTitle;
	private Optional<String> mDescription;
	private final int mViews = 0;
	private final long mViewTime = 0;
	
	Content(ContentType type, int ID) {
		
		mType = type;
		mID = ID;
	}
	
}
