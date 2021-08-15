/*
 *     MongoDBHandler
 *     Last Modified: 2021-08-14, 5:57 p.m.
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
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.db.pool.Pool;
import ca.bigcattech.MediaDB.db.tag.Tag;
import ca.bigcattech.MediaDB.image.ImageSignature;
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
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MongoDBHandler implements DBHandler {
	
	private static final String COLLECTION_CONTENT = "content";
	private static final String COLLECTION_TAGS = "tags";
	private static final String COLLECTION_POOLS = "pools";
	private static final String COLLECTION_SIGNATURES = "signature";
	
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
	private static final String KEY_CONTENT_POOLS = "content_pools";
	
	private static final String KEY_TAGS_NAME = "tags_name";
	private static final String KEY_TAGS_TYPE = "tags_type";
	private static final String KEY_TAGS_PARENTS = "tags_parents";
	private static final String KEY_TAGS_RESTRICTED = "tags_restricted";
	private static final String KEY_TAGS_USES = "tags_uses";
	
	private static final String KEY_POOLS_UID = "pools_uid";
	private static final String KEY_POOLS_TITLE = "pools_title";
	private static final String KEY_POOLS_DESCRIPTION = "pools_description";
	private static final String KEY_POOLS_PRIVATE = "pools_private";
	private static final String KEY_POOLS_RESTRICTED = "pools_restricted";
	private static final String KEY_POOLS_ALL_TAGS = "pools_all_tags";
	private static final String KEY_POOLS_CONTENT_HASHES = "pools_content_hashes";
	private static final String KEY_POOLS_THUMBNAIL_FILE = "pools_thumbnail_file";
	private static final String KEY_POOLS_FAVORITE = "pools_favorite";
	
	private static final String KEY_SIGNATURE_CONTENT_HASH = "signature_content_hash";
	
	private static final Logger log = LoggerFactory.getLogger(MongoDBHandler.class);
	
	private final MongoDatabase mDatabase;
	
	public MongoDBHandler(String database, String address) {
		
		log.info("Starting DBHandler");
		
		MongoClient mongoClient = MongoClients.create(
				MongoClientSettings.builder()
								   .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(address))))
								   .build());
		
		mDatabase = mongoClient.getDatabase(database);
		
		log.info("Done!");
		
	}
	
	private static Document ensureContentDocumentValid(Document document) {
		
		document.putIfAbsent(KEY_CONTENT_HASH, "null");
		document.putIfAbsent(KEY_CONTENT_FILE, "null");
		document.putIfAbsent(KEY_CONTENT_TYPE, "null");
		document.putIfAbsent(KEY_CONTENT_TAGS, new ArrayList<String>());
		document.putIfAbsent(KEY_CONTENT_TITLE, "None");
		document.putIfAbsent(KEY_CONTENT_DESCRIPTION, "None");
		document.putIfAbsent(KEY_CONTENT_VIEWS, 0);
		document.putIfAbsent(KEY_CONTENT_RESTRICTED, false);
		document.putIfAbsent(KEY_CONTENT_PRIVATE, false);
		document.putIfAbsent(KEY_CONTENT_FAVORITE, false);
		document.putIfAbsent(KEY_CONTENT_VIDEO_LENGTH, 0L);
		document.putIfAbsent(KEY_CONTENT_TIME_SPENT, 0L);
		document.putIfAbsent(KEY_CONTENT_POOLS, new ArrayList<String>());
		
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
		
		//We're doing this because I cant seem to get MongoDB to just store a pain int[] this will have to do for now. It should be a negligible performance impact.
		document.replace(KEY_CONTENT_POOLS, new ArrayList<>(Arrays.stream(content.getPools()).mapToObj(Integer::toString).collect(Collectors.toList())));
		//document.replace(KEY_CONTENT_POOLS, content.getPools());
		//document.replace(KEY_CONTENT_POOLS, Arrays.stream(content.getPools()).boxed().collect(Collectors.toCollection(ArrayList::new)));
		if (content.getSignature() != null) {
			if (document.containsKey(KEY_CONTENT_SIGNATURE))
				document.replace(KEY_CONTENT_SIGNATURE, createImageSignatureDocument(content.getSignature()));
			else document.put(KEY_CONTENT_SIGNATURE, createImageSignatureDocument(content.getSignature()));
		}
		else document.remove(KEY_CONTENT_SIGNATURE);
		
		return document;
		
	}
	
	private static Pool loadPoolFromDocument(Document document) {
		
		if (document == null) return null;
		
		ensurePoolDocumentValid(document);
		
		return Pool.builder(document.getInteger(KEY_POOLS_UID))
				   .title(document.getString(KEY_POOLS_TITLE))
				   .description(document.getString(KEY_POOLS_DESCRIPTION))
				   .isPrivate(document.getBoolean(KEY_POOLS_PRIVATE))
				   .isRestricted((document.getBoolean(KEY_POOLS_RESTRICTED)))
				   .allTags(document.getList(KEY_POOLS_ALL_TAGS, String.class).toArray(new String[]{}))
				   .contentHashes(document.getList(KEY_POOLS_CONTENT_HASHES, String.class).toArray(new String[]{}))
				   .isFavorite(document.getBoolean(KEY_POOLS_FAVORITE))
				   .thumbnailFile(document.getString(KEY_POOLS_THUMBNAIL_FILE))
				   .build();
		
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
						  .signature(loadImageSignatureFromContentDocument(document))
						  .videoLength(document.getLong(KEY_CONTENT_VIDEO_LENGTH))
						  .timeSpent(document.getLong(KEY_CONTENT_TIME_SPENT))
						  .pools(Arrays.stream(document.getList(KEY_CONTENT_POOLS, String.class).toArray(new String[]{})).mapToInt(Integer::valueOf).toArray())
						  .build();
			
		}
		catch (Content.ContentValidationException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	private static Document createImageSignatureDocument(ImageSignature signature) {
		
		Document document = new Document();
		
		List<List<String>> data = signature.convertDataStructure();
		document.put("0", data.get(0));
		document.put("1", data.get(1));
		document.put("2", data.get(2));
		document.put("3", data.get(3));
		document.put("4", data.get(4));
		
		if (signature.getHash() != null && !signature.getHash().isEmpty()) {
			document.put(KEY_SIGNATURE_CONTENT_HASH, signature.getHash());
		}
		
		return document;
		
	}
	
	private static ImageSignature loadImageSignatureFromDocument(Document document) {
		
		if (document == null) return null;
		
		List<List<String>> data = new ArrayList<>();
		
		data.add(document.getList("0", String.class));
		data.add(document.getList("1", String.class));
		data.add(document.getList("2", String.class));
		data.add(document.getList("3", String.class));
		data.add(document.getList("4", String.class));
		
		return new ImageSignature(document.getString(KEY_SIGNATURE_CONTENT_HASH), data);
		
	}
	
	@Deprecated
	private static ImageSignature loadImageSignatureFromContentDocument(Document document) {
		
		if (!document.containsKey(KEY_CONTENT_SIGNATURE)) return null;
		
		List<List<String>> data = new ArrayList<>();
		
		Document signatureDoc = (Document) document.get(KEY_CONTENT_SIGNATURE);
		
		data.add(signatureDoc.getList("0", String.class));
		data.add(signatureDoc.getList("1", String.class));
		data.add(signatureDoc.getList("2", String.class));
		data.add(signatureDoc.getList("3", String.class));
		data.add(signatureDoc.getList("4", String.class));
		
		return new ImageSignature(document.getString(KEY_CONTENT_HASH), data);
		
	}
	
	private static Tag loadTagFromDocument(Document document) {
		
		ensureTagDocumentValid(document);
		
		return new Tag(
				document.getString(KEY_TAGS_NAME),
				Tag.TagType.valueOf(document.getString(KEY_TAGS_TYPE)),
				document.getList(KEY_TAGS_PARENTS, String.class).toArray(new String[]{}),
				document.getBoolean(KEY_TAGS_RESTRICTED),
				document.getInteger(KEY_TAGS_USES)
		);
		
	}
	
	private static Document ensureTagDocumentValid(Document document) {
		
		document.putIfAbsent(KEY_TAGS_NAME, "null");
		document.putIfAbsent(KEY_TAGS_TYPE, Tag.TagType.TAG.name());
		document.putIfAbsent(KEY_TAGS_PARENTS, new ArrayList<String>());
		document.putIfAbsent(KEY_TAGS_RESTRICTED, false);
		document.putIfAbsent(KEY_TAGS_USES, 0);
		return document;
		
	}
	
	@Override
	public boolean poolExists(int uid) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_POOLS);
		return collection.countDocuments(Filters.eq(KEY_POOLS_UID, uid)) > 0;
		
	}
	
	@Override
	public void initDB() {
		
		log.info("Init MongoDBHandler");
		log.info("Using MongoDBHandler");
		
		createCollections();
		createIndexes();
		
		migrate();
		
		log.info("MongoDBHandler init finished");
		
	}
	
	@Override
	public void migrate() {
		
		log.info("Migrating data if needed");
		long start = System.currentTimeMillis();
		
		migrateSignatures();
		
		log.info("Migrating took " + (System.currentTimeMillis() - start) + "ms");
		
	}
	
	private void migrateSignatures() {
		
		List<Document> documents = new ArrayList<>();
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		collection.find(Filters.exists(KEY_CONTENT_SIGNATURE, true)).into(documents);
		
		if (!documents.isEmpty()) {
			ConcurrentLinkedQueue<Content> content = new ConcurrentLinkedQueue<>();
			documents.stream().parallel().filter(Objects::nonNull).forEach(document -> content.add(loadContentFromDocument(document)));
			log.info(content.size() + " ImageSignatures to migrate");
			
			content.stream().parallel().forEach(c -> {
				ImageSignature signature = c.getSignature();
				if (signature.getHash() == null || signature.getHash().isEmpty()) signature.setHash(c.getHash());
				exportSignature(signature);
				c.setSignature(null);
				try {
					exportContent(c);
				}
				catch (Content.ContentValidationException e) {
					e.printStackTrace();
				}
			});
		}
		
	}
	
	private void clearSignatureFromContent(String hash) {
	
	
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
		if (!collections.contains(COLLECTION_POOLS)) {
			log.info("Creating pools collection");
			mDatabase.createCollection(COLLECTION_POOLS);
			created = true;
		}
		if (!collections.contains(COLLECTION_SIGNATURES)) {
			log.info("Creating Signature collection");
			mDatabase.createCollection(COLLECTION_SIGNATURES);
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
		tags.createIndex(Indexes.ascending(KEY_TAGS_USES));
		
		//Indexes for the pools collection
		MongoCollection<Document> pools = mDatabase.getCollection(COLLECTION_POOLS);
		pools.createIndex(Indexes.ascending(KEY_POOLS_UID), new IndexOptions().unique(true));
		pools.createIndex(Indexes.text(KEY_POOLS_ALL_TAGS));
		
		//Indexes for the signatures collection
		
		MongoCollection<Document> signatures = mDatabase.getCollection(COLLECTION_SIGNATURES);
		signatures.createIndex(Indexes.ascending(KEY_SIGNATURE_CONTENT_HASH), new IndexOptions().unique(true));
		
	}
	
	/**
	 * Check if a hash value already exists in the database's content collection
	 *
	 * @param hash the hash value to check for in the database's content collection
	 * @return if the hash already exists, true for yes
	 */
	@Override
	public boolean checkHash(String hash) {
		
		if (hash == null) return false;
		MongoCollection<Document> content = mDatabase.getCollection(COLLECTION_CONTENT);
		return (content.countDocuments(Filters.eq(KEY_CONTENT_HASH, hash)) == 1);
		
	}
	
	@Override
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
			collection.findOneAndReplace(Filters.eq(KEY_CONTENT_HASH, content.getHash()), updateAndValidateDocFromContent(document, content));
		}
		
	}
	
	@Override
	public void deleteContent(String hash) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		collection.deleteOne(Filters.eq(KEY_CONTENT_HASH, hash));
		
	}
	
	@Override
	public long getNumContent() {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		return collection.countDocuments();
		
	}
	
	@Override
	public Pool[] getPoolFromUID(int[] uids) {
		
		ArrayList<Pool> out = new ArrayList<>();
		for (int uid : uids) {
			Pool pool = getPoolFromUID(uid);
			if (pool != null) out.add(pool);
		}
		
		return out.toArray(new Pool[]{});
		
	}
	
	@Override
	public Pool getPoolFromUID(int uid) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_POOLS);
		Document document = collection.find(Filters.eq(KEY_POOLS_UID, uid)).first();
		
		if (document == null) return null;
		
		return loadPoolFromDocument(document);
		
	}
	
	@Override
	public void exportPool(Pool pool) {
		
		pool.update(this);
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_POOLS);
		Document document = collection.find(Filters.eq(KEY_POOLS_UID, pool.getUID())).first();
		
		if (document == null) {
			collection.insertOne(updateAndValidateDocFromPool(new Document(), pool));
		}
		else {
			collection.replaceOne(Filters.eq(KEY_POOLS_UID, pool.getUID()), updateAndValidateDocFromPool(document, pool));
		}
		
	}
	
	private static Document ensurePoolDocumentValid(Document document) {
		
		if (!document.containsKey(KEY_POOLS_UID)) throw new RuntimeException("Invalid Pool Document, no UID.");
		document.putIfAbsent(KEY_POOLS_TITLE, "None");
		document.putIfAbsent(KEY_POOLS_DESCRIPTION, "None");
		document.putIfAbsent(KEY_POOLS_PRIVATE, false);
		document.putIfAbsent(KEY_POOLS_RESTRICTED, false);
		document.putIfAbsent(KEY_POOLS_CONTENT_HASHES, new ArrayList<String>());
		document.putIfAbsent(KEY_POOLS_ALL_TAGS, new ArrayList<String>());
		document.putIfAbsent(KEY_POOLS_FAVORITE, false);
		document.putIfAbsent(KEY_POOLS_THUMBNAIL_FILE, "null");
		
		return document;
		
	}
	
	private static Document ensurePoolDocumentValid(Document document, int uid) {
		
		if (!document.containsKey(KEY_POOLS_UID)) document.put(KEY_POOLS_UID, uid);
		else if (document.getInteger(KEY_POOLS_UID) != uid)
			throw new RuntimeException("Invalid Pool Document, UID not match.");
		return ensurePoolDocumentValid(document);
		
	}
	
	private static Document updateAndValidateDocFromPool(Document document, Pool pool) {
		
		ensurePoolDocumentValid(document, pool.getUID());
		
		document.replace(KEY_POOLS_TITLE, pool.getTitle());
		document.replace(KEY_POOLS_DESCRIPTION, pool.getDescription());
		document.replace(KEY_POOLS_PRIVATE, pool.isPrivate());
		document.replace(KEY_POOLS_RESTRICTED, pool.isRestricted());
		document.replace(KEY_POOLS_ALL_TAGS, new ArrayList<>(Arrays.asList(pool.getAllTags())));
		document.replace(KEY_POOLS_CONTENT_HASHES, new ArrayList<>(Arrays.asList(pool.getContentHashes())));
		document.replace(KEY_POOLS_FAVORITE, pool.isFavorite());
		document.replace(KEY_POOLS_THUMBNAIL_FILE, pool.getThumbnailFile());
		
		return document;
		
	}
	
	@Override
	public Content[] getContentFromHash(String[] hashes) {
		
		ArrayList<Content> out = new ArrayList<>(hashes.length);
		
		for (String hash : hashes) {
			
			Content tmp = getContentFromHash(hash);
			if (tmp != null) out.add(tmp);
			
		}
		
		out.trimToSize();
		return out.toArray(new Content[]{});
		
	}
	
	@Override
	public Content getContentFromHash(String hash) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		Document document = collection.find(Filters.eq(KEY_CONTENT_HASH, hash)).first();
		
		if (document == null) return null;
		
		return loadContentFromDocument(document);
		
	}
	
	@Override
	public Pool[] searchForPoolsByTags(boolean restricted, String[] tags) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_POOLS);
		List<Document> documents = new ArrayList<>((int) collection.countDocuments());
		if (tags.length == 0) collection.find().into(documents);
		else collection.find(Filters.all(KEY_POOLS_ALL_TAGS, new ArrayList<>(Arrays.asList(tags)))).into(documents);
		
		long start = System.currentTimeMillis();
		ConcurrentLinkedQueue<Pool> pools = new ConcurrentLinkedQueue<>();
		documents.stream().parallel().filter(Objects::nonNull).filter(filterPoolRestricted(restricted)).forEach(document -> pools.add(loadPoolFromDocument(document)));
		log.info("Loading pools from documents took: " + (System.currentTimeMillis() - start) + "ms");
		
		return pools.toArray(new Pool[]{});
		
	}
	
	@Override
	public Content[] searchForContentByTags(Options.SearchOptions searchOptions, String[] tags) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		List<Document> documents = new ArrayList<>();
		if (tags.length == 0 && searchOptions.allContentAllowed()) collection.find().into(documents);
		else collection.find(searchFilter(tags, searchOptions)).into(documents);
		
		long start = System.currentTimeMillis();
		ConcurrentLinkedQueue<Content> content = new ConcurrentLinkedQueue<>();
		documents.stream().parallel().filter(filterContentRestricted(searchOptions.isRestricted())).forEach(document -> content.add(loadContentFromDocument(document)));
		log.info("Loading content from documents took: " + (System.currentTimeMillis() - start) + "ms");
		
		return content.toArray(new Content[]{});
		
	}
	
	/**
	 * Previously this was deprecated, now it's private. That'll do
	 * WOW! This code performs bad.
	 * (An estimated 24 million string comparisons for 400 tags and 4000 content objects with roughly 15.25 tags each, this takes about 2.4 seconds to complete. This is totally unacceptable.)
	 * We still probably need to use it sometime, but we're going to primarily move to storing this value with the tag in the tag collection.
	 *
	 * @param tag the tag to check for and count for content with
	 * @return the number of content documents that contain the provided tag
	 */
	
	private long countContentWithTag(String tag) {
		
		return countContentWithTag(new String[]{tag});
	}
	
	/**
	 * Previously this was deprecated, now it's private. That'll do
	 * WOW! This code performs bad.
	 * (An estimated 24 million string comparisons for 400 tags and 4000 content objects with roughly 15.25 tags each, this takes about 2.4 seconds to complete. This is totally unacceptable.)
	 * We still probably need to use it sometime, but we're going to primarily move to storing this value with the tag in the tag collection.
	 *
	 * @param tags the tags to check for and count for content with
	 * @return the number of content documents that contain the provided tags
	 */
	private long countContentWithTag(String[] tags) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_CONTENT);
		return collection.countDocuments(Filters.all(KEY_CONTENT_TAGS, new ArrayList<>(Arrays.asList(tags))));
		
	}
	
	@Override
	public List<ImageSignature> getAllSignatures() {
		
		ConcurrentLinkedQueue<ImageSignature> signatures;
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_SIGNATURES);
		List<Document> documents = new ArrayList<>();
		collection.find().into(documents);
		
		signatures = new ConcurrentLinkedQueue<>();
		
		documents.stream().parallel().forEach(document -> signatures.add(loadImageSignatureFromDocument(document)));
		
		return new ArrayList<>(signatures);
		
	}
	
	@Override
	public void exportSignature(ImageSignature signature) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_SIGNATURES);
		Document document = collection.find(Filters.eq(KEY_SIGNATURE_CONTENT_HASH, signature.getHash())).first();
		
		if (document != null) {
			collection.replaceOne(Filters.eq(KEY_SIGNATURE_CONTENT_HASH, signature.getHash()), createImageSignatureDocument(signature));
		}
		else {
			collection.insertOne(createImageSignatureDocument(signature));
		}
		
	}
	
	@Override
	public ImageSignature getSignatureFromHash(String hash) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_SIGNATURES);
		Document document = collection.find(Filters.eq(KEY_SIGNATURE_CONTENT_HASH, hash)).first();
		
		return loadImageSignatureFromDocument(document);
		
	}
	
	@Override
	public boolean checkSignatureExists(String hash) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_SIGNATURES);
		return collection.countDocuments(Filters.eq(KEY_SIGNATURE_CONTENT_HASH, hash)) > 0;
		
	}
	
	@Override
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
	
	@Override
	public void updateAllWithTags(Tag[] tags) {
		
		if (tags.length == 0) return;
		
		updateAllWithTags(Arrays.stream(tags).map(Tag::getName).collect(Collectors.toList()).toArray(new String[]{}));
		
	}
	
	@Override
	public void updateAllWithTags(String[] tags) {
		
		updateAllContentWithTags(tags);
		updateAllPoolsWithTags(tags);
		
	}
	
	//Removing this as it doesnt actually get used. I'll leave it here just in case I want it later and wonder why it's not still there
	/*public void updateAllContentWithTag(Tag[] tags) {
		
		if (tags.length == 0) return;
		
		updateAllContentWithTags(Arrays.stream(tags).map(Tag::getName).collect(Collectors.toList()).toArray(new String[]{}));
		
	}
	
	public void updateAllPoolsWithTag(Tag[] tags) {
		
		if (tags.length == 0) return;
		
		updateAllPoolsWithTags(Arrays.stream(tags).map(Tag::getName).collect(Collectors.toList()).toArray(new String[]{}));
		
	}*/
	
	@Override
	public void updateAllPoolsWithTags(String[] tags) {
		
		if (tags.length == 0) return;
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_POOLS);
		List<Document> documents = new ArrayList<>();
		collection.find(Filters.in(KEY_POOLS_ALL_TAGS, tags)).into(documents);
		
		documents.stream().parallel().forEach(document -> {
			Pool pool = loadPoolFromDocument(document);
			if (pool == null) return;
			pool.update(this);
			exportPool(pool);
		});
		
	}
	
	@Override
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
	
	@Override
	public void updateAllPools() {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_POOLS);
		List<Document> documents = new ArrayList<>();
		collection.find().into(documents);
		
		documents.stream().parallel().forEach(document -> {
			Pool pool = loadPoolFromDocument(document);
			if (pool == null) return;
			pool.update(this);
			exportPool(pool);
		});
		
	}
	
	@Override
	public void updateAll() {
		
		updateAllContent();
		updateAllPools();
		updateAllTags();
	}
	
	@Override
	public void removeTagFromAllContent(String tag) {
		
		removeTagFromAllContent(new String[]{tag});
	}
	
	@Override
	public void removeTagFromAll(String tag) {
		
		removeTagFromAll(new String[]{tag});
	}
	
	@Override
	public void removeTagFromAll(String[] tags) {
		
		removeTagFromAllContent(tags);
		updateAllPools();
	}
	
	@Override
	public long getNumTags() {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		return collection.countDocuments();
	}
	
	@Override
	public void removeTagFromAllContent(String[] tags) {
		
		MongoCollection<Document> contentColl = mDatabase.getCollection(COLLECTION_CONTENT);
		List<Document> documents = new ArrayList<>();
		contentColl.find(Filters.in(KEY_CONTENT_TAGS, tags)).into(documents);
		
		documents.stream().parallel().forEach(document -> {
			Content content = loadContentFromDocument(document);
			if (content == null) return;
			content.removeTag(tags, this);
			content.update(this);
			try {
				exportContent(content);
			}
			catch (Content.ContentValidationException e) {
				e.printStackTrace();
			}
		});
		
	}
	
	@Override
	public boolean isTagRestricted(String[] tags) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		//Return true if the value is not == to 0, so if any document with a name in the tags array is set to restricted
		return collection.countDocuments(Filters.and(Filters.eq(KEY_TAGS_RESTRICTED, true), Filters.in(KEY_TAGS_NAME, tags))) != 0;
		
	}
	
	@Override
	public boolean deleteTag(String tag) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		DeleteResult result = collection.deleteOne(Filters.eq(KEY_TAGS_NAME, tag));
		return result.wasAcknowledged();
		
	}
	
	@Override
	public List<Tag> getAllTagsWithParent(String name) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		List<Document> documents = new ArrayList<>();
		collection.find(Filters.all(KEY_TAGS_PARENTS, name)).into(documents);
		
		ConcurrentLinkedQueue<Tag> tags = new ConcurrentLinkedQueue<>();
		
		documents.stream().parallel().forEach(document -> tags.add(loadTagFromDocument(document)));
		
		return new ArrayList<>(tags);
		
	}
	
	@Override
	public List<Tag> getAllTags() {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		List<Document> tagIn = new ArrayList<>();
		collection.find().into(tagIn);
		
		ConcurrentLinkedQueue<Tag> tags = new ConcurrentLinkedQueue<>();
		
		tagIn.stream().parallel().forEach(tag -> tags.add(loadTagFromDocument(tag)));
		
		return new ArrayList<>(tags);
		
	}
	
	@Override
	public void exportTag(Tag tag, boolean manualCount) {
		
		if (manualCount) tag.setUses((int) countContentWithTag(tag.getName()));
		
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
	
	@Override
	public void decrementTagUses(String[] tags) {
		
		for (String tagName : tags) {
			
			Tag tag = getTagFromName(tagName);
			tag.decrementUses();
			exportTag(tag, false);
			
		}
		
	}
	
	@Override
	public void incrementTagUses(String[] tags) {
		
		for (String tagName : tags) {
			
			Tag tag = getTagFromName(tagName);
			tag.incrementUses();
			exportTag(tag, false);
			
		}
		
	}
	
	@Override
	public void addTag(String tag) {
		
		addTag(new String[]{tag});
	}
	
	@Override
	public void addTag(String[] tags) {
		
		for (String tag : tags) {
			
			if (getTagFromName(tag.toLowerCase(Locale.ROOT)) != null) continue;
			
			exportTag(new Tag(tag), false);
			
		}
		
	}
	
	@Override
	public Tag getTagFromName(String name) {
		
		MongoCollection<Document> collection = mDatabase.getCollection(COLLECTION_TAGS);
		Document document = collection.find(Filters.eq(KEY_TAGS_NAME, name)).first();
		
		if (document == null) return null;
		
		Tag tag = loadTagFromDocument(document);
		if (tag.getNumUses() == 0) {
			exportTag(tag, true);
		}
		return tag;
		
	}
	
	@Override
	public void updateAllTags() {
		
		getAllTags().stream().parallel().forEach(tag -> exportTag(tag, true));
		
	}
	
	@Override
	public void updateAllTags(Tag[] tags) {
		
		Arrays.stream(tags).parallel().forEach(tag -> exportTag(tag, true));
		
	}
	
	private Document updateAndValidateDocFromTag(Document document, Tag tag) {
		
		ensureTagDocumentValid(document);
		
		document.replace(KEY_TAGS_NAME, tag.getName());
		document.replace(KEY_TAGS_TYPE, tag.getTagType().name());
		document.replace(KEY_TAGS_PARENTS, new ArrayList<>(Arrays.asList(tag.getParentTags())));
		document.replace(KEY_TAGS_RESTRICTED, tag.isRestricted());
		document.replace(KEY_TAGS_USES, tag.getNumUses());
		
		return document;
		
	}
	
	private static Bson searchFilter(String[] tags, Options.SearchOptions searchOptions) {
		
		if (searchOptions.allContentAllowed())
			return Filters.all(KEY_CONTENT_TAGS, new ArrayList<>(Arrays.asList(tags)));
		else if (tags.length > 0) return Filters.and(
				Filters.all(KEY_CONTENT_TAGS, new ArrayList<>(Arrays.asList(tags))),
				Filters.or(searchOptions.getContentTypeFilters())
		);
		else return Filters.or(searchOptions.getContentTypeFilters());
		
	}
	
	private static Predicate<Document> filterPoolRestricted(boolean restricted) {
		
		return p -> !p.getBoolean(KEY_POOLS_RESTRICTED) || restricted;
	}
	
	private static Predicate<Document> filterContentRestricted(boolean restricted) {
		
		return p -> !p.getBoolean(KEY_CONTENT_RESTRICTED) || restricted;
	}
	
}
