/*
 *     DisplayContentForm
 *     Last Modified: 2021-07-03, 2:22 a.m.
 *     Copyright (C) 2021-07-03, 2:22 a.m.  CameronBarnes
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
import ca.bigcattech.MediaDB.IO.FileTransferable;
import ca.bigcattech.MediaDB.IO.ImageSelection;
import ca.bigcattech.MediaDB.core.Session;
import ca.bigcattech.MediaDB.db.Tag;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.gui.components.AutoCompleteTextField;
import ca.bigcattech.MediaDB.gui.interfaces.IKeyListener;
import ca.bigcattech.MediaDB.utils.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MarqueePosition;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static javax.swing.SwingConstants.HORIZONTAL;

public class DisplayContentForm implements IKeyListener {
	
	private static final Logger log = LoggerFactory.getLogger(DisplayContentForm.class);
	
	public JPanel mContentPannel;
	private AutoCompleteTextField mSearch;
	private JButton mButtonSearch;
	private JList<String> mTags;
	private JTextArea mDescription;
	private JLabel mTitle;
	private JLabel mViews;
	private JCheckBox mPrivate;
	private JButton mButtonBack;
	private JButton mButtonHome;
	private JPanel mMediaPlayerPanel;
	private AutoCompleteTextField mAddTagField;
	private JScrollPane mScrollPane;
	private JPanel mControlsPanel;
	private JButton mButtonPrevious;
	private JButton mButtonNext;
	private JPanel mVideoControlsPanel;
	private JCheckBox mFavoriteCheckBox;
	private JButton mDelete;
	private JButton mCopy;
	private JLabel mTimeSpent;
	
	private JButton mSkipButton;
	private JButton mRewindButton;
	
	private final Content mContent;
	private final CallbackMediaPlayerComponent mMediaPlayerComponent;
	private final Session mSession;
	private ArrayList<String> mDictionary;
	
	private long mStartTime = 0L;
	
	public DisplayContentForm(Content content, Session session) {
		
		mContent = content;
		mSession = session;
		$$$setupUI$$$();
		
		addTag(content.getTags());
		mDescription.setText(content.getDescription());
		mTitle.setText(content.getTitle());
		mViews.setText("Views: " + mContent.incrementViews());
		mPrivate.setSelected(mContent.isPrivate());
		mFavoriteCheckBox.setSelected(mContent.isFavorite());
		mTimeSpent.setText("Watch Time: " + Utils.longToStrTime(mContent.getTimeSpent()));
		
		if (mContent.getType() != ContentType.GIF) {
			mMediaPlayerComponent = new CallbackMediaPlayerComponent();
			mMediaPlayerPanel.add(mMediaPlayerComponent, BorderLayout.CENTER);
			
			mMediaPlayerComponent.mediaPlayer().marquee().enable(true);
			mMediaPlayerComponent.mediaPlayer().marquee().setPosition(MarqueePosition.BOTTOM);
			mMediaPlayerComponent.mediaPlayer().controls().setRepeat(true);
			
			mMediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
				@Override
				public void error(MediaPlayer mediaPlayer) {
					
					release();
				}
			});
			
		}
		else {
			mMediaPlayerComponent = null;
			mMediaPlayerPanel.add(new JLabel(new ImageIcon(mContent.getFile().getPath())));
		}
		
		//TODO temp code, change this
		if (mContent.getType() == ContentType.VIDEO && mMediaPlayerComponent != null) {
			
			mRewindButton = new JButton("Rewind");
			mRewindButton.addActionListener(e -> mMediaPlayerComponent.mediaPlayer().controls().skipTime(-10000));
			mVideoControlsPanel.add(mRewindButton, BorderLayout.WEST);
			JButton pauseButton = new JButton("Pause");
			pauseButton.addActionListener(e -> mMediaPlayerComponent.mediaPlayer().controls().pause());
			mVideoControlsPanel.add(pauseButton, BorderLayout.CENTER);
			mSkipButton = new JButton("Skip");
			mSkipButton.addActionListener(e -> mMediaPlayerComponent.mediaPlayer().controls().skipTime(10000));
			
			pauseButton.addActionListener(e -> mMediaPlayerComponent.mediaPlayer().controls().pause());
			mVideoControlsPanel.add(mSkipButton, BorderLayout.EAST);
			
			final int maxPos = 1000;
			final boolean[] manualChangeBar = {false};
			JSlider progress = new JSlider(HORIZONTAL, 0, maxPos, 0);
			progress.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					
					super.mouseClicked(e);
					Rectangle r = progress.getBounds();
					float pos = e.getX() / ((float) r.width);
					if (pos > 1) pos = 1f;
					else if (pos < 0) pos = 0f;
					progress.setValue((int) (maxPos * pos));
					mMediaPlayerComponent.mediaPlayer().controls().setPosition(progress.getValue() / (float) maxPos);
					manualChangeBar[0] = true;
				}
			});
			
			JLabel currTime = new JLabel("00:00");
			JLabel length = new JLabel("00:00");
			
			mMediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
				@Override
				public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
					
					if (!manualChangeBar[0])
						progress.setValue(Math.min(maxPos, Math.round(newPosition * (float) maxPos)));
					else manualChangeBar[0] = false;
					if (length.getText().equals("00:00"))
						length.setText(Utils.longToStrTime(mediaPlayer.status().length()));
					currTime.setText(Utils.longToStrTime(mediaPlayer.status().time()));
					
					if (mContent.getVideoLength() == 0)
						mContent.setVideoLength(mMediaPlayerComponent.mediaPlayer().status().length());
					
				}
			});
			
			mMediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
				@Override
				public void finished(MediaPlayer mediaPlayer) {
					
					if (mSession.isSlideshow()) {
						next(false);
					}
				}
			});
			
			JSlider volume = new JSlider(HORIZONTAL, 0, 100, mSession.getVolume());
			volume.addChangeListener(e -> {
				mSession.setVolume(volume.getValue());
				mMediaPlayerComponent.mediaPlayer().audio().setVolume(mSession.getVolume());
			});
			volume.setMajorTickSpacing(25);
			volume.setMinorTickSpacing(5);
			volume.setPaintTicks(true);
			volume.setSnapToTicks(true);
			
			JPanel volumePanel = new JPanel();
			volumePanel.add(new JLabel("Volume:"), BorderLayout.NORTH);
			volumePanel.add(volume, BorderLayout.SOUTH);
			
			JPanel progressPanel = new JPanel(new BorderLayout());
			progressPanel.add(progress, BorderLayout.CENTER);
			progressPanel.add(currTime, BorderLayout.WEST);
			progressPanel.add(length, BorderLayout.EAST);
			progressPanel.add(volumePanel, BorderLayout.EAST);
			
			mVideoControlsPanel.add(progressPanel, BorderLayout.NORTH);
			
			//Set the font so that we have unicode support
			Font unicodeFont = FileSystemHandler.getUnicodeFont();
			if (unicodeFont != null) {
				mRewindButton.setFont(unicodeFont);
				mRewindButton.setText("\uf049");
				pauseButton.setFont(unicodeFont);
				pauseButton.setText("\uf04b\uf04c");
				mSkipButton.setFont(unicodeFont);
				mSkipButton.setText("\uf050");
			}
			
		}
		
		playContent();
		
		mDictionary = mSession.getDictionary();
		mDictionary.removeAll(Arrays.asList(mContent.getTags()));
		mAddTagField.setDBHandler(mSession.getDBHandler());
		mAddTagField.setDictionary(mDictionary);
		
		mSearch.setDBHandler(mSession.getDBHandler());
		mSearch.setDictionary(mSession.getDictionary());
		
		
		mAddTagField.addActionListener(e -> {
			addTag(mAddTagField.getText().toLowerCase().split(" "));
			mAddTagField.setText("");
		});
		
		mScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		mButtonHome.addActionListener(e -> {
			mSession.home();
			exit();
		});
		
		mButtonBack.addActionListener(e -> {
			mSession.back();
			exit();
		});
		
		mSearch.addActionListener(e -> {
			mSession.search(mSearch.getText());
			exit();
		});
		
		mButtonSearch.addActionListener(e -> {
			mSession.search(mSearch.getText());
			exit();
		});
		
		mDescription.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				
				super.keyTyped(e);
				mContent.setDescription(mDescription.getText());
			}
		});
		
		mPrivate.addActionListener(e -> mContent.setPrivate(mPrivate.isSelected()));
		mFavoriteCheckBox.addActionListener(e -> mContent.setFavorite(mFavoriteCheckBox.isSelected()));
		
		int temp = Arrays.asList(mSession.getResults()).indexOf(mContent);
		if (temp == 0) mButtonPrevious.setEnabled(false);
		mButtonPrevious.addActionListener(e -> {
			int index = Arrays.asList(mSession.getResults()).indexOf(mContent);
			if (index == 0) return;
			exit();
			mSession.content(mSession.getResults()[index - 1]);
		});
		
		if (temp == mSession.getResults().length - 1) mButtonNext.setEnabled(false);
		mButtonNext.addActionListener(e -> {
			int index = Arrays.asList(mSession.getResults()).indexOf(mContent);
			if (index == mSession.getResults().length) return;
			exit();
			mSession.content(mSession.getResults()[index + 1]);
		});
		
		mTags.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				
				super.mouseClicked(evt);
				
				JList<String> list = (JList<String>) evt.getSource();
				if (evt.getClickCount() == 2) {
					// Double-click detected
					
					Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
					if (evt.getButton() == MouseEvent.BUTTON1 && r != null && r.contains(evt.getPoint())) {
						
						String selectedTag = mTags.getSelectedValue().split(":")[0];
						mContent.removeTag(selectedTag);
						
						((DefaultListModel<String>) mTags.getModel()).removeElement(selectedTag);
						
					}
					
				}
				
			}
		});
		
		mDelete.addActionListener(e -> {
			
			if (JOptionPane.showConfirmDialog(mContentPannel, "Delete Content ?") != JOptionPane.YES_OPTION)
				return;
			
			mSession.getDBHandler().deleteContent(mContent.getHash());
			
			mSession.home();
			
			if (mMediaPlayerComponent != null) {
				mMediaPlayerComponent.mediaPlayer().submit(() -> {
					
					mMediaPlayerComponent.mediaPlayer().controls().stop();
					mMediaPlayerComponent.mediaPlayer().release();
					
					try {
						Files.delete(Path.of(mContent.getFile().getAbsolutePath()));
						Files.delete(Path.of(mContent.getThumbnailFile()));
					}
					catch (IOException ioException) {
						log.error("Failed to delete file from content directory", ioException);
					}
					
				});
			}
			else {
				try {
					Files.delete(Path.of(mContent.getFile().getAbsolutePath()));
				}
				catch (IOException ioException) {
					log.error("Failed to delete file from content directory", ioException);
				}
			}
			
		});
		
		mCopy.addActionListener(e -> {
			Transferable transferable;
			if (mContent.mType == ContentType.IMAGE) {
				transferable = new ImageSelection(mContent.getFile());
			}
			else {
				transferable = new FileTransferable(mContent.getFile());
			}
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
		});
		
	}
	
	public void playContent() {
		
		mStartTime = System.currentTimeMillis();
		
		if (mMediaPlayerComponent != null)
			mMediaPlayerComponent.mediaPlayer().submit(() -> {
				mMediaPlayerComponent.mediaPlayer().media().play(mContent.getFile().getAbsolutePath());
				mMediaPlayerComponent.mediaPlayer().audio().setVolume(mSession.getVolume());
			});
		
	}
	
	private void addTag(String[] tags) {
		
		if (mDictionary == null) mDictionary = mSession.getDictionary();
		
		ArrayList<String> tagsToAdd = new ArrayList<>(Arrays.asList(tags));
		mDictionary.removeAll(tagsToAdd);
		
		for (String name : tags) {
			
			Tag tag = mSession.getDBHandler().getTagFromName(name);
			if (tag == null) continue;
			
			tagsToAdd.addAll(Arrays.asList(tag.getParentTags()));
			
			addTag(tag.getParentTags());
			
		}
		
		for (String tag : tagsToAdd) {
			
			if (!((DefaultListModel<String>) mTags.getModel()).contains(tag))
				((DefaultListModel<String>) mTags.getModel()).addElement(tag);
			
		}
		
		String[] out = tagsToAdd.toArray(new String[]{});
		
		mSession.addTagToDictionary(out);
		mContent.addTags(out);
		
	}
	
	public void exit() {
		
		mContent.incrementTimeSpent(System.currentTimeMillis() - mStartTime);
		
		try {
			mSession.getDBHandler().exportContent(mContent);
		}
		catch (Content.ContentValidationException e) {
			e.printStackTrace();
		}
		release();
		
	}
	
	public void release() {
		
		if (mMediaPlayerComponent == null) return;
		// It is not allowed to call back into LibVLC from an event handling thread, so submit() is used
		mMediaPlayerComponent.mediaPlayer().submit(() -> mMediaPlayerComponent.mediaPlayer().controls().stop());
		mMediaPlayerComponent.mediaPlayer().submit(() -> mMediaPlayerComponent.mediaPlayer().release());
		
	}
	
	private void createUIComponents() {
		
		mTags = new JList<>(new DefaultListModel<>());
		mTags.setDragEnabled(false);
		mAddTagField = new AutoCompleteTextField();
		mAddTagField.setFocusTraversalKeysEnabled(false);
		mSearch = new AutoCompleteTextField();
		mSearch.setFocusTraversalKeysEnabled(false);
		
	}
	
	public boolean next(boolean slideShowCall) {
		
		if (mButtonNext.isEnabled()) {
			if (!slideShowCall && mSession.isSlideshow()) mSession.resetSlideShowTimer();
			mButtonNext.doClick();
			return true;
		}
		return false;
	}
	
	public boolean previous(boolean slideShowCall) {
		
		if (mButtonPrevious.isEnabled()) {
			if (!slideShowCall && mSession.isSlideshow()) mSession.resetSlideShowTimer();
			mButtonPrevious.doClick();
			return true;
		}
		return false;
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
		mContentPannel = new JPanel();
		mContentPannel.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
		mButtonSearch = new JButton();
		mButtonSearch.setText("Search");
		mContentPannel.add(mButtonSearch, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mContentPannel.add(mSearch, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		mScrollPane = new JScrollPane();
		mScrollPane.setAutoscrolls(false);
		mScrollPane.setHorizontalScrollBarPolicy(30);
		mScrollPane.setVerticalScrollBarPolicy(22);
		mContentPannel.add(mScrollPane, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(9, 3, new Insets(0, 0, 0, 0), -1, -1));
		mScrollPane.setViewportView(panel1);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(4, 5, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel2, new GridConstraints(7, 1, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		mTitle = new JLabel();
		mTitle.setText("Title");
		panel2.add(mTitle, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mDescription = new JTextArea();
		mDescription.setEditable(true);
		mDescription.setEnabled(true);
		panel2.add(mDescription, new GridConstraints(1, 0, 2, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		mViews = new JLabel();
		mViews.setText("Views: ");
		panel2.add(mViews, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mFavoriteCheckBox = new JCheckBox();
		mFavoriteCheckBox.setText("Favorite");
		panel2.add(mFavoriteCheckBox, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mDelete = new JButton();
		mDelete.setText("Delete");
		panel2.add(mDelete, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel2.add(spacer1, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mCopy = new JButton();
		mCopy.setText("Copy");
		panel2.add(mCopy, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mTimeSpent = new JLabel();
		mTimeSpent.setText("Watch Time:");
		panel2.add(mTimeSpent, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mPrivate = new JCheckBox();
		mPrivate.setText("Private");
		panel2.add(mPrivate, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mMediaPlayerPanel = new JPanel();
		mMediaPlayerPanel.setLayout(new BorderLayout(0, 0));
		panel1.add(mMediaPlayerPanel, new GridConstraints(1, 1, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel1.add(spacer2, new GridConstraints(1, 2, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		panel1.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel3.setOpaque(false);
		panel1.add(panel3, new GridConstraints(0, 0, 9, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, new Dimension(400, -1), 0, false));
		mTags.setName("Tags");
		mTags.setToolTipText("Tags");
		mTags.setVisibleRowCount(20);
		panel3.add(mTags, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Tags");
		panel3.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		panel3.add(mAddTagField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Add Tags: ");
		panel3.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mControlsPanel = new JPanel();
		mControlsPanel.setLayout(new BorderLayout(0, 0));
		panel1.add(mControlsPanel, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		mButtonPrevious = new JButton();
		mButtonPrevious.setHorizontalAlignment(2);
		mButtonPrevious.setText("<<<<<");
		mButtonPrevious.setToolTipText("Previous content");
		mControlsPanel.add(mButtonPrevious, BorderLayout.WEST);
		mButtonNext = new JButton();
		mButtonNext.setHorizontalAlignment(4);
		mButtonNext.setText(">>>>>");
		mButtonNext.setToolTipText("Next content");
		mControlsPanel.add(mButtonNext, BorderLayout.EAST);
		mVideoControlsPanel = new JPanel();
		mVideoControlsPanel.setLayout(new BorderLayout(0, 0));
		mVideoControlsPanel.setMaximumSize(new Dimension(2147483647, 2147483647));
		mVideoControlsPanel.setPreferredSize(new Dimension(0, 0));
		mControlsPanel.add(mVideoControlsPanel, BorderLayout.CENTER);
		mVideoControlsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
		mButtonBack = new JButton();
		mButtonBack.setText("Back");
		mContentPannel.add(mButtonBack, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mButtonHome = new JButton();
		mButtonHome.setText("Home");
		mContentPannel.add(mButtonHome, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return mContentPannel;
	}
	
	@Override
	public void keyTyped(KeyEvent e, boolean control) {
	
	}
	
	@Override
	public void keyPressed(KeyEvent e, boolean control) {
	
	}
	
	@Override
	public void keyReleased(KeyEvent e, boolean control) {
		
		if (control && mSkipButton != null && e.getKeyCode() == KeyEvent.VK_RIGHT) mSkipButton.doClick();
		else if (control && mRewindButton != null && e.getKeyCode() == KeyEvent.VK_LEFT) mRewindButton.doClick();
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT) next(false);
		else if (e.getKeyCode() == KeyEvent.VK_LEFT) previous(false);
		else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) mButtonBack.doClick();
		
	}
	
}
