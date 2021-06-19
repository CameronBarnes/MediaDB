/*
 *     ThumbnailHandler
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
