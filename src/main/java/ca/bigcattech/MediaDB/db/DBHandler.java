/*
 *     DBHandler
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

import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.image.SimilarityFinder;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DBHandler {
	
	private static final String COLLECTION_CONTENT = "content";
	private static final String COLLECTION_TAGS = "tags";
	
	private static final String KEY_CONTENT_HASH = "content_hash";
	private static final String KEY_CONTENT_FILE = "content_file";
	private static final String KEY_CONTENT_TAGS = "content_tags";
	private static final String KEY_CONTENT_TYPE = "content_type";
	private static final String KEY_CONTENT_RESTRICTED = "content_restricted";
	private static final String KEY_CONTENT_PRIVATE = "content_private";
	private static final String KEY_CONTENT_FAVORITE = "content_favorite";
	private static final String KEY_CONTENT_TITLE = "content_title";
	private static final String KEY_CONTENT_DESCRIPTION = "content_description";
	private static final String KEY_CONTENT_VIEWS = "content_views";
	private static final String KEY_CONTENT_SIGNATURE = "content_signature";
	private static final String KEY_CONTENT_VIDEO_LENGTH = "content_video_length";
	private static final String KEY_CONTENT_TIME_SPENT = "content_time_spent";
	
	private static final String KEY_TAGS_NAME = "tags_name";
	private static final String KEY_TAGS_TYPE = "tags_type";
	private static final String KEY_TAGS_PARENTS = "tags_parents";
	private static final String KEY_TAGS_RESTRICTED = "tags_restricted";
	
	private static final Logger log = LoggerFactory.getLogger(DBHandler.class);
	
	private final MongoClient mMongoClient;
	private final MongoDatabase mDatabase;
	
	public DBHandler(String database, String address) {
		
		log.info("Starting DBHandler");
		
		mMongoClient = MongoClients.create(
				MongoClientSettings.builder()
								   .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(address))))
								   .build());
		
		mDatabase = mMongoClient.getDatabase(database);
		
		log.info("Done!");
		
	}
	
	//======================================INITIALIZATION====================================================
	
	private static Document ensureContentDocumentValid(Document document) {
		
		document.putIfAbsent(KEY_CONTENT_HASH, "null");
		document.putIfAbsent(KEY_CONTENT_FILE, "null");
		document.putIfAbsent(KEY_CONTENT_TYPE, "null");
		document.putIfAbsent(KEY_CONTENT_TAGS, new String[]{});
		document.putIfAbsent(KEY_CONTENT_TITLE, "None");
		document.putIfAbsent(KEY_CONTENT_DESCRIPTION, "None");
		document.putIfAbsent(KEY_CONTENT_VIEWS, 0);
		document.putIfAbsent(KEY_CONTENT_RESTRICTED, false);
		document.putIfAbsent(KEY_CONTENT_PRIVATE, false);
		document.putIfAbsent(KEY_CONTENT_FAVORITE, false);
		document.putIfAbsent(KEY_CONTENT_VIDEO_LENGTH, 0L);
		document.putIfAbsent(KEY_CONTENT_TIME_SPENT, 0L);
		
		return document;
		
	}
	
	private static Document updateAndValidateDocFromContent(Document document, Content content) {
		
		ensureContentDocumentValid(document);
		
		document.replace(KEY_CONTENT_HASH, content.getHash());
		document.replace(KEY_CONTENT_FILE, content.getFile().toString());
		document.replace(KEY_CONTENT_TYPE, content.mType.name());
		document.replace(KEY_CONTENT_TAGS, new ArrayList<>(Arrays.asList(content.getTags())));
		document.replace(KEY_CONTENT_TITLE, content.getTitle());
		document.replace(KEY_CONTENT_DESCRIPTION, content.getDescription());
		document.replace(KEY_CONTENT_VIEWS, content.getViews());
		document.replace(KEY_CONTENT_RESTRICTED, content.isRestricted());
		document.replace(KEY_CONTENT_PRIVATE, content.isPrivate());
		document.replace(KEY_CONTENT_FAVORITE, content.isFavorite());
		document.replace(KEY_CONTENT_VIDEO_LENGTH, content.getVideoLength());
		document.replace(KEY_CONTENT_TIME_SPENT, content.getTimeSpent());
		if (content.getSignature() != null) {
			if (document.containsKey(KEY_CONTENT_SIGNATURE))
				document.replace(KEY_CONTENT_SIGNATURE, createImageSignatureDocument(content.getSignature()));
			else document.put(KEY_CONTENT_SIGNATURE, createImageSignatureDocument(content.getSignature()));
		}
		
		return document;
		
	}
	
	private static Content loadContentFromDocument(Document document) {
		
		if (document == null) return null;
		
		ensureContentDocumentValid(document);
		
		try {
			return Content.builder(ContentType.valueOf(document.getString(KEY_CONTENT_TYPE)))
						  .hash(document.getString(KEY_CONTENT_HASH))
						  .file(new File(document.getString(KEY_CONTENT_FILE)))
						  .tags(document.getList(KEY_CONTENT_TAGS, String.class).toArray(new String[0]))
						  .restricted(document.getBoolean(KEY_CONTENT_RESTRICTED))
						  .setPrivate(document.getBoolean(KEY_CONTENT_PRIVATE))
						  .setFavorite(document.getBoolean(KEY_CONTENT_FAVORITE))
						  .title(document.getString(KEY_CONTENT_TITLE))
						  .description(document.getString(KEY_CONTENT_DESCRIPTION))
						  .views(document.getInteger(KEY_CONTENT_VIEWS))
						  .signature(loadImageSignatureFromDocument(document))
						  .videoLength(document.getLong(KEY_CONTENT_VIDEO_LENGTH))
						  .timeSpent(document.getLong(KEY_CONTENT_TIME_SPENT))
						  .build();
			
		}
		catch (Content.ContentValidationException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	//======================================CONTENT HANDLING====================================================
	
	//++++++++++++++++++++++++++++++++++++++Exporting to database+++++++++++++++++++++++++++++++++++++++++++++++
	
	private static Document createImageSignatureDocument(SimilarityFinder.ImageSignature signature) {
		
		Document document = new Document();
		
		List<List<String>> data = signature.convertDataStructure();
		
		document.put("0", data.get(0));
		document.put("1", data.get(1));
		document.put("2", data.get(2));
		document.put("3", data.get(3));
		document.put("4", data.get(4));
		
		return document;
		
	}
	
	private static SimilarityFinder.ImageSignature loadImageSignatureFromDocument(Document document) {
		
		if (!document.containsKey(KEY_CONTENT_SIGNATURE)) return null;
		
		List<List<String>> data = new ArrayList<>();
		
		Document signatureDoc = (Document) document.get(KEY_CONTENT_SIGNATURE);
		
		data.add(signatureDoc.getList("0", String.class));
		data.add(signatureDoc.getList("1", String.class));
		data.add(signatureDoc.getList("2", String.class));
		data.add(signatureDoc.getList("3", String.class));
		data.add(signatureDoc.getList("4", String.class));
		
		return new SimilarityFinder.ImageSignature(data);
		
	}
	
	private static Tag loadTagFromDocument(Document document) {
		
		ensureTagDocumentValid(document);
		
		return new Tag(
				document.getString(KEY_TAGS_NAME),
				Tag.TagType.valueOf(document.getString(KEY_TAGS_TYPE)),
				document.getList(KEY_TAGS_PARENTS, String.class).toArray(new String[]{}),
				document.getBoolean(KEY_TAGS_RESTRICTED));
		
	}
	
	private static Document ensureTagDocumentValid(Document document) {
		
		document.putIfAbsent(KEY_TAGS_NAME, "null");
		document.putIfAbsent(KEY_TAGS_TYPE, Tag.TagType.TAG.name());
		document.putIfAbsent(KEY_TAGS_PARENTS, new String[]{});
		document.putIfAbsent(KEY_TAGS_RESTRICTED, false);
		return document;
		
	}
	
	//++++++++++++++++++++Getting content from the database++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public void initDB() {
		
		log.info("Init DBHandler");
		
		createCollections();
		createIndexes();
		
		log.info("DBHandler init finished");
		
	}
	
	private void createCollections() {
		
		log.info("Creating collections");
		
		ArrayList<String> collections = new ArrayList<>();
		mDatabase.listCollectionNames().into(collections);
		
		boolean created = false;
		
		if (!collections.contains(COLLECTION_CONTENT)) {
			log.info("Creating content collection");
			mDatabase.createCollection(COLLECTION_CONTENT);
			created = true;
		}
		if (!collections.contains(COLLECTION_TAGS)) {
			log.info("Creating tags collection");
			mDatabase.createCollection(COLLECTION_TAGS);
			created = true;
		}
		
		if (!created) log.info("Collections already exist");
		
	}
	
	private void createIndexes() {
		
		log.info("Creating indexes");
		
		//Indexes for the content collection
		MongoCollection<Document> content = mDatabase.getCollection(COLLECTION_CONTENT);
		//Unique because we should only have one piece of content with a given file hash,
		// and using a hashed index because we only want exact match and cant use two text indexes
		content.createIndex(Indexes.ascending(KEY_CONTENT_HASH), new IndexOptions().unique(true));
		//Using a text index because we want to search these terms
		content.createIndex(Indexes.text(KEY_CONTENT_TAGS));
		//An index for restricted or not
		content.createIndex(Indexes.ascending(KEY_CONTENT_RESTRICTED));
		
		//Indexes for the tags collection
		MongoCollection<Document> tags = mDatabase.getCollection(COLLECTION_TAGS);
		//Unique because we shouldn't have any duplicate tags
		tags.createIndex(Indexes.ascending(KEY_TAGS_NAME), new IndexOptions().unique(true));
		tags.createIndex(Indexes.ascending(KEY_TAGS_RESTRICTED));
		tags.createIndex(Indexes.ascending(KEY_TAGS_TYPE));
		
	}
	
	//=============================================CONTENT==========================================================
	
	/**
	 * Check if a hash value already exists in the database's content collection
	 *
	 * @param hash the hash value to check for in the database's content collection
	 * @return if the hash already exists, true for yes
	 */
	public boolean checkHash(String hash) {
		
		if (hash == null) return false;
		MongoCollection<Document> content = mDatabase.getCollection(COLLECTION_CONTENT);
		return (content.countDocuments(Filters.eq(KEY_CONTENT_HASH, hash)) == 1);
		
	}
	
	public void exportContent(Content content) throws Content.ContentValidationException {
		
		content.update(this);
		content.validate();
		
		addTag(content.getTags());
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		Document document = collection.find(Filters.eq(KEY_CONTENT_HASH, content.getHash())).first();
		
		if (document == null) {
			collection.insertOne(updateAndValidateDocFromContent(new Document(), content));
		}
		else {
			updateAndValidateDocFromContent(document, content);
			collection.findOneAndReplace(Filters.eq(KEY_CONTENT_HASH, content.getHash()), document);
		}
		
	}
	
	public void deleteContent(String hash) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		collection.deleteOne(Filters.eq(KEY_CONTENT_HASH, hash));
		
	}
	
	public long getNumContent() {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		return collection.countDocuments();
	}
	
	//++++++++++++++++++++++++++++++++++++++++Signature stuff++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public Content getContentFromHash(String hash) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		Document document = collection.find(Filters.eq(KEY_CONTENT_HASH, hash)).first();
		
		if (document == null) return null;
		
		return loadContentFromDocument(document);
		
	}
	
	public Content[] searchForContentByTags(boolean restricted, String[] tags) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		List<Document> documents = new ArrayList<>();
		if (tags.length == 0) collection.find().into(documents);
		else collection.find(Filters.all(KEY_CONTENT_TAGS, new ArrayList<>(Arrays.asList(tags)))).into(documents);
		ConcurrentLinkedQueue<Content> content = new ConcurrentLinkedQueue<>();
		
		documents.stream().parallel().forEach(document -> content.add(loadContentFromDocument(document)));
		
		if (!restricted) {
			List<Content> out = content.stream().parallel().collect(Collectors.toList());
			out.removeIf(Content::isRestricted);
			return out.toArray(new Content[]{});
		}
		else {
			return content.toArray(new Content[]{});
		}
		
		//Doing this in parallel now to improve speed
		/*for (Document document : documents) {
			content.add(loadContentFromDocument(document));
		}
		
		if (!restricted) {
			content.removeIf(Content::isRestricted);
		}
		
		return content.toArray(new Content[]{});*/
		
	}
	
	public long countContentWithTag(String tag) {
		
		return countContentWithTag(new String[]{tag});
	}
	
	//=============================================TAGS==========================================================
	
	//+++++++++++++++++++++++++++++++++++++Modify tags on content++++++++++++++++++++++++++++++++++++++++++++++++
	
	public long countContentWithTag(String[] tags) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		return collection.countDocuments(Filters.all(KEY_CONTENT_TAGS, new ArrayList<>(Arrays.asList(tags))));
		
	}
	
	public Set<Map.Entry<String, SimilarityFinder.ImageSignature>> getAllSignatures() {
		
		ConcurrentMap<String, SimilarityFinder.ImageSignature> signatures = new ConcurrentHashMap<>();
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		List<Document> documents = new ArrayList<>();
		collection.find(Filters.exists(KEY_CONTENT_SIGNATURE, true)).into(documents);
		
		documents.stream().parallel().forEach(document -> signatures.put(document.getString(KEY_CONTENT_HASH), loadImageSignatureFromDocument(document)));
		
		return signatures.entrySet();
		
	}
	
	public boolean addTagToContent(String hash, String tag) {
		
		return addTagToContent(hash, new String[]{tag});
	}
	
	public boolean addTagToContent(String hash, String[] tags) {
		
		addTag(tags);
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		Document document = collection.find(Filters.eq(KEY_CONTENT_HASH, hash)).first();
		
		//If the document doesnt exist then we return false to let the program know it needs to register new content with the database
		if (document == null) return false;
		
		document.putIfAbsent(KEY_CONTENT_TAGS, tags);
		document.computeIfPresent(KEY_CONTENT_TAGS, (key, val) -> {
			ArrayList<String> list = (ArrayList<String>) document.getList(KEY_CONTENT_TAGS, String.class);
			for (String tag : tags) {
				if (list.contains(tag)) continue;
				list.add(tag);
			}
			return list.toArray(new String[0]);
		});
		
		return true;
		
	}
	
	//++++++++++++++++++++++++++++++++++Update content tags based on tag parents++++++++++++++++++++++++++++++++++++
	
	public boolean removeTagFromContent(String hash, String tag) {
		
		return removeTagFromContent(hash, new String[]{tag});
	}
	
	public boolean removeTagFromContent(String hash, String[] tags) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		Document document = collection.find(Filters.eq(KEY_CONTENT_HASH, hash)).first();
		
		//If the document doesnt exist then we return false to let the program know it needs to register new content with the database
		if (document == null) return false;
		
		document.computeIfPresent(KEY_CONTENT_TAGS, (key, val) -> {
			ArrayList<String> list = (ArrayList<String>) document.getList(KEY_CONTENT_TAGS, String.class);
			for (String tag : tags) {
				list.remove(tag);
			}
			return list.toArray(new String[0]);
		});
		
		return true;
		
	}
	
	public void updateAllContentWithTags(Tag[] tags) {
		
		if (tags.length == 0) return;
		
		String[] names = new String[tags.length];
		for (int i = 0; i < tags.length; i++) {
			names[i] = tags[i].getName();
		}
		updateAllContentWithTags(names);
		
	}
	
	//++++++++++++++++++++++++++++++++++++remove tags from content+++++++++++++++++++++++++++++++++++++++++++++++
	
	public void updateAllContentWithTags(String[] tags) {
		
		if (tags.length == 0) return;
		
		MongoCollection<Document> contentColl = mDatabase.getCollection(COLLECTION_CONTENT);
		List<Document> documents = new ArrayList<>();
		contentColl.find(Filters.in(KEY_CONTENT_TAGS, tags)).into(documents);
		
		documents.stream().parallel().forEach(document -> {
			Content content = loadContentFromDocument(document);
			if (content == null) return;
			content.update(this);
			try {
				exportContent(content);
			}
			catch (Content.ContentValidationException e) {
				e.printStackTrace();
			}
		});
		
	}
	
	public void updateAllContent() {
		
		MongoCollection<Document> contentColl = mDatabase.getCollection(COLLECTION_CONTENT);
		List<Document> documents = new ArrayList<>();
		contentColl.find().into(documents);
		
		documents.stream().parallel().forEach(document -> {
			Content content = loadContentFromDocument(document);
			if (content == null) return;
			content.update(this);
			try {
				exportContent(content);
			}
			catch (Content.ContentValidationException e) {
				e.printStackTrace();
			}
		});
		
	}
	
	//++++++++++++++++++++++++++++Check restricted status on tags from content+++++++++++++++++++++++++++++++++++
	
	public void removeTagFromAllContent(String tag) {
		
		removeTagFromAllContent(new String[]{tag});
	}
	
	//+++++++++++++++++++++++++++++++++++++++Manage tag database+++++++++++++++++++++++++++++++++++++++++++++++++
	
	public long getNumTags() {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		return collection.countDocuments();
	}
	
	public void removeTagFromAllContent(String[] tags) {
		
		MongoCollection<Document> contentColl = mDatabase.getCollection(COLLECTION_CONTENT);
		List<Document> documents = new ArrayList<>();
		contentColl.find(Filters.in(KEY_CONTENT_TAGS, tags)).into(documents);
		
		documents.stream().parallel().forEach(document -> {
			Content content = loadContentFromDocument(document);
			if (content == null) return;
			content.removeTag(tags);
			content.update(this);
			try {
				exportContent(content);
			}
			catch (Content.ContentValidationException e) {
				e.printStackTrace();
			}
		});
		
	}
	
	public boolean isTagRestricted(String[] tags) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		//Return true if the value is not == to 0, so if any document with a name in the tags array is set to restricted
		return collection.countDocuments(Filters.and(Filters.eq(KEY_TAGS_RESTRICTED, true), Filters.in(KEY_TAGS_NAME, tags))) != 0;
		
	}
	
	public boolean deleteTag(String tag) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		DeleteResult result = collection.deleteOne(Filters.eq(KEY_TAGS_NAME, tag));
		return result.wasAcknowledged();
		
	}
	
	public List<Tag> getAllTagsWithParent(String name) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		List<Document> documents = new ArrayList<>();
		collection.find(Filters.all(KEY_TAGS_PARENTS, name)).into(documents);
		
		ConcurrentLinkedQueue<Tag> tags = new ConcurrentLinkedQueue<>();
		
		documents.stream().parallel().forEach(document -> tags.add(loadTagFromDocument(document)));
		
		return new ArrayList<>(tags);
		
	}
	
	public List<Tag> getAllTags() {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		List<Document> tagIn = new ArrayList<>();
		collection.find().into(tagIn);
		
		ConcurrentLinkedQueue<Tag> tags = new ConcurrentLinkedQueue<>();
		
		tagIn.stream().parallel().forEach(tag -> tags.add(loadTagFromDocument(tag)));
		
		return new ArrayList<>(tags);
		
	}
	
	public void exportTag(Tag tag) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		Document document = collection.find(Filters.eq(KEY_TAGS_NAME, tag.getName())).first();
		
		if (document == null) {
			collection.insertOne(updateAndValidateDocFromTag(new Document(), tag));
		}
		else {
			updateAndValidateDocFromTag(document, tag);
			collection.findOneAndReplace(Filters.eq(KEY_TAGS_NAME, tag.getName()), document);
		}
		
	}
	
	public void addTag(String tag) {
		
		addTag(new String[]{tag});
	}
	
	public void addTag(String[] tags) {
		
		for (String tag : tags) {
			
			if (getTagFromName(tag.toLowerCase(Locale.ROOT)) != null) continue;
			
			exportTag(new Tag(tag));
			
		}
		
	}
	
	public Tag getTagFromName(String name) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		Document document = collection.find(Filters.eq(KEY_TAGS_NAME, name)).first();
		
		if (document == null) return null;
		
		return loadTagFromDocument(document);
		
	}
	
	private Document updateAndValidateDocFromTag(Document document, Tag tag) {
		
		ensureTagDocumentValid(document);
		
		document.replace(KEY_TAGS_NAME, tag.getName());
		document.replace(KEY_TAGS_TYPE, tag.getTagType().name());
		document.replace(KEY_TAGS_PARENTS, new ArrayList<>(Arrays.asList(tag.getParentTags())));
		document.replace(KEY_TAGS_RESTRICTED, tag.isRestricted());
		
		return document;
		
	}
	
}
