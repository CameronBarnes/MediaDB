package ca.bigcattech.MediaDB.gui.forms;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.core.Ingest;
import ca.bigcattech.MediaDB.core.Session;
import ca.bigcattech.MediaDB.db.Tag;
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.gui.components.AutoCompleteTextField;
import ca.bigcattech.MediaDB.gui.interfaces.IKeyListener;
import ca.bigcattech.MediaDB.utils.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import uk.co.caprica.vlcj.player.base.MarqueePosition;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import static javax.swing.SwingConstants.HORIZONTAL;

public class IngestForm implements IKeyListener {
	
	private final Ingest.IngestTask mIngestTask;
	private final Session mSession;
	private final CallbackMediaPlayerComponent mMediaPlayerComponent;
	public JPanel mContentPanel;
	private JScrollPane scroll;
	private JTextField mTitle;
	private JTextArea mDescription;
	private JCheckBox mIsPrivate;
	private JPanel mMediaPlayerPanel;
	private AutoCompleteTextField mTagsField;
	private JList<String> mTagsList;
	private JButton mButtonDone;
	private JPanel mVideoControlsPanel;
	private JButton mCancel;
	private JProgressBar mProgressBar;
	private JButton mSkip;
	private JButton mRewindButton;
	private JButton mSkipButton;
	
	private final ArrayList<String> mDictionary;
	
	private boolean mPlaying = true;
	
	public IngestForm(Ingest.IngestTask task, Session session) {
		
		mIngestTask = task;
		mSession = session;
		$$$setupUI$$$();
		
		if (mIngestTask.getType() == ContentType.GIF) {
			mMediaPlayerComponent = null;
			mMediaPlayerPanel.add(new JLabel(new ImageIcon(mIngestTask.getFile().getPath())));
		}
		else {
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
		
		playContent();
		
		mDictionary = mSession.getDictionary();
		mTagsField.setDBHandler(mSession.getDBHandler());
		mTagsField.setDictionary(mDictionary);
		
		mProgressBar.setMaximum(mSession.getIngestTempNum());
		mProgressBar.setValue(mSession.getIngestTempNum() - mSession.getIngest().getNumIngestTasks());
		
		if (mIngestTask.getType() == ContentType.VIDEO) {
			
			mRewindButton = new JButton("Rewind");
			mRewindButton.addActionListener(e -> mMediaPlayerComponent.mediaPlayer().controls().skipTime(-10000));
			mVideoControlsPanel.add(mRewindButton, BorderLayout.WEST);
			JButton pauseButton = new JButton("Play/Pause");
			mMediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
				@Override
				public void finished(MediaPlayer mediaPlayer) {
					
					mPlaying = false;
					//pauseButton.setText("Play");
				}
			});
			pauseButton.addActionListener(e -> {
				if (mPlaying) {
					mMediaPlayerComponent.mediaPlayer().controls().pause();
					//pauseButton.setText("Play");
				}
				else {
					mMediaPlayerComponent.mediaPlayer().controls().play();
					//pauseButton.setText("Pause");
					mPlaying = true;
				}
			});
			mVideoControlsPanel.add(pauseButton, BorderLayout.CENTER);
			mSkipButton = new JButton("Skip");
			mSkipButton.addActionListener(e -> mMediaPlayerComponent.mediaPlayer().controls().skipTime(10000));
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
					if (!manualChangeBar[0]) progress.setValue(Math.min(maxPos, Math.round(newPosition * (float) maxPos)));
					else manualChangeBar[0] = false;
					if (length.getText().equals("00:00")) length.setText(Utils.longToStrTime(mediaPlayer.status().length()));
					currTime.setText(Utils.longToStrTime(mediaPlayer.status().time()));
					
					if (mIngestTask.getVideoLength() == 0) mIngestTask.setVideoLength(mMediaPlayerComponent.mediaPlayer().status().length());
					
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
			
			if (mSession.getOptions().isIngestAutoTagField()) {
				SwingUtilities.invokeLater(() -> mTagsField.requestFocusInWindow());
			}
			
		}
		
