/*
 *     UtilsTest
 *     Last Modified: 2021-06-18, 7:24 p.m.
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

package ca.bigcattech.utils;

import ca.bigcattech.MediaDB.utils.Utils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {
	
	@Test
	void getFileChecksum() throws IOException, NoSuchAlgorithmException {
		
		//Creating the file
		File file = new File("test.txt");
		file.createNewFile();
		
		//Write Hello World to the file
		FileWriter writer = new FileWriter(file);
		writer.write("Hello World!");
		writer.flush();
		writer.close();
		
		//Check values
		final String TEST_MD5_HASH = "ed076287532e86365e841e92bfc50d8c";
		final String TEST_SHA_256_HASH = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069";
		
		//Actually test
		MessageDigest digest = MessageDigest.getInstance("MD5");
		assertEquals(Utils.getFileChecksum(digest, file), TEST_MD5_HASH);
		digest = MessageDigest.getInstance("SHA-256");
		assertEquals(Utils.getFileChecksum(digest, file), TEST_SHA_256_HASH);
		
	}
	
}