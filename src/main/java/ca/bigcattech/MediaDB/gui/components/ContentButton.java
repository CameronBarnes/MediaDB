/*
 *     ContentButton
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

package ca.bigcattech.MediaDB.gui.components;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.utils.Utils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class ContentButton extends JButton {
	
	private final Content mContent;
	
	public ContentButton(Content content) {
		
		mContent = content;
		
		if (mContent.isRestricted())
			this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.RED, Color.RED));
		
		//Set the font so that we have unicode support
		Font unicodeFont = FileSystemHandler.getUnicodeFont();
		if (unicodeFont != null) {
			this.setFont(unicodeFont);
		}
		
		StringBuilder text = new StringBuilder();
		
		if (mContent.isRestricted() && unicodeFont != null) {
			text.append(" \uf023 ");
		}
		
		text.append(mContent.mType.name());
		text.append(" views ").append(mContent.getViews());
		
		if (mContent.isFavorite()) {
			if (unicodeFont != null) {
				text.append(" \uf005");
			}
			else text.append(" Favorite");
		}
		
		if (mContent.getType() == ContentType.VIDEO) {
			text.append(" \nlength").append(Utils.longToStrTime(mContent.getVideoLength()));
		}
		
		this.setText(text.toString());
		this.setHorizontalTextPosition(SwingConstants.CENTER);
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setPreferredSize(new Dimension(300, 320));
		
	}
	
	public void displayThumbnail() {
		
		this.setIcon(new ImageIcon(mContent.getThumbnailFile()));
	}
	
	public Content getContent() {
		
		return mContent;
	}
	
}
