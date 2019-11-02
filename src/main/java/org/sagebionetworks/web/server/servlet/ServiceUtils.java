package org.sagebionetworks.web.server.servlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ServiceUtils {
	public static void writeToFile(File temp, InputStream stream, final long maxAttachmentSizeBytes) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp, false));
		try {
			long size = 0;
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = stream.read(buffer)) > 0) {
				out.write(buffer, 0, length);
				size += length;
				if (size > maxAttachmentSizeBytes)
					throw new IllegalArgumentException("File size exceeds the limit of " + maxAttachmentSizeBytes + " MB for attachments");
			}
		} catch (Throwable e) {
			// if is any errors delete the tmp file
			if (out != null) {
				out.close();
			}
			temp.delete();
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Write the data in the passed input stream to a temp file.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static File writeToTempFile(InputStream stream, final long maxAttachmentSizeBytes) throws IOException {
		File temp = File.createTempFile("tempUploadedFile", ".tmp");
		writeToFile(temp, stream, maxAttachmentSizeBytes);
		return temp;
	}
}
