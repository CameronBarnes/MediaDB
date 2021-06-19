package ca.bigcattech.MediaDB.core;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.gui.frames.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		
		log.info("Starting up");
		
		FileSystemHandler.init();
		
		DBHandler dbHandler = new DBHandler("media_db", "127.0.0.1");
		dbHandler.initDB();
		
		Ingest ingest = new Ingest(dbHandler);
		
		Options options = FileSystemHandler.getOptions();
		MainFrame mainFrame = new MainFrame(options);
		Session session = new Session(mainFrame, dbHandler, ingest, options);
		
		session.displaySession();
		
		log.info("Init finish");
		
	}
	
}