		mTitle.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				super.keyTyped(e);
				mIngestTask.setTitle(mTitle.getText());
			}
		});
		
		mDescription.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				super.keyTyped(e);
				mIngestTask.setDescription(mDescription.getText());
			}
		});
		
		mIsPrivate.addActionListener(e -> mIngestTask.setPrivate(mIsPrivate.isSelected()));
		
		mTagsField.addActionListener(e -> {
			addTag(mTagsField.getText().toLowerCase().split(" "));
			mTagsField.setText("");
		});
		
		mButtonDone.addActionListener(e -> {
			//Just to make sure we didn't forget anything in there
			addTag(mTagsField.getText().toLowerCase().split(" "));
			mTagsField.setText("");
			
			mSession.ingest();
			release();
		});
		
		mTagsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				
				super.mouseClicked(evt);
				
				JList<String> list = (JList<String>) evt.getSource();
				if (evt.getClickCount() == 2) {
					// Double-click detected
					
					Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
					if (evt.getButton() == MouseEvent.BUTTON1 && r != null && r.contains(evt.getPoint())) {
						
						String selectedTag = mTagsList.getSelectedValue().split(":")[0];
						mIngestTask.removeTags(selectedTag);
						
						((DefaultListModel<String>) mTagsList.getModel()).removeElement(selectedTag);
						
					}
					
				}
				
			}
		});
		
		mCancel.addActionListener(e -> {
			mIngestTask.setCancel();
			release();
			mSession.home();
		});
		
		mSkip.addActionListener(e -> {
			mIngestTask.setSkip();
			release();
			mSession.ingest();
		});
	}
	
	public void playContent() {
		
		if (mMediaPlayerComponent != null) {
			EventQueue.invokeLater(() -> this.mMediaPlayerComponent.mediaPlayer().media().play(this.mIngestTask.getFile().getAbsolutePath()));
		}
	}
	
	public void release() {
		
		if (mMediaPlayerComponent != null) {
			// It is not allowed to call back into LibVLC from an event handling thread, so submit() is used
			mMediaPlayerComponent.mediaPlayer().submit(() -> mMediaPlayerComponent.mediaPlayer().controls().stop());
			mMediaPlayerComponent.mediaPlayer().submit(() -> mMediaPlayerComponent.mediaPlayer().release());
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		synchronized (mIngestTask) {
			mIngestTask.notifyAll();
		}
		
	}
	
	private void addTag(String tag) {
		
		addTag(new String[]{tag});
	}
	
	private void addTag(String[] tags) {
		
		ArrayList<String> tagsToAdd = new ArrayList<>(Arrays.asList(tags));
		mDictionary.removeAll(tagsToAdd);
		
		for (String name : tags) {
			
			Tag tag = mSession.getDBHandler().getTagFromName(name);
			if (tag == null) continue;
			
			tagsToAdd.addAll(Arrays.asList(tag.getParentTags()));
			
			addTag(tag.getParentTags());
			
		}
		
		for (String tag : tagsToAdd) {
			
			if (!((DefaultListModel<String>) mTagsList.getModel()).contains(tag))
				((DefaultListModel<String>) mTagsList.getModel()).addElement(tag);
			
		}
		
		String[] out = tagsToAdd.toArray(new String[]{});
		
		mSession.addTagToDictionary(out);
		mIngestTask.addTags(out);
		
	}
	
	private void createUIComponents() {
		
		mTagsList = new JList<>(new DefaultListModel<>());
		mTagsList.setDragEnabled(false);
		mTagsField = new AutoCompleteTextField(false, true);
		mTagsField.setFocusTraversalKeysEnabled(false);
		
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
		mContentPanel = new JPanel();
		mContentPanel.setLayout(new GridLayoutManager(5, 6, new Insets(0, 0, 0, 0), -1, -1));
		scroll = new JScrollPane();
		mContentPanel.add(scroll, new GridConstraints(3, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 300), new Dimension(-1, 400), 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
		scroll.setViewportView(panel1);
		mTitle = new JTextField();
		panel1.add(mTitle, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Title:");
		panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Description");
		panel1.add(label2, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mDescription = new JTextArea();
		panel1.add(mDescription, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		mIsPrivate = new JCheckBox();
		mIsPrivate.setText("Is Private");
		panel1.add(mIsPrivate, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		mContentPanel.add(scrollPane1, new GridConstraints(1, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, new Dimension(400, -1), 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
		scrollPane1.setViewportView(panel2);
		final JLabel label3 = new JLabel();
		label3.setText("Tags");
		panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		panel2.add(mTagsField, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		panel2.add(mTagsList, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		mButtonDone = new JButton();
		mButtonDone.setText("Done");
		mContentPanel.add(mButtonDone, new GridConstraints(4, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mVideoControlsPanel = new JPanel();
		mVideoControlsPanel.setLayout(new BorderLayout(0, 0));
		mContentPanel.add(mVideoControlsPanel, new GridConstraints(2, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		mMediaPlayerPanel = new JPanel();
		mMediaPlayerPanel.setLayout(new BorderLayout(0, 0));
		mContentPanel.add(mMediaPlayerPanel, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		mContentPanel.add(spacer2, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		mCancel = new JButton();
		mCancel.setText("Cancel");
		mContentPanel.add(mCancel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(400, -1), 0, false));
		mProgressBar = new JProgressBar();
		mProgressBar.setStringPainted(true);
		mContentPanel.add(mProgressBar, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mSkip = new JButton();
		mSkip.setText("Skip");
		mContentPanel.add(mSkip, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return mContentPanel;
	}
	
	@Override
	public void keyTyped(KeyEvent e, boolean control) {
	
	}
	
	@Override
	public void keyPressed(KeyEvent e, boolean control) {
	
	}
	
	@Override
	public void keyReleased(KeyEvent e, boolean control) {
		if (control && e.getKeyCode() == KeyEvent.VK_TAB) SwingUtilities.invokeLater(() -> mTagsField.requestFocusInWindow());
		else if (control && e.getKeyCode() == KeyEvent.VK_ENTER) mButtonDone.doClick();
		else if (control && e.getKeyCode() == KeyEvent.VK_BACK_SLASH) mSkip.doClick();
		else if (control && mSkipButton != null && e.getKeyCode() == KeyEvent.VK_RIGHT) mSkipButton.doClick();
		else if (control && mRewindButton != null && e.getKeyCode() == KeyEvent.VK_LEFT) mRewindButton.doClick();
	}
	
}
