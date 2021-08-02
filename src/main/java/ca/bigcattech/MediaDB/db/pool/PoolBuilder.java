/*
 *     PoolBuilder
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

import java.security.SecureRandom;

public class PoolBuilder {
	
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	
	private final Pool mPool;
	
	public PoolBuilder(int uid) {
		
		mPool = new Pool(uid);
	}
	
	private static int getNewUID(DBHandler dbHandler) {
		
		int uid = SECURE_RANDOM.nextInt();
		
		int tries = 0;
		while (dbHandler.poolExists(uid)) {
			uid = SECURE_RANDOM.nextInt();
			if (tries > 100) throw new RuntimeException("Too many tries to generate UID");
			tries++;
		}
		
		return uid;
		
	}
	
	public static Pool newPool(DBHandler dbHandler) {
		
		return new Pool(getNewUID(dbHandler));
	}
	
	public PoolBuilder title(String title) {
		
		mPool.setTitle(title);
		return this;
	}
	
	public PoolBuilder description(String description) {
		
		mPool.setDescription(description);
		return this;
	}
	
	public PoolBuilder isPrivate(boolean isPrivate) {
		
		mPool.setPrivate(isPrivate);
		return this;
	}
	
	public PoolBuilder isRestricted(boolean isRestricted) {
		
		mPool.setRestricted(isRestricted);
		return this;
	}
	
	public PoolBuilder isFavorite(boolean isFavorite) {
		
		mPool.setFavorite(isFavorite);
		return this;
	}
	
	public PoolBuilder contentHashes(String[] contentHashes) {
		
		mPool.setContentHashes(contentHashes);
		return this;
	}
	
	public PoolBuilder allTags(String[] allTags) {
		
		mPool.setAllTags(allTags);
		return this;
	}
	
	public PoolBuilder thumbnailFile(String thumbnailFile) {
		
		mPool.setThumbnailFile(thumbnailFile);
		return this;
	}
	
	public Pool build() {
		
		return mPool;
	}
	
}
