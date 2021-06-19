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
		
		super(new ImageIcon(content.getThumbnailFile()));
		
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
			else text.append( " Favorite");
		}
		
		if (mContent.getType() == ContentType.VIDEO) {
			text.append(" \nlength").append(Utils.longToStrTime(mContent.getVideoLength()));
		}
		
		this.setText(text.toString());
		this.setHorizontalTextPosition(SwingConstants.CENTER);
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setPreferredSize(new Dimension(300, 320));
		
	}
	
	public Content getContent() {
		return mContent;
	}
	
}
