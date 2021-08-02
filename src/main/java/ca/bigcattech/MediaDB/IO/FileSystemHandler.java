/*
 *     FileSystemHandler
 *     Last Modified: 2021-07-24, 2:13 a.m.
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

package ca.bigcattech.MediaDB.IO;

import ca.bigcattech.MediaDB.core.Options;
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.utils.Utils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileSystemHandler {
	
	public static final File INGEST_DIR = createRootSubdirectory(new File("ingest"));
	public static final File INGEST_PROCESS_DIR = createSubdirectory(new File("ingest"), new File("process"));
	public static final File CONTENT_DIR = createRootSubdirectory(new File("content"));
	public static final File CONTENT_THUMBNAIL_DIR = createSubdirectory(new File("content"), new File("thumbnails"));
	public static final File TRASH_DIR = createRootSubdirectory(new File("trash"));
	private static final Logger log = LoggerFactory.getLogger(FileSystemHandler.class.getName());
	
	private static final String FONT_URL = "https://use.fontawesome.com/releases/v5.15.3/fontawesome-free-5.15.3-desktop.zip";
	private static final String FONT_FILE_NAME = "Font Awesome 5 Free-Solid-900.otf";
	
	private FileSystemHandler() {
	
	}
	
	public static void init() {
		
		log.info("Creating project directory structure");
		
		INGEST_PROCESS_DIR.mkdirs();
		CONTENT_THUMBNAIL_DIR.mkdirs();
		TRASH_DIR.mkdirs();
		
		log.info("Downloading font");
		
		downloadFont();
		
		log.info("Done!");
		
	}
	
	private static final String KEY_OPTIONS_NUM_COLUMNS = "options_columns";
	private static final String KEY_OPTIONS_RESULTS_PER_PAGE = "options_results";
	private static final String KEY_INGEST_OPTIONS_AUTO_TAG_FIELD = "ingest_options_auto_tag_field";
	private static final String KEY_SEARCH_OPTIONS_FAVORITES = "search_options_favorites";
	private static final String KEY_SEARCH_OPTIONS_REVERSE_ORDER = "search_options_reverse_order";
	private static final String KEY_SEARCH_OPTIONS_SEARCH_ORDER = "search_options_search_order";
	private static final String KEY_SEARCH_OPTIONS_RESTRICTED = "search_options_restricted";
	private static final String KEY_CONTENT_OPTIONS_SLIDESHOW_TIMER = "content_options_slideshow_timer";
	
	public static Options getOptions() {
		
		try {
			String file = new String(Files.readAllBytes(Path.of("options.json")));
			Document document = Document.parse(file);
			return loadFromDocument(document);
		}
		catch (IOException e) {
			//e.printStackTrace(); //This is fine, because then we'll just return a new options object
		}
		
		return new Options();
		
	}
	
	private static Document validateDocument(Document document) {
		
		document.putIfAbsent(KEY_OPTIONS_NUM_COLUMNS, 5);
		document.putIfAbsent(KEY_OPTIONS_RESULTS_PER_PAGE, 250);
		document.putIfAbsent(KEY_INGEST_OPTIONS_AUTO_TAG_FIELD, false);
		document.putIfAbsent(KEY_SEARCH_OPTIONS_FAVORITES, false);
		document.putIfAbsent(KEY_SEARCH_OPTIONS_REVERSE_ORDER, false);
		document.putIfAbsent(KEY_SEARCH_OPTIONS_SEARCH_ORDER, Options.SearchOptions.SortType.HASH);
		document.putIfAbsent(KEY_CONTENT_OPTIONS_SLIDESHOW_TIMER, 5);
		document.putIfAbsent(KEY_SEARCH_OPTIONS_RESTRICTED, false);
		return document;
		
	}
	
	private static Options loadFromDocument(Document document) {
		
		validateDocument(document);
		Options options = new Options();
		options.setColumns(document.getInteger(KEY_OPTIONS_NUM_COLUMNS));
		options.setResultsPerPage(document.getInteger(KEY_OPTIONS_RESULTS_PER_PAGE));
		options.setIngestAutoTagField(document.getBoolean(KEY_INGEST_OPTIONS_AUTO_TAG_FIELD));
		options.getSearchOptions().setRestricted(document.getBoolean(KEY_SEARCH_OPTIONS_RESTRICTED));
		options.getSearchOptions().setFavoritesFirst(document.getBoolean(KEY_SEARCH_OPTIONS_FAVORITES));
		options.getSearchOptions().setReverseOrder(document.getBoolean(KEY_SEARCH_OPTIONS_REVERSE_ORDER));
		options.getSearchOptions().setSearchType(Options.SearchOptions.SortType.valueOf(document.getString(KEY_SEARCH_OPTIONS_SEARCH_ORDER)));
		options.setSlideshowTimer(document.getInteger(KEY_CONTENT_OPTIONS_SLIDESHOW_TIMER));
		return options;
		
	}
	
	public static void writeOptions(Options options) {
		
		Document document = writeToDocument(options);
		
		File file = new File("options.json");
		try {
			file.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		try (FileWriter writer = new FileWriter(file)) {
			
			writer.write(document.toJson());
			writer.flush();
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Document writeToDocument(Options options) {
		
		Document document = new Document();
		document.put(KEY_OPTIONS_NUM_COLUMNS, options.getColumns());
		document.put(KEY_OPTIONS_RESULTS_PER_PAGE, options.getResultsPerPage());
		document.put(KEY_INGEST_OPTIONS_AUTO_TAG_FIELD, options.isIngestAutoTagField());
		document.put(KEY_SEARCH_OPTIONS_RESTRICTED, options.getSearchOptions().isRestricted());
		document.put(KEY_SEARCH_OPTIONS_FAVORITES, options.getSearchOptions().isFavoritesFirst());
		document.put(KEY_SEARCH_OPTIONS_REVERSE_ORDER, options.getSearchOptions().isReverseOrder());
		document.put(KEY_SEARCH_OPTIONS_SEARCH_ORDER, options.getSearchOptions().getSearchType().name());
		document.put(KEY_CONTENT_OPTIONS_SLIDESHOW_TIMER, options.getSlideshowTimer());
		return document;
		
	}
	
	//================================Bellow here are some util functions====================================
	
	public static int getNumberContentInIngestFolder() {
		return listNumNotFolderRecursive(INGEST_DIR);
	}
	
	private static int listNumNotFolderRecursive(File folder) {
		
		if (folder == null || !folder.isDirectory()) return 0;
		
		int numFiles = 0;
		
		for (File file: Objects.requireNonNull(folder.listFiles())) {
			if (file.equals(folder)) continue;
			if (file.isDirectory()) {
				numFiles += listNumNotFolderRecursive(folder);
			}
			else numFiles++;
		}
		
		return numFiles;
		
	}
	
	public static Font getUnicodeFont() {
		
		File font = new File(FONT_FILE_NAME);
		if (!font.exists()) downloadFont();
		
		try {
			return Font.createFont(Font.TRUETYPE_FONT, font).deriveFont(16.0f);
		}
		catch (FontFormatException | IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static void downloadFont() {
		
		try {
			
			File fontFile = new File(FONT_FILE_NAME);
			
			if (!fontFile.exists()) {
				
				File zip = new File("tmp.zip");
				boolean good = downloadFile(FONT_URL, zip);
				
				if (good) {
					
					try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zip))) {
						ZipEntry zipEntry = zipInputStream.getNextEntry();
						boolean done = false;
						while (zipEntry != null && !done) {
							
							if (Utils.getLastSubstring(zipEntry.getName(), "/").equals(FONT_FILE_NAME)) {
								
								byte[] buffer = new byte[1024];
								try (FileOutputStream fileOutputStream = new FileOutputStream(fontFile)) {
									int len;
									while ((len = zipInputStream.read(buffer)) > 0) {
										fileOutputStream.write(buffer, 0, len);
									}
								}
								
								if (fontFile.exists()) done = true;
								
							}
							
							zipEntry = zipInputStream.getNextEntry();
							
						}
					}
					
					Files.delete(zip.toPath());
					
				}
				
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static boolean downloadFile(String fileURL, File fileOut) throws IOException {
		
		if (fileOut.exists()) throw new IOException("File already exists");
		
		URL url = new URL(fileURL);
		try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream())) {
			
			try (FileOutputStream fileOutputStream = new FileOutputStream(fileOut)) {
				fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			}
			
		}
		
		return fileOut.exists();
		
	}
	
	public static String getHashOfSourceImage(String fileName) {
		return Utils.getFirstSubstring(fileName, "[\\._]");
	}
	
	public static String getHashOfSourceImage(File file) {
		return Utils.getFirstSubstring(file.getName(), "[\\._]");
	}
	
	public static ContentType getContentTypeOfFile(File file) {
		
		ContentType type;
		
		switch (Utils.getLastSubstring(file.toString(), ".").toLowerCase()) {
			case "jpg", "jpeg", "png", "jfif" -> type = ContentType.IMAGE;
			case "mp4", "mov", "webm", "flv" -> type = ContentType.VIDEO;
			case "gif" -> type = ContentType.GIF;
			default -> type = ContentType.CONTENT;
		}
		
		return type;
		
	}
	
	public static String getExtension(File file) {
		
		return Utils.getLastSubstring(file.toString(), ".");
	}
	
	public static File subdirectory(File root, File subdirectory) {
		
		return new File(root.toString() + '\\' + subdirectory.toString());
	}
	
	public static File createSubdirectory(File root, File subdirectory) {
		
		File file = subdirectory(root, subdirectory);
		file.mkdirs();
		return file;
		
	}
	
	public static File createRootSubdirectory(File subdirectory) {
		
		subdirectory.mkdirs();
		return subdirectory;
		
	}
	
	public static File[] getFilesWithSuffixNoExt(File directory, String suffix) {
		
		if (!directory.exists() || !directory.isDirectory())
			throw new InvalidParameterException("File needs to exist and be a directory");
		return directory.listFiles((dir, name) -> Utils.removeLastSubstring(name, ".").endsWith(suffix));
		
	}
	
	public static File getDirectoryWithFileChooser() {
		
		JFileChooser chooser = new JFileChooser(INGEST_DIR);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int returnVal = chooser.showOpenDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		else return INGEST_DIR;
		
	}
	
}
