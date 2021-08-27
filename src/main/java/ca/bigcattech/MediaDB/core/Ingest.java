/*
 *     Ingest
 *     Last Modified: 2021-08-27, 4:23 p.m.
 *     Copyright (C) 2021-08-27, 4:23 p.m.  CameronBarnes
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

package ca.bigcattech.MediaDB.core;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.image.ImageSignature;
import ca.bigcattech.MediaDB.image.SimilarityFinder;
import ca.bigcattech.MediaDB.image.ThumbnailHandler;
import ca.bigcattech.MediaDB.utils.ImageUtils;
import ca.bigcattech.MediaDB.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Ingest {
	
	//=======================================NOTES=======================================
	
	//DONE Move file to temp folder
	//DONE hash file check DB for duplicate if true discard and end, if not rename file
	//DONE create thumbnail of image/video
	//DONE if image, create signature and save as json or similar format
	//KINDA DONE, CURRENTLY AUTOMATIC if we think it's a duplicate give user option to delete or keep
	//TODO if not duplicate, or if the user chooses to keep,
	
	//TODO decide if I want to do simultaneous ingest of multiple files at once or just one at a time, going with one at a time for now
	//^^^^^^^^^^ one at a time for now
	
	//=======================================Final Static variables============================
	
	public static final String SUFFIX_THUMBNAIL = "_THUMBNAIL";
	//public static final String PREFIX_DUPLICATE_HASH = "DUPLICATE_HASH_";
	//public static final String PREFIX_DUPLICATE_SIMILARITY_100 = "DUPLICATE_SIMILARITY_100_";
	public static final String PREFIX_DUPLICATE_SIMILARITY_500 = "DUPLICATE_SIMILARITY_500_";
	
	//=======================================BEGIN CLASS=======================================
	private static final Logger log = LoggerFactory.getLogger(Ingest.class);
	private final DBHandler mDBHandler;
	private final SimilarityFinder mSimilarityFinder;
	
	//Handling ingest processes
	private final Thread mIngestThread;
	private final ConcurrentLinkedQueue<IngestTask> mIngestTasks = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<String> mHashes = new ConcurrentLinkedQueue<>();
	private Session mSession;
	private boolean mRun = false;
	
	public Ingest(DBHandler dbHandler) {
		
		log.info("Setting up ingest handler");
		
		mDBHandler = dbHandler;
		mSimilarityFinder = new SimilarityFinder(mDBHandler);
		
		mIngestThread = new Thread(() -> {
			
			while (mRun) {
				if (!mIngestTasks.isEmpty()) {
					try {
						
						boolean cont = mIngestTasks.poll().ingest();
						if (!cont) {
							mIngestTasks.clear();
						}
						else mSession.ingest();
					}
					catch (IOException | Content.ContentValidationException e) {
						e.printStackTrace();
					}
				}
				else {
					synchronized (mIngestTasks) {
						try {
							mIngestTasks.wait();
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		});
		
		log.info("Done!");
		
	}
	
	public void setSession(Session session) {
		
		mSession = session;
	}
	
	public void start() {
		
		if (!mRun) {
			log.info("Starting ingest handler");
			if (mIngestThread.getState() == Thread.State.TERMINATED || mIngestThread.getState() == Thread.State.NEW)
				mIngestThread.start();
			mRun = true;
		}
		
	}
	
	public void stop() {
		
		log.info("Stopping ingest handler");
		mRun = false;
		//mIngestThread.interrupt();
		try {
			mIngestThread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("Done!");
		
	}
	
	public synchronized void validateContentFolder() {
		
		log.info("Validate content folder");
		Arrays.stream(Objects.requireNonNull(FileSystemHandler.CONTENT_DIR.listFiles())).parallel().forEach(file -> {
			
			if (file.isDirectory()) return;
			
			String name_hash = FileSystemHandler.getHashOfSourceImage(file);
			if (!mDBHandler.checkHash(name_hash)) {
				try {
					Files.move(file.toPath(), new File(FileSystemHandler.INGEST_DIR, file.getName()).toPath());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
		log.info("Validate content folder done");
	}
	
	public synchronized void purgeDuplicates() {
		
		purgeDuplicates(FileSystemHandler.INGEST_DIR);
	}
	
	public synchronized void purgeDuplicates(File folder) {
		
		if (!folder.isDirectory()) return;
		
		mHashes.clear();
		
		Arrays.stream(Objects.requireNonNull(folder.listFiles())).parallel().forEach(file -> {
			if (file.isDirectory()) return;
			try {
				String hash = Utils.getFileChecksum(MessageDigest.getInstance("SHA-256"), file);
				if (mHashes.contains(hash) || mDBHandler.checkHash(hash)) Files.delete(file.toPath());
				else mHashes.add(hash);
			}
			catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		});
		
		
	}
	
	public int getNumIngestTasks() {
		return mIngestTasks.size();
	}
	
	public synchronized void ingestContent() {
		
		ingestContent(FileSystemHandler.INGEST_DIR);
	}
	
	public synchronized void ingestContent(File directory) {
		
		log.info("Creating content ingest tasks");
		log.debug("Ingest Content");
		
		mHashes.clear();
		
		start();
		
		File[] files = directory.listFiles();
		if (files == null || files.length == 0) return;
		//We're going to parallelize this because it gets VERY slow when the number of files gets up there
		Arrays.stream(files).parallel().forEach(file -> {
			if (file.isDirectory()) return;
			boolean newFile = true;
			for (IngestTask task : mIngestTasks) {
				if (task.mFile.getName().equals(file.getName())) newFile = false;
			}
			if (newFile) {
				
				mIngestTasks.add(new IngestTask(file));
				
			}
		});
		
		if (!mIngestTasks.isEmpty()) {
			synchronized (mIngestTasks) {
				mIngestTasks.notifyAll();
			}
		}
		
	}
	
	enum IngestResult {
		
		HASH_DUPLICATE,
		SIMILARITY_DUPLICATE_100,
		SIMILARITY_DUPLICATE_500,
		INGEST
		
	}
	
	public class IngestTask {
		
		private final File mStartDir;
		
		private boolean mIsActive = false;
		
		private File mFile;
		private ContentType mType;
		private String mExtension;
		private String mHash;
		private String[] mTags = new String[]{};
		private String mTitle = "";
		private String mDescription = "";
		private boolean isPrivate = false;
		private ImageSignature mSignature;
		private long mVideoLength = 0L;
		private IngestResult mResult = null;
		
		private boolean mCancel = false;
		private boolean mSkip = false;
		
		private IngestTask(File file) {
			
			mFile = file;
			mStartDir = mFile.getParentFile();
			if (!mStartDir.isDirectory()) throw new RuntimeException("Invalid File: " + file.getName());
		}
		
		public boolean ingest() throws IOException, Content.ContentValidationException {
			
			if (mCancel && mResult != null) {
				trash(mResult);
				return true;
			}
			
			mIsActive = true;
			
			//Get the file's type based on extension
			mType = FileSystemHandler.getContentTypeOfFile(mFile);
			//Get the file's extension
			mExtension = FileSystemHandler.getExtension(mFile);
			//Calculate the file's hash
			hash();
			//Check the database to see if we already have this file by hash
			if (mHashes.contains(mHash) || mDBHandler.checkHash(mHash)) {
				trash(IngestResult.HASH_DUPLICATE);
				//cancel(); Idk why we're calling cancel here, trash will do all the work that we need done
				return true;
			}
			else mHashes.add(mHash);
			
			//Move the file
			//If destination file already exists, delete source file and set current file to file in ingest dir
			File dest = new File(FileSystemHandler.INGEST_PROCESS_DIR.toString() + '/' + mHash + '.' + mExtension);
			if (dest.exists()) {
				Files.delete(mFile.toPath());
				mFile = dest;
			}
			else {
				mFile = Files.move(mFile.toPath(), dest.toPath()).toFile();
			}
			
			//Process the file
			long start = System.currentTimeMillis();
			boolean newContent = process();
			log.info("Processing took: " + (System.currentTimeMillis() - start) + "ms");
			
			if (newContent) {
				
				//Going to call the UI and request the user add tags, a title, and description
				requestUserInfo();
				
				if (mSkip) {
					cancel();
					return true;
				}
				
				//If so we're done here
				if (mCancel) {
					cancel();
					return false;
				}
				
				//Move the content file to the content folder
				Path destination = new File(FileSystemHandler.CONTENT_DIR.toString() + '/' + mFile.getName()).toPath();
				boolean moved = false;
				int tried = 1;
				while (tried <= 10 && !moved) {
					
					try {
						mFile = Files.move(mFile.toPath(), destination).toFile();
						if (destination.toFile().exists()) moved = true;
					}
					catch (Exception e) {
						try {
							Thread.sleep(200L * tried);
						}
						catch (InterruptedException interruptedException) {
							interruptedException.printStackTrace();
						}
					}
					tried++;
					
				}
				if (!moved) throw new IOException("Failed to move file");
				
				//Move the thumbnail file to the content thumbnail folder
				if (mType == ContentType.VIDEO) {
					
					//TODO fix the video thumbnail problem
					/*Files.move(
							new File(FileSystemHandler.INGEST_PROCESS_DIR.toString() + '\\' + mHash + SUFFIX_THUMBNAIL + ".png").toPath(),
							new File(FileSystemHandler.CONTENT_THUMBNAIL_DIR.toString() + '\\' + mHash + SUFFIX_THUMBNAIL + ".png").toPath());*/
				}
				else if (mType == ContentType.IMAGE) {
					Files.move(
							new File(FileSystemHandler.INGEST_PROCESS_DIR.toString() + '/' + mHash + SUFFIX_THUMBNAIL + '.' + mExtension).toPath(),
							new File(FileSystemHandler.CONTENT_THUMBNAIL_DIR.toString() + '/' + mHash + SUFFIX_THUMBNAIL + '.' + mExtension).toPath());
				}
				
				//Package the file into a content object
				Content content = packageContent();
				//Send the content object to the database
				export(content);
				
			}
			
			return true;
			
		}
		
		private void requestUserInfo() {
			
			mSession.displayIngestTask(this);
			try {
				synchronized (this) {
					this.wait();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		private void cancel() throws IOException {
			
			//Delete the thumbnail
			if (mType == ContentType.IMAGE)
				Files.delete(new File(FileSystemHandler.INGEST_PROCESS_DIR.toString() + '/' + mHash + SUFFIX_THUMBNAIL + '.' + mExtension).toPath());
			//Move the content back to the ingest folder
			Path destination = new File(mStartDir.toString() + '/' + mFile.getName()).toPath();
			if (!mFile.exists() || destination.toFile().exists()) return;
			boolean moved = false;
			int tried = 1;
			while (tried < 6 && !moved) {
				
				try {
					mFile = Files.move(mFile.toPath(), destination).toFile();
					if (destination.toFile().exists()) moved = true;
				}
				catch (Exception e) {
					try {
						Thread.sleep(100L * tried);
					}
					catch (InterruptedException interruptedException) {
						interruptedException.printStackTrace();
					}
				}
				tried++;
				
			}
			
		}
		
		public void setSkip() {
			
			mSkip = true;
		}
		
		public void setCancel() {
			
			mCancel = true;
		}
		
		public boolean isActive() {
			
			return mIsActive;
		}
		
		public void setResult(IngestResult result) {
			
			mResult = result;
		}
		
		public long getVideoLength() {
			
			return mVideoLength;
		}
		
		public void setVideoLength(long length) {
			
			mVideoLength = length;
		}
		
		private void hash() {
			
			try {
				log.info("Hashing, this may take a second for large files.");
				mHash = Utils.getFileChecksum(MessageDigest.getInstance("SHA-256"), mFile);
			}
			catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
				throw new RuntimeException("File checksum failed");
			} //This shouldn't ever happen
			
		}
		
		private boolean process() throws IOException {
			
			//TODO fix the video thumbnail issue
			if (mType == ContentType.VIDEO) {//ThumbnailHandler.createVideoThumbnail(mFile, new File(FileSystemHandler.INGEST_PROCESS_DIR.toString() + '\\' + mHash + SUFFIX_THUMBNAIL + ".jpg"));
			}
			else if (mType == ContentType.IMAGE) {
				File thumbnail = new File(FileSystemHandler.INGEST_PROCESS_DIR.toString() + '/' + mHash + SUFFIX_THUMBNAIL + '.' + mExtension);
				ThumbnailHandler.createImageThumbnail(mFile, thumbnail);
				if (handleImageSimilarity(thumbnail)) {
					trash(mResult);
					Files.delete(thumbnail.toPath());
					return false;
				}
			}
			
			return true;
			
		}
		
		private boolean handleImageSimilarity(File thumbnail) throws IOException {
			
			mSignature = ImageUtils.calcImageSignature(thumbnail);
			mSignature.setHash(mHash);
			long start = System.currentTimeMillis();
			List<Map.Entry<String, Double>> results = mSimilarityFinder.checkSimilarity(mSignature);
			log.info("Similarity check took: " + (System.currentTimeMillis() - start) + "ms to check similarity");
			
			if (results.isEmpty()) return false; //No similar images
			if (results.get(0).getValue() <= 100) {
				mResult = IngestResult.SIMILARITY_DUPLICATE_100;
				return true;
			}
			if (results.get(0).getValue() <= 500) {
				mResult = IngestResult.SIMILARITY_DUPLICATE_500;
				return true;
			}
			
			//TODO actually do stuff here, we need some kind of user approval process
			//TODO not sure if we want to shove these in the trash while we're waiting for that or create some kind of temporary storage
			//Probably the latter ^^^^^^^^^^ but currently the former
			//TODO referring to the TODO above probably do this back in the process function rather than in here, we're sending out the effective result in the mResult var
			return false; //TODO not sure if I actually want to do this, but I'm going to say probably
			
		}
		
		private Content packageContent() throws Content.ContentValidationException {
			
			return Content.builder(mType)
						  .hash(mHash)
						  .file(mFile)
						  .signature(mSignature)
						  .title(mTitle)
						  .tags(mTags)
						  .description(mDescription)
						  .setPrivate(isPrivate)
						  .videoLength(mVideoLength)
						  .build();
			
		}
		
		private void export(Content content) {
			
			//TODO currently this method is really excessive, as just calling the DBHandler would be fine, but I'll leave it here for now
			try {
				mDBHandler.exportContent(content);
			}
			catch (Content.ContentValidationException e) {
				e.printStackTrace();
			}//This catch cant ever happen, we're already checking for valid before putting it into this method
			
			mResult = IngestResult.INGEST;
			
		}
		
		private void trash(IngestResult ingestResult) throws IOException {
			
			try {
				switch (ingestResult) {
					case HASH_DUPLICATE, SIMILARITY_DUPLICATE_100 -> Files.delete(mFile.toPath());
					case SIMILARITY_DUPLICATE_500 -> Files.move(mFile.toPath(), new File(FileSystemHandler.TRASH_DIR.toString() + '/' + PREFIX_DUPLICATE_SIMILARITY_500 + mHash + '.' + mExtension).toPath());
					case INGEST -> throw new InvalidParameterException("Trash is for failures, Ingest is a success tag");
				}
			}
			catch (FileAlreadyExistsException e) {
				Files.delete(mFile.toPath());
			}
			
		}
		
		public void addTags(String[] tags) {
			
			ArrayList<String> list = new ArrayList<>(Arrays.asList(mTags));
			for (String tag : tags) {
				if (list.contains(tag)) continue;
				list.add(tag);
			}
			mTags = list.toArray(new String[0]);
			
		}
		
		public void removeTags(String tag) {
			
			removeTags(new String[]{tag});
		}
		
		public void removeTags(String[] tags) {
			
			List<String> list = new ArrayList<>(Arrays.asList(mTags));
			for (String tag : tags) {
				if (tag.equals("")) continue;
				list.remove(tag);
			}
			mTags = list.toArray(new String[0]);
			
		}
		
		public void setDescription(String description) {
			
			mDescription = description;
		}
		
		public void setPrivate(boolean isPrivate) {
			
			this.isPrivate = isPrivate;
		}
		
		public void setTitle(String title) {
			
			mTitle = title;
		}
		
		public File getFile() {
			
			return mFile;
		}
		
		public ContentType getType() {
			
			return mType;
		}
		
	}
	
}
