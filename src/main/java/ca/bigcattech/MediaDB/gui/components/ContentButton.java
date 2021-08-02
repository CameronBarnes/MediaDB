/*
 *     ContentButton
 *     Last Modified: 2021-08-02, 6:46 a.m.
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

package ca.bigcattech.MediaDB.gui.components;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.db.content.Content;
import ca.bigcattech.MediaDB.db.content.ContentType;
import ca.bigcattech.MediaDB.db.pool.Pool;
import ca.bigcattech.MediaDB.utils.Utils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class ContentButton extends JButton {
	
	private final Content mContent;
	private final Pool mPool;
	
	public ContentButton(Content content) {
		
		mContent = content;
		mPool = null;
		
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
	
	public ContentButton(Pool pool) {
		
		mPool = pool;
		mContent = null;
		
		if (mPool.isRestricted())
			this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.RED, Color.RED));
		
		//Set the font so that we have unicode support
		Font unicodeFont = FileSystemHandler.getUnicodeFont();
		if (unicodeFont != null) {
			this.setFont(unicodeFont);
		}
		
		StringBuilder text = new StringBuilder();
		
		if (mPool.isRestricted() && unicodeFont != null) {
			text.append(" \uf023 ");
		}
		
		text.append(mPool.getTitle());
		
		if (mPool.isFavorite()) {
			if (unicodeFont != null) {
				text.append(" \uf005");
			}
			else text.append(" Favorite");
		}
		
		text.append(" size: ");
		text.append(mPool.getContentHashes().length);
		
		this.setText(text.toString());
		this.setHorizontalTextPosition(SwingConstants.CENTER);
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setPreferredSize(new Dimension(300, 320));
		
	}
	
	public void clearIcon() {
		
		Icon icon = getIcon();
		if (icon != null) ((ImageIcon) icon).getImage().flush();
	}
	
	public void displayThumbnail() {
		
		if (mContent != null)
			this.setIcon(new ImageIcon(mContent.getThumbnailFile()));
		else
			this.setIcon(new ImageIcon(mPool.getThumbnailFile()));
	}
	
	public Pool getPool() {
		
		if (mPool == null) throw new UnsupportedOperationException("This is a ContentButton with a Content object.");
		return mPool;
	}
	
	public Content getContent() {
		
		if (mContent == null) throw new UnsupportedOperationException("This is a ContentButton with a Pool object.");
		return mContent;
	}
	
	public ButtonType getButtonType() {
		
		if (mContent != null) return ButtonType.CONTENT;
		else return ButtonType.POOL;
	}
	
	public enum ButtonType {
		CONTENT,
		POOL
	}
	
}
