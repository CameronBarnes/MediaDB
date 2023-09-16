/*
 *     ThumbnailHandler
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

package ca.bigcattech.MediaDB.image;

import ca.bigcattech.MediaDB.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ThumbnailHandler {
	
	private ThumbnailHandler() {
	
	}

    public static boolean generateVideoThumbnail(Path inputPath, Path outputPath) {

        // Check that the input file exists and is not a directory
        if (!Files.exists(inputPath) || Files.isDirectory(inputPath)) {
            System.out.println("Error: Input file does not exist or is a directory");
            return false;
        }

        // Check that the output file does not already exist
        if (Files.exists(outputPath)) {
            System.out.println("Error: Output file already exists");
            return false;
        }

        // Use the ffmpeg command line tool to extract the thumbnail image
        // from the specified video file and save it to the specified output file
        String command = "";
        if (new File("ffmpeg.exe").exists()) {
            command = "./ffmpeg.exe -ss 3 -i " + "\"" + inputPath + "\"" + " -loglevel quiet -vframes 120 -r 60 -s 300x280 " + "\"" + outputPath + "\"";
        } else {
            command = "ffmpeg -ss 3 -i " + "\"" + inputPath + "\"" + " -loglevel quiet -vframes 120 -r 60 -s 300x280 " + "\"" + outputPath + "\"";
        }

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);

            // Read the output from the command
			/*BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}*/

            // Wait for the command to finish
            //System.out.println("Not Here!");
            process.waitFor();
            //System.out.println("Here!");

            // Return true if the command was successful
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
			e.printStackTrace();
            return false;
        }
    }

    public static boolean generateVideoThumbnail(String inputFile, String outputFile) {
        // Convert the input and output file paths from String to Path
        Path inputPath = Paths.get(inputFile);
        Path outputPath = Paths.get(outputFile);

        // Call the generateThumbnail method that accepts Path objects
        return generateVideoThumbnail(inputPath, outputPath);
    }

    public static void createImageThumbnail(File image, File outputFile) {
		
		try {
			ImageUtils.ImageData imageData = ImageUtils.loadImage(image);
			BufferedImage thumbnail;
			thumbnail = ImageUtils.thumbResizeImage(imageData.getBufferedImage(), 300, 300);
			ImageUtils.writeOutBufferedImage(thumbnail, outputFile, imageData.getFormat());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
