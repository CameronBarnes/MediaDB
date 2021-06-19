/*
 *     ImageSelection
 *     Last Modified: 2021-06-18, 7:28 p.m.
 *     Copyright (C) 2021-06-18, 7:28 p.m.  CameronBarnes
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

package ca.bigcattech.MediaDB.IO;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;

public class ImageSelection implements Transferable {
	
	Image mImage;
	
	public ImageSelection(Image image) {
		
		mImage = image;
	}
	
	public ImageSelection(File file) {
		
		mImage = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		
		return new DataFlavor[]{DataFlavor.imageFlavor};
	}
	
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		
		return flavor == DataFlavor.imageFlavor;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		
		if (flavor != DataFlavor.imageFlavor) throw new UnsupportedFlavorException(flavor);
		return mImage;
	}
	
}
