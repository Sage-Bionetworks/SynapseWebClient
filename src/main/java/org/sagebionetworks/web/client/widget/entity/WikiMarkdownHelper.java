package org.sagebionetworks.web.client.widget.entity;

import java.io.IOException;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;


/**
 * Abstraction for a helper that uploads/reads wiki pages' markdown
 * @author hso
 *
 */
public interface WikiMarkdownHelper {
	/**
	 * Uploads the given markdown to S3.
	 * 
	 * @param markdown
	 * @param wikiPageId
	 * @return
	 * @throws IOException
	 * @throws RestServiceException
	 */
	public FileHandle uploadMarkdown(String markdown, String wikiPageId) throws IOException, RestServiceException ;
	
	/**
	 * Retrieves the markdown content from the file handle. Reads and returns it as a string.
	 * @param fileHandleId
	 * @param wikiPageId
	 * @return
	 * @throws IOException
	 * @throws RestServiceException
	 */
	public String getMarkdownAsString(String fileHandleId, String wikiPageId) throws IOException, RestServiceException ;
}
