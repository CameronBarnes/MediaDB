/*
 *     TagsManagementForm
 *     Last Modified: 2021-08-01, 1:27 p.m.
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

package ca.bigcattech.MediaDB.gui.forms;

import ca.bigcattech.MediaDB.core.Session;
import ca.bigcattech.MediaDB.db.tag.Tag;
import ca.bigcattech.MediaDB.gui.components.AutoCompleteTextField;
import ca.bigcattech.MediaDB.utils.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class TagsManagementForm {
	
	private static final Logger log = LoggerFactory.getLogger(TagsManagementForm.class.getName());
	private static final Comparator<DefaultMutableTreeNode> treeNodeCompare = (a, b) -> {
		
		if (a.isLeaf() && !b.isLeaf()) return 1;
		else if (!a.isLeaf() && b.isLeaf()) return -1;
		else {
			return switch (Long.compare(((TagInfo) b.getUserObject()).getNumUses(), ((TagInfo) a.getUserObject()).getNumUses())) {
				case 1 -> 1;
				case -1 -> -1;
				default -> ((TagInfo) a.getUserObject()).getName().compareToIgnoreCase(((TagInfo) b.getUserObject()).getName());
			};
		}
		
	};
	private final Session mSession;
	private final LinkedList<Tag> mUpdatedTags = new LinkedList<>();
	public JPanel mContent;
	private JTree mTagTree;
	private JLabel mTagName;
	private JList<String> mTagParentList;
	private AutoCompleteTextField mAddParentTag;
	private JButton mHome;
	private JTextField mNewTagField;
	private AutoCompleteTextField mTagSearch;
	private JCheckBox mRestricted;
	private JButton mReload;
	private JList<Tag> mChildList;
	private AutoCompleteTextField mAddChildTag;
	private JButton mDeleteTag;
	private JButton mForce;
	private JComboBox<Tag.TagType> mTagType;
	private DefaultTreeModel mTreeModel;
	private Tag mCurrentTag;
	
	public TagsManagementForm(Session session) {
		
		$$$setupUI$$$();
		
		mSession = session;
		mHome.addActionListener(e -> {
			mSession.home();
			exit();
		});
		
		log.info("Populating JTree nodes");
		populateNodes();
		log.info("Done!");
		
		log.info("Sorting the tree");
		sortTreeTimSort((DefaultMutableTreeNode) mTreeModel.getRoot());
		log.info("Done!");
		
		mTagTree.expandRow(0);
		
		mSession.revalidateAndRepaintFrame();
		
		mAddChildTag.setDictionary(mSession.getDictionary());
		mAddParentTag.setDictionary(mSession.getDictionary());
		mTagSearch.setDictionary(mSession.getDictionary());
		
		Utils.addArrayToComboBox(mTagType, Arrays.asList(Tag.TagType.values()));
		
		mTagType.addActionListener(e -> mCurrentTag.setTagType((Tag.TagType) mTagType.getSelectedItem()));
		
		mTagTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		mTagTree.addTreeSelectionListener(e -> {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) mTagTree.getLastSelectedPathComponent();
			if (node == null) return;
			
			TagInfo tagNodeInfo = (TagInfo) node.getUserObject();
			if (tagNodeInfo == null) return;
			
			displayTag(tagNodeInfo.getTag());
			
		});
		
		mRestricted.addActionListener(e -> {
			if (mCurrentTag != null) {
				mCurrentTag.setRestricted(mRestricted.isSelected());
				addUpdatedTag(mCurrentTag);
			}
		});
		
		mAddParentTag.addActionListener(e -> {
			if (mCurrentTag != null) {
				String[] tags = mAddParentTag.getText().toLowerCase().split(" ");
				mCurrentTag.addParentTags(tags);
				for (String tag : tags) {
					
					if (tag.equals("") || tag.equals(" ")) continue;
					((DefaultListModel<String>) mTagParentList.getModel()).addElement(tag);
					
				}
				mAddParentTag.setText("");
				addUpdatedTag(mCurrentTag);
				
				sortParentTagList();
				
			}
		});
		
		mAddChildTag.addActionListener(e -> {
			if (mCurrentTag != null) {
				
				String[] tags = mAddChildTag.getText().toLowerCase(Locale.ROOT).split(" ");
				
				for (String tag : tags) {
					
					Tag childTag = addChildTag(tag);
					if (childTag == null) continue;
					((DefaultListModel<Tag>) mChildList.getModel()).addElement(childTag);
					
				}
				
				mAddChildTag.setText("");
				addUpdatedTag(mCurrentTag);
				
				sortChildTagList();
				
			}
		});
		
		mTagSearch.addActionListener(e -> {
			displayTag(mTagSearch.getText().toLowerCase(Locale.ROOT).split(" ")[0]);
			mTagSearch.setText("");
		});
		
		mNewTagField.addActionListener(e -> {
			addNewTag(mNewTagField.getText().toLowerCase().split(" "));
			mNewTagField.setText("");
		});
		
		mReload.addActionListener(e -> {
			mSession.home();
			exit();
			mSession.tags();
		});
		
		mTagParentList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				
				super.mouseClicked(evt);
				
				JList<String> list = (JList<String>) evt.getSource();
				if (evt.getClickCount() == 2) {
					// Double-click detected
					
					Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
					if (evt.getButton() == MouseEvent.BUTTON1 && r != null && r.contains(evt.getPoint())) {
						
						String selectedTag = mTagParentList.getSelectedValue();
						mCurrentTag.removeParentTag(selectedTag);
						
						((DefaultListModel<String>) mTagParentList.getModel()).removeElement(selectedTag);
						
						addUpdatedTag(mCurrentTag);
						
					}
					
				}
				
			}
		});
		
		mChildList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				
				super.mouseClicked(evt);
				
				JList<TagInfo> list = (JList<TagInfo>) evt.getSource();
				if (evt.getClickCount() == 2) {
					// Double-click detected
					
					Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
					if (evt.getButton() == MouseEvent.BUTTON1 && r != null && r.contains(evt.getPoint())) {
						
						Tag selectedTag = mChildList.getSelectedValue();
						removeChildTag(selectedTag);
						
						((DefaultListModel<Tag>) mChildList.getModel()).removeElement(selectedTag);
						
					}
					
				}
				
			}
		});
		
		mDeleteTag.addActionListener(e -> {
			if (mCurrentTag == null) return;
			if (JOptionPane.showConfirmDialog(mContent, "Delete tag: " + mCurrentTag.getName() + " ?") != JOptionPane.YES_OPTION)
				return;
			mSession.getDBHandler().removeTagFromAll(mCurrentTag.getName());
			mSession.getDBHandler().deleteTag(mCurrentTag.getName());
			mUpdatedTags.remove(mCurrentTag);
			mCurrentTag = null;
			mReload.doClick();
		});
		
		mForce.addActionListener(e -> mSession.getDBHandler().updateAll());
		
	}
	
	private static void sortTreeTimSort(DefaultMutableTreeNode parent) {
		
		Collections.list((Enumeration<?>) parent.preorderEnumeration()).stream()
				   .filter(DefaultMutableTreeNode.class::isInstance)
				   .map(DefaultMutableTreeNode.class::cast)
				   .filter(node -> !node.isLeaf())
				   .forEach(TagsManagementForm::timSort);
		
	}
	
	private static void timSort(DefaultMutableTreeNode parent) {
		
		int n = parent.getChildCount();
		ArrayList<DefaultMutableTreeNode> children = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			children.add((DefaultMutableTreeNode) parent.getChildAt(i));
		}
		
		children.sort(treeNodeCompare);
		parent.removeAllChildren();
		children.forEach(parent::add);
		
	}
	
	private void populateNodes() {
		
		for (Tag tag : mSession.getDBHandler().getAllTags()) {
			addNode(tag);
		}
		
	}
	
	private void sortParentTagList() {
		
		//TODO fix this, it's a fukin mess
		DefaultListModel<String> model = (DefaultListModel<String>) mTagParentList.getModel();
		String[] allTags = new String[model.size()];
		model.copyInto(allTags);
		ArrayList<String> tagsList = new ArrayList<>(Arrays.asList(allTags));
		tagsList.sort(String::compareToIgnoreCase);
		model.clear();
		model.addAll(tagsList);
		
	}
	
	private void sortChildTagList() {
		
		//TODO fix this, it's a fukin mess
		DefaultListModel<Tag> model = (DefaultListModel<Tag>) mChildList.getModel();
		Tag[] allTags = new Tag[model.size()];
		model.copyInto(allTags);
		ArrayList<Tag> tagsList = new ArrayList<>(Arrays.asList(allTags));
		tagsList.sort(Tag::compareToName);
		model.clear();
		model.addAll(tagsList);
		
	}
	
	private Tag addChildTag(String tag) {
		
		Tag child = mSession.getDBHandler().getTagFromName(tag);
		if (mCurrentTag == null) return null;
		if (child == null) child = new Tag(tag);
		
		child.addParentTag(mCurrentTag.getName());
		mSession.getDBHandler().exportTag(child, false);
		
		addUpdatedTag(child);
		
		return child;
		
	}
	
	private void removeChildTag(Tag child) {
		
		child.removeParentTag(mCurrentTag.getName());
		addUpdatedTag(child);
		mSession.getDBHandler().exportTag(child, false);
		
	}
	
	private void addUpdatedTag(Tag tag) {
		
		if (tag == null) return;
		
		if (mUpdatedTags.contains(tag)) return;
		for (Tag exist : mUpdatedTags) {
			if (exist.getName().equals(tag.getName())) return;
		}
		
		mUpdatedTags.add(tag);
		
	}
	
	private void addNewTag(String tag) {
		
		addNewTag(new String[]{tag});
	}
	
	private void addNewTag(String[] tags) {
		
		for (String name : tags) {
			
			Tag tag = mSession.getDBHandler().getTagFromName(name);
			if (tag == null) {
				tag = new Tag(name);
				mSession.getDBHandler().addTag(name);
			}
			
			addNode(tag);
			
		}
		
		mTreeModel.reload();
		mSession.revalidateAndRepaintFrame();
		
	}
	
	private void addNode(String name) {
		
		Tag tag = mSession.getDBHandler().getTagFromName(name);
		if (tag == null) tag = new Tag(name);
		addNode(tag);
		
	}
	
	private void addNode(Tag tag) {
		
		if (tag.getParentTags().length > 0) {
			for (String parent : tag.getParentTags()) {
				addNode(parent);
			}
		}
		else {
			if (!checkForChildTag((DefaultMutableTreeNode) mTreeModel.getRoot(), tag))
				((DefaultMutableTreeNode) mTreeModel.getRoot()).add(new DefaultMutableTreeNode(new TagInfo(tag)));
		}
		
		for (String parent : tag.getParentTags()) {
			
			DefaultMutableTreeNode node = getNode(parent);
			if (node == null || checkForChildTag(node, tag)) continue;
			node.add(new DefaultMutableTreeNode(new TagInfo(tag)));
			
		}
		
	}
	
	private boolean checkForChildTag(DefaultMutableTreeNode parent, String child) {
		
		for (int i = 0; i < parent.getChildCount(); i++) {
			
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parent.getChildAt(i);
			if (childNode == null) continue;
			if (((TagInfo) childNode.getUserObject()).getName().equals(child)) return true;
			
		}
		
		return false;
		
	}
	
	private boolean checkForChildTag(DefaultMutableTreeNode parent, Tag child) {
		
		for (int i = 0; i < parent.getChildCount(); i++) {
			
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parent.getChildAt(i);
			if (childNode == null) continue;
			if (((TagInfo) childNode.getUserObject()).getName().equals(child.getName())) return true;
			
		}
		
		return false;
		
	}
	
	private DefaultMutableTreeNode getNode(String name) {
		
		Iterator<TreeNode> iterator = ((DefaultMutableTreeNode) mTreeModel.getRoot()).depthFirstEnumeration().asIterator();
		
		while (iterator.hasNext()) {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) iterator.next();
			TagInfo tagNodeInfo = (TagInfo) node.getUserObject();
			if (tagNodeInfo == null) continue;
			if (tagNodeInfo.getName().equals(name)) return node;
			
		}
		
		return null;
		
	}
	
	private void displayTag(String tagName) {
		
		Tag tag = mSession.getDBHandler().getTagFromName(tagName);
		if (tag == null) tag = new Tag(tagName);
		displayTag(tag);
		
	}
	
	private void displayTag(Tag tag) {
		
		if (mCurrentTag != null) mSession.getDBHandler().exportTag(mCurrentTag, false);
		mCurrentTag = tag;
		
		mTagName.setText(tag.getName());
		mRestricted.setSelected(tag.isRestricted());
		mTagType.setSelectedItem(tag.getTagType());
		
		mTagParentList.setModel(new DefaultListModel<>());
		for (String parent : tag.getParentTags()) {
			
			if (!((DefaultListModel<String>) mTagParentList.getModel()).contains(tag))
				((DefaultListModel<String>) mTagParentList.getModel()).addElement(parent);
			
		}
		
		mChildList.setModel(new DefaultListModel<>());
		List<Tag> allChildTags = mSession.getDBHandler().getAllTagsWithParent(tag.getName());
		ArrayList<Tag> inModel = new ArrayList<>(allChildTags.size());
		for (Tag child : allChildTags) {
			
			if (inModel.stream().parallel().anyMatch(tagInfo -> tagInfo.getName().equals(child.getName()))) continue;
			inModel.add(child);
			((DefaultListModel<Tag>) mChildList.getModel()).addElement(child);
			
		}
		
		inModel.trimToSize();
		
		sortChildTagList();
		sortParentTagList();
		
	}
	
	private void exit() {
		
		if (mCurrentTag != null) mSession.getDBHandler().exportTag(mCurrentTag, false);
		mSession.getDBHandler().updateAllWithTags(mUpdatedTags.toArray(new Tag[]{}));
		mSession.getDBHandler().updateAllTags(mUpdatedTags.toArray(new Tag[]{}));
		
	}
	
	//==========================================================Tree sorting stuff here==========================================================
	
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
		mContent.setLayout(new GridLayoutManager(7, 12, new Insets(0, 0, 0, 0), -1, -1));
		mContent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
		mHome = new JButton();
		mHome.setText("Home");
		mContent.add(mHome, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setHorizontalAlignment(2);
		label1.setText("Tag Name: ");
		mContent.add(label1, new GridConstraints(0, 6, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mContent.add(mTagSearch, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Parent Tags: ");
		mContent.add(label2, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		mContent.add(scrollPane1, new GridConstraints(1, 0, 5, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane1.setViewportView(mTagTree);
		mTagName = new JLabel();
		mTagName.setText("");
		mContent.add(mTagName, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		mContent.add(spacer1, new GridConstraints(6, 6, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		mContent.add(spacer2, new GridConstraints(0, 9, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mReload = new JButton();
		mReload.setText("Reload");
		mContent.add(mReload, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
		mContent.add(panel1, new GridConstraints(2, 6, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JScrollPane scrollPane2 = new JScrollPane();
		panel1.add(scrollPane2, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane2.setViewportView(mTagParentList);
		final JLabel label3 = new JLabel();
		label3.setText("Add Parent: ");
		panel1.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		panel1.add(mAddParentTag, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
		mContent.add(panel2, new GridConstraints(5, 6, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JScrollPane scrollPane3 = new JScrollPane();
		panel2.add(scrollPane3, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane3.setViewportView(mChildList);
		final JLabel label4 = new JLabel();
		label4.setText("Add Child: ");
		panel2.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		panel2.add(mAddChildTag, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		mNewTagField = new JTextField();
		mContent.add(mNewTagField, new GridConstraints(6, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setText("Create new Tag: ");
		mContent.add(label5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mDeleteTag = new JButton();
		mDeleteTag.setText("Delete");
		mContent.add(mDeleteTag, new GridConstraints(0, 11, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JSeparator separator1 = new JSeparator();
		mContent.add(separator1, new GridConstraints(3, 6, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Child Tags:");
		mContent.add(label6, new GridConstraints(4, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label7 = new JLabel();
		label7.setText("Search Tag: ");
		mContent.add(label7, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mForce = new JButton();
		mForce.setText("Force");
		mForce.setToolTipText("Force update every piece of content in the database");
		mContent.add(mForce, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mRestricted = new JCheckBox();
		mRestricted.setText("Restricted");
		mContent.add(mRestricted, new GridConstraints(0, 10, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mTagType = new JComboBox();
		mContent.add(mTagType, new GridConstraints(1, 10, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label8 = new JLabel();
		label8.setText("Tag Type: ");
		mContent.add(label8, new GridConstraints(1, 9, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		mContent.add(spacer3, new GridConstraints(1, 7, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return mContent;
	}
	
	private void createUIComponents() {
		
		mTagParentList = new JList<>(new DefaultListModel<>());
		mTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
		mTagTree = new JTree(mTreeModel);
		mChildList = new JList<>(new DefaultListModel<>());
		
		mAddParentTag = new AutoCompleteTextField();
		mAddParentTag.setFocusTraversalKeysEnabled(false);
		mAddChildTag = new AutoCompleteTextField();
		mAddChildTag.setFocusTraversalKeysEnabled(false);
		mTagSearch = new AutoCompleteTextField(false, false);
		mTagSearch.setFocusTraversalKeysEnabled(false);
		
	}
	
	private class TagInfo {
		
		private Tag mTag;
		
		public TagInfo(String name) {
			
			mTag = mSession.getDBHandler().getTagFromName(name);
			if (mTag == null) {
				mTag = new Tag(name);
				mSession.getDBHandler().exportTag(mTag, false);
			}
			
		}
		
		public TagInfo(Tag tag) {
			
			mTag = tag;
		}
		
		public Tag getTag() {
			
			return mTag;
		}
		
		public String getName() {
			
			return mTag.getName();
		}
		
		public long getNumUses() {
			
			return mTag.getNumUses();
		}
		
		@Override
		public String toString() {
			
			return mTag.getName() + ": " + mTag.getNumUses();
		}
		
	}
	
}
