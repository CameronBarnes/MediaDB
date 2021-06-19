package ca.bigcattech.MediaDB.utils;

import ca.bigcattech.MediaDB.IO.FileSystemHandler;
import ca.bigcattech.MediaDB.image.SimilarityFinder;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

public class ImageUtils {
	
	private ImageUtils() {
	
	}
	
	/**
	 * Rough and dirty, but it'll be fast and probably maintain a good image ratio
	 *
	 * @param original    original image to be resized
	 * @param targetWidth the desired width for the output image
	 * @return the new resized image
	 */
	public static BufferedImage scalrSimpleResizeImage(BufferedImage original, int targetWidth) {
		
		return Scalr.resize(original, targetWidth);
	}
	
	/**
	 * @param original     original image to be resized
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @return the new resized image
	 */
	public static BufferedImage scalrResizeImage(BufferedImage original, int targetWidth, int targetHeight) {
		
		return Scalr.resize(original, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
	}
	
	/**
	 * @param original     original image to be resized
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param method       the scaling method to use
	 * @param convolveOp   an operation to perform on the image, Scalr.OP_ANTIALIAS is a good example
	 * @return the new resized image
	 */
	public static BufferedImage scalrResizeImage(BufferedImage original, int targetWidth, int targetHeight, Scalr.Method method, ConvolveOp convolveOp) {
		
		return Scalr.resize(original, method, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, convolveOp);
	}
	
	/**
	 * Defaults to JPEG output, should be a relatively high quality thumbnail
	 *
	 * @param original     original image to be resized
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @return the new resized image
	 * @throws IOException io operations could potentially fail, not currently catching that here
	 */
	public static BufferedImage thumbResizeImage(BufferedImage original, int targetWidth, int targetHeight) throws IOException {
		
		return thumbResizeImage(original, targetWidth, targetHeight, "JPEG", 1.0f);
	}
	
	/**
	 * Defaults to JPEG output, should be a relatively high quality thumbnail
	 *
	 * @param original     original image to be resized
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param format       the output format for the image
	 * @return the new resized image
	 * @throws IOException io operations could potentially fail, not currently catching that here
	 */
	public static BufferedImage thumbResizeImage(BufferedImage original, int targetWidth, int targetHeight, String format) throws IOException {
		
		return thumbResizeImage(original, targetWidth, targetHeight, format, 1.0f);
	}
	
	/**
	 * @param original     original image to be resized
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param quality      a value > 0 and <= 1 describing the percentage quality of the output image, default when not declared is 1
	 * @return the new resized image
	 * @throws IOException io operations could potentially fail, not currently catching that here
	 */
	public static BufferedImage thumbResizeImage(BufferedImage original, int targetWidth, int targetHeight, float quality) throws IOException {
		
		return thumbResizeImage(original, targetWidth, targetHeight, "JPEG", quality);
	}
	
	/**
	 * @param original     original image to be resized
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param format       the output format for the image
	 * @param quality      a value > 0 and <= 1 describing the percentage quality of the output image, default when not declared is 1
	 * @return the new resized image
	 * @throws IOException io operations could potentially fail, not currently catching that here
	 */
	public static BufferedImage thumbResizeImage(BufferedImage original, int targetWidth, int targetHeight, String format, float quality) throws IOException {
		
		if (quality > 1f || quality < 0f) throw new InvalidParameterException("Quality must be > 0 and <= 1");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Thumbnails.of(original)
				  .size(targetWidth, targetHeight)
				  .outputFormat(format)
				  .outputQuality(quality)
				  .toOutputStream(outputStream);
		byte[] data = outputStream.toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return ImageIO.read(inputStream);
	}
	
	/**
	 * create thumbnails of all the images in a directory, defaults to JPEG and 80% quality, saves with _THUMBNAIL suffix
	 *
	 * @param directory    the directory of images to make thumbnails of
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @throws IOException
	 */
	public static void thumbResizeDirectory(File directory, int targetWidth, int targetHeight) throws IOException {
		
		thumbResizeDirectory(directory, targetWidth, targetHeight, "JPEG", 0.80f);
	}
	
	/**
	 * create thumbnails of all the images in a directory, defaults to 80% quality, saves with _THUMBNAIL suffix
	 *
	 * @param directory    the directory of images to make thumbnails of
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param format       file format to output thumbnails as
	 * @throws IOException
	 */
	public static void thumbResizeDirectory(File directory, int targetWidth, int targetHeight, String format) throws IOException {
		
		thumbResizeDirectory(directory, targetWidth, targetHeight, format, 0.80f);
	}
	
	/**
	 * create thumbnails of all the images in a directory, defaults to JPEG, saves with _THUMBNAIL suffix
	 *
	 * @param directory    the directory of images to make thumbnails of
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param quality      a value > 0 and <= 1 describing the percentage quality of the output image, default when not declared is 0.80
	 * @throws IOException
	 */
	public static void thumbResizeDirectory(File directory, int targetWidth, int targetHeight, float quality) throws IOException {
		
		thumbResizeDirectory(directory, targetWidth, targetHeight, "JPEG", quality);
		
	}
	
	/**
	 * create thumbnails of all the images in a directory, defaults to 80% quality, saves with _THUMBNAIL suffix
	 *
	 * @param directory    the directory of images to make thumbnails of
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param format       file format to output thumbnails as
	 * @param quality      a value > 0 and <= 1 describing the percentage quality of the output image, default when not declared is 0.80
	 * @throws IOException
	 */
	public static void thumbResizeDirectory(File directory, int targetWidth, int targetHeight, String format, float quality) throws IOException {
		
		if (quality > 1f || quality < 0f) throw new InvalidParameterException("Quality must be > 0 and <= 1");
		Thumbnails.of(directory.listFiles())
				  .size(targetWidth, targetHeight)
				  .outputFormat(format)
				  .outputQuality(quality)
				  .toFiles(Rename.SUFFIX_HYPHEN_THUMBNAIL);
		
	}
	
	/**
	 * create thumbnails of all the images in a file array, defaults to 80% quality and JPEG format, saves with _THUMBNAIL suffix
	 *
	 * @param files        the files for the images to resize
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @throws IOException
	 */
	public static void thumbResizeFiles(File[] files, int targetWidth, int targetHeight) throws IOException {
		
		thumbResizeFiles(files, targetWidth, targetHeight, "JPEG", 0.80f);
	}
	
	/**
	 * create thumbnails of all the images in a file array, defaults to JPEG format, saves with _THUMBNAIL suffix
	 *
	 * @param files        the files for the images to resize
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param quality      a value > 0 and <= 1 describing the percentage quality of the output image, default when not declared is 0.80
	 * @throws IOException
	 */
	public static void thumbResizeFiles(File[] files, int targetWidth, int targetHeight, float quality) throws IOException {
		
		thumbResizeFiles(files, targetWidth, targetHeight, "JPEG", quality);
	}
	
	/**
	 * create thumbnails of all the images in a file array, defaults to 80% quality, saves with _THUMBNAIL suffix
	 *
	 * @param files        the files for the images to resize
	 * @param targetWidth  the desired width for the output image
	 * @param targetHeight the desired height for the output image
	 * @param format       file format to output thumbnails as
	 * @param quality      a value > 0 and <= 1 describing the percentage quality of the output image, default when not declared is 0.80
	 * @throws IOException
	 */
	public static void thumbResizeFiles(File[] files, int targetWidth, int targetHeight, String format, float quality) throws IOException {
		
		if (quality > 1f || quality < 0f) throw new InvalidParameterException("Quality must be > 0 and <= 1");
		Thumbnails.of(files)
				  .size(targetWidth, targetHeight)
				  .outputFormat(format)
				  .outputQuality(quality)
				  .toFiles(Rename.SUFFIX_HYPHEN_THUMBNAIL);
	}
	
	/**
	 * Write a buffered image to disk with the provided path and format
	 *
	 * @param image  image to write out
	 * @param file   the path including file name to write to
	 * @param format the file format to save the image as
	 * @throws IOException
	 */
	public static void writeOutBufferedImage(BufferedImage image, File file, String format) throws IOException {
		
		ImageIO.write(image, format, file);
	}
	
	/**
	 * Write a buffered image from a ImageData object to disk
	 *
	 * @param imageData the data to write, includes BufferedImage, Path including file name, and format
	 * @throws IOException
	 */
	public static void writeOutImageData(ImageData imageData) throws IOException {
		
		writeOutBufferedImage(imageData.mBufferedImage, imageData.mFile, imageData.mFormat);
	}
	
	/**
	 * Load an image from disk into a ImageData object
	 *
	 * @param image the path to the image
	 * @return an ImageData object with the Path, BufferedImage, and image format
	 * @throws IOException
	 */
	public static ImageData loadImage(File image) throws IOException {
		
		if (image.isDirectory()) throw new InvalidParameterException("Image must be a file not a directory");
		
		String format = FileSystemHandler.getExtension(image);
		BufferedImage bufferedImage = ImageIO.read(image);
		if (bufferedImage == null) throw new IOException("File: " + image.getName() + " Image is null.");
		return new ImageData(image, bufferedImage, format);
		
	}
	
	public static SimilarityFinder.ImageSignature calcImageSignature(File image) throws IOException {
		
		ImageData imageData = loadImage(image);
		return SimilarityFinder.calcSignature(imageData.getBufferedImage());
		
	}
	
	/**
	 * @param colorStr e.g. "#FFFFFF"
	 * @return Color
	 */
	public static Color hex2Rgb(String colorStr) {
		
		return new Color(
				Integer.valueOf(colorStr.substring(1, 3), 16),
				Integer.valueOf(colorStr.substring(3, 5), 16),
				Integer.valueOf(colorStr.substring(5, 7), 16));
	}
	
	public static String colorToHex(Color color) {
		
		return "#" + Integer.toHexString(color.getRGB()).substring(2);
	}
	
	public static class ImageData {
		
		private File mFile;
		private BufferedImage mBufferedImage;
		private String mFormat;
		
		public ImageData(File file, BufferedImage image, String format) {
			
			mFile = file;
			mBufferedImage = image;
			mFormat = format;
		}
		
		public File getFile() {
			
			return mFile;
		}
		
		public void setFile(File file) {
			
			mFile = file;
		}
		
		public void rename(File newName) {
			
			mFile.renameTo(newName);
			mFile = newName;
		}
		
		public BufferedImage getBufferedImage() {
			
			return mBufferedImage;
		}
		
		public void setBufferedImage(BufferedImage bufferedImage) {
			
			mBufferedImage = bufferedImage;
		}
		
		public String getFormat() {
			
			return mFormat;
		}
		
		public void setFormat(String format) {
			
			mFormat = format;
		}
		
	}
	
}
