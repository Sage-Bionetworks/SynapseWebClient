package org.sagebionetworks.web.client.widget.entity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.inject.Inject;

public class WikiMarkdownHelperImpl implements WikiMarkdownHelper {
	private AmazonS3Client s3Client;
	private SynapseClientImpl synapseClient;
	
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
	
	@Inject
	public WikiMarkdownHelperImpl(AmazonS3Client s3Client, SynapseClientImpl synapseClient) {
		this.s3Client = s3Client;
		this.synapseClient = synapseClient;
	}
	
	@Override
	public FileHandle uploadMarkdown(String markdown, String wikiPageId) throws IOException, RestServiceException {
		File tempFile = File.createTempFile(wikiPageId + "_markdown", ".tmp");
		if(markdown != null) {
			FileUtils.writeByteArrayToFile(tempFile, markdown.getBytes());
		} else {
			// When creating a wiki for the first time, markdown content doesn't exist
			// Uploaded file should be empty
			byte[] emptyByteArray = new byte[0];
			FileUtils.writeByteArrayToFile(tempFile, emptyByteArray);
		}
		String contentType = guessContentTypeFromStream(tempFile);
		return synapseClient.uploadFile(tempFile, contentType);
	}

	/**
	 * Guesses the content type of the file
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String guessContentTypeFromStream(File file)	throws FileNotFoundException, IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		try{
			// Let java guess from the stream.
			String contentType = URLConnection.guessContentTypeFromStream(is);
			// If Java fails then set the content type to be octet-stream
			if(contentType == null){
				contentType = APPLICATION_OCTET_STREAM;
			}
			return contentType;
		}finally{
			is.close();
		}
	}
	
	@Override
	public String getMarkdown(String fileHandleId, String wikiPageId) throws IOException, RestServiceException {
		S3FileHandle markdownHandle = (S3FileHandle) synapseClient.getFileHandle(fileHandleId);
		File tempFile = File.createTempFile(wikiPageId + "_markdown", ".tmp");
		// Retrieve uploaded markdown
		s3Client.getObject(new GetObjectRequest(markdownHandle.getBucketName(), 
				markdownHandle.getKey()), tempFile);
		// Read the file as a string
		return FileUtils.readFileToString(tempFile, "UTF-8");
	}

}
