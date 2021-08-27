/*
 *     MainFrame
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

package ca.bigcattech.MediaDB.gui.frames;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.core.Ingest;
import ca.bigcattech.MediaDB.core.Options;
import ca.bigcattech.MediaDB.core.Session;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.gui.forms.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainFrame extends JFrame {
	
	private static final Logger log = LoggerFactory.getLogger(MainFrame.class);
	private Session mSession;
	private IngestProgressForm mTempIngestProgressForm;
	private final JSpinner mColumnSpinner;
	
	private JFrame mHelpFrame;
	
	public MainFrame(Options options) {
		
		log.info("Setting up MainFrame");
		
		JMenuBar menuBar = new JMenuBar();
		JMenu jMenu = new JMenu();
		jMenu.setText("Options");
		
		menuBar.add(jMenu);
		
		mColumnSpinner = new JSpinner(new SpinnerNumberModel(options.getColumns(), 1, Integer.MAX_VALUE, 1));
		mColumnSpinner.addChangeListener(e -> mSession.getOptions().setColumns((Integer) mColumnSpinner.getValue()));
		
		JMenu searchOptions = new JMenu("Search Options");
		
		JCheckBoxMenuItem favoritesFirst = new JCheckBoxMenuItem("Favorites first");
		favoritesFirst.addActionListener(e -> mSession.getOptions().getSearchOptions().setFavoritesFirst(favoritesFirst.isSelected()));
		favoritesFirst.setSelected(options.getSearchOptions().isFavoritesFirst());
		
		JCheckBoxMenuItem reverseOrder = new JCheckBoxMenuItem("Reverse Order");
		reverseOrder.addActionListener(e -> mSession.getOptions().getSearchOptions().setReverseOrder(reverseOrder.isSelected()));
		reverseOrder.setSelected(options.getSearchOptions().isReverseOrder());
		
		searchOptions.add(favoritesFirst);
		searchOptions.add(reverseOrder);
		searchOptions.add(new JSeparator());
		searchOptions.add(new JLabel("Sort by: "));
		
		ButtonGroup searchModeButtonGroup = new ButtonGroup();
		for (Options.SearchOptions.SortType type : Options.SearchOptions.SortType.values()) {
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem(type.name());
			radioButton.addActionListener(e -> mSession.getOptions().getSearchOptions().setSearchType(Options.SearchOptions.SortType.valueOf(radioButton.getText())));
			if (options.getSearchOptions().getSearchType().equals(type)) radioButton.setSelected(true);
			searchModeButtonGroup.add(radioButton);
			searchOptions.add(radioButton);
		}
		
		JMenu ingestOptions = new JMenu("Ingest Options");
		JLabel purge = new JLabel("Purge Duplicates");
		purge.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				super.mouseClicked(e);
				if (mSession.getSessionState() == Session.SessionState.INGEST) return;
				JOptionPane.showMessageDialog(MainFrame.this, "Removing duplicate's in the Ingest folder. This will take a while.\nPress OK to continue.");
				mSession.getIngest().purgeDuplicates();
				if (mSession.getSessionState() == Session.SessionState.HOME) mSession.home();
			}
		});
		JLabel purgeDuplicateFolder = new JLabel("Purge from folder");
		purgeDuplicateFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				super.mouseClicked(e);
				if (mSession.getSessionState() == Session.SessionState.INGEST) return;
				File folder = FileSystemHandler.getDirectoryWithFileChooser();
				JOptionPane.showMessageDialog(MainFrame.this, "Removing duplicate's in the '" + folder.getName() + "' folder. This will take a while.\nPress OK to continue.");
				mSession.getIngest().purgeDuplicates(folder);
				if (mSession.getSessionState() == Session.SessionState.HOME) mSession.home();
				
			}
		});
		
		JLabel validateContentFolder = new JLabel("Validate Content Folder");
		validateContentFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				super.mouseClicked(e);
				mSession.getIngest().validateContentFolder();
			}
		});
		
		JCheckBoxMenuItem ingestTagFieldAuto = new JCheckBoxMenuItem("Auto Ingest Tag Field");
		ingestTagFieldAuto.setSelected(options.isIngestAutoTagField());
		ingestTagFieldAuto.addActionListener(e -> mSession.getOptions().setIngestAutoTagField(ingestTagFieldAuto.isSelected()));
		
		ingestOptions.add(ingestTagFieldAuto);
		ingestOptions.add(purge);
		ingestOptions.add(purgeDuplicateFolder);
		ingestOptions.add(validateContentFolder);
		
		JMenu contentView = new JMenu("Content View Options:");
		contentView.add("Slideshow timer (sec):");
		JSpinner slideshowTimerSpinner = new JSpinner(new SpinnerNumberModel(options.getSlideshowTimer(), 1, Integer.MAX_VALUE, 1));
		slideshowTimerSpinner.addChangeListener(e -> {
			mSession.getOptions().setSlideshowTimer((Integer) slideshowTimerSpinner.getValue());
			mSession.updateSlideShowTimer();
		});
		JLabel startSlideshow = new JLabel("Start Slideshow");
		startSlideshow.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				super.mouseClicked(e);
				mSession.startSlideShow();
			}
		});
		
		contentView.add(slideshowTimerSpinner);
		contentView.add(startSlideshow);
		
		JMenu migrate = new JMenu("Migrate");
		JButton migrateFileSeparators = new JButton("File Separators");
		migrateFileSeparators.addActionListener(e -> mSession.getDBHandler().manualMigrate());
		migrate.add(migrateFileSeparators);
		
		jMenu.add(searchOptions);
		jMenu.add(ingestOptions);
		jMenu.add(contentView);
		jMenu.add(migrate);
		jMenu.add(new JLabel("Result Columns"));
		jMenu.add(mColumnSpinner);
		
		JSpinner resultsPerPageSpin = new JSpinner(new SpinnerNumberModel(options.getResultsPerPage(), 0, Integer.MAX_VALUE, 1));
		resultsPerPageSpin.addChangeListener(e -> mSession.getOptions().setResultsPerPage((Integer) resultsPerPageSpin.getValue()));
		
		jMenu.add(new JLabel("Results Per Page"));
		jMenu.add(resultsPerPageSpin);
		
		JLabel helpButton = new JLabel("Help");
		helpButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				super.mouseClicked(e);
				if (mHelpFrame != null) {
					mHelpFrame.dispose();
				}
				mHelpFrame = new JFrame("MediaDB: Help");
				HelpForm form = new HelpForm();
				mHelpFrame.setContentPane(form.mContentPanel);
				mHelpFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				mHelpFrame.setSize(900, 600);
				mHelpFrame.setLocationRelativeTo(null);
				mHelpFrame.setVisible(true);
			}
		});
		menuBar.add(helpButton);
		
		this.setJMenuBar(menuBar);
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				FileSystemHandler.writeOptions(mSession.getOptions());
				System.exit(0);
			}
		});
		this.setLocationRelativeTo(null);
		this.setSize(1200, 800);
		this.setVisible(true);
		
		log.info("Done!");
		
	}
	
	public void setBackgroundColor(Color color) {
		
		for (Component component : this.getRootPane().getComponents()) {
			component.setForeground(color);
			component.setBackground(color);
		}
		
		this.revalidate();
		this.repaint();
		
	}
	
	public void setSession(Session session) {
		
		mSession = session;
		mColumnSpinner.setValue(session.getOptions().getColumns());
	}
	
	public void displayIngest(boolean update) {
		
		long start = System.currentTimeMillis();
		if (!update || mTempIngestProgressForm == null) mTempIngestProgressForm = new IngestProgressForm(mSession);
		mTempIngestProgressForm.update();
		try {
			this.setContentPane(mTempIngestProgressForm.mContent);
		}
		catch (IllegalArgumentException e) {
			mSession.ingest();
			//JOptionPane.showMessageDialog(this.getRootPane(), "Ingest Illegal Component pos err, sorry for the inconvenience.");
		}
		this.revalidate();
		this.repaint();
		log.info("Time for ingest display: " + (System.currentTimeMillis() - start) + "ms");
		
	}
	
	public void displayIngestTask(Ingest.IngestTask task) {
		
		IngestForm form = new IngestForm(task, mSession);
		mSession.setKeyListener(form);
		this.setContentPane(form.mContentPanel);
		this.revalidate();
		this.repaint();
		//form.playContent();
		
	}
	
	public DisplayContentForm displayContent(Content content) {
		
		mTempIngestProgressForm = null; //Just cleaning this up so it's not sticking around when we dont need it
		
		DisplayContentForm displayContentForm = new DisplayContentForm(content, mSession);
		this.setContentPane(displayContentForm.mContentPannel);
		this.revalidate();
		this.repaint();
		//displayContentForm.playContent();
		
		return displayContentForm;
		
	}
	
	public ContentResultsForm displaySearchResults() {
		
		mTempIngestProgressForm = null; //Just cleaning this up so it's not sticking around when we dont need it
		
		ContentResultsForm contentResultsForm = new ContentResultsForm(mSession);
		
		this.setContentPane(contentResultsForm.mContent);
		this.revalidate();
		this.repaint();
		
		contentResultsForm.updateScrollPos();
		
		return contentResultsForm;
		
	}
	
	public void displayHome() {
		
		mTempIngestProgressForm = null; //Just cleaning this up so it's not sticking around when we dont need it
		
		TitleSearchForm titleSearchForm = new TitleSearchForm(mSession);
		this.setContentPane(titleSearchForm.mContent);
		this.revalidate();
		this.repaint();
		
	}
	
	public void displayTagManagement() {
		
		TagsManagementForm tagsManagementForm = new TagsManagementForm(mSession);
		this.setContentPane(tagsManagementForm.mContent);
		this.revalidate();
		this.repaint();
		
	}
	
}
