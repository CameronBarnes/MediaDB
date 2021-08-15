/*
 *     HelpForm
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

import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.db.tag.Tag;
import ca.bigcattech.MediaDB.utils.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;

public class HelpForm {
	
	public JPanel mContentPanel;
	private JTextArea mOptionsText;
	private JTextArea mMainText;
	private JTextArea mSearchText;
	private JTextArea mTagsText;
	private JTextArea mIngestText;
	private JTextArea mContentText;
	
	public HelpForm() {
		
		String acceptableTagTypesWOrAndPeriod = Utils.convertArrayToGramaticallyAcceptableList(Tag.TagType.values(), false, true);
		
		mMainText.setText(String.format("""
				Basic Info:
				\tContent:
				\tContent has a Type of any value %s
				\tIt has a list of tags, it can have a title and a description.
				\tContent can be either manually set to private or automatically set as restricted by one of its tags.
				
				\tTags:
				\tA Tag has a name, and an optional type of any value %s
				\tA Tag can have a list of parent tags, which are all added when that tag is added to content.
				\tA Tag can be set to restricted. A restricted tag will make any content with it Restricted, or effectively private.
				
				\tAutocomplete:
				\tNearly all search and tag fields have tag autocompletion, you can use it with the TAB key.
				\tTags that are used more will show up first when using autocomplete. It is not case sensitive.
				\tTags that have already been used(Like when adding tags to content or as parents/children of other tags) will not autocomplete.
				\tIn later versions you will be able to change this in the options menu.
				
				Buttons on the main menu:
				\t'Tags' takes you to the tage management menu, there you can edit tags, create new tags, or delete existing ones.
				\t'Ingest' loads new content into the database from the ingest folder, the folder button allows you import content from a specific folder.
				\tThe 'Search' bar and buttons searches the database for content matching the tags provided excluding the blacklisted tags.
				\tThe 'Private' checkbox includes content marked as private or with restricted tags in the search.
				""", Utils.convertArrayToGramaticallyAcceptableList(ContentType.values(), false, true), acceptableTagTypesWOrAndPeriod));
		
		mSearchText.setText("""
				Search:
				\tThe Search function displays all results that contain all the tags provided and none of the blacklisted tags.
				\tIf the private checkbox\\option hasn't been selected all content marked private or with a restricted tag will be omitted.
				\tIf no tags are provided or only a blacklist is provided, all or all otherwise valid content will be displayed.
				\tSearch tags and blacklist tags should be separated by '||'. The | key is accessible with SHIFT + \\
				
				Search Results:
				\tThe number of results columns can be changed in the options menu.
				\tThe Search bar in the top right will display your current search terms, and can be used to search again with different terms.
				\tThe tag list on the left will list all the tags on all the content results, the number is either the total number of content with that tag.
				\t\tIn later versions you have the option to change this to the number of results with that tag.
				\tDouble clicking on a tag in the list will add that tag to your search terms and search again.
				\tDouble middle clicking on a tag in the list will add that tag to your blacklist and search again.
				\tYou can switch between pages at the bottom of the screen under the search results with the buttons or with the LEFT and RIGHT arrow keys.
				\tThe number of results to show per page can be changed in the options.
				""");
		
		mTagsText.setText(String.format("""
				Tags:
				\tA Tag has a name, and an optional type of any value %s
				\tA Tag can have a list of parent tags, which are all added when that tag is added to content.
				\tA Tag can be set to restricted. A restricted tag will make any content with it Restricted, or effectively private.
				
				When you leave this menu after editing a tag, that tag and all the content it is on will be updated.
				If you add a parent tag to a tag, all content with that tag(the child) will automatically have the parent tag added.
				eg, add parent tag 'plant' to tag 'tree', all content with the tag 'tree' will automatically have the 'plant' tag added.
				
				Buttons on this page:
				\tGeneral:
				\tThe 'Home' button will take you back to the main page and update any tags that you've edited.
				\tThe 'Reload' button will update the tag database and the tag list on this screen with any changes you've made without sending you back to the main menu.
				\tThe 'Force' button will force the database to check the tags on all content to ensure that it's valid. This may take some time and shouldn't be necessary.
				\tThe 'Search Tag' text bar at the top searches for a tag and selects it for editing, this is an alternative to the tree\\list view.
				\tThe tree\\list view displays all tags, and the number of content items they are used on. Parent tags are folders that hold their children. Click to select for editing.
				\tThe 'Create new Tag' text bar creates a new tag and selects it for editing.
				
				\tTag Editor:
				\tThe 'Tag Name' is displayed at the top above parent tags.
				\tThe 'Restricted' checkbox marks the Tag as restricted, any content with a restricted Tag is effectively private.
				\tThe 'Tag Type' chooser allows you to change the tag's type between %s
				\t\tNote This doesn't currently have any function, but may in later versions.
				\tThe 'Delete' button deletes a Tag and removes it from all content, a confirmation dialog is presented to be sure.
				\t\tNote that this will not remove any parent tags that may have been added by having this tag on content.
				\tThe 'Parent Tags' list lists all parent tags, when you add this tag to content, all parent tags are added with it.
				\tDouble clicking on a tag in the 'Parent Tags' list will remove that parent tag from this tag.
				\tThe 'Add Parent' text bar adds a parent tag to this tag, if the tag does not already exist a new tag will be created.
				\tThe 'Child Tags' list lists all child tags, this is effectively the same as selecting that child tag and adding this tag as a parent.
				\tDouble clicking on a tag in the 'Child Tags' list will remove this tag as a parent from the child.
				\tThe 'Add Child' text bar adds this tag as a parent to the child tag, if the tag does not already exist a new tag will be created.
				
				""", acceptableTagTypesWOrAndPeriod, acceptableTagTypesWOrAndPeriod));
		
		mIngestText.setText("""
				Buttons on this page:
				\tThe progress bar at the top shows the amount of ingest task completed as a percentage of the total queued.
				\t'Cancel' returns you to the main menu and cancels progress on the content you're currently working on, returning it to its source folder.
				\t'Skip' cancels progress on the content you're currently working on, returning it to its source folder, and moves on to the next one.
				\tThe 'Is Private' checkbox toggles this content as private or not, private content will not show up in searches unless you choose to allow private content.
				\tThe 'Title' and 'Description' fields allow you to set a title or description for the content. They are not required.
				\tDouble clicking on a tag in the 'Tags' list will remove that tag from this content.
				\tThe text bar under the 'Tags' list adds tags (separated by spaces) to this content, if the tag does not already exist, it is created.
				\t\tNote that any text left in here when you press 'Done' will be added as a tag(s).
				\t'Done' saves this content and all the provided info/tags and moves on to the next.
				
				Keyboard Shortcuts:
				\tWhen using the add tags field 'TAB' autocompletes.
				\t'CONTROL' - 'ENTER' is the same as pressing the 'Done' button.
				\t'CONTROL' - '\\' is the same as pressing the 'Skip' button.
				\t'CONTROL' - 'TAB' selects the tag entry field.
				\t'CONTROL' - 'SHIFT' - 'LEFT_ARROW' skips back in a video
				\t'CONTROL' - 'SHIFT' - 'RIGHT_ARROW' skips forward in a video
				""");
		
		mContentText.setText("""
				Buttons on this page:
				\tThe 'Home' button takes you back to the main menu and saves any changes made to this content.
				\tThe 'Back' button takes you back to the search results page and saves any changes made to this content.
				\tThe search bar and button at the top will start a new search when used, exiting this page to open a new set of search results. Any changes made to this content are saved.
				\tDouble clicking on a tag in the 'Tags' list will remove that tag from this content.
				\tThe 'Add Tags' text bar adds tags (separated by spaces) to this content, if the tag does not already exist, it is created.
				\tThe 'Delete' button deletes the content from the database as well as deleting the content file, a confirmation dialog is presented to be sure.
				\tThe 'Copy' button copies the image or other content to the clipboard.
				\t The '<<<<<' button switches to the previous content from the search results and the '>>>>>' button switches to the next. Any changes made to this content are saved.
				\t\tNote that the LEFT and RIGHT arrow keys also server the same function.
				\tVideo content has an extra three buttons between the previous and next content buttons, from left to right they are rewind, play/pause, and skip forward.
				\tThe 'Is Private' checkbox toggles this content as private or not, private content will not show up in searches unless you choose to allow private content.
				\tThe 'Favorite' checkbox marks this content as a favorite, you can change search ordering to show favorite content first in the options menu.
				
				Keyboard Shortcuts:
				\t'RIGHT_ARROW' is the same as pressing the '>>>>>' button.
				\t'LEFT_ARROW' is the same as pressing the '<<<<<' button.
				\t'CONTROL' - 'SHIFT' - 'LEFT_ARROW' skips back in a video
				\t'CONTROL' - 'SHIFT' - 'RIGHT_ARROW' skips forward in a video
				\t'BACKSPACE' goes back to the search results page.
				""");
		
		mOptionsText.setText("""
				Options:
				\t'Result Columns' changes how many columns wide the search results are to allow for different screen sizes. The default is 5.
				\t\tAt 1920x1080(1080p) the recommended setting is 4 or lower, at 2560x1440(1440p) or 3840x2160(4K) the recommended setting is 7 or lower.
				\t'Results Per Page' changes how many results are shown on each of the search results pages, setting this to 0 will make everything display on one page.
				\tHigher values (or 0) have poor performance and arent recommended. The default is 250, recommended is 500 or less.
				Search Options:
				\t'Favorites first' forces content marked as favorite to be shown first, even if it otherwise would be ranked lower (eg. views).
				\t'Sort by' changes how the search results are ordered.
				\tThe default is HASH, which is effectively random but wont change. TITLE is just an alphabetical sort by title. VIEWS sorts by number of views, highest first.
				Ingest Options:
				\t'Auto Ingest Tag Field' attempts to automatically select the tag entry box when starting a new ingest task.
				\t'Purge Duplicates' removes any duplicate files or any files already in the database from the Ingest folder.
				\t'Purge from folder' removes any duplicate files or any files already in the database from a folder of your choice.
				\t'Validate Content folder' moves any files from the content folder that shouldn't be there to the ingest folder.
				Content View Options:
				\t'Slideshow Timer' this is the number of seconds each image is displayed for in a slideshow
				\t'Start Slideshow' stat's a slideshow if you're currently viewing media.
				""");
		
	}
	
	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}
	
	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		
		mContentPanel = new JPanel();
		mContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		final JTabbedPane tabbedPane1 = new JTabbedPane();
		mContentPanel.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Main", panel1);
		mMainText = new JTextArea();
		mMainText.setEditable(false);
		mMainText.setTabSize(2);
		panel1.add(mMainText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Search and Results", panel2);
		mSearchText = new JTextArea();
		mSearchText.setEditable(false);
		mSearchText.setTabSize(2);
		panel2.add(mSearchText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Tag Management", panel3);
		mTagsText = new JTextArea();
		mTagsText.setEditable(false);
		mTagsText.setTabSize(2);
		panel3.add(mTagsText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Ingest", panel4);
		mIngestText = new JTextArea();
		mIngestText.setEditable(false);
		mIngestText.setTabSize(2);
		panel4.add(mIngestText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JPanel panel5 = new JPanel();
		panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Content DIsplay", panel5);
		mContentText = new JTextArea();
		mContentText.setEditable(false);
		mContentText.setTabSize(2);
		panel5.add(mContentText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Options", panel6);
		mOptionsText = new JTextArea();
		mOptionsText.setEditable(false);
		mOptionsText.setTabSize(2);
		panel6.add(mOptionsText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return mContentPanel;
	}
	
}
