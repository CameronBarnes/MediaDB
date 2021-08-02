/*
 *     Main
 *     Last Modified: 2021-08-02, 5:57 a.m.
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

package ca.bigcattech.MediaDB.core;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.db.MongoDBHandler;
import ca.bigcattech.MediaDB.gui.frames.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		
		log.info("Starting up");
		
		FileSystemHandler.init();
		
		DBHandler dbHandler = new MongoDBHandler("media_db", "127.0.0.1");
		dbHandler.initDB();
		
		Ingest ingest = new Ingest(dbHandler);
		
		Options options = FileSystemHandler.getOptions();
		MainFrame mainFrame = new MainFrame(options);
		Session session = new Session(mainFrame, dbHandler, ingest, options);
		
		session.displaySession();
		
		log.info("Init finish");
		
	}
	
}
