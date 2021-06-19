package ca.bigcattech.MediaDB.IO;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileTransferable implements Transferable {
	
	List<File> mFileList;
	
	public FileTransferable(File file) {
		mFileList = new ArrayList<>();
		mFileList.add(file);
	}
	
	public FileTransferable(List<File> files) {
		mFileList = files;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{DataFlavor.javaFileListFlavor};
	}
	
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor == DataFlavor.javaFileListFlavor;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor != DataFlavor.javaFileListFlavor) throw new UnsupportedFlavorException(flavor);
		return mFileList;
	}
	
}
