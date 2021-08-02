/*
 *     SimilarityFinder
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

package ca.bigcattech.MediaDB.image;

import ca.bigcattech.MediaDB.db.DBHandler;
import ca.bigcattech.MediaDB.utils.ImageUtils;
import ca.bigcattech.MediaDB.utils.Utils;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
Code largely based on https://web.archive.org/web/20160406063419/http://www.lac.inpe.br/JIPCookbook/6050-howto-compareimages.jsp
 */

public class SimilarityFinder {
	
	private final DBHandler mDBHandler;
	
	public SimilarityFinder(DBHandler dbHandler) {
		mDBHandler = dbHandler;
	}
	
	private static double calcDistance(ImageSignature signature, ImageSignature signatureOther) {
		
		double dist = 0;
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				
				int r1 = signature.getSignature()[x][y].getRed();
				int g1 = signature.getSignature()[x][y].getGreen();
				int b1 = signature.getSignature()[x][y].getBlue();
				int r2 = signatureOther.getSignature()[x][y].getRed();
				int g2 = signatureOther.getSignature()[x][y].getGreen();
				int b2 = signatureOther.getSignature()[x][y].getBlue();
				double tempDist = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
				dist += tempDist;
				
			}
		}
		return dist;
		
	}
	
	public static ImageSignature calcSignature(RenderedImage image) {
		
		Color[][] signature = new Color[5][5];
		
		float[] prop = new float[]{1f / 10f, 3f / 10f, 5f / 10f, 7f / 10f, 9f / 10f};
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				signature[x][y] = averageAround(image, prop[x], prop[y]);
			}
		}
		return new ImageSignature(signature);
		
	}
	
	private static Color averageAround(RenderedImage image, double px, double py) {
		
		DataBuffer imgData = image.getData(new Rectangle(image.getSampleModel().getWidth(), image.getSampleModel().getHeight())).getDataBuffer();
		
		double[] pixel = new double[3];
		double[] accum = new double[3];
		
		int sampleSize = 15;
		int numPixels = 0;
		
		try {
			for (double x = px * image.getWidth() - sampleSize; x < px * image.getWidth() + sampleSize; x++) {
				for (double y = py * image.getHeight() - sampleSize; y < py * image.getHeight() + sampleSize; y++) {
					
					image.getSampleModel().getPixel((int) x, (int) y, pixel, imgData);
					accum[0] += pixel[0];
					accum[1] += pixel[1];
					accum[2] += pixel[2];
					numPixels++;
					
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		accum[0] /= numPixels;
		accum[1] /= numPixels;
		accum[2] /= numPixels;
		
		return new Color((int) accum[0], (int) accum[1], (int) accum[2]);
		
	}
	
	public List<Map.Entry<String, Double>> checkSimilarity(File thumbnail) throws IOException {
		return checkSimilarity(ImageUtils.calcImageSignature(thumbnail));
	}
	
	public List<Map.Entry<String, Double>> checkSimilarity(ImageSignature signature) {
		
		ConcurrentHashMap<String, Double> results = new ConcurrentHashMap<>();
		
		Set<Map.Entry<String, ImageSignature>> signatures = mDBHandler.getAllSignatures();
		
		signatures.stream().parallel().filter(Objects::nonNull).forEach(entry -> {
			double result = calcDistance(signature, entry.getValue());
			if (result <= 1000) results.put(entry.getKey(), result);
		});
		
		return Utils.sortByValue(results);
		
	}
	
	public static class ImageSignature implements Serializable {
		
		private final Color[][] mSignature;
		
		public ImageSignature(Color[][] signature) {
			
			mSignature = signature;
			if (signature[0].length != 5 || signature[1].length != 5 || signature[2].length != 5 || signature[3].length != 5 || signature[4].length != 5)
				throw new InvalidParameterException("Signature must contain 25 color elements");
			
		}
		
		public ImageSignature(List<List<String>> signature) {
			
			mSignature = new Color[5][5];
			
			for (int x = 0; x < 5; x++) {
				List<String> data = signature.get(x);
				for (int y = 0; y < 5; y++) {
					mSignature[x][y] = ImageUtils.hex2Rgb(data.get(y));
				}
			}
			
		}
		
		public Color[][] getSignature() {
			
			return mSignature;
		}
		
		/**
		 * This converts from a 2D array of Color objects to a 2D List of colour hex String's
		 *
		 * @return Signature in List<List<String>> format, with the String being the Color in hex format
		 */
		public List<List<String>> convertDataStructure() {
			
			List<List<String>> output = new ArrayList<>();
			
			for (Color[] subArr : mSignature) {
				
				List<String> subOut = new ArrayList<>();
				output.add(subOut);
				
				for (Color color : subArr) {
					subOut.add(ImageUtils.colorToHex(color));
				}
				
			}
			
			return output;
			
		}
		
	}
	
}
