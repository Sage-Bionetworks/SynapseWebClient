package org.sagebionetworks.web.client.widget.entity;

import java.io.File;
import java.io.IOException;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;


/**
 * Abstraction for a helper that uploads/reads wiki pages' markdown
 * @author hso
 *
 */
public interface FileHandleZipHelper {

	/**
	 * Zips up content into a file.
	 * @param markdown
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public File zipUp(String content, String fileName) throws IOException;
	
	/**
	 * Gets the object from S3 and reads content into a string.
	 * @param handle
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public String getAndReadContent(S3FileHandle handle, String fileName) throws IOException;
	
	public FileHandle uploadMarkdown(String markdown, String wikiPageId) throws IOException, RestServiceException;
	public String getMarkdownAsString(String fileHandleId, String wikiPageId) throws IOException, RestServiceException;
}
