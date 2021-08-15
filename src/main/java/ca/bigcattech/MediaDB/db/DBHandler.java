/*
 *     DBHandler
 *     Last Modified: 2021-08-04, 2:26 p.m.
 *     Copyright (C) 2021-08-14, 5:57 p.m.  CameronBarnes
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

import ca.bigcattech.MediaDB.core.Options;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.pool.Pool;
import ca.bigcattech.MediaDB.db.tag.Tag;
import ca.bigcattech.MediaDB.image.ImageSignature;

import java.util.List;

public interface DBHandler {
	
	boolean poolExists(int uid);
	
	void initDB();
	
	void migrate();
	
	/**
	 * Check if a hash value already exists in the database's content collection
	 *
	 * @param hash the hash value to check for in the database's content collection
	 * @return if the hash already exists, true for yes
	 */
	boolean checkHash(String hash);
	
	void exportContent(Content content) throws Content.ContentValidationException;
	
	void deleteContent(String hash);
	
	long getNumContent();
	
	Pool[] getPoolFromUID(int[] uids);
	
	Pool getPoolFromUID(int uid);
	
	void exportPool(Pool pool);
	
	Content[] getContentFromHash(String[] hashes);
	
	Content getContentFromHash(String hash);
	
	Pool[] searchForPoolsByTags(boolean restricted, String[] tags);
	
	Content[] searchForContentByTags(Options.SearchOptions searchOptions, String[] tags);
	
	List<ImageSignature> getAllSignatures();
	
	void exportSignature(ImageSignature signature);
	
	ImageSignature getSignatureFromHash(String hash);
	
	boolean checkSignatureExists(String hash);
	
	void updateAllContentWithTags(String[] tags);
	
	void updateAllWithTags(Tag[] tags);
	
	void updateAllWithTags(String[] tags);
	
	void updateAllPoolsWithTags(String[] tags);
	
	void updateAllContent();
	
	void updateAllPools();
	
	void updateAll();
	
	void removeTagFromAllContent(String tag);
	
	void removeTagFromAll(String tag);
	
	void removeTagFromAll(String[] tags);
	
	long getNumTags();
	
	void removeTagFromAllContent(String[] tags);
	
	boolean isTagRestricted(String[] tags);
	
	boolean deleteTag(String tag);
	
	List<Tag> getAllTagsWithParent(String name);
	
	List<Tag> getAllTags();
	
	void exportTag(Tag tag, boolean manualCount);
	
	void decrementTagUses(String[] tags);
	
	void incrementTagUses(String[] tags);
	
	void addTag(String tag);
	
	void addTag(String[] tags);
	
	Tag getTagFromName(String name);
	
	void updateAllTags();
	
	void updateAllTags(Tag[] tags);
	
}
