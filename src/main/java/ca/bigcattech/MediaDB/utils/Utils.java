/*
 *     Utils
 *     Last Modified: 2023-09-16, 3:13 p.m.
 *     Copyright (C) 2023-09-16, 3:13 p.m.  CameronBarnes
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

package ca.bigcattech.MediaDB.utils;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Utils {
	
	private Utils() {
	
	}

    public static boolean stringArrNotContainsStrFromArray(String[] main, String[] blacklist) {

        for (String m : main) {
            for (String b : blacklist) {
                if (m.equals(b))
                    return false;
            }
        }

        return true;
		
	}

    public static String convertArrayToGrammaticallyAcceptableList(Object[] arr, boolean andOr, boolean period) {
		
		if (arr.length == 0) return period ? "." : "";
		else if (arr.length == 1) return arr[0].toString() + (period ? "." : "");
		else if (arr.length == 2) return arr[0].toString() + (andOr ? " and " : " or ") + arr[1].toString() + (period ? "." : "");
		else {
			StringBuilder out = new StringBuilder(arr[0] + ",");
			for (int i = 1; i < arr.length - 1; i++) {
				out.append(' ').append(arr[i].toString()).append(',');
			}
			out.append(andOr ? " and " : " or ").append(arr[arr.length - 1]);
			if (period)out.append('.');
			return out.toString();
		}
		
	}
	
	public static String getFirstSubstring(String text, String split) {
		
		try {
			return text.split(split)[0];
		}
		catch (Exception e) {
			System.err.printf("Input text is: %s Split regex is: %s%n", text, split);
			throw e;
		}
		
	}
	
	public static String getLastSubstring(String text, String split) {
		
		int num = text.lastIndexOf(split);
		return num == -1 ? text : text.substring(++num);
	}
	
	public static Character getLastChar(String string) {
		
		if (string.isEmpty()) return null;
		return string.charAt(string.length() - 1);
	}
	
	public static String removeLastSubstring(String text, String split) {
		
		String[] temp = text.split(split);
		if (temp.length == 1) return text;
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < temp.length - 1; i++) {
			out.append(temp[i]);
		}
		
		return out.toString();
		
	}
	
	public static String longToStrTime(long totalMillis) {
		
		long totalSecs = totalMillis / 1000;
		long hours = totalSecs / 3600;
		long minutes = (totalSecs % 3600) / 60;
		long seconds = totalSecs % 60;
		
		if (hours > 0) return String.format(" %02dh %02dm %02ds ", hours, minutes, seconds);
		else if (minutes > 0) return String.format(" %02dm %02ds ", minutes, seconds);
		else return String.format(" %02ds ", seconds);
		
	}
	
	public static String getFileChecksum(MessageDigest digest, File file) throws IOException {
		
		if (file.isDirectory()) return null;
		//Get file input stream for reading the file content
		try (FileInputStream fis = new FileInputStream(file)) {
			
			//Create byte array to read data in chunks
			byte[] byteArray = new byte[1024];
			int bytesCount;
			
			//Read file data and update in message digest
			while ((bytesCount = fis.read(byteArray)) != -1) {
				digest.update(byteArray, 0, bytesCount);
			}
			
		}
		
		//Get the hash's bytes
		byte[] bytes = digest.digest();
		
		//This bytes[] has bytes in decimal format;
		//Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for (byte aByte : bytes) {
			sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
		}
		
		//return complete hash
		return sb.toString();
		
	}
	
	public static <T> JComboBox<T> comboBoxFromArray(List<T> array) {
		
		JComboBox<T> comboBox = new JComboBox<>();
		for (T thing : array)	comboBox.addItem(thing);
		return comboBox;
		
	}
	
	public static <T> void addArrayToComboBox(JComboBox<T> comboBox, List<T> array) {
		
		for (T thing: array) comboBox.addItem(thing);
	}
	
	public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> sortByValue(Map<K, V> map) {
		
		List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());
		
		return list;
	}
	
}
