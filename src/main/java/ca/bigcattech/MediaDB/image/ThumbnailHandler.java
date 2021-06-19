package ca.bigcattech.MediaDB.image;

import ca.bigcattech.MediaDB.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//import org.bytedeco.javacv.FFmpegFrameGrabber;
//import org.bytedeco.javacv.Java2DFrameConverter;

public class ThumbnailHandler {
	
	private ThumbnailHandler() {
	
	}
	
	/*public static boolean createVideoThumbnail(File video, File outputFile) {
		
		//Separate the file type
		String format = FileSystemHandler.getExtension(video);
		
		try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(video)) {
			
			//Setup the frame grabber
			frameGrabber.setFormat(format);
			frameGrabber.start();
			
			Java2DFrameConverter frameConverter = new Java2DFrameConverter();
			
			ImageIO.write(frameConverter.convert(frameGrabber.grab()), "jpg", outputFile);
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return outputFile.exists();
		
	}*/
	
	public static boolean createImageThumbnail(File image, File outputFile) {
		
		try {
			ImageUtils.ImageData imageData = ImageUtils.loadImage(image);
			BufferedImage thumbnail;
			thumbnail = ImageUtils.thumbResizeImage(imageData.getBufferedImage(), 300, 300);
			ImageUtils.writeOutBufferedImage(thumbnail, outputFile, imageData.getFormat());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return outputFile.exists();
		
	}
	
}
