package com.app.sqlite.helper;
import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.io.IOUtilities;

/**
 * Helper classes associated with 
 * @author samkirton
 */
public final class ResourceHelper {
	/**
	 * Get the contents of a file as a string
	 * @param	inputStream	The inputStream of the file
	 * @return	The file contents as a string
	 */
	public static String getFileContents(InputStream inputStream) {
		return new String(getFileByteContents(inputStream));
	}
	
	/**
	 * Get the contents of a file as a byte[]
	 * @param	inputStream	The inputStream of the file
	 * @return	The file contents as a byte
	 */
	public static byte[] getFileByteContents(InputStream inputStream) {
		byte[] content = streamToBytes(inputStream);
		closeInputStream(inputStream);
		return content;
	}
	
	/**
	 * Convert an input stream to a byte array
	 * @param	inputStream	The input stream to convert to a byte array
	 * @return	The input stream as a byte array
	 */
	public static byte[] streamToBytes(InputStream inputStream) {
		byte[] content = null;
		
		try {
			content = IOUtilities.streamToBytes(inputStream);
		} catch (IOException e) { }
		
		return content;
	}

	
	/**
	 * Close the InputStream and set the object to NULL to get the garbage
	 * collector to clean it up as soon as possible.
	 * @param	inputStream	The InputStream reference to close
	 */
	public static void closeInputStream(InputStream inputStream) {
		if (inputStream instanceof InputStream) {
			try {
				inputStream.close();
			} catch (IOException e) { }
			inputStream = null;
		}
	}
}
