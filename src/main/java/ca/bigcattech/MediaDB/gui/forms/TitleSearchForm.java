/*
 *     TitleSearchForm
 *     Last Modified: 2021-08-02, 10:54 a.m.
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

package ca.bigcattech.MediaDB.gui.forms;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.core.Session;
import ca.bigcattech.MediaDB.gui.components.AutoCompleteTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class TitleSearchForm {
	
	public AutoCompleteTextField mSearchField;
	public JButton mSearchButton;
	public JPanel mContent;
	private JButton mIngestButton;
	private JCheckBox mSearchPrivate;
	private JButton mTagsButton;
	private JButton mIngestDir;
	private JLabel mNumTags;
	private JLabel mNumContentToIngest;
	private JLabel mNumContentInDatabase;
	private JCheckBox mOther;
	private JCheckBox mGIFs;
	private JCheckBox mVideos;
	private JCheckBox mImages;
	private JRadioButton mPoolsRadio;
	private JRadioButton mContentRadio;
	
	private final Session mSession;
	
	public TitleSearchForm(Session session) {
		
		mSession = session;
		$$$setupUI$$$();
		
		mNumTags.setText(mSession.getDBHandler().getNumTags() + " Tags in Database.");
		mNumContentInDatabase.setText(mSession.getDBHandler().getNumContent() + " Content in Database.");
		mNumContentToIngest.setText(FileSystemHandler.INGEST_DIR.listFiles().length + " Files to Ingest.");
		
		//Set the font so that we have unicode support
		Font unicodeFont = FileSystemHandler.getUnicodeFont();
		if (unicodeFont != null) {
			mIngestDir.setFont(unicodeFont);
			mIngestDir.setText("\uf07c");
		}
		else mIngestDir.setText("Folder");
		
		//Initializing checkbox values here
		mSearchPrivate.setSelected(session.getOptions().getSearchOptions().isRestricted());
		mImages.setSelected(session.getOptions().getSearchOptions().isImages());
		mVideos.setSelected(session.getOptions().getSearchOptions().isVideos());
		mGIFs.setSelected(session.getOptions().getSearchOptions().isGIFs());
		mOther.setSelected(session.getOptions().getSearchOptions().isOther());
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(mContentRadio);
		buttonGroup.add(mPoolsRadio);
		
		buttonGroup.clearSelection();
		mContentRadio.setSelected(true);
		
		//Action listeners go bellow here
		mSearchField.addActionListener(e -> search());
		mSearchButton.addActionListener(e -> search());
		
		mIngestButton.addActionListener(e -> mSession.ingest());
		mIngestDir.addActionListener(e -> mSession.ingest(FileSystemHandler.getDirectoryWithFileChooser()));
		mTagsButton.addActionListener(e -> mSession.tags());
		
		mSearchField.setDBHandler(mSession.getDBHandler());
		mSearchField.setDictionary(mSession.getDictionary());
		
		mSearchPrivate.addActionListener(e -> mSession.getOptions().getSearchOptions().setRestricted(mSearchPrivate.isSelected()));
		mImages.addActionListener(e -> mSession.getOptions().getSearchOptions().setImages(mImages.isSelected()));
		mVideos.addActionListener(e -> mSession.getOptions().getSearchOptions().setVideos(mVideos.isSelected()));
		mGIFs.addActionListener(e -> mSession.getOptions().getSearchOptions().setGIFs(mGIFs.isSelected()));
		mOther.addActionListener(e -> mSession.getOptions().getSearchOptions().setOther(mOther.isSelected()));
		
	}
	
	private void search() {
		
		if (mContentRadio.isSelected()) {
			mSession.searchContent(mSearchField.getText().toLowerCase(Locale.ROOT));
		}
		else {
			//TODO search pools
		}
		
	}
	
	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		
		createUIComponents();
		mContent = new JPanel();
		mContent.setLayout(new GridLayoutManager(6, 16, new Insets(0, 0, 0, 0), -1, -1));
		final Spacer spacer1 = new Spacer();
		mContent.add(spacer1, new GridConstraints(1, 3, 1, 8, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		mContent.add(spacer2, new GridConstraints(5, 3, 1, 8, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		mSearchField.setToolTipText("Enter tags here");
		mContent.add(mSearchField, new GridConstraints(3, 3, 1, 8, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final Spacer spacer3 = new Spacer();
		mContent.add(spacer3, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mSearchButton = new JButton();
		mSearchButton.setText("Search");
		mContent.add(mSearchButton, new GridConstraints(3, 11, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mIngestButton = new JButton();
		mIngestButton.setText("Ingest");
		mContent.add(mIngestButton, new GridConstraints(0, 14, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
		final Spacer spacer4 = new Spacer();
		mContent.add(spacer4, new GridConstraints(0, 2, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mSearchPrivate = new JCheckBox();
		mSearchPrivate.setText("Search Private");
		mContent.add(mSearchPrivate, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mTagsButton = new JButton();
		mTagsButton.setText("Tags");
		mContent.add(mTagsButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
		mIngestDir = new JButton();
		Font mIngestDirFont = this.$$$getFont$$$(null, Font.PLAIN, -1, mIngestDir.getFont());
		if (mIngestDirFont != null) mIngestDir.setFont(mIngestDirFont);
		mIngestDir.setText("Folder");
		mContent.add(mIngestDir, new GridConstraints(0, 15, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mNumTags = new JLabel();
		mNumTags.setText("Num Tags");
		mContent.add(mNumTags, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mNumContentToIngest = new JLabel();
		mNumContentToIngest.setText("Num to ingest");
		mContent.add(mNumContentToIngest, new GridConstraints(0, 13, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mNumContentInDatabase = new JLabel();
		mNumContentInDatabase.setText("Num Content");
		mContent.add(mNumContentInDatabase, new GridConstraints(2, 3, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer5 = new Spacer();
		mContent.add(spacer5, new GridConstraints(3, 12, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mGIFs = new JCheckBox();
		mGIFs.setText("GIFs");
		mContent.add(mGIFs, new GridConstraints(4, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mVideos = new JCheckBox();
		mVideos.setText("Videos");
		mContent.add(mVideos, new GridConstraints(4, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mImages = new JCheckBox();
		mImages.setText("Images");
		mContent.add(mImages, new GridConstraints(4, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mPoolsRadio = new JRadioButton();
		mPoolsRadio.setText("Pools");
		mContent.add(mPoolsRadio, new GridConstraints(2, 10, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mContentRadio = new JRadioButton();
		mContentRadio.setSelected(true);
		mContentRadio.setText("Content");
		mContent.add(mContentRadio, new GridConstraints(2, 9, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer6 = new Spacer();
		mContent.add(spacer6, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mOther = new JCheckBox();
		mOther.setText("Other");
		mContent.add(mOther, new GridConstraints(4, 8, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer7 = new Spacer();
		mContent.add(spacer7, new GridConstraints(1, 15, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
	}
	
	/**
	 * @noinspection ALL
	 */
	private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
		
		if (currentFont == null) return null;
		String resultName;
		if (fontName == null) {
			resultName = currentFont.getName();
		}
		else {
			Font testFont = new Font(fontName, Font.PLAIN, 10);
			if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
				resultName = fontName;
			}
			else {
				resultName = currentFont.getName();
			}
		}
		Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
		boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
		Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
		return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return mContent;
	}
	
	public void createUIComponents() {
		
		mSearchField = new AutoCompleteTextField();
		mSearchField.setFocusTraversalKeysEnabled(false);
		
	}
	
}
