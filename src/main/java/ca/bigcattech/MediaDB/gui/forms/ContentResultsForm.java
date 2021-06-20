/*
 *     ContentResultsForm
 *     Last Modified: 2021-06-19, 9:59 p.m.
 *     Copyright (C) 2021-06-19, 9:59 p.m.  CameronBarnes
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

import ca.bigcattech.MediaDB.core.Session;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.content.ContentComparator;
import ca.bigcattech.MediaDB.gui.components.AutoCompleteTextField;
import ca.bigcattech.MediaDB.gui.components.ContentButton;
import ca.bigcattech.MediaDB.gui.interfaces.IKeyListener;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ContentResultsForm implements IKeyListener {
	
	private static final Logger log = LoggerFactory.getLogger(ContentResultsForm.class.getName());
	public JPanel mContent;
	private JButton mSearchButton;
	private AutoCompleteTextField mSearchBar;
	private JButton mHomeButton;
	private JPanel mContentResultPannel;
	private JScrollPane mScrollPane;
	private JList<String> mSearchTagList;
	private JButton mPageLeft;
	private JButton mPageRight;
	private JLabel mPageLabel;
	private final Session mSession;
	
	public ContentResultsForm(Session session) {
		
		mSession = session;
		
		$$$setupUI$$$();
		
		mScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		mHomeButton.addActionListener(e -> {
			mSession.home();
		});
		mSearchButton.addActionListener(e -> {
			mSession.search(mSearchBar.getText());
		});
		mSearchBar.addActionListener(e -> {
			mSession.search(mSearchBar.getText());
		});
		
		long start = System.currentTimeMillis();
		loadContent(mSession.getResults());
		log.info((System.currentTimeMillis() - start) + "ms to add content buttons");
		
		setupTagListWContentTags();
		
		mSearchBar.setDBHandler(mSession.getDBHandler());
		mSearchBar.setDictionary(mSession.getDictionary());
		updateSearchBarText();
		
		mSearchTagList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				
				JList<String> list = (JList<String>) evt.getSource();
				if (evt.getClickCount() == 2) {
					// Double-click detected
					
					Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
					if (r != null && r.contains(evt.getPoint())) {
						
						//Left mouse button
						if (evt.getButton() == MouseEvent.BUTTON1) {
							
							String selectedTag = mSearchTagList.getSelectedValue().split(":")[0];
							if (!Arrays.asList(mSession.getSearchTags()).contains(selectedTag)) {
								ArrayList<String> newSearch = new ArrayList<>(Arrays.asList(mSession.getSearchTags()));
								newSearch.add(selectedTag);
								mSession.search(newSearch.toArray(new String[]{}), mSession.getSearchTagsBlacklist());
							}
							
						}
						else if (evt.getButton() == MouseEvent.BUTTON2) {
							
							String selectedTag = mSearchTagList.getSelectedValue().split(":")[0];
							if (!Arrays.asList(mSession.getSearchTags()).contains(selectedTag)) {
								ArrayList<String> newBlackList = new ArrayList<>(Arrays.asList(mSession.getSearchTagsBlacklist()));
								newBlackList.add(selectedTag);
								mSession.search(mSession.getSearchTags(), newBlackList.toArray(new String[]{}));
							}
							
						}
						
					}
					
				}
			}
		});
		
		if (mSession.getOptions().getResultsPerPage() == 0) {
			mPageLabel.setText("Page 1 of 1");
			mPageLeft.setEnabled(false);
			mPageRight.setEnabled(false);
		}
		else {
			mPageLabel.setText("Page " + (mSession.getResultPage() + 1) + " of " + mSession.getNumResultPages());
			if (mSession.getResultPage() == 0) mPageLeft.setEnabled(false);
			else if (mSession.getResultPage() == mSession.getNumResultPages() - 1) mPageRight.setEnabled(false);
		}
		
		mPageLeft.addActionListener(e -> {
			mSession.setResultPage(mSession.getResultPage() - 1);
			mSession.displaySession();
		});
		
		mPageRight.addActionListener(e -> {
			mSession.setResultPage(mSession.getResultPage() + 1);
			mSession.displaySession();
		});
		
		changeLayoutFromNumResults();
		
	}
	
	public void updateScrollPos() {
		
		log.info("Scroll bar stored value was: " + mSession.getScrollBarTempNum());
		mScrollPane.getVerticalScrollBar().setValue(mSession.getScrollBarTempNum());
		
	}
	
	private void updateSearchBarText() {
		
		StringBuilder str = new StringBuilder();
		
		for (String tag : mSession.getSearchTags()) {
			str.append(tag).append(' ');
		}
		
		if (mSession.getSearchTagsBlacklist().length > 0)
			str.append("| blacklist >>> | ");
		
		for (String banedTag : mSession.getSearchTagsBlacklist()) {
			str.append(banedTag).append(' ');
		}
		
		mSearchBar.setText(str.toString().trim());
		
	}
	
	@Deprecated
	private void addContent(Content content) {
		
		ContentButton contentButton = new ContentButton(content);
		contentButton.addActionListener(e -> {
			mSession.content(content);
			mSession.setScrollBarTempNum(mScrollPane.getVerticalScrollBar().getValue());
		});
		mContentResultPannel.add(contentButton);
		
	}
	
	private void loadContent(Content[] contentArr) {
		
		ConcurrentLinkedQueue<ContentButton> buttons = new ConcurrentLinkedQueue<>();
		
		if (mSession.getOptions().getResultsPerPage() != 0) {
			int startIndex = mSession.getResultPage() * mSession.getOptions().getResultsPerPage();
			if (mSession.getResultPage() == mSession.getNumResultPages() - 1) {
				contentArr = Arrays.asList(contentArr).subList(startIndex, contentArr.length - 1).toArray(new Content[]{});
			}
			else {
				contentArr = Arrays.asList(contentArr).subList(startIndex, startIndex + mSession.getOptions().getResultsPerPage()).toArray(new Content[]{});
			}
		}
		
		long start = System.currentTimeMillis();
		Arrays.stream(contentArr).parallel().forEach(content -> buttons.add(createButton(content)));
		ContentButton[] lastStep = buttons.toArray(new ContentButton[0]);
		log.info("Creating button objects took: " + (System.currentTimeMillis() - start) + "ms");
		
		start = System.currentTimeMillis();
		ContentComparator comparator = new ContentComparator(mSession.getOptions().getSearchOptions());
		Arrays.sort(lastStep, (a, b) -> comparator.compare(a.getContent(), b.getContent()));
		log.info((System.currentTimeMillis() - start) + "ms to sort content buttons");
		
		start = System.currentTimeMillis();
		Arrays.stream(lastStep).forEach(contentButton -> mContentResultPannel.add(contentButton));
		log.info((System.currentTimeMillis() - start) + "ms to add content buttons to the results panel");
		
		new Thread(() -> buttons.stream().parallel().forEach(ContentButton::displayThumbnail)).start();
		
	}
	
	private ContentButton createButton(Content content) {
		
		ContentButton contentButton = new ContentButton(content);
		contentButton.addActionListener(e -> {
			mSession.content(content);
			mSession.setScrollBarTempNum(mScrollPane.getVerticalScrollBar().getValue());
		});
		return contentButton;
		
	}
	
	private void setupTagListWContentTags() {
		
		TreeMap<String, Long> tagMap = new TreeMap<>();
		
		for (Content content : mSession.getResults()) {
			
			for (String tag : content.getTags()) {
				
				if (tagMap.containsKey(tag)) continue;
				
				tagMap.put(tag, mSession.getDBHandler().countContentWithTag(tag));
				
			}
			
		}
		
		Iterator<Entry<String, Long>> iterator = tagMap.entrySet().stream().sorted(Entry.<String, Long>comparingByValue().reversed()).iterator();
		
		while (iterator.hasNext()) {
			
			Entry<String, Long> entry = iterator.next();
			((DefaultListModel<String>) mSearchTagList.getModel()).addElement(entry.getKey() + ": " + entry.getValue());
			
		}
		
	}
	
	private void setupTagListWSearchTags() {
		
		TreeMap<String, Long> tagMap = new TreeMap<>();
		
		for (String tag : mSession.getSearchTags()) {
			
			tagMap.put(tag, mSession.getDBHandler().countContentWithTag(tag));
			
		}
		
		Iterator<Entry<String, Long>> iterator = tagMap.entrySet().stream().sorted(Entry.<String, Long>comparingByValue().reversed()).iterator();
		
		while (iterator.hasNext()) {
			
			Entry<String, Long> entry = iterator.next();
			((DefaultListModel<String>) mSearchTagList.getModel()).addElement(entry.getKey() + ": " + entry.getValue());
			
		}
		
	}
	
	public void changeLayoutFromNumResults() {
		
		mContentResultPannel.setLayout(new GridLayout(0, mSession.getOptions().getColumns()));
		
	}
	
	private void createUIComponents() {
		
		mContentResultPannel = new JPanel(new GridLayout(0, mSession.getOptions().getColumns()));
		mSearchTagList = new JList<>(new DefaultListModel<>());
		mSearchTagList.setDragEnabled(false);
		mSearchBar = new AutoCompleteTextField();
		mSearchBar.setFocusTraversalKeysEnabled(false);
		
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
		mContent.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
		mSearchButton = new JButton();
		mSearchButton.setText("Search");
		mContent.add(mSearchButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		mContent.add(spacer1, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mContent.add(mSearchBar, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		mHomeButton = new JButton();
		mHomeButton.setText("Home");
		mContent.add(mHomeButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mScrollPane = new JScrollPane();
		mScrollPane.setAutoscrolls(false);
		mScrollPane.setHorizontalScrollBarPolicy(31);
		mScrollPane.setVerticalScrollBarPolicy(22);
		mContent.add(mScrollPane, new GridConstraints(1, 2, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		mScrollPane.setViewportView(mContentResultPannel);
		final JScrollPane scrollPane1 = new JScrollPane();
		mContent.add(scrollPane1, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(400, -1), new Dimension(400, -1), 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		scrollPane1.setViewportView(panel1);
		panel1.add(mSearchTagList, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		mContent.add(panel2, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		mPageLeft = new JButton();
		mPageLeft.setText("<<<");
		panel2.add(mPageLeft, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel2.add(spacer2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mPageRight = new JButton();
		mPageRight.setText(">>>");
		panel2.add(mPageRight, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mPageLabel = new JLabel();
		mPageLabel.setText("Page 1 of 1");
		panel2.add(mPageLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		mContent.add(spacer3, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return mContent;
	}
	
	@Override
	public void keyTyped(KeyEvent e, boolean control) {
	
	}
	
	@Override
	public void keyPressed(KeyEvent e, boolean control) {
	
	}
	
	@Override
	public void keyReleased(KeyEvent e, boolean control) {
		
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			mPageRight.doClick();
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			mPageLeft.doClick();
		}
	}
	
}
