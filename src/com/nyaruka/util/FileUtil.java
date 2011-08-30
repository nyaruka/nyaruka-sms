package com.nyaruka.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class FileUtil {

	/**
	 * Reads the contents of a file into a string
	 */
	public static String slurpFile(File file)  {
		try {
			InputStream is = new FileInputStream(file);
			return new Scanner(is).useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
