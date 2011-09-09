package com.nyaruka.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Scanner;

public class FileUtil {

	public static String slurpStream(InputStream is) {
		return new Scanner(is).useDelimiter("\\A").next();		
	}
	
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
	
	public static void writeFile(File file, String contents) {		
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(contents);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void copyFile(File sourceFile, File destFile){
		try {
		    if(!destFile.exists()) {
		        destFile.createNewFile();
		    }
		
		    FileChannel source = null;
		    FileChannel destination = null;
		
		    try {
		        source = new FileInputStream(sourceFile).getChannel();
		        destination = new FileOutputStream(destFile).getChannel();
		        destination.transferFrom(source, 0, source.size());
		    }
		    finally {
		        if(source != null) {
		            source.close();
		        }
		        if(destination != null) {
		            destination.close();
		        }
		    }
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}
}
