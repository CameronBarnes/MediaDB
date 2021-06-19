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
