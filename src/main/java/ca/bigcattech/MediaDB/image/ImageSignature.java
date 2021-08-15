/*
 *     ImageSignature
 *     Last Modified: 2021-08-14, 5:56 p.m.
 *     Copyright (C) 2021-08-14, 5:57 p.m.  CameronBarnes
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

import java.awt.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class ImageSignature {
	
	private String mHash;
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
	
	public ImageSignature(String hash, Color[][] signature) {
		
		mHash = hash;
		mSignature = signature;
		if (signature[0].length != 5 || signature[1].length != 5 || signature[2].length != 5 || signature[3].length != 5 || signature[4].length != 5)
			throw new InvalidParameterException("Signature must contain 25 color elements");
		
	}
	
	public ImageSignature(String hash, List<List<String>> signature) {
		
		mHash = hash;
		mSignature = new Color[5][5];
		
		for (int x = 0; x < 5; x++) {
			List<String> data = signature.get(x);
			for (int y = 0; y < 5; y++) {
				mSignature[x][y] = ImageUtils.hex2Rgb(data.get(y));
			}
		}
		
	}
	
	public void setHash(String hash) {
		
		mHash = hash;
	}
	
	public String getHash() {
		
		return mHash;
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
